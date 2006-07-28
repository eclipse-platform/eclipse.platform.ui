/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor.NewObject;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;

/**
 * @since 3.2
 *
 */
public class LazyListBindingTest extends TestCase {
	
	/*
	 * Concerns:
	 * 
	 * 1) When the ILazyDataRequestor requests rows, the binding correctly
	 *    delegates to the ILazyDataProvider and returns the requested
	 *    rows.
	 *    
	 * 2) When an insert or delete request occurs from the ILazyDataRequestor,
	 *    the binding correctly delegates to its insert/delete handler and
	 *    does whatever is necessary to avoid CoModificationException after
	 *    performing the insert or delete.
	 *    
	 * 3) When the ILazyDataProvider (normally an IObservableList implementation)
	 *    sends a change event, the delta is properly processed and the correct
	 *    rows are added or removed to/from the ILazyDataRequestor.
	 */
	
	private WritableList model;
	private LazyDataRequestor target;
	private Binding binding;

	private DataBindingContext getDBC() {
		DataBindingContext dbc = new DataBindingContext();
		dbc.addBindingFactory(new DefaultBindingFactory());
		return dbc;
	}

	private void compareList(List windowData, String[] strings) {
		int i = 0;
		for (Iterator iter = windowData.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			assertTrue("Element not equal: " + element + " <equals> " + strings[i], element.equals(strings[i]));
			++i;
		}
	}
	
	private LazyInsertDeleteProvider insertDeleteProvider = new LazyInsertDeleteProvider() {
		private static final String TWO = "Two";
		
		public NewObject insertElementAt(LazyInsertEvent e) {
			model.add(1, TWO);
			return new ILazyDataRequestor.NewObject(1, TWO);
		}
		
		public boolean canDeleteElementAt(LazyDeleteEvent e) {
			return true;
		}
		
		public void deleteElementAt(LazyDeleteEvent e) {
			model.remove(e.position);
		}
	};
	
	protected void setUp() throws Exception {
		model = new WritableList(String.class);
		model.add("Un");
		model.add("Deux");
		model.add("Trois");
		model.add("Quatre");
		model.add("Cinq");
		target = new LazyDataRequestor(3);
		
		DataBindingContext dbc = getDBC();
		
		binding = dbc.bind(target, model, new BindSpec().setLazyInsertDeleteProvider(insertDeleteProvider));
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		binding.dispose();
	}
	
	public void testSetSize_ElementProviderHasTheRightStuff() throws Exception {
		compareList(target.getWindowData(), new String[] {"Un", "Deux", "Trois"});
	}

	public void testInsertAndDeleteFromTarget() throws Exception {
		target.requestInsert(null);
		compareList(target.getWindowData(), new String[] {"Un", "Two", "Deux"});
		compareList(model, new String[] {"Un", "Two", "Deux", "Trois", "Quatre", "Cinq"});
		target.requestDelete(1);
		compareList(target.getWindowData(), new String[] {"Un", "Deux", "Trois"});
		compareList(model, new String[] {"Un", "Deux", "Trois", "Quatre", "Cinq"});
		target.requestDelete(1);
		compareList(target.getWindowData(), new String[] {"Un", "Trois", "Quatre"});
		compareList(model, new String[] {"Un", "Trois", "Quatre", "Cinq"});
	}
	
	public void testInsertAndDeleteFromModel() throws Exception {
		model.add(1, "Two");
		compareList(target.getWindowData(), new String[] {"Un", "Two", "Deux"});
		compareList(model, new String[] {"Un", "Two", "Deux", "Trois", "Quatre", "Cinq"});
		model.remove(1);
		compareList(target.getWindowData(), new String[] {"Un", "Deux", "Trois"});
		compareList(model, new String[] {"Un", "Deux", "Trois", "Quatre", "Cinq"});
		model.remove(1);
		compareList(target.getWindowData(), new String[] {"Un", "Trois", "Quatre"});
		compareList(model, new String[] {"Un", "Trois", "Quatre", "Cinq"});
	}
}

