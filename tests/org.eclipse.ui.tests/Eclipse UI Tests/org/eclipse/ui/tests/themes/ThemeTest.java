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

import java.util.Arrays;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.0
 */
public abstract class ThemeTest extends UITestCase {
	private static final String MOCK_CSS_THEME = "org.eclipse.e4.ui.css.theme.mock";
	
    protected static final String BOGUSID = "BOGUSID";

    protected static final String THEME1 = "theme1";

    public static void assertArrayEquals(Object[] datas, Object[] datas2) {
        if (!Arrays.equals(datas, datas2)) {
            String expected = formatArray(datas);
            String actual = formatArray(datas2);
            fail("expected:<" + expected + "> but was:<" + actual + ">");
        }
    }

    protected static String formatArray(Object[] datas) {
        StringBuffer buffer = new StringBuffer();
        if (datas == null)
            buffer.append("null");
        else {
            buffer.append('[');
            for (int i = 0; i < datas.length; i++) {
                buffer.append(datas[i]);
                if (i != datas.length - 1)
                    buffer.append(',');
            }
        }
        return buffer.toString();
    }

    protected IThemeManager fManager;

    /**
     * @param testName
     */
    public ThemeTest(String testName) {
        super(testName);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
        fManager = fWorkbench.getThemeManager();
        fManager.setCurrentTheme(IThemeManager.DEFAULT_THEME);
        
        mockCSSTheme();    
    }

    private void mockCSSTheme() {
    	IThemeEngine themeEngine = (IThemeEngine) fWorkbench.getService(IThemeEngine.class);
        org.eclipse.e4.ui.css.swt.theme.ITheme currentTheme = themeEngine.getActiveTheme(); 
        if (currentTheme != null && !MOCK_CSS_THEME.equals(currentTheme.getId())) {
        	themeEngine.setTheme(MOCK_CSS_THEME, false);
        }
    }
    
    /**
     * @return
     */
    protected ITheme getDefaultTheme() {
        ITheme defaultTheme = fManager.getTheme(IThemeManager.DEFAULT_THEME);
        assertNotNull(defaultTheme);
        return defaultTheme;
    }

    /**
     * @return
     */
    protected ITheme getTheme1() {
        ITheme theme1 = fManager.getTheme(THEME1);
        assertNotNull(theme1);
        return theme1;
    }

}
