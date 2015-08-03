/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263868)
 *     Matthew Hall - bug 268203
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.set;

import java.util.Set;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;

/**
 * @param <E>
 *            type of the elements in the set
 * @since 3.3
 *
 */
public final class SelfSetProperty<E> extends SimpleSetProperty<Set<E>, E> {
	private final Object elementType;

	/**
	 * @param elementType
	 */
	public SelfSetProperty(Object elementType) {
		this.elementType = elementType;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected Set<E> doGetSet(Set<E> source) {
		return source;
	}

	@Override
	protected void doSetSet(Set<E> source, Set<E> set, SetDiff<E> diff) {
		diff.applyTo(source);
	}

	@Override
	public INativePropertyListener<Set<E>> adaptListener(ISimplePropertyListener<Set<E>, SetDiff<E>> listener) {
		return null; // no listener API
	}

	protected void doAddListener(Object source,
			INativePropertyListener<Set<E>> listener) {
	}

	protected void doRemoveListener(Set<E> source,
			INativePropertyListener<Set<E>> listener) {
	}
}