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
import org.eclipse.team.internal.ui.wizards.GlobalSynchronizeWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantReference;
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
public class GlobalRefreshAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate {

	public final static String NO_DEFAULT_PARTICPANT = "none"; //$NON-NLS-1$
	private Menu fMenu;
	private Action synchronizeAction;
	private IWorkbenchWindow window;

	class RefreshParticipantAction extends Action {
		private ISynchronizeParticipantReference participant;

		public void run() {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT, participant.getId());
			GlobalRefreshAction.this.run(participant);
		}

		public RefreshParticipantAction(int prefix, ISynchronizeParticipantReference participant) {
			super("&" + prefix + " " + participant.getDisplayName()); //$NON-NLS-1$ //$NON-NLS-2$
			this.participant = participant;
			setImageDescriptor(participant.getDescriptor().getImageDescriptor());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		ISynchronizeParticipantReference[] pages = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		setEnabled(pages.length >= 1);
	}

	public GlobalRefreshAction() {
		synchronizeAction = new Action(Policy.bind("GlobalRefreshAction.4")) { //$NON-NLS-1$
			public void run() {
				IWizard wizard = new GlobalSynchronizeWizard();
				WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
				dialog.open();
			}
		};
		setMenuCreator(this);
		updateTooltipMessage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
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
			if (description.getDescriptor().isGlobalSynchronize()) {
				Action action = new RefreshParticipantAction(i + 1, description);
				addActionToMenu(fMenu, action);
			}
		}
		addMenuSeparator();
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
		IWizard wizard = new GlobalSynchronizeWizard();
		if (id.equals(NO_DEFAULT_PARTICPANT)) {
			synchronizeAction.run();
		} else {
			ISynchronizeParticipantReference participant = TeamUI.getSynchronizeManager().get(id, null);
			if (participant != null) {
				run(participant);
			}
		}
	}
		
	private void run(ISynchronizeParticipantReference participant) {
		try {
			WizardDialog dialog = new WizardDialog(window.getShell(), participant.getParticipant().createSynchronizeWizard());
			dialog.open();
			updateTooltipMessage();
		} catch (TeamException e) {
			Utils.handle(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *           org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	protected void updateTooltipMessage() {
		setToolTipText(Policy.bind("GlobalRefreshAction.4")); //$NON-NLS-1$
	}
}