/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;

import org.eclipse.jface.action.*;

import org.eclipse.ui.*;
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
		fNext= new NavigationAction(bundle, true);
		fPrevious= new NavigationAction(bundle, false);
		fToolbarNext= new NavigationAction(bundle, true);
		fToolbarPrevious= new NavigationAction(bundle, false);
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
