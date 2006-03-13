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

package org.eclipse.jface.internal.databinding.provisional.observable;

/**
 * Listener for staleness events. An observable object is stale if its state will change
 * eventually.
 * 
 * @since 1.0
 */
public interface IStaleListener {
	
	/**
	 * Handle the event that the given observable object is now stale.
	 * @param source
	 */
	public void handleStale(IObservable source);

}
