package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.MenuManager;

/**
 * A dynamic menu manager is a contribution manager which realizes itself and its items
 * dynamically in a menu control; either as a menu bar, a sub-menu, or a context menu.
 * <p>
 * This class may be instantiated; it may also be subclassed.
 * </p>
 */
public class DynamicMenuManager extends MenuManager {
	/**
	 * Creates a menu manager.  The text and id are <code>null</code>.
	 * Typically used for creating a context menu, where it doesn't need to be referred to by id.
	 */
	public DynamicMenuManager() {
		this(null, null);
	}
	/**
	 * Creates a menu manager with the given text. The id of the menu
	 * is <code>null</code>.
	 * Typically used for creating a sub-menu, where it doesn't need to be referred to by id.
	 *
	 * @param text the text for the menu, or <code>null</code> if none
	 */
	public DynamicMenuManager(String text) {
		this(text, null);
	}
	/**
	 * Creates a menu manager with the given text and id.
	 * Typically used for creating a sub-menu, where it needs to be referred to by id.
	 *
	 * @param text the text for the menu, or <code>null</code> if none
	 * @param id the menu id, or <code>null</code> if it is to have no id
	 */
	public DynamicMenuManager(String text, String id) {
		super(text, id);
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isDynamic() {
		return true;
	}

}