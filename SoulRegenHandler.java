package com.example.mahitosoulregen;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Damage Mahito actually takes (after Jujutsu Craft's own reduction) from attackers who cannot
 * observe the soul is healed back quickly over the following ticks.
 */
@Mod.EventBusSubscriber(modid = MahitoSoulRegen.MOD_ID)
public class SoulRegenHandler {
    private static final String POOL_KEY = "mahitosoulregen_pool";

    // LOWEST priority: run after Jujutsu Craft has applied its own damage reduction,
    // so the amount we see is what Mahito actually takes.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || !isMahito(victim)) return;

        DamageSource source = event.getSource();
        Entity attacker = resolveAttacker(source);
        if (attacker == null) {
            if (!RegenConfig.ENVIRONMENT_UNOBSERVED.get()) return;
        } else if (canObserveSoul(attacker)) {
            return; // observed damage lands normally
        }

        // Mirror the cases where Jujutsu Craft itself does NOT apply its Mahito reduction
        // (it skips Mahito while Unstable / Domain Amplification, and for "ignore" attacks).
        if (RegenConfig.MIRROR_JJC_EXEMPTIONS.get()) {
            for (String id : RegenConfig.EXEMPT_VICTIM_EFFECTS.get()) {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                MobEffect eff = rl == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(rl);
                if (eff != null && victim.hasEffect(eff)) return;
            }
            if (hasIgnoreFlag(source.getEntity()) || hasIgnoreFlag(source.getDirectEntity())) return;
        }

        float amount = event.getAmount();
        if (amount <= 0f) return;

        float instant = amount * RegenConfig.INSTANT_FRACTION.get().floatValue();
        event.setAmount(amount - instant);

        CompoundTag data = victim.getPersistentData();
        data.putFloat(POOL_KEY, data.getFloat(POOL_KEY) + (amount - instant));
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level().isClientSide) return;

        CompoundTag data = e.getPersistentData();
        if (!data.contains(POOL_KEY)) return;

        float pool = data.getFloat(POOL_KEY);
        if (pool <= 0.01f || !e.isAlive()) {
            data.remove(POOL_KEY);
            return;
        }
        float step = Math.min(pool, Math.max(0.05f, pool * RegenConfig.REGEN_PER_TICK.get().floatValue()));
        e.heal(step);
        pool -= step;
        if (pool <= 0.01f) data.remove(POOL_KEY); else data.putFloat(POOL_KEY, pool);
    }

    private static boolean hasIgnoreFlag(Entity e) {
        return e != null && e.getPersistentData().getBoolean("ignore");
    }

    /**
     * Jujutsu Craft re-issues Mahito's damage with the projectile (or the attacker) as the source,
     * so the source entity is not always the real attacker. Walk back to the owner.
     */
    private static Entity resolveAttacker(DamageSource source) {
        Entity e = source.getEntity();
        if (e == null) e = source.getDirectEntity();
        if (e == null) return null;
        if (e instanceof Projectile p && p.getOwner() != null) return p.getOwner();
        String uuid = e.getPersistentData().getString("OWNER_UUID");
        if (!uuid.isEmpty() && e.level() instanceof ServerLevel sl) {
            try {
                Entity owner = sl.getEntity(UUID.fromString(uuid));
                if (owner != null) return owner;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return e;
    }

    private static boolean isMahito(Entity e) {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
        if (key != null && RegenConfig.MAHITO_IDS.get().contains(key.toString())) return true;
        String tag = RegenConfig.MAHITO_TAG.get();
        if (!tag.isEmpty() && e.getTags().contains(tag)) return true;
        return JjcCompat.hasTechnique(e, RegenConfig.MAHITO_TECHNIQUE_ID.get());
    }

    private static boolean canObserveSoul(Entity attacker) {
        // 1) JJC's own entity-type tag (Sukuna, Itadori, Toji, Mahito, ...)
        ResourceLocation tagId = ResourceLocation.tryParse(RegenConfig.OBSERVER_ENTITY_TYPE_TAG.get());
        if (tagId != null && attacker.getType().is(TagKey.create(Registries.ENTITY_TYPE, tagId))) return true;

        // 2) extra IDs and manual tag from the config
        var key = ForgeRegistries.ENTITY_TYPES.getKey(attacker.getType());
        if (key != null && RegenConfig.EXTRA_OBSERVER_IDS.get().contains(key.toString())) return true;
        String tag = RegenConfig.OBSERVER_TAG.get();
        if (!tag.isEmpty() && attacker.getTags().contains(tag)) return true;

        // 3) players with the "Observation of the Soul" advancement
        if (attacker instanceof ServerPlayer sp && sp.getServer() != null) {
            ResourceLocation advId = ResourceLocation.tryParse(RegenConfig.OBSERVER_ADVANCEMENT.get());
            if (advId != null) {
                Advancement adv = sp.getServer().getAdvancements().getAdvancement(advId);
                if (adv != null && sp.getAdvancements().getOrStartProgress(adv).isDone()) return true;
            }
        }
        return false;
    }
}
