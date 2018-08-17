/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 137877, 152543, 152540, 116920, 164247, 164653,
 *                     159768, 170848, 147515
 *     Bob Smith - bug 198880
 *     Ashley Cambrell - bugs 198903, 198904
 *     Matthew Hall - bugs 210115, 212468, 212223, 206839, 208858, 208322,
 *                    212518, 215531, 221351, 184830, 213145, 218269, 239015,
 *                    237703, 237718, 222289, 247394, 233306, 247647, 254524,
 *                    246103, 249992, 256150, 256543, 262269, 175735, 262946,
 *                    255734, 263693, 169876, 266038, 268336, 270461, 271720,
 *                    283204, 281723, 283428
 *     Ovidio Mallo - bugs 237163, 235195, 299619, 306611, 305367
 *     Eugen Neufeld - bug 461560
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 492268
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import org.eclipse.core.tests.databinding.observable.AbstractObservableTest;
import org.eclipse.core.tests.databinding.observable.list.AbstractObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.ComputedListTest;
import org.eclipse.core.tests.databinding.observable.list.DecoratingObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.MultiListTest;
import org.eclipse.core.tests.databinding.observable.list.ObservableListTest;
import org.eclipse.core.tests.databinding.observable.list.WritableListTest;
import org.eclipse.core.tests.databinding.observable.set.AbstractObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.ComputedSetTest;
import org.eclipse.core.tests.databinding.observable.set.DecoratingObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.ObservableSetTest;
import org.eclipse.core.tests.databinding.observable.set.UnionSetTest;
import org.eclipse.core.tests.databinding.observable.set.WritableSetTest;
import org.eclipse.core.tests.databinding.observable.value.DecoratingObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.SelectObservableValueTest;
import org.eclipse.core.tests.databinding.observable.value.WritableValueTest;
import org.eclipse.core.tests.internal.databinding.beans.BeanObservableListDecoratorTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableArrayBasedListTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableArrayBasedSetTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableListTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableSetTest;
import org.eclipse.core.tests.internal.databinding.beans.JavaBeanObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.ConstantObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.DelayedObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.EmptyObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.EmptyObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.IdentityObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.MapEntryObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.StalenessObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.UnmodifiableObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.ValidatedObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableListTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableSetTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.DetailObservableValueTest;
import org.eclipse.core.tests.internal.databinding.observable.masterdetail.ListDetailValueObservableListTest;
import org.eclipse.jface.tests.internal.databinding.swt.ButtonObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.swt.CComboSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.CLabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.ComboObservableValueTextTest;
import org.eclipse.jface.tests.internal.databinding.swt.GroupObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.LabelObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.SWTDelayedObservableValueDecoratorTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.swt.ScaleObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.ShellObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueMaxTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueMinTest;
import org.eclipse.jface.tests.internal.databinding.swt.SpinnerObservableValueSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueDefaultSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.swt.StyledTextObservableValueModifyTest;
import org.eclipse.jface.tests.internal.databinding.swt.TableSingleSelectionObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextEditableObservableValueTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueDefaultSelectionTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueFocusOutTest;
import org.eclipse.jface.tests.internal.databinding.swt.TextObservableValueModifyTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ObservableViewerElementSetTest;
import org.eclipse.jface.tests.internal.databinding.viewers.ViewerInputObservableValueTest;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import junit.framework.TestSuite;

@RunWith(AllTests.class)
public class BindingTestSuiteJunit3 {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Contract Tests");
		AbstractObservableListTest.addConformanceTest(suite);
		AbstractObservableSetTest.addConformanceTest(suite);
		AbstractObservableTest.addConformanceTest(suite);
		BeanObservableListDecoratorTest.addConformanceTest(suite);
		ButtonObservableValueTest.addConformanceTest(suite);
		CComboObservableValueSelectionTest.addConformanceTest(suite);
		CComboObservableValueTextTest.addConformanceTest(suite);
		CComboSingleSelectionObservableValueTest.addConformanceTest(suite);
		CLabelObservableValueTest.addConformanceTest(suite);
		ComboObservableValueSelectionTest.addConformanceTest(suite);
		ComboObservableValueTextTest.addConformanceTest(suite);
		ComputedListTest.addConformanceTest(suite);
		ComputedSetTest.addConformanceTest(suite);
		ConstantObservableValueTest.addConformanceTest(suite);
		DecoratingObservableListTest.addConformanceTest(suite);
		DecoratingObservableSetTest.addConformanceTest(suite);
		DecoratingObservableValueTest.addConformanceTest(suite);
		DelayedObservableValueTest.addConformanceTest(suite);
		DetailObservableListTest.addConformanceTest(suite);
		DetailObservableSetTest.addConformanceTest(suite);
		DetailObservableValueTest.addConformanceTest(suite);
		EmptyObservableListTest.addConformanceTest(suite);
		EmptyObservableSetTest.addConformanceTest(suite);
		GroupObservableValueTest.addConformanceTest(suite);
		IdentityObservableSetTest.addConformanceTest(suite);
		JavaBeanObservableArrayBasedListTest.addConformanceTest(suite);
		JavaBeanObservableArrayBasedSetTest.addConformanceTest(suite);
		JavaBeanObservableListTest.addConformanceTest(suite);
		JavaBeanObservableSetTest.addConformanceTest(suite);
		JavaBeanObservableValueTest.addConformanceTest(suite);
		LabelObservableValueTest.addConformanceTest(suite);
		ListDetailValueObservableListTest.addConformanceTest(suite);
		MapEntryObservableValueTest.addConformanceTest(suite);
		MultiListTest.addConformanceTest(suite);
		ObservableListTest.addConformanceTest(suite);
		ObservableSetTest.addConformanceTest(suite);
		ObservableViewerElementSetTest.addConformanceTest(suite);
		ScaleObservableValueMaxTest.addConformanceTest(suite);
		ScaleObservableValueMinTest.addConformanceTest(suite);
		ScaleObservableValueSelectionTest.addConformanceTest(suite);
		SelectObservableValueTest.addConformanceTest(suite);
		ShellObservableValueTest.addConformanceTest(suite);
		SpinnerObservableValueMaxTest.addConformanceTest(suite);
		SpinnerObservableValueMinTest.addConformanceTest(suite);
		SpinnerObservableValueSelectionTest.addConformanceTest(suite);
		StalenessObservableValueTest.addConformanceTest(suite);
		StyledTextObservableValueDefaultSelectionTest.addConformanceTest(suite);
		StyledTextObservableValueFocusOutTest.addConformanceTest(suite);
		StyledTextObservableValueModifyTest.addConformanceTest(suite);
		SWTDelayedObservableValueDecoratorTest.addConformanceTest(suite);
		TableSingleSelectionObservableValueTest.addConformanceTest(suite);
		TextEditableObservableValueTest.addConformanceTest(suite);
		TextObservableValueDefaultSelectionTest.addConformanceTest(suite);
		TextObservableValueFocusOutTest.addConformanceTest(suite);
		TextObservableValueModifyTest.addConformanceTest(suite);
		UnionSetTest.addConformanceTest(suite);
		UnmodifiableObservableListTest.addConformanceTest(suite);
		UnmodifiableObservableSetTest.addConformanceTest(suite);
		UnmodifiableObservableValueTest.addConformanceTest(suite);
		ValidatedObservableListTest.addConformanceTest(suite);
		ValidatedObservableSetTest.addConformanceTest(suite);
		ValidatedObservableValueTest.addConformanceTest(suite);
		ViewerInputObservableValueTest.addConformanceTest(suite);
		WritableListTest.addConformanceTest(suite);
		WritableSetTest.addConformanceTest(suite);
		WritableValueTest.addConformanceTest(suite);
		return suite;
	}
}