/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     IBM Corporation - see bug 137934
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 434283
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.examples.databinding.snippets.Snippet007ColorLabelProvider.Person.Gender;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * An example showing how to create a {@link ILabelProvider} that to provide
 * colors.
 */
public class Snippet007ColorLabelProvider {
	public static void main(String[] args) {
		final Display display = new Display();

		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			Shell shell = createShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		});

		display.dispose();
	}

	private static Shell createShell() {
		List<Person> persons = new ArrayList<>();
		persons.add(new Person("Fiona Apple", Gender.FEMALE));
		persons.add(new Person("Elliot Smith", Gender.MALE));
		persons.add(new Person("Diana Krall", Gender.FEMALE));
		persons.add(new Person("David Gilmour", Gender.MALE));

		Shell shell = new Shell();
		shell.setText("Gender Bender");
		shell.setLayout(new GridLayout());

		Table table = new Table(shell, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("No");
		column.setWidth(20);
		column = new TableColumn(table, SWT.NONE);
		column.setText("Name");
		column.setWidth(100);
		final TableViewer viewer = new TableViewer(table);

		IObservableList<Person> observableList = Observables.staticObservableList(persons);
		ObservableListContentProvider<Person> contentProvider = new ObservableListContentProvider<>();

		viewer.setContentProvider(contentProvider);

		// This does not have to correspond to the columns in the table,
		// we just list all attributes that affect the table content
		@SuppressWarnings("unchecked")
		IObservableMap<Person, ?>[] attributes = new IObservableMap[] {
				BeanProperties.value(Person.class, "name").observeDetail(contentProvider.getKnownElements()),
				BeanProperties.value(Person.class, "gender").observeDetail(contentProvider.getKnownElements()) };

		viewer.setLabelProvider(new ColorLabelProvider(attributes, shell.getDisplay(), persons));

		viewer.setInput(observableList);

		table.getColumn(0).pack();

		Button button = new Button(shell, SWT.PUSH);
		button.setText("Toggle Gender");

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()) {
					Person person = (Person) selection.getFirstElement();
					person.setGender(person.getGender() == Gender.MALE ? Gender.FEMALE : Gender.MALE);
				}
			}
		});

		shell.setSize(300, 400);
		shell.open();
		return shell;
	}

	static class Person {
		enum Gender {
			MALE, FEMALE
		}

		private String name;
		private Gender gender;

		private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

		Person(String name, Gender gender) {
			this.name = name;
			this.gender = gender;
		}

		public String getName() {
			return name;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}

		public Gender getGender() {
			return gender;
		}

		void setGender(Gender gender) {
			changeSupport.firePropertyChange("gender", this.gender, this.gender = gender);
		}
	}

	static class ColorLabelProvider extends ObservableMapLabelProvider implements ITableColorProvider {
		private Color maleColor;
		private Color femaleColor;
		private List<Person> persons;

		ColorLabelProvider(IObservableMap<?, ?>[] attributes, Display display, List<Person> persons) {
			super(attributes);
			this.persons = persons;
			this.maleColor = new Color(display, 255, 192, 203);
			this.femaleColor = display.getSystemColor(SWT.COLOR_BLUE);
		}

		// To drive home the point that attributes does not have to match the columns in
		// the table, we change the column text as follows:
		@Override
		public String getColumnText(Object element, int index) {
			if (index == 0) {
				return Integer.toString(persons.indexOf(element) + 1);
			}
			return ((Person) element).getName();
		}

		@Override
		public Color getBackground(Object element, int index) {
			return null;
		}

		@Override
		public Color getForeground(Object element, int index) {
			if (index == 0) {
				return null;
			}
			return ((Person) element).getGender() == Gender.MALE ? maleColor : femaleColor;
		}

		@Override
		public void dispose() {
			super.dispose();
			femaleColor.dispose();
		}
	}
}
