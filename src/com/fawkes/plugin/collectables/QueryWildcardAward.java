package com.fawkes.plugin.collectables;

public class QueryWildcardAward extends QueryAward {

	String wildcard;

	public QueryWildcardAward(String id, long date, int level, String wildcard) {
		super(id, date, level);
		this.wildcard = wildcard;

	}

	public String getWildcard() {
		return wildcard;

	}

}
