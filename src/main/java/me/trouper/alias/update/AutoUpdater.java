package me.trouper.alias.update;

import me.trouper.alias.Alias;
import me.trouper.alias.data.Common;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class AutoUpdater {

    public static boolean checkUpdate(JavaPlugin plugin, Common common) {
        try {
            if (UpdateUtils.isDevelopmentEnvironment(plugin)) {
                plugin.getLogger().info("Development environment detected, bypassing update check.");
                return false;
            }

            String updateURL = common.getUpdateURL();
            if (updateURL == null || updateURL.isEmpty()) {
                plugin.getLogger().warning("Update URL is not set.");
                return false;
            }

            File updateDir = new File("plugins/update");
            if (!updateDir.exists()) updateDir.mkdirs();

            File currentFile = UpdateUtils.findPluginJar(plugin);
            if (currentFile == null) {
                plugin.getLogger().severe("Could not locate plugin file in plugins folder.");
                return false;
            }

            byte[] currentHash = UpdateUtils.getFileHash(currentFile);
            String remoteHashURL = updateURL.replace("/download/", "/hash/sha256/");

            String remoteHashHex = UpdateUtils.fetchRemoteHash(remoteHashURL);
            if (remoteHashHex == null) {
                plugin.getLogger().warning("Failed to fetch remote hash from: " + remoteHashURL);
                return false;
            }

            String currentHashHex = UpdateUtils.bytesToHex(currentHash);
            if (remoteHashHex.equalsIgnoreCase(currentHashHex)) {
                plugin.getLogger().info("Plugin is up to date.");
                return false;
            }

            File updateFile = new File(updateDir, currentFile.getName());
            if (updateFile.exists()) {
                byte[] updateHash = UpdateUtils.getFileHash(updateFile);
                String updateHashHex = UpdateUtils.bytesToHex(updateHash);

                if (remoteHashHex.equalsIgnoreCase(updateHashHex)) {
                    plugin.getLogger().info("An update is already downloaded and ready.");
                    return false;
                } else {
                    plugin.getLogger().info("Found outdated update file in plugins/update/. It will be replaced.");
                    updateFile.delete();
                }
            }

            plugin.getLogger().info("Update available. Downloading new version...");
            File downloaded = downloadFile(updateURL);
            if (downloaded == null) {
                plugin.getLogger().warning("Failed to download update file.");
                return false;
            }

            File destination = new File(updateDir, currentFile.getName());
            Files.copy(downloaded.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Saved updated plugin to: " + destination.getAbsolutePath());

            return true;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during update check", e);
            return false;
        }
    }


    private static File downloadFile(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "AliasUpdater");
        conn.connect();

        if (conn.getResponseCode() != 200) return null;

        File tempFile = File.createTempFile("plugin_update_", ".jar");
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }


}
