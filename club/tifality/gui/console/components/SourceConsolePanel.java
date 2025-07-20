package club.tifality.gui.console.components;

import java.util.ArrayList;
import java.util.List;

import club.tifality.Tifality;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import club.tifality.utils.render.RenderingUtils;

public class SourceConsolePanel extends SourceComponent {
    private final List<SourceComponent> components = new ArrayList<>();
    private final float x = 100.0f;
    private final float y = 100.0f;
    private float dragX;
    private float dragY;
    private float lastDragX;
    private float lastDragY;
    private float width = 267.0f;
    private float height = 198.0f;
    private float lastWidth;
    private float lastHeight;
    private boolean dragging;
    private boolean expanding;

    private void prepareScissorBox(float x, float y, float x2, float y2) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scale = new ScaledResolution(mc);
        int factor = scale.getScaleFactor();
        GL11.glScissor((int)(x * (float)factor), (int)(((float)scale.getScaledHeight() - y2) * (float)factor), (int)((x2 - x) * (float)factor), (int)((y2 - y) * (float)factor));
    }

    @Override
    public void mousePressed(float mouseX, float mouseY, int mouseID) {
        float xOffset = this.x + this.dragX;
        float yOffset = this.y + this.dragY;
        if (mouseX >= xOffset && mouseY >= yOffset && mouseX <= xOffset + this.width && mouseY <= yOffset + 12.0f && mouseID == 0) {
            this.dragging = true;
            this.lastDragX = mouseX - this.dragX;
            this.lastDragY = mouseY - this.dragY;
        }
        if (mouseX >= xOffset + this.width - 4.0f - 5.0f && mouseY >= yOffset + 5.0f && mouseX <= xOffset + this.width - 4.0f && mouseY <= yOffset + 10.0f && mouseID == 0) {
            this.dragging = false;
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        if (mouseX >= xOffset + this.width - 5.0f && mouseY >= yOffset + this.height - 5.0f && mouseX <= xOffset + this.width && mouseY <= yOffset + this.height && mouseID == 0) {
            this.expanding = true;
            this.lastWidth = mouseX - this.width;
            this.lastHeight = mouseY - this.height;
        }
        for (SourceComponent component : this.components) {
            component.mousePressed(mouseX, mouseY, mouseID);
        }
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int mouseID) {
        if (mouseID == 0 && this.dragging || this.expanding) {
            this.dragging = false;
            this.expanding = false;
        }
        for (SourceComponent component : this.components) {
            component.mouseReleased(mouseX, mouseY, mouseID);
        }
    }

    @Override
    public void drawScreen(float mouseX, float mouseY) {
        float xOffset = this.x + this.dragX;
        float yOffset = this.y + this.dragY;
        RenderingUtils.drawRoundedRect(xOffset, yOffset, xOffset + this.width, yOffset + this.height, Colors.getColor(162), Colors.getColor(162));
        RenderingUtils.rectangle(xOffset + 4.0f, yOffset + 13.0f, xOffset + this.width - 4.0f, yOffset + this.height - 3.0f, Colors.getColor(62));
        if (!Tifality.getSourceConsoleGUI().sourceConsole.getStringList().isEmpty()) {
            GlStateManager.pushMatrix();
            this.prepareScissorBox(xOffset + 8.0f, yOffset + 20.0f, xOffset + this.width - 4.0f - 11.5f, yOffset + this.height - 19.5f);
            GL11.glEnable(3089);
            float yMEME = 0.0f;
            float maximum = (this.height - 20.0f) / 6.0f - 3.5f;
            if (Tifality.getSourceConsoleGUI().sourceConsole.getStringList().size() > (int)maximum) {
                yMEME = ((float) Tifality.getSourceConsoleGUI().sourceConsole.getStringList().size() - maximum) * -6.0f;
            }
            for (String str : Tifality.getSourceConsoleGUI().sourceConsole.getStringList()) {
                Wrapper.getVerdana10().drawString(str, xOffset + 9.0f, yOffset + 23.0f + yMEME, -1);
                yMEME += 6.0f;
            }
            GL11.glDisable(3089);
            GlStateManager.popMatrix();
        }
        RenderingUtils.rectangle(xOffset + this.width - 8.0f, yOffset + 19.0f, (double)(xOffset + this.width - 8.0f) - 7.5, (double)(yOffset + this.height - 20.0f) + 0.5, Colors.getColor(33));
        RenderingUtils.rectangle(xOffset + this.width - 9.0f, yOffset + 23.0f, xOffset + this.width - 8.0f - 7.0f, yOffset + this.height - 24.0f, Colors.getColor(60));
        RenderingUtils.rectangleBordered((double)(xOffset + this.width) - 8.5, yOffset + 19.0f, xOffset + this.width - 8.0f - 7.0f, (double)yOffset + 26.5, 0.5, Colors.getColor(47), Colors.getColor(38));
        RenderingUtils.rectangleBordered((double)(xOffset + this.width) - 8.5, yOffset + this.height - 20.0f - 7.0f, xOffset + this.width - 8.0f - 7.0f, yOffset + this.height - 20.0f, 0.5, Colors.getColor(47), Colors.getColor(38));
        GlStateManager.pushMatrix();
        GlStateManager.translate((double)(xOffset + this.width - 8.0f - 5.0f), (double)yOffset + 22.5, 0.0);
        GlStateManager.rotate(270.0f, 0.0f, 0.0f, 90.0f);
        RenderingUtils.rectangle(-1.0, -0.5, -0.5, 3.0, Colors.getColor(111));
        RenderingUtils.rectangle(-0.5, 0.0, 0.0, 2.5, Colors.getColor(111));
        RenderingUtils.rectangle(0.0, 0.5, 0.5, 2.0, Colors.getColor(111));
        RenderingUtils.rectangle(0.5, 1.0, 1.0, 1.5, Colors.getColor(111));
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate((double)(xOffset + this.width - 8.0f) - 2.5, (double)(yOffset + this.height) - 23.5, 0.0);
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 90.0f);
        RenderingUtils.rectangle(-1.0, -0.5, -0.5, 3.0, Colors.getColor(111));
        RenderingUtils.rectangle(-0.5, 0.0, 0.0, 2.5, Colors.getColor(111));
        RenderingUtils.rectangle(0.0, 0.5, 0.5, 2.0, Colors.getColor(111));
        RenderingUtils.rectangle(0.5, 1.0, 1.0, 1.5, Colors.getColor(111));
        GlStateManager.popMatrix();
        RenderingUtils.rectangle(xOffset + 8.0f, yOffset + 19.0f, xOffset + this.width - 8.0f, (double)yOffset + 19.5, Colors.getColor(0, 65));
        RenderingUtils.rectangle(xOffset + 8.0f, yOffset + 19.0f, (double)xOffset + 8.5, yOffset + this.height - 20.0f, Colors.getColor(0, 65));
        RenderingUtils.rectangle(xOffset + 8.0f, yOffset + this.height - 20.0f, xOffset + this.width - 8.0f, (double)(yOffset + this.height - 20.0f) + 0.5, Colors.getColor(255, 140));
        RenderingUtils.rectangle((double)(xOffset + this.width) - 8.5, yOffset + 19.0f, xOffset + this.width - 8.0f, (double)(yOffset + this.height - 20.0f) + 0.5, Colors.getColor(255, 140));
        RenderingUtils.rectangle(xOffset + 8.0f, (double)(yOffset + this.height) - 15.5, (double)(xOffset + this.width - 8.0f) - 40.5, yOffset + this.height - 15.0f, Colors.getColor(0, 65));
        RenderingUtils.rectangle(xOffset + 8.0f, (double)(yOffset + this.height) - 15.5, (double)xOffset + 8.5, yOffset + this.height - 7.0f, Colors.getColor(0, 65));
        RenderingUtils.rectangle(xOffset + this.width - 8.0f - 41.0f, (double)(yOffset + this.height) - 15.5, (double)(xOffset + this.width - 8.0f) - 40.5, yOffset + this.height - 7.0f, Colors.getColor(255, 140));
        RenderingUtils.rectangle(xOffset + 8.0f, (double)(yOffset + this.height) - 7.5, (double)(xOffset + this.width - 8.0f) - 40.5, yOffset + this.height - 7.0f, Colors.getColor(255, 140));
        RenderingUtils.rectangle(xOffset + this.width - 8.0f - 40.5f + 3.0f, (double)(yOffset + this.height) - 15.5, xOffset + this.width - 8.0f - 40.5f + 3.0f + 32.0f, yOffset + this.height - 16.0f + 9.0f, Colors.getColor(8));
        RenderingUtils.rectangle((double)(xOffset + this.width - 8.0f - 40.5f) + 3.5, yOffset + this.height - 15.0f, (double)(xOffset + this.width - 8.0f - 40.5f + 3.0f + 32.0f) - 0.5, (double)(yOffset + this.height - 16.0f + 9.0f) - 0.5, Colors.getColor(181));
        RenderingUtils.rectangle(xOffset + this.width - 8.0f - 40.5f + 4.0f, (double)(yOffset + this.height) - 14.5, (double)(xOffset + this.width - 8.0f - 40.5f + 3.0f) + 31.5, (double)(yOffset + this.height - 16.0f) + 8.5, Colors.getColor(62));
        RenderingUtils.rectangle(xOffset + this.width - 4.0f - 1.0f, (double)yOffset + 8.5, xOffset + this.width - 4.0f, (double)yOffset + 9.5, Colors.getColor(201));
        RenderingUtils.rectangle((double)(xOffset + this.width - 4.0f) - 1.5, yOffset + 8.0f, (double)(xOffset + this.width - 4.0f) - 0.5, yOffset + 9.0f, Colors.getColor(201));
        RenderingUtils.rectangle((double)(xOffset + this.width - 4.0f) - 1.5, yOffset + 7.0f, (double)(xOffset + this.width - 4.0f) - 0.5, yOffset + 6.0f, Colors.getColor(201));
        RenderingUtils.rectangle(xOffset + this.width - 4.0f - 1.0f, (double)yOffset + 5.5, xOffset + this.width - 4.0f, (double)yOffset + 6.5, Colors.getColor(201));
        RenderingUtils.rectangle(xOffset + this.width - 4.0f - 4.0f, (double)yOffset + 8.5, xOffset + this.width - 4.0f - 3.0f, (double)yOffset + 9.5, Colors.getColor(201));
        RenderingUtils.rectangle((double)(xOffset + this.width - 4.0f) - 3.5, yOffset + 8.0f, (double)(xOffset + this.width - 4.0f) - 2.5, yOffset + 9.0f, Colors.getColor(201));
        RenderingUtils.rectangle((double)(xOffset + this.width - 4.0f) - 3.5, yOffset + 7.0f, (double)(xOffset + this.width - 4.0f) - 2.5, yOffset + 6.0f, Colors.getColor(201));
        RenderingUtils.rectangle(xOffset + this.width - 4.0f - 4.0f, (double)yOffset + 5.5, xOffset + this.width - 4.0f - 3.0f, (double)yOffset + 6.5, Colors.getColor(201));
        RenderingUtils.rectangle(xOffset + this.width - 4.0f - 3.0f, (double)yOffset + 6.5, xOffset + this.width - 4.0f - 1.0f, (double)yOffset + 8.5, Colors.getColor(201));
        Wrapper.getVerdana10().drawString("Submit", xOffset + this.width - 8.0f - 40.5f + 6.0f, yOffset + this.height - 12.0f, -1);
        Wrapper.getVerdana10().drawString("Console", xOffset + 8.0f, yOffset + 7.0f, -1);
        if (this.dragging) {
            this.dragX = mouseX - this.lastDragX;
            this.dragY = mouseY - this.lastDragY;
        }
        if (this.expanding) {
            this.width = mouseX - this.lastWidth;
            this.height = mouseY - this.lastHeight;
            if (this.width < 70.0f) {
                this.width = 70.0f;
            }
            if (this.height < 55.0f) {
                this.height = 55.0f;
            }
        }
        for (SourceComponent component : this.components) {
            component.drawScreen(mouseX, mouseY);
        }
    }

    @Override
    public void keyboardTyped(int keyTyped) {
        if (Tifality.getSourceConsoleGUI().timer.hasElapsed(100L) && keyTyped == 41) {
            this.dragging = false;
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }
}

