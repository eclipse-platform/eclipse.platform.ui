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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;


/**
 * Manager for hierarchical models
 */
public class ChangeLogModelManager extends SynchronizeModelManager implements IPropertyChangeListener {
		
	/**
	 * @param configuration
	 */
	public ChangeLogModelManager(ISynchronizePageConfiguration configuration) {
		super(configuration);
		configuration.addPropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#dispose()
	 */
	public void dispose() {
		getConfiguration().removePropertyChangeListener(this);
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#getSupportedModelProviders()
	 */
	protected ISynchronizeModelProviderDescriptor[] getSupportedModelProviders() {
		return new ISynchronizeModelProviderDescriptor[] {
				new HierarchicalModelProvider.HierarchicalModelProviderDescriptor(),
				new CompressedFoldersModelProvider.CompressedFolderModelProviderDescriptor(),
				new ChangeLogModelProvider.ChangeLogModelProviderDescriptor()};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#createModelProvider(java.lang.String)
	 */
	protected ISynchronizeModelProvider createModelProvider(String id) {
		if(id == null) {
			if (getShowCompressedFolders()) {
				id = CompressedFoldersModelProvider.CompressedFolderModelProviderDescriptor.ID;
			} else {
				id = HierarchicalModelProvider.HierarchicalModelProviderDescriptor.ID;
			}
		}
		if(id.endsWith(CompressedFoldersModelProvider.CompressedFolderModelProviderDescriptor.ID)) {
			return new CompressedFoldersModelProvider(getSyncInfoSet());
		} else if(id.endsWith(HierarchicalModelProvider.HierarchicalModelProviderDescriptor.ID)) {
			return new HierarchicalModelProvider(getSyncInfoSet());
		} else {
				return new ChangeLogModelProvider(getSyncInfoSet());
		}
	}

	private SyncInfoTree getSyncInfoSet() {
		return (SyncInfoTree)getConfiguration().getProperty(ISynchronizePageConfiguration.P_SYNC_INFO_SET);
	}
	
	/**
	 * Return the state of the compressed folder setting.
	 * 
	 * @return the state of the compressed folder setting.
	 */
	private boolean getShowCompressedFolders() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
	}
}