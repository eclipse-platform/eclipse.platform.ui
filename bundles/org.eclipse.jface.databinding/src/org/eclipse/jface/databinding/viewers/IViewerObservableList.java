/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * {@link IObservableList} observing a JFace Viewer.
 *
 * @param <E>
 *            the type of elements in this collection
 *
 * @since 1.2
 */
public interface IViewerObservableList<E> extends IObservableList<E>, IViewerObservable {
}
