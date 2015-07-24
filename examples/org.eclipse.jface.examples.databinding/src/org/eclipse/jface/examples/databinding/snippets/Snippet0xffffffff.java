/*******************************************************************************
 * Copyright (c) 2006, 2014 The Pampered Chef, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Snippet -1.
 *
 * Hello, no databinding. Bind changes in a GUI to a Model object but don't
 * worry about propogating changes from the Model to the GUI -- using *manual*
 * code. (0xffffffff is -1 in 32-bit two's complement binary arithmatic)
 */
public class Snippet0xffffffff {
	public static void main(String[] args) {
		ViewModel viewModel = new ViewModel();
		Shell shell = new View(viewModel).createShell();

		// The SWT event loop
		Display display = Display.getCurrent();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// Print the results
		System.out.println("person.getName() = "
				+ viewModel.getPerson().getName());
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
	//
	// In this example, we only push changes from the GUI to the model, so we
	// don't worry about implementing JavaBeans bound properties. If we need
	// our GUI to automatically reflect changes in the Person object, the
	// Person object would need to implement the JavaBeans property change
	// listener methods.
	static class Person extends AbstractModelObject {
		// A property...
		String name = "John Smith";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	// The View's model--the root of our Model graph for this particular GUI.
	//
	// Typically each View class has a corresponding ViewModel class.
	//
	// The ViewModel is responsible for getting the objects to edit from the
	// data access tier. Since this snippet doesn't have any persistent objects
	// to
	// retrieve, this ViewModel just instantiates a model object to edit.
	static class ViewModel {
		// The model to bind
		private Person person = new Person();

		public Person getPerson() {
			return person;
		}
	}

	// The GUI view
	static class View {
		private ViewModel viewModel;

		public View(ViewModel viewModel) {
			this.viewModel = viewModel;
		}

		public Shell createShell() {
			// Build a UI
			final Display display = Display.getCurrent();
			Shell shell = new Shell(display);
			shell.setLayout(new RowLayout(SWT.VERTICAL));

			final Text name = new Text(shell, SWT.BORDER);

			// Bind it (manually)
			name.setText(viewModel.getPerson().getName());
			name.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					final String text = name.getText();
					// validation
					// conversion
					viewModel.getPerson().setName(text);
				}
			});
			viewModel.person.addPropertyChangeListener("name",
					new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							display.asyncExec(new Runnable() {
								@Override
								public void run() {
									final String newName = viewModel.person.getName();
									// conversion
									name.setText(newName);
								}
							});
						}
					});

			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}

}
