package org.eclipse.e4.ui.tests.css.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.e4.ui.tests.css.core.parser.FontFaceRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.MediaRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.StyleRuleTest;

public class CssCoreTestSuite extends TestSuite {

	public static Test suite() {
		return new CssCoreTestSuite();
	}
	public CssCoreTestSuite() {
		// $JUnit-BEGIN$
		addTestSuite(StyleRuleTest.class);
		addTestSuite(MediaRulesTest.class);
		addTestSuite(FontFaceRulesTest.class);
		// $JUnit-END$
	}
}
