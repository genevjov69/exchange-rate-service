package io.genevjov.ces.client.dto;

public record ExchangerateHostErrorResponse(
        Integer code,
        String type,
        String info) {
}
