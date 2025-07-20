package club.tifality.gui.altmanager;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.MinecraftFontRenderer;

public final class PasswordField extends GuiTextField {

    @Override
    public void drawTextBox() {
        String realText = this.getText();
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        String string = this.getText();
        int n2 = ((CharSequence)string).length();
        while (n < n2) {
            stringBuilder.append('*');
            ++n;
        }
        this.setText(stringBuilder.toString());
        super.drawTextBox();
        this.setText(realText);
    }

    public PasswordField(int componentId, MinecraftFontRenderer fontRendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontRendererObj, x, y, par5Width, par6Height);
    }
}

