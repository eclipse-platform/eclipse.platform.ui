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
package org.eclipse.core.internal.plugins;

import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.runtime.IPluginPrerequisite;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

//TODO Some methods in this class needs to be refined.  
public class PluginPrerequisite implements IPluginPrerequisite {
	private Bundle prereq = null;

	public PluginPrerequisite(Bundle b) {
		Assert.isNotNull(b);
		prereq = b;
	}

	public PluginVersionIdentifier getResolvedVersionIdentifier() {
		return new PluginVersionIdentifier((String) prereq.getHeaders().get(Constants.BUNDLE_VERSION));
	}

	public String getUniqueIdentifier() {
		return prereq.getGlobalName();
	}

	public PluginVersionIdentifier getVersionIdentifier() {
		return getResolvedVersionIdentifier();
	}

	public boolean isExported() {
		return true;
	}

	public boolean isMatchedAsGreaterOrEqual() {
		return false;
	}

	public boolean isMatchedAsCompatible() {
		return false;
	}

	public boolean isMatchedAsEquivalent() {
		return true;
	}

	public boolean isMatchedAsPerfect() {
		return false;
	}

	public boolean isMatchedAsExact() {
		return false;
	}

	public boolean isOptional() {
		return false;
	}

}
