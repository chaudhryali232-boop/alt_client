package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alpha432.oyvey.event.impl.render.Render2DEvent;
import me.alpha432.oyvey.event.system.Subscribe; // Added for event system
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
            if (player == mc.player) continue;
            if (player.isInvisible()) continue;

            renderNametag(graphics, player, delta);
        }
    }

    private void renderNametag(GuiGraphics graphics, Player player, float delta) {
        String name = player.getGameProfile().getName();
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPct = Mth.clamp(health / maxHealth, 0f, 1f);

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack offhand = player.getOffhandItem();

        int pad = 5;
        int headSize = 18;
        int iconSize = 16;
        int barHeight = 3;

        int nameWidth = mc.font.width(name);
        int topRowWidth = headSize + pad + nameWidth;

        ItemStack[] armorSlots = { helmet, chest, legs, boots };
        int armorCount = 0;
        for (ItemStack s : armorSlots) if (!s.isEmpty()) armorCount++;
        int armorRowWidth = armorCount * (iconSize + 2);
        if (!offhand.isEmpty()) armorRowWidth += iconSize + 6;

        int innerWidth = Math.max(topRowWidth, Math.max(armorRowWidth, 60));
        int tagW = innerWidth + pad * 2;
        int tagH = pad + headSize + pad + barHeight + pad + iconSize + pad;

        double wx = Mth.lerp(delta, player.xOld, player.getX());
        double wy = Mth.lerp(delta, player.yOld, player.getY()) + player.getBbHeight() + 0.3;
        double wz = Mth.lerp(delta, player.zOld, player.getZ());

        int[] screen = worldToScreen(wx, wy, wz);
        if (screen == null) return;

        int sx = screen[0] - tagW / 2;
        int sy = screen[1] - tagH / 2;

        drawRoundedRect(graphics, sx, sy, tagW, tagH, 5, 0xCC0D0D0D);

        int cx = sx + pad;
        int cy = sy + pad;

        drawPlayerHead(graphics, player, cx, cy, headSize);
        graphics.drawString(mc.font, name, cx + headSize + pad, cy + headSize / 2 - mc.font.lineHeight / 2, 0xFFFFFFFF, true);

        cy += headSize + pad;
        int barW = tagW - pad * 2;
        graphics.fill(cx, cy, cx + barW, cy + barHeight, 0xFF2A2A2A);
        graphics.fill(cx, cy, cx + (int)(barW * healthPct), cy + barHeight, getHealthColor(healthPct));

        String hpStr = String.format("%.1f", health);
        graphics.drawString(mc.font, hpStr, sx + tagW - pad - mc.font.width(hpStr), cy - mc.font.lineHeight - 1, getHealthColor(healthPct), true);

        cy += barHeight + pad;
        int ix = cx;
        for (ItemStack stack : armorSlots) {
            if (stack.isEmpty()) continue;
            graphics.renderItem(stack, ix, cy);
            ix += iconSize + 2;
        }

        if (!offhand.isEmpty()) {
            ix += 4;
            graphics.renderItem(offhand, ix, cy);
        }
    }

    private int getHealthColor(float pct) {
        int r = (int)(255 * (1f - pct));
        int g = (int)(255 * pct);
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    private void drawPlayerHead(GuiGraphics graphics, Player player, int x, int y, int size) {
        // Updated for modern SkinManager
        ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        graphics.blit(skin, x, y, size, size, 8f, 8f, 8, 8, 64, 64);
        graphics.blit(skin, x, y, size, size, 40f, 8f, 8, 8, 64, 64);
    }

    private void drawRoundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);
    }

    private int[] worldToScreen(double wx, double wy, double wz) {
        var cam = mc.gameRenderer.getMainCamera().getPos(); // Fix: getPos() instead of getPosition()

        float dx = (float)(wx - cam.x);
        float dy = (float)(wy - cam.y);
        float dz = (float)(wz - cam.z);

        Matrix4f view = RenderSystem.getModelViewMatrix();
        Matrix4f proj = RenderSystem.getProjectionMatrix();

        Vector4f pos = new Vector4f(dx, dy, dz, 1f);
        pos.mul(view); // Fix: .mul() for JOML
        pos.mul(proj);

        if (pos.w <= 0f) return null;

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int screenX = (int)((ndcX + 1f) / 2f * mc.getWindow().getGuiScaledWidth());
        int screenY = (int)((1f - ndcY) / 2f * mc.getWindow().getGuiScaledHeight());

        return new int[]{ screenX, screenY };
    }
}
