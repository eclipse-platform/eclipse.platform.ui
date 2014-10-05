/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 146397, 260337
 *******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ObservableValueEditingSupportTest extends AbstractSWTTestCase {
	private Shell shell;

	private ObservableValueEditingSupportStub editingSupport;

	private DataBindingContext dbc;

	private TableViewer viewer;

	private Bean bean;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.databinding.AbstractSWTTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		shell = getShell();
		dbc = new DataBindingContext();

		viewer = new TableViewer(shell);
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);

		editingSupport = new ObservableValueEditingSupportStub(viewer, dbc);
		column.setEditingSupport(editingSupport);

		WritableList input = WritableList.withElementType(String.class);
		bean = new Bean();
		bean.setValue("value");
		input.add(bean);

		// Bind viewer to input
		ViewerSupport.bind(viewer, input, BeanProperties.value(Bean.class,
				"value"));
	}

	public void testInitializeCellEditorValue_OrderOfOperations()
			throws Exception {
		assertEquals("precondition", 0, editingSupport.events.length());

		viewer.editElement(bean, 0);
		assertEquals(
				"createCellEditorObservable createElementObservable createBinding",
				editingSupport.events.toString());
	}

	public void testSaveCellEditorValue_UpdatesModel() throws Exception {
		shell.open();

		String newValue = bean.getValue() + "a";

		viewer.editElement(bean, 0);
		editingSupport.target.setValue(newValue);

		// force the focus to leave the editor updating the value
		closeCellEditor();

		assertTrue(editingSupport.binding.isDisposed());
		assertEquals(newValue, bean.getValue());
	}

	/**
	 * 
	 */
	protected void closeCellEditor() {
		editingSupport.text.notifyListeners(SWT.DefaultSelection, new Event());
	}

	public void testSaveCellEditorValue_IgnoreIfNotDirty() throws Exception {
		String initialValue = bean.getValue();

		shell.open();

		viewer.editElement(bean, 0);

		// force the focus to leave the editor updating the value
		closeCellEditor();

		assertTrue(editingSupport.binding.isDisposed());
		assertEquals(initialValue, bean.getValue());
	}

	public void testDisposesBinding() throws Exception {
		shell.open();

		viewer.editElement(bean, 0);
		assertFalse("precondition", editingSupport.binding.isDisposed());

		closeCellEditor();
		assertTrue(editingSupport.binding.isDisposed());
	}

	public void testDisposesTargetObservable() throws Exception {
		shell.open();

		viewer.editElement(bean, 0);
		assertFalse("precondition", editingSupport.target.isDisposed());

		closeCellEditor();
		assertTrue(editingSupport.target.isDisposed());
	}

	public void testDisposesModelObservable() throws Exception {
		shell.open();

		viewer.editElement(bean, 0);
		assertFalse("precondition", editingSupport.model.isDisposed());

		closeCellEditor();
		assertTrue(editingSupport.model.isDisposed());
	}

	public void testCanEdit_DefaultIsTrue() throws Exception {
		assertTrue(editingSupport.canEdit(bean));
	}

	private static class ObservableValueEditingSupportStub extends
			ObservableValueEditingSupport {
		StringBuffer events = new StringBuffer();

		Text text;

		TextCellEditor editor;

		Binding binding;

		IObservableValue target;

		IObservableValue model;

		/**
		 * @param viewer
		 * @param dbc
		 */
		public ObservableValueEditingSupportStub(ColumnViewer viewer,
				DataBindingContext dbc) {
			super(viewer, dbc);
			editor = new TextCellEditor((Composite) viewer.getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return super.canEdit(element);
		}

		private void event(String event) {
			if (events.length() > 0) {
				events.append(" ");
			}

			events.append(event);
		}

		@Override
		protected IObservableValue doCreateCellEditorObservable(
				CellEditor cellEditor) {
			event("createCellEditorObservable");

			text = (Text) cellEditor.getControl();
			return target = SWTObservables.observeText(cellEditor.getControl(),
					SWT.NONE);
		}

		@Override
		protected IObservableValue doCreateElementObservable(Object element,
				ViewerCell cell) {
			event("createElementObservable");
			return model = BeansObservables.observeValue(element, "value");
		}

		@Override
		protected Binding createBinding(IObservableValue target,
				IObservableValue model) {
			event("createBinding");

			return binding = super.createBinding(target, model);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}
	}
}
