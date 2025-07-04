package me.trouper.alias.server.systems.world.burning;

public class BurnOptions {
    private boolean disabled = false;
    private double setFireChance = 0.2;

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public double getSetFireChance() { return setFireChance; }
    public void setSetFireChance(double setFireChance) { this.setFireChance = setFireChance; }
}
