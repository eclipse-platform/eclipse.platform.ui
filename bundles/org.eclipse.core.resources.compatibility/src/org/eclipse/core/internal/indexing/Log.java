/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

import java.io.*;

class Log {

	/**
	 * Deletes the transaction log from the file system.
	 */
	static void delete(String storeName) {
		new File(name(storeName)).delete();
	}

	/** 
	 * Returns true iff the transaction log exists in the file system.
	 */
	static boolean exists(String storeName) {
		return new File(name(storeName)).exists();
	}

	/**
	 * Returns the name of the log file, given the store name.
	 */
	static String name(String storeName) {
		return storeName + ".log"; //$NON-NLS-1$
	}
}
