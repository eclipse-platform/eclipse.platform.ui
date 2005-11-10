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

import junit.framework.TestCase;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IConverter;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.IValidator;
import org.eclipse.jface.databinding.IdentityConverter;
import org.eclipse.jface.databinding.SettableValue;
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

	public void testBindValueModel() throws BindingException {
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

	public void testBindValueTarget() throws BindingException {
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

	public void testBindValuePropagation() throws BindingException {
		settableValue1.setValue(o1);
		settableValue2.setValue(o2);
		dbc.bind(settableValue1, settableValue2, null);
		assertEquals(o2, settableValue1.getValue());
		settableValue1.setValue(o1);
		assertEquals(o1, settableValue2.getValue());
		settableValue2.setValue(o2);
		assertEquals(o2, settableValue1.getValue());
	}
}
