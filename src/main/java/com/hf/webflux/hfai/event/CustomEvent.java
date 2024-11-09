package com.hf.webflux.hfai.event;

import org.springframework.context.ApplicationEvent;

public class CustomEvent extends ApplicationEvent {
    private final String message;

    public CustomEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

