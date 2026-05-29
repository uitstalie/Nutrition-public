package com.uitstalie.nutrition.nutrition.util.log;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Map;

public class ChatLogBuilder {

    // 基于多个<String, Style>组合成Component
    public static MutableComponent buildFromTextStyles(Map<String, Style> textStyles) {
        MutableComponent result = Component.literal("");
        for (Map.Entry<String, Style> entry : textStyles.entrySet()) {
            result.append(Component.literal(entry.getKey()).withStyle(entry.getValue()));
        }
        return result;
    }
}
