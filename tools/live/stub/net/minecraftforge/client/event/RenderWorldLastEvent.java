package net.minecraftforge.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.eventbus.api.Event;

/**
 * Shape-only compile stub for Forge 1.16.5 RenderWorldLastEvent.
 * Never runs (compile classpath only). Forge class, never obfuscated:
 * presence-pinned against the provisioned 36.2.42 universal by
 * tools/run-live.sh (getPartialTicks/getMatrixStack/getProjectionMatrix
 * measured there — the 1.16.5 event carries the MatrixStack whose top
 * the renderer uploads, the 1.12 float-only shape does not port).
 */
public class RenderWorldLastEvent extends Event {
    public float getPartialTicks() {
        return 0;
    }

    public MatrixStack getMatrixStack() {
        return null;
    }

    public Matrix4f getProjectionMatrix() {
        return null;
    }
}
