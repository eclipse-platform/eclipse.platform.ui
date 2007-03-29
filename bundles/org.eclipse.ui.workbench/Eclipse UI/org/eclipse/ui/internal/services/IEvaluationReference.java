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

import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * This is not meant to be implemented or extended by clients.
 * 
 * @since 3.3
 * 
 */
public interface IEvaluationReference extends IEvaluationResultCache {
	public IPropertyChangeListener getListener();

	public String getProperty();

	/**
	 * Sets whether or not the property change listener should be notified on
	 * changes to the expression held by this result cache. Setting this to
	 * <code>false</code> is useful for times in which you want to prevent flopping of the UI
	 * on variable changes (ie: shell activation).
	 * 
	 * @param evaluationEnabled
	 */
	public void setPostingChanges(boolean evaluationEnabled);

	/**
	 * Returns whether or not the property change listener should be notified on
	 * changes to the expression held by this result cache. Setting this to
	 * <code>false</code> is useful for times in which you want to prevent flopping of the UI
	 * on variable changes (ie: shell activation).
	 * 
	 * @return whether or not listeners should be notified at this time.
	 */
	public boolean isPostingChanges();
	
	/**
	 * Return an evaluation reference that could be impacted by changes to this
	 * evaluation reference.
	 * 
	 * @return the reference. May be <code>null</code>.
	 */
	public IEvaluationReference getTargetReference();
	
	/**
	 * Set an evaluation reference that could be impacted by changes to this
	 * evaluation reference.
	 * 
	 * @param ref the reference. May be <code>null</code>.
	 */
	public void setTargetReference(IEvaluationReference ref);
}
