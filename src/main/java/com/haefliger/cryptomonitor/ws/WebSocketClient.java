package com.haefliger.cryptomonitor.ws;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.haefliger.cryptomonitor.utils.Constants.*;

/**
 * Author diego-haefliger
 * Date 14/06/25
 */
@Slf4j
@Getter
@Setter
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

    private ConnectionListener connectionListener;
    private Map<String, List<String>> symbolIntervals;
    private final PriceHandler handler;
    private final Gson gson = new Gson();

    public static final String TOPIC = "topic";
    public static final String KLINE_PREFIX = "kline.";
    public static final String OP = "op";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String ARGS = "args";

    public WebSocketClient(Map<String, List<String>> symbolIntervals, PriceHandler handler) throws Exception {
        super(new URI(WEBSOCKET_URL_LINEAR));
        this.symbolIntervals = Collections.unmodifiableMap(symbolIntervals);
        this.handler = handler;
        loadHistoricalData();
    }

    private void loadHistoricalData() {

        // por segurança, limpa os dados antigos antes de carregar novos
        if (handler instanceof MultiSymbolPriceHandler multiHandler) {
            multiHandler.clearAll();
        }

        symbolIntervals.forEach((symbol, intervals) -> intervals.forEach(interval -> {
            try {
                fetchHistoricalKlines(symbol, interval);
            } catch (Exception e) {
                log.error("Falha ao buscar klines históricos para {} [{}]", symbol, interval, e);
            }
        }));
    }

    private void fetchHistoricalKlines(String symbol, String interval) throws Exception {
        String urlStr = String.format(WEBSOCKET_URL_KLINE, symbol, interval, LIMIT_RECORDS);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String json = reader.lines().collect(Collectors.joining());
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray data = obj.getAsJsonObject("result").getAsJsonArray("list");
            List<PricePoint> pricePoints = new java.util.ArrayList<>();
            for (JsonElement el : data) {
                JsonArray kline = el.getAsJsonArray();
                BigDecimal close = kline.get(4).getAsBigDecimal(); // Preço de fechamento
                long timestamp = kline.get(0).getAsLong(); // Timestamp em milissegundos
                pricePoints.add(new PricePoint(close, Instant.ofEpochMilli(timestamp)));
            }
            handler.addPricesHistorical(symbol, interval, pricePoints);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket Aberto");
        List<String> args = symbolIntervals.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(interval -> KLINE_PREFIX + interval + "." + entry.getKey()))
                .toList();
        String subscribeMsg = gson.toJson(Map.of(OP, SUBSCRIBE, ARGS, args));
        send(subscribeMsg);
    }

    @Override
    public void onMessage(String message) {
        JsonObject obj = JsonParser.parseString(message).getAsJsonObject();

        // Verifica se a mensagem contém o campo "topic" e se é um tópico de Kline
        if (obj.has(TOPIC) && obj.get(TOPIC).getAsString().startsWith(KLINE_PREFIX)) {
            String[] parts = obj.get(TOPIC).getAsString().split("\\.");
            String interval = parts[1];
            String symbol = parts[2];
            JsonElement dataElement = obj.get("data");
            if (dataElement.isJsonObject()) {
                handleKlineData(symbol, interval, dataElement.getAsJsonObject());
            } else if (dataElement.isJsonArray()) {
                dataElement.getAsJsonArray().forEach(element ->
                        handleKlineData(symbol, interval, element.getAsJsonObject()));
            }
        }
    }

    private void handleKlineData(String symbol, String interval, JsonObject data) {
        // Verifica se fechou o timestamp
        if (data.has("confirm") && data.get("confirm").getAsBoolean()) {
            BigDecimal close = data.get("close").getAsBigDecimal();
            long timestamp = data.get("timestamp").getAsLong();
            handler.addPrice(symbol, interval, close, Instant.ofEpochMilli(timestamp));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket Fechado: {}", reason);
        if (connectionListener != null) {
            connectionListener.onConnectionLost();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("Erro no WebSocket", ex);
        if (connectionListener != null) {
            connectionListener.onConnectionLost();
        }
    }

    public void updateSubscriptions(Map<String, List<String>> newSymbolIntervals) {
        // Desinscreve dos tópicos antigos
        List<String> oldArgs = symbolIntervals.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(interval -> KLINE_PREFIX + interval + "." + entry.getKey()))
                .toList();
        String unsubscribeMsg = gson.toJson(Map.of(OP, UNSUBSCRIBE, ARGS, oldArgs));
        send(unsubscribeMsg);

        // Inscreve nos novos tópicos
        List<String> newArgs = newSymbolIntervals.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(interval -> KLINE_PREFIX + interval + "." + entry.getKey()))
                .toList();
        String subscribeMsg = gson.toJson(Map.of(OP, SUBSCRIBE, ARGS, newArgs));
        send(subscribeMsg);

        // Atualiza o atributo interno para refletir os novos símbolos/intervalos
        this.symbolIntervals = Collections.unmodifiableMap(new HashMap<>(newSymbolIntervals));

        // Baixa o histórico para novos símbolos/intervalos
        loadHistoricalData();
    }

}
