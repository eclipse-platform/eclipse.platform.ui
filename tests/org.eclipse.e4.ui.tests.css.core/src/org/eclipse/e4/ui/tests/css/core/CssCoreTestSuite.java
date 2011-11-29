package org.eclipse.e4.ui.tests.css.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.e4.ui.tests.css.core.parser.CascadeTest;
import org.eclipse.e4.ui.tests.css.core.parser.FontFaceRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.RGBColorImplTest;
import org.eclipse.e4.ui.tests.css.core.parser.SelectorTest;
import org.eclipse.e4.ui.tests.css.core.parser.StyleRuleTest;
import org.eclipse.e4.ui.tests.css.core.parser.ValueTest;
import org.eclipse.e4.ui.tests.css.core.parser.ViewCSSTest;

public class CssCoreTestSuite extends TestSuite {

	public static Test suite() {
		return new CssCoreTestSuite();
	}

	public CssCoreTestSuite() {
		// $JUnit-BEGIN$
		addTestSuite(CascadeTest.class);
		addTestSuite(FontFaceRulesTest.class);
//		addTestSuite(MediaRulesTest.class);
		addTestSuite(RGBColorImplTest.class);
		addTestSuite(StyleRuleTest.class);
		addTestSuite(ViewCSSTest.class);
		addTestSuite(ValueTest.class);
		addTestSuite(SelectorTest.class);
		addTestSuite(CSSEngineTest.class);
		// $JUnit-END$
	}
}
