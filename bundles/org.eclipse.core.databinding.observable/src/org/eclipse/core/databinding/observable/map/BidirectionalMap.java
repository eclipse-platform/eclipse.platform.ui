/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 233306
 *******************************************************************************/
package org.eclipse.core.databinding.observable.map;

import org.eclipse.core.databinding.observable.Realm;

/**
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * @since 1.0
 * 
 * @deprecated This class is deprecated; use {@link BidiObservableMap} instead.
 */
public class BidirectionalMap extends ObservableMap {
	private IMapChangeListener mapListener = new IMapChangeListener() {
		public void handleMapChange(MapChangeEvent event) {
			fireMapChange(event.diff);
		}
	};

	/**
	 * @param wrappedMap
	 */
	public BidirectionalMap(IObservableMap wrappedMap) {
		super(wrappedMap.getRealm(), wrappedMap);
		wrappedMap.addMapChangeListener(mapListener);
	}
}
