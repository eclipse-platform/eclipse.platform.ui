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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.api.IWorkbenchPartTest;
import org.eclipse.ui.tests.api.MockWorkbenchPart;
import org.eclipse.ui.tests.util.CallHistory;


/**
 * @since 3.0
 */
public class IntroPartTest extends IWorkbenchPartTest {

    private IntroDescriptor oldDesc;

    /**
     * @param testName
     */
    public IntroPartTest(String testName) {
        super(testName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.IWorkbenchPartTest#openPart(org.eclipse.ui.IWorkbenchPage)
     */
    protected MockWorkbenchPart openPart(IWorkbenchPage page) throws Throwable {
        return (MockWorkbenchPart)page.getWorkbenchWindow().getWorkbench().getIntroManager().showIntro(page.getWorkbenchWindow(), false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.IWorkbenchPartTest#closePart(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.tests.api.MockWorkbenchPart)
     */
    protected void closePart(IWorkbenchPage page, MockWorkbenchPart part)
            throws Throwable {
		page.getWorkbenchWindow().getWorkbench().getIntroManager().closeIntro((IIntroPart) part);            
    }
    
	public void testOpenAndClose() throws Throwable {
		// Open a part.
		MockWorkbenchPart part = openPart(fPage);
		CallHistory history = part.getCallHistory();
		assertTrue(history.verifyOrder(new String[] {
			"init", "createPartControl", "setFocus", "standbyStateChanged"}));
		
		// Close the part.
		closePart(fPage, part);
		assertTrue(history.verifyOrder(new String[] {
			"init", "createPartControl", "setFocus", "dispose"}));
	}
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
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
