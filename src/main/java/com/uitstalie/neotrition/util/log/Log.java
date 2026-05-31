package com.uitstalie.neotrition.util.log;


import com.uitstalie.neotrition.Neotrition;
import com.uitstalie.neotrition.api.data.NutritionDataRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Map.entry;

public class Log {
    private static final Logger LOG = LoggerFactory.getLogger(Neotrition.MOD_ID);
    private static final String PREFIX = "Nutrition_";
    public static void d(String tag,String message){

        LOG.debug("{}:{}",PREFIX+tag,message);
        if(NutritionDataRegistry.isChatLogEnabled()){
            ChatLog.send(ChatLogBuilder.buildFromTextStyles(Map.ofEntries(
                    entry(tag+": ", Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true)),
                    entry(message, Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false))
            )));
        }
    }
    public static void w(String tag, String message) {
        LOG.warn("{}:{}", PREFIX + tag, message);
        if (NutritionDataRegistry.isChatLogEnabled()) {
            ChatLog.send(ChatLogBuilder.buildFromTextStyles(Map.ofEntries(
                    entry(tag + ": ", Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true)),
                    entry(message, Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(false))
            )));
        }
    }

    public static void e(String tag, String message) {
        LOG.error("{}:{}", PREFIX + tag, message);
        if (NutritionDataRegistry.isChatLogEnabled()) {
            ChatLog.send(ChatLogBuilder.buildFromTextStyles(Map.ofEntries(
                    entry(tag + ": ", Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true)),
                    entry(message, Style.EMPTY.withColor(ChatFormatting.RED).withBold(false))
            )));
        }
    }
}

