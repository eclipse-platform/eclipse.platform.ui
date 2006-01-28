/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.mapping.ISynchronizationConstants;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class ModelSelectionDropDownAction extends Action implements IMenuCreator, IPropertyChangeListener {

	private final ISynchronizePageConfiguration configuration;
	private MenuManager menuManager;
	private Action showAllAction;

	public ModelSelectionDropDownAction(ISynchronizePageConfiguration configuration) {
		Utils.initAction(this, "action.pickModels."); //$NON-NLS-1$
		this.configuration = configuration;
		getSynchronizationContext().getScope().addPropertyChangeListener(this);
		showAllAction = new Action("Show All Models") { 
			public void run() {
				Viewer v = ModelSelectionDropDownAction.this.configuration.getPage().getViewer();
				v.setInput(getSynchronizationContext());
			}
		};
		//showAllAction.setImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW));
		showAllAction.setActionDefinitionId("org.eclipse.team.ui.showAllModels"); //$NON-NLS-1$
		setMenuCreator(this);		
		update();	
	}

	private ISynchronizationContext getSynchronizationContext() {
		return (ISynchronizationContext)configuration.getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_CONTEXT);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IResourceMappingScope.MAPPINGS)) {
			Display display = TeamUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if(menuManager != null) {
						menuManager.dispose();
						menuManager = null;
					}
					update();
				}
			});
		}
	}

	public void dispose() {
		if(menuManager != null) {
			menuManager.dispose();
			menuManager = null;
		}
		getSynchronizationContext().getScope().removePropertyChangeListener(this);	
	}

	public Menu getMenu(Control parent) {
		Menu fMenu = null;
		if (menuManager == null) {
			menuManager = new MenuManager();
			fMenu = menuManager.createContextMenu(parent);
			menuManager.add(showAllAction);
			ModelProvider[] modelProviders = getSynchronizationContext().getScope().getModelProviders();
			if (modelProviders.length > 0)
				menuManager.add(new Separator());
			addModelsToMenu(modelProviders);
			
			menuManager.update(true);
		} else {
			fMenu = menuManager.getMenu();
		}
		return fMenu;
	}

	private void addModelsToMenu(ModelProvider[] modelProviders) {
		Object current = configuration.getPage().getViewer().getInput();
		for (int i = 0; i < modelProviders.length; i++) {
			ModelProvider provider = modelProviders[i];
			Action action = new ShowModelProviderAction(configuration, provider);
			action.setChecked(provider == current);
			menuManager.add(action);
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public void update() {
//		ISynchronizeParticipant current = fView.getParticipant();
//		ISynchronizeParticipantReference[] refs = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
//		String text = null;
//		if(current != null && refs.length > 0) {
//			text = NLS.bind(TeamUIMessages.GlobalRefreshAction_5, new String[] { Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, current.getName()) }); 
//			setToolTipText(text);
//			setText(text);
//		} else {
//			text = TeamUIMessages.GlobalRefreshAction_4; 
//			setToolTipText(text);
//			setText(text);
//		}
	}
}
