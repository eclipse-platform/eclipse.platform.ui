/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 *     Matthew Hall - bug 237884
 *     Ovidio Mallo - bugs 240590, 238909
 ******************************************************************************/

package org.eclipse.core.tests.databinding.validation;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.observable.ValidatedObservableValue;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class MultiValidatorTest extends AbstractDefaultRealmTestCase {
	private DependencyObservableValue dependency;
	private MultiValidator validator;
	private IObservableValue validationStatus;

	protected void setUp() throws Exception {
		super.setUp();
		dependency = new DependencyObservableValue(null, IStatus.class);
		validator = new MultiValidator() {
			protected IStatus validate() {
				return (IStatus) dependency.getValue();
			}
		};
		validationStatus = validator.getValidationStatus();
	}

	public void testConstructor_NullArgument() {
		try {
			new MultiValidator(null) {
				protected IStatus validate() {
					return null;
				}
			};
			fail("Expected AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public void testGetValidationStatus_NullResultYieldsOKStatus() {
		IStatus status = (IStatus) validationStatus.getValue();
		assertTrue(status.isOK()); // null -> OK
	}

	public void testGetValidationStatus_ExceptionThrownYieldsErrorStatus() {
		final RuntimeException e = new RuntimeException("message");
		validator = new MultiValidator() {
			protected IStatus validate() {
				throw e;
			}
		};
		assertEquals(ValidationStatus.error("message", e), validator
				.getValidationStatus().getValue());
	}

	public void testGetValidationStatus_TracksWithDependency() {
		IStatus newStatus = ValidationStatus.error("error");
		dependency.setValue(newStatus);
		assertEquals(newStatus, validationStatus.getValue());
	}

	public void testInit_AddsValidationProducer() {
		DataBindingContext dbc = new DataBindingContext();
		dbc.addValidationStatusProvider(validator);
		assertTrue(dbc.getValidationStatusProviders().contains(validator));
	}

	public void testObserveValidatedValue_NullArgument() {
		try {
			validator.observeValidatedValue(null);
			fail("Expected AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public void testObserveValidatedValue_WrongRealm() {
		Realm otherRealm = new CurrentRealm(true);
		try {
			validator.observeValidatedValue(new WritableValue(otherRealm));
			fail("Expected AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public void testObserveValidatedValue_ReturnValue() {
		WritableValue target = new WritableValue();
		ValidatedObservableValue validated = (ValidatedObservableValue) validator
				.observeValidatedValue(target);

		target.setValue(new Object());
		assertEquals(target.getValue(), validated.getValue());

		dependency.setValue(ValidationStatus.error("error"));
		assertFalse(validated.isStale());

		target.setValue(new Object());
		assertTrue(validated.isStale());
		assertFalse(target.getValue().equals(validated.getValue()));

		dependency.setValue(ValidationStatus.info("info")); // considered valid
		assertEquals(target.getValue(), validated.getValue());
		assertFalse(validated.isStale());
	}

	public void testBug237884_DisposeCausesNPE() {
		MultiValidator validator = new MultiValidator() {
			protected IStatus validate() {
				return ValidationStatus.ok();
			}
		};
		try {
			validator.dispose();
		} catch (NullPointerException e) {
			fail("Bug 237884: MultiValidator.dispose() causes NPE");
		}
	}

	public void testBug237884_MultipleDispose() {
		validator.dispose();
		validator.dispose();
	}

	public void testBug237884_Comment3_ValidationStatusAsDependencyCausesStackOverflow() {
		dependency = new DependencyObservableValue(new Object(), Object.class);
		validator = new MultiValidator() {
			private int counter;

			protected IStatus validate() {
				ObservableTracker.getterCalled(dependency);
				return ValidationStatus.info("info " + counter++);
			}
		};
		validationStatus = validator.getValidationStatus();

		// bug behavior: the validation status listener causes the validation
		// status observable to become a dependency of the validator.
		validationStatus.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				ObservableTracker.getterCalled(validationStatus);
			}
		});
		dependency.setValue(new Object());

		// at this point, because the validation status observable is a
		// dependency, changes to the validation status cause revalidation in an
		// infinite recursion.
		try {
			dependency.setValue(new Object());
		} catch (StackOverflowError e) {
			fail("Bug 237884: Accessing MultiValidator validation status from within listener "
					+ "causes infinite recursion");
		}
	}

	public void testBug237884_ValidationStatusListenerCausesLoopingDependency() {
		validationStatus.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				ObservableTracker.getterCalled(validationStatus);
			}
		});
		assertFalse(validator.getTargets().contains(validationStatus));
		// trigger revalidation
		dependency.setValue(ValidationStatus.info("info"));
		assertFalse(validator.getTargets().contains(validationStatus));
	}

	public void testBug237884_ValidationStatusAccessDuringValidationCausesLoopingDependency() {
		validator = new MultiValidator() {
			protected IStatus validate() {
				ObservableTracker.getterCalled(getValidationStatus());
				return (IStatus) dependency.getValue();
			}
		};
		// trigger revalidation
		dependency.setValue(ValidationStatus.info("info"));
		assertFalse(validator.getTargets().contains(validationStatus));
	}

	public void testBug240590_ValidationStatusSetWhileTrackingDependencies() {
		final IObservableValue noDependency = new WritableValue();
		validationStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				// Explicitly track the faked dependency.
				ObservableTracker.getterCalled(noDependency);
			}
		});

		// Trigger a validation change.
		dependency.setValue(ValidationStatus.error("new error"));

		// Make sure the faked dependency has not been included in the
		// dependency set (the validator's targets).
		assertFalse(validator.getTargets().contains(noDependency));
	}

	public void testValidationStaleness() {
		ValueChangeEventTracker validationChangeCounter = ValueChangeEventTracker
				.observe(validationStatus);

		StaleCounter validationStaleCounter = new StaleCounter();
		validationStatus.addStaleListener(validationStaleCounter);

		// Assert initial state.
		assertFalse(validationStatus.isStale());
		assertEquals(0, validationChangeCounter.count);
		assertEquals(0, validationStaleCounter.count);

		// Change to a stale state.
		dependency.setStale(true);
		assertTrue(validationStatus.isStale());
		assertEquals(0, validationChangeCounter.count);
		assertEquals(1, validationStaleCounter.count); // +1

		// The validation status is already stale so even if it gets another
		// stale event from its dependencies, it should not propagate that
		// event.
		dependency.fireStale();
		assertTrue(validationStatus.isStale());
		assertEquals(0, validationChangeCounter.count);
		assertEquals(1, validationStaleCounter.count);

		// Change the validation status while remaining stale.
		dependency.setValue(ValidationStatus.error("e1"));
		assertTrue(validationStatus.isStale());
		assertEquals(1, validationChangeCounter.count); // +1
		assertEquals(1, validationStaleCounter.count);

		// Move back to a non-stale state.
		dependency.setStale(false);
		assertFalse(dependency.isStale());
		assertFalse(validationStatus.isStale());
		assertEquals(2, validationChangeCounter.count); // +1
		assertEquals(1, validationStaleCounter.count);
	}

	public void testStatusValueChangeWhileValidationStale() {
		// Change to a stale state.
		dependency.setStale(true);
		assertTrue(validationStatus.isStale());

		// Even if the validation is stale, we want the current value to be
		// tracked.
		dependency.setValue(ValidationStatus.error("e1"));
		assertTrue(validationStatus.isStale());
		assertEquals(dependency.getValue(), validationStatus.getValue());
		dependency.setValue(ValidationStatus.error("e2"));
		assertTrue(validationStatus.isStale());
		assertEquals(dependency.getValue(), validationStatus.getValue());
	}

	public void testValidationStatusBecomesStaleThroughNewDependency() {
		final DependencyObservableValue nonStaleDependency = new DependencyObservableValue(
				ValidationStatus.ok(), IStatus.class);
		nonStaleDependency.setStale(false);

		final DependencyObservableValue staleDependency = new DependencyObservableValue(
				ValidationStatus.ok(), IStatus.class);
		staleDependency.setStale(true);

		validator = new MultiValidator() {
			protected IStatus validate() {
				if (nonStaleDependency.getValue() != null) {
					return (IStatus) nonStaleDependency.getValue();
				}
				return (IStatus) staleDependency.getValue();
			}
		};
		validationStatus = validator.getValidationStatus();

		assertFalse(validationStatus.isStale());

		StaleCounter validationStaleCounter = new StaleCounter();
		validationStatus.addStaleListener(validationStaleCounter);
		assertEquals(0, validationStaleCounter.count);

		// Setting the status of the non-stale dependency to null leads to the
		// new stale dependency being accessed which in turn should trigger a
		// stale event.
		nonStaleDependency.setValue(null);
		assertTrue(validationStatus.isStale());
		assertEquals(1, validationStaleCounter.count);
	}

	private static class DependencyObservableValue extends WritableValue {
		private boolean stale = false;

		public DependencyObservableValue(Object initialValue, Object valueType) {
			super(initialValue, valueType);
		}

		public boolean isStale() {
			ObservableTracker.getterCalled(this);
			return stale;
		}

		public void setStale(boolean stale) {
			if (this.stale != stale) {
				this.stale = stale;
				if (stale) {
					fireStale();
				} else {
					fireValueChange(Diffs.createValueDiff(doGetValue(),
							doGetValue()));
				}
			}
		}

		protected void fireStale() {
			super.fireStale();
		}
	}

	private static class StaleCounter implements IStaleListener {
		int count;

		public void handleStale(StaleEvent event) {
			count++;
		}
	}
}
