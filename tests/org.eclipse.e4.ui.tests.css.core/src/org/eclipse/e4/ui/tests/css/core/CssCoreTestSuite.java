/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
