package com.fawkes.plugin.collectables;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AwardGiveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private UUID uuid;
	private QueryAward a;

	public AwardGiveEvent(UUID uuid, QueryAward a) {
		this.uuid = uuid;
		this.a = a;

	}

	public UUID getUUID() {
		return uuid;

	}

	public QueryAward getAward() {
		return a;

	}

	@Override
	public HandlerList getHandlers() {
		return handlers;

	}

	public static HandlerList getHandlerList() {
		return handlers;

	}

}
