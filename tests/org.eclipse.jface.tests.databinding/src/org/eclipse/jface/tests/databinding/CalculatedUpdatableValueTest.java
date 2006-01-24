/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.beans.PropertyChangeEvent;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.updatables.CalculatedValue;
import org.eclipse.jface.databinding.updatables.SettableValue;
import org.eclipse.jface.examples.databinding.model.ModelObject;

/**
 * @since 3.2
 *
 */
public class CalculatedUpdatableValueTest extends TestCase {
	public void test_ctor() throws Exception {
		CalculatedValue cv = new CalculatedValue(Integer.TYPE) {
			protected Object calculate() {
				return new Integer(42);
			}
		};
		assertEquals("value type should be Integer.TYPE", Integer.TYPE, cv.getValueType());
	}
	
	public void test_getValue() throws Exception {
		CalculatedValue cv = new CalculatedValue(Integer.TYPE) {
			protected Object calculate() {
				return new Integer(42);
			}
		};
		assertEquals("Calculated value should be 42", new Integer(42), cv.getValue());
	}
	
	public void test_handleChange() throws Exception {
		final int[] seed = new int[] {42};
		CalculatedValue cv = new CalculatedValue(Integer.TYPE) {
			protected Object calculate() {
				return calcNewValue(seed);
			}
		};
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		seed[0]++;
		cv.getPropertyChangeListener().propertyChange(new PropertyChangeEvent(this, "", null, null));
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		seed[0]++;
		cv.getUpdatableChangeListener().handleChange(new ChangeEvent(this, ChangeEvent.CHANGE, null, null));
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
	}

	private Object calcNewValue(int[] seed) {
		return new Integer(seed[0] + 2);
	}
	
	public void test_hookAndUnhookDependantUpdatableValues() throws Exception {
		final int[] seed = new int[] {42};
		CalculatedValue cv = new CalculatedValue(Integer.TYPE) {
			protected Object calculate() {
				return calcNewValue(seed);
			}
		};
		SettableValue test1 = new SettableValue(Integer.TYPE);
		SettableValue test2 = new SettableValue(Integer.TYPE);
		
		// Hook updatables...
		cv.setDependencies(new IUpdatableValue[] {test1, test2});
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		
		seed[0]++;
		test1.setValue(new Integer(0));
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());

		seed[0]++;
		test2.setValue(new Integer(0));
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		
		// Unhook updatables...
		SettableValue test3 = new SettableValue(Integer.TYPE);
		SettableValue test4 = new SettableValue(Integer.TYPE);
		cv.setDependencies(new IUpdatableValue[] {test3, test4});
		
		Integer oldValue = (Integer) cv.getValue();

		seed[0]++;	// Calculation has changed
		test2.setValue(new Integer(0));	// should not update yet
		assertEquals("CalculatedValue should be " + oldValue, oldValue, cv.getValue());
		test3.setValue(new Integer(0)); // This should update
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());

		seed[0]++;
		test4.setValue(new Integer(0));
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
	}
	
	private static class TestModel extends ModelObject {
		public void fireChange() {
			firePropertyChange("fireChange", null, null);
		}
	}

	public void test_hookAndUnhookPropertyChangeListeners() throws Exception {
		final int[] seed = new int[] {42};
		CalculatedValue cv = new CalculatedValue(Integer.TYPE) {
			protected Object calculate() {
				return calcNewValue(seed);
			}
		};
		TestModel test1 = new TestModel();
		TestModel test2 = new TestModel();
		
		// Hook beans...
		cv.setDependencies(new Object[] {test1, test2});
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		
		seed[0]++;
		test1.fireChange();
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		
		seed[0]++;
		test2.fireChange();
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		
		// Unhook beans...
		TestModel test3 = new TestModel();
		TestModel test4 = new TestModel();
		cv.setDependencies(new Object[] {test3, test4});
		
		Integer oldValue = (Integer) cv.getValue();
		
		seed[0]++;
		test2.fireChange();
		assertEquals("CalculatedValue should be " + oldValue, oldValue, cv.getValue());
		test3.fireChange();
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
		
		seed[0]++;
		test4.fireChange();
		assertEquals("CalculatedValue should be " + calcNewValue(seed), calcNewValue(seed), cv.getValue());
	}
}
