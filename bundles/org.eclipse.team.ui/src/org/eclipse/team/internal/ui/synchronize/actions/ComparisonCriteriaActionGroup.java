/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ComparisonCriteria;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

/**
 * This action group allows the user to choose one or more comparison critera
 * to be applied to a comparison
 */
public class ComparisonCriteriaActionGroup extends Action implements IMenuCreator {
	
	private ComparisonCriteria[] criteria;
	private ComparisonCriteriaAction[] actions;
	private SubscriberInput input;
	private Menu fMenu;
	
	/**
	 * Action for filtering by change type.
	 */
	class ComparisonCriteriaAction extends Action {
		private ComparisonCriteria criteria;
		public ComparisonCriteriaAction(ComparisonCriteria criteria) {
			super(criteria.getName(), Action.AS_RADIO_BUTTON);
			this.criteria = criteria;
		}
		public void run() {
			ComparisonCriteriaActionGroup.this.activate(this);
		}
		public ComparisonCriteria getComparisonCriteria() {
			return criteria;
		}
	}
	
	public ComparisonCriteriaActionGroup(SubscriberInput input) {
		this.input = input;
		setMenuCreator(this);
		Utils.initAction(this, "action.comparisonCriteria."); //$NON-NLS-1$
		initializeActions();
	}
	
	public void activate(final ComparisonCriteriaAction activatedAction) {
		for (int i = 0; i < actions.length; i++) {
			ComparisonCriteriaAction action = actions[i];
			action.setChecked(activatedAction == action);
		}
		TeamUIPlugin.run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					// when the comparison criteria changes, recalculate the entire sync set based on
					// the new input.
					input.getSubscriber().setCurrentComparisonCriteria(activatedAction.getComparisonCriteria().getId());
					input.reset();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		});
	}
	
	public void initializeActions() {
		this.criteria = input.getSubscriber().getComparisonCriterias();
		this.actions = new ComparisonCriteriaAction[criteria.length];
		for (int i = 0; i < criteria.length; i++) {
			ComparisonCriteria c = criteria[i];
			actions[i] = new ComparisonCriteriaAction(c);
			actions[i].setChecked(c == input.getSubscriber().getCurrentComparisonCriteria());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		ISynchronizeParticipant[] pages = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		setEnabled(pages.length >= 1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}		
		fMenu= new Menu(parent);
		ComparisonCriteria currentComparisonCriteria = input.getSubscriber().getCurrentComparisonCriteria();
		for (int i = 0; i < actions.length; i++) {
			ComparisonCriteriaAction action = actions[i];			
			action.setChecked(action.getComparisonCriteria() == currentComparisonCriteria);
			addActionToMenu(fMenu, action);
		}
		return fMenu;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		// do nothing - this is a menu
	}
	
	public void addActionsToMenuMgr(IMenuManager menu) {
		ComparisonCriteria currentComparisonCriteria = input.getSubscriber().getCurrentComparisonCriteria();
		for (int i = 0; i < actions.length; i++) {
			ComparisonCriteriaAction action = actions[i];			
			action.setChecked(action.getComparisonCriteria() == currentComparisonCriteria);
			menu.add(action);
		}
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	protected void addMenuSeparator() {
		new MenuItem(fMenu, SWT.SEPARATOR);		
	}
}
