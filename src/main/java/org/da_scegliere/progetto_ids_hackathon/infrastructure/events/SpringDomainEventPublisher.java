package org.da_scegliere.progetto_ids_hackathon.infrastructure.events;

import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.ports.events.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
