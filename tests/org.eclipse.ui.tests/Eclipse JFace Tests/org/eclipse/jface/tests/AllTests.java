/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests;

import org.eclipse.jface.tests.labelProviders.DecoratingLabelProviderTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new AllTests();
    }

    public AllTests() {
        addTest(new org.eclipse.jface.tests.action.AllTests());
        addTest(new org.eclipse.jface.tests.dialogs.AllTests());
        addTest(new org.eclipse.jface.tests.images.AllTests());
        addTest(new org.eclipse.jface.tests.viewers.AllTests());
        addTest(new org.eclipse.jface.tests.layout.AllTests());
        addTest(new DecoratingLabelProviderTests());
    }
}
