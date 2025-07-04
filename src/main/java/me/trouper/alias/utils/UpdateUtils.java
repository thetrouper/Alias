package me.trouper.alias.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class UpdateUtils {

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    public static String fetchRemoteHash(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "AliasUpdater");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return reader.readLine().trim();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] getFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[8192];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        return digest.digest();
    }

    public static File findPluginJar(JavaPlugin plugin) {
        File pluginDir = new File("plugins");
        if (!pluginDir.exists() || !pluginDir.isDirectory()) return null;

        String expectedName = plugin.getDescription().getName();
        String expectedMain = plugin.getDescription().getMain();

        for (File file : pluginDir.listFiles()) {
            if (!file.getName().endsWith(".jar")) continue;

            try (JarFile jar = new JarFile(file)) {
                JarEntry entry = jar.getJarEntry("plugin.yml");
                if (entry == null) continue;

                try (InputStream is = jar.getInputStream(entry)) {
                    YamlConfiguration yml = new YamlConfiguration();
                    yml.load(new InputStreamReader(is));

                    String name = yml.getString("name");
                    String main = yml.getString("main");

                    if (expectedName.equalsIgnoreCase(name) && expectedMain.equalsIgnoreCase(main)) {
                        return file;
                    }

                } catch (Exception ignored) {}
            } catch (IOException ignored) {}
        }

        return null;
    }


    public static boolean isDevelopmentEnvironment(JavaPlugin plugin) {
        try {
            return "TRUE".equalsIgnoreCase(System.getenv("ALIAS_DEVELOPMENT"));
        } catch (Exception e) {
            return false;
        }
    }
}
