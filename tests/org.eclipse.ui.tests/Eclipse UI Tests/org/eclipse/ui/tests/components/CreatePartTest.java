/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.Part;
import org.eclipse.ui.tests.autotests.AbstractTestLogger;

/**
 * @since 3.1
 */
public class CreatePartTest extends PartTest {

    /**
     * @param testName
     * @param partBuilder
     */
    public CreatePartTest(AbstractTestLogger log, IPartBuilder partBuilder) {
        super("create", log, partBuilder);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    public String performTest() throws Throwable {
        Shell testShell = createShell();
        Part part = createPart(testShell);
        testShell.dispose();
        destroyPart(part);
        
        return "";
    }

}
