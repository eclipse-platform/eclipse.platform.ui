/*******************************************************************************
 * Copyright (c) 2005, 2018, 2023 IBM Corporation and others.
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
 *     Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 *     Dawid Pakuła <zulus@w3des.net> - Bug 536785
 *     Nikifor Fedorov (ArSysOp) - Import more than one project at once (eclipse.platform#226)
 *
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.ui.tests.navigator.cdt.CdtTest;
import org.eclipse.ui.tests.navigator.jst.JstPipelineTest;
import org.eclipse.ui.tests.navigator.resources.FoldersAsProjectsContributionTest;
import org.eclipse.ui.tests.navigator.resources.NestedResourcesTests;
import org.eclipse.ui.tests.navigator.resources.PathComparatorTest;
import org.eclipse.ui.tests.navigator.resources.ResourceMgmtActionProviderTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ InitialActivationTest.class, ActionProviderTest.class, ExtensionsTest.class, FilterTest.class,
		WorkingSetTest.class, ActivityTest.class, OpenTest.class, INavigatorContentServiceTests.class,
		ProgrammaticOpenTest.class, PipelineTest.class, PipelineChainTest.class, JstPipelineTest.class,
		LabelProviderTest.class, SorterTest.class, ViewerTest.class, CdtTest.class, M12Tests.class,
		FirstClassM1Tests.class, LinkHelperTest.class, ShowInTest.class, ResourceTransferTest.class,
		EvaluationCacheTest.class, ResourceMgmtActionProviderTests.class,
		NestedResourcesTests.class, PathComparatorTest.class, FoldersAsProjectsContributionTest.class,
		GoBackForwardsTest.class
		// DnDTest.class, // DnDTest.testSetDragOperation() fails
		// PerformanceTest.class // Does not pass on all platforms see bug 264449
})
public final class NavigatorTestSuite {
}
