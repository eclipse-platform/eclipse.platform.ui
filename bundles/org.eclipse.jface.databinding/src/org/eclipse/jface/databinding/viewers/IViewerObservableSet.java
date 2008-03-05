/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * {@link IObservableSet} observing a JFace Viewer.
 * 
 * @since 1.2
 * 
 */
public interface IViewerObservableSet extends IObservableSet, IViewerObservable {
}
