package com.itzbreadloaf.character;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import com.itzbreadloaf.brain.LoafBrain;

/** One active (spawned) character: the Carpet fake player, its saved record, and the brain that drives it. */
public final class LoafCharacter {
	public final EntityPlayerMPFake player;
	public final CharacterRecord record;
	public LoafBrain brain;
	/** Set while it is being despawned or after it died, so nothing ticks it again. */
	boolean deactivating;
	/** Server tick of the last right-click, so one click handled twice (interact + interactAt) only counts once. */
	long lastInteractTick = -1;
	String lastInteractPlayer = "";

	LoafCharacter(EntityPlayerMPFake player, CharacterRecord record) {
		this.player = player;
		this.record = record;
	}

	public EntityPlayerActionPack actions() {
		return ((ServerPlayerInterface) this.player).getActionPack();
	}

	public CharacterKind kind() {
		return this.record.kind;
	}

	public boolean isGone() {
		return this.deactivating || this.player.isRemoved() || this.player.hasDisconnected();
	}
}
