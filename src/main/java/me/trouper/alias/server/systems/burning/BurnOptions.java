package me.trouper.alias.server.systems.burning;

public class BurnOptions {
    private boolean disabled = false;
    private double setFireChance = 1.0 / 80;

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public double getSetFireChance() { return setFireChance; }
    public void setSetFireChance(double setFireChance) { this.setFireChance = setFireChance; }
}
