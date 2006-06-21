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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Shows how to bind a Combo so that when update its items, the selection is
 * retained if at all possible.
 *  
 * @since 3.2
 */
public class Snippet003UpdateComboBindUsingViewer {
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
		System.out.println(viewModel.getText());
	}
	
	// Minimal JavaBeans support
	static abstract class AbstractModelObject {
		private PropertyChangeSupport propertyChangeSupport = 
			new PropertyChangeSupport(this);
		
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		public void addPropertyChangeListener(String propertyName,
				PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
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
	
	// The View's model--the root of our Model graph for this particular GUI.
	static class ViewModel extends AbstractModelObject {
		private String text = "beef";
		private List choices = new ArrayList(); {
			choices.add("pork");
			choices.add("beef");
			choices.add("poultry");
			choices.add("vegatables");
		}
		
		public List getChoices() {
			return choices;
		}
		public void setChoices(List choices) {
			this.choices = choices;
			firePropertyChange("choices", null, null);
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			String oldValue = this.text;
			this.text = text;
			firePropertyChange("test", oldValue, text);
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
			
			Combo combo = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);
			ComboViewer viewer = new ComboViewer(combo);
			Button reset = new Button(shell, SWT.NULL);
			reset.setText("reset collection");
			reset.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					List newList = new ArrayList();
					newList.add("Chocolate");
					newList.add("Vanilla");
					newList.add("Mango Parfait");
					newList.add("beef");
					newList.add("Cheesecake");
					viewModel.setChoices(newList);
				}
			});
			
			// Print value out first
			System.out.println(viewModel.getText());
			
			// Bind it
			DataBindingContext bindingContext = createContext(shell);
			bindingContext.bind(new Property(viewer, ViewersProperties.CONTENT),
					new Property(viewModel, "choices"), null);
			bindingContext.bind(new Property(viewer, ViewersProperties.SINGLE_SELECTION), 
					new Property(viewModel, "text"), null);
			
			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}
}
