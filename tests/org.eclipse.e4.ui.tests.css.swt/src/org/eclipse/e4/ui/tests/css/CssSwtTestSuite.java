/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 419482
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 459961
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css;

import org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpersTest;
import org.eclipse.e4.ui.css.core.resources.SWTResourceRegistryKeyFactoryTest;
import org.eclipse.e4.ui.css.core.resources.SWTResourcesRegistryTest;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelperTest;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelperTest;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelperTest;
import org.eclipse.e4.ui.css.swt.helpers.PreferenceOverriddenByCssChangeListenerTest;
import org.eclipse.e4.ui.css.swt.properties.preference.EclipsePreferencesHandlerTest;
import org.eclipse.e4.ui.tests.css.properties.tabbed.TabbedPropertiesListTest;
import org.eclipse.e4.ui.tests.css.properties.tabbed.TabbedPropertiesTitleTest;
import org.eclipse.e4.ui.tests.css.swt.Bug419482Test;
import org.eclipse.e4.ui.tests.css.swt.Bug459961Test;
import org.eclipse.e4.ui.tests.css.swt.ButtonTest;
import org.eclipse.e4.ui.tests.css.swt.ButtonTextTransformTest;
import org.eclipse.e4.ui.tests.css.swt.CSSSWTWidgetTest;
import org.eclipse.e4.ui.tests.css.swt.CTabFolderTest;
import org.eclipse.e4.ui.tests.css.swt.CTabItemTest;
import org.eclipse.e4.ui.tests.css.swt.ColorDefinitionTest;
import org.eclipse.e4.ui.tests.css.swt.DescendentTest;
import org.eclipse.e4.ui.tests.css.swt.FontDefinitionTest;
import org.eclipse.e4.ui.tests.css.swt.GradientTest;
import org.eclipse.e4.ui.tests.css.swt.IEclipsePreferencesTest;
import org.eclipse.e4.ui.tests.css.swt.IdClassLabelColorTest;
import org.eclipse.e4.ui.tests.css.swt.InheritTest;
import org.eclipse.e4.ui.tests.css.swt.InnerClassElementTest;
import org.eclipse.e4.ui.tests.css.swt.LabelTest;
import org.eclipse.e4.ui.tests.css.swt.LabelTextTransformTest;
import org.eclipse.e4.ui.tests.css.swt.LinkTest;
import org.eclipse.e4.ui.tests.css.swt.MarginTest;
import org.eclipse.e4.ui.tests.css.swt.ShellActiveTest;
import org.eclipse.e4.ui.tests.css.swt.ShellTest;
import org.eclipse.e4.ui.tests.css.swt.StyledTextScrollbarTest;
import org.eclipse.e4.ui.tests.css.swt.TableTest;
import org.eclipse.e4.ui.tests.css.swt.TextTextTransformTest;
import org.eclipse.e4.ui.tests.css.swt.ThemeTest;
import org.eclipse.e4.ui.tests.css.swt.ThemesExtensionTest;
import org.eclipse.e4.ui.tests.css.swt.TreeTest;
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
	LinkTest.class,
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
	StyledTextScrollbarTest.class,
	TableTest.class,
	TreeTest.class,
	TabbedPropertiesListTest.class,
	TabbedPropertiesTitleTest.class})
public class CssSwtTestSuite {

}
