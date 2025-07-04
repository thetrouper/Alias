package me.trouper.alias.server.systems.display;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.systems.display.tracing.BlockDisplayRaytracer;
import me.trouper.alias.server.systems.display.tracing.CustomRaytracer;
import me.trouper.alias.server.systems.display.visual.Outliner;
import me.trouper.alias.server.systems.display.visual.Patterns;

public class DisplayManager {
    private final Patterns patterns;
    private final Outliner outliner;
    private final BlockDisplayRaytracer blockDisplayRaytracer;
    private final CustomRaytracer customRaytracer;

    public DisplayManager(AliasContext context) {
        this.patterns = new Patterns(context);
        this.blockDisplayRaytracer = new BlockDisplayRaytracer(context);
        this.outliner = new Outliner(blockDisplayRaytracer);
        this.customRaytracer = new CustomRaytracer(context);
    }

    public Patterns getPatterns() { return patterns; }
    public Outliner getOutliner() { return outliner; }
    public BlockDisplayRaytracer getBlockDisplayRaytracer() { return blockDisplayRaytracer; }
    public CustomRaytracer getCustomRaytracer() { return customRaytracer; }
}
