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
	/**
	 * Create an instance of Accelerator and initializes 
	 * it with its id, label, icon and menupath.
	 */ 
	public ActionDefinition(String id, String label, String icon, String menubarPath,
		/* Note:
		 * This implementation is using only the id.
		 */	
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
	/**
	 * Return this definition's id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * Return this definition's label.
	 */
	public String getLabel() {
		return label;
	}	
	/**
	 * Return this definition's icon name.
	 */
	public String getIcon() {
		return icon;
	}
	/**
	 * Return this definition's menu path.
	 */
	public String getMenubarPath() {
		return menubarPath;
	}
	/**
	 * Return this definition's tool bar path.
	 */
	public String getToolbarPath() {
		return toolbarPath;
	}	
	/**
	 * Return this definition's tooltip.
	 */
	public String getToolTip() {
		return toolTip;
	}
	/**
	 * Return this definition's help context id.
	 */
	public String getHelpContextId() {
		return helpContextId;
	}
	/**
	 * Return this definition's state.
	 */
	public String getState() {
		return state;	
	}
}
