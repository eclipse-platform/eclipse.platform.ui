/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
 *     Brad Reynolds - bug 164653, 159768, 170848, 147515
 *     Bob Smith - bug 198880
 *     Ashley Cambrell - bugs 198903, 198904
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.tests.databinding.AggregateValidationStatusTest;
import org.eclipse.core.tests.databinding.DatabindingContextTest;
import org.eclipse.core.tests.databinding.ListBindingTest;
import org.eclipse.core.tests.databinding.ObservablesManagerTest;
import org.eclipse.core.tests.databinding.UpdateStrategyTest;
import org.eclipse.core.tests.databinding.UpdateValueStrategyTest;
import org.eclipse.core.tests.databinding.ValueBindingTest;
import org.eclipse.core.tests.databinding.beans.BeansObservablesTest;
import org.eclipse.core.tests.databinding.conversion.NumberToStringConverterTest;
import org.eclipse.core.tests.databinding.conversion.StringToNumberConverterTest;
import org.eclipse.core.tests.databinding.observable.AbstractObservableTest;
import org.eclipse.core.tests.databinding.observable.DiffsTest;
import org.eclipse.core.tests.databinding.observable.Diffs_ListDiffTests;
import org.eclipse.core.tests.databinding.observable.ObservablesTest;
import org.eclipse.core.tests.databinding.observable.RealmTest;
import org.eclipse.core.tests.databinding.observable.list.AbstractObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.ObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.WritableListTest;
import org.eclipse.core.tests.databinding.observable.map.AbstractObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.ObservableMapTest;
import org.eclipse.core.tests.databinding.observable.map.WritableMapTest;
import org.eclipse.core.tests.databinding.observable.set.AbstractObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.ObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.UnionSetTest;
import org.eclipse.core.tests.databinding.observable.set.WritableSetTest;
import org.eclipse.core.tests.databinding.observable.value.AbstractObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.AbstractVetoableValueTest;
import org.eclipse.core.tests.databinding.observable.value.ComputedValueTest;
import org.eclipse.core.tests.databinding.observable.value.WritableValueTest;
import org.eclipse.core.tests.databinding.validation.ValidationStatusTest;
import org.eclipse.core.tests.internal.databinding.BindingMessagesTest;
import org.eclipse.core.tests.internal.databinding.BindingStatusTest;
import org.eclipse.core.tests.internal.databinding.QueueTest;
import org.eclipse.core.tests.internal.databinding.RandomAccessListIteratorTest;
import org.eclipse.core.tests.internal.databinding.conversion.DateConversionSupportTest;
import org.eclipse.core.tests.internal.databinding.conversion.IdentityConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.IntegerToStringConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToBigDecimalTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToBigIntegerConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToByteConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToDoubleConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToFloatConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToIntegerConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToLongConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.NumberToShortConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.ObjectToPrimitiveValidatorTest;
import org.eclipse.core.tests.internal.databinding.conversion.StatusToStringConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToBooleanConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToByteConverterTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserByteTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserDoubleTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserFloatTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserIntegerTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserLongTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserShortTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTest;
import org.eclipse.core.tests.internal.databinding.conversion.StringToShortConverterTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.BeanObservableListDecoratorTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.BeanObservableSetDecoratorTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.BeanObservableValueDecoratorTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.JavaBeanObservableArrayBasedListTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.JavaBeanObservableListTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.JavaBeanObservableMapTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.JavaBeanObservableSetTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.JavaBeanObservableValueTest;
import org.eclipse.core.tests.internal.databinding.internal.beans.ListenerSupportTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableValueTest;
import org.eclipse.core.tests.internal.databinding.validation.AbstractStringToNumberValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToByteValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToDoubleValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToFloatValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToIntegerValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToLongValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToShortValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.NumberToUnboundedNumberValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToByteValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToDoubleValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToFloatValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToIntegerValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToLongValidatorTest;
import org.eclipse.core.tests.internal.databinding.validation.StringToShortValidatorTest;
import org.eclipse.jface.tests.databinding.scenarios.BindingScenariosTestSuite;
import org.eclipse.jface.tests.databinding.swt.SWTObservablesTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableMapLabelProviderTest;
import org.eclipse.jface.tests.databinding.viewers.ObservableSetContentProviderTest;
import org.eclipse.jface.tests.examples.databinding.mask.internal.EditMaskLexerAndTokenTest;
import org.eclipse.jface.tests.examples.databinding.mask.internal.EditMaskParserTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ButtonObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CComboObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CComboSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.CLabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ComboObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ComboSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ControlObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.LabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ListSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ScaleObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ScaleObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.ScaleObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.SpinnerObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.SpinnerObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.SpinnerObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.SpinnerObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TableSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TextEditableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TextObservableValueModifyTest;
import org.eclipse.jface.tests.internal.databinding.internal.swt.TextObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.internal.viewers.SelectionProviderMultiSelectionObservableListTest;
import org.eclipse.jface.tests.internal.databinding.internal.viewers.SelectionProviderSingleSelectionObservableValueTest;

public class BindingTestSuite extends TestSuite {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new BindingTestSetup(new BindingTestSuite());
	}

	public BindingTestSuite() {
		// org.eclipse.core.tests.databinding
		addTestSuite(AggregateValidationStatusTest.class);
		addTestSuite(DatabindingContextTest.class);
		addTestSuite(ListBindingTest.class);
		addTestSuite(UpdateStrategyTest.class);
		addTestSuite(UpdateValueStrategyTest.class);
		addTestSuite(ValueBindingTest.class);
		addTestSuite(ObservablesManagerTest.class);

		// org.eclipse.core.tests.databinding.beans
		addTestSuite(BeansObservablesTest.class);

		// org.eclipse.core.tests.databinding.conversion
		addTestSuite(NumberToStringConverterTest.class);
		addTestSuite(StringToNumberConverterTest.class);

		// org.eclipse.core.tests.databinding.observable
		addTest(AbstractObservableTest.suite());
		addTestSuite(Diffs_ListDiffTests.class);
		addTestSuite(DiffsTest.class);
		addTestSuite(ObservablesTest.class);
		addTestSuite(RealmTest.class);

		// org.eclipse.core.tests.databinding.observable.list
		addTest(AbstractObservableListTest.suite());
		addTest(ObservableListTest.suite());
		addTest(WritableListTest.suite());

		// org.eclipse.core.tests.databinding.observable.map
		addTestSuite(AbstractObservableMapTest.class);
		addTestSuite(ObservableMapTest.class);
		addTestSuite(WritableMapTest.class);

		// org.eclipse.core.tests.databinding.observable.set
		addTest(AbstractObservableSetTest.suite());
		addTest(ObservableSetTest.suite());
		addTest(UnionSetTest.suite());
		addTest(WritableSetTest.suite());
		
		//org.eclipse.core.tests.databinding.observable.value
		addTestSuite(AbstractObservableValueTest.class);
		addTestSuite(AbstractVetoableValueTest.class);
		addTestSuite(ComputedValueTest.class);
		addTest(WritableValueTest.suite());
		
		//org.eclipse.core.tests.databinding.validation
		addTestSuite(ValidationStatusTest.class);
		
		// org.eclipse.core.tests.internal.databinding
		addTestSuite(BindingMessagesTest.class);
		addTestSuite(BindingStatusTest.class);
		addTestSuite(RandomAccessListIteratorTest.class);
		addTestSuite(QueueTest.class);

		// org.eclipse.core.tests.internal.databinding.conversion
		addTestSuite(DateConversionSupportTest.class);
		addTestSuite(IdentityConverterTest.class);
		addTestSuite(IntegerToStringConverterTest.class);
		addTestSuite(NumberToBigDecimalTest.class);
		addTestSuite(NumberToBigIntegerConverterTest.class);
		addTestSuite(NumberToByteConverterTest.class);
		addTestSuite(NumberToDoubleConverterTest.class);
		addTestSuite(NumberToFloatConverterTest.class);
		addTestSuite(NumberToIntegerConverterTest.class);
		addTestSuite(NumberToLongConverterTest.class);
		addTestSuite(NumberToShortConverterTest.class);
		addTestSuite(ObjectToPrimitiveValidatorTest.class);
		addTestSuite(StatusToStringConverterTest.class);
		addTestSuite(StringToBooleanConverterTest.class);
		addTestSuite(StringToByteConverterTest.class);
		addTestSuite(StringToNumberParserByteTest.class);
		addTestSuite(StringToNumberParserDoubleTest.class);
		addTestSuite(StringToNumberParserFloatTest.class);
		addTestSuite(StringToNumberParserIntegerTest.class);
		addTestSuite(StringToNumberParserLongTest.class);
		addTestSuite(StringToNumberParserShortTest.class);
		addTestSuite(StringToNumberParserTest.class);
		addTestSuite(StringToShortConverterTest.class);

		//org.eclipse.core.tests.internal.databinding.internal.beans
		addTestSuite(BeanObservableListDecoratorTest.class);
		addTestSuite(BeanObservableSetDecoratorTest.class);
		addTestSuite(BeanObservableValueDecoratorTest.class);
		addTestSuite(BeanObservableListDecoratorTest.class);
		addTestSuite(JavaBeanObservableArrayBasedListTest.class);
		addTestSuite(JavaBeanObservableListTest.class);
		addTestSuite(JavaBeanObservableMapTest.class);
		addTestSuite(JavaBeanObservableSetTest.class);
		addTest(JavaBeanObservableValueTest.suite());
		addTestSuite(ListenerSupportTest.class);
		
		//org.eclipse.core.tests.internal.databinding.observable
		addTestSuite(UnmodifiableObservableListTest.class);
		
		// org.eclipse.core.tests.internal.databinding.observable.masterdetail
		addTestSuite(DetailObservableListTest.class);
		addTestSuite(DetailObservableSetTest.class);
		addTestSuite(DetailObservableValueTest.class);

		// org.eclipse.core.tests.internal.databinding.validation
		addTestSuite(AbstractStringToNumberValidatorTest.class);
		addTestSuite(NumberToByteValidatorTest.class);
		addTestSuite(NumberToDoubleValidatorTest.class);
		addTestSuite(NumberToFloatValidatorTest.class);
		addTestSuite(NumberToIntegerValidatorTest.class);
		addTestSuite(NumberToLongValidatorTest.class);
		addTestSuite(NumberToShortValidatorTest.class);
		addTestSuite(NumberToUnboundedNumberValidatorTest.class);
		addTestSuite(StringToByteValidatorTest.class);
		addTestSuite(StringToDoubleValidatorTest.class);
		addTestSuite(StringToFloatValidatorTest.class);
		addTestSuite(StringToIntegerValidatorTest.class);
		addTestSuite(StringToLongValidatorTest.class);
		addTestSuite(StringToShortValidatorTest.class);

		// org.eclipse.jface.tests.databinding.scenarios
		addTest(BindingScenariosTestSuite.suite());
		// The files in this package are in the above test suite

		//org.eclipse.jface.tests.databinding.swt
		addTestSuite(SWTObservablesTest.class);
		
		// org.eclipse.jface.tests.databinding.viewers
		addTestSuite(ObservableMapLabelProviderTest.class);
		addTestSuite(ObservableSetContentProviderTest.class);
		
		//org.eclipse.jface.tests.example.databinding.mask.internal
		addTestSuite(EditMaskLexerAndTokenTest.class);
		addTestSuite(EditMaskParserTest.class);

		//org.eclipse.jface.tests.internal.databinding.internal.swt
		addTest(ButtonObservableValueTest.suite());
		addTestSuite(CComboObservableValueTest.class);
		addTest(CComboObservableValueSelectionTest.suite());
		addTest(CComboObservableValueTextTest.suite());
		addTestSuite(CComboSingleSelectionObservableValueTest.class);
		addTest(CComboSingleSelectionObservableValueTest.suite());
		addTest(CLabelObservableValueTest.suite());
		addTestSuite(ComboObservableValueTest.class);
		addTest(ComboObservableValueSelectionTest.suite());
		addTest(ComboObservableValueTextTest.suite());
		addTestSuite(ComboSingleSelectionObservableValueTest.class);
		
		addTestSuite(ControlObservableValueTest.class);
		addTest(LabelObservableValueTest.suite());
		addTestSuite(ListSingleSelectionObservableValueTest.class);
		addTest(ScaleObservableValueMinTest.suite());
		addTest(ScaleObservableValueMaxTest.suite());
		addTest(ScaleObservableValueSelectionTest.suite());
		
		addTestSuite(SpinnerObservableValueTest.class);
		addTest(SpinnerObservableValueMinTest.suite());
		addTest(SpinnerObservableValueMaxTest.suite());
		addTest(SpinnerObservableValueSelectionTest.suite());
		
		addTestSuite(TableObservableValueTest.class);
		addTest(TableSingleSelectionObservableValueTest.suite());
		addTest(TextEditableObservableValueTest.suite());
		addTest(TextObservableValueFocusOutTest.suite());
		addTest(TextObservableValueModifyTest.suite());
		addTestSuite(TextObservableValueTest.class);
		
		//org.eclipse.jface.tests.internal.databinding.internal.viewers
		addTestSuite(SelectionProviderMultiSelectionObservableListTest.class);
		addTestSuite(SelectionProviderSingleSelectionObservableValueTest.class);
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