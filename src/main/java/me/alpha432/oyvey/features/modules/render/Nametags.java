package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alpha432.oyvey.event.impl.render.Render2DEvent;
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

    @Override
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
        // --- Data ---
        String name     = player.getGameProfile().getName();
        float health    = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPct = Mth.clamp(health / maxHealth, 0f, 1f);

        ItemStack helmet  = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest   = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs    = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots   = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack offhand = player.getOffhandItem();

        // --- Layout ---
        int pad         = 5;
        int headSize    = 18;
        int iconSize    = 9;
        int barHeight   = 3;

        int nameWidth   = mc.font.width(name);
        int topRowWidth = headSize + pad + nameWidth;

        ItemStack[] armorSlots = { helmet, chest, legs, boots };
        int armorCount = 0;
        for (ItemStack s : armorSlots) if (!s.isEmpty()) armorCount++;
        int armorRowWidth = armorCount * (iconSize + 2);
        if (!offhand.isEmpty()) armorRowWidth += iconSize + 6;

        int innerWidth = Math.max(topRowWidth, Math.max(armorRowWidth, 60));
        int tagW       = innerWidth + pad * 2;
        int tagH       = pad + headSize + pad + barHeight + pad + iconSize + pad;

        // --- World to screen ---
        double wx = Mth.lerp(delta, player.xOld, player.getX());
        double wy = Mth.lerp(delta, player.yOld, player.getY()) + player.getBbHeight() + 0.3;
        double wz = Mth.lerp(delta, player.zOld, player.getZ());

        int[] screen = worldToScreen(wx, wy, wz);
        if (screen == null) return;

        int sx = screen[0] - tagW / 2;
        int sy = screen[1] - tagH / 2;

        // --- Background ---
        drawRoundedRect(graphics, sx, sy, tagW, tagH, 5, 0xCC0D0D0D);
        drawRoundedRectBorder(graphics, sx, sy, tagW, tagH, 5, 0xFF222222);

        int cx = sx + pad;
        int cy = sy + pad;

        // --- Player head ---
        drawPlayerHead(graphics, player, cx, cy, headSize);

        // --- Name ---
        graphics.drawString(mc.font, name, cx + headSize + pad, cy + headSize / 2 - mc.font.lineHeight / 2, 0xFFFFFFFF, true);

        cy += headSize + pad;

        // --- Health bar ---
        int barW = tagW - pad * 2;
        graphics.fill(cx, cy, cx + barW, cy + barHeight, 0xFF2A2A2A);
        graphics.fill(cx, cy, cx + (int)(barW * healthPct), cy + barHeight, getHealthColor(healthPct));

        // Health number
        String hpStr = String.format("%.1f", health);
        graphics.drawString(mc.font, hpStr,
                sx + tagW - pad - mc.font.width(hpStr),
                cy - mc.font.lineHeight - 1,
                getHealthColor(healthPct), true);

        cy += barHeight + pad;

        // --- Armor icons ---
        int ix = cx;
        for (ItemStack stack : armorSlots) {
            if (stack.isEmpty()) continue;
            graphics.renderItem(stack, ix, cy);
            if (stack.isDamageableItem()) {
                float dur = 1f - (float) stack.getDamageValue() / stack.getMaxDamage();
                graphics.fill(ix, cy + iconSize + 1, ix + iconSize, cy + iconSize + 2, 0xFF333333);
                graphics.fill(ix, cy + iconSize + 1, ix + (int)(iconSize * dur), cy + iconSize + 2, getHealthColor(dur));
            }
            ix += iconSize + 2;
        }

        // --- Offhand ---
        if (!offhand.isEmpty()) {
            ix += 4;
            graphics.renderItem(offhand, ix, cy);
            graphics.drawString(mc.font, "OFF", ix, cy - mc.font.lineHeight, 0xFFAAAAAA, true);
        }
    }

    // --- Helpers ---

    private int getHealthColor(float pct) {
        pct = Mth.clamp(pct, 0f, 1f);
        int r = (int)(255 * (1f - pct));
        int g = (int)(255 * pct);
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    private void drawPlayerHead(GuiGraphics graphics, Player player, int x, int y, int size) {
        ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        graphics.blit(skin, x, y, size, size, 8f, 8f, 8, 8, 64, 64);
        graphics.blit(skin, x, y, size, size, 40f, 8f, 8, 8, 64, 64);
    }

    private void drawRoundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        g.fill(x + r, y,     x + w - r, y + h,     color);
        g.fill(x,     y + r, x + r,     y + h - r, color);
        g.fill(x+w-r, y + r, x + w,     y + h - r, color);
        for (int i = 0; i < r; i++) {
            int len = (int) Math.sqrt((double) r * r - (double)(r - i - 1) * (r - i - 1));
            g.fill(x + r - len, y + i,         x + r,     y + i + 1,     color);
            g.fill(x + w - r,   y + i,         x+w-r+len, y + i + 1,     color);
            g.fill(x + r - len, y + h - i - 1, x + r,     y + h - i,     color);
            g.fill(x + w - r,   y + h - i - 1, x+w-r+len, y + h - i,     color);
        }
    }

    private void drawRoundedRectBorder(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        g.fill(x + r,     y,         x + w - r, y + 1,     color);
        g.fill(x + r,     y + h - 1, x + w - r, y + h,     color);
        g.fill(x,         y + r,     x + 1,     y + h - r, color);
        g.fill(x + w - 1, y + r,     x + w,     y + h - r, color);
    }

    private int[] worldToScreen(double wx, double wy, double wz) {
        var cam = mc.gameRenderer.getMainCamera().getPosition();

        float dx = (float)(wx - cam.x);
        float dy = (float)(wy - cam.y);
        float dz = (float)(wz - cam.z);

        Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix());
        Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());

        Vector4f pos = new Vector4f(dx, dy, dz, 1f);
        view.transform(pos);
        proj.transform(pos);

        if (pos.w <= 0f) return null;

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int screenX = (int)((ndcX + 1f) / 2f * mc.getWindow().getGuiScaledWidth());
        int screenY = (int)((1f - ndcY) / 2f * mc.getWindow().getGuiScaledHeight());

        return new int[]{ screenX, screenY };
    }
}
