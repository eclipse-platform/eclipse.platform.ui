/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.tests.api.workbenchpart.ArbitraryPropertyTest;
import org.eclipse.ui.tests.api.workbenchpart.Bug543609Test;
import org.eclipse.ui.tests.api.workbenchpart.DependencyInjectionViewTest;
import org.eclipse.ui.tests.api.workbenchpart.LifecycleViewTest;
import org.eclipse.ui.tests.api.workbenchpart.OverriddenTitleTest;
import org.eclipse.ui.tests.api.workbenchpart.RawIViewPartTest;
import org.eclipse.ui.tests.api.workbenchpart.ViewPartTitleTest;
import org.eclipse.ui.tests.ide.api.FileEditorInputTest;
import org.eclipse.ui.tests.ide.api.IDETest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	 IPageLayoutTest.class,
	 PlatformUITest.class,
	 IWorkbenchTest.class,
	 IWorkbenchWindowTest.class,
	 IWorkbenchPageTest.class,
	 IDeprecatedWorkbenchPageTest.class,
	 IActionFilterTest.class,
	 IPageListenerTest.class,
	 IAggregateWorkingSetTest.class,
	 IPageServiceTest.class,
	 IPerspectiveRegistryTest.class,
	 IPerspectiveDescriptorTest.class,
	 IFileEditorMappingTest.class,
	 IEditorDescriptorTest.class,
	 IEditorRegistryTest.class,
	 IPerspectiveListenerTest.class,
	 IWorkbenchWindowActionDelegateTest.class,
	 IViewActionDelegateTest.class,
	 IViewSiteTest.class,
	 IEditorSiteTest.class,
	 IActionBarsTest.class,
	 IViewPartTest.class,
	 IEditorPartTest.class,
	 IEditorActionBarContributorTest.class,
	 IPartServiceTest.class,
	 ISelectionServiceTest.class,
	 IWorkingSetTest.class,
	 IWorkingSetManagerTest.class,
	 IWorkingSetElementAdapterTests.class,
	 MockWorkingSetTest.class,
	 Bug42616Test.class,
	 StickyViewTest.class,
	 EditorIconTest.class,
	 RawIViewPartTest.class,
	 ViewPartTitleTest.class,
	 OverriddenTitleTest.class,
	 UIJobTest.class,
	 Bug75118Test.class,
	 FileEditorInputTest.class,
	 IDETest.class,
	 IEditorMatchingStrategyTest.class,
	 XMLMementoTest.class,
	 //IWorkbenchPartTestableTests.class,
	 ArbitraryPropertyTest.class,
	 LifecycleViewTest.class,
	 DependencyInjectionViewTest.class,
	 Bug407422Test.class,
	 MultipleWindowsTest.class,
	 Bug543609Test.class,
	 SaveablesListTest.class
})
public class ApiTestSuite {

}
