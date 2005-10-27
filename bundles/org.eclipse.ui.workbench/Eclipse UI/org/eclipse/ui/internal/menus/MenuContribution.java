/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.menus.MenuElement;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.sources.EvaluationResultCache;
import org.eclipse.ui.menus.IMenuContribution;
import org.eclipse.ui.menus.IMenuService;

/**
 * <p>
 * A token representing the contribution of a menu element. This token can later
 * be used to cancel that contribution. Without this token, then menu element
 * will only become inactive if the component in which the handler was
 * contributed is destroyed.
 * </p>
 * <p>
 * This caches the menu element, so that they can later be identified.
 * </p>
 * 
 * @since 3.2
 */
final class MenuContribution extends EvaluationResultCache implements
		IMenuContribution {

	/**
	 * The menu element that has been contributed. This value may be
	 * <code>null</code>.
	 */
	private final MenuElement menuElement;

	/**
	 * The menu service from which this menu contribution was request. This
	 * value is never <code>null</code>.
	 */
	private final IMenuService menuService;

	/**
	 * Constructs a new instance of <code>MenuContribution</code>.
	 * 
	 * @param menuElement
	 *            The menu element that has been contributed. This value may be
	 *            <code>null</code>.
	 * @param expression
	 *            The expression that must evaluate to <code>true</code>
	 *            before this menu contribution is visible. This value may be
	 *            <code>null</code> if it is always active.
	 * @param menuService
	 *            The menu service from which the contribution was requested;
	 *            must not be <code>null</code>.
	 * @see ISources
	 */
	public MenuContribution(final MenuElement menuElement,
			final Expression expression, final IMenuService menuService) {
		super(expression);

		if (menuElement == null) {
			throw new NullPointerException(
					"The menu element for a contribution cannot be null"); //$NON-NLS-1$
		}

		if (menuService == null) {
			throw new NullPointerException(
					"The menu service for a contribution cannot be null"); //$NON-NLS-1$
		}

		this.menuElement = menuElement;
		this.menuService = menuService;
	}

	public final MenuElement getMenuElement() {
		return menuElement;
	}

	public final IMenuService getMenuService() {
		return menuService;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("MenuContribution(menuElement="); //$NON-NLS-1$
		buffer.append(menuElement);
		buffer.append(",menuService="); //$NON-NLS-1$
		buffer.append(menuService);
		buffer.append(",sourcePriority="); //$NON-NLS-1$
		buffer.append(getSourcePriority());
		buffer.append(')');

		return buffer.toString();
	}
}
