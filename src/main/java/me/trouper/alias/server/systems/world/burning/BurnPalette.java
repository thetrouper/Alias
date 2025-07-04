package me.trouper.alias.server.systems.world.burning;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BurnPalette {
    private final List<List<BlockData>> burnWave;
    private final List<Material> firePalette;
    private final List<BlockData> preBurn;
    private final List<BlockData> midBurn;
    private final List<BlockData> midHeatPalette;
    private final List<BlockData> highHeat;

    private final List<BlockData> shortGrassPalette;
    private final List<BlockData> grassPalette;
    private final List<BlockData> stonePalette;
    private final List<BlockData> leavesPalette;
    private final List<BlockData> stoneBrickSlabPalette;
    private final List<BlockData> stoneBrickStairsPalette;
    private final List<BlockData> darkPrismarinePalette;
    private final List<BlockData> burnWaveTrail;

    public BurnPalette() {
        this.burnWaveTrail = Arrays.asList(
                Material.ORANGE_STAINED_GLASS.createBlockData(),
                Material.BLACK_STAINED_GLASS.createBlockData(),
                Material.GRAY_STAINED_GLASS.createBlockData(),
                Material.LIGHT_GRAY_STAINED_GLASS.createBlockData()
        );

        List<BlockData> baseBurnWave = new ArrayList<>();
        addRepeated(baseBurnWave, Material.ORANGE_STAINED_GLASS, 3);
        addRepeated(baseBurnWave, Material.SHROOMLIGHT, 2);
        baseBurnWave.add(Material.ORANGE_TERRACOTTA.createBlockData());
        baseBurnWave.add(Material.ORANGE_CONCRETE.createBlockData());
        baseBurnWave.add(Material.HONEYCOMB_BLOCK.createBlockData());

        this.burnWave = new ArrayList<>();
        for (BlockData block : baseBurnWave) {
            List<BlockData> waveStage = new ArrayList<>();
            waveStage.add(block);
            waveStage.add(block);
            waveStage.add(block);
            waveStage.addAll(burnWaveTrail);
            this.burnWave.add(waveStage);
        }

        this.firePalette = Arrays.asList(Material.FIRE);
        this.preBurn = Arrays.asList(Material.ORANGE_TERRACOTTA.createBlockData());
        this.midBurn = Arrays.asList(
                Material.MAGMA_BLOCK.createBlockData(),
                Material.ORANGE_TERRACOTTA.createBlockData()
        );
        this.midHeatPalette = Arrays.asList(Material.MAGMA_BLOCK.createBlockData());
        this.highHeat = Arrays.asList(
                Material.MAGMA_BLOCK.createBlockData(),
                Material.BLACKSTONE.createBlockData(),
                Material.MUD.createBlockData(),
                Material.TUFF.createBlockData()
        );

        this.shortGrassPalette = createShortGrassPalette();
        this.grassPalette = createGrassPalette();
        this.stonePalette = createStonePalette();
        this.leavesPalette = createLeavesPalette();
        this.stoneBrickSlabPalette = createStoneBrickSlabPalette();
        this.stoneBrickStairsPalette = createStoneBrickStairsPalette();
        this.darkPrismarinePalette = createDarkPrismarinePalette();
    }

    public List<BurnStage> burn(Block block, float heat) {
        Material type = block.getType();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (type == Material.SHORT_GRASS) {
            return Arrays.asList(new BurnStage(
                    random.nextLong(1, 10),
                    getRandomElement(shortGrassPalette)
            ));
        }

        if (Tag.LOGS_THAT_BURN.isTagged(type)) {
            BlockData preservedData = Material.POLISHED_BASALT.createBlockData();
            block.getBlockData().copyTo(preservedData);

            return Arrays.asList(
                    new BurnStage(random.nextLong(0, 1), getRandomElement(preBurn)),
                    new BurnStage(random.nextLong(1, 10), getRandomElement(midBurn)),
                    new BurnStage(random.nextLong(20, 200), preservedData)
            );
        }

        if (Tag.LEAVES.isTagged(type)) {
            return Arrays.asList(
                    new BurnStage(random.nextLong(1, 10), getRandomElement(preBurn)),
                    new BurnStage(random.nextLong(1, 10), getRandomElement(midBurn)),
                    new BurnStage(random.nextLong(10, 80), getRandomElement(leavesPalette))
            );
        }

        if (type == Material.STONE_BRICK_STAIRS) {
            BlockData preservedData = getRandomElement(stoneBrickStairsPalette).clone();
            block.getBlockData().copyTo(preservedData);
            return Arrays.asList(new BurnStage(random.nextLong(10, 60), preservedData));
        }

        if (type == Material.STONE_BRICK_SLAB) {
            BlockData preservedData = getRandomElement(stoneBrickSlabPalette).clone();
            block.getBlockData().copyTo(preservedData);
            return Arrays.asList(new BurnStage(random.nextLong(10, 60), preservedData));
        }

        Set<Material> grassTypes = Set.of(Material.GRASS_BLOCK, Material.DIRT, Material.SNOW_BLOCK,
                Material.SAND, Material.PODZOL, Material.COARSE_DIRT);
        if (grassTypes.contains(type)) {
            return Arrays.asList(
                    new BurnStage(random.nextLong(1, 5), getRandomElement(midBurn)),
                    new BurnStage(random.nextLong(10, 60), getRandomElement(applyHeat(grassPalette, heat)))
            );
        }

        Set<Material> stoneTypes = Set.of(Material.STONE, Material.ANDESITE, Material.GRAVEL, Material.COBBLESTONE);
        if (stoneTypes.contains(type)) {
            return Arrays.asList(
                    new BurnStage(random.nextLong(1, 5), getRandomElement(midBurn)),
                    new BurnStage(random.nextLong(10, 60), getRandomElement(applyHeat(stonePalette, heat)))
            );
        }

        if (type == Material.DARK_PRISMARINE) {
            return Arrays.asList(new BurnStage(
                    random.nextLong(10, 60),
                    getRandomElement(applyHeat(darkPrismarinePalette, heat))
            ));
        }

        if (Tag.WOODEN_FENCES.isTagged(type) || Tag.WOODEN_DOORS.isTagged(type) || Tag.WOODEN_TRAPDOORS.isTagged(type)) {
            if (random.nextFloat() < 0.2) return null;
            return Arrays.asList(new BurnStage(random.nextLong(10, 60), Material.AIR.createBlockData()));
        }

        if (!type.isSolid()) {
            return Arrays.asList(new BurnStage(random.nextLong(1, 10), Material.AIR.createBlockData()));
        }

        return null;
    }

    public List<Material> getFirePalette() {
        return firePalette;
    }

    public List<BlockData> getSmokePalette() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (random.nextFloat() < 0.25) {
            List<Material> options = Arrays.asList(Material.BLACK_CONCRETE, Material.ORANGE_TERRACOTTA);
            return Arrays.asList(
                    Material.SHROOMLIGHT.createBlockData(),
                    Material.ORANGE_CONCRETE.createBlockData(),
                    Material.ORANGE_TERRACOTTA.createBlockData(),
                    options.get(random.nextInt(options.size())).createBlockData()
            );
        }

        return Arrays.asList(
                Material.SHROOMLIGHT.createBlockData(),
                Material.ORANGE_CONCRETE.createBlockData(),
                Material.ORANGE_STAINED_GLASS.createBlockData(),
                Material.BLACK_STAINED_GLASS.createBlockData()
        );
    }

    private List<BlockData> applyHeat(List<BlockData> list, float heat) {
        if (heat > 0.9) return highHeat;
        if (heat < 0.2) return list;

        List<BlockData> result = new ArrayList<>(list);
        result.addAll(midHeatPalette);
        return result;
    }

    private void addRepeated(List<BlockData> list, Material material, int count) {
        for (int i = 0; i < count; i++) {
            list.add(material.createBlockData());
        }
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private List<BlockData> createShortGrassPalette() {
        List<BlockData> palette = new ArrayList<>();
        addRepeated(palette, Material.DEAD_BUSH, 12);
        palette.add(Material.DEAD_BRAIN_CORAL_FAN.createBlockData());
        palette.add(Material.DEAD_BRAIN_CORAL.createBlockData());
        palette.add(Material.DEAD_BUBBLE_CORAL_FAN.createBlockData());
        palette.add(Material.DEAD_FIRE_CORAL_FAN.createBlockData());
        palette.add(Material.DEAD_FIRE_CORAL.createBlockData());
        palette.add(Material.DEAD_HORN_CORAL_FAN.createBlockData());
        palette.add(Material.DEAD_TUBE_CORAL_FAN.createBlockData());

        for (BlockData data : palette) {
            if (data instanceof Waterlogged) {
                ((Waterlogged) data).setWaterlogged(false);
            }
        }
        return palette;
    }

    private List<BlockData> createGrassPalette() {
        List<BlockData> palette = new ArrayList<>();
        addRepeated(palette, Material.COARSE_DIRT, 4);
        addRepeated(palette, Material.ROOTED_DIRT, 4);
        addRepeated(palette, Material.TUFF, 2);
        palette.add(Material.DEAD_HORN_CORAL_BLOCK.createBlockData());
        palette.add(Material.DEAD_FIRE_CORAL_BLOCK.createBlockData());
        return palette;
    }

    private List<BlockData> createStonePalette() {
        List<BlockData> palette = new ArrayList<>();
        addRepeated(palette, Material.TUFF, 3);
        palette.add(Material.ANDESITE.createBlockData());
        palette.add(Material.DEAD_HORN_CORAL_BLOCK.createBlockData());
        palette.add(Material.DEEPSLATE.createBlockData());
        return palette;
    }

    private List<BlockData> createLeavesPalette() {
        List<BlockData> palette = new ArrayList<>();
        palette.add(Material.MANGROVE_ROOTS.createBlockData());
        addRepeated(palette, Material.AIR, 5);
        return palette;
    }

    private List<BlockData> createStoneBrickSlabPalette() {
        List<BlockData> palette = new ArrayList<>();
        addRepeated(palette, Material.STONE_BRICK_SLAB, 15);
        palette.add(Material.ANDESITE_SLAB.createBlockData());
        palette.add(Material.COBBLESTONE_SLAB.createBlockData());
        palette.add(Material.TUFF_SLAB.createBlockData());
        return palette;
    }

    private List<BlockData> createStoneBrickStairsPalette() {
        List<BlockData> palette = new ArrayList<>();
        addRepeated(palette, Material.STONE_BRICK_STAIRS, 15);
        palette.add(Material.ANDESITE_STAIRS.createBlockData());
        palette.add(Material.COBBLESTONE_STAIRS.createBlockData());
        palette.add(Material.TUFF_STAIRS.createBlockData());
        return palette;
    }

    private List<BlockData> createDarkPrismarinePalette() {
        List<BlockData> palette = new ArrayList<>();
        addRepeated(palette, Material.DARK_PRISMARINE, 15);
        palette.add(Material.DEEPSLATE.createBlockData());
        palette.add(Material.COBBLED_DEEPSLATE.createBlockData());
        return palette;
    }
}