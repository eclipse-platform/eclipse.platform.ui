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
package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.IPluginPrerequisite;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.Version;


/**
 * @deprecated Marking as deprecated to remove the warnings
 */
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
		Version specifiedVersion = prereq.getVersionRange() == null ? null : prereq.getVersionRange().getMinimum();
		if (specifiedVersion == null)
			return null;
		return new PluginVersionIdentifier(specifiedVersion.toString());
	}

	public boolean isExported() {
		return prereq.isExported();
	}

	public boolean isMatchedAsGreaterOrEqual() {
		return isMatchedAsGreaterOrEqual(prereq.getVersionRange());
	}

	public boolean isMatchedAsCompatible() {
		return isMatchedAsCompatible(prereq.getVersionRange());
	}

	public boolean isMatchedAsEquivalent() {
		return isMatchedAsEquivalent(prereq.getVersionRange());
	}

	public boolean isMatchedAsPerfect() {
		return isMatchedAsPerfect(prereq.getVersionRange());
	}

	public boolean isMatchedAsExact() {
		return isMatchedAsEquivalent();
	}

	public boolean isOptional() {
		return prereq.isOptional();
	}

	private static boolean isMatchedAsGreaterOrEqual(VersionRange versionRange) {
		if (versionRange == null || versionRange.getMinimum() == null)
			return false;
		Version minimum = versionRange.getMinimum();
		Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
		if (maximum.equals(Version.maxVersion))
			return true;
		return false;
	}

	private static boolean isMatchedAsPerfect(VersionRange versionRange) {
		if (versionRange == null || versionRange.getMinimum() == null)
			return false;
		Version minimum = versionRange.getMinimum();
		Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
		if (minimum.equals(maximum))
			return true;
		return false;
	}

	private static boolean isMatchedAsEquivalent(VersionRange versionRange) {
		if (versionRange == null || versionRange.getMinimum() == null)
			return false;
		Version minimum = versionRange.getMinimum();
		Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
		if (!minimum.isInclusive() || maximum.isInclusive())
			return false;
		else if (minimum.getMajorComponent() == maximum.getMajorComponent() - 1)
			return false;
		else if (minimum.getMajorComponent() != maximum.getMajorComponent())
			return false;
		else if (minimum.getMinorComponent() == maximum.getMinorComponent() - 1)
			return true;
		return false;
	}

	private static boolean isMatchedAsCompatible(VersionRange versionRange) {
		if (versionRange == null || versionRange.getMinimum() == null)
			return false;
		Version minimum = versionRange.getMinimum();
		Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
		if (!minimum.isInclusive() || maximum.isInclusive())
			return false;
		else if (minimum.getMajorComponent() == maximum.getMajorComponent() - 1)
			return true;
		return false;	
	}
}