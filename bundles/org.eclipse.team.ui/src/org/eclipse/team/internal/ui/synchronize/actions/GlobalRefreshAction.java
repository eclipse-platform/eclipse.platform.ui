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
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

/**
 * A global refresh action that allows the user to select the participant to refresh 
 * or the default action is to refresh the last selected participant. Participants are 
 * only listed if they support
 * <p>
 * This action is normally associated with the Team action set and is enabled by default
 * in the Team Synchronizing perspective.
 * </p>
 * @since 3.0
 */
public class GlobalRefreshAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate, ISynchronizeParticipantListener {

	public final static String NO_DEFAULT_PARTICPANT = "none"; //$NON-NLS-1$
	private Menu fMenu;
	private Action synchronizeAction;
	private IWorkbenchWindow window;
	private IAction actionProxy;

	class RefreshParticipantAction extends Action {
		private ISynchronizeParticipantReference participant;

		public void run() {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT, participant.getId());
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT_SEC_ID, participant.getSecondaryId());
			GlobalRefreshAction.this.run(participant);
		}

		public RefreshParticipantAction(int prefix, ISynchronizeParticipantReference participant) {
			super("&" + prefix + " " + Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, participant.getDisplayName())); //$NON-NLS-1$ //$NON-NLS-2$
			this.participant = participant;
			setImageDescriptor(participant.getDescriptor().getImageDescriptor());
		}
	}

	public GlobalRefreshAction() {
		synchronizeAction = new Action(Policy.bind("GlobalRefreshAction.4")) { //$NON-NLS-1$
			public void run() {
				IWizard wizard = new GlobalSynchronizeWizard();
				WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
				dialog.open();
			}
		};
		synchronizeAction.setImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW));
		setMenuCreator(this);
		TeamUI.getSynchronizeManager().addSynchronizeParticipantListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
		TeamUI.getSynchronizeManager().removeSynchronizeParticipantListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = new Menu(parent);
		ISynchronizeParticipantReference[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipantReference description = participants[i];
			Action action = new RefreshParticipantAction(i + 1, description);
			addActionToMenu(fMenu, action);
		}
		if(participants.length > 0) addMenuSeparator();
		addActionToMenu(fMenu, synchronizeAction);
		return fMenu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	protected void addMenuSeparator() {
		new MenuItem(fMenu, SWT.SEPARATOR);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		String id = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT);
		String secondaryId = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT_SEC_ID);
		IWizard wizard = new GlobalSynchronizeWizard();
		if (! id.equals(NO_DEFAULT_PARTICPANT)) {
			ISynchronizeParticipantReference participant = TeamUI.getSynchronizeManager().get(id, secondaryId);
			if (participant != null) {
				run(participant);
			}
		}
		synchronizeAction.run();
		actionProxy = action;
		updateTooltipText();
	}
		
	private void run(ISynchronizeParticipantReference participant) {
		ISynchronizeParticipant p;
		try {
			p = participant.getParticipant();
			p.run(null /* no workbench part */);
			updateTooltipText();
		} catch (TeamException e) {
			Utils.handle(e);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.ISynchronizeParticipantListener#participantsAdded(org.eclipse.team.ui.sync.ISynchronizeParticipant[])
	 */
	public void participantsAdded(ISynchronizeParticipant[] consoles) {
		Display display = TeamUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				updateTooltipText();
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
				updateTooltipText();
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *           org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		actionProxy = action;
	}
	
	protected void updateTooltipText() {
		if (actionProxy != null) {
			String id = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT);
			String secondaryId = TeamUIPlugin.getPlugin().getPreferenceStore().getString(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT_SEC_ID);
			if (!id.equals(NO_DEFAULT_PARTICPANT)) {
				ISynchronizeParticipantReference ref = TeamUI.getSynchronizeManager().get(id, secondaryId);
				if (ref != null) {
					actionProxy.setToolTipText(Policy.bind("GlobalRefreshAction.5", ref.getDisplayName())); //$NON-NLS-1$
					return;
				}
			}
			actionProxy.setToolTipText(Policy.bind("GlobalRefreshAction.4")); //$NON-NLS-1$
		}
	}
}