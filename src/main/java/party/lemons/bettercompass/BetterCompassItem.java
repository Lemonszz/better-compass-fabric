package party.lemons.bettercompass;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class BetterCompassItem extends Item
{
	public BetterCompassItem()
	{
		super(new Item.Settings().itemGroup(ItemGroup.TOOLS).stackSize(1));

		this.addProperty(new Identifier("angle"),  new ItemPropertyGetter() {

			@Override
			public float call(ItemStack stack, World world, LivingEntity entityIn)
			{
				if(stack.isHeldInItemFrame())
					return 0.0F;

				if(entityIn == null)
					return 0.0F;
				else
				{
					CompoundTag tags  = stack.getTag();
					boolean flag = entityIn != null;
					Entity entity = flag ? entityIn : stack.getHoldingItemFrame();

					if(world == null)
						world = entity.world;

					double d0;
					int dim = 0;
					if(tags != null && tags.containsKey("dim"))
						dim = tags.getInt("dim");

					boolean isSameDim = entityIn.dimension.getRawId() == dim;
					boolean show = isSameDim && (world.dimension.hasVisibleSky());

					if(show)
					{
						double d1 = flag ? (double)entity.yaw : this.getFrameRotation((ItemFrameEntity)entity);
						d1 = MathHelper.floorMod(d1 / 360.0D, 1.0D);

						double d2;
						if(tags == null)
						{
							d2 = this.getSpawnToAngle(world, entity, stack) / (Math.PI * 2D);
						}
						else
						{
							d2=  this.getTagAngle(world, entity, stack) / (Math.PI * 2D);
						}
						d0 = 0.5D - (d1 - 0.25D - d2);
					}
					else
					{
						if(tags != null && tags.containsKey("rotation"))
							d0 = tags.getDouble("rotation");
						else
							d0 = Math.random();
					}
					return MathHelper.floorMod((float)d0, 1.0F);
				}
			}

			@Environment(EnvType.CLIENT)
			private double getFrameRotation(ItemFrameEntity frame)
			{
				return (double)MathHelper.wrapDegrees(180 + frame.facing.getHorizontal() * 90);
			}

			@Environment(EnvType.CLIENT)
			private double getSpawnToAngle(World world, Entity entity, ItemStack stack)
			{
				BlockPos var3 = world.method_8395();
				return Math.atan2((double)var3.getZ() - entity.z, (double)var3.getX() - entity.x);
			}

			@Environment(EnvType.CLIENT)
			private double getTagAngle(World world, Entity entity, ItemStack stack)
			{
				BlockPos var3 = TagHelper.deserializeBlockPos(stack.getTag().getCompound("pos"));
				return Math.atan2((double)var3.getZ() - entity.z, (double)var3.getX() - entity.x);
			}
		});
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx)
	{
		if(!ctx.getWorld().dimension.hasVisibleSky())
			return ActionResult.FAILURE;

		CompoundTag tags = ctx.getItemStack().getTag();
		if(tags == null)
			tags = new CompoundTag();

		tags.put("pos", TagHelper.serializeBlockPos(ctx.getPos()));
		tags.putInt("dim", ctx.getPlayer().dimension.getRawId());

		ctx.getItemStack().setTag(tags);
		ctx.getPlayer().addChatMessage(new TranslatableTextComponent("bettercompass.message.set"), true);

		return ActionResult.SUCCESS;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<TextComponent> tips, TooltipOptions options)
	{
		if(options.isAdvanced() && stack.getTag() != null)
		{
			TextComponent txt = new TranslatableTextComponent("bettercompass.message.info").setStyle(new Style().setColor(TextFormat.DARK_PURPLE));
			tips.add(txt);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int var4, boolean var5)
	{
		if(worldIn.isRemote)
		{
			if(stack.hasTag())
			{
				CompoundTag tags = stack.getTag();

				long lastUpdateTick = 0;
				double rotation = 0;
				double rota = 0;
				if(tags.containsKey("last_update"))
					lastUpdateTick = tags.getLong("last_update");
				if(tags.containsKey("rotation"))
					rotation = tags.getDouble("rotation");
				if(tags.containsKey("rota"))
					rota = tags.getDouble("rota");


				if (worldIn.getTime() != lastUpdateTick)
				{
					lastUpdateTick = worldIn.getTime();
					double d0 = Math.random() - rotation;
					d0 = MathHelper.floorMod(d0 + 0.5D, 1.0D) - 0.5D;
					rota += d0 * 0.1D;
					rota *= 0.8D;
					rotation = MathHelper.floorMod(rotation + rota, 1.0D);

					tags.putLong("last_update", lastUpdateTick);
					tags.putDouble("rotation", rotation);
					tags.putDouble("rota", rota);
				}
			}
		}
	}
}
