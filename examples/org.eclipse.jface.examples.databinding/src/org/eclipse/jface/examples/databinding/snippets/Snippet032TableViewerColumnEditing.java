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
 *     Heiko Ahlig - bug 267712
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283, 297495
 *******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.CellEditorProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Demonstrates binding a TableViewer with multiple columns to a collection.
 */
public class Snippet032TableViewerColumnEditing {

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

		display.dispose();
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
		String name;
		String firstName;

		public Person(String firstName, String name) {
			this.name = name;
			this.firstName = firstName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = name);
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			firePropertyChange("firstName", this.firstName, this.firstName = firstName);
		}
	}

	/**
	 * The View's model--the root of our Model graph for this particular GUI.
	 */
	static class ViewModel {
		// The model to bind
		private final List<Person> people = new LinkedList<>();
		{
			people.add(new Person("Dave", "Orme"));
			people.add(new Person("Gili", "Mendel"));
			people.add(new Person("Joe", "Winchester"));
			people.add(new Person("Boris", "Bokowski"));
			people.add(new Person("Brad", "Reynolds"));
			people.add(new Person("Thomas", "Schindl"));
			people.add(new Person("Lars", "Vogel"));
			people.add(new Person("Simon", "Scholz"));
			people.add(new Person("Stefan", "Xenos"));
		}

		public List<Person> getPeople() {
			return people;
		}
	}

	/** The GUI view. */
	static class View {
		private final ViewModel viewModel;
		private Table committers;
		private Label selectedCommitterName;
		private Label selectedCommitterFirstName;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			shell.setLayout(new GridLayout(2, true));
			committers = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
			committers.setLinesVisible(true);
			committers.setHeaderVisible(true);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.horizontalSpan = 2;
			committers.setLayoutData(layoutData);

			GridData fieldLayoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			selectedCommitterName = new Label(shell, SWT.NONE);
			selectedCommitterName.setLayoutData(fieldLayoutData);

			selectedCommitterFirstName = new Label(shell, SWT.NONE);
			selectedCommitterFirstName.setLayoutData(fieldLayoutData);

			DataBindingContext bindingContext = new DataBindingContext();
			bindGUI(bindingContext);

			shell.setSize(250, 300);
			shell.open();
			return shell;
		}

		protected void bindGUI(DataBindingContext bindingContext) {
			TableViewer peopleViewer = new TableViewer(committers);

			TableViewerColumn columnName = new TableViewerColumn(peopleViewer, SWT.NONE);
			columnName.getColumn().setText("Name");
			columnName.getColumn().setWidth(100);

			TableViewerColumn columnFirstName = new TableViewerColumn(peopleViewer, SWT.NONE);
			columnFirstName.getColumn().setText("FirstName");
			columnFirstName.getColumn().setWidth(100);

			// Bind viewer to model
			IBeanValueProperty<Person, String> propName = BeanProperties.value(Person.class, "name", String.class);
			IBeanValueProperty<Person, String> propFirstname = BeanProperties.value(Person.class, "firstName",
					String.class);

			IValueProperty<CellEditor, String> cellEditorControlText = CellEditorProperties.control()
					.value(WidgetProperties.text(SWT.Modify));

			columnName.setEditingSupport(ObservableValueEditingSupport.create(peopleViewer, bindingContext,
					new TextCellEditor(committers), cellEditorControlText, propName));

			columnFirstName.setEditingSupport(ObservableValueEditingSupport.create(peopleViewer, bindingContext,
					new TextCellEditor(committers), cellEditorControlText, propFirstname));

			ObservableListContentProvider<Person> contentProvider = new ObservableListContentProvider<>();
			peopleViewer.setContentProvider(contentProvider);

			// Bind the LabelProviders to the model and columns
			IObservableMap<Person, ? extends String>[] result = Properties
					.observeEach(contentProvider.getKnownElements(), propName, propFirstname);

			columnName.setLabelProvider(new ObservableMapCellLabelProvider(result[0]));
			columnFirstName.setLabelProvider(new ObservableMapCellLabelProvider(result[1]));

			peopleViewer.setInput(new WritableList<>(viewModel.getPeople(), Person.class));

			// Bind selectedCommitter labels to the name and firstname of the current
			// selection
			IObservableValue<Person> selection = ViewerProperties.singleSelection(Person.class).observe(peopleViewer);
			bindingContext.bindValue(WidgetProperties.text().observe(selectedCommitterName),
					BeanProperties.value(Person.class, "name", String.class).observeDetail(selection));
			bindingContext.bindValue(WidgetProperties.text().observe(selectedCommitterFirstName),
					BeanProperties.value(Person.class, "firstName", String.class).observeDetail(selection));
		}
	}
}
