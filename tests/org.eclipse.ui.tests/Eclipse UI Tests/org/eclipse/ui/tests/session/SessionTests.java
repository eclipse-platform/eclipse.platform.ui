/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.util.Util;
import org.eclipse.ui.tests.markers.MarkersViewColumnSizeTest;
import org.eclipse.ui.tests.statushandlers.StatusHandlerConfigurationSuite;
import org.eclipse.ui.tests.statushandlers.StatusHandlingConfigurationTest;

/**
 * @since 3.1
 */
public class SessionTests extends TestSuite {

	/**
	 * @return
	 */
	public static Test suite() {
		return new SessionTests();
	}

	/**
	 * 
	 */
	public SessionTests() {
		addHandlerStateTests();
		addIntroTests();
		addEditorTests();
		addViewStateTests();
		addThemeTests();
		addStatusHandlingTests();
		addRestoredSessionTest();
		addWindowlessSessionTest();
	}

	/**
	 * 
	 */
	private void addWindowlessSessionTest() {
		// Windowless apps are available only on Cocoa
		if (Util.isCocoa()) {
			Map arguments = new HashMap(2);
			arguments.put("product", null);
			arguments.put("testApplication",
					"org.eclipse.ui.tests.windowLessRcpApplication");
			WorkbenchSessionTest test = new WorkbenchSessionTest(
					"windowlessSessionTests", arguments);
			test.addTest(WindowlessSessionTest.suite());
			addTest(test);
		}
	}

	/**
	 * 
	 */
	private void addStatusHandlingTests() {
		//actually we do not care which workspace is used
		StatusHandlerConfigurationSuite test = new StatusHandlerConfigurationSuite("themeSessionTests");
		test.addTest(StatusHandlingConfigurationTest.suite());
		addTest(test);
	}

	/**
	 * 
	 */
	private void addThemeTests() {
		WorkbenchSessionTest test = new WorkbenchSessionTest("themeSessionTests");
		test.addTest(ThemeStateTest.suite());
		addTest(test);
	}

	private void addRestoredSessionTest() {
		Map arguments = new HashMap(2);
		arguments.put("product", null);
		arguments.put("testApplication", "org.eclipse.ui.tests.rcpSessionApplication");
		WorkbenchSessionTest test = new WorkbenchSessionTest("introSessionTests", arguments);
		test.addTest(RestoreSessionTest.suite());
		addTest(test);
	}

	/**
	 * Add editor tests that involve starting and stopping sessions.
	 */
	private void addEditorTests() {
		WorkbenchSessionTest test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(Bug95357Test.suite());
		addTest(test);
		
		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(EditorWithStateTest.suite());
		addTest(test);

		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(ArbitraryPropertiesEditorTest.suite());
		addTest(test);
	}

	/**
	 * Adds tests related to command and handler state.
	 * 
	 * @since 3.2
	 */
	private void addHandlerStateTests() {
		WorkbenchSessionTest test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(HandlerStateTest.suite());
		addTest(test);
	}

	/**
	 * Adds intro related session tests.
	 */
	private void addIntroTests() {
		WorkbenchSessionTest test = new WorkbenchSessionTest("introSessionTests");
		test.addTest(IntroSessionTests.suite());
		addTest(test);
	}

	/**
	 * Add a view state test that involves state from one session to the other.
	 * 
	 * BTW: the <b>editorSessionTests</b> is the zip file to grab the default
	 * workspace for these particular session tests.
	 */
	private void addViewStateTests() {
		WorkbenchSessionTest test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(Bug98800Test.suite());
		addTest(test);
		
		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(Bug108033Test.suite());
		addTest(test);
		
		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(ArbitraryPropertiesViewTest.suite());
		addTest(test);
		
		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(NonRestorableViewTest.suite());
		addTest(test);
		
		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(NonRestorablePropertySheetTest.suite());
		addTest(test);

		test = new WorkbenchSessionTest("editorSessionTests");
		test.addTest(MarkersViewColumnSizeTest.suite());
		addTest(test);
	}
}
