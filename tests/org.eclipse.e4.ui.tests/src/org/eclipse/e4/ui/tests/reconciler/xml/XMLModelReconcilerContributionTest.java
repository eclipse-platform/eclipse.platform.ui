/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler.xml;

import org.eclipse.e4.ui.internal.workbench.ModelReconcilingService;
import org.eclipse.e4.ui.tests.reconciler.ModelReconcilerContributionTest;
import org.eclipse.e4.ui.workbench.modeling.IModelReconcilingService;

public class XMLModelReconcilerContributionTest extends
		ModelReconcilerContributionTest {

	@Override
	protected IModelReconcilingService getModelReconcilingService() {
		return new ModelReconcilingService();
	}

	protected boolean serializedStateHasChanges(Object state) {
		assertTrue(state instanceof org.w3c.dom.Document);
		org.w3c.dom.Document doc = (org.w3c.dom.Document) state;
		assertTrue(doc.hasChildNodes() && doc.getChildNodes().getLength() == 1
				&& "changes".equals(doc.getFirstChild().getNodeName()));
		return doc.getFirstChild().hasChildNodes();
	}
}
