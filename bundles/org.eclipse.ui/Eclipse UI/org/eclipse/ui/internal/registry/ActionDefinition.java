package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Captures the attributes of an action definition.
 */
public class ActionDefinition {
	private String id;
	private String label;
	private String icon;
	private String menubarPath;
	private String toolbarPath;
	private String toolTip;
	private String helpContextId;
	private String state;

	public ActionDefinition(String id, String label, String icon, String menubarPath,
		String toolbarPath, String tooltip, String helpContextId, String state) {
		this.id = id;
		this.label = label;
		this.icon = icon;
		this.menubarPath = menubarPath;
		this.toolbarPath = toolbarPath;
		this.toolTip = tooltip;
		this.helpContextId = helpContextId;
		this.state = state;		
	}

	public String getId() {
		return id;
	}
	public String getLabel() {
		return label;
	}
	public String getIcon() {
		return icon;
	}
	public String getMenubarPath() {
		return menubarPath;
	}
	public String getToolbarPath() {
		return toolbarPath;
	}	
	public String getToolTip() {
		return toolTip;
	}
	public String getHelpContextId() {
		return helpContextId;
	}
	public String getState() {
		return state;	
	}
}
