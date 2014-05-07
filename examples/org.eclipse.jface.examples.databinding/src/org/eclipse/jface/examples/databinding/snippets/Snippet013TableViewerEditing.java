/*******************************************************************************
 * Copyright (c) 2006, 2009 The Pampered Chef, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 *     Tom Schindl - cell editing
 *     Matthew Hall - bugs 260329, 260337
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Demonstrates binding a TableViewer to a collection using the 3.3 Viewer APIs.
 */
public class Snippet013TableViewerEditing {
	public static void main(String[] args) {
		final Display display = new Display();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				ViewModel viewModel = new ViewModel();
				Shell shell = new View(viewModel).createShell();

				// The SWT event loop
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		});
	}

	// Minimal JavaBeans support
	public static abstract class AbstractModelObject {
		private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
				this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName,
					listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName,
					listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue,
					newValue);
		}
	}

	// The data model class. This is normally a persistent class of some sort.
	static class Person extends AbstractModelObject {
		// A property...
		String name = "John Smith";

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			String oldValue = this.name;
			this.name = name;
			firePropertyChange("name", oldValue, name);
		}
	}

	// The View's model--the root of our Model graph for this particular GUI.
	//
	// Typically each View class has a corresponding ViewModel class.
	// The ViewModel is responsible for getting the objects to edit from the
	// data access tier. Since this snippet doesn't have any persistent objects
	// ro retrieve, this ViewModel just instantiates a model object to edit.
	static class ViewModel {
		// The model to bind
		private List people = new LinkedList();
		{
			people.add(new Person("Steve Northover"));
			people.add(new Person("Grant Gayed"));
			people.add(new Person("Veronika Irvine"));
			people.add(new Person("Mike Wilson"));
			people.add(new Person("Christophe Cornu"));
			people.add(new Person("Lynne Kues"));
			people.add(new Person("Silenio Quarti"));
		}

		public List getPeople() {
			return people;
		}
	}

	/**
	 * Editing support that uses JFace Data Binding to control the editing
	 * lifecycle. The standard EditingSupport get/setValue(...) lifecycle is not
	 * used.
	 * 
	 * @since 3.3
	 */
	private static class InlineEditingSupport extends
			ObservableValueEditingSupport {
		private CellEditor cellEditor;

		/**
		 * @param viewer
		 * @param dbc
		 */
		public InlineEditingSupport(ColumnViewer viewer, DataBindingContext dbc) {

			super(viewer, dbc);
			cellEditor = new TextCellEditor((Composite) viewer.getControl());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected IObservableValue doCreateCellEditorObservable(
				CellEditor cellEditor) {

			return SWTObservables.observeText(cellEditor.getControl(),
					SWT.Modify);
		}

		@Override
		protected IObservableValue doCreateElementObservable(Object element,
				ViewerCell cell) {
			return BeansObservables.observeValue(element, "name");
		}
	}

	// The GUI view
	static class View {
		private ViewModel viewModel;
		private Table committers;
		private Label selectedCommitter;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			shell.setLayout(new FillLayout(SWT.VERTICAL));
			committers = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
			committers.setLinesVisible(true);

			selectedCommitter = new Label(shell, SWT.NONE);
			// Set up data binding. In an RCP application, the threading
			// Realm
			// will be set for you automatically by the Workbench. In an SWT
			// application, you can do this once, wrpping your binding
			// method call.
			DataBindingContext bindingContext = new DataBindingContext();
			bindGUI(bindingContext);

			// Open and return the Shell
			shell.setSize(100, 300);
			shell.open();
			return shell;
		}

		protected void bindGUI(DataBindingContext bindingContext) {
			// Since we're using a JFace Viewer, we do first wrap our Table...
			TableViewer peopleViewer = new TableViewer(committers);
			TableViewerColumn column = new TableViewerColumn(peopleViewer,
					SWT.NONE);
			column.setEditingSupport(new InlineEditingSupport(peopleViewer,
					bindingContext));
			column.getColumn().setWidth(100);

			// Bind viewer to model
			ViewerSupport.bind(peopleViewer, new WritableList(viewModel
					.getPeople(), Person.class), BeanProperties.value(
					Person.class, "name"));

			// bind selectedCommitter label to the name of the current selection
			IObservableValue selection = ViewersObservables
					.observeSingleSelection(peopleViewer);
			bindingContext.bindValue(SWTObservables
					.observeText(selectedCommitter), BeansObservables
					.observeDetailValue(selection, "name", String.class));
		}
	}

}
