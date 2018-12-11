package party.lemons.bettercompass;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.registry.Registry;

public class BetterCompass implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		Registry.register(Registry.ITEMS, "minecraft:compass", new BetterCompassItem());
	}
}
