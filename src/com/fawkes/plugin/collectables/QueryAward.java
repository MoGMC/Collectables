package com.fawkes.plugin.collectables;

public class QueryAward {

	private String id, meta;
	private long date;
	private int level;

	public QueryAward(String id, long date, int level) {
		this.id = id;
		this.date = date;
		this.level = level;

	}

	public void addMeta(String meta) {
		this.meta = meta;

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

	// TODO: more use for meta.
	public String getMeta() {
		return meta;

	}

	public boolean hasMeta() {
		return meta != null;

	}

}
