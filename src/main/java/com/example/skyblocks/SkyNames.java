package com.example.skyblocks;

import java.util.Locale;

/**
 * THE list of Shadered+ skies. Add a new sky here (and in tools/skies/order.txt, same position).
 * No dependencies on purpose: it is read while Shadered's SkyblockType enum is being created.
 *
 * For each name:
 *  - Shadered sky type   SKYBLOCKS_<NAME>   (SkyblockType, added by SkyblockTypeMixin)
 *  - Shadered item type  SKYBLOCKS_<NAME>   (SkyblockItemTypes, added by SkyblockItemTypesMixin)
 *  - item                skyblocks:<name>_sky_block (a Shadered SkyblockItem)
 *  - skybox textures     skyblocks:textures/environment/<name>/
 *  - lang keys           skyblock_type.skyblocks.<name>, item.skyblocks.<name>_sky_block
 */
public final class SkyNames {
    public static final String[] NAMES = {"jupiter", "cat", "twilight", "hell", "nebula", "aurora", "snow"};

    public static String enumName(int index) {
        return "SKYBLOCKS_" + NAMES[index].toUpperCase(Locale.ROOT);
    }

    public static String itemId(int index) {
        return NAMES[index] + "_sky_block";
    }

    private SkyNames() {}
}
