/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.examples.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.examples.databinding.model.SimplePerson;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class PersonTests {
	@Test
	public void testSetName() {
		SimplePerson person = new SimplePerson();
		Listener listener = new Listener();

		person.addPropertyChangeListener(listener);
		assertEquals(0, listener.count);
		assertNull(listener.lastEvent);
		person.setState("new state"); //$NON-NLS-1$

		assertEquals(1, listener.count);
		assertNotNull(listener.lastEvent);
		assertEquals("state", listener.lastEvent.getPropertyName()); //$NON-NLS-1$
		assertEquals("", listener.lastEvent.getOldValue());
		assertEquals("new state", listener.lastEvent.getNewValue()); //$NON-NLS-1$
		assertEquals("new state", person.getState());
	}

	private class Listener implements PropertyChangeListener {
		private int count;
		private PropertyChangeEvent lastEvent;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			count++;
			this.lastEvent = evt;
		}
	}
}
