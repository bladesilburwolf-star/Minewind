package com.minewind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic HUD system for Morrowind-style UI
 * Features:
 * - Custom health/magicka/stamina bars
 * - Skill and attribute displays
 * - Spell and weapon hotkeys
 * - Custom crosshair
 * - Quest and dialogue indicators
 */
public class HUDSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HUDSystem.class);
    
    // HUD constants
    private static final int HUD_MARGIN = 10;
    private static final int BAR_WIDTH = 180;
    private static final int BAR_HEIGHT = 20;
    private static final int BAR_SPACING = 25;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BAR_BACKGROUND = 0x80000000;
    private static final int HEALTH_COLOR = 0xFF0000;
    private static final int MAGICKA_COLOR = 0x0000FF;
    private static final int STAMINA_COLOR = 0x00FF00;
    
    // HUD state
    private boolean showHealthBar;
    private boolean showMagickaBar;
    private boolean showStaminaBar;
    private boolean showSkills;
    private boolean showAttributes;
    private boolean showCrosshair;
    private boolean showHotkeys;
    
    // HUD elements
    private final List<HUDElement> hudElements;
    
    public HUDSystem() {
        this.showHealthBar = true;
        this.showMagickaBar = true;
        this.showStaminaBar = true;
        this.showSkills = false;
        this.showAttributes = false;
        this.showCrosshair = true;
        this.showHotkeys = true;
        this.hudElements = new ArrayList<>();
    }
    
    public void initialize() {
        LOGGER.info("Initializing HUD system...");
        
        // Register default HUD elements
        registerDefaultElements();
        
        LOGGER.info("HUD system initialized with {} elements", hudElements.size());
    }
    
    public void onClientStart() {
        LOGGER.info("HUD system started");
    }
    
    public void onClientStop() {
        LOGGER.info("HUD system stopped");
        hudElements.clear();
    }
    
    private void registerDefaultElements() {
        // Register default HUD elements
        hudElements.add(new HUDElement("health_bar", HUDElementType.HEALTH_BAR, 10, 10));
        hudElements.add(new HUDElement("magicka_bar", HUDElementType.MAGICKA_BAR, 10, 35));
        hudElements.add(new HUDElement("stamina_bar", HUDElementType.STAMINA_BAR, 10, 60));
        hudElements.add(new HUDElement("crosshair", HUDElementType.CROSSHAIR, 0, 0));
        hudElements.add(new HUDElement("hotkeys", HUDElementType.HOTKEYS, 10, 100));
        
        LOGGER.debug("Registered {} default HUD elements", hudElements.size());
    }
    
    /**
     * Render the HUD
     */
    public void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getWidth();
        int screenHeight = client.getWindow().getHeight();
        
        // Get player data
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            float health = attributeSystem.getHealth();
            float magicka = attributeSystem.getMagicka();
            float stamina = attributeSystem.getStamina();
            
            // Draw health bar
            if (showHealthBar) {
                drawBar(context, HUD_MARGIN, HUD_MARGIN, BAR_WIDTH, BAR_HEIGHT, 
                       health, attributeSystem.getHealth(), "Health", 
                       HEALTH_COLOR, textRenderer);
            }
            
            // Draw magicka bar
            if (showMagickaBar) {
                drawBar(context, HUD_MARGIN, HUD_MARGIN + BAR_HEIGHT + BAR_SPACING, BAR_WIDTH, BAR_HEIGHT,
                       magicka, attributeSystem.getMagicka(), "Magicka",
                       MAGICKA_COLOR, textRenderer);
            }
            
            // Draw stamina bar
            if (showStaminaBar) {
                drawBar(context, HUD_MARGIN, HUD_MARGIN + (BAR_HEIGHT + BAR_SPACING) * 2, BAR_WIDTH, BAR_HEIGHT,
                       stamina, attributeSystem.getStamina(), "Stamina",
                       STAMINA_COLOR, textRenderer);
            }
        }
        
        // Draw crosshair
        if (showCrosshair) {
            drawCrosshair(context, screenWidth / 2, screenHeight / 2);
        }
        
        // Draw hotkeys
        if (showHotkeys) {
            drawHotkeys(context, textRenderer);
        }
        
        // Render custom HUD elements
        for (HUDElement element : hudElements) {
            renderElement(context, element, tickDelta);
        }
    }
    
    /**
     * Draw a status bar (health, magicka, stamina)
     */
    private void drawBar(DrawContext context, int x, int y, int width, int height,
                        float current, float max, String label, int color, TextRenderer textRenderer) {
        // Draw background
        context.fill(x, y, x + width, y + height, BAR_BACKGROUND);
        
        // Draw fill
        float fillWidth = (current / max) * width;
        context.fill(x, y, x + (int) fillWidth, y + height, color);
        
        // Draw border
        context.drawBorder(x, y, width, height, 0xFFFFFFFF);
        
        // Draw label
        String text = String.format("%s: %.0f/%.0f", label, current, max);
        context.drawText(textRenderer, text, x, y - 10, TEXT_COLOR, false);
    }
    
    /**
     * Draw custom crosshair
     */
    private void drawCrosshair(DrawContext context, int centerX, int centerY) {
        int size = 10;
        int thickness = 2;
        int color = 0xFFFFFFFF;
        
        // Horizontal line
        context.fill(centerX - size, centerY - thickness / 2, 
                     centerX + size, centerY + thickness / 2, color);
        
        // Vertical line
        context.fill(centerX - thickness / 2, centerY - size,
                     centerX + thickness / 2, centerY + size, color);
    }
    
    /**
     * Draw hotkeys (spells and weapons)
     */
    private void drawHotkeys(DrawContext context, TextRenderer textRenderer) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            SpellSystem spellSystem = mod.getSpellSystem();
            
            int x = HUD_MARGIN;
            int y = 100;
            int size = 24;
            int spacing = 30;
            
            // Draw spell hotkeys (1-9)
            for (int i = 1; i <= 9; i++) {
                int finalI = i;
                spellSystem.getKnownSpells().values().stream()
                    .filter(spell -> spell.getId().equals("fireball"))
                    .findFirst()
                    .ifPresent(spell -> {
                        context.drawText(textRenderer, String.valueOf(finalI), x + 4, y + 4, TEXT_COLOR, false);
                    });
                
                x += spacing;
            }
        }
    }
    
    /**
     * Render a custom HUD element
     */
    private void renderElement(DrawContext context, HUDElement element, float tickDelta) {
        switch (element.getType()) {
            case HEALTH_BAR -> renderHealthBar(context, element, tickDelta);
            case MAGICKA_BAR -> renderMagickaBar(context, element, tickDelta);
            case STAMINA_BAR -> renderStaminaBar(context, element, tickDelta);
            case CROSSHAIR -> renderCrosshair(context, element, tickDelta);
            case HOTKEYS -> renderHotkeysElement(context, element, tickDelta);
            case SKILL_DISPLAY -> renderSkillDisplay(context, element, tickDelta);
            case ATTRIBUTE_DISPLAY -> renderAttributeDisplay(context, element, tickDelta);
        }
    }
    
    private void renderHealthBar(DrawContext context, HUDElement element, float tickDelta) {
        // Custom health bar rendering
    }
    
    private void renderMagickaBar(DrawContext context, HUDElement element, float tickDelta) {
        // Custom magicka bar rendering
    }
    
    private void renderStaminaBar(DrawContext context, HUDElement element, float tickDelta) {
        // Custom stamina bar rendering
    }
    
    private void renderCrosshair(DrawContext context, HUDElement element, float tickDelta) {
        // Custom crosshair rendering
    }
    
    private void renderHotkeysElement(DrawContext context, HUDElement element, float tickDelta) {
        // Custom hotkeys rendering
    }
    
    private void renderSkillDisplay(DrawContext context, HUDElement element, float tickDelta) {
        // Skill display rendering
    }
    
    private void renderAttributeDisplay(DrawContext context, HUDElement element, float tickDelta) {
        // Attribute display rendering
    }
    
    /**
     * Toggle HUD element visibility
     */
    public void toggleHealthBar() {
        showHealthBar = !showHealthBar;
    }
    
    public void toggleMagickaBar() {
        showMagickaBar = !showMagickaBar;
    }
    
    public void toggleStaminaBar() {
        showStaminaBar = !showStaminaBar;
    }
    
    public void toggleSkills() {
        showSkills = !showSkills;
    }
    
    public void toggleAttributes() {
        showAttributes = !showAttributes;
    }
    
    public void toggleCrosshair() {
        showCrosshair = !showCrosshair;
    }
    
    public void toggleHotkeys() {
        showHotkeys = !showHotkeys;
    }
    
    /**
     * Add a custom HUD element
     */
    public void addHUDElement(HUDElement element) {
        hudElements.add(element);
        LOGGER.debug("Added HUD element: {}", element.getId());
    }
    
    /**
     * Remove a HUD element
     */
    public void removeHUDElement(String id) {
        hudElements.removeIf(element -> element.getId().equals(id));
        LOGGER.debug("Removed HUD element: {}", id);
    }
    
    public enum HUDElementType {
        HEALTH_BAR, MAGICKA_BAR, STAMINA_BAR, CROSSHAIR, HOTKEYS, SKILL_DISPLAY, ATTRIBUTE_DISPLAY
    }
    
    /**
     * HUD element representation
     */
    public static class HUDElement {
        private final String id;
        private final HUDElementType type;
        private int x;
        private int y;
        private int width;
        private int height;
        private boolean visible;
        
        public HUDElement(String id, HUDElementType type, int x, int y) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = 100;
            this.height = 20;
            this.visible = true;
        }
        
        public String getId() {
            return id;
        }
        
        public HUDElementType getType() {
            return type;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}
