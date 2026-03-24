package me.alpha432.oyvey.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class Nametags extends Module {

    public Nametags() {
        super("Nametags", "Shows detailed nametags above players.", Category.RENDER);
    }

    // Called every render tick via your addon's event system
    // Hook this into your RenderWorldLastEvent or equivalent
    public void onRenderWorld(GuiGraphics graphics, float partialTicks) {
        if (mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (player.isInvisible()) continue;

            renderNametag(graphics, player, partialTicks);
        }
    }

    private void renderNametag(GuiGraphics graphics, Player player, float partialTicks) {
        // --- Collect data ---
        String name = player.getGameProfile().getName();
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = health / maxHealth;

        ItemStack helmet   = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest    = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs     = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots    = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack offhand  = player.getOffhandItem();

        // --- Layout constants ---
        int padding      = 5;
        int headSize     = 20;
        int armorIconSize = 9;
        int lineHeight   = 10;

        // Calculate name width
        int nameWidth = mc.font.width(name);

        // Armor row: up to 4 icons + offhand
        int armorCount = 0;
        if (!helmet.isEmpty())  armorCount++;
        if (!chest.isEmpty())   armorCount++;
        if (!legs.isEmpty())    armorCount++;
        if (!boots.isEmpty())   armorCount++;

        int armorRowWidth = armorCount * (armorIconSize + 2);
        if (!offhand.isEmpty()) armorRowWidth += armorIconSize + 4; // small gap before offhand

        // Total tag width
        int contentWidth = Math.max(nameWidth + headSize + padding * 2, armorRowWidth + padding * 2);
        int tagWidth  = contentWidth + padding * 2;
        int tagHeight = headSize + lineHeight + armorIconSize + padding * 4 + 6; // head + name + health bar + armor

        // --- World-to-screen projection ---
        // Get the entity's position interpolated
        double ex = Mth.lerp(partialTicks, player.xOld, player.getX());
        double ey = Mth.lerp(partialTicks, player.yOld, player.getY()) + player.getBbHeight() + 0.35;
        double ez = Mth.lerp(partialTicks, player.zOld, player.getZ());

        double cx = mc.player.xOld + (mc.player.getX() - mc.player.xOld) * partialTicks;
        double cy = mc.player.yOld + (mc.player.getY() - mc.player.yOld) * partialTicks;
        double cz = mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * partialTicks;

        double dx = ex - cx;
        double dy = ey - cy;
        double dz = ez - cz;

        // Simple distance check - don't render if too far
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 64) return;

        // Project to screen
        int[] screen = worldToScreen(ex, ey, ez);
        if (screen == null) return;

        int sx = screen[0] - tagWidth / 2;
        int sy = screen[1] - tagHeight / 2;

        // --- Draw background (rounded rect) ---
        drawRoundedRect(graphics, sx, sy, tagWidth, tagHeight, 6, 0xCC101010);

        // Optional subtle border
        drawRoundedRectOutline(graphics, sx, sy, tagWidth, tagHeight, 6, 0xFF2A2A2A);

        int cursorX = sx + padding;
        int cursorY = sy + padding;

        // --- Draw player head ---
        drawPlayerHead(graphics, player, cursorX, cursorY, headSize);

        // --- Draw name ---
        int nameX = cursorX + headSize + padding;
        int nameY = cursorY + (headSize / 2) - (lineHeight / 2);
        graphics.drawString(mc.font, Component.literal(name), nameX, nameY, 0xFFFFFFFF, true);

        cursorY += headSize + padding;

        // --- Draw health bar ---
        int barWidth = tagWidth - padding * 2;
        int barHeight = 4;
        // Background
        graphics.fill(cursorX, cursorY, cursorX + barWidth, cursorY + barHeight, 0xFF333333);
        // Foreground - color shifts red->yellow->green
        int healthColor = getHealthColor(healthPercent);
        graphics.fill(cursorX, cursorY, cursorX + (int)(barWidth * healthPercent), cursorY + barHeight, healthColor);

        // Health text on right
        String healthStr = String.format("%.1f", health);
        graphics.drawString(mc.font, healthStr,
                sx + tagWidth - padding - mc.font.width(healthStr),
                cursorY - lineHeight,
                healthColor, true);

        cursorY += barHeight + padding;

        // --- Draw armor icons ---
        int iconX = cursorX;
        ItemStack[] armorSlots = { helmet, chest, legs, boots };
        for (ItemStack stack : armorSlots) {
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, iconX, cursorY);

                // Durability overlay
                if (stack.isDamageableItem()) {
                    float durability = 1f - (float) stack.getDamageValue() / stack.getMaxDamage();
                    int durColor = getHealthColor(durability);
                    // small 1px durability line under icon
                    graphics.fill(iconX, cursorY + armorIconSize,
                            iconX + (int)(armorIconSize * durability), cursorY + armorIconSize + 1,
                            durColor);
                }

                iconX += armorIconSize + 2;
            }
        }

        // --- Draw offhand item (separated) ---
        if (!offhand.isEmpty()) {
            iconX += 4; // gap
            graphics.renderItem(offhand, iconX, cursorY);

            // Label "OFF" above it in tiny text
            graphics.drawString(mc.font, "OFF", iconX, cursorY - 8, 0xFFAAAAAA, true);
        }
    }

    // --- Helpers ---

    private int getHealthColor(float percent) {
        if (percent > 0.6f) {
            // Green to Yellow
            float t = (percent - 0.6f) / 0.4f;
            int r = (int)(255 * (1 - t));
            return 0xFF000000 | (r << 16) | (0xFF << 8);
        } else {
            // Yellow to Red
            float t = percent / 0.6f;
            int g = (int)(255 * t);
            return 0xFF000000 | (0xFF << 16) | (g << 8);
        }
    }

    private void drawPlayerHead(GuiGraphics graphics, Player player, int x, int y, int size) {
        // Render the player's face skin texture (8x8 region from skin, scaled to size)
        ResourceLocation skin = mc.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();
        RenderSystem.setShaderTexture(0, skin);
        // Face layer (u=8, v=8, uWidth=8, vHeight=8 on a 64x64 skin)
        graphics.blit(skin, x, y, size, size, 8, 8, 8, 8, 64, 64);
        // Hat layer
        graphics.blit(skin, x, y, size, size, 40, 8, 8, 8, 64, 64);
    }

    private void drawRoundedRect(GuiGraphics graphics, int x, int y, int w, int h, int r, int color) {
        // Fill center + sides
        graphics.fill(x + r, y, x + w - r, y + h, color);
        graphics.fill(x, y + r, x + r, y + h - r, color);
        graphics.fill(x + w - r, y + r, x + w, y + h - r, color);
        // Corners (approximate with small fills)
        for (int i = 0; i < r; i++) {
            int len = (int)(Math.sqrt(r * r - (r - i - 1) * (r - i - 1)));
            graphics.fill(x + r - len, y + i, x + r, y + i + 1, color);
            graphics.fill(x + w - r, y + i, x + w - r + len, y + i + 1, color);
            graphics.fill(x + r - len, y + h - i - 1, x + r, y + h - i, color);
            graphics.fill(x + w - r, y + h - i - 1, x + w - r + len, y + h - i, color);
        }
    }

    private void drawRoundedRectOutline(GuiGraphics graphics, int x, int y, int w, int h, int r, int color) {
        // Top and bottom edges
        graphics.fill(x + r, y, x + w - r, y + 1, color);
        graphics.fill(x + r, y + h - 1, x + w - r, y + h, color);
        // Left and right edges
        graphics.fill(x, y + r, x + 1, y + h - r, color);
        graphics.fill(x + w - 1, y + r, x + w, y + h - r, color);
    }

    private int[] worldToScreen(double wx, double wy, double wz) {
        // Uses Minecraft's projection to convert world coords to screen coords
        net.minecraft.world.phys.Vec3 projected =
                net.minecraft.client.Camera.class.cast(mc.gameRenderer.getMainCamera())
                        .getPosition();

        // Delegate to vanilla screen projection util if available in your mappings,
        // otherwise use a basic matrix approach:
        org.joml.Matrix4f proj = RenderSystem.getProjectionMatrix();
        org.joml.Matrix4f view = new org.joml.Matrix4f(RenderSystem.getModelViewMatrix());

        double dx = wx - projected.x;
        double dy = wy - projected.y;
        double dz = wz - projected.z;

        org.joml.Vector4f pos = new org.joml.Vector4f((float)dx, (float)dy, (float)dz, 1f);
        view.transform(pos);
        proj.transform(pos);

        if (pos.w <= 0) return null; // behind camera

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int screenX = (int)((ndcX + 1f) / 2f * mc.getWindow().getGuiScaledWidth());
        int screenY = (int)((1f - ndcY) / 2f * mc.getWindow().getGuiScaledHeight());

        return new int[]{ screenX, screenY };
    }
}
