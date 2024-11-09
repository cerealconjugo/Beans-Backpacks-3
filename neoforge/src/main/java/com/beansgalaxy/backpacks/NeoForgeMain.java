package com.beansgalaxy.backpacks;


import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.fluids.FluidStack;

@Mod(Constants.MOD_ID)
public class NeoForgeMain {

    public static final DataComponentType<FluidStack>
                DATA_FLUID = Traits.register("data_fluid", FluidStack.CODEC, FluidStack.STREAM_CODEC);

    public NeoForgeMain(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

    }
}