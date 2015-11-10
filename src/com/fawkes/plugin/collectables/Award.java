package com.fawkes.plugin.collectables;

import java.util.ArrayList;

/* for handling queries */

public class Award {
	
	String name;
	String material;
	ArrayList<String> description;
	Category category;
	short id;
	long date;
	int level;

	public Award(String name, String type, ArrayList<String> description, String category, short id, long date, int level) {
		this.name = name;
		this.material = type;
		this.description = description;
		this.category = Category.getCategory(category);
		this.id = id;
		this.date = date;
		this.level = level;

	}
	
	public String getName() {
		return name;
	}
	
	public String getMaterial() {
		return material;
	}
	
	public ArrayList<String> getDescription() {
		return description;
	}
	
	public Category getCategory() {
		return category;
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

