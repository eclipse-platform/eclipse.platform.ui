/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Constants;

/**
 * A prerequisite entry declared by a plug-in. The declaration causes
 * classes defined by the prerequisite plug-in to be visible
 * to the plug-in that declared the dependency.
 *
 * @see IPluginDescriptor#getPluginPrerequisites()
 * @deprecated 
 * In Eclipse 3.0 the plug-in prerequisite representation was changed.  Clients of 
 * <code>IPluginPrerequisite</code> are directed to the headers associated with the relevant bundle.
 * In particular, the <code>Require-Bundle</code> header contains all available information
 * about the prerequisites of a plug-in.  Having retrieved the header, the {@link ManifestElement}
 * helper class can be used to parse the value and discover the individual 
 * prerequisite plug-ins.  The various header attributes are defined in {@link Constants}.
 * <p>For example, 
 * <pre>    String header = bundle.getHeaders().get(Constants.REQUIRE_BUNDLE);
 *     ManifestElement[] elements = ManifestElement.parseHeader(
 *         Constants.REQUIRE_BUNDLE, header);
 *     if (elements == null) 
 *         return;
 *     elements[0].getValue();   // the prerequisite plug-in id
 *     elements[0].getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);   // the prerequisite plug-in version
 *     ...
 * </pre>
 * </p><p>
 * See {@link IPluginDescriptor} for information on the relationship between plug-in 
 * descriptors and bundles.
 * </p><p>
 * This interface must only be used by plug-ins 
 * which explicitly require the org.eclipse.core.runtime.compatibility plug-in.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPluginPrerequisite {
	/**
	 * Returns the actual version identifier that is used
	 * at runtime to resolve this prerequisite dependency,
	 * or null, if the dependency is not resolved.
	 * 
	 * @return the plug-in version identifier, or null
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     BundleDescription prereq = spec.getSupplier();
	 * </pre>
	 */
	public PluginVersionIdentifier getResolvedVersionIdentifier();

	/**
	 * Returns the plug-in identifier of the prerequisite plug-in.
	 * 
	 * @return the plug-in identifier
	 * @deprecated Given a manifest element equivalent of a plug-in 
	 * prerequisite (see the class comment), this method is replaced by:
	 * <pre>
	 *     element.getValue();
	 * </pre>
	 */
	public String getUniqueIdentifier();

	/**
	 * Returns the version identifier of the prerequisite plug-in,
	 * or <code>null</code> if none.
	 * 
	 * @return the plug-in version identifier, or <code>null</code> if 
	 *    none was specified
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     Version reqMinVersion = spec.getVersionRange().getMinimum();
	 * </pre>
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
	 * @deprecated Given a manifest element equivalent of a plug-in 
	 * prerequisite (see the class comment), this method is replaced by:
	 * <pre>
	 *     element.getAttribute(Constants.REPROVIDE_ATTRIBUTE);
	 * </pre>
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
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     VersionRange versionRange = spec.getVersionRange();
	 *     if (versionRange == null || versionRange.getMinimum() == null)
	 *         return false;
	 *     Version minimum = versionRange.getMinimum();
	 *     Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
	 *     if (maximum.equals(Version.maxVersion))
	 *         return true;
	 *     return false;
	 * </pre>
	 */
	public boolean isMatchedAsGreaterOrEqual();

	/**
	 * Indicates that this plug-in prerequisite can be resolved
	 * against a configured plug-in with a compatible identifier.
	 *
	 * @return <code>true</code> if compatible match is allowed,
	 *   <code>false</code> if exact match is required.
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     VersionRange versionRange = spec.getVersionRange();
	 *     if (versionRange == null || versionRange.getMinimum() == null)
	 *         return false;
	 *     Version minimum = versionRange.getMinimum();
	 *     Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
	 *     if (!minimum.isInclusive() || maximum.isInclusive())
	 *         return false;
	 *     else if (minimum.getMajorComponent() == maximum.getMajorComponent() - 1)
	 *         return true;
	 *     return false;	
	 * </pre>
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
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     VersionRange versionRange = spec.getVersionRange();
	 *     if (versionRange == null || versionRange.getMinimum() == null)
	 *         return false;
	 *     Version minimum = versionRange.getMinimum();
	 *     Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
	 *     if (!minimum.isInclusive() || maximum.isInclusive())
	 *         return false;
	 *     else if (minimum.getMajorComponent() == maximum.getMajorComponent() - 1)
	 *         return false;
	 *     else if (minimum.getMajorComponent() != maximum.getMajorComponent())
	 *         return false;
	 *     else if (minimum.getMinorComponent() == maximum.getMinorComponent() - 1)
	 *         return true;
	 *     return false;
	 * </pre>
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
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     VersionRange versionRange = spec.getVersionRange();
	 *     if (versionRange == null || versionRange.getMinimum() == null)
	 *         return false;
	 *     Version minimum = versionRange.getMinimum();
	 *     Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
	 *     if (minimum.equals(maximum))
	 *         return true;
	 *     return false;
	 * </pre>
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
	 * @deprecated Callers of this method should interrogate the current {@link State}
	 * of the platform.  For example, 
	 * <pre>
	 *     State state = Platform.getPlatformAdmin().getState();
	 *     BundleDescription bundle = state.getBundle("my plug-in id", my plug-in version);
	 *     BundleSpecification spec = bundle.getRequiredBundle("required plug-in id");
	 *     VersionRange versionRange = spec.getVersionRange();
	 *     if (versionRange == null || versionRange.getMinimum() == null)
	 *         return false;
	 *     Version minimum = versionRange.getMinimum();
	 *     Version maximum = versionRange.getMaximum() == null ? Version.maxVersion : versionRange.getMaximum();
	 *     if (!minimum.isInclusive() || maximum.isInclusive())
	 *         return false;
	 *     else if (minimum.getMajorComponent() == maximum.getMajorComponent() - 1)
	 *         return false;
	 *     else if (minimum.getMajorComponent() != maximum.getMajorComponent())
	 *         return false;
	 *     else if (minimum.getMinorComponent() == maximum.getMinorComponent() - 1)
	 *         return true;
	 *     return false;
	 * </pre>
	 */
	public boolean isMatchedAsExact();

	/**
	 * Indicates whether this plug-in prerequisite is optional.  If a required (i.e., non-optional)
	 * prerequisite is missing, this plugin is disabled.  
	 *
	 * @return <code>true</code> if this prerequisite is optional, <code>false</code> otherwise
	 * @deprecated Given a manifest element equivalent of a plug-in 
	 * prerequisite (see the class comment), this method is replaced by:
	 * <pre>
	 *     "true".equals(element.getAttribute(Constants.OPTIONAL_ATTRIBUTE);
	 * </pre>
	 */
	public boolean isOptional();
}
