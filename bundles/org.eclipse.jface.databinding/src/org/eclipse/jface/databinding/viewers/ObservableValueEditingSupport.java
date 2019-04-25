/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Matthew Hall - bug 234496
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * {@link EditingSupport} using the JFace Data Binding concepts to handle the
 * updating of an element from a {@link CellEditor}.
 * <p>
 * If {@code M} and {@code T} are different then they must be converted to each
 * other.
 *
 * @param <E> type of the model element with a property that is being edited
 * @param <M> type of the value in the model that is being edited, the value of
 *            the property on the model element
 * @param <T> type of the target value that actually is being edited by the user
 *
 * @since 1.2
 */
public abstract class ObservableValueEditingSupport<E, M, T> extends EditingSupport {
	/**
	 * Returns an ObservableValueEditingSupport instance which binds the given
	 * cell editor property to the given element property.
	 *
	 * @param viewer
	 *            the column viewer
	 * @param dbc
	 *            the DataBindingContext used for binding between the cell
	 *            editor and the viewer element.
	 * @param cellEditor
	 *            the cell editor
	 * @param cellEditorProperty
	 *            the cell editor property to be bound to the element.
	 * @param elementProperty
	 *            the element property to be bound to the cell editor.
	 * @return an ObservableValueEditingSupport instance using the given
	 *         arguments.
	 * @since 1.3
	 */
	public static <E, M, T> EditingSupport create(ColumnViewer viewer,
			DataBindingContext dbc, final CellEditor cellEditor,
			final IValueProperty<? super CellEditor, T> cellEditorProperty,
			final IValueProperty<E, M> elementProperty) {
		return new ObservableValueEditingSupport<E, M, T>(viewer, dbc) {
			@Override
			protected IObservableValue<T> doCreateCellEditorObservable(CellEditor cellEditor) {
				return cellEditorProperty.observe(cellEditor);
			}

			@Override
			protected IObservableValue<M> doCreateElementObservable(E element, ViewerCell cell) {
				return elementProperty.observe(element);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
		};
	}

	/**
	 * Maintains references to the instances currently imployed while editing.
	 * Will be <code>null</code> when not editing.
	 */
	private EditingState<T, M> editingState;

	private final ColumnViewerEditorActivationListenerHelper activationListener = new ColumnViewerEditorActivationListenerHelper();

	private ColumnViewer viewer;

	private DataBindingContext dbc;

	/**
	 * Constructs a new instance with the provided <code>viewer</code> and
	 * <code>dbc</code>.
	 *
	 * @param viewer
	 *            viewer to edit
	 * @param dbc
	 *            dbc to create <code>Bindings</code>
	 */
	public ObservableValueEditingSupport(ColumnViewer viewer, DataBindingContext dbc) {
		super(viewer);

		if (dbc == null) {
			throw new IllegalArgumentException("Parameter dbc was null."); //$NON-NLS-1$
		}

		this.viewer = viewer;
		this.dbc = dbc;
	}

	/**
	 * Default implementation always returns <code>true</code>.
	 *
	 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
	 */
	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	/**
	 * Default implementation always returns <code>null</code> as this will be
	 * handled by the Binding.
	 *
	 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
	 */
	@Override
	protected Object getValue(Object element) {
		// no op
		return null;
	}

	/**
	 * Default implementation does nothing as this will be handled by the
	 * Binding.
	 *
	 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	protected void setValue(Object element, Object value) {
		// no op
	}

	/**
	 * Creates a {@link Binding} between the editor and the element to be
	 * edited. Invokes {@link #doCreateCellEditorObservable(CellEditor)},
	 * {@link #doCreateElementObservable(Object, ViewerCell)}, and then
	 * {@link #createBinding(IObservableValue, IObservableValue)}.
	 */
	@Override
	final protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		IObservableValue<T> target = doCreateCellEditorObservable(cellEditor);
		Assert.isNotNull(target, "doCreateCellEditorObservable(...) did not return an observable"); //$NON-NLS-1$

		@SuppressWarnings("unchecked")
		IObservableValue<M> model = doCreateElementObservable((E) cell.getElement(), cell);
		Assert.isNotNull(model, "doCreateElementObservable(...) did not return an observable"); //$NON-NLS-1$

		dirty = false;

		Binding binding = createBinding(target, model);

		target.addChangeListener(_event -> dirty = true);

		Assert.isNotNull(binding, "createBinding(...) did not return a binding"); //$NON-NLS-1$

		editingState = new EditingState<>(binding, target, model);

		getViewer().getColumnViewerEditor().addEditorActivationListener(activationListener);
	}

	/**
	 * Creates the observable value for the CellEditor.
	 *
	 * @param cellEditor
	 * @return observable value
	 */
	protected abstract IObservableValue<T> doCreateCellEditorObservable(CellEditor cellEditor);

	/**
	 * Creates the observable value for the element.
	 *
	 * @param element
	 * @param cell
	 * @return observable value
	 */
	protected abstract IObservableValue<M> doCreateElementObservable(E element, ViewerCell cell);

	/**
	 * Creates a new binding for the provided <code>target</code> and
	 * <code>model</code>. Default {@link UpdateValueStrategy value update
	 * strategies} are used with the target to model updating on
	 * {@link UpdateValueStrategy#POLICY_CONVERT}.
	 *
	 * @param target
	 * @param model
	 * @return binding
	 */
	// TODO j: These values are converted, do not need to be the same
	protected Binding createBinding(IObservableValue<T> target, IObservableValue<M> model) {
		return dbc.bindValue(target, model, new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_CONVERT), null);
	}

	boolean dirty = false;

	/**
	 * Updates the model from the target.
	 */
	@Override
	final protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		if (dirty) {
			editingState.binding.updateTargetToModel();
			dirty = false;
		}
	}

	private class ColumnViewerEditorActivationListenerHelper extends ColumnViewerEditorActivationListener {

		@Override
		public void afterEditorActivated(ColumnViewerEditorActivationEvent event) {
			// do nothing
		}

		@Override
		public void afterEditorDeactivated(
				ColumnViewerEditorDeactivationEvent event) {
			editingState.dispose();
			editingState = null;

			viewer.getColumnViewerEditor().removeEditorActivationListener(this);
		}

		@Override
		public void beforeEditorActivated(ColumnViewerEditorActivationEvent event) {
			// do nothing
		}

		@Override
		public void beforeEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {
			// do nothing
		}
	}

	/**
	 * Maintains references to objects that only live for the length of the edit
	 * cycle.
	 */
	private static class EditingState<T, M> {
		IObservableValue<T> target;
		IObservableValue<M> model;
		Binding binding;

		EditingState(Binding binding, IObservableValue<T> target, IObservableValue<M> model) {
			this.binding = binding;
			this.target = target;
			this.model = model;
		}

		void dispose() {
			binding.dispose();
			target.dispose();
			model.dispose();
		}
	}
}
