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
package org.eclipse.update.standalone;
import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;


/**
 * Removes a configured site.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public class RemoveSiteCommand extends ScriptedCommand {
	private IConfiguredSite csite;
	private File sitePath;
	
	/**
	 * @param toSite if specified, list only the features from the specified local install site
	 */
	public RemoveSiteCommand(String toSite) throws Exception {
		try {
			if (toSite != null) {
				sitePath = new File(toSite);
				if (!sitePath.getName().equals("eclipse")) //$NON-NLS-1$
					sitePath = new File(sitePath, "eclipse"); //$NON-NLS-1$
				if (!sitePath.exists())
					throw new Exception(Policy.bind("Standalone.noSite") + toSite); //$NON-NLS-1$
					
				IConfiguredSite[] csites = SiteManager.getLocalSite().getCurrentConfiguration().getConfiguredSites();
				for (int i=0; i<csites.length; i++) {
					File f = new File(csites[i].getSite().getURL().getFile());
					if (f.equals(sitePath)) {
						csite = csites[i];
						break;
					}
				}
				
				if (csite == null)
					throw new Exception(Policy.bind("Standalone.noConfiguredSite") + toSite); //$NON-NLS-1$
			} else {
				throw new Exception(Policy.bind("Standalone.noSite3")); //$NON-NLS-1$
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
		try {
			getConfiguration().removeConfiguredSite(csite);
			// update the sites array
			getConfiguration().getConfiguredSites();
			SiteManager.getLocalSite().save();
			return true;
		} catch (CoreException e) {
			UpdateCore.log(e);
			return false;
		}
	}
}
