package net.minecraftforge.eventbus.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * E1 compile stub: shape-only eventbus 4.x API (never obfuscated). Never
 * runs (compile classpath only). Presence is pinned by
 * tools/run-live.sh (E3) — drift fails loudly. Retention RUNTIME is
 * load-bearing (mirrors the real annotation): eventbus discovers
 * handlers through RuntimeVisibleAnnotations, so a CLASS-retention stub
 * would register nothing, silently — found live on another bridge,
 * never again silently.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
}
