package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.File;

/**
 * Local system change listener interface.
 * @see LocalSystemInfo#addInfoListener(ILocalSystemInfoListener)
 * @see LocalSystemInfo#removeInfoListener(ILocalSystemInfoListener)
 * @see LocalSystemInfo#fireRootChanged(File)
 * @since 2.0
 */
public interface ILocalSystemInfoListener {
	
	/**
	 * Root change notification.
	 * Called each time there are relevant file system changes
	 * detected. This specifically includes changes to the
	 * file system structure as a result of removable drive/ media
	 * operations (eg. CD insertion), and changes to volume 
	 * mount structure.
	 * @param path file path to the root of the changed file
	 * system structure. Any current paths beyond
	 * the specified root are assumed to be invalidated.
	 * @since 2.0
	 */
	public void rootChanged(File path);
}
