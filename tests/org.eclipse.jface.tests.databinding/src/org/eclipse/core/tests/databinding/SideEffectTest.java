/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link ISideEffect}.
 *
 * @since 3.2
 */
public class SideEffectTest extends AbstractDefaultRealmTestCase {
	// TODO: Add test cases for {@link SideEffect#create(Runnable)},
	// {@link SideEffect#create(java.util.function.Supplier,
	// java.util.function.Consumer)}
	// {@link SideEffect#pause()}, and {@link SideEffect#resume()}
	// - Validate that runIfDirty does nothing when paused

	private ISideEffect sideEffect;
	private int sideEffectInvocations;

	private WritableValue<String> defaultDependency;
	private WritableValue<String> alternateDependency;
	private WritableValue<Boolean> useDefaultDependency;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		defaultDependency = new WritableValue<>("", null);
		alternateDependency = new WritableValue<>("", null);
		useDefaultDependency = new WritableValue<>(true, null);

		sideEffect = ISideEffect.createPaused(() -> {
			if (useDefaultDependency.getValue()) {
				defaultDependency.getValue();
			} else {
				alternateDependency.getValue();
			}
			sideEffectInvocations++;
		});
	}

	@Test
	public void testSideEffectDoesntRunUntilResumed() throws Exception {
		runAsync();
		assertEquals(0, sideEffectInvocations);
	}

	@Test
	public void testSideEffectRunsWhenResumed() throws Exception {
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);
	}

	@Test(expected = IllegalStateException.class)
	public void testResumingSideEffectMultipleTimesThrowsIllegalStateException() throws Exception {
		sideEffect.resume();
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);
	}

	@Test
	public void testSideEffectSelectsCorrectDependency() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);

		// Confirm that the SideEffect is reacting to defaultDependency
		defaultDependency.setValue("foo");
		runAsync();
		assertEquals(2, sideEffectInvocations);

		// Confirm that the SideEffect is not reacting to alternateDependency
		alternateDependency.setValue("foo");
		runAsync();
		assertEquals(2, sideEffectInvocations);

		// Now change the branch that the side effect ran through and ensure
		// that it selected the correct new dependency (and removed the old one)
		useDefaultDependency.setValue(false);
		runAsync();
		assertEquals(3, sideEffectInvocations);

		// Confirm that the SideEffect is not reacting to defaultDependency
		defaultDependency.setValue("bar");
		runAsync();
		assertEquals(3, sideEffectInvocations);

		// Confirm that the SideEffect is reacting to alternateDependency
		alternateDependency.setValue("bar");
		runAsync();
		assertEquals(4, sideEffectInvocations);
	}

	@Test
	public void testChangingMultipleDependenciesOnlyRunsTheSideEffectOnce() throws Exception {
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);

		defaultDependency.setValue("Foo");
		alternateDependency.setValue("Foo");
		useDefaultDependency.setValue(false);

		runAsync();
		assertEquals(2, sideEffectInvocations);
	}

	@Test
	public void testChangingDependencyRerunsSideEffect() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now change the dependency
		defaultDependency.setValue("Foo");
		runAsync();

		// Ensure that the side effect ran again as a result
		assertEquals(2, sideEffectInvocations);
	}

	@Test
	public void testChangingUnrelatedNodeDoesntRunSideEffect() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now change the currently-unused dependency
		alternateDependency.setValue("Bar");
		runAsync();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	@Test
	public void testDeactivatedSideEffectWontRunWhenTriggeredByDependency() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now deactivate the side-effect and trigger one of its dependencies
		defaultDependency.setValue("Foo");
		sideEffect.dispose();
		runAsync();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	@Test
	public void testDeactivatedSideEffectWontRunWhenRunIfDirtyInvoked() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		sideEffect.pause();
		defaultDependency.setValue("MakeItDirty");
		sideEffect.runIfDirty();
		runAsync();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	@Test
	public void testRunIfDirtyDoesNothingIfSideEffectNotDirty() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now deactivate the side-effect and trigger one of its dependencies
		sideEffect.runIfDirty();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	@Test
	public void testRunIfDirty() throws Exception {
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);
		defaultDependency.setValue("Foo");
		sideEffect.runIfDirty();
		assertEquals(2, sideEffectInvocations);
	}

	@Test
	public void testNestedDependencyChangeAndRunIfDirtyCompletes() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();
		WritableValue<Object> invalidator = new WritableValue<>(new Object(), null);
		ISideEffect innerSideEffect = ISideEffect.create(() -> {
			invalidator.getValue();
		});

		ISideEffect.createPaused(() -> {
			// Make sure that there are no infinite loops.
			assertFalse(hasRun.get());
			hasRun.set(true);
			invalidator.setValue(new Object());
			innerSideEffect.runIfDirty();
		}).resume();

		runAsync();
		assertTrue(hasRun.get());
	}

	@Test
	public void testNestedInvalidateAndRunIfDirtyCompletes() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();
		final WritableValue<Object> makesThingsDirty = new WritableValue<>(null, null);
		ISideEffect innerSideEffect = ISideEffect.createPaused(() -> {
			makesThingsDirty.getValue();
		});

		innerSideEffect.resume();

		ISideEffect.createPaused(() -> {
			// Make sure that there are no infinite loops.
			assertFalse(hasRun.get());
			hasRun.set(true);
			makesThingsDirty.setValue(new Object());
			innerSideEffect.runIfDirty();
		}).resume();

		runAsync();
		assertTrue(hasRun.get());
	}

	@Test
	public void testConsumeOnceDoesntPassNullToConsumer() throws Exception {
		AtomicBoolean consumerHasRun = new AtomicBoolean();
		WritableValue<Object> makesThingsDirty = new WritableValue<>(null, null);
		ComputedValue<Object> value = new ComputedValue<>() {
			@Override
			protected Object calculate() {
				makesThingsDirty.getValue();
				return null;
			}
		};

		ISideEffect consumeOnce = ISideEffect.consumeOnceAsync(value::getValue, Object -> {
			consumerHasRun.set(true);
		});

		makesThingsDirty.setValue(new Object());
		runAsync();
		makesThingsDirty.setValue(new Object());
		runAsync();
		assertFalse(consumerHasRun.get());
		consumeOnce.dispose();
	}

	@Test
	public void testConsumeOnceDoesntRunTwice() throws Exception {
		AtomicInteger numberOfRuns = new AtomicInteger();
		WritableValue<Object> makesThingsDirty = new WritableValue<>(null, null);
		WritableValue<Object> returnValue = new WritableValue<>(null, null);
		ComputedValue<Object> value = new ComputedValue<>() {
			@Override
			protected Object calculate() {
				makesThingsDirty.getValue();
				return returnValue.getValue();
			}
		};

		ISideEffect consumeOnce = ISideEffect.consumeOnceAsync(value::getValue, Object -> {
			numberOfRuns.set(numberOfRuns.get() + 1);
		});

		makesThingsDirty.setValue(new Object());
		runAsync();
		assertEquals(0, numberOfRuns.get());

		returnValue.setValue("Foo");
		runAsync();
		assertEquals(1, numberOfRuns.get());

		returnValue.setValue("Bar");
		runAsync();
		assertEquals(1, numberOfRuns.get());
		consumeOnce.dispose();
	}

	@Test
	public void testConsumeOnceDoesntRunAtAllIfDisposed() throws Exception {
		AtomicInteger numberOfRuns = new AtomicInteger();
		WritableValue<Object> returnValue = new WritableValue<>("foo", null);

		ISideEffect consumeOnce = ISideEffect.consumeOnceAsync(returnValue::getValue, Object -> {
			numberOfRuns.set(numberOfRuns.get() + 1);
		});

		consumeOnce.dispose();

		runAsync();
		assertEquals(0, numberOfRuns.get());
	}

	@Test
	public void testConsumeOnceRunsIfInitialValueNonNull() throws Exception {
		AtomicInteger numberOfRuns = new AtomicInteger();
		WritableValue<Object> returnValue = new WritableValue<>("foo", null);

		ISideEffect consumeOnce = ISideEffect.consumeOnceAsync(returnValue::getValue, Object -> {
			numberOfRuns.set(numberOfRuns.get() + 1);
		});

		runAsync();
		assertEquals(1, numberOfRuns.get());

		consumeOnce.dispose();
	}

	@Test
	public void testNestedSideEffectCreation() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();

		// Make sure that creating a SideEffect within another side effect works
		// propely.
		ISideEffect.createPaused(() -> {
			ISideEffect.createPaused(() -> {
				assertFalse(hasRun.get());
				hasRun.set(true);
			}).resume();
		}).resume();
		runAsync();
		assertTrue(hasRun.get());
	}

	@Test
	public void testSideEffectFiresDisposeEvent() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();

		// Make sure that a dispose event is sent correctly.
		ISideEffect disposeTest = ISideEffect.createPaused(() -> {
		});
		disposeTest.resume();
		disposeTest.addDisposeListener(sideEffect -> {
			assertTrue(disposeTest == sideEffect);
			hasRun.set(true);
		});
		disposeTest.dispose();
		runAsync();
		assertTrue(hasRun.get());
	}

	@Test
	public void testCanRemoveDisposeListener() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();

		// Make sure that a dispose event is sent correctly.
		ISideEffect disposeTest = ISideEffect.createPaused(() -> {
		});
		disposeTest.resume();
		Consumer<ISideEffect> disposeListener = sideEffect -> {
			hasRun.set(true);
		};
		disposeTest.addDisposeListener(disposeListener);
		disposeTest.removeDisposeListener(disposeListener);
		disposeTest.dispose();
		runAsync();
		assertFalse(hasRun.get());
	}

	@Test
	public void testObservableInMap() {
		HashMap<String, IObservableValue<String>> hashMap = new HashMap<>();
		IObservableValue<String> observableValue = new WritableValue<>();
		observableValue.setValue("Simon");
		hashMap.put("Simon", observableValue);

		ISideEffect.create(() -> {
			IObservableValue<String> observable = hashMap.get("Simon");
			observable.getValue();
			sideEffectInvocations++;
		});

		assertEquals(1, sideEffectInvocations);

		observableValue.setValue("Stefan");
		runAsync();

		assertEquals(2, sideEffectInvocations);
	}

	// Doesn't currently work, but this would be a desirable property for
	// SideEffect to have
	// public void testInvalidateSelf() throws Exception {
	// AtomicInteger runCount = new AtomicInteger();
	// WritableValue<Object> invalidator = new WritableValue<>(null,
	// null);
	// // Make sure that if a side effect invalidates it self, it will run at
	// // least once more but eventually stop.
	// ISideEffect[] sideEffect = new ISideEffect[1];
	// sideEffect[0] = ISideEffect.createPaused(() -> {
	// assertTrue(runCount.get() < 2);
	// invalidator.getValue();
	// int count = runCount.incrementAndGet();
	// if (count == 1) {
	// invalidator.setValue(new Object());
	// }
	// });
	// sideEffect[0].resume();
	// runAsync();
	// assertEquals(2, runCount.get());
	// }
}
