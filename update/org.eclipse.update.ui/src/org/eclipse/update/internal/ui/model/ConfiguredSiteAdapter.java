/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.configuration.*;

/**
 * @version 	1.0
 * @author
 */
public class ConfiguredSiteAdapter
	extends UIModelObject
	implements IConfiguredSiteAdapter {
	private IInstallConfiguration config;
	private IConfiguredSite csite;
		
	public ConfiguredSiteAdapter(IInstallConfiguration config, IConfiguredSite csite) {
		this.csite = csite;
		this.config = config;
	}
	
	public IConfiguredSite getConfiguredSite() {
		return csite;
	}

	public IInstallConfiguration getInstallConfiguration() {
		return config;
	}
	
	public boolean equals(Object object) {
		if (object==null) return false;
		if (object == this) return true;
		if (object instanceof ConfiguredSiteAdapter) {
			ConfiguredSiteAdapter adapter = (ConfiguredSiteAdapter)object;
			return csite!=null && csite.getSite().equals(adapter.getConfiguredSite().getSite());
		}
		return false;
	}
	
	public String toString() {
		return getConfiguredSite().getSite().getURL().toString();
	}
}
