package com.adaptor.deadrecall.core.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Optional compatibility transport for externally owned death-backpack recovery. */
public interface DeathBackpackRecoveryTransport {
    boolean recover(ServerPlayer player, ItemStack deathBackpack);

    static void register(DeathBackpackRecoveryTransport adapter) { Holder.adapter = adapter; }
    static Optional<DeathBackpackRecoveryTransport> current() { return Optional.ofNullable(Holder.adapter); }

    final class Holder {
        private static volatile DeathBackpackRecoveryTransport adapter;
        private Holder() { }
    }
}
