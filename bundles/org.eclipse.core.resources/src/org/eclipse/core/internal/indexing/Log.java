package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		new RandomAccessFile(name(storeName), "rw").close();
	} catch (IOException e) {
		throw new PageStoreException(PageStoreException.LogCreateFailure);
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
	return storeName + ".log";
	}
}
