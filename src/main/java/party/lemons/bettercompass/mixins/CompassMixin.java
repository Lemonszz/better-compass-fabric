package party.lemons.bettercompass.mixins;

import org.spongepowered.asm.mixin.Mixin;
import party.lemons.bettercompass.BetterCompass;

import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TagHelper;

@Mixin(CompassItem.class)
public abstract class CompassMixin extends Item {
	public CompassMixin(Settings item$Settings_1) {
		super(item$Settings_1);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		if (!ctx.getWorld().dimension.hasVisibleSky())
			return ActionResult.FAIL;
		
		ItemStack compassStack = ctx.getStack();
		compassStack.decrement(1);
		ItemStack betterCompass = new ItemStack(BetterCompass.BOUND_COMPASS);
		
		CompoundTag tags = betterCompass.getTag();
		if (tags == null)
			tags = new CompoundTag();
		
		tags.put("pos", TagHelper.serializeBlockPos(ctx.getBlockPos()));
		tags.putInt("dim", ctx.getPlayer().dimension.getRawId());
		
		betterCompass.setTag(tags);
		ctx.getPlayer().addChatMessage(new TranslatableText("bettercompass.message.set"), true);
		
		ctx.getPlayer().giveItemStack(betterCompass);
		
		return ActionResult.SUCCESS;
	}
}
