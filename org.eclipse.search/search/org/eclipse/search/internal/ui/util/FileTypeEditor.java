/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorRegistry;

import org.eclipse.search.internal.ui.SearchMessages;

public class FileTypeEditor extends SelectionAdapter implements DisposeListener, SelectionListener {
	
	private Combo fTextField;
	private Button fBrowseButton;

	final static String TYPE_DELIMITER= SearchMessages.getString("FileTypeEditor.typeDelimiter"); //$NON-NLS-1$

	public FileTypeEditor(IEditorRegistry registry, Combo textField, Button browseButton) {
		fTextField= textField;
		fBrowseButton= browseButton;
		
		fTextField.addDisposeListener(this);
		fBrowseButton.addDisposeListener(this);
		fBrowseButton.addSelectionListener(this);
	}
	
	public void widgetDisposed(DisposeEvent event) {
		Widget widget= event.widget;
		if (widget == fTextField) 
			fTextField= null;
		else if (widget	== fBrowseButton)
			fBrowseButton= null;
	}
	
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == fBrowseButton)
			handleBrowseButton();
	}
		
	public void widgetDoubleSelected(SelectionEvent event) {
	}
	/**
	 *	Answer a collection of the currently-specified resource types
	 *
	 *	@return java.util.Vector
	 */
	public Set getFileTypes() {
		Set result= new HashSet();
			StringTokenizer tokenizer= new StringTokenizer(fTextField.getText(), TYPE_DELIMITER);

			while (tokenizer.hasMoreTokens()) {
				String currentExtension= tokenizer.nextToken().trim();
					result.add(currentExtension);
			}
		return result;
	}
	/**
	 *	Populate self's import types field based upon the passed types collection
	 *
	 *	@param types java.util.Vector
	 */
	public void setFileTypes(Set types) {
		fTextField.setText(typesToString(types));
	}
	protected void handleBrowseButton() {
		TypeFilteringDialog dialog= new TypeFilteringDialog(fTextField.getShell(), getFileTypes());
		if (dialog.open() == Window.OK) {
			setFileTypes(new HashSet(Arrays.asList(dialog.getResult())));
		}
	}

	public static String typesToString(Set types) {
		StringBuffer result= new StringBuffer();
		Iterator typesIter= types.iterator();
		boolean first= true;
		while (typesIter.hasNext()) {
			if (!first) {
				result.append(TYPE_DELIMITER);
				result.append(" "); //$NON-NLS-1$
			} else
				first= false;
			result.append(typesIter.next());
		}
		return result.toString();
	}
}
