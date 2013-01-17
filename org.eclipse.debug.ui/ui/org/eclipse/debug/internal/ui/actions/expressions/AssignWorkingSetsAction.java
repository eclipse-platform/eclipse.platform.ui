/*******************************************************************************
 * Copyright (c) 2012 Tensilica and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abeer Bagul (Tensilica) - initial API and implementation (bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.expression.workingset.IExpressionWorkingSetConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;

/**
 * Shows a submenu which lists all expression working sets. 
 * User can select any of the working sets in the submenu to assign 
 * selected expressions to this working set.
 * <p>
 * The submenu also contains a menuitem "Create working set..." 
 * which opens the Add New Expression Working Set dialog.
 */
public class AssignWorkingSetsAction extends WatchExpressionAction {
	
	/* 
	 * Represents a checkable expression workingset menuitem
	 * 
	 * When user checks this menuitem, expressions selected in the view
	 * will be added to this working set.
	 */
	private class SelectWorkingSetAction extends Action {
		
		private IWorkingSet workingSet;
		
		SelectWorkingSetAction(IWorkingSet workingSet) {
			super(workingSet.getName(), AS_CHECK_BOX);
			this.workingSet = workingSet;
		}
		
		public void run() {
			IWatchExpression[] selectedExpressions = getSelectedExpressions();
			if (isChecked()) {
			//add all selected expressions to the workingset
			//ignore if any selected expression is already in the working set
				List newExpressions = new ArrayList();
				newExpressions.addAll(Arrays.asList(workingSet.getElements()));

				IAdaptable[] existingExpressions = workingSet.getElements();
				
				for (int i=0; i<selectedExpressions.length; i++) {
					IWatchExpression expressionToAdd = selectedExpressions[i];
					int j = 0;
					for (; j<existingExpressions.length; j++) {
						IAdaptable existingAdaptable = existingExpressions[j];
						IWatchExpression existingExpression = (IWatchExpression) existingAdaptable.getAdapter(IWatchExpression.class);
						if (existingExpression != null 
							&& existingExpression.equals(expressionToAdd))
							break;
					}
					if (j == existingExpressions.length)
						newExpressions.add(expressionToAdd);
				}
				
				IAdaptable[] newElements = new IAdaptable[newExpressions.size()];
				for (int i=0, size=newExpressions.size(); i<size; i++)
					newElements[i] = (IAdaptable) newExpressions.get(i);
            	workingSet.setElements(newElements);
			}
			else {
			//remove selected expressions from this workingset
				List newExpressions = new ArrayList();
				newExpressions.addAll(Arrays.asList(workingSet.getElements()));
				
				for (int i=0; i<selectedExpressions.length; i++) {
					IWatchExpression expressionToRemove = selectedExpressions[i];

					for (int j=0, size=newExpressions.size(); j<size; j++) {
						IAdaptable existingAdaptable = (IAdaptable) newExpressions.get(j);
						IWatchExpression existingExpression = (IWatchExpression) existingAdaptable.getAdapter(IWatchExpression.class);
						if (existingExpression != null 
							&& existingExpression.equals(expressionToRemove)) {
							newExpressions.remove(j--);
							break;
						}
					}
				}
				
				IAdaptable[] newElements = new IAdaptable[newExpressions.size()];
				for (int i=0, size=newExpressions.size(); i<size; i++)
					newElements[i] = (IAdaptable) newExpressions.get(i);
            	workingSet.setElements(newElements);
			}
		}
	}
	
	/*
	 * Click this menuitem to open the "New Expression Working Set" page.
	 * Expressions selected in the view are pre-checked in the page, 
	 * user just has to type a working set name and click OK.
	 */
	private class CreateWorkingSetAction extends Action {
		
		CreateWorkingSetAction() {
			super(ActionMessages.AssignWorkingSetsAction_0, SWT.PUSH);
		}
		
		public void run() {
			IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
			IWorkingSetNewWizard wizard = manager.createWorkingSetNewWizard(
					new String[] {IExpressionWorkingSetConstants.EXPRESSION_WORKINGSET_ID});
			if (wizard != null)
			{
				WizardDialog dialog = new WizardDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
						wizard);

				dialog.create();
				
				if (dialog.open() == Window.OK) {
					IWorkingSet workingSet = wizard.getSelection();
					manager.addWorkingSet(workingSet);
				}
			}
		}
	}

	public void run(IAction action) {
		//noop, we have a submenu
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		
		//assign a submenu to this action
		action.setMenuCreator(new IMenuCreator() {
			
			private Menu workingSetsSubMenu;
			
			/*
			 * Create a submenu.
			 * For each expression workingset defined in the workspace,
			 * add a checkbox menuitem to the submenu.
			 * If the selected expressions are already assigned 
			 * to any workingset, pre-check the menuitem 
			 * representing that workingset.
			 * When user checks a workingset, assign 
			 * expressions selected in the view to this workingset.
			 * When user unchecks a workingset, remove
			 * expressions selected in the view from this workingset.
			 * Pre-check only those workingsets 
			 * which contain all selected expressions. 
			 */
			public Menu getMenu(Menu parent) {
				if (workingSetsSubMenu == null) {
					workingSetsSubMenu = new Menu(parent);
					createMenuItems();
					workingSetsSubMenu.addMenuListener(new MenuListener() {
						
						public void menuShown(MenuEvent e) {
							//noop
							
						}
						
						public void menuHidden(MenuEvent e) {
							//noop
							
						}
					});
				}
				
				return workingSetsSubMenu;
			}
			
			public Menu getMenu(Control parent) {
				//noop - we are creating a submenu for a menuitem
				return null;
			}
			
			public void dispose() {
				//dispose the submenu
				if (workingSetsSubMenu != null) {
					if (!workingSetsSubMenu.isDisposed())
						workingSetsSubMenu.dispose();
					workingSetsSubMenu = null;
				}
			}
			
			private void createMenuItems() {
				//get all expression working sets
				IWorkingSet[] allWorkingSets = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
				List expressionWorkingSets = new ArrayList();
				for (int i=0; i<allWorkingSets.length; i++)
				{
					IWorkingSet workingSet = allWorkingSets[i];
					if (IExpressionWorkingSetConstants.EXPRESSION_WORKINGSET_ID.equals(workingSet.getId()))
						expressionWorkingSets.add(workingSet);
				}
				
				int i = 0;
				//for each expression working set, create a menuitem
				for (int size=expressionWorkingSets.size(); i<size; i++)
				{
					IWorkingSet expressionWorkingSet = (IWorkingSet) expressionWorkingSets.get(i);
					SelectWorkingSetAction selectAction = new SelectWorkingSetAction(expressionWorkingSet);
					ActionContributionItem selectItem = new ActionContributionItem(selectAction);
					selectItem.fill(workingSetsSubMenu, i);
										
					//if all selected expressions are included in this working set,
					//check it					
					IWatchExpression[] selectedExpressions = getSelectedExpressions();
					int j = 0;
					for (; j<selectedExpressions.length; j++)
					{
						IAdaptable[] existingExpressions = expressionWorkingSet.getElements();
						
						int k = 0;
						for (; k<existingExpressions.length; k++)
						{
							IAdaptable existingAdaptable = existingExpressions[k];
							IWatchExpression existingExpression = (IWatchExpression) existingAdaptable.getAdapter(IWatchExpression.class);

							if (existingExpression != null 
								&& (existingExpression.equals(selectedExpressions[j])))
								break;
						}
						
						if (k == existingExpressions.length)
							break;
					}
					
					selectAction.setChecked(j != 0 && j == selectedExpressions.length);
				}
				
				Separator separator = new Separator();
				separator.fill(workingSetsSubMenu, i);
				i++;
				
				//create a menuitem to open the "Add new expression working set" page
				CreateWorkingSetAction createAction = new CreateWorkingSetAction();
				ActionContributionItem createItem = new ActionContributionItem(createAction);
				createItem.fill(workingSetsSubMenu, i);
			}
		});
	}
}
