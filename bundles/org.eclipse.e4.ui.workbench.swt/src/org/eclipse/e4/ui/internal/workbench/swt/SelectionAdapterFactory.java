/*******************************************************************************
 * Copyright (c) 2010, 2025 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.expressions.ICountable;
import org.eclipse.core.expressions.IIterable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Adapts ISelection instances to either IIterable or ICountable. For use with
 * core expressions.
 *
 * @since 3.3
 */
public class SelectionAdapterFactory implements IAdapterFactory {

	private static final ICountable ICOUNT_0 = () -> 0;

	private static final ICountable ICOUNT_1 = () -> 1;

	private static final IIterable<?> ITERATE_EMPTY = () -> Collections.emptyList().iterator();

	/**
	 * The classes we can adapt to.
	 */
	private static final Class<?>[] CLASSES = { IIterable.class, ICountable.class };

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ISelection) {
			if (adapterType == IIterable.class) {
				return adapterType.cast(iterable((ISelection) adaptableObject));
			} else if (adapterType == ICountable.class) {
				return adapterType.cast(countable((ISelection) adaptableObject));
			}
		}
		return null;
	}

	private IIterable<?> iterable(final ISelection sel) {
		if (sel.isEmpty()) {
			return ITERATE_EMPTY;
		}
		if (sel instanceof IStructuredSelection) {
			return ((IStructuredSelection) sel)::iterator;
		}
		final List<Object> list = Arrays.asList(new Object[] { sel });
		return list::iterator;
	}

	private ICountable countable(final ISelection sel) {
		if (sel.isEmpty()) {
			return ICOUNT_0;
		}
		if (sel instanceof IStructuredSelection) {
			final IStructuredSelection ss = (IStructuredSelection) sel;
			return ss::size;
		}
		return ICOUNT_1;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return CLASSES;
	}
}
