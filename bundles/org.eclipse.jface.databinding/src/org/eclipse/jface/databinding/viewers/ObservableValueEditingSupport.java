/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 * @since 1.2
 */
public abstract class ObservableValueEditingSupport extends EditingSupport {
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
	public static EditingSupport create(ColumnViewer viewer,
			DataBindingContext dbc, final CellEditor cellEditor,
			final IValueProperty cellEditorProperty,
			final IValueProperty elementProperty) {
		return new ObservableValueEditingSupport(viewer, dbc) {
			protected IObservableValue doCreateCellEditorObservable(
					CellEditor cellEditor) {
				return cellEditorProperty.observe(cellEditor);
			}

			protected IObservableValue doCreateElementObservable(
					Object element, ViewerCell cell) {
				return elementProperty.observe(element);
			}

			protected CellEditor getCellEditor(Object element) {
				return cellEditor;
			}
		};
	}

	/**
	 * Maintains references to the instances currently imployed while editing.
	 * Will be <code>null</code> when not editing.
	 */
	private EditingState editingState;

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
	public ObservableValueEditingSupport(ColumnViewer viewer,
			DataBindingContext dbc) {
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
	protected boolean canEdit(Object element) {
		return true;
	}

	/**
	 * Default implementation always returns <code>null</code> as this will be
	 * handled by the Binding.
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
	 */
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
	protected void setValue(Object element, Object value) {
		// no op
	}

	/**
	 * Creates a {@link Binding} between the editor and the element to be
	 * edited. Invokes {@link #doCreateCellEditorObservable(CellEditor)},
	 * {@link #doCreateElementObservable(Object, ViewerCell)}, and then
	 * {@link #createBinding(IObservableValue, IObservableValue)}.
	 */
	final protected void initializeCellEditorValue(CellEditor cellEditor,
			ViewerCell cell) {
		IObservableValue target = doCreateCellEditorObservable(cellEditor);
		Assert
				.isNotNull(target,
						"doCreateCellEditorObservable(...) did not return an observable"); //$NON-NLS-1$

		IObservableValue model = doCreateElementObservable(cell.getElement(),
				cell);
		Assert.isNotNull(model,
				"doCreateElementObservable(...) did not return an observable"); //$NON-NLS-1$

		Binding binding = createBinding(target, model);
		Assert
				.isNotNull(binding,
						"createBinding(...) did not return a binding"); //$NON-NLS-1$

		editingState = new EditingState(binding, target, model);

		getViewer().getColumnViewerEditor().addEditorActivationListener(
				activationListener);
	}

	/**
	 * Creates the observable value for the CellEditor.
	 * 
	 * @param cellEditor
	 * @return observable value
	 */
	protected abstract IObservableValue doCreateCellEditorObservable(
			CellEditor cellEditor);

	/**
	 * Creates the observable value for the element.
	 * 
	 * @param element
	 * @param cell
	 * @return observable value
	 */
	protected abstract IObservableValue doCreateElementObservable(
			Object element, ViewerCell cell);

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
	protected Binding createBinding(IObservableValue target,
			IObservableValue model) {
		return dbc.bindValue(target, model, new UpdateValueStrategy(
				UpdateValueStrategy.POLICY_CONVERT), null);
	}

	/**
	 * Updates the model from the target.
	 */
	final protected void saveCellEditorValue(CellEditor cellEditor,
			ViewerCell cell) {
		editingState.binding.updateTargetToModel();
	}

	private class ColumnViewerEditorActivationListenerHelper extends
			ColumnViewerEditorActivationListener {

		public void afterEditorActivated(ColumnViewerEditorActivationEvent event) {
			// do nothing
		}

		public void afterEditorDeactivated(
				ColumnViewerEditorDeactivationEvent event) {
			editingState.dispose();
			editingState = null;

			viewer.getColumnViewerEditor().removeEditorActivationListener(this);
		}

		public void beforeEditorActivated(
				ColumnViewerEditorActivationEvent event) {
			// do nothing
		}

		public void beforeEditorDeactivated(
				ColumnViewerEditorDeactivationEvent event) {
			// do nothing
		}
	}

	/**
	 * Maintains references to objects that only live for the length of the edit
	 * cycle.
	 */
	private static class EditingState {
		IObservableValue target;

		IObservableValue model;

		Binding binding;

		EditingState(Binding binding, IObservableValue target,
				IObservableValue model) {
			this.binding = binding;
			this.target = target;
			this.model = model;
		}

		void dispose() {
			target.dispose();
			model.dispose();
			binding.dispose();
		}
	}
}
