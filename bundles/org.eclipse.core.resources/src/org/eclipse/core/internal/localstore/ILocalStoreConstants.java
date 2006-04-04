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
package org.eclipse.core.internal.localstore;

public interface ILocalStoreConstants {

	/** Common constants for History Store classes. */
	public final static int SIZE_LASTMODIFIED = 8;
	public static final int SIZE_COUNTER = 1;
	public static final int SIZE_KEY_SUFFIX = SIZE_LASTMODIFIED + SIZE_COUNTER;

	/** constants for safe chunky streams */

	// 40b18b8123bc00141a2596e7a393be1e
	public static final byte[] BEGIN_CHUNK = {64, -79, -117, -127, 35, -68, 0, 20, 26, 37, -106, -25, -93, -109, -66, 30};

	// c058fbf323bc00141a51f38c7bbb77c6
	public static final byte[] END_CHUNK = {-64, 88, -5, -13, 35, -68, 0, 20, 26, 81, -13, -116, 123, -69, 119, -58};

	/** chunk delimiter size */
	// BEGIN_CHUNK and END_CHUNK must have the same length
	public static final int CHUNK_DELIMITER_SIZE = BEGIN_CHUNK.length;
}
