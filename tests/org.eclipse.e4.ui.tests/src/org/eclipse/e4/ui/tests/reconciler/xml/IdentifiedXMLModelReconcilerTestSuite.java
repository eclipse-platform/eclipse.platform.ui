/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

public class IdentifiedXMLModelReconcilerTestSuite extends TestSuite {

	public static Test suite() {
		return new IdentifiedXMLModelReconcilerTestSuite();
	}

	public IdentifiedXMLModelReconcilerTestSuite() {
		addTestSuite(IdentifiedXMLModelReconcilerApplicationTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerBindingContainerTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerCommandTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerContributionTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerElementContainerTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerGenericTileTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerHandlerContainerTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerHandlerTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerHandledItemTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerItemTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerKeyBindingTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerKeySequenceTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerPartTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerTrimContainerTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerViewSashContainerTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerViewStackTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerUIElementTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerUIItemTest.class);
		addTestSuite(IdentifiedXMLModelReconcilerWindowTest.class);

		addTestSuite(IdentifiedXMLModelReconcilerScenarioTest.class);
	}

}
