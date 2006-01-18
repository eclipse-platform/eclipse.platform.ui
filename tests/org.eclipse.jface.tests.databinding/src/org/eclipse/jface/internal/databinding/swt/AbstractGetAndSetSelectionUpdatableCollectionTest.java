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

package org.eclipse.jface.internal.databinding.swt;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.SelectionAwareUpdatableCollection;

/**
 * @since 3.2
 *
 */
public abstract class AbstractGetAndSetSelectionUpdatableCollectionTest extends TestCase {

	public void testGetSelectedObject() {		
		SelectionAwareUpdatableCollection updatable = getSelectionAwareUpdatable(new String[] {"foo", "bar"}); 
		assertEquals("No initial selection should be found.", null, updatable.getSelectedObject());
		setSelectedValueOfControl("bar");
		assertEquals("bar", updatable.getSelectedObject());	
	}

	/*
	 * Test method for 'org.eclipse.jface.internal.databinding.swt.CComboUpdatableCollection.setSelectedObject(Object)'
	 */
	public void testSetSelectedObject() {
		SelectionAwareUpdatableCollection updatable = getSelectionAwareUpdatable(new String[] {"foo", "bar"}); 
		updatable.setSelectedObject("bar");
		assertEquals("bar", getSelectedObjectOfControl());
		updatable.setSelectedObject("bar1");
		assertEquals("bar", getSelectedObjectOfControl());
		updatable.setSelectedObject("foo");
		assertEquals("foo", getSelectedObjectOfControl());
		updatable.setSelectedObject(null);
		assertEquals(null, getSelectedObjectOfControl());		
	}
	
	protected abstract SelectionAwareUpdatableCollection getSelectionAwareUpdatable(String[] values);
	
	protected abstract Object getSelectedObjectOfControl();
	
	protected abstract void setSelectedValueOfControl(String value);
}
