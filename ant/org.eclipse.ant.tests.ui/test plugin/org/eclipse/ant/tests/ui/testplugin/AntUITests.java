/*******************************************************************************
 * Copyright (c) 2002, 2015 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 *
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial implementation
 * 	   IBM Corporation - additional tests
 *******************************************************************************/

package org.eclipse.ant.tests.ui.testplugin;

import org.eclipse.ant.tests.ui.APITests;
import org.eclipse.ant.tests.ui.AntUtilTests;
import org.eclipse.ant.tests.ui.AntViewTests;
import org.eclipse.ant.tests.ui.BuildTests;
import org.eclipse.ant.tests.ui.ModelProjectTests;
import org.eclipse.ant.tests.ui.debug.BreakpointTests;
import org.eclipse.ant.tests.ui.debug.PropertyTests;
import org.eclipse.ant.tests.ui.debug.RunToLineTests;
import org.eclipse.ant.tests.ui.debug.StackTests;
import org.eclipse.ant.tests.ui.debug.SteppingTests;
import org.eclipse.ant.tests.ui.editor.AntEditorContentOutlineTests;
import org.eclipse.ant.tests.ui.editor.AntEditorTests;
import org.eclipse.ant.tests.ui.editor.CodeCompletionTest;
import org.eclipse.ant.tests.ui.editor.OccurrencesFinderTests;
import org.eclipse.ant.tests.ui.editor.TaskDescriptionProviderTest;
import org.eclipse.ant.tests.ui.editor.formatter.FormattingPreferencesTest;
import org.eclipse.ant.tests.ui.editor.formatter.XmlDocumentFormatterTest;
import org.eclipse.ant.tests.ui.editor.formatter.XmlFormatterTest;
import org.eclipse.ant.tests.ui.editor.formatter.XmlTagFormatterTest;
import org.eclipse.ant.tests.ui.externaltools.BuilderCoreUtilsTests;
import org.eclipse.ant.tests.ui.externaltools.MigrationTests;
import org.eclipse.ant.tests.ui.separateVM.SeparateVMTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for the Ant UI
 */
public class AntUITests extends TestSuite {

	public static Test suite() {

		TestSuite suite = new AntUITests();
		suite.setName("Ant UI Unit Tests"); //$NON-NLS-1$
		suite.addTestSuite(BuildTests.class);
		suite.addTestSuite(SeparateVMTests.class);
		suite.addTestSuite(AntEditorTests.class);
		suite.addTestSuite(CodeCompletionTest.class);
		suite.addTestSuite(TaskDescriptionProviderTest.class);
		suite.addTestSuite(AntEditorContentOutlineTests.class);
		suite.addTestSuite(MigrationTests.class);
		suite.addTestSuite(BuilderCoreUtilsTests.class);
		suite.addTestSuite(ModelProjectTests.class);
		suite.addTestSuite(FormattingPreferencesTest.class);
		suite.addTestSuite(XmlDocumentFormatterTest.class);
		suite.addTestSuite(XmlTagFormatterTest.class);
		suite.addTestSuite(XmlFormatterTest.class);
		suite.addTestSuite(AntUtilTests.class);
		suite.addTestSuite(AntViewTests.class);
		suite.addTestSuite(BreakpointTests.class);
		suite.addTestSuite(RunToLineTests.class);
		suite.addTestSuite(SteppingTests.class);
		suite.addTestSuite(PropertyTests.class);
		suite.addTestSuite(OccurrencesFinderTests.class);
		suite.addTestSuite(StackTests.class);
		suite.addTestSuite(APITests.class);
		return suite;
	}
}
