/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
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
		Text text = new Text(shell, SWT.NONE);
		shell.open();

		String newValue = bean.getValue() + "a";

		viewer.editElement(bean, 0);
		editingSupport.editor.setValue(newValue);

		// force the focus to leave the editor updating the value
		text.setFocus();

		assertEquals(newValue, bean.getValue());
	}

	public void testDisposesBinding() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		shell.open();

		viewer.editElement(bean, 0);
		assertFalse("precondition", editingSupport.binding.isDisposed());

		text.setFocus();
		assertTrue(editingSupport.binding.isDisposed());
	}

	public void testDisposesTargetObservable() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		shell.open();

		viewer.editElement(bean, 0);
		assertEquals("precondition", 0, editingSupport.target.disposed);

		text.setFocus();
		assertEquals(1, editingSupport.target.disposed);
	}

	public void testDisposesModelObservable() throws Exception {
		Text text = new Text(shell, SWT.NONE);
		shell.open();

		viewer.editElement(bean, 0);
		assertEquals("precondition", 0, editingSupport.model.disposed);

		text.setFocus();
		assertEquals(1, editingSupport.model.disposed);
	}

	public void testCanEdit_DefaultIsTrue() throws Exception {
		assertTrue(editingSupport.canEdit(bean));
	}

	private static class ObservableValueEditingSupportStub extends
			ObservableValueEditingSupport {
		StringBuffer events = new StringBuffer();

		TextCellEditor editor;

		Binding binding;

		ObservableValueDecorator target;

		ObservableValueDecorator model;

		/**
		 * @param viewer
		 * @param dbc
		 */
		public ObservableValueEditingSupportStub(ColumnViewer viewer,
				DataBindingContext dbc) {
			super(viewer, dbc);
			editor = new TextCellEditor((Composite) viewer.getControl());
		}

		protected boolean canEdit(Object element) {
			return super.canEdit(element);
		}

		public CellEditor getCellEditor() {
			return editor;
		}

		private void event(String event) {
			if (events.length() > 0) {
				events.append(" ");
			}

			events.append(event);
		}

		protected IObservableValue doCreateCellEditorObservable(
				CellEditor cellEditor) {
			event("createCellEditorObservable");

			return target = new ObservableValueDecorator(SWTObservables
					.observeText(cellEditor.getControl(), SWT.NONE));
		}

		protected IObservableValue doCreateElementObservable(Object element,
				ViewerCell cell) {
			event("createElementObservable");
			return model = new ObservableValueDecorator(BeansObservables
					.observeValue(element, "value"));
		}

		protected Binding createBinding(IObservableValue target,
				IObservableValue model) {
			event("createBinding");

			return binding = super.createBinding(target, model);
		}

		protected CellEditor getCellEditor(Object element) {
			return editor;
		}
	}

	/**
	 * Decorator that will allow for tracking calls to dispose(). We really need
	 * an isDisposed() method on IObservable...
	 */
	private static class ObservableValueDecorator implements IObservableValue {
		int disposed;

		IObservableValue delegate;

		ObservableValueDecorator(IObservableValue delegate) {
			this.delegate = delegate;
		}

		public boolean isDisposed() {
			return disposed > 0;
		}

		public synchronized void dispose() {
			disposed++;
			delegate.dispose();
		}

		public void addChangeListener(IChangeListener listener) {
			delegate.addChangeListener(listener);
		}

		public void addDisposeListener(IDisposeListener listener) {
			delegate.addDisposeListener(listener);
		}

		public void addStaleListener(IStaleListener listener) {
			delegate.addStaleListener(listener);
		}

		public void addValueChangeListener(IValueChangeListener listener) {
			delegate.addValueChangeListener(listener);
		}

		public Realm getRealm() {
			return delegate.getRealm();
		}

		public Object getValue() {
			return delegate.getValue();
		}

		public Object getValueType() {
			return delegate.getValueType();
		}

		public boolean isStale() {
			return delegate.isStale();
		}

		public void removeChangeListener(IChangeListener listener) {
			delegate.removeChangeListener(listener);
		}

		public void removeDisposeListener(IDisposeListener listener) {
			delegate.removeDisposeListener(listener);
		}

		public void removeStaleListener(IStaleListener listener) {
			delegate.removeStaleListener(listener);
		}

		public void removeValueChangeListener(IValueChangeListener listener) {
			delegate.removeValueChangeListener(listener);
		}

		public void setValue(Object value) {
			delegate.setValue(value);
		}
	}
}
