/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.menus;

import org.eclipse.ui.services.IServiceLocator;

/**
 * Allow a menu contribution to be initialized with the appropriate service
 * locator.
 * 
 * @since 3.4
 */
public interface IWorkbenchContribution {
	/**
	 * The service locator for this contribution. It will potentially exist
	 * longer than the lifecycle of this specific contribution, so
	 * ContributionItems should remove themselves from any listeners or services
	 * in their dispose() calls.
	 * 
	 * @param serviceLocator
	 *            the locator which services can be retrieved. Will not be
	 *            <code>null</code>
	 */
	public void initialize(IServiceLocator serviceLocator);
}
