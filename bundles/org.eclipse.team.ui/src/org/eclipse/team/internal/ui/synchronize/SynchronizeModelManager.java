/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IActionBars;

/**
 * Manages the models that can be displayed by a synchronize page
 */
public abstract class SynchronizeModelManager extends SynchronizePageActionGroup {
	
	private static final String P_LAST_PROVIDER = TeamUIPlugin.ID + ".P_LAST_MODELPROVIDER"; //$NON-NLS-1$
	
	private ISynchronizeModelProvider modelProvider;
	private List toggleModelProviderActions;
	private ISynchronizePageConfiguration configuration;
	private StructuredViewerAdvisor advisor;
	
	/**
	 * Action that allows changing the model providers supported by this advisor.
	 */
	private class ToggleModelProviderAction extends Action implements IPropertyChangeListener {
		private ISynchronizeModelProviderDescriptor descriptor;
		protected ToggleModelProviderAction(ISynchronizeModelProviderDescriptor descriptor) {
			super(descriptor.getName(), Action.AS_RADIO_BUTTON);
			setImageDescriptor(descriptor.getImageDescriptor());
			setToolTipText(descriptor.getName());
			this.descriptor = descriptor;
			update();
			configuration.addPropertyChangeListener(this);
		}

		public void run() {
			ISynchronizeModelProvider mp = getActiveModelProvider();
			if (!mp.getDescriptor().getId().equals(descriptor.getId())) {
				mp.saveState();
				internalPrepareInput(descriptor.getId(), null);
				setInput();
			}
		}
		
		public void update() {
			ISynchronizeModelProvider mp = getActiveModelProvider();
			if(mp != null) {
				setChecked(mp.getDescriptor().getId().equals(descriptor.getId()));
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(SynchronizePageConfiguration.P_MODEL)) {
				update();
			}
		}
	}
	
	public SynchronizeModelManager(ISynchronizePageConfiguration configuration) {
		Assert.isNotNull(configuration, "configuration cannot be null"); //$NON-NLS-1$
		this.configuration = configuration;
		configuration.addActionContribution(this);
	}
	
	/**
	 * Initialize the model manager to be used with the provided advisor.
	 */
	public void setViewerAdvisor(StructuredViewerAdvisor advisor) {
		this.advisor = advisor;
	}
	
	/**
	 * Return the list of supported model providers for this advisor.
	 * @param viewer
	 * @return
	 */
	protected abstract ISynchronizeModelProviderDescriptor[] getSupportedModelProviders();
	
	/**
	 * Get the model provider that will be used to create the input
	 * for the adviser's viewer.
	 * @return the model provider
	 */
	protected abstract ISynchronizeModelProvider createModelProvider(String id);
	
	protected ISynchronizeModelProvider getActiveModelProvider() {
		return modelProvider;
	}
	
	protected Object internalPrepareInput(String id, IProgressMonitor monitor) {
		if(modelProvider != null) {
			modelProvider.dispose();
		}
		modelProvider = createModelProvider(id);		
		IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
		if(pageSettings != null) {
			pageSettings.put(P_LAST_PROVIDER, modelProvider.getDescriptor().getId());
		}
		return modelProvider.prepareInput(monitor);
	}
	
	/**
	 * Gets a new selection that contains the view model objects that
	 * correspond to the given objects. The advisor will try and
	 * convert the objects into the appropriate viewer objects. 
	 * This is required because the model provider controls the actual 
	 * model elements in the viewer and must be consulted in order to
	 * understand what objects can be selected in the viewer.
	 * <p>
	 * This method does not affect the selection of the viewer itself.
	 * It's main purpose is for testing and should not be used by other
	 * clients.
	 * </p>
	 * @param object the objects to select
	 * @return a selection corresponding to the given objects
	 */
	public ISelection getSelection(Object[] objects) {
		if (modelProvider != null) {
	 		Object[] viewerObjects = new Object[objects.length];
			for (int i = 0; i < objects.length; i++) {
				viewerObjects[i] = modelProvider.getMapping(objects[i]);
			}
			return new StructuredSelection(viewerObjects);
		} else {
			return StructuredSelection.EMPTY;
		}
	}
	
	/**
	 * Sets a new selection for this viewer and optionally makes it visible. The advisor will try and
	 * convert the objects into the appropriate viewer objects. This is required because the model
	 * provider controls the actual model elements in the viewer and must be consulted in order to
	 * understand what objects can be selected in the viewer.
	 * 
	 * @param object the objects to select
	 * @param reveal <code>true</code> if the selection is to be made visible, and
	 *                  <code>false</code> otherwise
	 */
	protected void setSelection(Object[] objects, boolean reveal) {
		ISelection selection = getSelection(objects);
		if (!selection.isEmpty()) {
			advisor.setSelection(selection, reveal);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		ISynchronizeModelProviderDescriptor[] providers = getSupportedModelProviders();
		// We only need switching of layouts if there is more than one model provider
		if (providers.length > 1) {
			toggleModelProviderActions = new ArrayList();
			for (int i = 0; i < providers.length; i++) {
				final ISynchronizeModelProviderDescriptor provider = providers[i];
				toggleModelProviderActions.add(new ToggleModelProviderAction(provider));
			}
		}
		// The input may of been set already. In that case, don't change it and
		// simply assign it to the view.
		if(modelProvider == null) {
			String defaultProviderId = null; /* use providers prefered */
			IDialogSettings pageSettings = configuration.getSite().getPageSettings();
			if(pageSettings != null) {
				defaultProviderId = pageSettings.get(P_LAST_PROVIDER); 
			}
			internalPrepareInput(defaultProviderId, null);
		}
		setInput();
	}
	
	/**
	 * Set the input of the viewer
	 */
	protected void setInput() {
		configuration.setProperty(SynchronizePageConfiguration.P_MODEL, modelProvider.getModelRoot());
		if(advisor != null)
			advisor.setInput(modelProvider);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		if (toggleModelProviderActions == null) return;
		IToolBarManager toolbar = actionBars.getToolBarManager();
		IMenuManager menu = actionBars.getMenuManager();
		IContributionItem group = findGroup(menu, ISynchronizePageConfiguration.LAYOUT_GROUP);
		if(menu != null && group != null) {
			MenuManager layout = new MenuManager(Policy.bind("action.layout.label")); //$NON-NLS-1$
			menu.appendToGroup(group.getId(), layout);	
			appendToMenu(null, layout);
		} else if(toolbar != null) {
			group = findGroup(toolbar, ISynchronizePageConfiguration.LAYOUT_GROUP);
			if (group != null) {
				appendToMenu(group.getId(), toolbar);
			}
		}
	}
	
	private void appendToMenu(String groupId, IContributionManager menu) {
		for (Iterator iter = toggleModelProviderActions.iterator(); iter.hasNext();) {
			if (groupId == null) {
				menu.add((Action) iter.next());
			} else {
				menu.appendToGroup(groupId, (Action) iter.next());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#dispose()
	 */
	public void dispose() {
		if(modelProvider != null) {
			modelProvider.dispose();
		}
		super.dispose();
	}
	
	/**
	 * @return Returns the configuration.
	 */
	public ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}
}
