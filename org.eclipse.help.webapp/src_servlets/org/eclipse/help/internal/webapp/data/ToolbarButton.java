package org.eclipse.help.internal.webapp.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */


/**
 * This class calls eclipse API's directly, so it should only be
 * instantiated in the workbench scenario, not in the infocenter.
 */
public class ToolbarButton{
	private String name;
	private String tooltip;
	private String image;
	private String action;
	private boolean isSeparator;
	
	public ToolbarButton() {
		isSeparator = true;
	}
	 
	public ToolbarButton(String name, String tooltip, String image, String action) {
		this.name = name;
		this.tooltip = tooltip;
		this.image = image;
		this.action = action;
	}
	
	public boolean isSeparator() {
		return isSeparator;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTooltip() {
		return tooltip;
	}
	
	/**
	 * Returns the enabled gray image
	 * @return String
	 */
	public String getImage() {
		int i = image.lastIndexOf('/');
		return image.substring(0, i) + "/e_"+ image.substring(i+1);
	}
	
	/**
	 * Returns the image when selected
	 * @return String
	 */
	public String getOnImage() {
		return image;
	}
	
	public String getAction() {
		return action;
	}	
}
