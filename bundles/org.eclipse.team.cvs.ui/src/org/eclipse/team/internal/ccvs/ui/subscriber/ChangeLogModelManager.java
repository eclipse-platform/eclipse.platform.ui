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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * Manager for hierarchical models
 */
public class ChangeLogModelManager extends HierarchicalModelManager implements IPropertyChangeListener {
    
    private static final String P_COMMIT_SET_ENABLED = CVSUIPlugin.ID + ".P_COMMIT_SET_ENABLED"; //$NON-NLS-1$
    
    public static final String COMMIT_SET_GROUP = "CommitSet"; //$NON-NLS-1$
    
	/** support for showing change logs for ranges of tags **/
	private CVSTag tag1;
	private CVSTag tag2;
	
	boolean enabled = false;
	
	private class ToggleCommitSetAction extends Action {
        public ToggleCommitSetAction() {
            super(Policy.bind("ChangeLogModelManager.0"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CHANGELOG)); //$NON-NLS-1$
            update();
        }
        public void run() {
            enabled = !enabled;
            update();
            setInput(getSelectedProviderId(), null);
        }
        private void update() {
            setChecked(enabled);
        }
	}
	
	private class CommitSetActionContribution extends SynchronizePageActionGroup {
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			
			appendToGroup(
					ISynchronizePageConfiguration.P_TOOLBAR_MENU, 
					COMMIT_SET_GROUP,
					new ToggleCommitSetAction());
		}
	}
	
	public ChangeLogModelManager(ISynchronizePageConfiguration configuration) {
		this(configuration, null, null);
	}
	
	public ChangeLogModelManager(ISynchronizePageConfiguration configuration, CVSTag tag1, CVSTag tag2) {
		super(configuration);
		this.tag1 = tag1;
		this.tag2 = tag2;
		configuration.addPropertyChangeListener(this);
		configuration.setProperty(SynchronizePageConfiguration.P_MODEL_MANAGER, this);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, COMMIT_SET_GROUP);
		configuration.addActionContribution(new CommitSetActionContribution());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#dispose()
	 */
	public void dispose() {
		getConfiguration().removePropertyChangeListener(this);
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#createModelProvider(java.lang.String)
	 */
	protected ISynchronizeModelProvider createModelProvider(String id) {
	    if (enabled) {
	        return new ChangeLogModelProvider(getConfiguration(), getSyncInfoSet(), tag1, tag2, id);
	    } else {
	        return super.createModelProvider(id);
	    }
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#getSelectedProviderId()
     */
    protected String getSelectedProviderId() {
        String id = super.getSelectedProviderId();
        if (id.equals(ChangeLogModelProvider.ChangeLogModelProviderDescriptor.ID)) {
            return ((ChangeLogModelProvider)getActiveModelProvider()).getSubproviderId();
        } else {
            return id;
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#saveProviderSettings(java.lang.String)
     */
    protected void saveProviderSettings(String id) {
        super.saveProviderSettings(id);
        IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
		if(pageSettings != null) {
			pageSettings.put(P_COMMIT_SET_ENABLED, enabled);
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    public void initialize(ISynchronizePageConfiguration configuration) {
        // Load our setting before invoking super since the inherited
        // initialize will create the provider
        IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
		if(pageSettings != null) {
		    enabled = pageSettings.getBoolean(P_COMMIT_SET_ENABLED);
		}
        super.initialize(configuration);
    }
}
