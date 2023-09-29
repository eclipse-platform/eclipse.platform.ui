/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 *     Matthew Hall - bugs 260329, 260337
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442278, 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet 018: Binding to the checked elements in a CheckboxTableViewer.
 */
public class Snippet018CheckboxTableViewerCheckedSelection {
	public static void main(String[] args) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = new View(createSampleModel()).createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	private static ViewModel createSampleModel() {
		ViewModel viewModel = new ViewModel();

		Person stan = createPerson("Stan");
		Person kyle = createPerson("Kyle");
		Person eric = createPerson("Eric");
		Person kenny = createPerson("Kenny");
		Person wendy = createPerson("Wendy");
		Person butters = createPerson("Butters");

		stan.setFriends(kyle, eric, kenny, wendy);
		kyle.setFriends(stan, eric, kenny);
		eric.setFriends(eric);
		kenny.setFriends(stan, kyle, eric);
		wendy.setFriends(stan);
		butters.setFriends();

		viewModel.setPeople(Arrays.asList(stan, kyle, eric, kenny, wendy, butters));
		return viewModel;
	}

	private static Person createPerson(String name) {
		Person person = new Person();
		person.setName(name);
		return person;
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
		private String name;
		private Set<Person> friends = new HashSet<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = name);
		}

		public Set<Person> getFriends() {
			return new HashSet<>(friends);
		}

		public void setFriends(Person... friends) {
			setFriends(new HashSet<>(Arrays.asList(friends)));
		}

		public void setFriends(Set<Person> friends) {
			firePropertyChange("friends", this.friends, this.friends = new HashSet<>(friends));
		}

		@Override
		public String toString() {
			return name;
		}

	}

	/**
	 * The View's model--the root of our Model graph for this particular GUI.
	 */
	static class ViewModel extends AbstractModelObject {
		private List<Person> people = new ArrayList<>();

		public List<Person> getPeople() {
			return new ArrayList<>(people);
		}

		public void setPeople(List<Person> people) {
			firePropertyChange("people", this.people, this.people = new ArrayList<>(people));
		}
	}

	/** The GUI view. */
	static class View {
		private final ViewModel viewModel;

		private Shell shell;

		private Button addPersonButton;
		private Button removePersonButton;
		private TableViewer peopleViewer;
		private Text personName;
		private CheckboxTableViewer friendsViewer;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			shell = new Shell();

			createUI(shell);

			// Bind UI
			bindUI();

			shell.setSize(shell.computeSize(400, SWT.DEFAULT));
			shell.open();

			return shell;
		}

		private void createUI(Shell shell) {
			shell.setText("Binding checked elements in CheckboxTableViewer");
			shell.setLayout(new GridLayout(2, false));

			new Label(shell, SWT.NONE).setText("People");

			Composite buttons = new Composite(shell, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(buttons);
			GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(buttons);
			addPersonButton = new Button(buttons, SWT.PUSH);
			addPersonButton.setText("Add");
			GridDataFactory.fillDefaults().applyTo(addPersonButton);
			removePersonButton = new Button(buttons, SWT.PUSH);
			removePersonButton.setText("Remove");
			GridDataFactory.fillDefaults().applyTo(removePersonButton);

			Composite peopleComposite = new Composite(shell, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(peopleComposite);
			TableColumnLayout peopleColumnLayout = new TableColumnLayout();
			peopleComposite.setLayout(peopleColumnLayout);

			peopleViewer = new TableViewer(peopleComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);

			Table peopleTable = peopleViewer.getTable();
			peopleTable.setHeaderVisible(true);
			peopleTable.setLinesVisible(true);

			TableColumn nameColumn = new TableColumn(peopleTable, SWT.NONE);
			nameColumn.setText("Name");
			peopleColumnLayout.setColumnData(nameColumn, new ColumnWeightData(1));

			TableColumn friendsColumn = new TableColumn(peopleTable, SWT.NONE);
			friendsColumn.setText("Friends");
			peopleColumnLayout.setColumnData(friendsColumn, new ColumnWeightData(3));

			new Label(shell, SWT.NONE).setText("Name");

			personName = new Text(shell, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(personName);

			new Label(shell, SWT.NONE).setText("Friends");

			Composite friendsComposite = new Composite(shell, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(friendsComposite);
			TableColumnLayout friendsColumnLayout = new TableColumnLayout();
			friendsComposite.setLayout(friendsColumnLayout);

			friendsViewer = CheckboxTableViewer.newCheckList(friendsComposite,
					SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);

			Table friendsTable = friendsViewer.getTable();
			friendsTable.setHeaderVisible(true);
			friendsTable.setLinesVisible(true);
			TableColumn friendNameColumn = new TableColumn(friendsTable, SWT.NONE);
			friendNameColumn.setText("Name");
			friendsColumnLayout.setColumnData(friendNameColumn, new ColumnWeightData(1));

			GridDataFactory.fillDefaults().grab(true, true).applyTo(friendsViewer.getTable());
		}

		private void bindUI() {
			DataBindingContext bindingContext = new DataBindingContext();

			final IObservableList<Person> people = BeanProperties.list(ViewModel.class, "people", Person.class)
					.observe(viewModel);

			addPersonButton.addListener(SWT.Selection, event -> {
				InputDialog dlg = new InputDialog(shell, "Add Person", "Enter name:", "<Name>", newText -> {
					if (newText == null || newText.length() == 0) {
						return "Name cannot be empty";
					}
					return null;
				});
				if (dlg.open() == Window.OK) {
					Person person = new Person();
					person.setName(dlg.getValue());
					people.add(person);
					peopleViewer.setSelection(new StructuredSelection(person));
				}
			});

			removePersonButton.addListener(SWT.Selection, event -> {
				IStructuredSelection selected = peopleViewer.getStructuredSelection();
				if (selected.isEmpty()) {
					return;
				}
				Person person = (Person) selected.getFirstElement();
				if (MessageDialog.openConfirm(shell, "Remove person", "Remove " + person.getName() + "?")) {
					people.remove(person);
				}
			});

			ViewerSupport.bind(peopleViewer, people, BeanProperties.values(Person.class, "name", "friends"));

			final IObservableValue<Person> selectedPerson = ViewerProperties.singleSelection(Person.class)
					.observe(peopleViewer);

			IObservableValue<Boolean> personSelected = ComputedValue.create(() -> selectedPerson.getValue() != null);
			bindingContext.bindValue(WidgetProperties.enabled().observe(removePersonButton), personSelected);
			bindingContext.bindValue(WidgetProperties.enabled().observe(friendsViewer.getTable()), personSelected);

			bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(personName),
					BeanProperties.value(Person.class, "name", String.class).observeDetail(selectedPerson));

			ViewerSupport.bind(friendsViewer, people, BeanProperties.value(Person.class, "name"));

			bindingContext.bindSet(ViewerProperties.checkedElements(Person.class).observe((Viewer) friendsViewer),
					BeanProperties.set(Person.class, "friends", Person.class).observeDetail(selectedPerson));
		}
	}
}