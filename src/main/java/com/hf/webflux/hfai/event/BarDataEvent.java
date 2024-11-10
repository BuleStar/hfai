package com.hf.webflux.hfai.event;

import com.hf.webflux.hfai.entity.BarData;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BarDataEvent extends ApplicationEvent {
    private final BarData barData;

    public BarDataEvent(Object source, BarData barData) {
        super(source);
        this.barData = barData;
    }
}
