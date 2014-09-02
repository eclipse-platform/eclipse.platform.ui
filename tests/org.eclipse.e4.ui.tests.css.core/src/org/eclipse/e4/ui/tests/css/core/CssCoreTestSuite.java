/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 430052
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core;

import org.eclipse.e4.ui.tests.css.core.parser.CascadeTest;
import org.eclipse.e4.ui.tests.css.core.parser.FontFaceRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.ImportTest;
import org.eclipse.e4.ui.tests.css.core.parser.MediaRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.RGBColorImplTest;
import org.eclipse.e4.ui.tests.css.core.parser.SelectorTest;
import org.eclipse.e4.ui.tests.css.core.parser.StyleRuleTest;
import org.eclipse.e4.ui.tests.css.core.parser.ValueTest;
import org.eclipse.e4.ui.tests.css.core.parser.ViewCSSTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CascadeTest.class,
	FontFaceRulesTest.class,
	MediaRulesTest.class,
	RGBColorImplTest.class,
	StyleRuleTest.class,
	ViewCSSTest.class,
	ValueTest.class,
	SelectorTest.class,
	CSSEngineTest.class,
	ImportTest.class
})
public class CssCoreTestSuite {
}
