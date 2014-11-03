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
package org.eclipse.ui.tests.contexts;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * A test for whether part identifiers work properly for EnabledSubmissions.
 *
 * @since 3.1
 */
public final class Bug74990Test extends UITestCase {

    /**
     * Constructs a new instance of <code>Bug74990Test</code>.
     *
     * @param name
     *            The name of the test
     */
    public Bug74990Test(final String name) {
        super(name);
    }

    /**
     * Tests whether a part-specific context -- submitted via Java code -- is
     * matched properly. This is only using the part id. The test verifies that
     * it is active when the part is active, and not active when the part is not
     * active.
     *
     * @throws PartInitException
     *             If something goes wrong creating the part to which this
     *             handler is tied.
     *
     */
    public final void testPartIdSubmission() throws PartInitException {
        // Define a command.
        final String testContextId = "org.eclipse.ui.tests.contexts.Bug74990";
        final IWorkbenchContextSupport contextSupport = fWorkbench
                .getContextSupport();
        final IContext testContext = contextSupport.getContextManager()
                .getContext(testContextId);

        // Create an enabled submission.
        final EnabledSubmission testSubmission = new EnabledSubmission(
                "org.eclipse.ui.tests.api.MockViewPart", null, null,
                testContextId);
        contextSupport.addEnabledSubmission(testSubmission);

        try {
            // Test to make sure the context is not currently enabled.
            assertTrue("The MockViewPart context should not be enabled",
                    !testContext.isEnabled());

            /*
             * Open a window with the MockViewPart, and make sure it now
             * enabled.
             */
            final IWorkbenchPage page = openTestWindow().getActivePage();
            final IViewPart openedView = page
                    .showView("org.eclipse.ui.tests.api.MockViewPart");
            page.activate(openedView);
            while (fWorkbench.getDisplay().readAndDispatch()) {
				;
			}
            assertTrue("The MockViewPart context should be enabled",
                    testContext.isEnabled());

            // Hide the view, and test that is becomes disabled again.
            page.hideView(openedView);
            while (fWorkbench.getDisplay().readAndDispatch()) {
				;
			}
            assertTrue("The MockViewPart context should not be enabled",
                    !testContext.isEnabled());

        } finally {
            contextSupport.removeEnabledSubmission(testSubmission);
        }

    }
}
