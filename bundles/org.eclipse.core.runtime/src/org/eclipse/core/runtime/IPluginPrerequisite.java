package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A prerequisite entry declared by a plug-in. The declaration causes
 * classes defined by the prerequisite plug-in to be visible
 * to the plug-in that declared the dependency.
 * <p>
 * This interface is not intended to be implemented by developers.
 * </p>
 *
 * @see IPluginDescriptor#getPluginPrerequisites
 */
public interface IPluginPrerequisite {
/**
 * Returns the actual version identifier that is used
 * at runtime to resolve this prerequisite dependency,
 * or null, if the dependency is not resolved.
 * 
 * @return the plug-in version identifier, or null
 */
public PluginVersionIdentifier getResolvedVersionIdentifier();
/**
 * Returns the plug-in identifier of the prerequisite plug-in.
 * 
 * @return the plug-in identifier
 */
public String getUniqueIdentifier();
/**
 * Returns the version identifier of the prerequisite plug-in,
 * or <code>null</code> if none.
 * 
 * @return the plug-in version identifier, or <code>null</code> if 
 *    none was specified
 */
public PluginVersionIdentifier getVersionIdentifier();
/**
 * Indicates whether this prerequisite plug-in is further exposed to any
 * plug-ins that declare a dependency on this plug-in. This allows
 * for chaining of dependencies. For example, if plug-in A depends
 * on plug-in B which depends on plug-in C, the classes from C 
 * are typically visible to B, but not to A.  A can get around this 
 * if either B explicitly exports its dependency on C, or 
 * A explicitly declares C as a prerequisite in addition to B.
 * 
 * @return <code>true</code> if this prerequisite plug-in is exposed,
 *    <code>false</code> otherwise
 */
public boolean isExported();
/**
 * Indicates that this plug-in prerequisite can be resolved
 * against a configured plug-in with an identifier that is
 * greater than or equal to it.
 *
 * @return <code>true</code> if greater or equal match is allowed,
 *   <code>false</code> otherwise.
 * @since 2.0
 */
public boolean isMatchedAsGreaterOrEqual();
/**
 * Indicates that this plug-in prerequisite can be resolved
 * against a configured plug-in with a compatible identifier.
 *
 * @return <code>true</code> if compatible match is allowed,
 *   <code>false</code> if exact match is required.
 */
public boolean isMatchedAsCompatible();
/**
 * Indicates that this plug-in prerequisite can only be resolved
 * against a configured plug-in with an equivalent plug-in 
 * identifier.
 *
 * @return <code>true</code> if only equivalent identifier match
 * satisfies this dependency, <code>false</code> otherwise.
 * @since 2.0
 */
public boolean isMatchedAsEquivalent();
/**
 * Indicates that this plug-in prerequisite can only be resolved
 * against a configured plug-in with a plug-in identifier that
 * is perfectly equal.
 *
 * @return <code>true</code> if only perfectly equal
 * identifier match satisfies this dependency,
 * <code>false</code> otherwise.
 * @since 2.0
 */
public boolean isMatchedAsPerfect();
/**
 * Indicates that this plug-in prerequisite can only be resolved
 * against a configured plug-in with exactly the same plug-in 
 * identifier.
 *
 * @return <code>true</code> if only exact identifier match
 * satisfies this dependency, <code>false</code> if compatible
 * plug-in will satisfy this dependency.
 */
public boolean isMatchedAsExact();
/**
 * Indicates whether this plug-in prerequisite is optional.  If a required (i.e., non-optional)
 * prerequisite is missing, this plugin is disabled.  
 *
 * @return <code>true</code> if this prerequisite is optional, <code>false</code> otherwise
 */
public boolean isOptional();
}
