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
package org.eclipse.help.internal.search.federated;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.SearchIndexWithIndexingProgress;

public class IndexerJob extends Job {
	public static final String FAMILY = "org.eclipse.help.base.indexer"; //$NON-NLS-1$
	public IndexerJob() {
		super(HelpBaseResources.IndexerJob_name); 
	}
	protected IStatus run(IProgressMonitor monitor) {
		SearchIndexWithIndexingProgress index = BaseHelpSystem.getLocalSearchManager().getIndex(Platform.getNL());
		try {
			BaseHelpSystem.getLocalSearchManager().ensureIndexUpdated(monitor, index);
			return Status.OK_STATUS;
		}
		catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
		catch (Exception e) {
			return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.OK, HelpBaseResources.IndexerJob_error, e); 
		}
	}
	public boolean belongsTo(Object family) {
		return FAMILY.equals(family);
	}
}
