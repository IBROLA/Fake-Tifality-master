package club.tifality.manager.config;

import club.tifality.gui.altmanager.Alt;
import club.tifality.gui.altmanager.AltManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Alts extends ConfigManager.CustomFile {
    public Alts(String name, boolean loadOnStart) {
        super(name, loadOnStart);
    }

    @Override
    public void loadFile() throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(this.getFile()));
        while ((line = reader.readLine()) != null) {
            String[] arguments2 = line.split(":");
            for (int i = 0; i < 2; ++i) {
                arguments2[i].replace(" ", "");
            }
            if (arguments2.length > 2) {
                AltManager.registry.add(new Alt(arguments2[0], arguments2[1], arguments2[2], arguments2.length > 3 ? Alt.Status.valueOf(arguments2[3]) : Alt.Status.Unchecked));
                continue;
            }
            AltManager.registry.add(new Alt(arguments2[0], arguments2[1]));
        }
        reader.close();
        System.out.println("Loaded " + this.getName() + " File!");
    }

    @Override
    public void saveFile() throws IOException {
        PrintWriter alts = new PrintWriter(new FileWriter(this.getFile()));
        for (Alt alt : AltManager.registry) {
            if (alt.getMask().equals("")) {
                alts.println(alt.getUsername() + ":" + alt.getPassword() + ":" + alt.getUsername() + ":" + alt.getStatus());
                continue;
            }
            alts.println(alt.getUsername() + ":" + alt.getPassword() + ":" + alt.getMask() + ":" + alt.getStatus());
        }
        alts.close();
    }
}

