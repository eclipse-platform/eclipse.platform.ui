/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.local;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.unix.UnixFileHandler;
import org.eclipse.core.internal.filesystem.local.unix.UnixFileNatives;

/**
 * Dispatches methods backed by native code to the appropriate platform specific 
 * implementation depending on a library provided by a fragment. Failing this it tries
 * to use Java 7 NIO/2 API's (if they are available).
 */
public class LocalFileNativesManager {
	private static final NativeHandler DEFAULT = new NativeHandler() {
		public boolean putFileInfo(String fileName, IFileInfo info, int options) {
			return false;
		}

		public int getSupportedAttributes() {
			return 0;
		}

		public FileInfo fetchFileInfo(String fileName) {
			return new FileInfo();
		}
	};

	private static NativeHandler DELEGATE = DEFAULT;

	static {
		if (UnixFileNatives.isUsingNatives()) {
			DELEGATE = new UnixFileHandler();
		} else if (LocalFileNatives.isUsingNatives()) {
			DELEGATE = new LocalFileHandler();
		} else {
			try {
				Class c = LocalFileNativesManager.class.getClassLoader().loadClass("org.eclipse.core.internal.filesystem.jdk7.Java7Handler"); //$NON-NLS-1$

				DELEGATE = (NativeHandler) c.newInstance();
			} catch (ClassNotFoundException e) {
				// Class was missing?
				// Leave the delegate as default
			} catch (LinkageError e) {
				// Maybe the bundle was somehow loaded, the class was there but the bytecodes were the wrong version?
				// Leave the delegate as default
			} catch (IllegalAccessException e) {
				// We could not instantiate the object because we have no access
				// Leave delegate as default
			} catch (InstantiationException e) {
				// We could not instantiate the object because of something unexpected
				// Leave delegate as default
			} catch (ClassCastException e) {
				// The handler does not inherit from the correct class
				// Leave delegate as default
			}
		}
	}

	public static int getSupportedAttributes() {
		return DELEGATE.getSupportedAttributes();
	}

	public static FileInfo fetchFileInfo(String fileName) {
		return DELEGATE.fetchFileInfo(fileName);
	}

	public static boolean putFileInfo(String fileName, IFileInfo info, int options) {
		return DELEGATE.putFileInfo(fileName, info, options);
	}

	public static boolean isUsingNatives() {
		return DELEGATE != DEFAULT;
	}
}
