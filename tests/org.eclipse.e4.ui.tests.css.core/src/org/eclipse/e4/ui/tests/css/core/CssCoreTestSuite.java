/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 430052, 419377
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core;

import org.eclipse.e4.ui.css.core.impl.engine.AbstractCSSEngineTest;
import org.eclipse.e4.ui.tests.css.core.parser.CascadeTest;
import org.eclipse.e4.ui.tests.css.core.parser.FontFaceRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.ImportTest;
import org.eclipse.e4.ui.tests.css.core.parser.InheritTest;
import org.eclipse.e4.ui.tests.css.core.parser.MediaRulesTest;
import org.eclipse.e4.ui.tests.css.core.parser.RGBColorImplTest;
import org.eclipse.e4.ui.tests.css.core.parser.SelectorTest;
import org.eclipse.e4.ui.tests.css.core.parser.StyleRuleTest;
import org.eclipse.e4.ui.tests.css.core.parser.ValueTest;
import org.eclipse.e4.ui.tests.css.core.parser.ViewCSSTest;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({
	CascadeTest.class,
	FontFaceRulesTest.class,
	MediaRulesTest.class,
	RGBColorImplTest.class,
	StyleRuleTest.class,
	ViewCSSTest.class,
	ValueTest.class,
	SelectorTest.class,
	CSSEngineTest.class,
	ImportTest.class,
	InheritTest.class,
	AbstractCSSEngineTest.class
})
public class CssCoreTestSuite {
}
