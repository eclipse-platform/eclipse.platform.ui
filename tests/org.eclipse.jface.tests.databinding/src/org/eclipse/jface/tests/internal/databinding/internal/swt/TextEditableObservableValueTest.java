/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.internal.swt.TextEditableObservableValue;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.1
 */
public class TextEditableObservableValueTest extends
		AbstractSWTTestCase {
	
	private Text text;
	private TextEditableObservableValue value; 

	protected void setUp() throws Exception {
		super.setUp();
		
		text = new Text(getShell(), SWT.NONE);
		value = new TextEditableObservableValue(text);		
	}
	
	public void testGetValue() throws Exception {
		text.setEditable(false);
		assertEquals(Boolean.valueOf(text.getEditable()), value.getValue());
		
		text.setEditable(true);
		assertEquals(Boolean.valueOf(text.getEditable()), value.getValue());
	}
	
	public void testSetValue() throws Exception {
		text.setEditable(false);
		value.setValue(Boolean.TRUE);
		assertEquals(Boolean.TRUE, Boolean.valueOf(text.getEditable()));
		
		value.setValue(Boolean.FALSE);
		assertEquals(Boolean.FALSE, Boolean.valueOf(text.getEditable()));
	}
	
	public void testGetType() throws Exception {
		assertEquals(Boolean.TYPE, value.getValueType());
	}
}
