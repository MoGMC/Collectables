package com.fawkes.plugin.collectables;

public class QueryAward {

	String id;
	long date;
	int level;

	public QueryAward(String id, long date, int level) {
		this.id = id;
		this.date = date;
		this.level = level;

	}

	public String getId() {
		return id;

	}

	public long getDate() {
		return date;

	}

	public int getLevel() {
		return level;

	}

}
