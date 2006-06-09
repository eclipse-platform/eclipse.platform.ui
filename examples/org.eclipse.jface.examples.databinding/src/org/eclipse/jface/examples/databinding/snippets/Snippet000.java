/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.snippets;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.beans.BeanObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.NestedObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersObservableFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Hello, databinding.  Illustrates the basic Model-ViewModel-Binding-View
 * architecture used in data binding applications.
 */
public class Snippet000 {
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
		System.out.println(viewModel.getPerson().getName());
	}
	
	// The data model class.  This is normally a persistent class of some sort.
	// 
	// In this example, we only push changes from the GUI to the model, so we 
	// don't worry about implementing JavaBeans bound properties.  If we need
	// our GUI to automatically reflect changes in the Person object, the 
	// Person object would need to implement the JavaBeans property change
	// listener methods.
	static class Person {
		// Constructor
		public Person(String name) {
			this.name = name;
		}

		// A property...
		String name;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
	}
	
	// The View's model--the root of our Model graph for this particular GUI.
	static class ViewModel {
		// The model to bind
		private Person person = new Person("HelloWorld");
		
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
		
		// A standard createContext factory method.  Copy this into your app
		// and modify it if you need to.
		private DataBindingContext createContext(Composite parent) {
			final DataBindingContext context = new DataBindingContext();
			
			parent.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					context.dispose();
				}
			});

			context.addObservableFactory(new NestedObservableFactory(context));
			context.addObservableFactory(new BeanObservableFactory(context, null,
					new Class[] { Widget.class }));
			context.addObservableFactory(new SWTObservableFactory());
			context.addObservableFactory(new ViewersObservableFactory());
			context.addObservableFactory(new DefaultObservableFactory(context));
			context.addBindSupportFactory(new DefaultBindSupportFactory());
			context.addBindingFactory(new DefaultBindingFactory());
			context.addBindingFactory(new ViewersBindingFactory());
			
			return context;
		}
		
		public Shell createShell() {
			// Build a UI
			Shell shell = new Shell(Display.getCurrent());
			shell.setLayout(new RowLayout(SWT.VERTICAL));
			
			Text name = new Text(shell, SWT.BORDER);
			
			// Bind it
			DataBindingContext bindingContext = createContext(shell);
			Person person = viewModel.getPerson();
			bindingContext.bind(name, new Property(person, "name"), null);
			
			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}
	
}
