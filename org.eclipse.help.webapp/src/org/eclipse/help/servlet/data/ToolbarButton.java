package org.eclipse.help.servlet.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;

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
	
	public String getImage() {
		return image;
	}
	
	public String getAction() {
		return action;
	}	
}
