package com.adaptor.deadrecall.mixin;

import com.adaptor.deadrecall.bootstrap.RemnantContainerSafetyCutover;
import com.adaptor.deadrecall.bootstrap.VanillaTweaksCutover;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Selectively disables legacy shared Mixins after their feature owner cuts over. */
public final class DeadRecallLegacyMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) { }
    @Override public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".ShulkerBoxBlockEntityMixin")
                || mixinClassName.endsWith(".ShulkerBoxSlotMixin")) {
            return !RemnantContainerSafetyCutover.usesExternalAuthority();
        }
        if (mixinClassName.endsWith(".StructureTemplateMixin")) {
            return !VanillaTweaksCutover.usesExternalBookshelfAuthority();
        }
        return true;
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) { }
    @Override public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) { }
}
