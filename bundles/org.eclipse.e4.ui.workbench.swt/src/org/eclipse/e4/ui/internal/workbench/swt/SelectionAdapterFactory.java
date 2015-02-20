/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
	private static final ICountable ICOUNT_0 = new ICountable() {
		@Override
		public int count() {
			return 0;
		}
	};
	private static final ICountable ICOUNT_1 = new ICountable() {
		@Override
		public int count() {
			return 1;
		}
	};
	private static final IIterable ITERATE_EMPTY = new IIterable() {
		@Override
		public Iterator<?> iterator() {
			return Collections.EMPTY_LIST.iterator();
		}
	};

	/**
	 * The classes we can adapt to.
	 */
	private static final Class<?>[] CLASSES = new Class[] { IIterable.class,
			ICountable.class };

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

	private Object iterable(final ISelection sel) {
		if (sel.isEmpty()) {
			return ITERATE_EMPTY;
		}
		if (sel instanceof IStructuredSelection) {
			return new IIterable() {
				@Override
				public Iterator<?> iterator() {
					return ((IStructuredSelection) sel).iterator();
				}
			};
		}
		final List<?> list = Arrays.asList(new Object[] { sel });
		return new IIterable() {

			@Override
			public Iterator<?> iterator() {
				return list.iterator();
			}
		};
	}

	private Object countable(final ISelection sel) {
		if (sel.isEmpty()) {
			return ICOUNT_0;
		}
		if (sel instanceof IStructuredSelection) {
			final IStructuredSelection ss = (IStructuredSelection) sel;
			return new ICountable() {
				@Override
				public int count() {
					return ss.size();
				}
			};
		}
		return ICOUNT_1;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return CLASSES;
	}
}
