/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.operations;

/**
 * Listener for update model changes. 
 * Usually, when features are installed, configured, etc.
 * a GUI may need to update its state, so it will have to register
 * with the OperationsManager for update events.
 * IUpdateModelChangedListener
 */
public interface IUpdateModelChangedListener {
	/**
	 * Called after a feature/site/etc. is added
	 * @param parent
	 * @param children
	 */
	public void objectsAdded(Object parent, Object [] children);
	/**
	 * Called after a feature/site/etc. is removed.
	 * @param parent
	 * @param children
	 */
	public void objectsRemoved(Object parent, Object [] children);
	/**
	 * Called when there are changes to a site/feature/etc.
	 * @param object
	 * @param property
	 */
	public void objectChanged(Object object, String property);
}
