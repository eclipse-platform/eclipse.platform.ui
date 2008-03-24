/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.validation;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.observable.ValidatedObservableValue;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class MultiValidatorTest extends AbstractDefaultRealmTestCase {
	private WritableValue dependency;
	private MultiValidator validator;
	private IObservableValue validationStatus;

	protected void setUp() throws Exception {
		super.setUp();
		dependency = new WritableValue(null, IStatus.class);
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
}
