/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.progress.WorkbenchSiteProgressService;

/**
 * The ProgressUtil is a repository for methods that will be replaced by
 * eventual API changes. All methods here are subject to change.
 */
public class ProgressUtil {

	/**
	 * Return the progress service for this site if there is on. <b>NOTE:</b>.
	 * This is temporary API that will be replaced by
	 * IWorkbenchPartSite.getAdapter(IWorkbenchSiteProgressService) when
	 * changes can be made to IWorkbenchPartSite.
	 * 
	 * @param site
	 * @return
	 */
	public IWorkbenchSiteProgressService getProgressService(IWorkbenchPartSite site) {
		if (site instanceof PartSite)
			return new WorkbenchSiteProgressService((PartSite) site);
		else
			return null;
	}

}
