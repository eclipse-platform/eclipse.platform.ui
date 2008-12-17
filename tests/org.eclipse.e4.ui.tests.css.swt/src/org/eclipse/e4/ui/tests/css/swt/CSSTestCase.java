package org.eclipse.e4.ui.tests.css.swt;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.widgets.Display;

public class CSSTestCase extends TestCase {
	
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
}
