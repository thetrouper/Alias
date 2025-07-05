package me.trouper.alias.server.systems.freeze;

import me.trouper.alias.AliasContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager {
    private final AliasContext context;
    private final Map<UUID, FreezeSession> sessions = new ConcurrentHashMap<>();

    public FreezeManager(AliasContext context) {
        this.context = context;
    }

    void register(FreezeSession session) {
        sessions.put(session.uuid(), session);
    }

    void unregister(UUID uuid) {
        sessions.remove(uuid);
    }

    public FreezeSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public boolean isFrozen(UUID uuid) {
        return sessions.containsKey(uuid);
    }
}
