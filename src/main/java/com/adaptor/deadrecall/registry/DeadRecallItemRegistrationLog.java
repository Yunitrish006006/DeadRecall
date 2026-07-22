package com.adaptor.deadrecall.registry;

import com.adaptor.deadrecall.Deadrecall;

/** Logs bundle item registration without loading compatibility item facades. */
public final class DeadRecallItemRegistrationLog {
    private DeadRecallItemRegistrationLog() { }

    public static void register() {
        Deadrecall.LOGGER.info("正在註冊模組物品...");
    }
}
