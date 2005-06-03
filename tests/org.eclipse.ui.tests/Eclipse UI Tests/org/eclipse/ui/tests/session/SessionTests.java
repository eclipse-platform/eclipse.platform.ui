/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class SessionTests extends TestSuite {

	/**
	 * 
	 */
	public SessionTests() {
		addIntroTests();
		addEditorTests();
	}

	/**
	 * @return
	 */
	public static Test suite() {
    	return new SessionTests();
    }
	
	/**
	 * Adds intro related session tests.
	 */
	private void addIntroTests() {
		addTest(new WorkbenchSessionTest("introSessionTests", IntroSessionTests.class));
	}
	
	/**
	 * Add editor tests that involve starting and stopping sessions.
	 */
	private void addEditorTests() {
		addTest(new WorkbenchSessionTest("editorSessionTests",
				Bug95357Test.class));		
	}
}
