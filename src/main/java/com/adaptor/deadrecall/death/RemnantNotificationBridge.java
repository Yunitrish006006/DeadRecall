package com.adaptor.deadrecall.death;

import com.adaptor.deadrecall.Deadrecall;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Proxy;

/** Installs bundle notifications into Remnant only when the optional module is present. */
public final class RemnantNotificationBridge {
    private RemnantNotificationBridge() { }

    public static void installIfPresent() {
        try {
            Class<?> notifications = Class.forName("dev.totem.remnant.death.DeathBackpackNotifications");
            Object adapter = Proxy.newProxyInstance(RemnantNotificationBridge.class.getClassLoader(), new Class<?>[] {notifications},
                    (proxy, method, arguments) -> {
                        if ("created".equals(method.getName()) && arguments != null && arguments.length == 3) {
                            DeathBackpackCaptureService.notifyCaptureCompleted(
                                    (ServerPlayer) arguments[0], (Integer) arguments[1], (BlockPos) arguments[2]);
                        } else if ("recovered".equals(method.getName()) && arguments != null && arguments.length == 1) {
                            DeathBackpackRecoveryService.notifyRecoveredFromExternalAdapter((ServerPlayer) arguments[0]);
                        }
                        return null;
                    });
            notifications.getMethod("register", notifications).invoke(null, adapter);
        } catch (ClassNotFoundException ignored) {
            // Standalone compatibility bundle has no Remnant notification seam.
        } catch (ReflectiveOperationException exception) {
            Deadrecall.LOGGER.warn("Unable to install Remnant notification bridge", exception);
        }
    }
}
