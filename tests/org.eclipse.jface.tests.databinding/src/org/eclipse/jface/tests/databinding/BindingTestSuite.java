/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jface.tests.databinding.scenarios.BindingScenariosTestSuite;

public class BindingTestSuite extends TestSuite {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new BindingTestSuite();
	}

	public BindingTestSuite() {
		addTestSuite(ObservableTest.class);
		addTestSuite(JavaBeansScalarObservableValueFactoryTest.class);
		addTestSuite(DatabindingContextTest.class);
		//addTestSuite(ObservableCollectionTest.class);
		addTestSuite(SelectionAwareObservableCollectionTest.class);
		addTest(BindingScenariosTestSuite.suite());
	}

	/**
	 * @return
	 */
	public static boolean failingTestsDisabled() {
		return true;
	}
}
