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
package org.eclipse.update.standalone;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;

/**
 * Adds a new site.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public class AddSiteCommand extends ScriptedCommand {
	private ISite site;
	private File sitePath;
	
	/**
	 * @param fromSite if specified, list only the features from the specified local install site
	 */
	public AddSiteCommand(String fromSite) throws Exception {
		try {
			if (fromSite != null) {
				sitePath = new File(fromSite);
				if (!sitePath.exists())
					throw new Exception(Policy.bind("Standalone.noSite") + fromSite); //$NON-NLS-1$
					
				URL fromSiteURL = sitePath.toURL();
				site = SiteManager.getSite(fromSiteURL, null);
				if (site == null) {
					throw new Exception(Policy.bind("Standalone.noSite") + fromSite); //$NON-NLS-1$
				}
				IConfiguredSite csite = site.getCurrentConfiguredSite();
				if (csite != null)
					throw new Exception(Policy.bind("Standalone.siteConfigured") + fromSite); //$NON-NLS-1$
			} else {
				throw new Exception(Policy.bind("Standalone.noSite3") ); //$NON-NLS-1$
			}		
		} catch (Exception e) {
			throw e;
		} 
	}

	/**
	 */
	public boolean run(IProgressMonitor monitor) {
		// check if the config file has been modifed while we were running
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			UpdateCore.log(status);
			return false;
		}
		
		if (site == null)
			return false;
			
		try {
			IConfiguredSite csite = getConfiguration().createConfiguredSite(sitePath);
			getConfiguration().addConfiguredSite(csite);
			// update the sites array to pick up new site
			getConfiguration().getConfiguredSites();
			SiteManager.getLocalSite().save();
			return true;
		} catch (CoreException e) {
			UpdateCore.log(e);
			return false;
		}
	}
}
