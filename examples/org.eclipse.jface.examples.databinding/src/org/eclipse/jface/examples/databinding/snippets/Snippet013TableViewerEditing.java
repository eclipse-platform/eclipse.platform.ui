/*******************************************************************************
 * Copyright (c) 2006, 2018 The Pampered Chef, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 *     Tom Schindl - cell editing
 *     Matthew Hall - bugs 260329, 260337
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 *     Patrik Suzzi - 479848
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
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
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new View(new ViewModel()).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});
	}

	/** Helper class for implementing JavaBeans support. */
	public static abstract class AbstractModelObject {
		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	/**
	 * The data model class.
	 * <p>
	 * This example implements full JavaBeans bound properties so that changes to
	 * instances of this class will automatically be propagated to the UI.
	 */
	static class Person extends AbstractModelObject {
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

	/**
	 * The View's model--the root of our Model graph for this particular GUI.
	 */
	static class ViewModel {
		// The model to bind
		private final List<Person> people = new LinkedList<>();
		{
			people.add(new Person("Steve Northover"));
			people.add(new Person("Grant Gayed"));
			people.add(new Person("Veronika Irvine"));
			people.add(new Person("Mike Wilson"));
			people.add(new Person("Christophe Cornu"));
			people.add(new Person("Lynne Kues"));
			people.add(new Person("Silenio Quarti"));
			people.add(new Person("Boris Bokowski"));
			people.add(new Person("Matthew Hall"));
			people.add(new Person("Thomas Schindl"));
			people.add(new Person("Lars Vogel"));
			people.add(new Person("Simon Scholz"));
			people.add(new Person("Stefan Xenos"));
			people.add(new Person("Jens Lideström"));
		}

		public List<Person> getPeople() {
			return people;
		}
	}

	/**
	 * Editing support that uses JFace Data Binding to control the editing
	 * lifecycle. The standard EditingSupport get/setValue(...) lifecycle is not
	 * used.
	 */
	private static class InlineEditingSupport extends ObservableValueEditingSupport<Person, String, String> {

		private final CellEditor cellEditor;

		public InlineEditingSupport(ColumnViewer viewer, DataBindingContext bindingContext) {
			super(viewer, bindingContext);
			cellEditor = new TextCellEditor((Composite) viewer.getControl());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected IObservableValue<String> doCreateCellEditorObservable(CellEditor cellEditor) {
			return WidgetProperties.text(SWT.Modify).observe(cellEditor.getControl());
		}

		@Override
		protected IObservableValue<String> doCreateElementObservable(Person element, ViewerCell cell) {
			return BeanProperties.value(Person.class, "name", String.class).observe(element);
		}
	}

	/** The GUI view. */
	static class View {
		private final ViewModel viewModel;
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
			// Set up data binding. In an RCP application, the threading Realm will be set
			// for you automatically by the Workbench. In an SWT application, you can do
			// this once, wrapping your binding method call.
			DataBindingContext bindingContext = new DataBindingContext();
			bindGUI(bindingContext);

			shell.setSize(400, 600);
			shell.open();
			return shell;
		}

		protected void bindGUI(DataBindingContext bindingContext) {
			TableViewer peopleViewer = new TableViewer(committers);
			TableViewerColumn column = new TableViewerColumn(peopleViewer, SWT.NONE);
			column.setEditingSupport(new InlineEditingSupport(peopleViewer, bindingContext));
			column.getColumn().setWidth(100);

			// Bind viewer to model
			ViewerSupport.bind(peopleViewer, new WritableList<>(viewModel.getPeople(), Person.class),
					BeanProperties.value(Person.class, "name"));

			// Bind selectedCommitter label to the name of the current selection
			IObservableValue<Person> selection = ViewerProperties.singleSelection(Person.class).observe(peopleViewer);
			bindingContext.bindValue(WidgetProperties.text().observe(selectedCommitter),
					BeanProperties.value(Person.class, "name", String.class).observeDetail(selection));
		}
	}

}
