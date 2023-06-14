package ru.nern.pickupme.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo callbackInfo)
    {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if(!player.getWorld().isClient && player.hasPassengers() && player.isSneaking() && player.isOnGround())
            player.getFirstPassenger().stopRiding();
    }
}
