/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import org.eclipse.core.databinding.observable.IObservablesListener;

/**
 * Listener for changes to observable maps.
 * 
 * @since 1.0
 * 
 */
public interface IMapChangeListener extends IObservablesListener {

	/**
	 * Handle a change an observable map. The given event object must only be
	 * used locally in this method because it may be reused for other change
	 * notifications. The diff object referenced by the event is immutable and
	 * may be used non-locally.
	 * 
	 * @param event
	 *            the event
	 */
	void handleMapChange(MapChangeEvent event);

}
