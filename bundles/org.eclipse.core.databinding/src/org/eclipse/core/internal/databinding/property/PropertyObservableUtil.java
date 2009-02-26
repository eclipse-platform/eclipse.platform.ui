/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 265727
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;

/**
 * @since 3.3
 * 
 */
public class PropertyObservableUtil {
	/**
	 * Causes the target observable to be disposed when the source observable is
	 * disposed.
	 * 
	 * @param source
	 *            the source observable
	 * @param target
	 *            the target observable
	 */
	public static void cascadeDispose(IObservable source,
			final IObservable target) {
		source.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent staleEvent) {
				target.dispose();
			}
		});
	}
}
