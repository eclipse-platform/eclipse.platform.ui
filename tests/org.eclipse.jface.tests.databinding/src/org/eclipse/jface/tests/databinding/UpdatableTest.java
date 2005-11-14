/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.jface.databinding.*;
import org.eclipse.jface.tests.databinding.util.Mocks;

public class UpdatableTest extends TestCase {

	private static class MyUpdatable extends Updatable {
		public void fireChange(int changeType, Object oldValue,
				Object newValue, int position) {
			fireChangeEvent(changeType, oldValue, newValue, position);
		}

		public void fireChange(int changeType, Object oldValue, Object newValue) {
			fireChangeEvent(changeType, oldValue, newValue);
		}
	}

	private MyUpdatable updatable;

	protected void setUp() throws Exception {
		super.setUp();
		updatable = new MyUpdatable();
	}

	protected void tearDown() throws Exception {
		if (updatable != null) {
			try {
				updatable.dispose();
			} catch (Exception ex) {
				// ignore
			}
		}
		updatable = null;
		super.tearDown();
	}

	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Updatable.addChangeListener(IChangeListener)'
	 */
	public void testAddChangeListener() {
		IChangeListener changeListenerMock = (IChangeListener) Mocks
				.createOrderedMock(IChangeListener.class);

		// testing that no methods on the updatable are called when adding the
		// change listener
		Mocks.startChecking(changeListenerMock);
		updatable.addChangeListener(changeListenerMock);

		// testing that handleChange is actually called
		Mocks.reset(changeListenerMock);
		changeListenerMock.handleChange(null);
		changeListenerMock.handleChange(null);
		Mocks.startChecking(changeListenerMock);
		updatable.fireChange(0, null, null, 0);
		updatable.fireChange(0, null, null, 0);
		Mocks.verify(changeListenerMock);

		// testing that handleChange is called just once
		Mocks.reset(changeListenerMock);
		changeListenerMock.handleChange(null);
		Mocks.startChecking(changeListenerMock);
		updatable.addChangeListener(changeListenerMock);
		updatable.fireChange(0, null, null, 0);
		Mocks.verify(changeListenerMock);
	}

	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Updatable.removeChangeListener(IChangeListener)'
	 */
	public void testRemoveChangeListener() {
		IChangeListener changeListenerMock = (IChangeListener) Mocks
				.createOrderedMock(IChangeListener.class);

		// testing that no methods on the updatable are called when removing the
		// change listener
		Mocks.startChecking(changeListenerMock);
		updatable.removeChangeListener(changeListenerMock);

		// testing that no methods on the updatable are called after adding and
		// removing the change listener
		updatable.addChangeListener(changeListenerMock);
		updatable.removeChangeListener(changeListenerMock);

		updatable.fireChange(0, null, null, 0);
	}

	/*
	 * Test method for 'org.eclipse.jface.databinding.Updatable.fireChangeEvent(int,
	 * Object, Object)'
	 */
	public void testFireChangeEvent() {
		IChangeListener changeListenerMock = (IChangeListener) Mocks
				.createMock(IChangeListener.class,
						new Mocks.EqualityComparator() {
							public boolean equals(Object o1, Object o2) {
								ChangeEvent changeEvent1 = (ChangeEvent) o1;
								ChangeEvent changeEvent2 = (ChangeEvent) o2;
								return changeEvent1.getChangeType() == changeEvent2
										.getChangeType()
										&& changeEvent1.getPosition() == changeEvent2
												.getPosition()
										&& changeEvent1.getOldValue() == changeEvent2
												.getOldValue()
										&& changeEvent1.getNewValue() == changeEvent2
												.getNewValue();
							}
						});
		updatable.addChangeListener(changeListenerMock);

		Object o1 = new Object();
		Object o2 = new Object();

		changeListenerMock.handleChange(new ChangeEvent(updatable, 0, null,
				null, 0));
		changeListenerMock.handleChange(new ChangeEvent(updatable, 0, null,
				null, 0));
		changeListenerMock.handleChange(new ChangeEvent(updatable,
				ChangeEvent.CHANGE, o1, o2, 0));
		changeListenerMock.handleChange(new ChangeEvent(updatable,
				ChangeEvent.CHANGE, o1, o2, 42));
		Mocks.startChecking(changeListenerMock);
		updatable.fireChange(0, null, null);
		updatable.fireChange(0, null, null, 0);
		updatable.fireChange(ChangeEvent.CHANGE, o1, o2);
		updatable.fireChange(ChangeEvent.CHANGE, o1, o2, 42);
		Mocks.verify(changeListenerMock);
	}

}
