/*
 * Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;

import org.eclipse.jface.action.*;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.EditorActionBarContributor;

import org.eclipse.compare.*;


public class CompareEditorContributor extends EditorActionBarContributor {
	
	private IEditorPart fActiveEditorPart= null;

	private IgnoreWhiteSpaceAction fIgnoreWhitespace;
	private NavigationAction fNext;
	private NavigationAction fPrevious;
	
	private NavigationAction fToolbarNext;
	private NavigationAction fToolbarPrevious;


	public CompareEditorContributor() {
		ResourceBundle bundle= CompareUIPlugin.getResourceBundle();
		
		fIgnoreWhitespace= new IgnoreWhiteSpaceAction(bundle, null);
		WorkbenchHelp.setHelp(fIgnoreWhitespace, ICompareContextIds.IGNORE_WHITESPACE_ACTION);
		
		fNext= new NavigationAction(bundle, true);
		WorkbenchHelp.setHelp(fNext, ICompareContextIds.GLOBAL_NEXT_DIFF_ACTION);
		
		fPrevious= new NavigationAction(bundle, false);
		WorkbenchHelp.setHelp(fPrevious, ICompareContextIds.GLOBAL_PREVIOUS_DIFF_ACTION);
		
		fToolbarNext= new NavigationAction(bundle, true);
		WorkbenchHelp.setHelp(fToolbarNext,ICompareContextIds.NEXT_DIFF_ACTION);
		
		fToolbarPrevious= new NavigationAction(bundle, false);
		WorkbenchHelp.setHelp(fToolbarPrevious, ICompareContextIds.PREVIOUS_DIFF_ACTION);
	}

	/*
	 * @see EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager tbm) {
		tbm.add(new Separator());
		tbm.add(fIgnoreWhitespace);
		tbm.add(fToolbarNext);
		tbm.add(fToolbarPrevious);
	}
	
	/*
	 * @see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menuManager) {
	}

	public void setActiveEditor(IEditorPart targetEditor) {
				
		if (fActiveEditorPart == targetEditor)
			return;
			
		fActiveEditorPart= targetEditor;
		
		if (fActiveEditorPart != null) {
			IEditorInput input= fActiveEditorPart.getEditorInput();
			if (input instanceof CompareEditorInput) {
				CompareEditorInput compareInput= (CompareEditorInput) input;
				fNext.setCompareEditorInput(compareInput);
				fPrevious.setCompareEditorInput(compareInput);
				// Begin fix http://bugs.eclipse.org/bugs/show_bug.cgi?id=20105
				fToolbarNext.setCompareEditorInput(compareInput);
				fToolbarPrevious.setCompareEditorInput(compareInput);
				// End fix http://bugs.eclipse.org/bugs/show_bug.cgi?id=20105
			}
		}
			
		if (targetEditor instanceof CompareEditor) {
			IActionBars actionBars= getActionBars();
		
			CompareEditor editor= (CompareEditor) targetEditor;
			editor.setActionBars(actionBars);
		
			actionBars.setGlobalActionHandler(IWorkbenchActionConstants.NEXT, fNext);
			actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PREVIOUS, fPrevious);

			CompareConfiguration cc= editor.getCompareConfiguration();
			fIgnoreWhitespace.setCompareConfiguration(cc);
		}		
	}
}
