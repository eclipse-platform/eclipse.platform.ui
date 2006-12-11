/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import junit.framework.TestCase;

import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Unit tests for the BindSpec class.
 * 
 * @since 3.2
 */
public class BindSpecTests extends TestCase {
	/**
	 * Asserts the BindSpec state when using the default constructor.
	 */
	public void testDefaultConstructor() {
		DefaultBindSpec spec = new DefaultBindSpec();
		assertNull(spec.getDomainValidator());
		assertNull(spec.getModelToTargetConverter());
		assertNull(spec.getModelUpdatePolicy());
		assertNull(spec.getTargetToModelConverter());
		assertNull(spec.getTargetUpdatePolicy());
		assertNull(spec.getTargetValidator());
		assertNull(spec.getValidatePolicy());
		assertTrue(spec.isUpdateModel());
		assertTrue(spec.isUpdateTarget());
	}

	/**
	 * Asserts that when a validator is set it will always be the sole validator
	 * and will remove any existing validators.
	 */
	public void testSetValidator() {
		DefaultBindSpec spec = new DefaultBindSpec();

		IValidator v3 = new Validator();
		spec.setTargetValidator(v3);
		assertSame(v3, spec.getTargetValidator());
	}

	/**
	 * Asserts that when <code>null</code> getTypeConverstionValidator() will
	 * return <code>null</code> and getTypeConversionValidators returns an
	 * empty array.
	 */
	public void testGetNullValidator() {
		DefaultBindSpec spec = new DefaultBindSpec();
		spec.setTargetValidator(null);
		assertNull(spec.getTargetValidator());
	}

	/**
	 * Asserts that when a model to target converter is set it will always be
	 * the sole converter and will remove any existing converters.
	 */
	public void testSetModelToTargetConverter() {
		DefaultBindSpec spec = new DefaultBindSpec();

		IConverter c3 = new Converter();
		spec.setModelToTargetConverter(c3);
		assertSame(c3, spec.getModelToTargetConverter());
	}

	/**
	 * Asserts that when <code>null</code> getModelToTargetConverter() will
	 * return <code>null</code> and getModelToTargetConverters() returns an
	 * empty array.
	 */
	public void testGetNullModelToTargetConverter() {
		DefaultBindSpec spec = new DefaultBindSpec();

		spec.setModelToTargetConverter(null);
		assertNull(spec.getModelToTargetConverter());
	}

	/**
	 * Asserts that when a target to model converter is set it will always be
	 * the sole converter and will remove any existing converters.
	 */
	public void testSetTargetToModelConverter() {
		DefaultBindSpec spec = new DefaultBindSpec();

		IConverter c3 = new Converter();
		spec.setTargetToModelConverter(c3);
		assertSame(c3, spec.getTargetToModelConverter());
	}

	/**
	 * Asserts that when <code>null</code> getTargetToModelConverter() will
	 * return <code>null</code> and getTargetToModelConverters() returns an
	 * empty array.
	 * 
	 */
	public void testGetNullTargetToModelConverter() {
		DefaultBindSpec spec = new DefaultBindSpec();
		spec.setTargetToModelConverter(null);
		assertNull(spec.getTargetToModelConverter());
	}

	private class Converter implements IConverter {
		public Object convert(Object fromObject) {
			return null;
		}

		public Object getFromType() {
			return null;
		}

		public Object getToType() {
			return null;
		}
	}

	private class Validator implements IValidator {
		public IStatus validate(Object value) {
			return Status.OK_STATUS;
		}
	}
}
