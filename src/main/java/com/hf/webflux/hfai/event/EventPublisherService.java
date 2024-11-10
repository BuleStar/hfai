package com.hf.webflux.hfai.event;

import com.hf.webflux.hfai.entity.BarData;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisherService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishCustomEvent(final String message) {
        CustomEvent customEvent = new CustomEvent(this, message);
        applicationEventPublisher.publishEvent(customEvent);
    }

    public void BarDataEvent(final BarData message) {
        BarDataEvent barDataEvent = new BarDataEvent(this, message);
        applicationEventPublisher.publishEvent(barDataEvent);
    }
}
