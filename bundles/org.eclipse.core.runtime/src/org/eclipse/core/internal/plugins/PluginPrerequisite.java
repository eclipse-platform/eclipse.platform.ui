package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;
import java.io.PrintWriter;

public class PluginPrerequisite extends PluginPrerequisiteModel implements IPluginPrerequisite {
public PluginVersionIdentifier getResolvedVersionIdentifier() {
	String version = getResolvedVersion();
	return version == null ? null : new PluginVersionIdentifier(version);
}
public String getUniqueIdentifier() {
	return getPlugin();
}
public PluginVersionIdentifier getVersionIdentifier() {
	String version = getVersion();
	return version == null ? null : new PluginVersionIdentifier(version);
}
public boolean isExported() {
	return getExport();
}
public boolean isMatchedAsCompatible() {
	return !isMatchedAsExact();
}
public boolean isMatchedAsExact() {
	return getMatch();
}
}
