/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.databinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.databinding.observable.list.ListDiff;
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
 * 
 * <pre>
 * WorkbenchProperties.singleSelection().observe(
 * 		getSite().getService(ISelectionService.class))
 * </pre>
 * 
 * </p>
 * 
 * @since 3.5
 */
public class WorkbenchProperties {
	/**
	 * Returns a value property which observes the source object as the adapted
	 * type, using the platform adapter manager. If the source is of the target
	 * type, or can be adapted to the target type, this is used as the value of
	 * property, otherwise <code>null</code>.
	 * 
	 * @param adapter
	 *            the adapter class
	 * @return a value property which observes the source object as the adapted
	 *         type.
	 */
	public static IValueProperty adaptedValue(Class adapter) {
		return adaptedValue(adapter, Platform.getAdapterManager());
	}

	/**
	 * Returns a value property which observes the source object as the adapted
	 * type. If the source object is of the target type, or can be adapted to
	 * the target type, this is used as the value of property, otherwise
	 * <code>null</code>.
	 * 
	 * @param adapter
	 *            the adapter class
	 * @param adapterManager
	 *            the adapter manager used to adapt source objects
	 * @return a value property which observes the source object as the adapted
	 *         type.
	 */
	static IValueProperty adaptedValue(final Class adapter,
			final IAdapterManager adapterManager) {
		return new AdaptedValueProperty(adapter, adapterManager);
	}

	/**
	 * Returns a property for observing the first element of a structured
	 * selection as exposed by {@link ISelectionService}.
	 * 
	 * @return an observable value
	 */
	public static IValueProperty singleSelection() {
		return singleSelection(null, false);
	}

	/**
	 * Returns a property for observing the first element of a structured
	 * selection as exposed by {@link ISelectionService}.
	 * 
	 * @param partId
	 *            the part id, or <code>null</code> if the selection can be from
	 *            any part
	 * @param postSelection
	 *            <code>true</code> if the selection should be delayed for
	 *            keyboard-triggered selections
	 * 
	 * @return an observable value
	 */
	public static IValueProperty singleSelection(String partId,
			boolean postSelection) {
		return new SingleSelectionProperty(partId, postSelection);
	}

	/**
	 * Returns a property for observing the elements of a structured selection
	 * as exposed by {@link ISelectionService}.
	 * 
	 * @return an observable value
	 */
	public static IListProperty multipleSelection() {
		return multipleSelection(null, false);
	}

	/**
	 * Returns a property for observing the elements of a structured selection
	 * as exposed by {@link ISelectionService}.
	 * 
	 * @param partId
	 *            the part id, or <code>null</code> if the selection can be from
	 *            any part
	 * @param postSelection
	 *            <code>true</code> if the selection should be delayed for
	 *            keyboard-triggered selections
	 * 
	 * @return an observable value
	 */
	public static IListProperty multipleSelection(String partId,
			boolean postSelection) {
		return new MultiSelectionProperty(partId, postSelection);
	}

	static final class AdaptedValueProperty extends SimpleValueProperty {
		private final Class adapter;
		private final IAdapterManager adapterManager;

		private AdaptedValueProperty(Class adapter,
				IAdapterManager adapterManager) {
			this.adapter = adapter;
			this.adapterManager = adapterManager;
		}

		public Object getValueType() {
			return adapter;
		}

		protected Object doGetValue(Object source) {
			if (adapter.isInstance(source))
				return source;
			return adapterManager.getAdapter(source, adapter);
		}

		protected void doSetValue(Object source, Object value) {
			throw new UnsupportedOperationException();
		}

		public INativePropertyListener adaptListener(
				ISimplePropertyListener listener) {
			return null;
		}
	}

	static class SingleSelectionProperty extends SimpleValueProperty {
		private final String partId;
		private final boolean post;

		SingleSelectionProperty(String partId, boolean post) {
			this.partId = partId;
			this.post = post;
		}

		public INativePropertyListener adaptListener(
				ISimplePropertyListener listener) {
			return new SelectionServiceListener(this, listener, partId, post);
		}

		protected Object doGetValue(Object source) {
			ISelection selection;
			if (partId != null) {
				selection = ((ISelectionService) source).getSelection(partId);
			} else {
				selection = ((ISelectionService) source).getSelection();
			}
			if (selection instanceof IStructuredSelection) {
				return ((IStructuredSelection) selection).getFirstElement();
			}
			return null;
		}

		protected void doSetValue(Object source, Object value) {
			throw new UnsupportedOperationException();
		}

		public Object getValueType() {
			return Object.class;
		}
	}

	static class MultiSelectionProperty extends SimpleListProperty {
		private final String partId;
		private final boolean post;

		MultiSelectionProperty(String partId, boolean post) {
			this.partId = partId;
			this.post = post;
		}

		public INativePropertyListener adaptListener(
				ISimplePropertyListener listener) {
			return new SelectionServiceListener(this, listener, partId, post);
		}

		public Object getElementType() {
			return Object.class;
		}

		protected List doGetList(Object source) {
			ISelection selection;
			if (partId != null) {
				selection = ((ISelectionService) source).getSelection(partId);
			} else {
				selection = ((ISelectionService) source).getSelection();
			}
			if (selection instanceof IStructuredSelection) {
				return new ArrayList(((IStructuredSelection) selection)
						.toList());
			}
			return Collections.EMPTY_LIST;
		}

		protected void doSetList(Object source, List list, ListDiff diff) {
			throw new UnsupportedOperationException();
		}
	}

	static class SelectionServiceListener extends NativePropertyListener
			implements ISelectionListener {
		private final String partId;
		private final boolean post;

		public SelectionServiceListener(IProperty property,
				ISimplePropertyListener wrapped, String partID, boolean post) {
			super(property, wrapped);
			this.partId = partID;
			this.post = post;
		}

		protected void doAddTo(Object source) {
			ISelectionService selectionService = (ISelectionService) source;
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

		protected void doRemoveFrom(Object source) {
			ISelectionService selectionService = (ISelectionService) source;
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

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			fireChange(part, null);
		}
	}
}
