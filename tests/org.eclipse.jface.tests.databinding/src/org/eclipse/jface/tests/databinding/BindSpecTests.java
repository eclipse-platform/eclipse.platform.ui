/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920, 159768
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
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
		assertNull(spec.getModelToTargetConverter());
		assertNull(spec.getModelUpdatePolicy());
		assertNull(spec.getTargetToModelConverter());
		assertNull(spec.getTargetUpdatePolicy());

		assertTrue(spec.isUpdateModel());
		assertTrue(spec.isUpdateTarget());
		assertNull(spec.getTargetValidatePolicy());
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

	public void testAddTargetValidator() throws Exception {
		BindSpec bindSpec = new BindSpec();

		IValidator validator = new ValidatorStub();
		bindSpec.addTargetValidator(BindingEvent.PIPELINE_AFTER_GET, validator);

		IValidator[] validators = bindSpec
				.getTargetValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertNotNull("validators are null", validators);
		assertEquals("validator count", 1, validators.length);
		assertEquals("after get validator", validator, validators[0]);
	}

	public void testAddTargetValidatorMultiplesOfSameType() throws Exception {
		BindSpec bindSpec = new BindSpec();

		IValidator validator = new ValidatorStub();
		bindSpec.addTargetValidator(BindingEvent.PIPELINE_AFTER_GET, validator);
		bindSpec.addTargetValidator(BindingEvent.PIPELINE_AFTER_GET, validator);

		IValidator[] validators = bindSpec
				.getTargetValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertEquals(2, validators.length);
		assertTrue("validators", Arrays.equals(new IValidator[] { validator,
				validator }, validators));
	}

	public void testAddTargetValidatorMultiplesOfVaryingTypes()
			throws Exception {
		BindSpec bindSpec = new BindSpec();
		IValidator validator1 = new ValidatorStub();
		bindSpec
				.addTargetValidator(BindingEvent.PIPELINE_AFTER_GET, validator1);

		IValidator validator2 = new ValidatorStub();
		bindSpec.addTargetValidator(BindingEvent.PIPELINE_AFTER_CONVERT,
				validator2);

		IValidator[] validators = bindSpec
				.getTargetValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertEquals(1, validators.length);
		assertTrue("validators", Arrays.equals(new IValidator[] { validator1 },
				validators));

		validators = bindSpec
				.getTargetValidators(BindingEvent.PIPELINE_AFTER_CONVERT);
		assertEquals(1, validators.length);
		assertTrue("validators", Arrays.equals(new IValidator[] { validator2 },
				validators));
	}

	public void testGetTargetValidatorsNoneRegistered() throws Exception {
		BindSpec bindSpec = new BindSpec();
		IValidator[] validators = bindSpec
				.getTargetValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertNotNull("null should never be returned", validators);
		assertEquals("empty array should be returned", 0, validators.length);
	}

	public void testAddModelValidator() throws Exception {
		BindSpec bindSpec = new BindSpec();

		IValidator validator = new ValidatorStub();
		bindSpec.addModelValidator(BindingEvent.PIPELINE_AFTER_GET, validator);

		IValidator[] validators = bindSpec
				.getModelValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertNotNull("validators are null", validators);
		assertEquals("validator count", 1, validators.length);
		assertEquals("after get validator", validator, validators[0]);
	}

	public void testAddModelValidatorMultiplesOfSameType() throws Exception {
		BindSpec bindSpec = new BindSpec();

		IValidator validator = new ValidatorStub();
		bindSpec.addModelValidator(BindingEvent.PIPELINE_AFTER_GET, validator);
		bindSpec.addModelValidator(BindingEvent.PIPELINE_AFTER_GET, validator);

		IValidator[] validators = bindSpec
				.getModelValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertEquals(2, validators.length);
		assertTrue("validators", Arrays.equals(new IValidator[] { validator,
				validator }, validators));
	}

	public void testAddModelValidatorMultiplesOfVaryingTypes() throws Exception {
		BindSpec bindSpec = new BindSpec();
		IValidator validator1 = new ValidatorStub();
		bindSpec.addModelValidator(BindingEvent.PIPELINE_AFTER_GET, validator1);

		IValidator validator2 = new ValidatorStub();
		bindSpec.addModelValidator(BindingEvent.PIPELINE_AFTER_CONVERT,
				validator2);

		IValidator[] validators = bindSpec
				.getModelValidators(BindingEvent.PIPELINE_AFTER_GET);
		assertEquals(1, validators.length);
		assertTrue("validators", Arrays.equals(new IValidator[] { validator1 },
				validators));

		validators = bindSpec
				.getModelValidators(BindingEvent.PIPELINE_AFTER_CONVERT);
		assertEquals(1, validators.length);
		assertTrue("validators", Arrays.equals(new IValidator[] { validator2 },
				validators));
	}
	
	public void testFillBindSpecDefaults() throws Exception {
		class Spec extends BindSpec {
			protected void fillBindSpecDefaults(IObservable target,
					IObservable model) {
				super.fillBindSpecDefaults(target, model);
			}
		}

		Spec bindSpec = new Spec();
		bindSpec.fillBindSpecDefaults(null, null);

		for (Iterator it = BindingEvent.PIPELINE_CONSTANTS.keySet().iterator(); it
				.hasNext();) {
			Integer integer = (Integer) it.next();
			int position = integer.intValue();
			IValidator[] targetValidators = bindSpec
					.getTargetValidators(position);

			String display = (String) BindingEvent.PIPELINE_CONSTANTS.get(integer);
			assertEquals("target position " + display + " should have 1 validator",
					1, targetValidators.length);
			assertTrue("target position " + display + " should return OK status",
					targetValidators[0].validate(null).isOK());

			IValidator[] modelValidators = bindSpec.getModelValidators(position);
			assertEquals("model position " + display + " should have 1 validator",
					1, modelValidators.length);
			assertTrue("model position " + display + " should return OK status",
					modelValidators[0].validate(null).isOK());
		}
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

	private class ValidatorStub implements IValidator {
		public IStatus validate(Object value) {
			return Status.OK_STATUS;
		}
	}
}
