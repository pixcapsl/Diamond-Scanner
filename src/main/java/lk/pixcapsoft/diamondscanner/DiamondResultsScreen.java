package lk.pixcapsoft.diamondscanner;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import java.util.List;

public class DiamondResultsScreen extends Screen {
    private final Screen parent;
    private final List<BlockPos> diamondPositions;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 12;
    private static final int VISIBLE_LINES = 15;

    public DiamondResultsScreen(Screen parent, List<BlockPos> diamondPositions) {
        super(Text.literal("Diamond Scan Results"));
        this.parent = parent;
        this.diamondPositions = diamondPositions;
    }

    @Override
    protected void init() {
        // Close button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> {
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        
        // Result count
        String countText = "Found " + diamondPositions.size() + " diamond ore(s)";
        context.drawCenteredTextWithShadow(this.textRenderer, countText, this.width / 2, 30, 0x55FFFF);
        
        // Box background
        int boxX = this.width / 2 - 150;
        int boxY = 50;
        int boxWidth = 300;
        int boxHeight = VISIBLE_LINES * LINE_HEIGHT + 10;
        
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xAA000000);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, 0xFF555555);
        
        // Render diamond positions with scroll
        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + VISIBLE_LINES, diamondPositions.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            BlockPos pos = diamondPositions.get(i);
            String posText = "💎 " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
            int textY = boxY + 5 + (i - startIndex) * LINE_HEIGHT;
            context.drawTextWithShadow(this.textRenderer, posText, boxX + 10, textY, 0xFFFFFF);
        }
        
        // Scroll indicators
        if (scrollOffset > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, "▲ Scroll Up", this.width / 2, boxY - 15, 0xFFFF55);
        }
        if (endIndex < diamondPositions.size()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "▼ Scroll Down", this.width / 2, boxY + boxHeight + 5, 0xFFFF55);
        }
        
        // Instructions
        if (diamondPositions.size() > VISIBLE_LINES) {
            context.drawCenteredTextWithShadow(this.textRenderer, "Use mouse wheel to scroll", this.width / 2, this.height - 50, 0xAAAAAA);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, diamondPositions.size() - VISIBLE_LINES);
        
        if (verticalAmount > 0) {
            // Scroll up
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (verticalAmount < 0) {
            // Scroll down
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
}