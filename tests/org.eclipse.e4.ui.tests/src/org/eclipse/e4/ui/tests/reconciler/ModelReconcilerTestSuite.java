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

package org.eclipse.e4.ui.tests.reconciler;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.e4.ui.tests.reconciler.xml.XMLModelReconcilerTestSuite;
import org.junit.runner.RunWith;

@RunWith(org.junit.runners.AllTests.class)
public class ModelReconcilerTestSuite extends TestSuite {

	public static Test suite() {
		return new ModelReconcilerTestSuite();
	}

	public ModelReconcilerTestSuite() {
		addTestSuite(E4XMIResourceFactoryTest.class);
		addTest(XMLModelReconcilerTestSuite.suite());
	}

}
