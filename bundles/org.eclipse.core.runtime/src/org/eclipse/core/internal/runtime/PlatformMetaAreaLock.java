/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.nio.channels.FileLock;

//TODO Given the callers, it should be removed 
public class PlatformMetaAreaLock {
	private FileLock fileLock;
	private FileOutputStream fileStream;
	private File lockFile;

	public PlatformMetaAreaLock(File lockFile) {
		this.lockFile = lockFile;
	}

	public boolean acquire() throws IOException {
		fileStream = new FileOutputStream(lockFile, true);
		fileLock = fileStream.getChannel().tryLock();
		return fileLock != null;
	}

	public void release() {
		if (fileLock != null) {
			try {
				fileLock.release();
			} catch (IOException e) {
				//don't complain, we're making a best effort to clean up
			}
			fileLock = null;
		}
		if (fileStream != null) {
			try {
				fileStream.close();
			} catch (IOException e) {
				//don't complain, we're making a best effort to clean up
			}
			fileStream = null;
		}
	}
}