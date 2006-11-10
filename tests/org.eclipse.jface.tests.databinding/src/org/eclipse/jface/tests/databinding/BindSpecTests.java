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

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IDomainValidator;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationError;

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
        BindSpec spec = new BindSpec();
        assertNull(spec.getDomainValidator());
        assertNull(spec.getModelToTargetConverter());
        assertNull(spec.getModelUpdatePolicy());
        assertNull(spec.getTargetToModelConverter());
        assertNull(spec.getTargetUpdatePolicy());
        assertNull(spec.getTypeConversionValidator());
        assertNull(spec.getValidatePolicy());
        assertTrue(spec.isUpdateModel());
        assertTrue(spec.isUpdateTarget());
    }

    /**
     * Asserts the BindSpec state when using the constructor that accepts
     * arrays.
     */
    public void testArrayConstructor() {
        Converter[] toTarget = new Converter[] { new Converter() };
        Converter[] toModel = new Converter[] { new Converter() };
        Validator[] validator = new Validator[] { new Validator() };
        DomainValidator domainValidator = new DomainValidator();
        Integer modelPolicy = new Integer(0);
        Integer targetPolicy = new Integer(1);
        Integer validatePolicy = new Integer(2);

        BindSpec spec = new BindSpecExt(toTarget,
                toModel,
                validator,
                domainValidator,
                modelPolicy,
                validatePolicy,
                targetPolicy);
        assertEquals(toTarget, spec.getModelToTargetConverters());
        assertEquals(toModel, spec.getTargetToModelConverters());
        assertEquals(validator, spec.getTypeConversionValidators());
        assertEquals(domainValidator, spec.getDomainValidator());
        assertEquals(modelPolicy, spec.getModelUpdatePolicy());
        assertEquals(targetPolicy, spec.getTargetUpdatePolicy());
        assertEquals(validatePolicy, spec.getValidatePolicy());
        assertTrue(spec.isUpdateModel());
        assertTrue(spec.isUpdateTarget());
    }

    /**
     * Extension that makes the protected constructor visible to the test.
     */
    private static class BindSpecExt extends BindSpec {
        protected BindSpecExt(IConverter[] modelToTargetConverter, IConverter[] targetToModelConverter,
                IValidator[] targetValidator, IDomainValidator domainValidator, Integer modelUpdatePolicy,
                Integer validatePolicy, Integer targetUpdatePolicy) {
            super(modelToTargetConverter,
                    targetToModelConverter,
                    targetValidator,
                    domainValidator,
                    modelUpdatePolicy,
                    validatePolicy,
                    targetUpdatePolicy);
        }
    }

    /**
     * Asserts that when a validator is set it will always be the sole validator
     * and will remove any existing validators.
     */
    public void testSetValidator() {
        BindSpec spec = new BindSpec();

        IValidator v1 = new Validator();
        IValidator v2 = new Validator();

        spec.setValidators(new IValidator[] { v1, v2 });
        assertEquals(2, spec.getTypeConversionValidators().length);

        IValidator v3 = new Validator();
        spec.setValidator(v3);
        assertEquals(1, spec.getTypeConversionValidators().length);
        assertSame(v3, spec.getTypeConversionValidator());
        assertSame(v3, spec.getTypeConversionValidators()[0]);
    }

    /**
     * Asserts that when <code>null</code> getTypeConverstionValidator() will
     * return <code>null</code> and getTypeConversionValidators returns an
     * empty array.
     */
    public void testGetNullValidator() {
        BindSpec spec = new BindSpec();
        spec.setValidator(null);
        assertNull(spec.getTypeConversionValidator());
        assertEquals(0, spec.getTypeConversionValidators().length);
    }

    /**
     * Asserts that when a model to target converter is set it will always be
     * the sole converter and will remove any existing converters.
     */
    public void testSetModelToTargetConverter() {
        BindSpec spec = new BindSpec();

        IConverter c1 = new Converter();
        IConverter c2 = new Converter();

        spec.setModelToTargetConverters(new IConverter[] { c1, c2 });
        assertEquals(2, spec.getModelToTargetConverters().length);

        IConverter c3 = new Converter();
        spec.setModelToTargetConverter(c3);
        assertEquals(1, spec.getModelToTargetConverters().length);
        assertSame(c3, spec.getModelToTargetConverter());
        assertSame(c3, spec.getModelToTargetConverters()[0]);
    }

    /**
     * Asserts that when <code>null</code> getModelToTargetConverter() will
     * return <code>null</code> and getModelToTargetConverters() returns an
     * empty array.
     */
    public void testGetNullModelToTargetConverter() {
        BindSpec spec = new BindSpec();

        spec.setModelToTargetConverter(null);
        assertNull(spec.getModelToTargetConverter());
        assertEquals(0, spec.getModelToTargetConverters().length);
    }

    /**
     * Asserts that when a target to model converter is set it will always be
     * the sole converter and will remove any existing converters.
     */
    public void testSetTargetToModelConverter() {
        BindSpec spec = new BindSpec();

        IConverter c1 = new Converter();
        IConverter c2 = new Converter();

        spec.setTargetToModelConverters(new IConverter[] { c1, c2 });
        assertEquals(2, spec.getTargetToModelConverters().length);

        IConverter c3 = new Converter();
        spec.setTargetToModelConverter(c3);
        assertEquals(1, spec.getTargetToModelConverters().length);
        assertSame(c3, spec.getTargetToModelConverter());
        assertSame(c3, spec.getTargetToModelConverters()[0]);
    }

    /**
     * Asserts that when <code>null</code> getTargetToModelConverter() will
     * return <code>null</code> and getTargetToModelConverters() returns an
     * empty array.
     * 
     */
    public void testGetNullTargetToModelConverter() {
        BindSpec spec = new BindSpec();
        spec.setTargetToModelConverter(null);
        assertNull(spec.getTargetToModelConverter());
        assertEquals(0, spec.getTargetToModelConverters().length);
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
        public ValidationError isPartiallyValid(Object value) {
            return null;
        }

        public ValidationError isValid(Object value) {
            return null;
        }
    }

    private class DomainValidator implements IDomainValidator {
        public ValidationError isValid(Object value) {
            return null;
        }
    }
}
