package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alpha432.oyvey.event.impl.render.Render2DEvent;
import me.alpha432.oyvey.event.system.Subscribe;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Nametags extends Module {

    public Nametags() {
        super("Nametags", "Renders detailed nametags above players.", Category.RENDER);
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        GuiGraphics graphics = event.getContext();
        float delta = event.getDelta();

        if (mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player || player.isInvisible()) continue;
            renderNametag(graphics, player, delta);
        }
    }

    private void renderNametag(GuiGraphics graphics, Player player, float delta) {
        String name = player.getGameProfile().getName();
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPct = Mth.clamp(health / maxHealth, 0f, 1f);

        // Layout constants
        int pad = 5;
        int headSize = 18;
        int barHeight = 3;

        // World to screen projection
        double wx = Mth.lerp(delta, player.xOld, player.getX());
        double wy = Mth.lerp(delta, player.yOld, player.getY()) + player.getBbHeight() + 0.3;
        double wz = Mth.lerp(delta, player.zOld, player.getZ());

        int[] screen = worldToScreen(wx, wy, wz);
        if (screen == null) return;

        int tagW = Math.max(mc.font.width(name) + headSize + pad * 3, 60);
        int tagH = headSize + pad * 2 + barHeight + 2;

        int sx = screen[0] - tagW / 2;
        int sy = screen[1] - tagH / 2;

        // Background
        graphics.fill(sx, sy, sx + tagW, sy + tagH, 0xCC0D0D0D);

        // Name & Head
        drawPlayerHead(graphics, player, sx + pad, sy + pad, headSize);
        graphics.drawString(mc.font, name, sx + headSize + pad * 2, sy + pad + (headSize / 2 - 4), 0xFFFFFFFF, true);

        // Health Bar
        int barY = sy + headSize + pad + 1;
        graphics.fill(sx + pad, barY, sx + tagW - pad, barY + barHeight, 0xFF2A2A2A);
        graphics.fill(sx + pad, barY, sx + pad + (int)((tagW - pad * 2) * healthPct), barY + barHeight, getHealthColor(healthPct));
    }

    private int getHealthColor(float pct) {
        int r = (int)(255 * (1f - pct));
        int g = (int)(255 * pct);
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    private void drawPlayerHead(GuiGraphics graphics, Player player, int x, int y, int size) {
        // Fix for SkinManager mapping error
        ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        graphics.blit(skin, x, y, size, size, 8f, 8f, 8, 8, 64, 64);
        graphics.blit(skin, x, y, size, size, 40f, 8f, 8, 8, 64, 64);
    }

    private int[] worldToScreen(double wx, double wy, double wz) {
        // Fix for getPosition() -> getPos() mapping
        var cam = mc.gameRenderer.getMainCamera().getPos();

        float dx = (float)(wx - cam.x);
        float dy = (float)(wy - cam.y);
        float dz = (float)(wz - cam.z);

        // Fix for Matrix4f instantiation and transform calls
        Matrix4f view = RenderSystem.getModelViewMatrix();
        Matrix4f proj = RenderSystem.getProjectionMatrix();

        Vector4f pos = new Vector4f(dx, dy, dz, 1f);
        pos.mul(view);
        pos.mul(proj);

        if (pos.w <= 0f) return null;

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int screenX = (int)((ndcX + 1f) / 2f * mc.getWindow().getGuiScaledWidth());
        int screenY = (int)((1f - ndcY) / 2f * mc.getWindow().getGuiScaledHeight());

        return new int[]{ screenX, screenY };
    }
}
