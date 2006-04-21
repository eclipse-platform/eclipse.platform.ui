/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.IModelProviderDescriptor;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.internal.ui.synchronize.actions.RefreshActionContribution;
import org.eclipse.team.ui.mapping.ITeamContentProviderManager;
import org.eclipse.team.ui.synchronize.*;

/**
 * A synchronize page for displaying a {@link ModelSynchronizeParticipant}.
 * 
 * @since 3.2
 **/
public class ModelSynchronizePage extends AbstractSynchronizePage {

	private ModelSynchronizeParticipant participant;

	/**
	 * Create a page from the given configuration
	 * @param configuration a page configuration
	 */
	public ModelSynchronizePage(ISynchronizePageConfiguration configuration) {
		super(configuration);
		this.participant = (ModelSynchronizeParticipant)configuration.getParticipant();
		configuration.setComparisonType(isThreeWay() 
						? ISynchronizePageConfiguration.THREE_WAY 
						: ISynchronizePageConfiguration.TWO_WAY);
		configuration.addActionContribution(new RefreshActionContribution());
	}

	private boolean isThreeWay() {
		return getParticipant().getContext().getType() == ISynchronizationContext.THREE_WAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#updateMode(int)
	 */
	protected void updateMode(int mode) {
		// Nothing to do
	}

	/**
	 * Return the participant of this page.
	 * @return the participant of this page
	 */
	protected ModelSynchronizeParticipant getParticipant() {
		return participant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#createViewerAdvisor(org.eclipse.swt.widgets.Composite)
	 */
	protected AbstractViewerAdvisor createViewerAdvisor(Composite parent) {
		CommonViewerAdvisor commonViewerAdvisor = new CommonViewerAdvisor(parent, getConfiguration());
		commonViewerAdvisor.addEmptyTreeListener((DiffTreeChangesSection)getChangesSection());
		updateMode(getConfiguration().getMode());
		return commonViewerAdvisor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizePage#createChangesSection()
	 */
	protected ChangesSection createChangesSection(Composite parent) {
		return new DiffTreeChangesSection(parent, this, getConfiguration());
	}
	
	public void init(ISynchronizePageSite site) {
		super.init(site);
		IDialogSettings pageSettings = site.getPageSettings();
		if(pageSettings != null) {
			String savedId = pageSettings.get(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
			if (savedId != null && ! savedId.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
				getConfiguration().setProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER, savedId);
			}
		}
	}
	
	public boolean aboutToChangeProperty(ISynchronizePageConfiguration configuration, String key, Object newValue) {
		if (key.equals(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER)) {
			if (!(newValue instanceof String)) {
				return false;
			}
			String currentSetting = (String)configuration.getProperty(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER);
			if (currentSetting != null && currentSetting.equals(newValue))
				return false;
			
			Object input = null;
			if (!newValue.equals(ModelSynchronizeParticipant.ALL_MODEL_PROVIDERS_VISIBLE)) {
				ModelProvider provider = getModelProvider((String)newValue);
				if (provider != null) {
					input = provider;
					configuration.setProperty(
							ISynchronizePageConfiguration.P_PAGE_DESCRIPTION,
							NLS.bind(TeamUIMessages.ShowModelProviderAction_0, new String[] {Utils.getLabel(provider), Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, configuration.getParticipant().getName()) }));
				}	
			} else {
				input = (ISynchronizationContext)configuration.getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT);
				configuration.setProperty(
						ISynchronizePageConfiguration.P_PAGE_DESCRIPTION,
						Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, configuration.getParticipant().getName()));
				IDialogSettings pageSettings = configuration.getSite().getPageSettings();
				if(pageSettings != null) {
					pageSettings.put(ModelSynchronizeParticipant.P_VISIBLE_MODEL_PROVIDER, (String)newValue);
				}
			}
			if (input != null) {
				Viewer viewer = getViewer();
				if (viewer != null)
					viewer.setInput(input);
				return true;
			}
			return false;
		}
		return super.aboutToChangeProperty(configuration, key, newValue);
	}

	private ModelProvider getModelProvider(String id) {
		try {
			IModelProviderDescriptor desc = ModelProvider.getModelProviderDescriptor((String)id);
			if (desc != null) {
				return desc.getModelProvider();
			}
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		return null;
	}

}
