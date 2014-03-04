/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.0
 */
public class ThemesTestSuite extends TestSuite {

    public static Test suite() {
        return new ThemesTestSuite();
    }

    public ThemesTestSuite() {
        addTest(new TestSuite(ThemeAPITest.class));
        addTest(new TestSuite(JFaceThemeTest.class));
        addTest(new TestSuite(WorkbenchThemeChangedHandlerTest.class));
        addTest(new TestSuite(ThemeRegistryModifiedHandlerTest.class));
    }
}
