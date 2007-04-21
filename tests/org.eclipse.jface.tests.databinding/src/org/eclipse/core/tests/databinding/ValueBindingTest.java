/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164653, 159768
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class ValueBindingTest extends AbstractDefaultRealmTestCase {
	private WritableValue target;

	private WritableValue model;

	private DataBindingContext dbc;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		target = WritableValue.withValueType(String.class);
		model = WritableValue.withValueType(String.class);
		dbc = new DataBindingContext();
	}

	/**
	 * Bug 152543.
	 * 
	 * @throws Exception
	 */
	public void testNoUpdateTargetFromModel() throws Exception {
		try {
			new DataBindingContext().bindValue(new ObservableValueStub(),
					new ObservableValueStub(), new UpdateValueStrategy(
							UpdateValueStrategy.POLICY_NEVER),
					new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testValuePropagation() throws Exception {
		String initialValue = "value";
		model.setValue(initialValue);

		assertFalse(model.getValue().equals(target.getValue()));
		dbc.bindValue(target, model, null, null);

		assertEquals(target.getValue(), model.getValue());
	}

	public void testGetTarget() throws Exception {
		Binding binding = dbc.bindValue(target, model, null, null);

		assertEquals(target, binding.getTarget());
	}

	public void testGetModel() throws Exception {
		Binding binding = dbc.bindValue(target, model, null, null);

		assertEquals(model, binding.getModel());
	}

	public void testOKStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc.bindValue(target, model, null, null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertEquals("value copied to model", value, model.getValue());
		assertTrue(((IStatus) binding.getValidationStatus().getValue()).isOK());
	}

	public void testWarningStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy()
						.setAfterGetValidator(warningValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertEquals("value copied to model", value, model.getValue());
		assertEquals("warning status", IStatus.WARNING, ((IStatus) binding
				.getValidationStatus().getValue()).getSeverity());
	}

	public void testInfoStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc
				.bindValue(target, model, new UpdateValueStrategy()
						.setAfterGetValidator(infoValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertEquals("value copied to model", value, model.getValue());
		assertEquals("info status", IStatus.INFO, ((IStatus) binding
				.getValidationStatus().getValue()).getSeverity());
	}

	public void testErrorStatusInValidationDoesNotUpdateModel()
			throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy()
						.setAfterGetValidator(errorValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertFalse("value not copied to model", value.equals(model.getValue()));
		assertEquals("error status", IStatus.ERROR, ((IStatus) binding
				.getValidationStatus().getValue()).getSeverity());
	}

	public void testCancelStatusInValidationDoesNotUpdateModel()
			throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy()
						.setAfterGetValidator(cancelValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertFalse("value not copied to model", value.equals(model.getValue()));
		assertEquals("cancel status", IStatus.CANCEL, ((IStatus) binding
				.getValidationStatus().getValue()).getSeverity());
	}

	public void testStatusesFromEveryPhaseAreReturned() throws Exception {
		UpdateValueStrategy strategy = new UpdateValueStrategy() {
			protected IStatus doSet(IObservableValue observableValue,
					Object value) {
				super.doSet(observableValue, value);
				return ValidationStatus.info("");
			}
		};

		strategy.setAfterGetValidator(warningValidator());
		strategy.setAfterConvertValidator(infoValidator());
		strategy.setBeforeSetValidator(warningValidator());

		Binding binding = dbc.bindValue(target, model, strategy, null);
		String value = "value";
		assertFalse(value.equals(model.getValue()));

		target.setValue(value);
		assertEquals(value, model.getValue());
		IStatus status = (IStatus) binding.getValidationStatus().getValue();
		assertTrue(status.isMultiStatus());
		assertEquals("max status", IStatus.WARNING, status.getSeverity());

		MultiStatus multiStatus = (MultiStatus) status;
		assertEquals(4, multiStatus.getChildren().length);
		IStatus[] children = multiStatus.getChildren();

		assertEquals("after get severity", IStatus.WARNING, children[0]
				.getSeverity());
		assertEquals("after convert severity", IStatus.INFO, children[1]
				.getSeverity());
		assertEquals("before set severity", IStatus.WARNING, children[2]
				.getSeverity());
		assertEquals("doSet severity", IStatus.INFO, children[3].getSeverity());
	}

	public void testStatusIsInstanceOfBindingStatus() throws Exception {
		Binding binding = dbc.bindValue(target, model, null, null);
		assertTrue(binding.getValidationStatus().getValue() instanceof BindingStatus);
	}
	
	public void testDiffsAreCheckedForEqualityBeforeUpdate() throws Exception {
		class WritableValueStub extends WritableValue {
			public WritableValueStub() {
				super("", String.class);
			}
			
			protected void fireValueChange(ValueDiff diff) {
				super.fireValueChange(diff);
			}
		}
		
		WritableValueStub target = new WritableValueStub();
		WritableValue model = WritableValue.withValueType(String.class);
		
		class Strategy extends UpdateValueStrategy {
			int afterGetCount;
			public IStatus validateAfterGet(Object value) {
				afterGetCount++;
				return super.validateAfterGet(value);
			}
		}
		
		Strategy strategy = new Strategy();
		dbc.bindValue(target, model, strategy, null);
		int count = strategy.afterGetCount;
		
		target.fireValueChange(Diffs.createValueDiff("", ""));
		assertEquals("update does not occur", count, strategy.afterGetCount);
	}
	
	private IValidator warningValidator() {
		return new IValidator() {
			public IStatus validate(Object value) {
				return ValidationStatus.warning("");
			}
		};
	}

	private IValidator infoValidator() {
		return new IValidator() {
			public IStatus validate(Object value) {
				return ValidationStatus.info("");
			}
		};
	}

	private IValidator errorValidator() {
		return new IValidator() {
			public IStatus validate(Object value) {
				return ValidationStatus.error("");
			}
		};
	}

	private IValidator cancelValidator() {
		return new IValidator() {
			public IStatus validate(Object value) {
				return ValidationStatus.cancel("");
			}
		};
	}

	private static class ObservableValueStub extends AbstractObservableValue {
		protected Object doGetValue() {
			// do nothing
			return null;
		}

		public Object getValueType() {
			// do nothing
			return null;
		}

		protected void doSetValue(Object value) {

		}
	}
}
