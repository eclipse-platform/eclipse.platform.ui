package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Local Volume Info.
 * Represents local file system information for a specific volume.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see org.eclipse.update.configuration.LocalSystemInfo
 * @since 2.0
 */
public interface IVolume extends IAdaptable {

	/**
	 * Returns the available free space on this volume.
	 * Returns the amount of free space available to this
	 * user on the volume. The
	 * method takes into account any space quotas or other
	 * native mechanisms that may restrict space usage
	 * on a given volume.
	 * @return the amount of free space available (in units
	 * of Kbyte), or an indication the size is not known 
	 * @see LocalSystemInfo#SIZE_UNKNOWN
	 * @since 2.0
	 */
	public long getFreeSpace();
	
	/**
	 * returns volume label.
	 * Returns the label of the volume.
	 * @return volume label (as string), or <code>null</code> if
	 * the label cannot be determined.
	 * @since 2.0
	 */
	public String getLabel();
	
	/**
	 * Returns volume type.
	 * Returns the type of the volume.
	 * @return volume type
	 * @see LocalSystemInfo#VOLUME_UNKNOWN
	 * @see LocalSystemInfo#VOLUME_INVALID_PATH
	 * @see LocalSystemInfo#VOLUME_REMOVABLE
	 * @see LocalSystemInfo#VOLUME_FIXED
	 * @see LocalSystemInfo#VOLUME_REMOTE
	 * @see LocalSystemInfo#VOLUME_CDROM
	 * @see LocalSystemInfo#VOLUME_FLOPPY_3
	 * @see LocalSystemInfo#VOLUME_FLOPPY_5
	 * @since 2.0
	 */
	public int getType();
	
	/**
	 * Returns the volume path.
	 * Returns the path that represents the mount point of the volume.
	 * @return mount point file
	 * @since 2.0
	 */
	public File getFile();
}