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
package org.eclipse.ui.tests.rcp.performance;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.1
 */
public class RCPPerformanceTestSetup extends TestSetup {

	public RCPPerformanceTestSetup(Test test) {
		super(test);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// The RCP tests are currently run in the context of the SDK product, which specifies
		// the Java perspective as the default, and to show progress on startup.  
		// We don't want these in effect when running the RCP tests.
		// Also disable intro.
		IPreferenceStore prefs = PlatformUI.getPreferenceStore();
		prefs.setValue(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, "");
		prefs.setValue(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP, false);
		prefs.setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, false);
	}
	
}
