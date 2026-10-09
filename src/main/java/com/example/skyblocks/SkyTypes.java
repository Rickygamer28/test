package com.example.skyblocks;

import com.noodlegamer76.shadered.client.util.SkyblockType;
import com.noodlegamer76.shadered.item.SkyblockItemTypes;

/** Translates between our sky index (position in SkyNames.NAMES) and Shadered's enums. */
public final class SkyTypes {
    public static SkyblockType type(int index) {
        return SkyblockType.valueOf(SkyNames.enumName(index));
    }

    public static SkyblockItemTypes itemType(int index) {
        return SkyblockItemTypes.valueOf(SkyNames.enumName(index));
    }

    /** Our sky index for a Shadered sky type, or -1 if it is one of Shadered's own. */
    public static int index(SkyblockType type) {
        if (type == null) {
            return -1;
        }
        for (int i = 0; i < SkyNames.NAMES.length; i++) {
            if (type.name().equals(SkyNames.enumName(i))) {
                return i;
            }
        }
        return -1;
    }

    private SkyTypes() {}
}
