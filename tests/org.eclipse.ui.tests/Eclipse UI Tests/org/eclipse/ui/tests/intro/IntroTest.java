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
import org.eclipse.ui.intro.IIntroManager;
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
        IIntroManager introManager = window.getWorkbench().getIntroManager();
        IIntroPart part = introManager.showIntro(window, false);
        assertNotNull(part);
        assertFalse(introManager.isIntroStandby(part));
        introManager.closeIntro(part);
        assertNull(introManager.getIntro());
        
        part = introManager.showIntro(window, true);
        assertNotNull(part);
        assertTrue(introManager.isIntroStandby(part));
        introManager.closeIntro(part);
        assertNull(introManager.getIntro());
	}
    
    public void testStandby() {
        IWorkbench workbench = window.getWorkbench();        
        IIntroPart part = workbench.getIntroManager().showIntro(window, false);
        assertNotNull(part);
        assertFalse(workbench.getIntroManager().isIntroStandby(part));
        workbench.getIntroManager().setIntroStandby(part, true);
        assertTrue(workbench.getIntroManager().isIntroStandby(part));        
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
