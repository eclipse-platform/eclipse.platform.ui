/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * {@link IObservableSet} observing a JFace Viewer.
 *
 * @param <E>
 *            the type of the elements in this set
 *
 * @since 1.2
 */
public interface IViewerObservableSet<E> extends IObservableSet<E>, IViewerObservable {
}
