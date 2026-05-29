package com.uitstalie.nutrition.nutrition.util.ticker;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.api.data.NutritionDataRegistry;
import com.uitstalie.nutrition.nutrition.api.data.config.NutritionConfigJson;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import com.uitstalie.nutrition.nutrition.util.log.Log;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 统一 second event 定时器。
 *
 * <h3>驱动模型</h3>
 * <ol>
 *   <li>监听 {@link ServerTickEvent.Post}，按 20 tick = 1 second 累计。</li>
 *   <li>每秒遍历所有在线 {@link ServerPlayer}，驱动其 {@link TimerState#tick()}。</li>
 *   <li>当某个玩家的定时器触发（{@code tick()} 返回 true），调用已注册的回调。</li>
 *   <li>定时器状态保存在 {@link NutritionCapability} 中，玩家退出冻结、登录恢复。</li>
 * </ol>
 *
 * <h3>回调注册</h3>
 * 其他模块通过 {@link #addCallback(Consumer)} 订阅 second event。回调在服务端线程执行。
 *
 * <h3>频率同步</h3>
 * 每个玩家首次定时器激活或 config 重载后，会自动从 {@link NutritionDataRegistry} 拉取当前频率。
 */
@EventBusSubscriber(modid = Nutrition.MOD_ID)
public class Ticker {

    /** 全局 tick 计数器（模 20，用于判断是否到达一秒边界）。 */
    private static int tickCounter;

    /** 已注册的 second event 回调列表，线程安全。 */
    private static final List<Consumer<ServerPlayer>> callbacks = new CopyOnWriteArrayList<>();

    /**
     * 注册一个 second event 回调。
     * 当任意玩家的定时器触发时，回调会被调用并传入该玩家。
     */
    public static void addCallback(Consumer<ServerPlayer> callback) {
        callbacks.add(callback);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        NutritionConfigJson config = NutritionDataRegistry.config();
        if (config == null) return;
        int frequency = config.frequency.toSeconds();

        // 使用 ServerTickEvent.Post 时，getServer() 可用
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            NutritionCapability cap = player.getData(AttributeTypeRegistry.NutritionCapability);
            if (cap == null) continue;

            TimerState timer = cap.getTimerState();
            if (timer == null) continue;

            // 同步频率（仅当发生变化时更新，TimerState.setFrequency 内部处理首次随机偏移）
            if (timer.getFrequency() != frequency) {
                timer.setFrequency(frequency);
            }

            if (timer.tick()) {
                // 定时器触发：通知所有回调
                for (Consumer<ServerPlayer> callback : callbacks) {
                    try {
                        callback.accept(player);
                    } catch (Exception e) {
                        // 单个回调异常不应打断其他回调
                        Log.e("Ticker", "Second event callback failed for "
                                + player.getGameProfile().getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
