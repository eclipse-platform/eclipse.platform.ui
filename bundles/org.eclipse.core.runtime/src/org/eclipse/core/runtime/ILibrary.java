package org.eclipse.core.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * A runtime library declared in a plug-in.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPluginDescriptor#getRuntimeLibraries 
 */
public interface ILibrary {
/**
 * Returns the content filters, or <code>null</code>.
 * Each content filter identifies a specific class, or
 * a group of classes, using a notation and matching rules
 * equivalent to Java <code>import</code> declarations
 * (e.g., "java.io.File", or "java.io.*"). Returns <code>null</code>
 * if the library is not exported, or it is fully exported
 * (no filtering).
 *
 * @return the content filters, or <code>null</codel> if none
 */
public String[] getContentFilters();
/**
 * Returns the path of this runtime library, relative to the
 * installation location.
 *
 * @return the path of the library
 * @see IPluginDescriptor#getInstallURL
 */
public IPath getPath();
/**
 * Returns whether the library is exported. The contents of an exported
 * library may be visible to other plug-ins that declare a dependency
 * on the plug-in containing this library, subject to content filtering.
 * Libraries that are not exported are entirely private to the declaring
 * plug-in.
 *
 * @return <code>true</code> if the library is exported, <code>false</code>
 *    if it is private
 */
public boolean isExported();
/**
 * Returns whether this library is fully exported. A library is considered
 * fully exported iff it is exported and has no content filters.
 *
 * @return <code>true</code> if the library is fully exported, and
 *    <code>false</code> if it is private or has filtered content
 */
public boolean isFullyExported();
}
