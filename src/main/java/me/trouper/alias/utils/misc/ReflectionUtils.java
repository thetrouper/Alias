package me.trouper.alias.utils.misc;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectionUtils {

    public static Set<Class<?>> getClassesInPackage(JavaPlugin plugin, String pkg) {
        Set<Class<?>> classes = new HashSet<>();
        String path = pkg.replace('.', '/');
        try {
            File file = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            try (JarFile jar = new JarFile(file)) {
                for (JarEntry entry : jar.stream().toList()) {
                    String name = entry.getName();
                    if (!name.endsWith(".class") || !name.startsWith(path)) continue;

                    String className = name.replace('/', '.').replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to scan package: " + pkg);
            e.printStackTrace();
        }
        return classes;
    }
}

