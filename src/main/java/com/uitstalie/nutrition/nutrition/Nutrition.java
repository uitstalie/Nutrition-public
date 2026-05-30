package com.uitstalie.nutrition.nutrition;

import com.uitstalie.nutrition.nutrition.api.data.NutritionDataRegistry;
import com.uitstalie.nutrition.nutrition.api.data.config.NutritionConfigProvider;
import com.uitstalie.nutrition.nutrition.api.data.effect.NutritionEffectProvider;
import com.uitstalie.nutrition.nutrition.api.data.group.NutritionGroupProvider;
import com.uitstalie.nutrition.nutrition.api.data.item.NutritionItemProvider;
import com.uitstalie.nutrition.nutrition.capabilities.NutritionCapabilities;
import com.uitstalie.nutrition.nutrition.network.NetworkPayloadHandler;
import com.uitstalie.nutrition.nutrition.server.NutritionCommand;
import com.uitstalie.nutrition.nutrition.server.NutritionTestCommand;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import com.uitstalie.nutrition.nutrition.service.NutritionRuntimeService;
import com.uitstalie.nutrition.nutrition.service.NutritionAutoGenerateService;
import com.uitstalie.nutrition.nutrition.util.ticker.Ticker;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(Nutrition.MOD_ID)
public class Nutrition {

    public static final String MOD_ID = "nutrition";

    public Nutrition(IEventBus modEventBus, ModContainer modContainer) {
        NutritionDataRegistry.initialize();

        modEventBus.addListener(this::gatherData);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);

        modEventBus.addListener(RegisterCapabilitiesEvent.class, NutritionCapabilities::attachCapabilityPlayer);
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, NetworkPayloadHandler::register);
        AttributeTypeRegistry.register(modEventBus);

        Ticker.addCallback(NutritionRuntimeService::onSecondEvent);
    }

    /**
     * 注册 /nutrition 命令。
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NutritionCommand.register(event.getDispatcher());
        NutritionTestCommand.register(event.getDispatcher());
    }

    /**
     * Phase 5/6: 玩家登录时全量同步 + 立即触发 effect 重算。
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        NutritionRuntimeService.onPlayerLogin(player);
    }

    /**
     * 服务端启动完成后，若 mod 列表有变化则自动运行 autogen。
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        NutritionAutoGenerateService.autoGenerateIfNeeded(event.getServer());
    }

    private void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(),
                new NutritionConfigProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper())
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new NutritionGroupProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper())
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new NutritionItemProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper())
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new NutritionEffectProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper())
        );
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        NutritionDataRegistry.addReloadListeners(event);
    }
}
