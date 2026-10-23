package com.itzbreadloaf.brain;

import com.itzbreadloaf.mixin.FishingHookAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Real fishing: stand by water near home, cast the rod (right-click), wait until a fish bites (the bobber dips),
 * reel in (right-click). The catch flies to the fisher and gets picked up. Used by Fisher Madison and the
 * fisherman ItzBreadLoaf on the fishing dock.
 */
public final class Fishing {
	private final LoafBrain brain;
	private @Nullable BlockPos water;
	private int ticks;
	private int cooldown;
	private int pickupTicks;

	public Fishing(LoafBrain brain) {
		this.brain = brain;
	}

	/** True for a few seconds after reeling in, so the catch can be picked up. */
	public boolean isCollecting() {
		return this.pickupTicks > 0;
	}

	/** Reel in (if the line is out) and stop. */
	public void stop() {
		if (this.brain.player.fishing != null) {
			this.brain.controls.useOnce();
		}
		this.water = null;
	}

	/** One tick of fishing around {@code center} with the rod in hotbar {@code rodSlot}. False when not fishing. */
	public boolean tick(BlockPos center, int rodSlot) {
		if (this.pickupTicks > 0) {
			this.pickupTicks--;
		}
		if (this.cooldown > 0) {
			this.cooldown--;
		}
		ServerLevel level = this.brain.player.level();
		if (this.water == null) {
			if (this.cooldown > 0 || this.brain.ticks % 20 != 0 || level.isDarkOutside()) {
				return false;
			}
			BlockPos found = this.brain.findBlock(center, 10, 3, state -> state.getFluidState().is(FluidTags.WATER)
					&& state.getFluidState().isSource());
			if (found == null || !level.getBlockState(found.above()).isAir()) {
				this.cooldown = 200;
				return false;
			}
			this.water = found;
			this.ticks = 0;
		}
		Vec3 waterTop = Vec3.atCenterOf(this.water).add(0.0, 0.5, 0.0);
		if (this.brain.player.position().distanceTo(waterTop) > 5.0) {
			this.brain.nav.moveTo(waterTop, 3.5, Navigator.Speed.WALK);
			return true;
		}
		this.brain.nav.stop();
		int rod = Inv.ready(this.brain.player, stack -> stack.is(Items.FISHING_ROD), rodSlot);
		if (rod < 0) {
			this.water = null;
			this.cooldown = 1200;
			return false;
		}
		this.brain.controls.selectSlot(rod);
		this.brain.controls.lookAt(waterTop);
		this.ticks++;
		if (this.brain.player.fishing == null) {
			if (this.ticks % 20 == 1) {
				this.brain.controls.lookInstantly(Controls.yawTo(this.brain.player.position(), waterTop), 10.0F);
				this.brain.controls.useOnce();
			}
			if (this.ticks > 1200) {
				this.cooldown = 400;
				this.water = null;
			}
			return true;
		}
		boolean biting = ((FishingHookAccessor) this.brain.player.fishing).loaf$isBiting();
		if (biting || this.ticks > 900) {
			this.brain.controls.useOnce();
			this.pickupTicks = 60;
			this.ticks = 0;
			if (this.brain.random.nextInt(4) == 0) {
				this.cooldown = 200;
				this.water = null;
			}
		}
		return true;
	}
}
