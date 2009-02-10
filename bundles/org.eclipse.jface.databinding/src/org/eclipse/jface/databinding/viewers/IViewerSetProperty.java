/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link ISetProperty} for observing a JFace viewer
 * 
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewerSetProperty extends ISetProperty {
	/**
	 * Returns an {@link IViewerObservableSet} observing this set property on
	 * the given viewer
	 * 
	 * @param viewer
	 *            the source viewer
	 * @return an observable set observing this set property on the given viewer
	 */
	public IViewerObservableSet observe(Viewer viewer);
}
