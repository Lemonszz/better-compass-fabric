package party.lemons.bettercompass;

import net.fabricmc.api.ModInitializer;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BetterCompass implements ModInitializer {
	public static final Item BOUND_COMPASS = new BoundCompassItem(new Item.Settings().maxCount(1));
	
	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("bettercompass","bound_compass"), BOUND_COMPASS);
	}
}
