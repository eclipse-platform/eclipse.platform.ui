/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.indexing;

import java.io.*;
import java.util.*;

class Log {


/**
 * Creates the log file in the file system.  The string 
 * argument is the name of the page store for which this log will
 * be created.
 */
static void create(String storeName) throws PageStoreException {
	try {
		new RandomAccessFile(name(storeName), "rw").close(); //$NON-NLS-1$
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.LogCreateFailure, e);
	}
}
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
