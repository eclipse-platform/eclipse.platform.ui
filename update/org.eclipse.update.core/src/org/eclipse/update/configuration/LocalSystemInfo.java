package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.util.ArrayList;

import org.eclipse.update.internal.core.UpdateManagerPlugin;
 
/**
 * Utility class providing local file system information.
 * The class attempts to load a native library implementation
 * of its methods. If successful, the method calls are delegated
 * to the native implementation. Otherwise a default non-native
 * implementation is used. 
 * @see ILocalSystemInfoListener
 * @since 2.0
 */
public class LocalSystemInfo {
	
	/**
	 * Indicates the amount of available free space is not known
	 * @see LocalSystemInfo#getFreeSpace(File)
	 * @since 2.0
	 */
	public static final long SIZE_UNKNOWN = -1;
	
	/**
	 * Indicates the volume type is not known
	 * @see LocalSystemInfo#getType(File)
	 * @since 2.0
	 */
	public static final int VOLUME_UNKNOWN = -1;
	
	/**
	 * Indicates the volume could not be determined from path
	 * @see LocalSystemInfo#getType(File)
	 * @since 2.0
	 */
	public static final int VOLUME_INVALID_PATH = -2;
	
	/**
	 * Indicates the volume is removable
	 * @see LocalSystemInfo#getType(File)
	 * @since 2.0
	 */
	public static final int VOLUME_REMOVABLE = 1;
	
	/**
	 * Indicates the volume is fixed (not removable)
	 * @see LocalSystemInfo#getType(File)
	 * @since 2.0
	 */
	public static final int VOLUME_FIXED = 2;
	
	/**
	 * Indicates a remote (network) volume
	 * @see LocalSystemInfo#getType(File)
	 * @since 2.0
	 */
	public static final int VOLUME_REMOTE = 3;
	
	/**
	 * Indicates a cdrom volume
	 * @see LocalSystemInfo#getType(File)
	 * @since 2.0
	 */
	public static final int VOLUME_CDROM = 4;	
	
	private static ArrayList listeners = new ArrayList();	
	private static boolean hasNatives = false;	
	static {
		try {
			System.loadLibrary("update"); //$NON-NLS-1$
			hasNatives = true;
		} catch (UnsatisfiedLinkError e) {
			//DEBUG
			if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
				UpdateManagerPlugin.getPlugin().debug("Unable to load native library 'update'."); //$NON-NLS-1$
			}
				UpdateManagerPlugin.getPlugin().debug("Unable to load native library 'update'."); //$NON-NLS-1$			
			hasNatives = false;
		}
	}
	
	/**
	 * Determines available free space on a volume.
	 * Returns the amount of free space available to this
	 * user on the volume containing the specified path. The
	 * method takes into account any space quotas or other
	 * native mechanisms that may restrict space usage
	 * on a given volume.
	 * @param path file path. May contain path elements beyond
	 * the volume "root"
	 * @return the amount of free space available (in units
	 * of Kbyte), or an indication the size is not known 
	 * @see LocalSystemInfo#SIZE_UNKNOWN
	 * @since 2.0
	 */
	public static long getFreeSpace(File path) {
		if (hasNatives) {
			try {
				return nativeGetFreeSpace(path);
			} catch (UnsatisfiedLinkError e) {
			}
		}
		return SIZE_UNKNOWN;
	}
	
	/**
	 * Determines volume label.
	 * Returns the label of the volume containing the specified
	 * path.
	 * @param path file path. May contain path elements beyond
	 * the volume "root"
	 * @return volume label (as string), or <code>null</code> if
	 * the label cannot be determined.
	 * @since 2.0
	 */
	public static String getLabel(File path) {
		if (hasNatives) {
			try {
				return nativeGetLabel(path);
			} catch (UnsatisfiedLinkError e) {
			}
		}
		return null;
	}
	
	/**
	 * Determines volume type.
	 * Returns the type of the volume containing the specified
	 * path.
	 * @param path file path. May contain path elements beyond
	 * the volume "root"
	 * @return volume type
	 * @see LocalSystemInfo#VOLUME_UNKNOWN
	 * @see LocalSystemInfo#VOLUME_INVALID_PATH
	 * @see LocalSystemInfo#VOLUME_REMOVABLE
	 * @see LocalSystemInfo#VOLUME_FIXED
	 * @see LocalSystemInfo#VOLUME_REMOTE
	 * @see LocalSystemInfo#VOLUME_CDROM
	 * @since 2.0
	 */
	public static int getType(File path) {
		if (hasNatives) {
			try {
				return nativeGetType(path);
			} catch (UnsatisfiedLinkError e) {
			}
		}
		return VOLUME_UNKNOWN;
	}
	
	/**
	 * Lists the file system mount points.
	 * @return array of absolute file paths representing mount
	 * points, or <code>null</code> if none found
	 * @since 2.0
	 */
	public static String[] listMountPoints() {
		if (hasNatives) {
			try {
				return nativeListMountPoints();
			} catch (UnsatisfiedLinkError e) {
			}
		}
		return null;
	}
	
	/**
	 * Add local system change listener.
	 * Allows a listener to be added for monitoring changes
	 * in the local system information. The listener is notified
	 * each time there are relevant file system changes
	 * detected. This specifically includes changes to the
	 * file system structure as a result of removable drive/ media
	 * operations (eg. CD insertion), and changes to volume 
	 * mount structure.
	 * @param listener change listener
	 * @since 2.0
	 */
	public static void addInfoListener(ILocalSystemInfoListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	/**
	 * Remove local system change listener
	 * @param listener change listener
	 * @since 2.0
	 */
	public static void removeInfoListener(ILocalSystemInfoListener listener) {
		listeners.remove(listener);
	}
		
	/**
	 * Notify listeners of change.
	 * @param path file path representing the "root" of the
	 * change file system structure. Any current paths beyond
	 * the specified "root" are assumed to be invalidated.
	 */
	public static void fireRootChanged(File path) {
		for (int i=0; i< listeners.size(); i++) {
			((ILocalSystemInfoListener)listeners.get(i)).rootChanged(path);
		}
	}
		
	/*
	 * Native implementations.
	 */
	private static native long nativeGetFreeSpace(File path);
	private static native String nativeGetLabel(File path);
	private static native int nativeGetType(File path);
	private static native String[] nativeListMountPoints();
}
