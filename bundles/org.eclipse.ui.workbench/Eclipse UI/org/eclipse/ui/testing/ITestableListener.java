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

/**
 * A listener for events from an <code>ITestable</code>.
 * 
 * @since 3.0
 */
public interface ITestableListener {
	
	/**
	 * Notification of an event from a testable object.
	 * 
	 * @param event the event object
	 */
	public void testableEvent(TestableEvent event);

}
