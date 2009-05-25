/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bugs 116920, 164653, 159768
 *     Matthew Hall - bugs 260329, 271148
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
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
	private Binding binding;

	private List log;

	protected void setUp() throws Exception {
		super.setUp();

		target = WritableValue.withValueType(String.class);
		model = WritableValue.withValueType(String.class);
		dbc = new DataBindingContext();
		log = new ArrayList();
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
		dbc.bindValue(target, model);

		assertEquals(target.getValue(), model.getValue());
	}

	public void testGetTarget() throws Exception {
		Binding binding = dbc.bindValue(target, model);

		assertEquals(target, binding.getTarget());
	}

	public void testGetModel() throws Exception {
		Binding binding = dbc.bindValue(target, model);

		assertEquals(model, binding.getModel());
	}

	public void testOKStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc.bindValue(target, model);

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
		Binding binding = dbc.bindValue(target, model);
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

	public void testPostInit_UpdatePolicy_UpdateToTarget_UpdateToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_UPDATE),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_UPDATE));
		assertEquals(Arrays.asList(new String[] { "model-get", "model-convert",
				"model-after-convert", "target-before-set", "target-set",
				"target-get", "target-convert", "target-after-convert",
				"model-before-set" }), log);
	}

	public void testPostInit_UpdatePolicy_UpdateToTarget_ConvertToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_CONVERT),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_UPDATE));
		assertEquals(Arrays.asList(new String[] { "model-get", "model-convert",
				"model-after-convert", "target-before-set", "target-set",
				"target-get", "target-convert", "target-after-convert",
				"model-before-set" }), log);
	}

	public void testPostInit_UpdatePolicy_UpdateToTarget_OnRequestToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_ON_REQUEST),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_UPDATE));
		assertEquals(Arrays.asList(new String[] { "model-get", "model-convert",
				"model-after-convert", "target-before-set", "target-set" }),
				log);

		log.clear();
		target.setValue(new Object());
		assertEquals(Collections.singletonList("target-set"), log);

		log.clear();
		binding.validateTargetToModel();
		assertEquals(
				Arrays.asList(new String[] { "target-get", "target-convert",
						"target-after-convert", "model-before-set" }), log);

		log.clear();
		binding.updateTargetToModel();
		assertEquals(Arrays.asList(new String[] { "target-get",
				"target-convert", "target-after-convert", "model-before-set",
				"model-set" }), log);
	}

	public void testPostInit_UpdatePolicy_UpdateToTarget_NeverToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_NEVER),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_UPDATE));
		assertEquals(Arrays.asList(new String[] { "model-get", "model-convert",
				"model-after-convert", "target-before-set", "target-set" }),
				log);

		log.clear();
		target.setValue(new Object());
		assertEquals(Collections.singletonList("target-set"), log);

		log.clear();
		binding.validateTargetToModel();
		assertEquals(Collections.EMPTY_LIST, log);

		log.clear();
		binding.updateTargetToModel();
		assertEquals(Collections.EMPTY_LIST, log);
	}

	public void testPostInit_UpdatePolicy_ConvertToTarget_UpdateToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_UPDATE),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_CONVERT));
		assertEquals(
				Arrays.asList(new String[] { "model-get", "model-convert",
						"model-after-convert", "target-before-set",
						"target-get", "target-convert", "target-after-convert",
						"model-before-set" }), log);

		log.clear();
		target.setValue(new Object());
		assertEquals(Arrays.asList(new String[] { "target-set", "target-get",
				"target-convert", "target-after-convert", "model-before-set",
				"model-set" }), log);

		log.clear();
		model.setValue(new Object());
		assertEquals(Arrays.asList(new String[] { "model-set", "model-get",
				"model-convert", "model-after-convert" }), log);
	}

	private void bindLoggingValue(UpdateValueStrategy targetToModel,
			UpdateValueStrategy modelToTarget) {
		// Set model and target to different values to ensure we get a change
		// notification when the binding is first created.
		model.setValue("1");
		target.setValue("2");

		target.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				log.add("target-set");
			}
		});
		model.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				log.add("model-set");
			}
		});
		binding = dbc.bindValue(target, model, targetToModel, modelToTarget);
	}

	private UpdateValueStrategy loggingModelToTargetStrategy(int updatePolicy) {
		return new UpdateValueStrategy(updatePolicy).setAfterGetValidator(
				loggingValidator(log, "model-get")).setConverter(
				loggingConverter(log, "model-convert"))
				.setAfterConvertValidator(
						loggingValidator(log, "model-after-convert"))
				.setBeforeSetValidator(
						loggingValidator(log, "target-before-set"));
	}

	private UpdateValueStrategy loggingTargetToModelStrategy(int updatePolicy) {
		return new UpdateValueStrategy(updatePolicy).setAfterGetValidator(
				loggingValidator(log, "target-get")).setConverter(
				loggingConverter(log, "target-convert"))
				.setAfterConvertValidator(
						loggingValidator(log, "target-after-convert"))
				.setBeforeSetValidator(
						loggingValidator(log, "model-before-set"));
	}

	private IValidator loggingValidator(final List log, final String message) {
		return new IValidator() {
			public IStatus validate(Object value) {
				log.add(message);
				return ValidationStatus.ok();
			}
		};
	}

	private IConverter loggingConverter(final List log, final String message) {
		return new Converter(null, null) {
			public Object convert(Object fromObject) {
				log.add(message);
				return fromObject;
			}
		};
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
