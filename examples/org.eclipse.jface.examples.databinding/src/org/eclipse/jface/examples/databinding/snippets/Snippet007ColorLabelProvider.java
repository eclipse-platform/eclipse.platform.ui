/*******************************************************************************
 * Copyright (c) 2006, 2014 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
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
 * An example showing how to create a {@link ILabelProvider label provider} that
 * to provide colors.
 *
 * @since 3.2
 */
public class Snippet007ColorLabelProvider {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final List persons = new ArrayList();
		persons.add(new Person("Fiona Apple", Person.FEMALE));
		persons.add(new Person("Elliot Smith", Person.MALE));
		persons.add(new Person("Diana Krall", Person.FEMALE));
		persons.add(new Person("David Gilmour", Person.MALE));

		final Display display = new Display();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			@Override
			public void run() {
				Shell shell = new Shell(display);
				shell.setText("Gender Bender");
				shell.setLayout(new GridLayout());

				Table table = new Table(shell, SWT.SINGLE | SWT.H_SCROLL
						| SWT.V_SCROLL | SWT.BORDER);
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

				IObservableList observableList = Observables
						.staticObservableList(persons);
				ObservableListContentProvider contentProvider = new ObservableListContentProvider();

				viewer.setContentProvider(contentProvider);

				// this does not have to correspond to the columns in the table,
				// we just list all attributes that affect the table content.
				IObservableMap[] attributes = new IObservableMap[2];
				attributes[0] = BeanProperties.value(Person.class, "name").observeDetail(
						contentProvider.getKnownElements());
				attributes[1] = BeanProperties.value(Person.class, "gender").observeDetail(
						contentProvider.getKnownElements());

				class ColorLabelProvider extends ObservableMapLabelProvider
						implements ITableColorProvider {
					Color male = display.getSystemColor(SWT.COLOR_BLUE);

					Color female = new Color(display, 255, 192, 203);

					ColorLabelProvider(IObservableMap[] attributes) {
						super(attributes);
					}

					// to drive home the point that attributes does not have to
					// match
					// the columns
					// in the table, we change the column text as follows:
					@Override
					public String getColumnText(Object element, int index) {
						if (index == 0) {
							return Integer
									.toString(persons.indexOf(element) + 1);
						}
						return ((Person) element).getName();
					}

					@Override
					public Color getBackground(Object element, int index) {
						return null;
					}

					@Override
					public Color getForeground(Object element, int index) {
						if (index == 0)
							return null;
						Person person = (Person) element;
						return (person.getGender() == Person.MALE) ? male
								: female;
					}

					@Override
					public void dispose() {
						super.dispose();
						female.dispose();
					}
				}
				viewer.setLabelProvider(new ColorLabelProvider(attributes));

				viewer.setInput(observableList);

				table.getColumn(0).pack();

				Button button = new Button(shell, SWT.PUSH);
				button.setText("Toggle Gender");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						IStructuredSelection selection = viewer.getStructuredSelection();
						if (selection != null && !selection.isEmpty()) {
							Person person = (Person) selection
									.getFirstElement();
							person
									.setGender((person.getGender() == Person.MALE) ? Person.FEMALE
											: Person.MALE);
						}
					}
				});

				shell.setSize(300, 400);
				shell.open();

				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		});
		display.dispose();
	}

	static class Person {
		static final int MALE = 0;

		static final int FEMALE = 1;

		private String name;

		private int gender;

		private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
				this);

		Person(String name, int gender) {
			this.name = name;
			this.gender = gender;
		}

		/**
		 * Returns the name. Method declared public to satisfy Java bean
		 * conventions
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param listener
		 */
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}

		/**
		 * Returns the gender. Method declared public to satisfy Java bean
		 * conventions
		 *
		 * @return the gender
		 */
		public int getGender() {
			return gender;
		}

		void setGender(int gender) {
			changeSupport.firePropertyChange("gender", this.gender,
					this.gender = gender);
		}
	}
}
