/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

import java.util.ArrayList;

public class Item extends Intro implements IActionItem, IPerformWhenItem, ISubItemItem {
	private String title;
	private boolean skip;
	private ArrayList itemExtensions;
	
	private Action action;
	private PerformWhen performWhen;
	
	private ArrayList subItems;
	private ArrayList conditionalSubItems;
	private ArrayList repeatedSubItems; 
	

	/**
	 * Constructor for Item.
	 */
	public Item() {
		super();
	}
	
	public Item(String title, String description, String href, String contextId, boolean skip) {
		super(description, href, contextId);
		this.title = title;
		this.skip = skip;
	}
	
	/**
	 * Returns the title.
	 * @return String
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Returns whether this item is dynamic. An item is dynamic if it
	 * has performWhen condition, conditionalSubItems or repeatedSubItems.
	 *
	 * @return <code>true</code> if this item is dynamic, and
	 *  <code>false</code> for normal items
	 */
	public boolean isDynamic() {
		if( performWhen != null ||
			(conditionalSubItems != null && conditionalSubItems.size() > 0) ||
			(repeatedSubItems != null && repeatedSubItems.size() > 0)) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the skip.
	 * @return boolean
	 */
	public boolean isSkip() {
		return this.skip;
	}

	/**
	 * @param skip The skip to set.
	 */
	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	/**
	 * Sets the title.
	 * @param title The title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the item extensions for this item.
	 * @param exts the extensions to set
	 */
	public void setItemExtensions(ArrayList exts){
		this.itemExtensions = exts;	
	}
	
	/**
	 * Returns the item extensions, if any, for this item,.
	 * @return list of the extensions or <code>null</code>
	 */
	public ArrayList getItemExtensions(){
		return itemExtensions;
	}

	/**
	 * @return Returns the action.
	 */
	public Action getAction() {
		return action;
	}
	
	/**
	 * @param action The action to set.
	 */
	public void setAction(Action action) {
		this.action = action;
	}
	
	/**
	 * @return Returns the performWhen.
	 */
	public PerformWhen getPerformWhen() {
		return performWhen;
	}
	
	/**
	 * @param performWhen The performWhen to set.
	 */
	public void setPerformWhen(PerformWhen performWhen) {
		this.performWhen = performWhen;
	}
	
	/**
	 * @param conditionalSubItem the conditionalSubItem to add.
	 */
	public void addConditionalSubItem(ConditionalSubItem conditionalSubItem) {
		if(conditionalSubItems == null) {
			conditionalSubItems = new ArrayList();
		}
		conditionalSubItems.add(conditionalSubItem);
	}
	
	/**
	 * @return Returns the conditionalSubItems.
	 */
	public ArrayList getConditionalSubItems() {
		return conditionalSubItems;
	}
	
	/**
	 * @param repeatedSubItem the RepeatedSubItem to add.
	 */
	public void addRepeatedSubItem(RepeatedSubItem repeatedSubItem) {
		if(repeatedSubItems == null) {
			repeatedSubItems = new ArrayList();
		}
		repeatedSubItems.add(repeatedSubItem);
	}

	/**
	 * @return Returns the repeatedSubItems.
	 */
	public ArrayList getRepeatedSubItems() {
		return repeatedSubItems;
	}
	
	/**
	 * @param subItem the SubItem to add.
	 */
	public void addSubItem(SubItem subItem) {
		if(subItems == null) {
			subItems = new ArrayList();
		}
		subItems.add(subItem);
	}

	/**
	 * @return Returns the subItems.
	 */
	public ArrayList getSubItems() {
		return subItems;
	}
}
