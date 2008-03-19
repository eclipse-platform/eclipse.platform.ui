/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * A wrapper class for viewer elements, which uses an {@link IElementComparer}
 * for computing {@link Object#equals(Object) equality} and
 * {@link Object#hashCode() hashes}.
 * 
 * @since 1.2
 */
class ViewerElementWrapper {
	private final Object element;
	private final IElementComparer comparer;

	ViewerElementWrapper(Object element, IElementComparer comparer) {
		this.element = element;
		this.comparer = comparer;
	}

	public boolean equals(Object obj) {
		return comparer.equals(element, ((ViewerElementWrapper) obj).element);
	}

	public int hashCode() {
		return comparer.hashCode(element);
	}

	Object unwrap() {
		return element;
	}
}