/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159539
 *     Brad Reynolds - bug 140644
 *     Brad Reynolds - bug 159940
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.BindSpec;
import org.eclipse.jface.databinding.Binding;
import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.databinding.observable.Realm;
import org.eclipse.jface.databinding.observable.list.IListChangeListener;
import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.list.ListDiff;
import org.eclipse.jface.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.ListBinding;
import org.eclipse.jface.internal.databinding.internal.ValueBinding;
import org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Byte;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.conversion.IdentityConverter;
import org.eclipse.jface.internal.databinding.provisional.conversion.ToStringConverter;
import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.swt.widgets.Display;

public class DatabindingContextTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
        
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
	}

    public void testDisposeBindings() throws Exception {
        DataBindingContext dbc = new DataBindingContext();
        
        Binding binding = new BindingStub(dbc);
        dbc.addBinding(binding);
        
        assertFalse(binding.isDisposed());
        dbc.dispose();
        assertTrue("binding should be diposed when dbc is disposed", binding.isDisposed());
    }
    
    public void testDisposeChildContexts() throws Exception {
        DataBindingContext dbc = new DataBindingContext();
        
        DataBindingContext child = new DataBindingContext(dbc, Realm.getDefault(), new BindSupportFactory[0]);
        Binding binding = new BindingStub(child);
        child.addBinding(binding);
        
        assertFalse(binding.isDisposed());
        dbc.dispose();
        assertTrue("binding should be disposed when a parent context is disposed", binding.isDisposed());
    }

    public void testBindValue() throws Exception {
        DataBindingContext dbc = new DataBindingContext();
        IObservableValue target = new WritableValue(String.class);
        IObservableValue model = new WritableValue(String.class);
        
        Binding binding = dbc.bindValue(target, model, null);
        assertTrue("binding is of the incorrect type", binding instanceof ValueBinding);
    }
    
    public void testBindList() throws Exception {
        DataBindingContext dbc = new DataBindingContext();
        IObservableList target = new WritableList();
        IObservableList model = new WritableList();
        
        Binding binding = dbc.bindList(target, model, null);
        assertTrue("binding is of the incorrect type", binding instanceof ListBinding);
    }

	
	public void testFillBindSpecDefaultsMultipleConvertersAndValidators() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		
		BindSpec bs = new BindSpec();
		bs.setModelToTargetConverters(new IConverter[] {
				null, new ToStringConverter(), null
		});
		bs.setTargetToModelConverters(new IConverter[] {
				null, new ConvertString2Byte(), null
		});
		
		dbc.fillBindSpecDefaults(dbc, bs, Object.class, Object.class);
		
		assertConverterType(bs, 0, IdentityConverter.class, bs.getModelToTargetConverters());
		assertConverterType(bs, 1, ToStringConverter.class, bs.getModelToTargetConverters());
		assertConverterType(bs, 2, IdentityConverter.class, bs.getModelToTargetConverters());

		assertConverterType(bs, 0, IdentityConverter.class, bs.getTargetToModelConverters());
		assertConverterType(bs, 1, ConvertString2Byte.class, bs.getTargetToModelConverters());
		assertConverterType(bs, 2, IdentityConverter.class, bs.getTargetToModelConverters());
	}

	public void testWithDefaults() throws Exception {        
        DataBindingContext dbc = new DataBindingContext();
        assertNotNull("context was not initialized with defaults", dbc.createConverter(String.class, String.class));
	}

    private void assertConverterType(BindSpec bs, int element, Class clazz, IConverter[] converters) {
		assertEquals("model2target[" + element + "] = identity", clazz, converters[element].getClass());
	}
    
    /**
     * Asserts that ValidationError is populated and change events are fired
     * when a Binding that is associated with a context is in error.
     * 
     * @throws Exception
     */
    public void testValidationError() throws Exception {
        WritableValue targetObservable = new WritableValue(String.class);
        WritableValue modelObservable = new WritableValue(String.class);

        final String errorMessage = "error";
        org.eclipse.jface.databinding.DataBindingContext dbc = new org.eclipse.jface.databinding.DataBindingContext();
        ValueChangeCounter errorCounter = new ValueChangeCounter();
        ListChangeCounter errorsCounter = new ListChangeCounter();

        IObservableValue error = dbc.getValidationError();
        error.addValueChangeListener(errorCounter);
        assertNull(error.getValue());

        IObservableList errors = dbc.getValidationErrors();
        errors.addListChangeListener(errorsCounter);
        assertEquals(0, errors.size());

        IValidator validator = new IValidator() {
            public ValidationError isPartiallyValid(Object value) {
                return null;
            }

            public ValidationError isValid(Object value) {
                return ValidationError.error(errorMessage);
            }
        };

        dbc.bindValue(targetObservable,
                modelObservable,
                new org.eclipse.jface.databinding.BindSpec().setValidator(validator));

        targetObservable.setValue("");
        assertNotNull(error.getValue());
        assertEquals(errorMessage, error.getValue().toString());
        assertEquals(1, errors.size());
        assertEquals(1, errorsCounter.count);
        assertEquals(1, errorCounter.count);
    }

    /**
     * Asserts that then
     * {@link DataBindingContext#bindValue(IObservableValue, IObservableValue, org.eclipse.jface.databinding.BindSpec)}
     * if invoked the created binding is added to the internal list of bindings.
     * 
     * @throws Exception
     */
    public void testBindValueAddBinding() throws Exception {
        WritableValue targetValue = new WritableValue(String.class);
        WritableValue modelValue = new WritableValue(String.class);

        org.eclipse.jface.databinding.DataBindingContext dbc = new org.eclipse.jface.databinding.DataBindingContext();
        assertNotNull(dbc.getBindings());
        assertEquals(0, dbc.getBindings().size());

        org.eclipse.jface.databinding.Binding binding = dbc.bindValue(targetValue, modelValue, null);
        assertNotNull(binding);
        assertNotNull(dbc.getBindings());
        assertEquals(1, dbc.getBindings().size());
        assertEquals(binding, dbc.getBindings().get(0));
    }

    /**
     * Asserts that when
     * {@link DataBindingContext#bindList(IObservableList, IObservableList, org.eclipse.jface.databinding.BindSpec)}
     * is invoked the created binding is added to the intenal list of bindings.
     * 
     * @throws Exception
     */
    public void testBindListAddBinding() throws Exception {
        WritableList targetList = new WritableList(Object.class);
        WritableList modelList = new WritableList(Object.class);

        org.eclipse.jface.databinding.DataBindingContext dbc = new org.eclipse.jface.databinding.DataBindingContext();
        assertNotNull(dbc.getBindings());
        assertEquals(0, dbc.getBindings().size());

        org.eclipse.jface.databinding.Binding binding = dbc.bindList(targetList, modelList, null);
        assertNotNull(binding);
        assertNotNull(dbc.getBindings());
        assertEquals(1, dbc.getBindings().size());
        assertEquals(binding, dbc.getBindings().get(0));
    }

    public void testGetBindingsImmutability() throws Exception {
        org.eclipse.jface.databinding.DataBindingContext dbc = new org.eclipse.jface.databinding.DataBindingContext();
        BindingStub binding = new BindingStub(null);
        dbc.addBinding(binding);

        try {
            dbc.getBindings().remove(0);
            fail("exception should have been thrown");
        } catch (UnsupportedOperationException e) {
        }
    }
    
    public void testRemoveBinding() throws Exception {
        BindingStub binding = new BindingStub(null);
        org.eclipse.jface.databinding.DataBindingContext dbc = new org.eclipse.jface.databinding.DataBindingContext();
        dbc.addBinding(binding);
        
        assertTrue("context should contain the binding", dbc.getBindings().contains(binding));
        assertTrue("removing the factory should return true", dbc.removeBinding(binding));
        assertFalse("binding should have been removed", dbc.getBindings().contains(binding));
        assertFalse("when not found false should be returned", dbc.removeBinding(binding));
    }
    
    /**
     * {@link IValueChangeListener} implementation that counts the times
     * handleValueChange(...) is invoked.
     * 
     * @since 3.2
     */
    private static class ValueChangeCounter implements IValueChangeListener {
        int count;

        public void handleValueChange(IObservableValue source, ValueDiff diff) {
            count++;
        }
    }

    /**
     * {@link IListChangeListener} implementation that counts the times
     * handleListChange(...) is invoked.
     * 
     */
    private static class ListChangeCounter implements IListChangeListener {
        int count;

        public void handleListChange(IObservableList source, ListDiff diff) {
            count++;
        }
    }
    
    private static class BindingStub extends org.eclipse.jface.databinding.Binding {
        org.eclipse.jface.databinding.DataBindingContext context;

        public BindingStub(org.eclipse.jface.databinding.DataBindingContext context) {
            super(context);
        }
        
        public IObservableValue getPartialValidationError() {
            return null;
        }

        public IObservableValue getValidationError() {
            return null;
        }

        public void updateModelFromTarget() {            
        }

        public void updateTargetFromModel() {            
        }
    }
}
