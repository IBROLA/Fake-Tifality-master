package club.tifality.gui.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import club.tifality.gui.console.components.SourceComponent;
import club.tifality.gui.console.components.SourceConsolePanel;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.client.gui.GuiScreen;

public class SourceConsoleGUI extends GuiScreen {
    private final List<SourceComponent> components = new ArrayList<>();
    public SourceConsole sourceConsole = new SourceConsole();
    public TimerUtil timer = new TimerUtil();

    public SourceConsoleGUI() {
        this.components.add(new SourceConsolePanel());
    }

    @Override
    public void initGui() {
        this.timer.reset();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (SourceComponent component : this.components) {
            component.drawScreen(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (SourceComponent component : this.components) {
            component.mousePressed(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (SourceComponent component : this.components) {
            component.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (SourceComponent component : this.components) {
            component.keyboardTyped(keyCode);
        }
    }
}

