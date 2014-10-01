/*******************************************************************************
 * Copyright (c) 2008, 2014 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IInputValidator;
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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet 018: Binding to the checked elements in a CheckboxTableViewer.
 */
public class Snippet018CheckboxTableViewerCheckedSelection {
	public static void main(String[] args) {
		// The SWT event loop
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				ViewModel viewModel = createSampleModel();

				Shell shell = new View(viewModel).createShell();
				shell.open();
				while (!shell.isDisposed())
					if (!display.readAndDispatch())
						display.sleep();
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

		setFriends(stan, new Person[] { kyle, eric, kenny, wendy });
		setFriends(kyle, new Person[] { stan, eric, kenny });
		setFriends(eric, new Person[] { eric });
		setFriends(kenny, new Person[] { stan, kyle, eric });
		setFriends(wendy, new Person[] { stan });
		setFriends(butters, new Person[0]);

		Person[] people = new Person[] { stan, kyle, eric, kenny, wendy,
				butters };
		viewModel.setPeople(Arrays.asList(people));
		return viewModel;
	}

	private static Person createPerson(String name) {
		Person person = new Person();
		person.setName(name);
		return person;
	}

	private static void setFriends(Person person, Person[] friends) {
		person.setFriends(new HashSet(Arrays.asList(friends)));
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

	// The data model class.
	static class Person extends AbstractModelObject {
		private String name;
		private Set friends = new HashSet();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			firePropertyChange("name", this.name, this.name = name);
		}

		public Set getFriends() {
			return new HashSet(friends);
		}

		public void setFriends(Set friends) {
			firePropertyChange("friends", this.friends,
					this.friends = new HashSet(friends));
		}

		@Override
		public String toString() {
			return name;
		}
	}

	// The View's model--the root of our Model graph for this particular GUI.
	//
	// Typically each View class has a corresponding ViewModel class.
	//
	// The ViewModel is responsible for getting the objects to edit from the
	// data access tier. Since this snippet doesn't have any persistent objects
	// to retrieve, this ViewModel just instantiates a model object to edit.
	static class ViewModel extends AbstractModelObject {
		private List people = new ArrayList();

		public List getPeople() {
			return new ArrayList(people);
		}

		public void setPeople(List people) {
			firePropertyChange("people", this.people,
					this.people = new ArrayList(people));
		}
	}

	// The GUI view
	static class View {
		private ViewModel viewModel;

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
			final Display display = Display.getCurrent();
			shell = new Shell(display);

			createUI(shell);

			// Bind UI
			bindUI();

			// Open and return the Shell
			shell.setSize(shell.computeSize(400, SWT.DEFAULT));
			shell.open();
			return shell;
		}

		private void createUI(Shell shell) {
			shell.setText("Binding checked elements in CheckboxTableViewer");
			shell.setLayout(new GridLayout(2, false));

			new Label(shell, SWT.NONE).setText("People");

			Composite buttons = new Composite(shell, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(
					buttons);
			GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true)
					.applyTo(buttons);
			addPersonButton = new Button(buttons, SWT.PUSH);
			addPersonButton.setText("Add");
			GridDataFactory.fillDefaults().applyTo(addPersonButton);
			removePersonButton = new Button(buttons, SWT.PUSH);
			removePersonButton.setText("Remove");
			GridDataFactory.fillDefaults().applyTo(removePersonButton);

			Composite peopleComposite = new Composite(shell, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(
					peopleComposite);
			TableColumnLayout peopleColumnLayout = new TableColumnLayout();
			peopleComposite.setLayout(peopleColumnLayout);

			peopleViewer = new TableViewer(peopleComposite, SWT.SINGLE
					| SWT.BORDER | SWT.FULL_SELECTION);

			Table peopleTable = peopleViewer.getTable();
			peopleTable.setHeaderVisible(true);
			peopleTable.setLinesVisible(true);

			TableColumn nameColumn = new TableColumn(peopleTable, SWT.NONE);
			nameColumn.setText("Name");
			peopleColumnLayout.setColumnData(nameColumn,
					new ColumnWeightData(1));

			TableColumn friendsColumn = new TableColumn(peopleTable, SWT.NONE);
			friendsColumn.setText("Friends");
			peopleColumnLayout.setColumnData(friendsColumn,
					new ColumnWeightData(3));

			new Label(shell, SWT.NONE).setText("Name");

			personName = new Text(shell, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false)
					.applyTo(personName);

			new Label(shell, SWT.NONE).setText("Friends");

			Composite friendsComposite = new Composite(shell, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(
					friendsComposite);
			TableColumnLayout friendsColumnLayout = new TableColumnLayout();
			friendsComposite.setLayout(friendsColumnLayout);

			friendsViewer = CheckboxTableViewer.newCheckList(friendsComposite,
					SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);

			Table friendsTable = friendsViewer.getTable();
			friendsTable.setHeaderVisible(true);
			friendsTable.setLinesVisible(true);
			TableColumn friendNameColumn = new TableColumn(friendsTable,
					SWT.NONE);
			friendNameColumn.setText("Name");
			friendsColumnLayout.setColumnData(friendNameColumn,
					new ColumnWeightData(1));

			GridDataFactory.fillDefaults().grab(true, true).applyTo(
					friendsViewer.getTable());
		}

		private void bindUI() {
			DataBindingContext dbc = new DataBindingContext();

			final IObservableList people = BeanProperties.list(viewModel.getClass(), "people").observe(viewModel);

			addPersonButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					InputDialog dlg = new InputDialog(shell, "Add Person",
							"Enter name:", "<Name>", new IInputValidator() {
								@Override
								public String isValid(String newText) {
									if (newText == null
											|| newText.length() == 0)
										return "Name cannot be empty";
									return null;
								}
							});
					if (dlg.open() == Window.OK) {
						Person person = new Person();
						person.setName(dlg.getValue());
						people.add(person);
						peopleViewer.setSelection(new StructuredSelection(
								person));
					}
				}
			});

			removePersonButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					IStructuredSelection selected = peopleViewer.getStructuredSelection();
					if (selected.isEmpty())
						return;
					Person person = (Person) selected.getFirstElement();
					if (MessageDialog.openConfirm(shell, "Remove person",
							"Remove " + person.getName() + "?"))
						people.remove(person);
				}
			});

			ViewerSupport.bind(peopleViewer, people, BeanProperties.values(
					Person.class, new String[] { "name", "friends" }));

			final IObservableValue selectedPerson = ViewersObservables
					.observeSingleSelection(peopleViewer);

			IObservableValue personSelected = new ComputedValue(Boolean.TYPE) {
				@Override
				protected Object calculate() {
					return Boolean.valueOf(selectedPerson.getValue() != null);
				}
			};
			dbc.bindValue(WidgetProperties.enabled().observe(removePersonButton),
					personSelected);
			dbc.bindValue(WidgetProperties.enabled().observe(friendsViewer
					.getTable()), personSelected);

			dbc.bindValue(
					WidgetProperties.text(SWT.Modify).observe(personName),
					BeanProperties.value((Class) selectedPerson.getValueType(), "name", String.class)
					.observeDetail(selectedPerson));

			ViewerSupport.bind(friendsViewer, people, BeanProperties.value(
					Person.class, "name"));

			dbc.bindSet(ViewersObservables.observeCheckedElements(
					friendsViewer, Person.class),BeanProperties.set((Class) selectedPerson.getValueType(), "friends", Person.class)
					.observeDetail(selectedPerson));
		}
	}
}