/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

public class XMLModelReconcilerTestSuite extends TestSuite {

	public static Test suite() {
		return new XMLModelReconcilerTestSuite();
	}

	public XMLModelReconcilerTestSuite() {
		addTestSuite(XMLModelReconcilerApplicationTest.class);
		addTestSuite(XMLModelReconcilerApplicationElementTest.class);
		addTestSuite(XMLModelReconcilerBindingContainerTest.class);
		addTestSuite(XMLModelReconcilerBindingTableTest.class);
		addTestSuite(XMLModelReconcilerCommandTest.class);
		addTestSuite(XMLModelReconcilerContributionTest.class);
		addTestSuite(XMLModelReconcilerElementContainerTest.class);
		addTestSuite(XMLModelReconcilerGenericTileTest.class);
		addTestSuite(XMLModelReconcilerHandlerContainerTest.class);
		addTestSuite(XMLModelReconcilerHandlerTest.class);
		addTestSuite(XMLModelReconcilerHandledItemTest.class);
		addTestSuite(XMLModelReconcilerItemTest.class);
		addTestSuite(XMLModelReconcilerKeyBindingTest.class);
		addTestSuite(XMLModelReconcilerKeySequenceTest.class);
		addTestSuite(XMLModelReconcilerMenuTest.class);
		addTestSuite(XMLModelReconcilerMenuContributionTest.class);
		addTestSuite(XMLModelReconcilerMenuContributionsTest.class);
		addTestSuite(XMLModelReconcilerMenuItemTest.class);
		addTestSuite(XMLModelReconcilerParameterTest.class);
		addTestSuite(XMLModelReconcilerPartTest.class);
		addTestSuite(XMLModelReconcilerPartDescriptorTest.class);
		addTestSuite(XMLModelReconcilerPerspectiveTest.class);
		addTestSuite(XMLModelReconcilerPlaceholderTest.class);
		addTestSuite(XMLModelReconcilerToolBarContributionTest.class);
		addTestSuite(XMLModelReconcilerToolBarContributionsTest.class);
		addTestSuite(XMLModelReconcilerToolBarTest.class);
		addTestSuite(XMLModelReconcilerTrimContainerTest.class);
		addTestSuite(XMLModelReconcilerTrimContributionTest.class);
		addTestSuite(XMLModelReconcilerTrimContributionsTest.class);
		addTestSuite(XMLModelReconcilerUIElementTest.class);
		addTestSuite(XMLModelReconcilerUIItemTest.class);
		addTestSuite(XMLModelReconcilerWindowTest.class);

		addTestSuite(XMLModelReconcilerScenarioTest.class);

		addTestSuite(ModelReconcilingServiceTest.class);
	}

}
