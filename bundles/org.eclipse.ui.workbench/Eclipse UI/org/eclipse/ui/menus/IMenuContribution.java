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

package org.eclipse.ui.menus;

import org.eclipse.jface.menus.MenuElement;
import org.eclipse.ui.internal.services.IEvaluationResultCache;

/**
 * <p>
 * A token representing the contribution of a menu element. This token can later
 * be used to remove the contribution. Without this token, then the contribution
 * will only become inactive if the component in which the handler was activated
 * is destroyed.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @since 3.1
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.ISourceProvider
 */
public interface IMenuContribution extends IEvaluationResultCache {

	/**
	 * Returns the handler that should be activated.
	 * 
	 * @return The handler; may be <code>null</code>.
	 */
	public MenuElement getMenuElement();

	/**
	 * Returns the menu service from which this contribution was requested. This
	 * is used to ensure that a contribution can only be retracted from the same
	 * service which issued it.
	 * 
	 * @return The menu service; never <code>null</code>.
	 */
	public IMenuService getMenuService();
}
