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
package org.eclipse.update.configurator.migration;

import java.util.Map;
import java.util.Set;

public interface IPluginInfo {
	public Map getLibraries();
	public String[] getLibrariesName();
	public String[] getRequires();
	public String getMasterId();
	public String getMasterVersion();
	public String getPluginClass();
	public String getUniqueId();
	public String getVersion();
	public boolean isFragment();
	public Set getPackageFilters();
}
