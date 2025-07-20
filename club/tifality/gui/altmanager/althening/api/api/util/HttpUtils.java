package club.tifality.gui.altmanager.althening.api.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class HttpUtils {
    protected String connect(String url) throws IOException {
        String link;
        InputStream inputStream = new URL(url).openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        while ((link = reader.readLine()) != null) {
            sb.append(link).append("\n");
        }
        return sb.toString();
    }
}

