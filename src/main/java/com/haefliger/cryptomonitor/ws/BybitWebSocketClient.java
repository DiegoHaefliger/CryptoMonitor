package com.haefliger.cryptomonitor.ws;

import com.google.gson.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
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
public class BybitWebSocketClient extends WebSocketClient {

    private final Map<String, List<String>> symbolIntervals;
    private final PriceHandler handler;
    private final Gson gson = new Gson();

    public BybitWebSocketClient(Map<String, List<String>> symbolIntervals, PriceHandler handler) throws Exception {
        super(new URI(WEBSOCKET_URL_LINEAR));
        this.symbolIntervals = Collections.unmodifiableMap(symbolIntervals);
        this.handler = handler;
        loadHistoricalData();
    }

    private void loadHistoricalData() {
        symbolIntervals.forEach((symbol, intervals) -> intervals.forEach(interval -> {
            try {
                fetchHistoricalKlines(symbol, interval);
            } catch (Exception e) {
                log.error("Failed to fetch historical klines for {} [{}]", symbol, interval, e);
            }
        }));
    }

    private void fetchHistoricalKlines(String symbol, String interval) throws Exception {
        String urlStr = String.format("%s?category=linear&symbol=%s&interval=%s&limit=%s",
                WEBSOCKET_URL_KLINE, symbol, interval, LIMIT_RECORDS);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String json = reader.lines().collect(Collectors.joining());
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray data = obj.getAsJsonObject("result").getAsJsonArray("list");
            for (JsonElement el : data) {
                JsonArray kline = el.getAsJsonArray();
                double close = kline.get(4).getAsDouble();
                long timestamp = kline.get(0).getAsLong();
                handler.addPrice(symbol, interval, close, Instant.ofEpochMilli(timestamp), false);
            }
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket Opened");
        List<String> args = symbolIntervals.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(interval -> KLINE_PREFIX + interval + "." + entry.getKey()))
                .toList();
        String subscribeMsg = gson.toJson(Map.of(
                OP, SUBSCRIBE,
                ARGS, args
        ));
        send(subscribeMsg);
    }

    @Override
    public void onMessage(String message) {
        JsonObject obj = JsonParser.parseString(message).getAsJsonObject();
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
        if (data.has("confirm") && data.get("confirm").getAsBoolean()) {
            double close = data.get("close").getAsDouble();
            long timestamp = data.get("timestamp").getAsLong();
            handler.addPrice(symbol, interval, close, Instant.ofEpochMilli(timestamp), true);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket Closed: {}", reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket Error", ex);
    }
}
