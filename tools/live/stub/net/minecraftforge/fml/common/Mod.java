package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * E1 compile stub: shape-only Forge 1.16.5-36.2.42 API
 * (javafmllanguage jar, never obfuscated). Never runs (compile classpath
 * only). Member {@code value} is pinned by tools/run-live.sh (E3) —
 * drift fails loudly. Retention RUNTIME is load-bearing (mirrors the
 * real annotation): the mod list discovers mods through visible
 * annotations only.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();
}
