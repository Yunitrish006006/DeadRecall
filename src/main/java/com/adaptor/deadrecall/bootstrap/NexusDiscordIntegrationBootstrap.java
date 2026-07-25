package com.adaptor.deadrecall.bootstrap;

import com.adaptor.deadrecall.Deadrecall;
import com.adaptor.deadrecall.DiscordBridge;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Installs the compatibility bundle's Discord notification adapter into the
 * external Nexus seam without making either feature a compile-time dependency
 * of the other.
 */
public final class NexusDiscordIntegrationBootstrap {
    private static final String INTEGRATIONS_CLASS = "dev.totem.nexus.space.NexusOptionalIntegrations";
    private static final String LISTENER_CLASS = INTEGRATIONS_CLASS + "$Listener";

    private NexusDiscordIntegrationBootstrap() {
    }

    public static void installIfExternalAuthority() {
        if (!NexusCutover.usesExternalAuthority()) {
            return;
        }

        try {
            ClassLoader loader = NexusDiscordIntegrationBootstrap.class.getClassLoader();
            Class<?> listenerType = Class.forName(LISTENER_CLASS, true, loader);
            Class<?> integrations = Class.forName(INTEGRATIONS_CLASS, true, loader);
            Object listener = Proxy.newProxyInstance(
                    loader,
                    new Class<?>[]{listenerType},
                    new DiscordNotificationListener()
            );
            integrations.getMethod("install", listenerType).invoke(null, listener);
            Deadrecall.LOGGER.info("Installed Nexus optional notification adapter through the compatibility bundle");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not install the Nexus optional notification adapter", exception);
        }
    }

    private static final class DiscordNotificationListener implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                return switch (methodName) {
                    case "toString" -> "DeadRecall Nexus Discord notification adapter";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> null;
                };
            }

            if (arguments == null) {
                throw new IllegalStateException("Nexus notification callback has no arguments: " + methodName);
            }
            switch (methodName) {
                case "deathBackpackRecovered" -> DiscordBridge.sendDeathBackpackRecovered((String) arguments[0]);
                case "publicSpaceUnitUpdate" -> DiscordBridge.sendSpaceUnitPublicUpdate(
                        (String) arguments[0], (String) arguments[1]);
                case "adminAction" -> DiscordBridge.sendAdminAction(
                        (String) arguments[0], (String) arguments[1], (String) arguments[2]);
                default -> throw new IllegalStateException("Unknown Nexus notification callback: " + methodName);
            }
            return null;
        }
    }
}
