package org.eclipse.e4.ui.tests.css.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.e4.ui.tests.css.core.parser.FontFaceRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.MediaRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.StyleRuleTest;

public class CssCoreTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("All CSS core tests");
		// $JUnit-BEGIN$
		suite.addTestSuite(StyleRuleTest.class);
		suite.addTestSuite(MediaRulesTest.class);
		suite.addTestSuite(FontFaceRulesTest.class);
		// $JUnit-END$
		return suite;
	}
}
