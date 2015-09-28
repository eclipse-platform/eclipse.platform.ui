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

package org.eclipse.core.databinding.observable.list;

import org.eclipse.core.databinding.observable.IObservablesListener;

/**
 * Listener for changes to observable lists.
 *
 * @param <E>
 *            the type of elements in this collection
 *
 * @since 1.0
 */
@FunctionalInterface
public interface IListChangeListener<E> extends IObservablesListener {

	/**
	 * Handle a change to an observable list. The change is described by the
	 * diff object. The given event object must only be used locally in this
	 * method because it may be reused for other change notifications. The diff
	 * object referenced by the event is immutable and may be used non-locally.
	 *
	 * @param event
	 */
	void handleListChange(ListChangeEvent<? extends E> event);
}
