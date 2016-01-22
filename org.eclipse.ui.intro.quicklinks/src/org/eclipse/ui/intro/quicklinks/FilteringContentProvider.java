/*******************************************************************************
 * Copyright (c) 2016 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Manumitting Technologies Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro.quicklinks;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A structured content provider that first transforms elements with a function.
 *
 * @param <O>
 *            the object type
 */
public class FilteringContentProvider<O> implements IStructuredContentProvider {

	private Function<Object, O> function;

	public FilteringContentProvider(Function<Object, O> f) {
		this.function = f;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return Stream.of((Object[]) inputElement).map(function).toArray();
		} else if (inputElement instanceof Collection) {
			return ((Collection<?>) inputElement).stream().map(function).toArray();
		}
		return Stream.of(inputElement).map(function).toArray();
	}
}
