package party.lemons.bettercompass;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TagHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.List;

public class BoundCompassItem extends Item {
	public BoundCompassItem(Item.Settings settings) {
		super(settings);
		this.addPropertyGetter(new Identifier("angle"), new ItemPropertyGetter() {
			@Environment(EnvType.CLIENT)
			public float call(ItemStack stack, World world, LivingEntity livingEntity) {
				if (livingEntity == null && !stack.isInFrame()) {
					return 0.0F;
				} else {
					boolean bool = livingEntity != null;
					Entity entity = bool ? livingEntity : stack.getFrame();
					if(entity == null) {
						return 0.0F;
					}
					if (world == null) {
						world = entity.world;
					}
					
					boolean show = world.dimension.hasVisibleSky();
					double angle2;
					if(stack.hasTag()) {
						CompoundTag tags = stack.getTag();
						angle2 = this.getAngleFromTag(stack, entity);
						if(tags.containsKey("dim")) {
							show = show && tags.getInt("dim") == entity.dimension.getRawId();
						}
					} else {
						angle2 = this.getAngleToSpawn(world, entity);
						show = show && entity.dimension.equals(world.getDimension().getType());
					}
					
					double doub3;
					if (show) {
						double doub = bool ? (double)entity.yaw : this.getYaw((ItemFrameEntity)entity);
						doub = MathHelper.floorMod(doub / 360.0D, 1.0D);
						double doub2 = angle2 / 6.2831854820251465D;
						doub3 = 0.5D - (doub - 0.25D - doub2);
					} else {
						doub3 = Math.random();
					}
					
					if (bool) {
						doub3 = this.getAngle(stack, world, doub3);
					}
					
					return MathHelper.floorMod((float)doub3, 1.0F);
				}
			}
			
			@Environment(EnvType.CLIENT)
			private double getAngle(ItemStack stack, World world, double doub) {
				if (world.getTime() != BoundCompassItem.getLastTick(stack)) {
					BoundCompassItem.setLastTick(stack, world, world.getTime());
					double doub2 = doub - BoundCompassItem.getAngle(stack);
					doub2 = MathHelper.floorMod(doub2 + 0.5D, 1.0D) - 0.5D;
					BoundCompassItem.setStep(stack, world, BoundCompassItem.getStep(stack) + (doub2 * 0.1D));
					BoundCompassItem.setStep(stack, world, BoundCompassItem.getStep(stack) * 0.8D);
					BoundCompassItem.setAngle(stack, world, MathHelper.floorMod(BoundCompassItem.getAngle(stack) + BoundCompassItem.getStep(stack), 1.0D));
				}
				
				return BoundCompassItem.getAngle(stack);
			}
			
			@Environment(EnvType.CLIENT)
			private double getYaw(ItemFrameEntity itemFrameEntity) {
				return MathHelper.wrapDegrees(180 + itemFrameEntity.getHorizontalFacing().getHorizontal() * 90);
			}
			
			@Environment(EnvType.CLIENT)
			private double getAngleToSpawn(IWorld iWorld, Entity entity) {
				return this.getAngleToPos(iWorld.getSpawnPos(), entity);
			}
			
			@Environment(EnvType.CLIENT)
			private double getAngleToPos(BlockPos pos, Entity entity) {
				return Math.atan2((double)pos.getZ() - entity.z, (double)pos.getX() - entity.x);
			}
			
			@Environment(EnvType.CLIENT)
			private double getAngleFromTag(ItemStack stack, Entity entity) {
				return getAngleToPos(TagHelper.deserializeBlockPos(stack.getTag().getCompound("pos")), entity);
			}
		});
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		if (!ctx.getWorld().dimension.hasVisibleSky())
			return ActionResult.FAIL;
		
		CompoundTag tags = ctx.getStack().getTag();
		if (tags == null)
			tags = new CompoundTag();
		
		tags.put("pos", TagHelper.serializeBlockPos(ctx.getBlockPos()));
		tags.putInt("dim", ctx.getPlayer().dimension.getRawId());
		
		ctx.getStack().setTag(tags);
		ctx.getPlayer().addChatMessage(new TranslatableText("bettercompass.message.set"), true);
		
		return ActionResult.SUCCESS;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tips, TooltipContext ttc) {
		if (ttc.isAdvanced() && stack.hasTag()) {
			BlockPos pos = TagHelper.deserializeBlockPos(stack.getTag().getCompound("pos"));
			Text txt = new TranslatableText("bettercompass.message.info",pos.getX(),pos.getY(),pos.getZ(),stack.getTag().getInt("dim")).setStyle(new Style().setColor(Formatting.DARK_PURPLE));
			tips.add(txt);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static ItemStack setAngle(ItemStack stack, IWorld world, double angle) {
		CompoundTag tags = stack.getTag();
		if (tags == null) {
			tags = new CompoundTag();
			tags.put("pos", TagHelper.serializeBlockPos(world.getSpawnPos()));
			tags.putInt("dim", 0);
		}
		tags.putDouble("angle", angle);
		stack.setTag(tags);
		return stack;
	}
	
	@Environment(EnvType.CLIENT)
	public static ItemStack setStep(ItemStack stack, IWorld world, double step) {
		CompoundTag tags = stack.getTag();
		if (tags == null) {
			tags = new CompoundTag();
			tags.put("pos", TagHelper.serializeBlockPos(world.getSpawnPos()));
			tags.putInt("dim", 0);
		}
		tags.putDouble("step", step);
		stack.setTag(tags);
		return stack;
	}
	
	@Environment(EnvType.CLIENT)
	public static ItemStack setLastTick(ItemStack stack, IWorld world, long lastTick) {
		CompoundTag tags = stack.getTag();
		if (tags == null) {
			tags = new CompoundTag();
			tags.put("pos", TagHelper.serializeBlockPos(world.getSpawnPos()));
			tags.putInt("dim", 0);
		}
		tags.putLong("lasttick", lastTick);
		stack.setTag(tags);
		return stack;
	}
	
	@Environment(EnvType.CLIENT)
	public static double getAngle(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag.containsKey("angle")) {
				return tag.getDouble("angle");
			}
		}
		return 0.0;
	}
	
	@Environment(EnvType.CLIENT)
	public static double getStep(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag.containsKey("step")) {
				return tag.getDouble("step");
			}
		}
		return 0.0;
	}
	
	@Environment(EnvType.CLIENT)
	public static long getLastTick(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			if (tag.containsKey("lasttick")) {
				return tag.getLong("lasttick");
			}
		}
		return 0;
	}
}
