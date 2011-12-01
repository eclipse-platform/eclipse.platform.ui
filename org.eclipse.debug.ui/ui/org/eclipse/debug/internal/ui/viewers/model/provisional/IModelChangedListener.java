/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

/**
 * A model changed listener is notified of changes in a model. A model
 * is represented by an {@link IModelProxy}.
 *
 * @since 3.2
 * @see IModelProxy
 * @see IModelDelta
 */
public interface IModelChangedListener {
	
	/**
	 * Notification a model has changed as described by the given delta.
	 * 
	 * @param delta model delta
	 * @param proxy proxy that created the delta
	 */
	public void modelChanged(IModelDelta delta, IModelProxy proxy);

}
