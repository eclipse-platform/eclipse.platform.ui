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

import org.eclipse.jface.databinding.observable.IObservable;
import org.eclipse.jface.examples.databinding.DataBindingFactory1;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.ListModelDescription;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersProperties;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Demonstrates nested selection.<br>
 * At the first level, user may select a person.<br>
 * At the second level, user may select a city to associate with the selected<br>
 * person or edit the person's name.
 */
public class Snippet001NestedSelectionWithCombo {
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
	
	// The data model class.  This is normally a persistent class of some sort.
	// 
	// This example implements full JavaBeans bound properties so that changes
	// to instances of this class will automatically be propogated to the UI.
	static class Person extends AbstractModelObject {
		// Constructor
		public Person(String name, String city) {
			this.name = name;
			this.city = city;
		}

		// Some JavaBean bound properties...
		String name;
		String city;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			String oldValue = this.name;
			this.name = name;
			firePropertyChange("name", oldValue, name);
		}
		
		public String getCity() {
			return city;
		}
		
		public void setCity(String city) {
			String oldValue = this.city;
			this.city = city;
			firePropertyChange("city", oldValue, city);
		}
	}
	
	// The View's model--the root of our GUI's Model graph
	//
	// Typically each View class has a corresponding ViewModel class.  
	// The ViewModel is responsible for getting the objects to edit from the 
	// DAO.  Since this snippet doesn't have any persistent objects to 
	// retrieve, this ViewModel just instantiates some objects to edit.
	// 
	// This ViewModel also implements JavaBean bound properties.
	static class ViewModel extends AbstractModelObject {
		// The model to bind
		private ArrayList people = new ArrayList(); {
			people.add(new Person("Wile E. Coyote", "Tucson"));
			people.add(new Person("Road Runner", "Lost Horse"));
			people.add(new Person("Bugs Bunny", "Forrest"));
		}
		
		// Choice of cities for the Combo
		private ArrayList cities = new ArrayList(); {
			cities.add("Tucson");
			cities.add("AcmeTown");
			cities.add("Lost Horse");
			cities.add("Forrest");
			cities.add("Lost Mine");
		}
		
		public ArrayList getPeople() {
			return people;
		}
		
		public ArrayList getCities() {
			return cities;
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
			Shell shell = new Shell(Display.getCurrent());
			
			List peopleList = new List(shell, SWT.BORDER);
			Text name = new Text(shell, SWT.BORDER);
			Combo city = new Combo(shell, SWT.BORDER | SWT.READ_ONLY);
			
			// Bind it
			DataBindingContext bindingContext = new DataBindingFactory1().createContext(shell);
			
			ListViewer peopleListViewer = new ListViewer(peopleList);
			//ComboViewer cityViewer = new ComboViewer(city);

			bindingContext.bind(peopleListViewer, 
					new ListModelDescription(
							new Property(viewModel, 
									"people", 
									Person.class, 
									Boolean.TRUE), "name"),
							null);
			
			IObservable selectedObservable = bindingContext.createObservable(
					new Property(peopleListViewer, 
									ViewersProperties.SINGLE_SELECTION,
									Person.class,
									Boolean.FALSE));
			
			bindingContext.bind(name, 
					new Property(selectedObservable, 
							"name", 
							String.class, 
							Boolean.FALSE), 
					null);
			
			bindingContext.bind(new Property(city, SWTProperties.ITEMS),
					new Property(viewModel, "cities"),
					null);
			
			bindingContext.bind(new Property(city, SWTProperties.SELECTION),
					new Property(selectedObservable, 
							"city", 
							String.class, 
							Boolean.FALSE),
					null);

			GridLayoutFactory.swtDefaults().applyTo(shell);
			// Open and return the Shell
			shell.pack();
			shell.open();
			return shell;
		}
	}
	
}
