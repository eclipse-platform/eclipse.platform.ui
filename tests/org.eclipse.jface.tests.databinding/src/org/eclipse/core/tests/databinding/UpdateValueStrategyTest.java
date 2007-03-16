/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import java.util.Date;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.validation.StringToByteValidator;
import org.eclipse.core.internal.databinding.validation.StringToDateValidator;
import org.eclipse.core.internal.databinding.validation.StringToDoubleValidator;
import org.eclipse.core.internal.databinding.validation.StringToFloatValidator;
import org.eclipse.core.internal.databinding.validation.StringToIntegerValidator;
import org.eclipse.core.internal.databinding.validation.StringToLongValidator;
import org.eclipse.core.internal.databinding.validation.StringToShortValidator;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class UpdateValueStrategyTest extends AbstractDefaultRealmTestCase {
	public void testDefaultValidatorForStringToInteger() throws Exception {
		assertDefaultValidator(String.class, Integer.class, StringToIntegerValidator.class);
	}
	
	public void testDefaultValidatorForStringToIntegerPrimitive() throws Exception {
		assertDefaultValidator(String.class, Integer.TYPE, StringToIntegerValidator.class);
	}
	
	public void testDefaultValidatorForStringToLong() throws Exception {
		assertDefaultValidator(String.class, Long.class, StringToLongValidator.class);		
	}
	
	public void testDefaultValidatorForStringToLongPrimitive() throws Exception {
		assertDefaultValidator(String.class, Long.TYPE, StringToLongValidator.class);		
	}
	
	public void testDefaultValidatorForStringToFloat() throws Exception {
		assertDefaultValidator(String.class, Float.class, StringToFloatValidator.class);				
	}
	
	public void testDefaultValidatorForStringToFloatPrimitive() throws Exception {
		assertDefaultValidator(String.class, Float.TYPE, StringToFloatValidator.class);						
	}
	
	public void testDefaultValidatorForStringToDouble() throws Exception {
		assertDefaultValidator(String.class, Double.class, StringToDoubleValidator.class);
	}
	
	public void testDefaultValidatorForStringToDoublePrimitive() throws Exception {
		assertDefaultValidator(String.class, Double.TYPE, StringToDoubleValidator.class);
	}
	
	public void testDefaultValidatorForStringToByte() throws Exception {
		assertDefaultValidator(String.class, Byte.class, StringToByteValidator.class);
	}
	
	public void testDefaultValidatorForStringToBytePrimitive() throws Exception {
		assertDefaultValidator(String.class, Byte.TYPE, StringToByteValidator.class);
	}
	
	public void testDefaultValidatorForStringToShort() throws Exception {
		assertDefaultValidator(String.class, Short.class, StringToShortValidator.class);
	}

	public void testDefaultValidatorForStringToShortPrimitive() throws Exception {
		assertDefaultValidator(String.class, Short.TYPE, StringToShortValidator.class);
	}

	public void testDefaultValidatorForStringToDate() throws Exception {
		assertDefaultValidator(String.class, Date.class, StringToDateValidator.class);
	}
	
	public void testCachesDefaultedValidators() throws Exception {
		WritableValue source = WritableValue.withValueType(String.class);
		WritableValue destination = WritableValue.withValueType(Integer.class);
		
		UpdateValueStrategyStub strategy = new UpdateValueStrategyStub();
		strategy.fillDefaults(source, destination);
		
		IValidator validator = strategy.validator;
		assertNotNull(validator);
		
		strategy = new UpdateValueStrategyStub();
		strategy.fillDefaults(source, destination);
		
		assertSame(validator,strategy.validator);
	}
	
	private void assertDefaultValidator(Class fromType, Class toType, Class validatorType) {
		WritableValue source = WritableValue.withValueType(fromType);
		WritableValue destination = WritableValue.withValueType(toType);
		
		UpdateValueStrategyStub strategy = new UpdateValueStrategyStub();
		strategy.fillDefaults(source, destination);
		
		IValidator validator = strategy.validator;
		assertNotNull("validator not null", validator);
		assertTrue("converter instanceof " + validatorType, validatorType.isInstance(validator));
	}
	
	class UpdateValueStrategyStub extends UpdateValueStrategy {
		IValidator validator;
		
		protected void fillDefaults(IObservableValue source,
				IObservableValue destination) {
			super.fillDefaults(source, destination);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.databinding.UpdateValueStrategy#createValidator(java.lang.Object, java.lang.Object)
		 */
		protected IValidator createValidator(Object fromType, Object toType) {
			validator = super.createValidator(fromType, toType);
			return validator;
		}
	}
}
