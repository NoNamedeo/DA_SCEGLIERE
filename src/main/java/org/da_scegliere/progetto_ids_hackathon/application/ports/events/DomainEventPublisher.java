package org.da_scegliere.progetto_ids_hackathon.application.ports.events;

public interface DomainEventPublisher {

    void publish(Object event);
}
