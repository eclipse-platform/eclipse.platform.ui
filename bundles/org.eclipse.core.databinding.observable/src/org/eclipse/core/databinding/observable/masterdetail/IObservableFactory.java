/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.IObservable;

/**
 * Generates an {@link IObservable} when passed a target instance.
 *
 * @param <T>
 *            type of the target
 * @param <E>
 *            type of the observable constructed by this factory; this type must
 *            extend or implement IObservable
 *
 * @since 1.0
 */
public interface IObservableFactory<T, E extends IObservable> {

	/**
	 * Creates an observable for the given target object.
	 *
	 * @param target
	 * @return the new observable
	 */
	public E createObservable(T target);

}
