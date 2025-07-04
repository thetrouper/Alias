package me.trouper.alias.data;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.AutoRegistrar;

import java.util.List;
import java.util.Optional;

/**
 * Manages loading, retrieving, and saving of JsonSerializable instances
 * registered via AutoRegistrar.
 */
public class DataManager {
    private final AutoRegistrar registrar;

    public DataManager(AliasContext context) {
        this.registrar = context.getAutoRegistrar();
    }

    /**
     * Loads the data for the given JsonSerializable type from file, updates the registry,
     * and returns the loaded instance (or fallback if loading failed).
     *
     * @param clazz the class type to load
     * @param <T>   type extending JsonSerializable
     * @return loaded or fallback instance
     */
    public <T extends JsonSerializable<?>> T load(Class<T> clazz) {
        Optional<T> opt = getOptional(clazz);
        if (opt.isEmpty()) {
            return null;
        }
        T instance = opt.get();
        T loaded = JsonSerializable.load(instance.getFile(), clazz, instance);

        if (loaded != instance) {
            List<JsonSerializable<?>> list = registrar.getSerializables();
            list.remove(instance);
            list.add(loaded);
        }
        return loaded;
    }

    /**
     * Retrieves the registered JsonSerializable instance without loading.
     *
     * @param clazz the class type to retrieve
     * @param <T>   type extending JsonSerializable
     * @return instance or null if not found
     */
    public <T extends JsonSerializable<?>> T get(Class<T> clazz) {
        return getOptional(clazz).orElse(null);
    }

    private <T extends JsonSerializable<?>> Optional<T> getOptional(Class<T> clazz) {
        return registrar.getSerializables().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }

    /**
     * Saves the registered JsonSerializable instance to its file.
     * Does nothing if the type is not registered.
     *
     * @param clazz the class type to save
     * @param <T>   type extending JsonSerializable
     */
    public <T extends JsonSerializable<?>> void save(Class<T> clazz) {
        getOptional(clazz).ifPresent(JsonSerializable::save);
    }
}
