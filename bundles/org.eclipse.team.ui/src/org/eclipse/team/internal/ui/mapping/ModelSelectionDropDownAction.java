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

import org.eclipse.core.resources.mapping.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.ISynchronizationConstants;
import org.eclipse.team.ui.mapping.ModelOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;

public class ModelSelectionDropDownAction extends Action implements IMenuCreator, ISynchronizationScopeChangeListener {

	private final ISynchronizePageConfiguration configuration;
	private MenuManager menuManager;
	private Action showAllAction;
	private org.eclipse.jface.util.IPropertyChangeListener listener;

	public ModelSelectionDropDownAction(ISynchronizePageConfiguration configuration) {
		Utils.initAction(this, "action.pickModels."); //$NON-NLS-1$
		this.configuration = configuration;
		listener = new org.eclipse.jface.util.IPropertyChangeListener() {
					public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
						if (event.getProperty() == ISynchronizationConstants.P_ACTIVE_MODEL_PROVIDER) {
							update();
						}
					}
				};
		this.configuration.addPropertyChangeListener(listener);
		getSynchronizationContext().getScope().addScopeChangeListener(this);
		showAllAction = new Action("Show All") { 
			public void run() {
				Viewer v = ModelSelectionDropDownAction.this.configuration.getPage().getViewer();
				v.setInput(getSynchronizationContext());
				ModelSelectionDropDownAction.this.configuration.setProperty(
						ISynchronizationConstants.P_ACTIVE_MODEL_PROVIDER,
						ISynchronizationConstants.ALL_MODEL_PROVIDERS_ACTIVE);
				ModelSelectionDropDownAction.this.configuration.setProperty(
						ISynchronizePageConfiguration.P_PAGE_DESCRIPTION,
						ModelSelectionDropDownAction.this.configuration.getParticipant().getName());
			}
		};
		//showAllAction.setImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW));
		//showAllAction.setActionDefinitionId("org.eclipse.team.ui.showAllModels"); //$NON-NLS-1$
		setMenuCreator(this);		
		update();	
	}

	private ISynchronizationContext getSynchronizationContext() {
		return (ISynchronizationContext)configuration.getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_CONTEXT);
	}

	public void dispose() {
		if(menuManager != null) {
			menuManager.dispose();
			menuManager = null;
		}
		getSynchronizationContext().getScope().removeScopeChangeListener(this);
		configuration.removePropertyChangeListener(listener);
	}

	public Menu getMenu(Control parent) {
		Menu fMenu = null;
		if (menuManager == null) {
			menuManager = new MenuManager();
			fMenu = menuManager.createContextMenu(parent);
			menuManager.add(showAllAction);
			ModelProvider[] modelProviders = ((ModelSynchronizeParticipant)configuration.getParticipant()).getActiveModelProviders();
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
		String id = getActiveProviderId();
		for (int i = 0; i < modelProviders.length; i++) {
			ModelProvider provider = modelProviders[i];
			Action action = new ShowModelProviderAction(configuration, provider);
			action.setChecked(provider.getDescriptor().getId().equals(id));
			menuManager.add(action);
		}
	}

	private String getActiveProviderId() {
		String id = (String)configuration.getProperty(ISynchronizationConstants.P_ACTIVE_MODEL_PROVIDER);
		if (id == null)
			id = ISynchronizationConstants.ALL_MODEL_PROVIDERS_ACTIVE;
		return id;
	}

	public Menu getMenu(Menu parent) {
		return null;
	}
	
	private ModelProvider getNextProvider() {
		ModelProvider[] providers = getSynchronizationContext().getScope().getModelProviders();
		providers = ModelOperation.sortByExtension(providers);
		String id = getActiveProviderId();
		int index = 0;
		if (id != null) {
			for (int i = 0; i < providers.length; i++) {
				ModelProvider provider = providers[i];
				if (provider.getDescriptor().getId().equals(id)) {
					index = i + 1;
					break;
				}
			}
			if (index == providers.length)
				index = 0;
		}
		return providers[index];
	}

	public void update() {
		ModelProvider next = getNextProvider();
		String text = NLS.bind("Show {0}", next.getDescriptor().getLabel()); 
		setToolTipText(text);
		setText(text);
		if (menuManager != null) {
			IContributionItem[] items = menuManager.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				if (item instanceof ActionContributionItem) {
					ActionContributionItem aci = (ActionContributionItem) item;
					IAction a = aci.getAction();
					if (a instanceof ShowModelProviderAction) {
						ShowModelProviderAction action = (ShowModelProviderAction) a;
						action.setChecked(action.getProviderId().equals(getActiveProviderId()));
					}
				}
			}
		}
		// TODO: need to update the check mark
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ModelProvider next = getNextProvider();
		Action action = new ShowModelProviderAction(configuration, next);
		action.run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ISynchronizationScopeChangeListener#scopeChanged(org.eclipse.team.core.mapping.ISynchronizationScope, org.eclipse.core.resources.mapping.ResourceMapping[], org.eclipse.core.resources.mapping.ResourceTraversal[])
	 */
	public void scopeChanged(ISynchronizationScope scope, ResourceMapping[] newMappings, ResourceTraversal[] newTraversals) {
		if (newMappings.length > 0) {
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
}
