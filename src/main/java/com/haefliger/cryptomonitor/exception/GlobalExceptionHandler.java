package com.haefliger.cryptomonitor.exception;

import com.haefliger.cryptomonitor.exception.dto.ApiErrorDetailResponse;
import com.haefliger.cryptomonitor.exception.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.ZonedDateTime;
import java.util.List;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<ConstraintViolationException> {

    @Context UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        List<ApiErrorDetailResponse> errors =
                ex.getConstraintViolations().stream()
                        .map(violation -> new ApiErrorDetailResponse(violation.getMessage()))
                        .toList();

        ApiErrorResponse response =
                new ApiErrorResponse(
                        ZonedDateTime.now(),
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        "Bad Request",
                        errors,
                        uriInfo.getAbsolutePath().getPath());

        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }
}
