package net.levelz.mixin.entity;

import java.util.ArrayList;
import java.util.Iterator;

import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.data.LevelLists;
import net.levelz.init.ConfigInit;
import net.levelz.stats.PlayerStatsManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;beginTradeWith(Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ArrayList<Object> levelList = LevelLists.villagerList;
        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
            this.sayNo();
            player.sendMessage(new TranslatableText("item.levelz." + levelList.get(0) + ".tooltip", levelList.get(1)), true);
            info.setReturnValue(ActionResult.FAIL);
        }
    }

    @ModifyVariable(method = "afterUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), ordinal = 0)
    private int afterUsing(int original) {
        if (this.getCurrentCustomer() != null) {
            return original + (int) (((PlayerStatsManagerAccess) this.getCurrentCustomer()).getPlayerStatsManager(this.getCurrentCustomer()).getLevel("trade")
                    * (float) LevelLists.wanderingTraderList.get(3));
        } else
            return original;
    }

    @Inject(method = "prepareOffersFor", at = @At(value = "TAIL"))
    private void prepareOffersForMixin(PlayerEntity player, CallbackInfo info) {
        if (!player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            Iterator<TradeOffer> var5 = this.getOffers().iterator();
            while (var5.hasNext()) {
                TradeOffer tradeOffer2 = (TradeOffer) var5.next();
                int k = (int) Math.floor((0.3D + 0.0625D * (double) ((PlayerStatsManagerAccess) player).getPlayerStatsManager(player).getLevel("trade") * ConfigInit.CONFIG.tradeBonus)
                        * (double) tradeOffer2.getOriginalFirstBuyItem().getCount());
                tradeOffer2.increaseSpecialPrice(-Math.max(k, 1));
            }
        }
    }

    @Shadow
    private void sayNo() {
    }
}