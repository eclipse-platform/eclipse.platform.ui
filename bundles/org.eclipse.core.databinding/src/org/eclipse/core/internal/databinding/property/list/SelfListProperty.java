/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263868)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.list;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.list.SimpleListProperty;

/**
 * @since 3.3
 * 
 */
public class SelfListProperty extends SimpleListProperty {
	private final Object elementType;

	/**
	 * @param elementType
	 */
	public SelfListProperty(Object elementType) {
		this.elementType = elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected List doGetList(Object source) {
		// An observable may cache the returned list. We clone the list
		// to prevent computed diffs from always being empty.
		return new ArrayList((List) source);
	}

	protected void doSetList(Object source, List list, ListDiff diff) {
		diff.applyTo((List) source);
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null; // no listener API
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
	}
}