package com.adaptor.deadrecall;

import com.adaptor.deadrecall.migration.LegacyAliasHandoffVerifier;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Final compatibility transition host.
 *
 * <p>TotemCore owns the retained {@code deadrecall:*} item aliases. DeadRecall
 * now verifies that handoff and packages the exact transition module graph; it
 * no longer registers gameplay items or legacy aliases itself.</p>
 */
public final class Deadrecall implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DeadRecall");

    @Override
    public void onInitialize() {
        LegacyAliasHandoffVerifier.verify();
        LOGGER.info(
                "DeadRecall transition host verified {} TotemCore-owned legacy item aliases; "
                        + "this world can remain decode-safe after the DeadRecall host is removed",
                LegacyAliasHandoffVerifier.mappingCount()
        );
    }
}
