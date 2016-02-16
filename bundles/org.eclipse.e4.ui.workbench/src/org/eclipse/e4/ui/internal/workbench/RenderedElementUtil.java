/*******************************************************************************
 * Copyright (c) 2014, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Axel Richard <axel.richard@obeo.fr> - Bug 392457
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;

/**
 * Utility class that encapsulates the representation of 'rendered' menu and tool bar elements in
 * the model.
 */
public class RenderedElementUtil {

	/**
	 * A tag value that indicates a menu, menu item or tool item is 'rendered'
	 */
	private static final String RENDERED_TAG = "Rendered"; //$NON-NLS-1$

	/**
	 * A transient data key for a rendered element's 'contribution manager'.
	 */
	private static final String CONTRIBUTION_MANAGER_KEY = "ContributionManager"; //$NON-NLS-1$

	/**
	 * Create a 'rendered' menu instance
	 *
	 * @return the new instance
	 */
	public static MMenu createRenderedMenu() {
		final MMenu object = MMenuFactory.INSTANCE.createMenu();
		object.getTags().add(RENDERED_TAG);
		return object;

	}

	/**
	 * Create a 'rendered' menu item instance
	 *
	 * @return the new instance
	 */
	public static MMenuItem createRenderedMenuItem() {
		final MDirectMenuItem object = MMenuFactory.INSTANCE.createDirectMenuItem();
		object.getTags().add(RENDERED_TAG);
		return object;
	}

	/**
	 * Create a 'rendered' tool bar instance
	 *
	 * @return the new instance
	 */
	public static MToolBar createRenderedToolBar() {
		final MToolBar object = MMenuFactory.INSTANCE.createToolBar();
		object.getTags().add(RENDERED_TAG);
		return object;
	}

	/**
	 * Create a 'rendered' tool bar element instance
	 *
	 * @return the new instance
	 *
	 * @since 1.4
	 */
	public static MToolControl createRenderedToolBarElement() {
		final MToolControl object = MMenuFactory.INSTANCE.createToolControl();
		object.getTags().add(RENDERED_TAG);
		return object;
	}

	/**
	 * Return the contribution manager value for the 'rendered' element.
	 *
	 * @param element
	 *            a rendered element.
	 * @return the contribution manager or <code>null</code>
	 */
	public static Object getContributionManager(MUIElement element) {
		return element.getTransientData().get(CONTRIBUTION_MANAGER_KEY);
	}

	/**
	 * Test whether the UI element is an 'rendered' menu.
	 *
	 * @param element
	 *            the UI element
	 * @return <code>true</code> if the element is a 'rendered' menu
	 */
	public static boolean isRenderedMenu(MUIElement element) {
		return element instanceof MMenu && element.getTags().contains(RENDERED_TAG);
	}

	/**
	 * Test whether the UI element is a 'rendered' menu item.
	 *
	 * @param element
	 *            a UI element
	 * @return <code>true</code> if the element is a 'rendered' menu item
	 */
	public static boolean isRenderedMenuItem(MUIElement element) {
		return element instanceof MDirectMenuItem && element.getTags().contains(RENDERED_TAG);
	}

	/**
	 * Set the contribution manager value for a 'rendered' element.
	 *
	 * @param element
	 *            a rendered element
	 * @param contributionManager
	 *            a contribution manager
	 */
	public static void setContributionManager(MUIElement element, Object contributionManager) {
		element.getTransientData().put(CONTRIBUTION_MANAGER_KEY, contributionManager);
	}
}
