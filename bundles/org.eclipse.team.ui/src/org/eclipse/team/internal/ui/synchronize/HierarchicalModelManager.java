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

import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.StructuredViewerAdvisor;

/**
 * Manager for hierarchical models
 */
public class HierarchicalModelManager extends SynchronizeModelManager {
	
	/**
	 * @param configuration
	 */
	public HierarchicalModelManager(StructuredViewerAdvisor advisor, ISynchronizePageConfiguration configuration) {
		super(advisor, configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeModelManager#getSupportedModelProviders()
	 */
	protected ISynchronizeModelProviderDescriptor[] getSupportedModelProviders() {
		return new ISynchronizeModelProviderDescriptor[] {
				new HierarchicalModelProvider.HierarchicalModelProviderDescriptor(),
				new CompressedFoldersModelProvider.CompressedFolderModelProviderDescriptor() };
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
		} else {
			return new HierarchicalModelProvider(getSyncInfoSet());
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
}
