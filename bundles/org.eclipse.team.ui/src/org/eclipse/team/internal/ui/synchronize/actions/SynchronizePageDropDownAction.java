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

import org.eclipse.jface.action.*;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizeView;
import org.eclipse.team.internal.ui.wizards.GlobalSynchronizeWizard;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.texteditor.IUpdate;

public class SynchronizePageDropDownAction extends Action implements IMenuCreator, ISynchronizeParticipantListener, IUpdate {

		private ISynchronizeView fView;
		private Menu fMenu;
		private Action synchronizeAction;
	
		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			updateTooltipText();
		}
		
		protected ISynchronizeParticipantReference[] getParticipants() {
			return TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		}
		
		protected boolean select(ISynchronizeParticipantReference ref) {
			return true;
		}

		public SynchronizePageDropDownAction(ISynchronizeView view) {
			fView= view;
			Utils.initAction(this, "action.refreshSubscriber."); //$NON-NLS-1$
			
			synchronizeAction = new Action(Policy.bind("GlobalRefreshAction.4")) { //$NON-NLS-1$
				public void run() {
					IWizard wizard = new GlobalSynchronizeWizard();
					WizardDialog dialog = new WizardDialog(fView.getViewSite().getShell(), wizard);
					dialog.open();
				}
			};
			synchronizeAction.setImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW));
			setMenuCreator(this);		
			update();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#dispose()
		 */
		public void dispose() {
			if (fMenu != null) {
				fMenu.dispose();
				fMenu = null;
			}
			TeamUI.getSynchronizeManager().removeSynchronizeParticipantListener(this);
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
			final ISynchronizeParticipantReference[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
			addParticipantsToMenu(fMenu, participants);
			if(participants.length > 0) 	addMenuSeparator();
			addActionToMenu(fMenu, synchronizeAction);
			TeamUI.getSynchronizeManager().addSynchronizeParticipantListener(this);	
			return fMenu;
		}
	
		protected void addParticipantsToMenu(Menu parent, ISynchronizeParticipantReference[] refs) {
			ISynchronizeParticipant current = fView.getParticipant();
			for (int i = 0; i < refs.length; i++) {
				ISynchronizeParticipantReference page = refs[i];
				Action action = new ShowSynchronizeParticipantAction(fView, page);  
				try {
					boolean isCurrent = page.getParticipant().equals(current);
					action.setChecked(isCurrent);
				} catch (TeamException e) {
					continue;
				}
				addActionToMenu(fMenu, action);
			}
		}
		
		protected void addActionToMenu(Menu parent, Action action) {
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(parent, -1);
		}

		protected void addMenuSeparator() {
			new MenuItem(fMenu, SWT.SEPARATOR);		
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			ISynchronizeParticipant current = fView.getParticipant();
			if(current != null) {
				current.run(fView);
			} else {
				synchronizeAction.run();
			}
			update();
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.sync.ISynchronizeParticipantListener#participantsAdded(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
		 */
		public void participantsAdded(ISynchronizeParticipant[] consoles) {
			Display display = TeamUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					update();
				}
			});
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.sync.ISynchronizeParticipantListener#participantsRemoved(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
		 */
		public void participantsRemoved(ISynchronizeParticipant[] consoles) {
			Display display = TeamUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if (fMenu != null) {
						fMenu.dispose();
					}
					update();
				}
			});
		}
		
		private void updateTooltipText() {
			ISynchronizeParticipant current = fView.getParticipant();
			ISynchronizeParticipantReference[] refs = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
			String text = null;
			if(current != null && refs.length > 0) {
				text = Policy.bind("GlobalRefreshAction.5", Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, current.getName())); //$NON-NLS-1$
				setToolTipText(text);
				setText(text);
			} else {
				text = Policy.bind("GlobalRefreshAction.4"); //$NON-NLS-1$
				setToolTipText(text);
				setText(text);
			}
		}
}