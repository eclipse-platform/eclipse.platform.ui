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

import org.eclipse.core.runtime.IPluginPrerequisite;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.Version;

public class PluginPrerequisite implements IPluginPrerequisite {
	private BundleSpecification prereq = null;

	public PluginPrerequisite(BundleSpecification b) {
		prereq = b;
	}

	public PluginVersionIdentifier getResolvedVersionIdentifier() {
		Version actualVersion = prereq.getActualVersion();
		if (actualVersion == null)
			return null;
		return new PluginVersionIdentifier(actualVersion.toString());
	}

	public String getUniqueIdentifier() {
		return prereq.getName();
	}

	public PluginVersionIdentifier getVersionIdentifier() {
		Version specifiedVersion = prereq.getVersionSpecification();
		if (specifiedVersion == null)
			return null;
		return new PluginVersionIdentifier(specifiedVersion.toString());
	}

	public boolean isExported() {
		return prereq.isExported();
	}

	public boolean isMatchedAsGreaterOrEqual() {
		return prereq.getMatchingRule() == BundleSpecification.GREATER_EQUAL_MATCH;
	}

	public boolean isMatchedAsCompatible() {
		return prereq.getMatchingRule() == BundleSpecification.MAJOR_MATCH || prereq.getMatchingRule() == BundleSpecification.NO_MATCH;
	}

	public boolean isMatchedAsEquivalent() {
		return prereq.getMatchingRule() == BundleSpecification.MINOR_MATCH;
	}

	public boolean isMatchedAsPerfect() {
		return prereq.getMatchingRule() == BundleSpecification.QUALIFIER_MATCH;
	}

	public boolean isMatchedAsExact() {
		return isMatchedAsEquivalent();
	}

	public boolean isOptional() {
		return prereq.isOptional();
	}

}