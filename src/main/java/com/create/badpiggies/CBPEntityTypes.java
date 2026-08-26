package com.create.badpiggies;

import com.create.badpiggies.client.PlungerHarpoonRenderer;
import com.create.badpiggies.entity.PlungerHarpoonEntity;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

import static com.create.badpiggies.CreateBadPiggies.REGISTRATE;


public class CBPEntityTypes {
    public static final EntityEntry<PlungerHarpoonEntity> PLUNGER_HARPOON_PROJECTILE =
            REGISTRATE.entity("plunger_harpoon", PlungerHarpoonEntity::new, MobCategory.MISC)
                    .renderer(() -> PlungerHarpoonRenderer::new)
                    .transform((builder) -> builder.properties(b -> b
                            .clientTrackingRange(8)
                            .sized(0.5f, 0.5f)
                            .updateInterval(1)))
                    .register();

    public static void load() {}
}
