/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 137877, 152543, 152540
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164247
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.masterdetail.DetailObservableValueTest;
import org.eclipse.core.tests.databinding.beans.BeansObservablesTest;
import org.eclipse.core.tests.databinding.observable.map.AbstractObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.ObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.WritableMapTest;
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
import org.eclipse.jface.tests.databinding.mask.EditMaskParserTest;
import org.eclipse.jface.tests.databinding.observable.ThreadRealmTest;
import org.eclipse.jface.tests.databinding.observable.list.AbstractObservableListTest;
import org.eclipse.jface.tests.databinding.observable.list.ObservableListTest;
import org.eclipse.jface.tests.databinding.observable.list.WritableListTest;
import org.eclipse.jface.tests.databinding.observable.value.AbstractObservableValueTest;
import org.eclipse.jface.tests.databinding.observable.value.AbstractVetoableValueTest;
import org.eclipse.jface.tests.databinding.observable.value.WritableValueTest;
import org.eclipse.jface.tests.databinding.scenarios.BindingScenariosTestSuite;
import org.eclipse.jface.tests.databinding.swt.TextObservableValueTests;
import org.eclipse.jface.tests.databinding.viewers.ObservableMapLabelProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableSetContentProviderTest;
import org.eclipse.jface.tests.examples.model.PersonTests;
import org.eclipse.jface.tests.internal.databinding.internal.ListBindingTest;
import org.eclipse.jface.tests.internal.databinding.internal.ValueBindingTest;
import org.eclipse.jface.tests.internal.databinding.internal.beans.JavaBeanObservableMapTest;
import org.eclipse.jface.tests.internal.databinding.internal.beans.JavaBeanObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.observable.UnmodifiableObservableListTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ButtonObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CComboObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CLabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ComboObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ControlObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.LabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.SpinnerObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.viewers.SelectionProviderSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.provisional.observable.AbstractObservableTest;
import org.eclipse.jface.tests.internal.databinding.provisional.observable.ObservablesTest;
import org.eclipse.jface.tests.internal.databinding.provisional.viewers.SelectionObservableValueTest;

public class BindingTestSuite extends TestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new BindingTestSuite();
    }

	public BindingTestSuite() {
		//org.eclipse.core.databinding.observable.masterdetail
		addTestSuite(DetailObservableValueTest.class);
		
		// org.eclipse.core.databinding.observable.masterdetail
		addTestSuite(DetailObservableValueTest.class);

		// org.eclipse.core.tests.databinding.observable.map
		addTestSuite(AbstractObservableMapTest.class);
		addTestSuite(ObservableMapTest.class);
		addTestSuite(WritableMapTest.class);
		
		// org.eclipse.core.internal.databinding.internal.beans
		addTestSuite(JavaBeanObservableValueTest.class);

		// org.eclipse.core.tests.databinding.beans
		addTestSuite(BeansObservablesTest.class);

		// org.eclipse.jface.internal.databinding.provisional.conversion
		addTestSuite(IdentityConverterTest.class);

		// org.eclipse.jface.internal.databinding.provisional.factories
		addTestSuite(DefaultBindSupportFactoryBooleanPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryBytePrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryDoublePrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryFloatPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryIntTest.class);
		addTestSuite(DefaultBindSupportFactoryLongPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryShortPrimitiveTest.class);
		addTestSuite(DefaultBindSupportFactoryTest.class);

		// org.eclipse.jface.internal.databinding.provisional.validation
		addTestSuite(ObjectToPrimitiveValidatorTest.class);

		// org.eclipse.jface.tests.databinding
		addTestSuite(BindSpecTests.class);
		addTestSuite(ComputedValueTest.class);
		addTestSuite(DatabindingContextTest.class);
		addTestSuite(IDiffsTest.class);
		addTestSuite(ObservableTest.class);
		addTestSuite(RandomAccessListIteratorTest.class);

		// org.eclipse.jface.tests.databinding.mask
		addTestSuite(EditMaskLexerAndTokenTest.class);
		addTestSuite(EditMaskParserTest.class);

		// org.eclipse.jface.tests.databinding.observable
		addTestSuite(ThreadRealmTest.class);
		
		// org.eclipse.jface.tests.databinding.observable.list
		addTestSuite(AbstractObservableListTest.class);
		addTestSuite(ObservableListTest.class);
		addTestSuite(WritableListTest.class);

		// org.eclipse.jface.tests.databinding.observable.value
		addTestSuite(AbstractObservableValueTest.class);
		addTestSuite(AbstractVetoableValueTest.class);
		addTestSuite(WritableValueTest.class);

		// org.eclipse.jface.tests.databinding.scenarios
		addTest(BindingScenariosTestSuite.suite());
		// The files in this package are in the above test suite

		// org.eclipse.jface.tests.databinding.swt
		// FIXME
		// addTestSuite(AbstractGetAndSetSelectionObservableCollectionTest.class);
		// FIXME
		// addTestSuite(AutoSelectTableViewerCollectionExtendedTest.class);
		// FIXME addTestSuite(AutoSelectTableViewerCollectionTest.class);
		// FIXME addTestSuite(CComboObservableCollectionTest.class);
		// FIXME addTestSuite(ComboObservableCollectionTest.class);
		// FIXME
		// addTestSuite(CopyOfAutoSelectTableViewerCollectionExtendedTest.class);
		// FIXME addTestSuite(ListObservableCollectionTest.class);
		// FIXME addTestSuite(ObservableCollectionViewerTest.class);
		// FIXME addTestSuite(TableViewerObservableCollectionTest.class);
		addTestSuite(TextObservableValueTests.class);

		// org.eclipse.jface.tests.databinding.views
		addTestSuite(ObservableMapLabelProviderTest.class);
		addTestSuite(ObservableSetContentProviderTest.class);

		// org.eclipse.jface.tests.examples.model
		addTestSuite(PersonTests.class);

		// org.eclipse.jface.tests.internal.databinding.internal
		addTestSuite(ListBindingTest.class);
		addTestSuite(ValueBindingTest.class);
		
		//org.eclipse.jface.tests.internal.databinding.internal.beans
		addTestSuite(JavaBeanObservableMapTest.class);
		addTestSuite(JavaBeanObservableValueTest.class);

		// org.eclipse.jface.tests.internal.databinding.internal.observable
		addTestSuite(UnmodifiableObservableListTest.class);

		// org.eclipse.jface.tests.internal.databinding.internal.swt
		addTestSuite(ButtonObservableValueTest.class);
		addTestSuite(CComboObservableValueTest.class);
		addTestSuite(CLabelObservableValueTest.class);
		addTestSuite(ComboObservableValueTest.class);
		addTestSuite(ControlObservableValueTest.class);
		addTestSuite(LabelObservableValueTest.class);
		addTestSuite(SpinnerObservableValueTest.class);
		addTestSuite(TableObservableValueTest.class);

		// org.eclipse.jface.tests.internal.databinding.internal.viewers
		addTestSuite(SelectionProviderSingleSelectionObservableValueTest.class);
		// FIXME addTestSuite(SVOCWLTest.class);

		// org.eclipse.jface.tests.internal.databinding.provisional.observable
		addTestSuite(AbstractObservableTest.class);
		addTestSuite(ObservablesTest.class);

		// org.eclipse.jface.tests.internal.databinding.provisional.viewers
		addTestSuite(SelectionObservableValueTest.class);
	}

	/**
	 * @param testCase
	 *            TODO
	 * @return true if the given test is temporarily disabled
	 */
	public static boolean failingTestsDisabled(TestCase testCase) {
		System.out.println("Ignoring disabled test: " + testCase.getClass().getName() + "." + testCase.getName());
		return true;
	}

	/**
	 * @param testSuite
	 *            TODO
	 * @return true if the given test is temporarily disabled
	 */
	public static boolean failingTestsDisabled(TestSuite testSuite) {
		System.out.println("Ignoring disabled test: " + testSuite.getClass().getName() + "." + testSuite.getName());
		return true;
	}
}