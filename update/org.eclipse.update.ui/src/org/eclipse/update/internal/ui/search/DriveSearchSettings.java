/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.update.internal.ui.search;

/**
 * @version 	1.0
 * @author
 */
public class DriveSearchSettings {
public static final int DISABLED = 0;
public static final int DEEP = - 1;
public static final int SHALLOW = -2;
public static final int CUSTOM = -3;

	private String name;
	private boolean checked = false;
	private int searchDepth = DEEP;
	private int numberOfLevels = Integer.MAX_VALUE;
	
	public DriveSearchSettings() {
	}
	
	public DriveSearchSettings(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the checked.
	 * @return Returns a boolean
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * Sets the checked.
	 * @param checked The checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * Gets the searchDepth.
	 * @return Returns a int
	 */
	public int getSearchDepth() {
		return searchDepth;
	}

	/**
	 * Sets the searchDepth.
	 * @param searchDepth The searchDepth to set
	 */
	public void setSearchDepth(int searchDepth) {
		this.searchDepth = searchDepth;
	}

	/**
	 * Gets the numberOfLevels.
	 * @return Returns a int
	 */
	public int getNumberOfLevels() {
		return numberOfLevels;
	}

	/**
	 * Sets the numberOfLevels.
	 * @param numberOfLevels The numberOfLevels to set
	 */
	public void setNumberOfLevels(int numberOfLevels) {
		this.numberOfLevels = numberOfLevels;
	}

	void load(String value) {
		int loc = value.indexOf(',');
		name = value.substring(0, loc);
		checked = true;
		String depth = value.substring(loc+1);
		int idepth = DEEP;

		try {
			idepth = Integer.parseInt(depth);
			if (idepth>0) {
				searchDepth = CUSTOM;
				numberOfLevels = idepth;
			}
			else if (idepth<0) {
				searchDepth = idepth;
			} 
			else checked = false;
		}
		catch (NumberFormatException e) {
		}
	}
	
	String encode() {
		int idepth=DISABLED;
		
		if (checked)
		   idepth = searchDepth == CUSTOM ? numberOfLevels:searchDepth;
		String result = name + ","+idepth;
		return result;
	}
	
	public String toString() {
		return getName();
	}
}
