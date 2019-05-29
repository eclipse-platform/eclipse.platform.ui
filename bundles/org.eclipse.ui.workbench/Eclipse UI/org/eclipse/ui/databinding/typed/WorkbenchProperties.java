/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
 *     Matthew Hall - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.databinding.typed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Factory methods for creating properties for the Workbench.
 *
 * <p>
 * Examples:
 * </p>
 *
 * <pre>
 * WorkbenchProperties.singleSelection().observe(getSite().getService(ISelectionService.class))
 * </pre>
 *
 * <p>
 * This class is a new version of the deprecated class with the same name in the
 * parent package. The difference is that this class returns typed property
 * objects. This class is located in its own package to be able to coexist with
 * the old version while having the same name.
 *
 * @since 3.117
 */
public class WorkbenchProperties {
	/**
	 * Returns a value property which observes the source object as the adapted
	 * type, using the platform adapter manager. If the source is of the target
	 * type, or can be adapted to the target type, this is used as the value of
	 * property, otherwise <code>null</code>.
	 *
	 * @param adapter the adapter class
	 * @return a value property which observes the source object as the adapted
	 *         type.
	 */
	public static <S, T> IValueProperty<S, T> adaptedValue(Class<T> adapter) {
		return adaptedValue(adapter, Platform.getAdapterManager());
	}

	/**
	 * Returns a value property which observes the source object as the adapted
	 * type. If the source object is of the target type, or can be adapted to the
	 * target type, this is used as the value of property, otherwise
	 * <code>null</code>.
	 *
	 * @param adapter        the adapter class
	 * @param adapterManager the adapter manager used to adapt source objects
	 * @return a value property which observes the source object as the adapted
	 *         type.
	 */
	public static <S, T> IValueProperty<S, T> adaptedValue(Class<T> adapter,
			final IAdapterManager adapterManager) {
		return new AdaptedValueProperty<>(adapter, adapterManager);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, T> IValueProperty<S, T> singleSelection() {
		return singleSelection(null, false);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 *
	 * @return an observable value
	 */
	@SuppressWarnings("unchecked")
	public static <S extends ISelectionService, T> IValueProperty<S, T> singleSelection(String partId,
			boolean postSelection) {
		return (IValueProperty<S, T>) singleSelection(partId, postSelection, Object.class);
	}

	/**
	 * Returns a property for observing the first element of a structured selection
	 * as exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 * @param valueType     value type of the selection
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, T> IValueProperty<S, T> singleSelection(String partId,
			boolean postSelection, Class<T> valueType) {
		return new SingleSelectionProperty<>(partId, postSelection, valueType);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @return an observable value
	 */
	@SuppressWarnings("unchecked")
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection() {
		return (IListProperty<S, E>) multipleSelection(Object.class);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @param elementType element type of the selection
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection(Class<E> elementType) {
		return multipleSelection(null, false, elementType);
	}


	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 *
	 * @return an observable value
	 */
	@SuppressWarnings("unchecked")
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection(String partId,
			boolean postSelection) {
		return (IListProperty<S, E>) multipleSelection(partId, postSelection, Object.class);
	}

	/**
	 * Returns a property for observing the elements of a structured selection as
	 * exposed by {@link ISelectionService}.
	 *
	 * @param partId        the part id, or <code>null</code> if the selection can
	 *                      be from any part
	 * @param postSelection <code>true</code> if the selection should be delayed for
	 *                      keyboard-triggered selections
	 * @param elementType   type of selection elements
	 *
	 * @return an observable value
	 */
	public static <S extends ISelectionService, E> IListProperty<S, E> multipleSelection(String partId,
			boolean postSelection, Class<E> elementType) {
		return new MultiSelectionProperty<>(partId, postSelection, elementType);
	}

	private static final class AdaptedValueProperty<S, T> extends SimpleValueProperty<S, T> {
		private final Class<T> adapter;
		private final IAdapterManager adapterManager;

		private AdaptedValueProperty(Class<T> adapter, IAdapterManager adapterManager) {
			this.adapter = adapter;
			this.adapterManager = adapterManager;
		}

		@Override
		public Object getValueType() {
			return adapter;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected T doGetValue(S source) {
			if (adapter.isInstance(source))
				return (T) source;
			return adapterManager.getAdapter(source, adapter);
		}

		@Override
		protected void doSetValue(S source, T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
			return null;
		}
	}

	private static class SingleSelectionProperty<S extends ISelectionService, T> extends SimpleValueProperty<S, T> {
		private final String partId;
		private final boolean post;
		private final Object elementType;

		private SingleSelectionProperty(String partId, boolean post, Object elementType) {
			this.partId = partId;
			this.post = post;
			this.elementType = elementType;
		}

		@Override
		public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
			return new SelectionServiceListener<>(this, listener, partId, post);
		}

		@Override
		protected T doGetValue(S source) {
			ISelection selection;
			if (partId != null) {
				selection = ((ISelectionService) source).getSelection(partId);
			} else {
				selection = ((ISelectionService) source).getSelection();
			}
			if (selection instanceof IStructuredSelection) {
				@SuppressWarnings("unchecked")
				T elem = (T) ((IStructuredSelection) selection).getFirstElement();
				return elem;
			}
			return null;
		}

		@Override
		protected void doSetValue(S source, T value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getValueType() {
			return elementType;
		}
	}

	private static class MultiSelectionProperty<S extends ISelectionService, E> extends SimpleListProperty<S, E> {
		private final String partId;
		private final boolean post;
		private final Object elementType;

		MultiSelectionProperty(String partId, boolean post, Object elementType) {
			this.partId = partId;
			this.post = post;
			this.elementType = elementType;
		}

		@Override
		public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ListDiff<E>> listener) {
			return new SelectionServiceListener<>(this, listener, partId, post);
		}

		@Override
		public Object getElementType() {
			return elementType;
		}

		@Override
		protected List<E> doGetList(S source) {
			ISelection selection;
			if (partId != null) {
				selection = ((ISelectionService) source).getSelection(partId);
			} else {
				selection = ((ISelectionService) source).getSelection();
			}
			if (selection instanceof IStructuredSelection) {
				List<E> list = ((IStructuredSelection) selection).toList();
				return new ArrayList<>(list);
			}
			return Collections.emptyList();
		}

		@Override
		protected void doSetList(S source, List<E> list, ListDiff<E> diff) {
			throw new UnsupportedOperationException();
		}
	}

	private static class SelectionServiceListener<S extends ISelectionService, D extends IDiff>
			extends NativePropertyListener<S, D>
			implements ISelectionListener {
		private final String partId;
		private final boolean post;

		private SelectionServiceListener(IProperty property, ISimplePropertyListener<S, D> wrapped, String partID,
				boolean post) {
			super(property, wrapped);
			this.partId = partID;
			this.post = post;
		}

		@Override
		protected void doAddTo(S source) {
			ISelectionService selectionService = source;
			if (post) {
				if (partId != null) {
					selectionService.addPostSelectionListener(partId, this);
				} else {
					selectionService.addPostSelectionListener(this);
				}
			} else {
				if (partId != null) {
					selectionService.addSelectionListener(partId, this);
				} else {
					selectionService.addSelectionListener(this);
				}
			}
		}

		@Override
		protected void doRemoveFrom(S source) {
			ISelectionService selectionService = source;
			if (post) {
				if (partId != null) {
					selectionService.removePostSelectionListener(partId, this);
				} else {
					selectionService.removePostSelectionListener(this);
				}
			} else {
				if (partId != null) {
					selectionService.removeSelectionListener(partId, this);
				} else {
					selectionService.removeSelectionListener(this);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			fireChange((S) part, null);
		}
	}
}
