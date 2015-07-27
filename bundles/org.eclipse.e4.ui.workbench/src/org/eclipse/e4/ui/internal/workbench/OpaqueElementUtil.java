/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;

/**
 * Utility class that encapsulates the representation of 'opaque' menu and tool bar elements in the
 * model.
 */
public class OpaqueElementUtil {

	/**
	 * A tag value that indicates a menu, menu item, menu separator or tool item is 'opaque'
	 */
	private static final String OPAQUE_TAG = "Opaque"; //$NON-NLS-1$

	/**
	 * A transient value key for the 'opaque item'
	 */
	private static final String OPAQUE_ITEM_KEY = "OpaqueItem"; //$NON-NLS-1$

	/**
	 * Remove the 'opaque item' from the UI Element
	 *
	 * @param uiElement
	 *            the UI element
	 * @return the removed 'opaque item' or <code>null</code>
	 */
	public static Object clearOpaqueItem(MUIElement uiElement) {
		return uiElement.getTransientData().remove(OPAQUE_ITEM_KEY);
	}

	/**
	 * Create an opaque menu
	 *
	 * @return a new opaque menu
	 */
	public static MMenu createOpaqueMenu() {
		final MMenu menu = MMenuFactory.INSTANCE.createMenu();
		menu.getTags().add(OPAQUE_TAG);
		return menu;
	}

	/**
	 * Create an opaque menu item
	 *
	 * @return a new opaque menu item.
	 */
	public static MMenuItem createOpaqueMenuItem() {
		final MMenuItem item = MMenuFactory.INSTANCE.createDirectMenuItem();
		item.getTags().add(OPAQUE_TAG);
		return item;
	}

	/**
	 * Create an opaque menu separator
	 *
	 * @return a new opaque menu separator
	 */
	public static MMenuSeparator createOpaqueMenuSeparator() {
		final MMenuSeparator separator = MMenuFactory.INSTANCE.createMenuSeparator();
		separator.getTags().add(OPAQUE_TAG);
		return separator;
	}

	/**
	 * Create an opaque tool item
	 *
	 * @return a new opaque tool item.
	 */
	public static MToolItem createOpaqueToolItem() {
		final MToolItem item = MMenuFactory.INSTANCE.createDirectToolItem();
		item.getTags().add(OPAQUE_TAG);
		return item;
	}

	/**
	 * Get the 'opaque item' associated with the UI Element.
	 *
	 * @param uiElement
	 *            a UI element
	 * @return the opaque item or <code>null</code>
	 */
	public static Object getOpaqueItem(MUIElement uiElement) {
		return uiElement.getTransientData().get(OPAQUE_ITEM_KEY);
	}

	/**
	 * Tests whether a menu element is an opaque menue
	 *
	 * @param item
	 * @return <code>true</code> if the element is an 'opaque' menu
	 */
	public static boolean isOpaqueMenu(MUIElement item) {
		return item != null && item instanceof MMenu && item.getTags().contains(OPAQUE_TAG);
	}

	/**
	 * Tests whether a menu element is an opaque menu item
	 *
	 * @param item
	 *            a menu item.
	 * @return <code>true</code> if the menu element is an 'opaque' menu item
	 */
	public static boolean isOpaqueMenuItem(MUIElement item) {
		return item != null && item instanceof MDirectMenuItem
				&& item.getTags().contains(OPAQUE_TAG);
	}

	/**
	 * Tests whether a menu element is an opaque menu separator
	 *
	 * @param item
	 *            a menu item.
	 * @return <code>true</code> if the menu element is an 'opaque' separator
	 */
	public static boolean isOpaqueMenuSeparator(MUIElement item) {
		return item != null && item instanceof MMenuSeparator
				&& item.getTags().contains(OPAQUE_TAG);
	}

	/**
	 * Tests whether a UI element is an 'opaque' tool item
	 *
	 * @param uiElement
	 *            a UI element.
	 * @return <code>true</code> if the tool item is 'opaque'
	 */
	public static boolean isOpaqueToolItem(MUIElement uiElement) {
		return uiElement != null && uiElement instanceof MDirectToolItem
				&& uiElement.getTags().contains(OPAQUE_TAG);
	}

	/**
	 * Set the 'opaque item' associated with the UI Element.
	 *
	 * @param uiElement
	 *            the UI Element.
	 * @param opaqueItem
	 *            the opaque item.
	 */
	public static void setOpaqueItem(MUIElement uiElement, Object opaqueItem) {
		uiElement.getTransientData().put(OPAQUE_ITEM_KEY, opaqueItem);
	}

}
