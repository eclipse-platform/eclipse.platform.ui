/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.activities;

import org.eclipse.ui.internal.activities.CompoundActivityService;
import org.eclipse.ui.internal.activities.MutableActivityService;

/**
 * <p>
 * This class allows clients to broker instances of <code>IActivityService</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IActivityService
 */
public final class ActivityServiceFactory {

	/**
	 * Creates a new instance of ICompoundActivityService.
	 * 
	 * @return a new instance of ICompoundActivityService. Clients should not
	 *         make assumptions about the concrete implementation outside the
	 *         contract of <code>ICompoundActivityService</code>. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static ICompoundActivityService getCompoundActivityService() {
		return new CompoundActivityService();
	}

	/**
	 * Creates a new instance of IMutableActivityService.
	 * 
	 * @return a new instance of IMutableActivityService. Clients should not
	 *         make assumptions about the concrete implementation outside the
	 *         contract of <code>IMutableActivityService</code>. Guaranteed
	 *         not to be <code>null</code>.
	 */
	public static IMutableActivityService getMutableActivityService() {
		return new MutableActivityService();
	}

	private ActivityServiceFactory() {
	}
}
