package com.fawkes.plugin.collectables;

public enum Category {
	
	MAIN("Main Menu"),
	HOLIDAY("Holiday"),
	MONTHLY("Monhtly"),
	MISC("Miscellaneous"),
	ACHIEVEMENT("Achievement"),
	EXCLUSIVE("Exclusive");
	
	String string;
	
	Category(String string) {
		this.string = string;
	}
	
	public String getString() {
		return string;
	}
	
	public static Category getCategory(String category) {
		
		switch (category.toLowerCase()) {
		
		case "main": return MAIN;
		case "holiday": return HOLIDAY;
		case "monthly": return MONTHLY;
		case "misc": return MISC;
		case "achievement": return ACHIEVEMENT;
		case "exclusive": return EXCLUSIVE;
		
		default: return null;		
		}
				
	}

}
