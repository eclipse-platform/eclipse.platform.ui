package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class VersionedIdentifier {
	private String id;
	private Version version;
	
	private static final String	SEPARATOR = "_";
	
	public VersionedIdentifier(String idWithVersion) {
		int loc = idWithVersion.lastIndexOf(SEPARATOR);
		String id = idWithVersion;
		if (loc != -1) {
			id = idWithVersion.substring(0, loc);
			String versionName = idWithVersion.substring(loc+1);
			version = new Version(versionName);
		}
		else {
			version = new Version();
		}
	}
	
	public VersionedIdentifier(String id, String versionName) {
		this.id = id;
		this.version = new Version(versionName);
	}
	
	public String getIdentifier() {
		return id;
	}
	
	public Version getVersion() {
		return version;
	}
	
	public boolean equals(VersionedIdentifier versionedId) {
		return versionedId.getIdentifier().equals(id) &&
		versionedId.getVersion().equals(version);
	}
	
	public String toString(){
		return id+SEPARATOR+version.toString();
	}
}

