/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.intro;

import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.api.IWorkbenchPartTest;
import org.eclipse.ui.tests.api.MockPart;
import org.eclipse.ui.tests.harness.util.CallHistory;

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

    @Override
	protected MockPart openPart(IWorkbenchPage page) throws Throwable {
        return (MockIntroPart) page.getWorkbenchWindow().getWorkbench()
                .getIntroManager().showIntro(page.getWorkbenchWindow(), false);
    }

    @Override
	protected void closePart(IWorkbenchPage page, MockPart part)
            throws Throwable {
        assertTrue(page.getWorkbenchWindow().getWorkbench().getIntroManager()
                .closeIntro((IIntroPart) part));
    }

    @Override
	public void testOpenAndClose() throws Throwable {
        // Open a part.
        MockPart part = openPart(fPage);
        CallHistory history = part.getCallHistory();
        assertTrue(history.verifyOrder(new String[] { "init",
                "createPartControl", "setFocus", "standbyStateChanged" }));

        // Close the part.
        closePart(fPage, part);
        assertTrue(history.verifyOrder(new String[] { "init",
                "createPartControl", "setFocus", "dispose" }));
    }

    /**
     * Tests to ensure that the image of the descriptor is the same as the part.
     * 
     * @throws Throwable
     */
    public void testImage() throws Throwable {
        MockPart part = openPart(fPage);
        ImageDescriptor imageDescriptor = getIntroDesc().getImageDescriptor();
        assertNotNull(imageDescriptor);

        Image descImage = imageDescriptor.createImage(false);
        assertNotNull(descImage);

        Image partImage = part.getTitleImage();
        assertNotNull(partImage);
        assertTrue(Arrays.equals(descImage.getImageData().data, partImage
                .getImageData().data));
        if (descImage != null)
            descImage.dispose();
        closePart(fPage, part);
    }

    @Override
	protected void doSetUp() throws Exception {
        super.doSetUp();
        oldDesc = Workbench.getInstance().getIntroDescriptor();
        IntroDescriptor testDesc = getIntroDesc();
        Workbench.getInstance().setIntroDescriptor(testDesc);
    }

    /**
     * @return
     */
    private IntroDescriptor getIntroDesc() {
        return (IntroDescriptor) WorkbenchPlugin.getDefault()
                .getIntroRegistry().getIntro("org.eclipse.ui.testintro");
    }

    @Override
	protected void doTearDown() throws Exception {
        super.doTearDown();
        Workbench.getInstance().setIntroDescriptor(oldDesc);
    }
}
