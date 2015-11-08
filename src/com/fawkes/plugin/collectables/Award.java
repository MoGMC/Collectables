package com.fawkes.plugin.collectables;

/* for handling queries */

public class Award {

	short id;
	long date;
	int level;

	public Award(short id, long date, int level) {
		this.id = id;
		this.date = date;
		this.level = level;

	}

	public short getId() {
		return id;

	}

	public long getDate() {
		return date;

	}

	public int getLevel() {
		return level;

	}

}
