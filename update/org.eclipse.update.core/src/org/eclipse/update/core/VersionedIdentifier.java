package org.eclipse.update.core;
import org.eclipse.core.internal.boot.Policy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class VersionedIdentifier {
	private String id;
	private Version version;
	private static final String	SEPARATOR = "_"; //$NON-NLS-1$
	
/**
* @since 2.0 
	 */
	public VersionedIdentifier(String idWithVersion) {
		
		if (idWithVersion==null || (idWithVersion=idWithVersion.trim()).equals("")) { //$NON-NLS-1$
				this.id = ""; //$NON-NLS-1$
				this.version = new Version(0,0,0);
			}
		
		int loc = idWithVersion.lastIndexOf(SEPARATOR);
		if (loc != -1) {
			id = idWithVersion.substring(0, loc);
			String versionName = idWithVersion.substring(loc+1);
			version = new Version(versionName);
		} else {
			this.id = "";			 //$NON-NLS-1$
			version = new Version(0,0,0);
		}
	}
	
/**
* @since 2.0 
	 */
	public VersionedIdentifier(String id, String versionName) {
		if (id==null || (id=id.trim()).equals("") || versionName==null) //$NON-NLS-1$
				throw new IllegalArgumentException(Policy.bind("VersionedIdentifier.IdOrVersionNull",id,versionName)); //$NON-NLS-1$
		this.id = id;
		this.version = new Version(versionName);
	}
	
/**
* @since 2.0 
	 */
	public String getIdentifier() {
		return id;
	}
	
/**
* @since 2.0 
	 */
	public Version getVersion() {
		return version;
	}
	
/**
* @since 2.0 
	 */
	public String toString(){
		return id.equals("")?"":id+SEPARATOR+version.toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
	

/**
* @since 2.0 
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof VersionedIdentifier)) return false;
		VersionedIdentifier vid = (VersionedIdentifier)obj;
		if (!this.id.equals(vid.id)) return false;
		return this.version.equals(vid.version);
	}
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

}

