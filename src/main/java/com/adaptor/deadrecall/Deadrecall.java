package com.adaptor.deadrecall;

import com.adaptor.deadrecall.migration.DeadRecallLegacyItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compatibility-bundle host.
 *
 * <p>DeadRecall owns only legacy identifier migration. Gameplay is provided by
 * the exact Totem module graph nested into the release artifact.</p>
 */
public final class Deadrecall implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DeadRecall");

    @Override
    public void onInitialize() {
        DeadRecallLegacyItems.register();
        LOGGER.info(
                "DeadRecall compatibility host initialized with {} legacy item mappings",
                DeadRecallLegacyItems.mappingCount()
        );
    }
}
