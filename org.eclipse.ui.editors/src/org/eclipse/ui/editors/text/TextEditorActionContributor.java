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
package org.eclipse.ui.editors.text;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;


/**
 * Manages the installation and de-installation of global actions for the default text editor.
 * <p>
 * If instantiated and used as-is, this contributor connects global actions and adds actions
 * for line delimiter conversion and encoding support.</p>
 * 
 * @since 2.0
 */
public class TextEditorActionContributor extends BasicTextEditorActionContributor {

	/** Convert to Windows action. */
	private RetargetTextEditorAction fConvertToWindows;
	/** Convert to UNIX action. */
	private RetargetTextEditorAction fConvertToUNIX;
	/** Convert to MAC action. */
	private RetargetTextEditorAction fConvertToMac;
	/** Change encoding action. */
	private RetargetTextEditorAction fChangeEncodingAction;
	
	/**
	 * Creates a new contributor.
	 */
	public TextEditorActionContributor() {
		super();
		
		// line delimiter conversion
		fConvertToWindows= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToWindows."); //$NON-NLS-1$ 
		fConvertToUNIX= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToUNIX."); //$NON-NLS-1$ 
		fConvertToMac= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToMac."); //$NON-NLS-1$
		
		fChangeEncodingAction= new RetargetTextEditorAction(TextEditorMessages.getResourceBundle(), "Editor.ChangeEncodingAction."); //$NON-NLS-1$
	}	
	
	/**
	 * Internally sets the active editor to the actions provided by this contributor.
	 * Cannot be overridden by subclasses.
	 * 
	 * @param part the editor
	 */
	private void doSetActiveEditor(IEditorPart part) {
				
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;
		
		IActionBars actionBars= getActionBars();
		if (actionBars != null) {
			actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), getAction(textEditor, IDEActionFactory.ADD_TASK.getId()));
			actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), getAction(textEditor, IDEActionFactory.BOOKMARK.getId()));
		}
			
		// line delimiter conversion
		fConvertToWindows.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS));
		fConvertToUNIX.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX));
		fConvertToMac.setAction(getAction(textEditor, ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC));
		
		fChangeEncodingAction.setAction(getAction(textEditor, ITextEditorActionConstants.CHANGE_ENCODING));
	}
	
	/*
	 * @see IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}
	
	/*
	 * @see IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}

	/*
	 * @see EditorActionBarContributor#init(org.eclipse.ui.IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);
		
		IMenuManager menuManager= bars.getMenuManager();
		IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			MenuManager subMenu= new MenuManager(TextEditorMessages.getString("Editor.ConvertLineDelimiters.label")); //$NON-NLS-1$
			subMenu.add(fConvertToWindows);
			subMenu.add(fConvertToUNIX);
			subMenu.add(fConvertToMac);
	
			editMenu.add(subMenu);
			editMenu.add(fChangeEncodingAction);
		}
	}
}
