/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.wizards;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WizardTestSuite extends TestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new WizardTestSuite();
    }

    public WizardTestSuite() {
    	addTestSuite(ButtonAlignmentTest.class);
    	addTestSuite(WizardTest.class);
    	addTestSuite(WizardProgressMonitorTest.class);
    }
}
