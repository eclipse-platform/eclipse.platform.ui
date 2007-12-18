/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

/**
 * Internal methods used by the framework to prevent inappropriate listener
 * notification.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @since 3.4
 */
public interface IEvaluationReference extends IEvaluationResultCache {
	/**
	 * Sets whether or not the property change listener should be notified on
	 * changes to the expression held by this result cache. Setting this to
	 * <code>false</code> will prevent re-evaluations and listener
	 * notifications. This can be called by the framework at any time.
	 * 
	 * @param evaluationEnabled
	 */
	public void setPostingChanges(boolean evaluationEnabled);

	/**
	 * Returns whether or not the property change listener should be notified on
	 * changes to the expression held by this result cache.
	 * 
	 * @return whether or not listeners should be notified at this time.
	 */
	public boolean isPostingChanges();

}
