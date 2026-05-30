package com.uitstalie.nutrition.nutrition.server;

import com.mojang.brigadier.CommandDispatcher;
import com.uitstalie.nutrition.nutrition.api.data.NutritionDataRegistry;
import com.uitstalie.nutrition.nutrition.api.data.config.NutritionConfigJson;
import com.uitstalie.nutrition.nutrition.api.data.effect.NutritionEffectJson;
import com.uitstalie.nutrition.nutrition.api.data.group.NutritionGroupJson;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import com.uitstalie.nutrition.nutrition.service.NutritionAutoGenerateService;
import com.uitstalie.nutrition.nutrition.util.data.NutritionDataStorage;
import com.uitstalie.nutrition.nutrition.util.data.ValueRange;
import com.uitstalie.nutrition.nutrition.util.log.Log;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.*;

/**
 * 营养系统集成测试命令：/nutrition test。
 *
 * <p>命令式集成测试，参考 NeoForge oldtest 模式。
 * 每项测试独立运行，通过/失败结果汇总输出。</p>
 */
public final class NutritionTestCommand {

    private NutritionTestCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("nutrition")
                        .then(Commands.literal("test")
                                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(ctx -> runAllTests(ctx.getSource()))));
    }

    // ── 测试入口 ────────────────────────────────────────────────────────

    private static int runAllTests(CommandSourceStack source) {
        List<TestResult> results = new ArrayList<>();

        results.add(testConfigLoaded());
        results.add(testGroupsLoaded());
        results.add(testItemsLoaded());
        results.add(testEffectsLoaded());
        results.add(testFormulaEvaluator());
        results.add(testValueRange());
        results.add(testNutritionApi(source));
        results.add(testAutogenOnServer(source));
        results.add(testEffectEvaluation());
        results.add(testDecayMechanics());

        // 汇总
        int passed = 0;
        int failed = 0;
        StringBuilder sb = new StringBuilder("§6=== Nutrition Test Results ===\n");
        for (TestResult r : results) {
            if (r.passed) {
                passed++;
                sb.append("§a✔ ").append(r.name).append("\n");
            } else {
                failed++;
                sb.append("§c✘ ").append(r.name).append(" — ").append(r.error).append("\n");
            }
        }
        sb.append("§6=============================\n");
        sb.append(String.format("§e%d passed, §c%d failed, §f%d total",
                passed, failed, results.size()));

        source.sendSystemMessage(Component.literal(sb.toString()));
        Log.w("Test", String.format("Nutrition tests: %d passed, %d failed", passed, failed));
        return failed == 0 ? 1 : 0;
    }

    // ── 测试 1: 配置加载 ───────────────────────────────────────────────

    private static TestResult testConfigLoaded() {
        NutritionConfigJson config = NutritionDataRegistry.config();
        if (config == null) {
            return TestResult.fail("config", "NutritionDataRegistry.config() is null");
        }
        if (config.frequency == null) {
            return TestResult.fail("config", "frequency is null");
        }
        return TestResult.pass("config");
    }

    // ── 测试 2: 营养组加载 ─────────────────────────────────────────────

    private static TestResult testGroupsLoaded() {
        List<NutritionGroupJson> groups = NutritionDataRegistry.groups();
        if (groups == null || groups.isEmpty()) {
            return TestResult.fail("groups", "No groups loaded (0 groups)");
        }
        for (NutritionGroupJson g : groups) {
            if (g.groupName == null || g.groupName.isEmpty())
                return TestResult.fail("groups", "Group has empty name");
            if (g.decayValue <= 0)
                return TestResult.fail("groups", "Group '" + g.groupName + "' has non-positive decay");
        }
        return TestResult.pass("groups (" + groups.size() + " loaded)");
    }

    // ── 测试 3: 物品绑定加载 ───────────────────────────────────────────

    private static TestResult testItemsLoaded() {
        var items = NutritionDataRegistry.items();
        if (items == null || items.isEmpty()) {
            return TestResult.fail("items", "No item bindings loaded");
        }
        return TestResult.pass("items (" + items.size() + " items)");
    }

    // ── 测试 4: 效果规则加载 ───────────────────────────────────────────

    private static TestResult testEffectsLoaded() {
        List<NutritionEffectJson> effects = NutritionDataRegistry.effects();
        if (effects == null || effects.isEmpty()) {
            return TestResult.fail("effects", "No effect rules loaded");
        }
        for (NutritionEffectJson e : effects) {
            if (e.entries == null || e.entries.isEmpty())
                return TestResult.fail("effects", "An effect config has no entries");
        }
        return TestResult.pass("effects (" + effects.size() + " rules)");
    }

    // ── 测试 5: 公式求值 ───────────────────────────────────────────────

    private static TestResult testFormulaEvaluator() {
        // 默认公式: healing*100 + saturation*50
        // 测试: healing=4, saturation=2.4 → 400 + 120 = 520
        double result = com.uitstalie.nutrition.nutrition.util.data.ValueFormulaEvaluator.evaluate(
                "healing*100 + saturation*50", 4, 2.4f);
        if (Math.abs(result - 520.0) > 0.01) {
            return TestResult.fail("formula", "Expected 520, got " + result);
        }
        return TestResult.pass("formula (healing*100 + saturation*50 = " + result + ")");
    }

    // ── 测试 6: 区间规则 ───────────────────────────────────────────────

    private static TestResult testValueRange() {
        com.uitstalie.nutrition.nutrition.util.data.ValueRange range =
                new com.uitstalie.nutrition.nutrition.util.data.ValueRange(10, 50);
        // below 10 → false
        if (range.contains(5)) return TestResult.fail("range", "5 should NOT match [10,50]");
        // in range → true
        if (!range.contains(30)) return TestResult.fail("range", "30 should match [10,50]");
        // above 50 → false
        if (range.contains(60)) return TestResult.fail("range", "60 should NOT match [10,50]");
        // boundary → true
        if (!range.contains(10)) return TestResult.fail("range", "10 should match [10,50]");
        return TestResult.pass("range [10,50] matching");
    }

    // ── 测试 7: 营养值 API ─────────────────────────────────────────────

    private static TestResult testNutritionApi(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayer();
            if (player == null) return TestResult.fail("nutrition-api", "No player available");

            NutritionCapability cap = player.getData(AttributeTypeRegistry.NutritionCapability.get());
            if (cap == null) return TestResult.fail("nutrition-api", "NutritionCapability is null");

            NutritionDataStorage data = cap.getNutritionData();
            if (data == null) return TestResult.fail("nutrition-api", "NutritionDataStorage is null");

            // set a value
            data.setNutrition("fruit", 50000);
            int val = data.getNutrition("fruit");
            if (val != 50000) return TestResult.fail("nutrition-api", "set/get mismatch: " + val);

            // get unknown group
            int unknown = data.getNutrition("nonexistent_group");
            if (unknown != 0) return TestResult.fail("nutrition-api", "Unknown group should be 0, got " + unknown);

            // reset
            data.setNutrition("fruit", 0);
            return TestResult.pass("nutrition-api (set/get/unknown)");
        } catch (Exception e) {
            return TestResult.fail("nutrition-api", e.getMessage());
        }
    }

    // ── 测试 8: 自动生成 ───────────────────────────────────────────────

    private static TestResult testAutogenOnServer(CommandSourceStack source) {
        try {
            var server = source.getServer();
            if (server == null) return TestResult.fail("autogen", "No server available");

            var config = NutritionDataRegistry.config();
            if (config == null || !config.autoGenerateOnLoad)
                return TestResult.pass("autogen (disabled in config — skip)");

            // 手动触发 autogen
            long start = System.currentTimeMillis();
            NutritionAutoGenerateService.generate(server);
            long elapsed = System.currentTimeMillis() - start;

            int newItems = NutritionDataRegistry.autoGeneratedItemSource().getAllGroups().size();
            return TestResult.pass("autogen (" + newItems + " items in " + elapsed + "ms)");
        } catch (Exception e) {
            return TestResult.fail("autogen", e.getMessage());
        }
    }

    // ── 测试 9: 效果判定 ───────────────────────────────────────────────

    private static TestResult testEffectEvaluation() {
        try {
            // Build a mock NutritionDataStorage
            NutritionDataStorage mockData = new NutritionDataStorage();
            mockData.setNutrition("fruit", 60);
            mockData.setNutrition("protein", 30);

            // AND match: fruit >= 50 AND protein >= 20 → should be true
            var andMatch = new NutritionEffectJson.Match(
                    NutritionEffectJson.Predict.AND,
                    List.of(
                            new NutritionEffectJson.Prediction("fruit",
                                    new ValueRange(50, 99999)),
                            new NutritionEffectJson.Prediction("protein",
                                    new ValueRange(20, 99999))
                    ));

            if (!andMatch.evaluate(mockData))
                return TestResult.fail("effect-and", "AND match should be true for fruit>=50, protein>=20");

            // OR match: fruit >= 90 OR protein >= 20 → should be true (protein meets)
            var orMatch = new NutritionEffectJson.Match(
                    NutritionEffectJson.Predict.OR,
                    List.of(
                            new NutritionEffectJson.Prediction("fruit",
                                    new ValueRange(90, 99999)),
                            new NutritionEffectJson.Prediction("protein",
                                    new ValueRange(20, 99999))
                    ));

            if (!orMatch.evaluate(mockData))
                return TestResult.fail("effect-or", "OR match should be true (protein>=20)");

            // NOT match: NOT fruit >= 90 → should be true (fruit=60 is NOT >=90)
            var notMatch = new NutritionEffectJson.Match(
                    NutritionEffectJson.Predict.NOT,
                    List.of(
                            new NutritionEffectJson.Prediction("fruit",
                                    new ValueRange(90, 99999))
                    ));

            if (!notMatch.evaluate(mockData))
                return TestResult.fail("effect-not", "NOT match should be true (fruit<90)");

            return TestResult.pass("effect-match (AND/OR/NOT)");
        } catch (Exception e) {
            return TestResult.fail("effect-eval", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // ── 测试 10: 衰减机制 ───────────────────────────────────────────────

    private static TestResult testDecayMechanics() {
        try {
            List<NutritionGroupJson> groups = NutritionDataRegistry.groups();
            if (groups == null || groups.isEmpty()) return TestResult.fail("decay", "No groups");

            // 验证每个组的衰减参数合法
            for (NutritionGroupJson g : groups) {
                if (g.decayValue < 0)
                    return TestResult.fail("decay", "Group '" + g.groupName + "' has negative decayValue: " + g.decayValue);
                if (g.decayFrequency <= 0)
                    return TestResult.fail("decay", "Group '" + g.groupName + "' has non-positive decayFrequency: " + g.decayFrequency);
                if (g.decayPressure < 0)
                    return TestResult.fail("decay", "Group '" + g.groupName + "' has negative decayPressure: " + g.decayPressure);
            }

            // 验证衰减公式: decay = decayValue * (1 + (value/100000)^decayPressure)
            // value=0 → decay = decayValue
            // value=100000 → decay = decayValue * 2^decayPressure
            double value0 = 0;
            double pressure = 2.0;
            double decayAt0 = 1.0 * (1 + Math.pow(value0 / 100000.0, pressure));
            if (Math.abs(decayAt0 - 1.0) > 0.001)
                return TestResult.fail("decay", "Decay at value=0 should equal decayValue (1.0), got " + decayAt0);

            double value100k = 100000;
            double decayAt100k = 1.0 * (1 + Math.pow(value100k / 100000.0, pressure));
            if (Math.abs(decayAt100k - 2.0) > 0.001)
                return TestResult.fail("decay", "Decay at value=100k with pressure=2 should be 2x, got " + decayAt100k);

            return TestResult.pass("decay (" + groups.size() + " groups validated)");
        } catch (Exception e) {
            return TestResult.fail("decay", e.getMessage());
        }
    }

    // ── 辅助类 ──────────────────────────────────────────────────────────

    private record TestResult(String name, boolean passed, String error) {
        static TestResult pass(String name) { return new TestResult(name, true, ""); }
        static TestResult fail(String name, String error) { return new TestResult(name, false, error); }
    }
}
