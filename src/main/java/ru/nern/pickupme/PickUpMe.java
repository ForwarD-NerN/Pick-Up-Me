package ru.nern.pickupme;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PickUpMe implements ModInitializer
{
	public static final Logger LOGGER = LoggerFactory.getLogger("pickupme");

	@Override
	public void onInitialize()
	{
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
		{
			if(handler.player.hasVehicle() && handler.player.getVehicle() instanceof PlayerEntity)
				handler.player.stopRiding();
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
		{
			if(!world.isClient && hand == Hand.MAIN_HAND && entity instanceof PlayerEntity && player.getStackInHand(hand).isEmpty())
			{
				if(player.hasPassengers() && player.getFirstPassenger() == entity) return ActionResult.PASS;

				Entity lastPassenger = player;

				while (lastPassenger.getFirstPassenger() != null && lastPassenger.getFirstPassenger() != entity)
					lastPassenger = lastPassenger.getFirstPassenger();


				entity.startRiding(lastPassenger);

				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});
	}
}
