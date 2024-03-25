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
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet which updates the GUI without any databinding. Bind changes in a GUI
 * to a Model object but don't worry about propagating changes from the Model to
 * the GUI -- using *manual* code.
 */
public class Snippet_NoDatabinding {
	public static void main(String[] args) {
		final Display display = new Display();
		View view = new View(new ViewModel());
		Shell shell = view.createShell();

		// The SWT event loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();

		// Print the results
		System.out.println("person.getName() = " + view.viewModel.getPerson().getName());
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
	 * In this example, we only push changes from the GUI to the model, so we don't
	 * worry about implementing JavaBeans bound properties. If we need our GUI to
	 * automatically reflect changes in the Person object, the Person object would
	 * need to implement the JavaBeans property change listener methods.
	 */
	static class Person extends AbstractModelObject {
		String name = "John Smith";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * The View's model--the root of our Model graph for this particular GUI.
	 */
	static class ViewModel {
		// The model to bind
		private final Person person = new Person();

		public Person getPerson() {
			return person;
		}
	}

	/** The GUI view. */
	static class View {
		private ViewModel viewModel = new ViewModel();

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			Shell shell = new Shell();
			shell.setLayout(new RowLayout(SWT.VERTICAL));

			final Text name = new Text(shell, SWT.BORDER);

			// Bind it (manually)
			name.setText(viewModel.getPerson().getName());
			name.addModifyListener(e -> {
				final String text = name.getText();
				// validation
				// conversion
				viewModel.getPerson().setName(text);
			});
			viewModel.person.addPropertyChangeListener("name", evt -> shell.getDisplay().asyncExec(() -> {
				final String newName = viewModel.person.getName();
				// conversion
				name.setText(newName);
			}));

			shell.pack();
			shell.open();
			return shell;
		}
	}

}
