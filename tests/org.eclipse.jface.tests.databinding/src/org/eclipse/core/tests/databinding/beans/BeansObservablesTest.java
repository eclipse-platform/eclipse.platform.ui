/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164268
 ******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class BeansObservablesTest extends TestCase {
	String[] elements = null;
	Model model = null;
	Class elementType = null;
	
	protected void setUp() throws Exception {
		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
		
		elements = new String[] {"1", "2", "3"};
		model = new Model(elements);
		elementType = String.class;
	}
	
	public void testObserveListInferredAttributeType() throws Exception {
		IObservableList list = BeansObservables.observeList(Realm.getDefault(), model, "valuesArray", null);
		assertEquals("element type", elementType, list.getElementType());
	}
	
	public void testObserveListNonInferredAttributeType() throws Exception {
		elementType = Object.class;
		IObservableList list = BeansObservables.observeList(Realm.getDefault(), model, "values", null);
		assertEquals("element type", elementType, list.getElementType());
	}
	
	public void testListFactory() throws Exception {
		IObservableFactory factory = BeansObservables.listFactory(Realm.getDefault(), "values", elementType);		
		IObservableList list = (IObservableList) factory.createObservable(model);
		
		assertTrue("elements of the list", Arrays.equals(elements, list.toArray(new String[list.size()])));
		assertEquals("element type", elementType, list.getElementType());
	}
	
	public void testObserveDetailListElementType() throws Exception {
		WritableValue parent = new WritableValue(Model.class);
		parent.setValue(model);
		IObservableList list = BeansObservables.observeDetailList(Realm.getDefault(), parent, "values", elementType);
		
		assertEquals("element type", elementType, list.getElementType());
		assertTrue("elements of list", Arrays.equals(elements, list.toArray(new String[list.size()])));
	}
	
	private static class Model {
		private List values = new ArrayList();
		
		public Model(String[] values) {
			this.values.addAll(Arrays.asList(values));
		}
		
		public List getValues() {
			return values;
		}
		
		public String[] getValuesArray() {
			return (String[]) values.toArray(new String[values.size()]);
		}
	}
}
