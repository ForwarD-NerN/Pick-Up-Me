package ru.nern.pickupme.mixin;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow public abstract World getWorld();

	@Inject(method = "removePassenger", at = @At("TAIL"))
	private void onRemovePassenger(Entity passenger, CallbackInfo callbackInfo)
	{
		Entity entity = (Entity) (Object) this;

		if(!entity.getWorld().isClient && entity instanceof PlayerEntity)
			((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
	private void onStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir)
	{
		if(!entity.getWorld().isClient && entity instanceof PlayerEntity)
			((ServerPlayerEntity)entity).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
	}

	@Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At("HEAD"), cancellable = true)
	private void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
		ci.cancel();
		Entity entity = (Entity) (Object) this;
		if (!entity.hasPassenger(passenger)) return;

		double d = entity.getY() + entity.getMountedHeightOffset() + passenger.getHeightOffset();

		if(getWorld().isClient)
			d = getOffset(passenger, entity);


		positionUpdater.accept(passenger, entity.getX(), d, entity.getZ());
	}

	@Environment(EnvType.CLIENT)
	private double getOffset(Entity passenger, Entity vehicle)
	{
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.options.getPerspective().isFirstPerson() && passenger instanceof PlayerEntity && passenger.getVehicle() == mc.player)
			return vehicle.getY() + vehicle.getMountedHeightOffset();

		return vehicle.getY() + vehicle.getMountedHeightOffset() + passenger.getHeightOffset();
	}


}
