/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingAdapter;
import org.eclipse.jface.internal.databinding.provisional.BindingEvent;
import org.eclipse.jface.internal.databinding.provisional.BindingException;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Byte;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.conversion.IdentityConverter;
import org.eclipse.jface.internal.databinding.provisional.conversion.ToStringConverter;
import org.eclipse.jface.internal.databinding.provisional.description.NestedProperty;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.NestedObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.provisional.observable.value.AbstractObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.jface.tests.databinding.util.Mocks;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DatabindingContextTest extends TestCase {

	boolean failed = false;

	DataBindingContext dbc;

	IObservableValue observableValueRMock;

	IValidator validatorMock;

	WritableValue settableValue1;

	WritableValue settableValue2;

	Object o1 = new Object();

	Object o2 = new Object();

	private static IConverter identityConverter = new IdentityConverter(
			Object.class);

	protected void setUp() throws Exception {
		super.setUp();
		dbc = DataBindingContext.createContext(new IObservableFactory[0]);
		observableValueRMock = (IObservableValue) Mocks
				.createRelaxedMock(IObservableValue.class);
		validatorMock = (IValidator) Mocks.createMock(IValidator.class);
		settableValue1 = new WritableValue(Object.class);
		settableValue2 = new WritableValue(Object.class);
	}

	protected void tearDown() throws Exception {
		if (!failed) {
			Mocks.verify(observableValueRMock);
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

	public void testRegisterForDispose() {
		final boolean[] disposeCalled = new boolean[] { false };
		IObservableValue target = new WritableValue(Integer.TYPE) {
			public void dispose() {
				super.dispose();
				disposeCalled[0] = true;
			}
		};
		WritableValue model = new WritableValue(Integer.TYPE);
		model.setValue(new Integer(12));
		Display display = new Display();
		Shell shell = new Shell(display);
		final DataBindingContext dbc = DataBindingContext
				.createContext(new IObservableFactory[] {});
		registerContextToDispose(shell, dbc);
		dbc.registerForDispose(target);
		dbc.registerForDispose(model);
		dbc.bind(target, model, null);
		assertEquals("target should now have model's value", 12,
				((Integer) target.getValue()).intValue());
		target.setValue(new Integer(9));
		assertEquals("model should now have target's value", 9,
				((Integer) model.getValue()).intValue());
		shell.dispose();
		display.dispose();
		assertTrue("dispose should have been called", disposeCalled[0]);
	}

	private class DisposableObservable extends AbstractObservableValue {
		protected Object computeValue() {
			return null;
		}

		public void setValue(Object value) {
		}

		public Object getValueType() {
			return Object.class;
		}

		protected Object doGetValue() {
			return null;
		}

		boolean isDisposed = false;

		public void dispose() {
			super.dispose();
			isDisposed = true;
		}

		public boolean isDisposed() {
			return isDisposed;
		}
	}

	private class DisposableObservableFactory implements IObservableFactory {
		public IObservable createObservable(Object description) {
			return new DisposableObservable();
		}
	}

	public void testDisposeCalled() {
		Display display = new Display();
		Shell shell = new Shell(display);
		DataBindingContext dbc = DataBindingContext
				.createContext(new IObservableFactory[] { new DisposableObservableFactory() });
		registerContextToDispose(shell, dbc);
		DisposableObservable u = (DisposableObservable) dbc
				.createObservable(null);
		assertFalse("is not disposed", u.isDisposed());
		shell.dispose();
		display.dispose();
		assertTrue("is disposed", u.isDisposed());
	}

	private void registerContextToDispose(Shell shell,
			final DataBindingContext dbc) {
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dbc.dispose();
			}
		});
	}

	public void testBindValueModel() {
		Mocks.reset(observableValueRMock);
		observableValueRMock.addValueChangeListener(null);
		observableValueRMock.getValue();
		observableValueRMock.getValueType();
		Mocks.setLastReturnValue(observableValueRMock, Object.class);
		validatorMock.isValid(null);
		Mocks.startChecking(observableValueRMock);
		Mocks.startChecking(validatorMock);
		dbc.bind(settableValue1, observableValueRMock, new BindSpec(
				identityConverter, identityConverter, validatorMock, null));
		Mocks.verify(observableValueRMock);
	}

	public void testBindValueTarget() {
		observableValueRMock.addValueChangeListener(null);
		observableValueRMock.setValue(null);
		observableValueRMock.getValue();
		observableValueRMock.getValueType();
		Mocks.setLastReturnValue(observableValueRMock, Object.class);
		validatorMock.isValid(null);
		Mocks.startChecking(observableValueRMock);
		Mocks.startChecking(validatorMock);
		dbc.bind(observableValueRMock, settableValue2, new BindSpec(
				identityConverter, identityConverter, validatorMock, null));
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

	public void testBindingListeners() {
		final int[] calls = new int[] { 0, 0 };
		// this exact sequence of positions are not API and may change from
		// release to release.
		// This is just here to check that we got a sane sequence of pipeline
		// positions
		// and to catch when the sequence changes when we don't expect it to
		// change.
		//
		// See BindingEvent#pipelinePosition for details.
		final int[] pipelinePositions = new int[] { 0, 1, 2, 3, 4, 0, 2, 4, 1,
				0, 1, 2, 0, 2, 4, 1 };
		settableValue1.setValue(o1);
		settableValue2.setValue(o2);
		Binding binding = dbc.bind(settableValue1, settableValue2, null);
		binding.addBindingEventListener(new BindingAdapter() {
			public ValidationError bindingEvent(BindingEvent e) {
				// Make sure we get the right sequence of pipeline positions
				assertEquals("Unexpected pipeline position at call #"
						+ calls[0], pipelinePositions[calls[0]],
						e.pipelinePosition);
				calls[0]++;
				return null;
			}
		});
		binding.addBindingEventListener(new BindingAdapter() {
			public ValidationError bindingEvent(BindingEvent e) {
				calls[1]++;
				return null;
			}
		});
		assertEquals(o2, settableValue1.getValue());
		assertEquals(
				"Both binding events should be called the same number of times",
				calls[0], calls[1]);
		settableValue1.setValue(o1);
		assertEquals(o1, settableValue2.getValue());
		assertEquals(
				"Both binding events should be called the same number of times",
				calls[0], calls[1]);
		settableValue2.setValue(o2);
		assertEquals(
				"Both binding events should be called the same number of times",
				calls[0], calls[1]);
		assertEquals(o2, settableValue1.getValue());

		// Now test forcing an error from the event handler...
		binding.addBindingEventListener(new BindingAdapter() {
			public ValidationError bindingEvent(BindingEvent e) {
				if (e.pipelinePosition == BindingEvent.PIPELINE_AFTER_CONVERT) {
					return ValidationError.error("error");
				}
				return null;
			}
		});
		settableValue1.setValue(o1);
		settableValue2.setValue(o2);
		assertEquals(
				"Both binding events should be called the same number of times",
				calls[0], calls[1]);
		assertEquals("binding events should be called at least once", true,
				calls[0] > 0);
	}

	public void testCollectionBindingListeners() {

		WritableList v1 = new WritableList();
		WritableList v2 = new WritableList();

		Binding binding = dbc.bind(v1, v2, null);
		final int[] calls = new int[] { 0 };
		binding.addBindingEventListener(new BindingAdapter() {
			public ValidationError bindingEvent(BindingEvent e) {
				calls[0]++;
				return null;
			}
		});

		v2.add(0, "test");
		assertBindingCalls(calls);
		v2.remove(0);
		assertBindingCalls(calls);
		v2.add(0, "test2");
		assertBindingCalls(calls);
		v2.set(0, "test3");
		assertBindingCalls(calls);
	}

	private void assertBindingCalls(final int[] calls) {
		assertTrue("Should have seen some binding event calls", calls[0] > 0);
		calls[0] = 0;
	}

	public void testCreateNestedObservableWithArrays() {
		// String parentObject = "";
		// NestedProperty nestedProperty = new NestedProperty(parentObject, new
		// String[] {"nestedChild1", "nestedChild2", "foo"}, new Class[]
		// {Integer.class, String.class, Float.class});
		// DataBindingContext ctx = DataBinding.createContext(new
		// IObservableFactory[] {new MockObservableFactory(), new
		// NestedObservableFactory()});
		// INestedObservableValue observableValue = (INestedObservableValue)
		// ctx.createObservable(nestedProperty);
		// assertEquals("The child IObservable does not have the right type.",
		// Float.class, observableValue.getValueType());
		//
		// observableValue = ((INestedObservableValue)
		// observableValue.getOuterObservableValue());
		// assertEquals("The child IObservable does not have the right type.",
		// String.class, observableValue.getValueType());
		//	
		// MockObservableValue v = ((MockObservableValue)
		// observableValue.getOuterObservableValue());
		// assertEquals("The child IObservable does not have the right getter.",
		// "nestedChild1", v.getDescription());
		// assertSame("The child IObservable does not have a correct parent
		// target object.", parentObject, v.getOuterObservableValue());
		// assertEquals("The child IObservable does not have the right type.",
		// Integer.class, v.getType());
	}

	public void testCreateNestedObservableWithPrototypeClass() {
		// String parentObject = "";
		// NestedProperty nestedProperty = new NestedProperty(parentObject,
		// "nestedChild1.nestedChild2.foo", NestedParent.class);
		// DataBindingContext ctx = DataBinding.createContext(new
		// IObservableFactory[] {new MockObservableFactory(), new
		// NestedObservableFactory()});
		// INestedObservableValue observableValue = (INestedObservableValue)
		// ctx.createObservable(nestedProperty);
		// assertEquals("The child IObservable does not have the right type.",
		// String.class, observableValue.getValueType());
		//
		// observableValue = ((INestedObservableValue)
		// observableValue.getOuterObservableValue());
		// assertEquals("The child IObservable does not have the right type.",
		// NestedChild2.class, observableValue.getValueType());
		//	
		// MockObservableValue v = ((MockObservableValue)
		// observableValue.getOuterObservableValue());
		// assertEquals("The child IObservable does not have the right getter.",
		// "nestedChild1", v.getDescription());
		// assertSame("The child IObservable does not have a correct parent
		// target object.", parentObject, v.getOuterObservableValue());
		// assertEquals("The child IObservable does not have the right type.",
		// NestedChild1.class, v.getType());
	}

	public void testCreateNestedObservableWithPrototypeClassAndInvalidPath() {
		String parentObject = "";
		NestedProperty nestedProperty = new NestedProperty(parentObject,
				"nestedChild1.nestedChild3.foo", NestedParent.class);
		try {
			DataBindingContext ctx = new DataBindingContext();
			ctx.addObservableFactory(new MockObservableFactory());
			ctx.addObservableFactory(new NestedObservableFactory(ctx));
			ctx.createObservable(nestedProperty);
			fail("Expected binding exception.");
		} catch (BindingException be) {
		}
	}
	
	public void testFillBindSpecDefaultsMultipleConvertersAndValidators() throws Exception {
		DataBindingContext dbc = new DataBindingContext();
		dbc.addBindSupportFactory(new DefaultBindSupportFactory());
		
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

	private void assertConverterType(BindSpec bs, int element, Class clazz, IConverter[] converters) {
		assertEquals("model2target[" + element + "] = identity", clazz, converters[element].getClass());
	}
	
	//-------------------------------------------------------------------------
	// Fixture classes
	//-------------------------------------------------------------------------
	
	public class MockObservableFactory implements IObservableFactory {
		public IObservable createObservable(Object description) {
			Property property = (Property) description;
			return new MockObservableValue(property.getObject(), property
					.getPropertyID(), property.getPropertyType());
		}
	}

	public class MockObservableValue extends AbstractObservableValue {
		public Object targetObject;

		public Object description;

		private Class type;

		public MockObservableValue(Object targetObject, Object description,
				Class type) {
			super();
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

		public Object getOuterObservableValue() {
			return targetObject;
		}

		public Object computeValue() {
			return null;
		}

		public Object getValueType() {
			return null;
		}

		public void setValue(Object value) {
		}

		protected Object doGetValue() {
			return null;
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
