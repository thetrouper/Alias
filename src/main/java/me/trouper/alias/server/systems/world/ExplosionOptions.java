package me.trouper.alias.server.systems.world;

import me.trouper.alias.server.systems.world.burning.BurnOptions;

public class ExplosionOptions {
    private double coreRadius = 3.0;
    private double falloffRadius = 8.0;
    private double maxBurnRadius = 15.0;
    private double baseDamage = 20;
    private double destructionDelay = 0.0; // SECONDS
    private double burnDelay = 0.5; // SECONDS
    private double maxHeat = 1.0;
    private double minHeat = 0.1;
    private boolean createParticles = true;
    private boolean playSound = true;
    private BurnOptions burnOptions = new BurnOptions();

    public double getCoreRadius() { return coreRadius; }
    public ExplosionOptions setCoreRadius(double coreRadius) { this.coreRadius = coreRadius; return this; }

    public double getFalloffRadius() { return falloffRadius; }
    public ExplosionOptions setFalloffRadius(double falloffRadius) { this.falloffRadius = falloffRadius; return this; }

    public double getMaxBurnRadius() { return maxBurnRadius; }
    public ExplosionOptions setMaxBurnRadius(double maxBurnRadius) { this.maxBurnRadius = maxBurnRadius; return this; }

    public double getBaseDamage() { return baseDamage; }
    public ExplosionOptions setBaseDamage(double baseDamage) { this.baseDamage = baseDamage; return this; }

    public double getDestructionDelay() { return destructionDelay; }
    public ExplosionOptions setDestructionDelay(double destructionDelay) { this.destructionDelay = destructionDelay; return this; }

    public double getBurnDelay() { return burnDelay; }
    public ExplosionOptions setBurnDelay(double burnDelay) { this.burnDelay = burnDelay; return this; }

    public double getMaxHeat() { return maxHeat; }
    public ExplosionOptions setMaxHeat(double maxHeat) { this.maxHeat = maxHeat; return this; }

    public double getMinHeat() { return minHeat; }
    public ExplosionOptions setMinHeat(double minHeat) { this.minHeat = minHeat; return this; }

    public boolean isCreateParticles() { return createParticles; }
    public ExplosionOptions setCreateParticles(boolean createParticles) { this.createParticles = createParticles; return this; }

    public boolean isPlaySound() { return playSound; }
    public ExplosionOptions setPlaySound(boolean playSound) { this.playSound = playSound; return this; }

    public BurnOptions getBurnOptions() { return burnOptions; }
    public ExplosionOptions setBurnOptions(BurnOptions burnOptions) { this.burnOptions = burnOptions; return this; }
}
