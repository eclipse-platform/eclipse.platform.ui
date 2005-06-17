/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.ui.PlatformUI;

/**
 * Tests the FindReplaceDialog.
 * 
 * @since 3.1
 */
public class FindReplaceDialogTest extends TestCase {
	
	private Accessor fFindReplaceDialog;
	
	
	public FindReplaceDialogTest(String name) {
		super(name);
	}
	
	
	protected void setUp() {
		Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		fFindReplaceDialog= new Accessor("org.eclipse.ui.texteditor.FindReplaceDialog", getClass().getClassLoader(), new Object[] {shell});
		fFindReplaceDialog.invoke("create", null);
	}
	
	public static Test suite() {
		return new TestSuite(FindReplaceDialogTest.class); 
	}
	
	protected void tearDown () {
		fFindReplaceDialog.invoke("close", null);
		fFindReplaceDialog= null;
	}
	
	public void testInitialButtonState() {
		Boolean value;
		value= (Boolean)fFindReplaceDialog.invoke("isWholeWordSearch", null);
		assertFalse(value.booleanValue());
		value= (Boolean)fFindReplaceDialog.invoke("isWholeWordSetting", null);
		assertFalse(value.booleanValue());
		value= (Boolean)fFindReplaceDialog.invoke("isWrapSearch", null);
		assertFalse(value.booleanValue());
		value= (Boolean)fFindReplaceDialog.invoke("isRegExSearch", null);
		assertFalse(value.booleanValue());
		value= (Boolean)fFindReplaceDialog.invoke("isRegExSearchAvailableAndChecked", null);
		assertFalse(value.booleanValue());
		Button checkbox= (Button)fFindReplaceDialog.get("fIsRegExCheckBox");
		assertTrue(checkbox.isEnabled());
		checkbox= (Button)fFindReplaceDialog.get("fWholeWordCheckBox");
		assertFalse(checkbox.isEnabled()); // there's no word in the Find field
	}
	
	public void testDisableWholeWordIfRegEx() {
		Combo findField= (Combo)fFindReplaceDialog.get("fFindField");
		findField.setText("word");

		Button isRegExCheckBox= (Button)fFindReplaceDialog.get("fIsRegExCheckBox");
		Button wholeWordCheckbox= (Button)fFindReplaceDialog.get("fWholeWordCheckBox");

		assertTrue(isRegExCheckBox.isEnabled());
		assertTrue(wholeWordCheckbox.isEnabled());
		
		fFindReplaceDialog.set("fIsTargetSupportingRegEx", true);
		isRegExCheckBox.setSelection(true);
		wholeWordCheckbox.setSelection(true);
		fFindReplaceDialog.invoke("updateButtonState", null);

		assertTrue(isRegExCheckBox.isEnabled());
		assertFalse(wholeWordCheckbox.isEnabled());
		
		// XXX: enable once https://bugs.eclipse.org/bugs/show_bug.cgi?id=72462 has been fixed
//		assertFalse(wholeWordCheckbox.getSelection());
	}
	
	public void testDisableWholeWordIfNotWord() {
		Combo findField= (Combo)fFindReplaceDialog.get("fFindField");
		Button isRegExCheckBox= (Button)fFindReplaceDialog.get("fIsRegExCheckBox");
		Button wholeWordCheckbox= (Button)fFindReplaceDialog.get("fWholeWordCheckBox");

		fFindReplaceDialog.set("fIsTargetSupportingRegEx", false);
		isRegExCheckBox.setSelection(false);
		wholeWordCheckbox.setSelection(true);
		fFindReplaceDialog.invoke("updateButtonState", null);
		
		findField.setText("word");
		assertTrue(isRegExCheckBox.isEnabled());
		assertTrue(wholeWordCheckbox.isEnabled());
		assertTrue(wholeWordCheckbox.getSelection());

		findField.setText("no word");
		assertTrue(isRegExCheckBox.isEnabled());
		assertFalse(wholeWordCheckbox.isEnabled());
		
		// XXX: enable once https://bugs.eclipse.org/bugs/show_bug.cgi?id=72462 has been fixed
//		assertFalse(wholeWordCheckbox.getSelection());
	}
}
