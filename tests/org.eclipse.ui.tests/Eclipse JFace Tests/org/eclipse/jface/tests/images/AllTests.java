/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - DecorationOverlayIconTest
 *******************************************************************************/
package org.eclipse.jface.tests.images;

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
        addTestSuite(ImageRegistryTest.class);
        addTestSuite(ResourceManagerTest.class);
        addTestSuite(FileImageDescriptorTest.class);
		addTestSuite(DecorationOverlayIconTest.class);
    }
}
