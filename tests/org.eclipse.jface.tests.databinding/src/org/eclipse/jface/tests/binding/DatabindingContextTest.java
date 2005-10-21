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
package org.eclipse.jface.tests.binding;

import junit.framework.TestCase;

import org.eclipse.jface.binding.BindingException;
import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IConverter;
import org.eclipse.jface.binding.IUpdatableValue;
import org.eclipse.jface.binding.IValidator;
import org.eclipse.jface.binding.SettableValue;
import org.eclipse.jface.tests.binding.util.Mocks;

public class DatabindingContextTest extends TestCase {

	DatabindingContext dbc;

	IUpdatableValue updatableValueRMock;

	IValidator validatorMock;

	SettableValue settableValue1;

	SettableValue settableValue2;

	Object o1 = new Object();

	Object o2 = new Object();

	private static IConverter identityConverter = new IConverter() {

		public Class getModelType() {
			return Object.class;
		}

		public Class getTargetType() {
			return Object.class;
		}

		public Object convertTargetToModel(Object object) {
			return object;
		}

		public Object convertModelToTarget(Object object) {
			return object;
		}
	};

	protected void setUp() throws Exception {
		super.setUp();
		dbc = new DatabindingContext();
		updatableValueRMock = (IUpdatableValue) Mocks
				.createRelaxedMock(IUpdatableValue.class);
		validatorMock = (IValidator) Mocks.createMock(IValidator.class);
		settableValue1 = new SettableValue(Object.class);
		settableValue2 = new SettableValue(Object.class);
	}

	protected void tearDown() throws Exception {
		Mocks.verify(updatableValueRMock);
		Mocks.verify(validatorMock);
		super.tearDown();
	}

	public void testBindValueModel() {
		Mocks.reset(updatableValueRMock);
		updatableValueRMock.addChangeListener(null);
		updatableValueRMock.getValue();
		validatorMock.isValid(null);
		Mocks.startChecking(updatableValueRMock);
		Mocks.startChecking(validatorMock);
		dbc.bind(settableValue1, updatableValueRMock, identityConverter,
				validatorMock);
		Mocks.verify(updatableValueRMock);
	}

	public void testBindValueTarget() {
		updatableValueRMock.addChangeListener(null);
		updatableValueRMock.setValue(null, null);
		updatableValueRMock.getValue();
		validatorMock.isValid(null);
		Mocks.startChecking(updatableValueRMock);
		Mocks.startChecking(validatorMock);
		dbc.bind(updatableValueRMock, settableValue2, identityConverter,
				validatorMock);
	}

	public void testBindValuePropagation() throws BindingException {
		settableValue1.setValue(o1, null);
		settableValue2.setValue(o2, null);
		dbc.bind(settableValue1, settableValue2);
		assertEquals(o2, settableValue1.getValue());
		settableValue1.setValue(o1, null);
		assertEquals(o1, settableValue2.getValue());
		settableValue2.setValue(o2, null);
		assertEquals(o2, settableValue1.getValue());
	}
}
