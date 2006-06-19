/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.examples.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.model.SimplePerson;

/**
 * @since 3.2
 *
 */
public class PersonTests extends TestCase {
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
		
		public void propertyChange(PropertyChangeEvent evt) {
			count++;
			this.lastEvent = evt;
		}
	}
}
