/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;


public class VersionedIdentifier {
	private String identifier = ""; //$NON-NLS-1$
	private int major = 0;
	private int minor = 0;
	private int service = 0;
	private String qualifier = ""; //$NON-NLS-1$
	private String version;

	public static final int LESS_THAN = -1;
	public static final int EQUAL = 0;
	public static final int EQUIVALENT = 1;
	public static final int COMPATIBLE = 2;
	public static final int GREATER_THAN = 3;
	
	public VersionedIdentifier(String id, String version) {
		this.identifier = id;
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public boolean equalIdentifiers(VersionedIdentifier id) {
		if (id == null)
			return identifier == null;
		return id.identifier.equals(identifier);
	}

	public int compareVersion(VersionedIdentifier id) {

		if (id == null) {
			if (major == 0 && minor == 0 && service == 0)
				return -1;
			return 1;
		}

		if (major > id.major)
			return GREATER_THAN;
		if (major < id.major)
			return LESS_THAN;
		if (minor > id.minor)
			return COMPATIBLE;
		if (minor < id.minor)
			return LESS_THAN;
		if (service > id.service)
			return EQUIVALENT;
		if (service < id.service)
			return LESS_THAN;
		return compareQualifiers(qualifier, id.qualifier);
	}

	private int compareQualifiers(String q1, String q2) {
		int result = q1.compareTo(q2);
		if (result < 0)
			return LESS_THAN;
		else if (result > 0)
			return EQUIVALENT;
		else
			return EQUAL;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof VersionedIdentifier))
			return false;
		
		VersionedIdentifier other = (VersionedIdentifier)obj;
		return equalIdentifiers(other) &&
			this.major == other.major &&
			this.minor == other.minor &&
			this.service == other.service &&
			this.version.equals(other.version) && 
			compareQualifiers(this.qualifier, other.qualifier) == EQUAL;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (identifier + "_" + version).hashCode(); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return identifier + "_" + version; //$NON-NLS-1$
	}
}
