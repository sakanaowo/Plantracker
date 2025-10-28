package com.example.tralalero.common.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Predefined color palette for labels
 * Must match backend: plantracker-backend/src/common/constants/labels.constant.ts
 */
public class LabelColors {

    public static class LabelColor {
        private final String name;
        private final String hex;      // Main color (for text/border)
        private final String bg;        // Light background color

        public LabelColor(String name, String hex, String bg) {
            this.name = name;
            this.hex = hex;
            this.bg = bg;
        }

        public String getName() {
            return name;
        }

        public String getHex() {
            return hex;
        }

        public String getBg() {
            return bg;
        }
    }

    public static final List<LabelColor> PALETTE = Arrays.asList(
            new LabelColor("Red", "#EF4444", "#FEE2E2"),
            new LabelColor("Orange", "#F97316", "#FFEDD5"),
            new LabelColor("Amber", "#F59E0B", "#FEF3C7"),
            new LabelColor("Yellow", "#EAB308", "#FEF9C3"),
            new LabelColor("Lime", "#84CC16", "#ECFCCB"),
            new LabelColor("Green", "#22C55E", "#DCFCE7"),
            new LabelColor("Emerald", "#10B981", "#D1FAE5"),
            new LabelColor("Teal", "#14B8A6", "#CCFBF1"),
            new LabelColor("Cyan", "#06B6D4", "#CFFAFE"),
            new LabelColor("Sky", "#0EA5E9", "#E0F2FE"),
            new LabelColor("Blue", "#3B82F6", "#DBEAFE"),
            new LabelColor("Indigo", "#6366F1", "#E0E7FF"),
            new LabelColor("Violet", "#8B5CF6", "#EDE9FE"),
            new LabelColor("Purple", "#A855F7", "#F3E8FF"),
            new LabelColor("Fuchsia", "#D946EF", "#FAE8FF"),
            new LabelColor("Pink", "#EC4899", "#FCE7F3"),
            new LabelColor("Rose", "#F43F5E", "#FFE4E6"),
            new LabelColor("Gray", "#6B7280", "#F3F4F6")
    );

    /**
     * Maximum number of labels that can be assigned to a single task
     */
    public static final int MAX_LABELS_PER_TASK = 5;

    /**
     * Helper function to validate if a color is in the predefined palette
     */
    public static boolean isValidLabelColor(String hex) {
        for (LabelColor color : PALETTE) {
            if (color.getHex().equals(hex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper function to get color by name
     */
    public static LabelColor getLabelColorByName(String name) {
        for (LabelColor color : PALETTE) {
            if (color.getName().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return null;
    }
}
