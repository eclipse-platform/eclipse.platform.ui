/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.runtime.IPath;

/**
 * Provides special internal access to the workspace resource implementation.
 * This class is to be used for testing purposes only.
 * 
 * @since 2.1
 */
public class TestingSupport {
	/* 
	 * Class cannot be instantiated.
	 */
	private TestingSupport() {
		// not allowed
	}

	/**
	 * Call HistoryStore.accept().
	 */
	public static void accept(HistoryStore store, IPath path, IHistoryStoreVisitor visitor, boolean partialMatch) {
		store.accept(path, visitor, partialMatch);
	}
	
	/**
	 * Call IHistoryStore.removeGarbage().
	 * @since 3.0
	 */
	public static void removeGarbage(IHistoryStore store) {
		store.removeGarbage();
	}
}