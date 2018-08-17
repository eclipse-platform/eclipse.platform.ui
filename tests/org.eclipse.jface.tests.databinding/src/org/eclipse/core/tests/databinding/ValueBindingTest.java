/*******************************************************************************
 * Copyright (c) 2006, 2018	 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bugs 116920, 164653, 159768
 *     Matthew Hall - bugs 260329, 271148
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.eclipse.core.databinding.UpdateValueStrategy.POLICY_NEVER;
import static org.eclipse.core.databinding.UpdateValueStrategy.POLICY_UPDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class ValueBindingTest extends AbstractDefaultRealmTestCase {
	private WritableValue<Object> target;

	private WritableValue<String> model;

	private DataBindingContext dbc;
	private Binding binding;

	private List<String> log;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		target = WritableValue.withValueType(String.class);
		model = WritableValue.withValueType(String.class);
		dbc = new DataBindingContext();
		log = new ArrayList<>();
	}

	@Override
	public void tearDown() throws Exception {
		dbc.dispose();
		model.dispose();
		target.dispose();
	}

	/**
	 * Bug 152543.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNoUpdateTargetFromModel() throws Exception {
		try {
			new DataBindingContext().bindValue(new ObservableValueStub<>(), new ObservableValueStub<>(),
					new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER),
					new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testValuePropagation() throws Exception {
		String initialValue = "value";
		model.setValue(initialValue);

		assertFalse(model.getValue().equals(target.getValue()));
		dbc.bindValue(target, model);

		assertEquals(target.getValue(), model.getValue());
	}

	@Test
	public void testGetTarget() throws Exception {
		Binding binding = dbc.bindValue(target, model);

		assertEquals(target, binding.getTarget());
	}

	@Test
	public void testGetModel() throws Exception {
		Binding binding = dbc.bindValue(target, model);

		assertEquals(model, binding.getModel());
	}

	@Test
	public void testOKStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc.bindValue(target, model);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertEquals("value copied to model", value, model.getValue());
		assertTrue(binding.getValidationStatus().getValue().isOK());
	}

	@Test
	public void testWarningStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy<Object, String>().setAfterGetValidator(warningValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertEquals("value copied to model", value, model.getValue());
		assertEquals("warning status", IStatus.WARNING, binding.getValidationStatus().getValue().getSeverity());
	}

	@Test
	public void testInfoStatusInValidationUpdatesModel() throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy<Object, String>().setAfterGetValidator(infoValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertEquals("value copied to model", value, model.getValue());
		assertEquals("info status", IStatus.INFO, binding
				.getValidationStatus().getValue().getSeverity());
	}

	@Test
	public void testErrorStatusInValidationDoesNotUpdateModel()
			throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy<Object, String>()
						.setAfterGetValidator(errorValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertFalse("value not copied to model", value.equals(model.getValue()));
		assertEquals("error status", IStatus.ERROR, binding
				.getValidationStatus().getValue().getSeverity());
	}

	@Test
	public void testCancelStatusInValidationDoesNotUpdateModel()
			throws Exception {
		Binding binding = dbc.bindValue(target, model,
				new UpdateValueStrategy<Object, String>().setAfterGetValidator(cancelValidator()), null);

		String value = "value";
		assertFalse(value.equals(model.getValue()));
		target.setValue(value);

		assertFalse("value not copied to model", value.equals(model.getValue()));
		assertEquals("cancel status", IStatus.CANCEL, binding
				.getValidationStatus().getValue().getSeverity());
	}

	@Test
	public void testStatusesFromEveryPhaseAreReturned() throws Exception {
		UpdateValueStrategy<Object, String> strategy = new UpdateValueStrategy<Object, String>() {
			@Override
			protected IStatus doSet(IObservableValue<? super String> observableValue, String value) {
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
		IStatus status = binding.getValidationStatus().getValue();
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

	@Test
	public void testStatusIsInstanceOfBindingStatus() throws Exception {
		Binding binding = dbc.bindValue(target, model);
		assertTrue(binding.getValidationStatus().getValue() instanceof BindingStatus);
	}

	@Test
	public void testDiffsAreCheckedForEqualityBeforeUpdate() throws Exception {
		class WritableValueStub extends WritableValue<String> {
			public WritableValueStub() {
				super("", String.class);
			}

			@Override
			protected void fireValueChange(ValueDiff<String> diff) {
				super.fireValueChange(diff);
			}
		}

		WritableValueStub target = new WritableValueStub();
		WritableValue<String> model = WritableValue.withValueType(String.class);

		class Strategy extends UpdateValueStrategy<String, String> {
			int afterGetCount;

			@Override
			public IStatus validateAfterGet(String value) {
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

	@Test
	public void testPostInit_UpdatePolicy_UpdateToTarget_UpdateToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_UPDATE),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_UPDATE));
		assertEquals(Arrays.asList(new String[] { "model-get", "model-convert",
				"model-after-convert", "target-before-set", "target-set",
				"target-get", "target-convert", "target-after-convert",
				"model-before-set" }), log);
	}

	@Test
	public void testPostInit_UpdatePolicy_UpdateToTarget_ConvertToModel() {
		bindLoggingValue(
				loggingTargetToModelStrategy(UpdateValueStrategy.POLICY_CONVERT),
				loggingModelToTargetStrategy(UpdateValueStrategy.POLICY_UPDATE));
		assertEquals(Arrays.asList(new String[] { "model-get", "model-convert",
				"model-after-convert", "target-before-set", "target-set",
				"target-get", "target-convert", "target-after-convert",
				"model-before-set" }), log);
	}

	@Test
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

	@Test
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

	@Test
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
		model.setValue("dummy model value");
		assertEquals(Arrays.asList(new String[] { "model-set", "model-get",
				"model-convert", "model-after-convert" }), log);
	}

	/**
	 * test for bug 491678
	 */
	@Test
	public void testTargetValueIsSyncedToModelIfModelWasNotSyncedToTarget() {
		bindLoggingValue(new UpdateValueStrategy<>(true, POLICY_UPDATE), new UpdateValueStrategy<>(true, POLICY_NEVER));
		assertEquals(model.getValue(), target.getValue());
	}

	private void bindLoggingValue(UpdateValueStrategy<Object, String> targetToModel,
			UpdateValueStrategy<String, Object> modelToTarget) {
		// Set model and target to different values to ensure we get a change
		// notification when the binding is first created.
		model.setValue("1");
		target.setValue("2");

		target.addValueChangeListener(event -> log.add("target-set"));
		model.addValueChangeListener(event -> log.add("model-set"));
		binding = dbc.bindValue(target, model, targetToModel, modelToTarget);
	}

	private UpdateValueStrategy<String, Object> loggingModelToTargetStrategy(int updatePolicy) {
		return new UpdateValueStrategy<String, Object>(updatePolicy)
				.setAfterGetValidator(loggingValidator(log, "model-get"))
				.setConverter(loggingConverter(log, "model-convert"))
				.setAfterConvertValidator(loggingValidator(log, "model-after-convert"))
				.setBeforeSetValidator(loggingValidator(log, "target-before-set"));
	}

	private UpdateValueStrategy<Object, String> loggingTargetToModelStrategy(int updatePolicy) {
		return new UpdateValueStrategy<Object, String>(updatePolicy)
				.setAfterGetValidator(loggingValidator(log, "target-get"))
				.setConverter(loggingConverter(log, "target-convert"))
				.setAfterConvertValidator(loggingValidator(log, "target-after-convert"))
				.setBeforeSetValidator(loggingValidator(log, "model-before-set"));
	}

	private IValidator<Object> loggingValidator(final List<String> log, final String message) {
		return value -> {
			log.add(message);
			return ValidationStatus.ok();
		};
	}

	private <F, T> IConverter<F, T> loggingConverter(final List<String> log, final String message) {
		return new Converter<F, T>(null, null) {
			@SuppressWarnings("unchecked")
			@Override
			public T convert(F fromObject) {
				log.add(message);
				return (T) fromObject;
			}
		};
	}

	private IValidator<Object> warningValidator() {
		return value -> ValidationStatus.warning("");
	}

	private IValidator<Object> infoValidator() {
		return value -> ValidationStatus.info("");
	}

	private IValidator<Object> errorValidator() {
		return value -> ValidationStatus.error("");
	}

	private IValidator<Object> cancelValidator() {
		return value -> ValidationStatus.cancel("");
	}

	private static class ObservableValueStub<T> extends AbstractObservableValue<T> {
		@Override
		protected T doGetValue() {
			// do nothing
			return null;
		}

		@Override
		public Object getValueType() {
			// do nothing
			return null;
		}

		@Override
		protected void doSetValue(T value) {

		}
	}
}
