package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
public boolean isMatchedAsCompatible() {
	return !isMatchedAsExact();
}
/**
 * @see IPluginPrerequisite
 */
public boolean isMatchedAsExact() {
	return getMatch();
}
/**
 * @see IPluginPrerequisite
 */
public boolean isOptional() {
	return getOptional();
}
}
