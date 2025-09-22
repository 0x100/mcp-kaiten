package ru.coder1.mcp.kaiten.client;

import lombok.Getter;

@Getter
public class KaitenHttpException extends RuntimeException {
    private final int status;

    public KaitenHttpException(int status, String message) {
        super(message);
        this.status = status;
    }
}
