/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import java.util.Locale;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

/**
 * 
 */
public class LocaleTest extends AbstractNavigatorTest {

	
	public LocaleTest(String testName) {
		super(testName);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
	}
	
	
	public void testLocales() throws WorkbenchException{
		Locale[] locales = Locale.getAvailableLocales();
		Locale oldLocale = Locale.getDefault();
		for (int i = 0; i < locales.length; i++) {
			Locale.setDefault(locales[i]);
			IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench()
					.getPerspectiveRegistry().getPerspectives();
			for (int j = 0; j < perspectives.length; j++) {
				IPerspectiveDescriptor descriptor = perspectives[j];
				PlatformUI.getWorkbench().openWorkbenchWindow(
						descriptor.getId(),
						ResourcesPlugin.getWorkspace().getRoot());
			}
		}
		Locale.setDefault(oldLocale);
	}

}
