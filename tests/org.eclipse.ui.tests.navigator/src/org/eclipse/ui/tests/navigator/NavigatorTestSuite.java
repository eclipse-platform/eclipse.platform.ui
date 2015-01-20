/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.ui.tests.navigator.cdt.CdtTest;
import org.eclipse.ui.tests.navigator.jst.JstPipelineTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ InitialActivationTest.class, ActionProviderTest.class, ExtensionsTest.class, FilterTest.class,
		WorkingSetTest.class, ActivityTest.class, OpenTest.class, INavigatorContentServiceTests.class,
		ProgrammaticOpenTest.class, PipelineTest.class, PipelineChainTest.class, JstPipelineTest.class,
		LabelProviderTest.class, SorterTest.class, ViewerTest.class, CdtTest.class, M12Tests.class,
		FirstClassM1Tests.class, LinkHelperTest.class, ResourceTransferTest.class
		// DnDTest.class, // DnDTest.testSetDragOperation() fails
		// PerformanceTest.class // Does not pass on all platforms see bug 264449
})
public final class NavigatorTestSuite {


}
