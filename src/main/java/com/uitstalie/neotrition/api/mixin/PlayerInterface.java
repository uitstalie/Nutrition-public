package com.uitstalie.neotrition.api.mixin;

import net.minecraft.world.entity.player.Player;

/**
 * 这玩意是用来通过接口完成强制类型转换，实现将Player和foodData相绑定，避免编译上的问题
 */
public interface PlayerInterface {
    void setPlayer(Player player);
}
