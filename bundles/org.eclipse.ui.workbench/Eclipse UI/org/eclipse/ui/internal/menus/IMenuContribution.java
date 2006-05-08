/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

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
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is meant to exist in the <code>org.eclipse.ui.menus</code>
 * package.
 * </p>
 * 
 * @since 3.2
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
