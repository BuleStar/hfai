package com.hf.webflux.hfai.event;

import com.hf.webflux.hfai.service.BarDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BarDataEventListener {

    @Autowired
    private BarDataService barDataService;

    @Async("asyncExecutor")
    @EventListener
    public void handleBarDataEvent(BarDataEvent event) {
//        log.info("Received BarData event - {}", event.getMessage());
        // Here you can add reactive processing if needed
        Mono.just(event.getBarData())
                .doOnNext(message -> {
                    // Process the event asynchronously if needed
                    barDataService.saveOrUpdateByTime(message);
                })
                .subscribe();
    }
}
