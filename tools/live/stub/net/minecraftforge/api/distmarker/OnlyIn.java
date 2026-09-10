package net.minecraftforge.api.distmarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom entity compile stub: shape-only Forge 1.16.5-36.2.42 API
 * (forgespi 3.2.0 jar, never obfuscated). Never runs (compile classpath
 * only). Measured with javap -v against the provisioned forgespi jar:
 * RUNTIME retention, TYPE/FIELD/METHOD/CONSTRUCTOR targets,
 * {@code value()} plus a defaulted {@code _interface()} — the default
 * is load-bearing (without it every {@code @OnlyIn} use must name the
 * member). The runtime cleaner strips annotated members on the wrong
 * dist (the 1.16.5 {@code @SideOnly}); pinned in tools/run-live.sh —
 * drift fails loudly.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
        ElementType.CONSTRUCTOR})
public @interface OnlyIn {
    Dist value();

    Class<?> _interface() default Object.class;
}
