package com.adaptor.deadrecall.core.api;

import java.util.Optional;

/** Versioned optional transport contract for Discord-facing event delivery. */
public interface DiscordEventTransport {
    void send(String event, String username, String message);

    static void register(DiscordEventTransport adapter) {
        Holder.adapter = adapter;
    }

    static Optional<DiscordEventTransport> current() {
        return Optional.ofNullable(Holder.adapter);
    }

    final class Holder {
        private static volatile DiscordEventTransport adapter;
        private Holder() {
        }
    }
}
