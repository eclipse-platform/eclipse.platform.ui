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
 *     Coconut Palm Software, Inc. - Initial API and implementation
 *     Matthew Hall - bugs 260329, 260337
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Demonstrates binding a TableViewer to a collection.
 */
public class Snippet017TableViewerWithDerivedColumns {
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

	private static Person UNKNOWN = new Person("unknown", null, null);

	/**
	 * The data model class.
	 * <p>
	 * This example implements full JavaBeans bound properties so that changes to
	 * instances of this class will automatically be propagated to the UI.
	 */
	static class Person extends AbstractModelObject {
		String name = "Donald Duck";
		Person mother;
		Person father;

		public Person(String name, Person mother, Person father) {
			this.name = name;
			this.mother = mother;
			this.father = father;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			String oldValue = this.name;
			this.name = name;
			firePropertyChange("name", oldValue, name);
		}

		public Person getMother() {
			return mother;
		}

		public void setMother(Person mother) {
			firePropertyChange("mother", this.mother, this.mother = mother);
		}

		public Person getFather() {
			return father;
		}

		public void setFather(Person father) {
			firePropertyChange("father", this.father, this.father = father);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * The View's model--the root of our Model graph for this particular GUI.
	 */
	static class ViewModel {
		// The model to bind
		private final IObservableList<Person> people = new WritableList<>();
		{
			Person fergus = new Person("Fergus McDuck", UNKNOWN, UNKNOWN);
			Person downy = new Person("Downy O'Drake", UNKNOWN, UNKNOWN);
			Person scrooge = new Person("Scrooge McDuck", downy, fergus);
			Person hortense = new Person("Hortense McDuck", downy, fergus);
			Person quackmore = new Person("Quackmore Duck", UNKNOWN, UNKNOWN);
			Person della = new Person("Della Duck", hortense, quackmore);
			Person donald = new Person("Donald Duck", hortense, quackmore);
			donald.setFather(quackmore);
			donald.setMother(hortense);
			della.setFather(quackmore);
			della.setMother(hortense);
			hortense.setMother(downy);
			hortense.setFather(fergus);
			scrooge.setMother(downy);
			scrooge.setFather(fergus);
			people.add(UNKNOWN);
			people.add(downy);
			people.add(fergus);
			people.add(scrooge);
			people.add(quackmore);
			people.add(hortense);
			people.add(della);
			people.add(donald);
		}

		public IObservableList<Person> getPeople() {
			return people;
		}
	}

	/** The GUI view. */
	static class View {
		private final ViewModel viewModel;
		private Table duckFamily;
		private Text nameText;
		private Combo motherCombo;
		private Combo fatherCombo;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			duckFamily = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
			duckFamily.setHeaderVisible(true);
			GridDataFactory.defaultsFor(duckFamily).span(2, 1).applyTo(duckFamily);
			createColumn("Name");
			createColumn("Mother");
			createColumn("Father");
			createColumn("Grandmother");
			duckFamily.setLinesVisible(true);

			new Label(shell, SWT.NONE).setText("Name:");
			nameText = new Text(shell, SWT.BORDER);
			GridDataFactory.defaultsFor(nameText).grab(true, false).applyTo(nameText);

			new Label(shell, SWT.NONE).setText("Mother:");
			motherCombo = new Combo(shell, SWT.READ_ONLY);

			new Label(shell, SWT.NONE).setText("Father:");
			fatherCombo = new Combo(shell, SWT.READ_ONLY);

			DataBindingContext bindingContext = new DataBindingContext();
			bindGUI(bindingContext);

			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(shell);
			shell.setSize(500, 300);
			shell.open();
			return shell;
		}

		private void createColumn(String string) {
			final TableColumn column = new TableColumn(duckFamily, SWT.NONE);
			column.setWidth(100);
			column.setText(string);
		}

		protected void bindGUI(DataBindingContext bindingContext) {
			TableViewer peopleViewer = new TableViewer(duckFamily);
			peopleViewer.addFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return element != UNKNOWN;
				}
			});

			// Bind viewers to model
			ViewerSupport.bind(peopleViewer, viewModel.getPeople(),
					BeanProperties.values("name", "mother.name", "father.name", "mother.mother.name"));

			// Bind viewer selection to detail fields
			IObservableValue<Person> selection = ViewerProperties.singleSelection(Person.class).observe(peopleViewer);
			bindingContext.bindValue(WidgetProperties.text().observe(nameText),
					BeanProperties.value(Person.class, "name", String.class).observeDetail(selection));

			ComboViewer mothercomboViewer = new ComboViewer(motherCombo);
			ViewerSupport.bind(mothercomboViewer, viewModel.getPeople(), BeanProperties.value("name"));
			bindingContext.bindValue(ViewerProperties.singleSelection(Person.class).observe(mothercomboViewer),
					BeanProperties.value(Person.class, "mother", Person.class).observeDetail(selection));

			ComboViewer fatherComboViewer = new ComboViewer(fatherCombo);
			ViewerSupport.bind(fatherComboViewer, viewModel.getPeople(), BeanProperties.value("name"));
			bindingContext.bindValue(ViewerProperties.singleSelection(Person.class).observe(fatherComboViewer),
					BeanProperties.value(Person.class, "father", Person.class).observeDetail(selection));
		}
	}

}
