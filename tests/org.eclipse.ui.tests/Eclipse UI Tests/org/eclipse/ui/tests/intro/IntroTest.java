/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.intro;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.util.UITestCase;


/**
 * @since 3.0
 */
public class IntroTest extends UITestCase {

    IWorkbenchWindow window = null;
    private IntroDescriptor oldDesc;
    
    	/**
     * @param testName
     */
    public IntroTest(String testName) {
        super(testName);
    }

    public void testShow() {
        IWorkbench workbench = window.getWorkbench();        
        IIntroPart part = workbench.showIntro(window, false);
        assertNotNull(part);
        assertFalse(workbench.isIntroStandby(part));
        workbench.closeIntro(part);
        assertNull(workbench.getIntro());
        
        part = workbench.showIntro(window, true);
        assertNotNull(part);
        assertTrue(workbench.isIntroStandby(part));
        workbench.closeIntro(part);
        assertNull(workbench.getIntro());
	}
    
    public void testStandby() {
        IWorkbench workbench = window.getWorkbench();        
        IIntroPart part = workbench.showIntro(window, false);
        assertNotNull(part);
        assertFalse(workbench.isIntroStandby(part));
        workbench.setIntroStandby(part, true);
        assertTrue(workbench.isIntroStandby(part));        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
        window = openTestWindow();
        oldDesc = Workbench.getInstance().getIntroDescriptor();
        IntroDescriptor testDesc = (IntroDescriptor)WorkbenchPlugin.getDefault().getIntroRegistry().getIntro("org.eclipse.ui.testintro");
        Workbench.getInstance().setIntroDescriptor(testDesc);        
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
     */
    protected void doTearDown() throws Exception {
        super.doTearDown();
        Workbench.getInstance().setIntroDescriptor(oldDesc);
    }
}
