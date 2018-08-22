/*******************************************************************************
 * Copyright (c) 2009, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 265727
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

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
		source.addDisposeListener(staleEvent -> target.dispose());
	}
}
