package me.trouper.alias.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.*;

public class ItemSimilarity {
    
    public static class SimilarityConfiguration {
        private double durabilityWeight = 0.05;
        private double enchantmentsWeight = 0.10;
        private double displayNameWeight = 0.10;
        private double loreWeight = 0.15;
        private double itemFlagsWeight = 0.25;
        private double customModelDataWeight = 0.20;
        private double specificMetaWeight = 0.07;

        private boolean checkDurability = true;
        private boolean checkEnchantments = true;
        private boolean checkDisplayName = true;
        private boolean checkLore = true;
        private boolean checkItemFlags = true;
        private boolean checkCustomModelData = true;
        private boolean checkSpecificMeta = true;

        public static SimilarityConfiguration defaultConfig() {
            return new SimilarityConfiguration();
        }

        public static SimilarityConfiguration fastConfig() {
            return new SimilarityConfiguration()
                    .setCheckDisplayName(false)
                    .setCheckLore(false)
                    .redistributeWeights();
        }

        public static SimilarityConfiguration preciseConfig() {
            return new SimilarityConfiguration()
                    .setDurabilityWeight(0.08)
                    .setEnchantmentsWeight(0.20)
                    .setDisplayNameWeight(0.15)
                    .setLoreWeight(0.20)
                    .setItemFlagsWeight(0.15)
                    .setCustomModelDataWeight(0.10)
                    .setSpecificMetaWeight(0.12);
        }

        private SimilarityConfiguration redistributeWeights() {
            double totalActiveWeight = 0.0;
            int activeFeatures = 0;

            if (checkDurability) {
                totalActiveWeight += durabilityWeight;
                activeFeatures++;
            }
            if (checkEnchantments) {
                totalActiveWeight += enchantmentsWeight;
                activeFeatures++;
            }
            if (checkDisplayName) {
                totalActiveWeight += displayNameWeight;
                activeFeatures++;
            }
            if (checkLore) {
                totalActiveWeight += loreWeight;
                activeFeatures++;
            }
            if (checkItemFlags) {
                totalActiveWeight += itemFlagsWeight;
                activeFeatures++;
            }
            if (checkCustomModelData) {
                totalActiveWeight += customModelDataWeight;
                activeFeatures++;
            }
            if (checkSpecificMeta) {
                totalActiveWeight += specificMetaWeight;
                activeFeatures++;
            }

            if (activeFeatures > 0 && totalActiveWeight != 1.0) {
                double redistributionFactor = 1.0 / totalActiveWeight;

                if (checkDurability) durabilityWeight *= redistributionFactor;
                if (checkEnchantments) enchantmentsWeight *= redistributionFactor;
                if (checkDisplayName) displayNameWeight *= redistributionFactor;
                if (checkLore) loreWeight *= redistributionFactor;
                if (checkItemFlags) itemFlagsWeight *= redistributionFactor;
                if (checkCustomModelData) customModelDataWeight *= redistributionFactor;
                if (checkSpecificMeta) specificMetaWeight *= redistributionFactor;
            }

            return this;
        }

        public double getDurabilityWeight() {
            return durabilityWeight;
        }

        public double getEnchantmentsWeight() {
            return enchantmentsWeight;
        }

        public double getDisplayNameWeight() {
            return displayNameWeight;
        }

        public double getLoreWeight() {
            return loreWeight;
        }

        public double getItemFlagsWeight() {
            return itemFlagsWeight;
        }

        public double getCustomModelDataWeight() {
            return customModelDataWeight;
        }

        public double getSpecificMetaWeight() {
            return specificMetaWeight;
        }

        public boolean isCheckDurability() {
            return checkDurability;
        }

        public boolean isCheckEnchantments() {
            return checkEnchantments;
        }

        public boolean isCheckDisplayName() {
            return checkDisplayName;
        }

        public boolean isCheckLore() {
            return checkLore;
        }

        public boolean isCheckItemFlags() {
            return checkItemFlags;
        }

        public boolean isCheckCustomModelData() {
            return checkCustomModelData;
        }

        public boolean isCheckSpecificMeta() {
            return checkSpecificMeta;
        }

        public SimilarityConfiguration setDurabilityWeight(double weight) {
            this.durabilityWeight = weight;
            return this;
        }

        public SimilarityConfiguration setEnchantmentsWeight(double weight) {
            this.enchantmentsWeight = weight;
            return this;
        }

        public SimilarityConfiguration setDisplayNameWeight(double weight) {
            this.displayNameWeight = weight;
            return this;
        }

        public SimilarityConfiguration setLoreWeight(double weight) {
            this.loreWeight = weight;
            return this;
        }

        public SimilarityConfiguration setItemFlagsWeight(double weight) {
            this.itemFlagsWeight = weight;
            return this;
        }

        public SimilarityConfiguration setCustomModelDataWeight(double weight) {
            this.customModelDataWeight = weight;
            return this;
        }

        public SimilarityConfiguration setSpecificMetaWeight(double weight) {
            this.specificMetaWeight = weight;
            return this;
        }

        public SimilarityConfiguration setCheckDurability(boolean check) {
            this.checkDurability = check;
            return this;
        }

        public SimilarityConfiguration setCheckEnchantments(boolean check) {
            this.checkEnchantments = check;
            return this;
        }

        public SimilarityConfiguration setCheckDisplayName(boolean check) {
            this.checkDisplayName = check;
            return this;
        }

        public SimilarityConfiguration setCheckLore(boolean check) {
            this.checkLore = check;
            return this;
        }

        public SimilarityConfiguration setCheckItemFlags(boolean check) {
            this.checkItemFlags = check;
            return this;
        }

        public SimilarityConfiguration setCheckCustomModelData(boolean check) {
            this.checkCustomModelData = check;
            return this;
        }

        public SimilarityConfiguration setCheckSpecificMeta(boolean check) {
            this.checkSpecificMeta = check;
            return this;
        }

        public SimilarityConfiguration normalize() {
            return redistributeWeights();
        }
    }

    private static final SimilarityConfiguration DEFAULT_CONFIG = SimilarityConfiguration.defaultConfig();

    /**
     * Calculate similarity between two ItemStacks using default configuration
     *
     * @param item1 First ItemStack
     * @param item2 Second ItemStack
     * @return Similarity score from 0.0 to 1.0
     */
    public static double calculateSimilarity(ItemStack item1, ItemStack item2) {
        return calculateSimilarity(item1, item2, DEFAULT_CONFIG);
    }

    /**
     * Calculate similarity between two ItemStacks with custom configuration
     *
     * @param item1  First ItemStack
     * @param item2  Second ItemStack
     * @param config Configuration for weight and feature settings
     * @return Similarity score from 0.0 to 1.0
     */
    public static double calculateSimilarity(ItemStack item1, ItemStack item2, SimilarityConfiguration config) {
        if (item1 == null && item2 == null) return 1.0;
        if (item1 == null || item2 == null) return 0.0;
        if (!item1.getType().equals(item2.getType())) return 0.0;

        double totalScore = 0.0;

        if (config.isCheckDurability()) {
            totalScore += compareDurability(item1, item2) * config.getDurabilityWeight();
        }

        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 == null && meta2 == null) {
            if (config.isCheckDisplayName()) totalScore += config.getDisplayNameWeight();
            if (config.isCheckLore()) totalScore += config.getLoreWeight();
            if (config.isCheckEnchantments()) totalScore += config.getEnchantmentsWeight();
            if (config.isCheckItemFlags()) totalScore += config.getItemFlagsWeight();
            if (config.isCheckCustomModelData()) totalScore += config.getCustomModelDataWeight();
            if (config.isCheckSpecificMeta()) totalScore += config.getSpecificMetaWeight();
        } else if (meta1 == null || meta2 == null) {
            // Do nothing if only one is null, as they don't match.
        } else {
            if (config.isCheckDisplayName()) {
                totalScore += compareDisplayNames(meta1, meta2) * config.getDisplayNameWeight();
            }
            if (config.isCheckLore()) {
                totalScore += compareLore(meta1, meta2) * config.getLoreWeight();
            }
            if (config.isCheckEnchantments()) {
                totalScore += compareEnchantments(meta1, meta2) * config.getEnchantmentsWeight();
            }
            if (config.isCheckItemFlags()) {
                totalScore += compareItemFlags(meta1, meta2) * config.getItemFlagsWeight();
            }
            if (config.isCheckCustomModelData()) {
                totalScore += compareCustomModelData(meta1, meta2) * config.getCustomModelDataWeight();
            }
            if (config.isCheckSpecificMeta()) {
                totalScore += compareSpecificMeta(meta1, meta2) * config.getSpecificMetaWeight();
            }
        }

        return Math.min(1.0, Math.max(0.0, totalScore));
    }


    private static double compareDurability(ItemStack item1, ItemStack item2) {
        if (!(item1.getType().getMaxDurability() > 0)) return 1.0;
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if (meta1 instanceof Damageable && meta2 instanceof Damageable) {
            int damage1 = ((Damageable) meta1).getDamage();
            int damage2 = ((Damageable) meta2).getDamage();

            if (damage1 == damage2) return 1.0;

            int maxDurability = item1.getType().getMaxDurability();
            int difference = Math.abs(damage1 - damage2);

            return 1.0 - ((double) difference / maxDurability);
        }

        return 1.0;
    }

    private static double compareDisplayNames(ItemMeta meta1, ItemMeta meta2) {
        String name1 = meta1.hasDisplayName() ? meta1.getDisplayName() : null;
        String name2 = meta2.hasDisplayName() ? meta2.getDisplayName() : null;

        if (name1 == null && name2 == null) return 1.0;
        if (name1 == null || name2 == null) return 0.0;

        return name1.equals(name2) ? 1.0 : calculateStringSimilarity(name1, name2);
    }

    private static double compareLore(ItemMeta meta1, ItemMeta meta2) {
        List<String> lore1 = meta1.hasLore() ? meta1.getLore() : null;
        List<String> lore2 = meta2.hasLore() ? meta2.getLore() : null;

        if (lore1 == null && lore2 == null) return 1.0;
        if (lore1 == null || lore2 == null) return 0.0;

        return compareStringLists(lore1, lore2);
    }

    private static double compareEnchantments(ItemMeta meta1, ItemMeta meta2) {
        Map<Enchantment, Integer> enchants1 = meta1.getEnchants();
        Map<Enchantment, Integer> enchants2 = meta2.getEnchants();

        if (enchants1.isEmpty() && enchants2.isEmpty()) return 1.0;
        if (enchants1.isEmpty() || enchants2.isEmpty()) {
            return enchants1.isEmpty() && enchants2.isEmpty() ? 1.0 : 0.0;
        }

        Set<Enchantment> allEnchants = new HashSet<>(enchants1.keySet());
        allEnchants.addAll(enchants2.keySet());

        double totalSimilarity = 0.0;

        for (Enchantment enchant : allEnchants) {
            int level1 = enchants1.getOrDefault(enchant, 0);
            int level2 = enchants2.getOrDefault(enchant, 0);

            if (level1 == 0 || level2 == 0) {
                continue;
            }

            int maxLevel = Math.max(level1, level2);
            int difference = Math.abs(level1 - level2);
            double enchantSimilarity = 1.0 - ((double) difference / maxLevel);

            totalSimilarity += enchantSimilarity;
        }

        return allEnchants.isEmpty() ? 1.0 : totalSimilarity / allEnchants.size();
    }

    private static double compareItemFlags(ItemMeta meta1, ItemMeta meta2) {
        Set<ItemFlag> flags1 = meta1.getItemFlags();
        Set<ItemFlag> flags2 = meta2.getItemFlags();

        if (flags1.isEmpty() && flags2.isEmpty()) return 1.0;

        Set<ItemFlag> allFlags = new HashSet<>(flags1);
        allFlags.addAll(flags2);

        Set<ItemFlag> commonFlags = new HashSet<>(flags1);
        commonFlags.retainAll(flags2);

        return allFlags.isEmpty() ? 1.0 : (double) commonFlags.size() / allFlags.size();
    }

    private static double compareCustomModelData(ItemMeta meta1, ItemMeta meta2) {
        boolean hasCustom1 = meta1.hasCustomModelData();
        boolean hasCustom2 = meta2.hasCustomModelData();

        if (!hasCustom1 && !hasCustom2) return 1.0;
        if (hasCustom1 != hasCustom2) return 0.0;

        return meta1.getCustomModelData() == meta2.getCustomModelData() ? 1.0 : 0.0;
    }

    private static double compareSpecificMeta(ItemMeta meta1, ItemMeta meta2) {
        if (!meta1.getClass().equals(meta2.getClass())) return 0.0;

        if (meta1 instanceof PotionMeta) {
            return comparePotionMeta((PotionMeta) meta1, (PotionMeta) meta2);
        } else if (meta1 instanceof BookMeta) {
            return compareBookMeta((BookMeta) meta1, (BookMeta) meta2);
        } else if (meta1 instanceof LeatherArmorMeta) {
            return compareLeatherArmorMeta((LeatherArmorMeta) meta1, (LeatherArmorMeta) meta2);
        } else if (meta1 instanceof FireworkMeta) {
            return compareFireworkMeta((FireworkMeta) meta1, (FireworkMeta) meta2);
        } else if (meta1 instanceof BannerMeta) {
            return compareBannerMeta((BannerMeta) meta1, (BannerMeta) meta2);
        } else if (meta1 instanceof SkullMeta) {
            return compareSkullMeta((SkullMeta) meta1, (SkullMeta) meta2);
        } else if (meta1 instanceof ArmorMeta) {
            return compareArmorMeta((ArmorMeta) meta1, (ArmorMeta) meta2);
        } else if (meta1 instanceof MusicInstrumentMeta) {
            return compareMusicInstrumentMeta((MusicInstrumentMeta) meta1, (MusicInstrumentMeta) meta2);
        }

        return 1.0;
    }

    private static double comparePotionMeta(PotionMeta meta1, PotionMeta meta2) {
        double score = 0.0;
        int comparisons = 0;

        PotionType type1 = meta1.getBasePotionType();
        PotionType type2 = meta2.getBasePotionType();
        score += (type1 == type2) ? 1.0 : 0.0;
        comparisons++;

        List<PotionEffect> effects1 = meta1.getCustomEffects();
        List<PotionEffect> effects2 = meta2.getCustomEffects();
        score += comparePotionEffects(effects1, effects2);
        comparisons++;

        Color color1 = meta1.getColor();
        Color color2 = meta2.getColor();
        score += Objects.equals(color1, color2) ? 1.0 : 0.0;
        comparisons++;

        return score / comparisons;
    }

    private static double compareBookMeta(BookMeta meta1, BookMeta meta2) {
        double score = 0.0;
        int comparisons = 0;

        String title1 = meta1.getTitle();
        String title2 = meta2.getTitle();
        score += Objects.equals(title1, title2) ? 1.0 :
                (title1 != null && title2 != null ? calculateStringSimilarity(title1, title2) : 0.0);
        comparisons++;

        String author1 = meta1.getAuthor();
        String author2 = meta2.getAuthor();
        score += Objects.equals(author1, author2) ? 1.0 :
                (author1 != null && author2 != null ? calculateStringSimilarity(author1, author2) : 0.0);
        comparisons++;

        List<String> pages1 = meta1.getPages();
        List<String> pages2 = meta2.getPages();
        score += compareStringLists(pages1, pages2);
        comparisons++;

        BookMeta.Generation gen1 = meta1.getGeneration();
        BookMeta.Generation gen2 = meta2.getGeneration();
        score += Objects.equals(gen1, gen2) ? 1.0 : 0.0;
        comparisons++;

        return score / comparisons;
    }

    private static double compareLeatherArmorMeta(LeatherArmorMeta meta1, LeatherArmorMeta meta2) {
        Color color1 = meta1.getColor();
        Color color2 = meta2.getColor();

        if (color1.equals(color2)) return 1.0;

        int r1 = color1.getRed(), g1 = color1.getGreen(), b1 = color1.getBlue();
        int r2 = color2.getRed(), g2 = color2.getGreen(), b2 = color2.getBlue();

        double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
        double maxDistance = Math.sqrt(3 * Math.pow(255, 2));

        return 1.0 - (distance / maxDistance);
    }

    private static double compareFireworkMeta(FireworkMeta meta1, FireworkMeta meta2) {
        double score = 0.0;
        int comparisons = 0;

        score += (meta1.getPower() == meta2.getPower()) ? 1.0 : 0.0;
        comparisons++;

        List<FireworkEffect> effects1 = meta1.getEffects();
        List<FireworkEffect> effects2 = meta2.getEffects();
        score += compareFireworkEffects(effects1, effects2);
        comparisons++;

        return score / comparisons;
    }

    private static double compareBannerMeta(BannerMeta meta1, BannerMeta meta2) {
        List<Pattern> patterns1 = meta1.getPatterns();
        List<Pattern> patterns2 = meta2.getPatterns();

        return compareBannerPatterns(patterns1, patterns2);
    }

    private static double compareSkullMeta(SkullMeta meta1, SkullMeta meta2) {
        String owner1 = meta1.getOwner();
        String owner2 = meta2.getOwner();

        return Objects.equals(owner1, owner2) ? 1.0 : 0.0;
    }

    private static double compareArmorMeta(ArmorMeta meta1, ArmorMeta meta2) {
        ArmorTrim trim1 = meta1.getTrim();
        ArmorTrim trim2 = meta2.getTrim();

        if (trim1 == null && trim2 == null) return 1.0;
        if (trim1 == null || trim2 == null) return 0.0;

        return trim1.equals(trim2) ? 1.0 : 0.0;
    }

    private static double compareMusicInstrumentMeta(MusicInstrumentMeta meta1, MusicInstrumentMeta meta2) {
        return Objects.equals(meta1.getInstrument(), meta2.getInstrument()) ? 1.0 : 0.0;
    }

    private static double calculateStringSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        if (str1.equals(str2)) return 1.0;

        Set<String> bigrams1 = getBigrams(str1.toLowerCase());
        Set<String> bigrams2 = getBigrams(str2.toLowerCase());

        Set<String> intersection = new HashSet<>(bigrams1);
        intersection.retainAll(bigrams2);

        Set<String> union = new HashSet<>(bigrams1);
        union.addAll(bigrams2);

        return union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
    }

    private static Set<String> getBigrams(String input) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < input.length() - 1; i++) {
            result.add(input.substring(i, i + 2));
        }
        return result;
    }

    private static double compareStringLists(List<String> list1, List<String> list2) {
        if (list1.isEmpty() && list2.isEmpty()) return 1.0;
        if (list1.isEmpty() || list2.isEmpty()) return 0.0;

        int maxSize = Math.max(list1.size(), list2.size());
        double totalSimilarity = 0.0;

        for (int i = 0; i < maxSize; i++) {
            String str1 = i < list1.size() ? list1.get(i) : null;
            String str2 = i < list2.size() ? list2.get(i) : null;

            if (str1 == null && str2 == null) {
                totalSimilarity += 1.0;
            } else if (str1 == null || str2 == null) {
                totalSimilarity += 0.0;
            } else {
                totalSimilarity += calculateStringSimilarity(str1, str2);
            }
        }

        return totalSimilarity / maxSize;
    }

    private static double comparePotionEffects(List<PotionEffect> effects1, List<PotionEffect> effects2) {
        if (effects1.isEmpty() && effects2.isEmpty()) return 1.0;
        if (effects1.isEmpty() || effects2.isEmpty()) return 0.0;

        Map<org.bukkit.potion.PotionEffectType, PotionEffect> map1 = new HashMap<>();
        Map<org.bukkit.potion.PotionEffectType, PotionEffect> map2 = new HashMap<>();

        for (PotionEffect effect : effects1) map1.put(effect.getType(), effect);
        for (PotionEffect effect : effects2) map2.put(effect.getType(), effect);

        Set<org.bukkit.potion.PotionEffectType> allTypes = new HashSet<>(map1.keySet());
        allTypes.addAll(map2.keySet());

        double totalSimilarity = 0.0;

        for (org.bukkit.potion.PotionEffectType type : allTypes) {
            PotionEffect effect1 = map1.get(type);
            PotionEffect effect2 = map2.get(type);

            if (effect1 == null || effect2 == null) continue;

            double similarity = 0.0;

            int duration1 = effect1.getDuration();
            int duration2 = effect2.getDuration();
            int maxDuration = Math.max(duration1, duration2);
            similarity += maxDuration == 0 ? 1.0 : 1.0 - ((double) Math.abs(duration1 - duration2) / maxDuration);

            int amp1 = effect1.getAmplifier();
            int amp2 = effect2.getAmplifier();
            similarity += (amp1 == amp2) ? 1.0 : 0.0;

            similarity += (effect1.isAmbient() == effect2.isAmbient()) ? 1.0 : 0.0;
            similarity += (effect1.hasParticles() == effect2.hasParticles()) ? 1.0 : 0.0;
            similarity += (effect1.hasIcon() == effect2.hasIcon()) ? 1.0 : 0.0;

            totalSimilarity += similarity / 5.0;
        }

        return allTypes.isEmpty() ? 1.0 : totalSimilarity / allTypes.size();
    }

    private static double compareFireworkEffects(List<FireworkEffect> effects1, List<FireworkEffect> effects2) {
        if (effects1.isEmpty() && effects2.isEmpty()) return 1.0;
        if (effects1.size() != effects2.size()) return 0.0;

        double totalSimilarity = 0.0;

        for (int i = 0; i < effects1.size(); i++) {
            FireworkEffect effect1 = effects1.get(i);
            FireworkEffect effect2 = effects2.get(i);

            double similarity = 0.0;
            int comparisons = 0;

            similarity += (effect1.getType() == effect2.getType()) ? 1.0 : 0.0;
            comparisons++;

            similarity += compareColors(effect1.getColors(), effect2.getColors());
            comparisons++;

            similarity += compareColors(effect1.getFadeColors(), effect2.getFadeColors());
            comparisons++;

            similarity += (effect1.hasTrail() == effect2.hasTrail()) ? 1.0 : 0.0;
            comparisons++;

            similarity += (effect1.hasFlicker() == effect2.hasFlicker()) ? 1.0 : 0.0;
            comparisons++;

            totalSimilarity += similarity / comparisons;
        }

        return totalSimilarity / effects1.size();
    }

    private static double compareColors(List<Color> colors1, List<Color> colors2) {
        if (colors1.isEmpty() && colors2.isEmpty()) return 1.0;
        if (colors1.size() != colors2.size()) return 0.0;

        double totalSimilarity = 0.0;

        for (int i = 0; i < colors1.size(); i++) {
            Color color1 = colors1.get(i);
            Color color2 = colors2.get(i);

            if (color1.equals(color2)) {
                totalSimilarity += 1.0;
            } else {
                int r1 = color1.getRed(), g1 = color1.getGreen(), b1 = color1.getBlue();
                int r2 = color2.getRed(), g2 = color2.getGreen(), b2 = color2.getBlue();

                double distance = Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
                double maxDistance = Math.sqrt(3 * Math.pow(255, 2));

                totalSimilarity += 1.0 - (distance / maxDistance);
            }
        }

        return totalSimilarity / colors1.size();
    }

    private static double compareBannerPatterns(List<Pattern> patterns1, List<Pattern> patterns2) {
        if (patterns1.isEmpty() && patterns2.isEmpty()) return 1.0;
        if (patterns1.size() != patterns2.size()) return 0.0;

        double totalSimilarity = 0.0;

        for (int i = 0; i < patterns1.size(); i++) {
            Pattern pattern1 = patterns1.get(i);
            Pattern pattern2 = patterns2.get(i);

            double similarity = 0.0;

            similarity += (pattern1.getPattern() == pattern2.getPattern()) ? 1.0 : 0.0;

            similarity += (pattern1.getColor() == pattern2.getColor()) ? 1.0 : 0.0;

            totalSimilarity += similarity / 2.0;
        }

        return totalSimilarity / patterns1.size();
    }
}