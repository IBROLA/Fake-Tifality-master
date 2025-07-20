package club.tifality.gui.console.components;

public interface ISourceComponent {
    public void mousePressed(float mouseX, float mouseY, int mouseZ);

    public void mouseReleased(float mouseX, float mouseY, int mouseZ);

    public void drawScreen(float mouseX, float mouseY);

    public void keyboardTyped(int keyTyped);
}

