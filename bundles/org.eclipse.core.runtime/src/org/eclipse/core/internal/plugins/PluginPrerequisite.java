/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;

public class PluginPrerequisite extends PluginPrerequisiteModel implements IPluginPrerequisite {
/**
 * @see IPluginPrerequisite
 */
public PluginVersionIdentifier getResolvedVersionIdentifier() {
	String version = getResolvedVersion();
	return version == null ? null : new PluginVersionIdentifier(version);
}
/**
 * @see IPluginPrerequisite
 */
public String getUniqueIdentifier() {
	return getPlugin();
}
/**
 * @see IPluginPrerequisite
 */
public PluginVersionIdentifier getVersionIdentifier() {
	String version = getVersion();
	return version == null ? null : new PluginVersionIdentifier(version);
}
/**
 * @see IPluginPrerequisite
 */
public boolean isExported() {
	return getExport();
}
/**
 * @see IPluginPrerequisite
 */
public boolean isMatchedAsGreaterOrEqual() {
	return getMatchByte() == PREREQ_MATCH_GREATER_OR_EQUAL;
}
/**
 * @see IPluginPrerequisite
 */
public boolean isMatchedAsCompatible() {
	return (getMatchByte() == PREREQ_MATCH_COMPATIBLE) ||
	        ((getVersionIdentifier() != null) && (getMatchByte() == PREREQ_MATCH_UNSPECIFIED));
}
/**
 * @see IPluginPrerequisite
 */
public boolean isMatchedAsEquivalent() {
	return getMatchByte() == PREREQ_MATCH_EQUIVALENT;
}
/**
 * @see IPluginPrerequisite
 */
public boolean isMatchedAsPerfect() {
	return getMatchByte() == PREREQ_MATCH_PERFECT;
}
/**
 * @see IPluginPrerequisite
 */
public boolean isMatchedAsExact() {
	return isMatchedAsEquivalent();
}
/**
 * @see IPluginPrerequisite
 */
public boolean isOptional() {
	return getOptional();
}
}
