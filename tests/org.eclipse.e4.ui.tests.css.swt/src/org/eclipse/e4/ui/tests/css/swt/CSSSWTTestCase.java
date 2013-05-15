/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class CSSSWTTestCase extends TestCase {
	
	public CSSEngine createEngine(String styleSheet, Display display) {
		CSSEngine engine = new CSSSWTEngineImpl(display);
		
		engine.setErrorHandler(new CSSErrorHandler() {
			public void error(Exception e) {
				fail(e.getMessage());
			}
		});
		
		try {
			engine.parseStyleSheet(new StringReader(styleSheet));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return engine;
	
	}
	
	/**
	 * Parse and apply the style sheet, forgetting previous style sheets applied.
	 * This is helpful for reusing the same engine but writing independent tests.
	 * Styles are applied down the widget hierarchy.
	 * @param engine the engine
	 * @param widget the start of the widget hierarchy
	 * @param styleSheet a string style sheet
	 */
	public void clearAndApply(CSSEngine engine, Widget widget, String styleSheet) {

		//Forget all previous styles
		engine.reset();

		try {
			engine.parseStyleSheet(new StringReader(styleSheet));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		engine.applyStyles(widget, true, true);
	}
	
	/**
	 * Asserts that two int arrays are equal in size and contents. If they are not
	 * an AssertionFailedError is thrown.
	 */
	static public void assertEquals(int[] expected, int[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
		    assertEquals(expected[i], actual[i]);			
		}
	}

    @Override
    protected void tearDown() throws Exception {
        Display display = Display.getDefault();
        if (!display.isDisposed()) {
            for (Shell shell : display.getShells()) {
                shell.dispose();
            }
        }
        super.tearDown();
    }

}
