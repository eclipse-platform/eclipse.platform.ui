/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import org.eclipse.core.databinding.observable.IObservablesListener;


/**
 * Listener for changes of observable lists.
 * 
 * @since 1.0
 */
public interface IListChangeListener extends IObservablesListener {
	
	/**
	 * Handle a change to the given observable list. The change is described by the diff objects.
	 * @param event
	 */
	void handleListChange(ListChangeEvent event);

}
