package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

/**
 * An object which represents the common attributes
 * of a plug-in or a non-plug-in entry in the
 * packaging manifest.
 * <p>
 * This class cannot be instantiated and must be subclassed.
 * </p>
 * @since 2.0
 */

public abstract class ContentEntryModel extends ModelObject {
	
	public static final long UNKNOWN_SIZE = -1;
	
	private long downloadSize = UNKNOWN_SIZE;
	private long installSize = UNKNOWN_SIZE;
	private String os;
	private String ws;
	private String nl;
	
	/**
	 * Creates a uninitialized entry model object.
	 * 
	 * @since 2.0
	 */
	protected ContentEntryModel() {
		super();
	}
	
	/**
	 * Returns the total download size for the entry.
	 *
	 * @return the entry download size in KBytes
	 * 		or <code>-1</code> if not known
	 * @since 2.0
	 */
	public long getDownloadSize() {
		return downloadSize;
	}
	
	/**
	 * Returns the total install size for the entry.
	 *
	 * @return the entry install size in KBytes
	 * 		or <code>-1</code> if not known
	 * @since 2.0
	 */
	public long getInstallSize() {
		return installSize;
	}
	
	/**
	 * Optional operating system specification.
	 * A comma-separated list of os designators defined by Eclipse 
	 * (in org.eclipse.core.boot.BootLoader).
	 * Indicates this entry should only be installed on one of the specified
	 * os systems. If this attribute is not specified, the entry can be
	 * installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support (user can force installation of feature regardless of this setting).
	 *
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the operating system specification or <code>null</code>.
	 * @since 2.0 
	 */

	public String getOS() {
		return os;
	}
	
	/**
	 * Optional windowing system specification. 
	 * A comma-separated list of ws designators defined by Eclipse
	 * (in org.eclipse.core.boot.BootLoader).
	 * Indicates this feature should only be installed on one of the specified
	 * ws systems. If this attribute is not specified, the feature can be
	 * installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support (user can force installation of feature regardless of this setting).
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the windowing system specification or <code>null</code>.
	 * @since 2.0 
	 */

	public String getWS() {
		return ws;
	}
	
	/**
	 * Optional locale specification. 
	 * A comma-separated list of locale designators defined by Java.
	 * Indicates this feature should only be installed on a system running
	 * with a compatible locale (using Java locale-matching rules).
	 * If this attribute is not specified, the feature can be installed 
	 * on all systems (language-neutral implementation). 
	 * 
	 * This information is used as a hint by the installation and update
	 *  support (user can force installation of feature regardless of this setting).
	 * 
	 * @return the locale specification or <code>null</code>.
	 * @since 2.0 
	 */

	public String getNL() {
		return nl;
	}
	
	/**
	 * Sets the entry download size.
	 * This object must not be read-only.
	 *
	 * @param downloadSize the entry download size in KBytes.
	 * @since 2.0
	 */	
	public void setDownloadSize(long downloadSize) {
		assertIsWriteable();
		if (downloadSize < 0)
			this.downloadSize = UNKNOWN_SIZE;
		else
			this.downloadSize = downloadSize;
	}
	
	/**
	 * Sets the entry install size.
	 * This object must not be read-only.
	 *
	 * @param installSize the entry install size in KBytes.
	 * @since 2.0
	 */	
	public void setInstallSize(long installSize) {
		assertIsWriteable();
		if (installSize < 0)
			this.installSize = UNKNOWN_SIZE;
		else
			this.installSize = installSize;
	}
	
	/**
	 * Sets the operating system specification.
	 * This object must not be read-only.
	 *
	 * @see org.eclipse.core.boot.BootLoader 
	 * @param os comma-separated list of OS identifiers as defined by Eclipse.
	 * @since 2.0
	 */	
	public void setOS(String os) {
		assertIsWriteable();
		this.os = os;
	}
	
	/**
	 * Sets the windowing system specification.
	 * This object must not be read-only.
	 *
	 * @see org.eclipse.core.boot.BootLoader 
	 * @param ws comma-separated list of WS identifiers as defined by Eclipse.
	 * @since 2.0
	 */	
	public void setWS(String ws) {
		assertIsWriteable();
		this.ws = ws;
	}
	
	/**
	 * Sets the locale specification.
	 * This object must not be read-only.
	 *
	 * @param nl comma-separated list of locale identifiers as defined by Java.
	 * @since 2.0
	 */	
	public void setNL(String nl) {
		assertIsWriteable();
		this.nl = nl;
	}
}
