/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.internal.databinding.provisional.conversion.IdentityConverterTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryBooleanPrimitiveTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryBytePrimitiveTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryDoublePrimitiveTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryFloatPrimitiveTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryIntTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryLongPrimitiveTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryShortPrimitiveTest;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactoryTest;
import org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimitiveValidatorTest;
import org.eclipse.jface.tests.databinding.mask.EditMaskLexerAndTokenTest;
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
		addTestSuite(DefaultBindSupportFactoryTest.class);
		addTestSuite(IDiffsTest.class);
		// addTestSuite(ObservableCollectionTest.class);
		addTestSuite(SelectionAwareObservableCollectionTest.class);
		addTest(BindingScenariosTestSuite.suite());
		addTestSuite(DefaultBindSupportFactoryIntTest.class);
		addTestSuite(DefaultBindSupportFactoryDoublePrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryBytePrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryLongPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryShortPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryBooleanPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryFloatPrimitiveTest.class);
		addTestSuite(ObjectToPrimitiveValidatorTest.class);
		addTestSuite(IdentityConverterTest.class);
		addTestSuite(LazyListBindingTest.class);
		addTestSuite(EventEditorObservableLazyDataRequestorTest.class);
		addTestSuite(EditMaskLexerAndTokenTest.class);
	}

	/**
	 * @param testCase
	 *            TODO
	 * @return true if the given test is temporarily disabled
	 */
	public static boolean failingTestsDisabled(TestCase testCase) {
		System.out.println("Ignoring disabled test: "
				+ testCase.getClass().getName() + "." + testCase.getName());
		return true;
	}

	/**
	 * @param testSuite
	 *            TODO
	 * @return true if the given test is temporarily disabled
	 */
	public static boolean failingTestsDisabled(TestSuite testSuite) {
		System.out.println("Ignoring disabled test: "
				+ testSuite.getClass().getName() + "." + testSuite.getName());
		return true;
	}
}