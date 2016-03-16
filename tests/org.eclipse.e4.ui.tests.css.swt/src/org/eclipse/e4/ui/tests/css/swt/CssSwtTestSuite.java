/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 419482
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 459961
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpersTest;
import org.eclipse.e4.ui.css.core.resources.SWTResourceRegistryKeyFactoryTest;
import org.eclipse.e4.ui.css.core.resources.SWTResourcesRegistryTest;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelperTest;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelperTest;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelperTest;
import org.eclipse.e4.ui.css.swt.helpers.PreferenceOverriddenByCssChangeListenerTest;
import org.eclipse.e4.ui.css.swt.properties.preference.EclipsePreferencesHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// note to contributors: please ignore Eclipse default formatting and keep one class per line.
@RunWith(Suite.class)
@Suite.SuiteClasses({
	CSSSWTFontHelperTest.class,
	CSSSWTColorHelperTest.class,
	CSSResourcesHelpersTest.class,
	SWTResourceRegistryKeyFactoryTest.class,
	SWTResourcesRegistryTest.class,
	FontDefinitionTest.class,
	ColorDefinitionTest.class,
	ThemesExtensionTest.class,
	IEclipsePreferencesTest.class,
	EclipsePreferencesHelperTest.class,
	CSSSWTWidgetTest.class,
	LabelTest.class,
	CTabFolderTest.class,
	CTabItemTest.class,
	IdClassLabelColorTest.class,
	ShellTest.class,
	ButtonTest.class,
	GradientTest.class,
	MarginTest.class,
	InnerClassElementTest.class,
	EclipsePreferencesHandlerTest.class,
	PreferenceOverriddenByCssChangeListenerTest.class,
	ButtonTextTransformTest.class,
	LabelTextTransformTest.class,
	TextTextTransformTest.class,
	DescendentTest.class,
	ThemeTest.class,
	Bug459961Test.class,
	Bug419482Test.class,
	ShellActiveTest.class,
	InheritTest.class,
	StyledTextScrollbarTest.class })
public class CssSwtTestSuite {

}
