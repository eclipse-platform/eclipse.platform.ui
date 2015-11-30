/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler.xml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ XMLModelReconcilerApplicationTest.class, XMLModelReconcilerApplicationElementTest.class,
		XMLModelReconcilerBindingContainerTest.class, XMLModelReconcilerBindingTableTest.class,
		XMLModelReconcilerCommandTest.class, XMLModelReconcilerContributionTest.class,
		XMLModelReconcilerElementContainerTest.class, XMLModelReconcilerGenericTileTest.class,
		XMLModelReconcilerHandlerContainerTest.class, XMLModelReconcilerHandlerTest.class,
		XMLModelReconcilerHandledItemTest.class, XMLModelReconcilerItemTest.class,
		XMLModelReconcilerKeyBindingTest.class, XMLModelReconcilerKeySequenceTest.class,
		XMLModelReconcilerMenuTest.class, XMLModelReconcilerMenuContributionTest.class,
		XMLModelReconcilerMenuContributionsTest.class, XMLModelReconcilerMenuItemTest.class,
		XMLModelReconcilerParameterTest.class, XMLModelReconcilerPartTest.class,
		XMLModelReconcilerPartDescriptorTest.class, XMLModelReconcilerPerspectiveTest.class,
		XMLModelReconcilerPlaceholderTest.class, XMLModelReconcilerToolBarContributionTest.class,
		XMLModelReconcilerToolBarContributionsTest.class, XMLModelReconcilerToolBarTest.class,
		XMLModelReconcilerTrimContainerTest.class, XMLModelReconcilerTrimContributionTest.class,
		XMLModelReconcilerTrimContributionsTest.class, XMLModelReconcilerUIElementTest.class,
		XMLModelReconcilerUIItemTest.class, XMLModelReconcilerWindowTest.class,

		XMLModelReconcilerScenarioTest.class,

		ModelReconcilingServiceTest.class,

})
public class XMLModelReconcilerTestSuite {
}
