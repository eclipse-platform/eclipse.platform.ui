/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.NestedProperty;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.converters.IdentityConverter;
import org.eclipse.jface.databinding.updatables.SettableValue;
import org.eclipse.jface.databinding.validator.IValidator;
import org.eclipse.jface.tests.databinding.util.Mocks;

public class DatabindingContextTest extends TestCase {

	boolean failed = false;

	IDataBindingContext dbc;

	IUpdatableValue updatableValueRMock;

	IValidator validatorMock;

	SettableValue settableValue1;

	SettableValue settableValue2;

	Object o1 = new Object();

	Object o2 = new Object();

	private static IConverter identityConverter = new IdentityConverter(Object.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		dbc = DataBinding.createContext(new IUpdatableFactory[0]);
		updatableValueRMock = (IUpdatableValue) Mocks
				.createRelaxedMock(IUpdatableValue.class);
		validatorMock = (IValidator) Mocks.createMock(IValidator.class);
		settableValue1 = new SettableValue(Object.class);
		settableValue2 = new SettableValue(Object.class);
	}

	protected void tearDown() throws Exception {
		if (!failed) {
			Mocks.verify(updatableValueRMock);
			Mocks.verify(validatorMock);
		}
		super.tearDown();
	}

	protected void runTest() throws Throwable {
		try {
			super.runTest();
		} catch (Throwable th) {
			failed = true;
			throw th;
		}
	}

	public void testBindValueModel() {
		Mocks.reset(updatableValueRMock);
		updatableValueRMock.addChangeListener(null);
		updatableValueRMock.getValue();
		updatableValueRMock.getValueType();
		Mocks.setLastReturnValue(updatableValueRMock, Object.class);
		validatorMock.isValid(null);
		Mocks.startChecking(updatableValueRMock);
		Mocks.startChecking(validatorMock);
		dbc.bind(settableValue1, updatableValueRMock, new BindSpec(
				identityConverter, validatorMock));
		Mocks.verify(updatableValueRMock);
	}

	public void testBindValueTarget() {
		updatableValueRMock.addChangeListener(null);
		updatableValueRMock.setValue(null);
		updatableValueRMock.getValue();
		updatableValueRMock.getValueType();
		Mocks.setLastReturnValue(updatableValueRMock, Object.class);
		validatorMock.isValid(null);
		Mocks.startChecking(updatableValueRMock);
		Mocks.startChecking(validatorMock);
		dbc.bind(updatableValueRMock, settableValue2, new BindSpec(
				identityConverter, validatorMock));
	}

	public void testBindValuePropagation() {
		settableValue1.setValue(o1);
		settableValue2.setValue(o2);
		dbc.bind(settableValue1, settableValue2, null);
		assertEquals(o2, settableValue1.getValue());
		settableValue1.setValue(o1);
		assertEquals(o1, settableValue2.getValue());
		settableValue2.setValue(o2);
		assertEquals(o2, settableValue1.getValue());
	}
	
	public void testCreateNestedUpdatableWithArrays() {
		String parentObject = "";
		NestedProperty nestedProperty = new NestedProperty(parentObject, new String[] {"nestedChild1", "nestedChild2", "foo"}, new Class[] {Integer.class, String.class, Float.class});
		IDataBindingContext ctx = DataBinding.createContext(new IUpdatableFactory[] {new MockUpdatableFactory()});
		MockUpdatableValue updatableValue = (MockUpdatableValue) ctx.createNestedUpdatable(nestedProperty);
		assertEquals("The child IUpdatable does not have the right getter.", "foo", updatableValue.getDescription());
		assertEquals("The child IUpdatable does not have the right type.", Float.class, updatableValue.getType());

		updatableValue = ((MockUpdatableValue) updatableValue.getTargetObject());
		assertEquals("The child IUpdatable does not have the right getter.", "nestedChild2", updatableValue.getDescription());
		assertEquals("The child IUpdatable does not have the right type.", String.class, updatableValue.getType());
	
		updatableValue = ((MockUpdatableValue) updatableValue.getTargetObject());
		assertEquals("The child IUpdatable does not have the right getter.", "nestedChild1", updatableValue.getDescription());
		assertSame("The child IUpdatable does not have a correct parent target object.", parentObject, updatableValue.getTargetObject());
		assertEquals("The child IUpdatable does not have the right type.", Integer.class, updatableValue.getType());
	}
	
	public void testCreateNestedUpdatableWithPrototypeClass() {
		String parentObject = "";
		NestedProperty nestedProperty = new NestedProperty(parentObject, "nestedChild1.nestedChild2.foo", NestedParent.class);
		IDataBindingContext ctx = DataBinding.createContext(new IUpdatableFactory[] {new MockUpdatableFactory()});
		MockUpdatableValue updatableValue = (MockUpdatableValue) ctx.createNestedUpdatable(nestedProperty);
		assertEquals("The child IUpdatable does not have the right getter.", "foo", updatableValue.getDescription());
		assertEquals("The child IUpdatable does not have the right type.", String.class, updatableValue.getType());

		updatableValue = ((MockUpdatableValue) updatableValue.getTargetObject());
		assertEquals("The child IUpdatable does not have the right getter.", "nestedChild2", updatableValue.getDescription());
		assertEquals("The child IUpdatable does not have the right type.", NestedChild2.class, updatableValue.getType());
	
		updatableValue = ((MockUpdatableValue) updatableValue.getTargetObject());
		assertEquals("The child IUpdatable does not have the right getter.", "nestedChild1", updatableValue.getDescription());
		assertSame("The child IUpdatable does not have a correct parent target object.", parentObject, updatableValue.getTargetObject());
		assertEquals("The child IUpdatable does not have the right type.", NestedChild1.class, updatableValue.getType());
	}
	
	public void testCreateNestedUpdatableWithPrototypeClassAndInvalidPath() {
		String parentObject = "";
		NestedProperty nestedProperty = new NestedProperty(parentObject, "nestedChild1.nestedChild3.foo", NestedParent.class);
		try {
			IDataBindingContext ctx = DataBinding.createContext(new IUpdatableFactory[] {new MockUpdatableFactory()});
			MockUpdatableValue updatableValue = (MockUpdatableValue) ctx.createNestedUpdatable(nestedProperty);
			fail("Expected binding exception.");
		} catch (BindingException be) {			
		}
	}

	public class MockUpdatableFactory implements IUpdatableFactory {

		public IUpdatable createUpdatable(Map properties, Object description, IDataBindingContext bindingContext) {
			Property property = (Property) description;
			return new MockUpdatableValue(property.getObject(), property.getPropertyID(), property.getPropertyType());
		}
	}
	
	public class MockUpdatableValue extends UpdatableValue {
		public Object targetObject;
		public Object description;
		private Class type;
		
		public MockUpdatableValue(Object targetObject, Object description, Class type) {
			super();
			// TODO Auto-generated constructor stub
			this.targetObject = targetObject;
			this.description = description;
			this.type = type;
		}
		
		public Object getDescription() {
			return description;
		}

		public Class getType() {
			return type;
		}

		public Object getTargetObject() {
			return targetObject;
		}



		public Object getValue() {
			// TODO Auto-generated method stub
			return null;
		}

		public Class getValueType() {
			return null;
		}

		public void setValue(Object value) {
		}
		
	}
	
	private class NestedParent {
		public NestedChild1 getNestedChild1() {
			return new NestedChild1();
		}
	}

	private class NestedChild1 {
		public NestedChild2 getNestedChild2() {
			return new NestedChild2();
		}
	}

	private class NestedChild2 {
		public String getFoo() {
			return "foo";
		}
	}	
}
