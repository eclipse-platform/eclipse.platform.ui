/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

/**
 * 
 *
 * @since 3.2
 */
public interface IModelProxy {

	public void init(IPresentationContext context);
	public void dispose();
	public void addModelChangedListener(IModelChangedListener listener);
	public void removeModelChangedListener(IModelChangedListener listener);
	
	// TODO: should be part of the implementation rather than the interface
	public void fireModelChanged(IModelDelta delta);
	
}
