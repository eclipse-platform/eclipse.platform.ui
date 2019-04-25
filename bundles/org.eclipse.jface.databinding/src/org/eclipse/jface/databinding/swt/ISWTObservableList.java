/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * {@link IObservableList} observing an SWT widget.
 *
 * @since 1.3
 *
 * @param <E>
 *            the type of elements in this collection
 *
 */
public interface ISWTObservableList<E> extends ISWTObservable, IObservableList<E> {

}
