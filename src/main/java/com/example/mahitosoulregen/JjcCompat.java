package com.example.mahitosoulregen;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

/** Reads Jujutsu Craft's player variables by reflection, so there is no compile-time dependency. */
final class JjcCompat {
    private static final Logger LOG = LogManager.getLogger(MahitoSoulRegen.MOD_ID);
    private static boolean initDone;
    private static Capability<?> cap;
    private static Field tech1, tech2;

    private JjcCompat() {}

    private static synchronized void init() {
        if (initDone) return;
        initDone = true;
        try {
            Class<?> vars = Class.forName("net.mcreator.jujutsucraft.network.JujutsucraftModVariables");
            Field capField = vars.getDeclaredField("PLAYER_VARIABLES_CAPABILITY");
            capField.setAccessible(true);
            cap = (Capability<?>) capField.get(null);

            Class<?> pv = Class.forName("net.mcreator.jujutsucraft.network.JujutsucraftModVariables$PlayerVariables");
            tech1 = pv.getDeclaredField("PlayerCurseTechnique");
            tech2 = pv.getDeclaredField("PlayerCurseTechnique2");
            tech1.setAccessible(true);
            tech2.setAccessible(true);
        } catch (Throwable t) {
            cap = null;
            LOG.warn("Could not hook Jujutsu Craft player variables; player-Mahito detection by technique is disabled. Use the mahito_regen tag instead.", t);
        }
    }

    /** True if this player's first or second technique slot equals the given technique id. */
    static boolean hasTechnique(Entity e, double id) {
        if (id < 0 || !(e instanceof Player)) return false;
        init();
        if (cap == null) return false;
        try {
            Object pv = e.getCapability(cap).resolve().orElse(null);
            if (pv == null) return false;
            return tech1.getDouble(pv) == id || tech2.getDouble(pv) == id;
        } catch (Throwable t) {
            return false;
        }
    }
}
