/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables.details;


import java.util.Iterator;
import java.util.Set;

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;

/**
 * Drop down action that displays the available detail panes for a selection.
 */
public class AvailableDetailPanesAction extends Action implements IMenuCreator {
	
	private VariablesView fView;
	private Menu fMenu;
	private Set fAvailableIDs;
	
	/**
	 * Each entry in the menu will be of this type.  It represents one possible detail pane
	 * that the user can select.  If the user selects it, the display is changed to use that
	 * detail pane and the preferred detail pane map in the pane manager is updated.
	 * 
	 * @see DetailPaneManager
	 * @since 3.3
	 */
	private class SetDetailsViewerAction extends Action {
		
		private String fPaneID;
		private Set fPossiblePaneIDs;
		private VariablesView fVarView;
		
		public SetDetailsViewerAction(String name, String paneID, Set possiblePaneIDs, VariablesView view){
			super(name,AS_RADIO_BUTTON);
			fPaneID = paneID;
			fPossiblePaneIDs = possiblePaneIDs;
			fVarView = view;
		}
		
		public void run() {
			// Don't change viewers unless the user is selecting a different viewer than the one currently displayed
			if (isChecked() && !fVarView.getDetailPane().getCurrentViewerID().equals(fPaneID)){
				DetailPaneManager.getDefault().setPreferredDetailPane(fPossiblePaneIDs, fPaneID);
				fVarView.populateDetailPane();
			}
		}
			
	}
	
	public AvailableDetailPanesAction(VariablesView view) {
		setView(view);
		
		setText(DetailMessages.AvailableDetailPanesAction_0);  
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.VARIABLES_SELECT_DETAIL_PANE);
		
		setEnabled(false);
		setMenuCreator(this);
		init();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
	}
	
	protected VariablesView getView() {
		return fView;
	}
	
	protected void setView(VariablesView view) {
		fView = view;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
		setView(null);
		fAvailableIDs.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}
	
	protected void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		
		fMenu= new Menu(parent);
		
		Iterator iter = fAvailableIDs.iterator();
		int i = 0;
		String currentViewer = getView().getDetailPane().getCurrentViewerID();
		while (iter.hasNext()) {
			String currentID = (String) iter.next();
			
			StringBuffer name = new StringBuffer();
			//add the numerical accelerator
            i++;
			if (i < 9) {
                name.append('&');
                name.append(i);
                name.append(' ');
            }
			
			String typeName = DetailPaneManager.getDefault().getNameFromID(currentID);
			if (typeName != null && typeName.length() > 0){
				name.append(typeName);
			} else {
				name.append(currentID);				
			}
					
			IAction action = new SetDetailsViewerAction(name.toString(),currentID,fAvailableIDs,getView());
			
			if (currentID.equals(currentViewer)){
				action.setChecked(true);
			}
			
			addActionToMenu(fMenu, action);	
		}
		
		return fMenu;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void init() {
		
		ISelection viewerSelection = getView().getViewer().getSelection();
		if (viewerSelection instanceof IStructuredSelection){
			IStructuredSelection selection = (IStructuredSelection)viewerSelection;
			fAvailableIDs = DetailPaneManager.getDefault().getAvailablePaneIDs(selection);
			if (fAvailableIDs.size() > 1){
				setEnabled(true);
			}
		}
	}
}
