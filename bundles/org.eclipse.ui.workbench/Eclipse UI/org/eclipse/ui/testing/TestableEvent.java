/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.testing;

import java.util.EventObject;

/**
 * An event from a testable object.
 * 
 * @since 3.0
 */
public class TestableEvent extends EventObject {

	/**
	 * Event code indicating that tests can now be run.
	 */
	public static final int CAN_RUN_TESTS = 1;
	
	/**
	 * The event code.
	 */
	private int eventCode;
	
	/**
	 * Constructs a new testable event
	 *  
	 * @param testable the testable object
	 */
	public TestableEvent(TestableObject testable, int eventCode) {
		super(testable);
		this.eventCode = eventCode;
	}
	
	/**
	 * Returns the testable object
	 * 
	 * @return the testable object
	 */
	public TestableObject getTestableObject() {
		return (TestableObject) getSource();
	}
	
	/**
	 * Returns the event code.
	 * 
	 * @return the event code
	 */
	public int getEventCode() {
		return eventCode;
	}
}
