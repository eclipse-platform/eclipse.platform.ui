/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import org.eclipse.compare.*;


public class CompareEditorContributor extends EditorActionBarContributor {

	private IgnoreWhiteSpaceAction fIgnoreWhitespace;
	private ShowPseudoConflicts fShowPseudoConflicts;


	public CompareEditorContributor() {
		ResourceBundle bundle= CompareUIPlugin.getResourceBundle();
		fIgnoreWhitespace= new IgnoreWhiteSpaceAction(bundle, null);
		fShowPseudoConflicts= new ShowPseudoConflicts(bundle, null);
	}

	public void contributeToToolBar(IToolBarManager tbm) {
		tbm.add(new Separator());
		tbm.add(fIgnoreWhitespace);
		tbm.add(fShowPseudoConflicts);
	}

	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof CompareEditor) {
			CompareConfiguration cc= ((CompareEditor) targetEditor).getCompareConfiguration();
			fIgnoreWhitespace.setCompareConfiguration(cc);
			fShowPseudoConflicts.setCompareConfiguration(cc);
		}
	}
}
