package club.tifality.gui.console;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SourceConsole {
    private final List<String> stringList = new CopyOnWriteArrayList<>();

    public void addStringList(String str) {
        if (this.stringList.size() > 50) {
            this.stringList.remove(this.stringList.get(0));
        }
        this.stringList.add(str);
    }

    public void clearStringList() {
        this.stringList.clear();
    }

    public List<String> getStringList() {
        return this.stringList;
    }

    public String processCommand(String input) {
        String output = "output";
        return output;
    }
}

