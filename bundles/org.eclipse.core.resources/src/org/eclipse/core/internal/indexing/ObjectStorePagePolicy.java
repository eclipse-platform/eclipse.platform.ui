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

public class ObjectStorePagePolicy extends AbstractPagePolicy {

	/**
	 * @see PagePolicy#createPage(int, byte[], PageStore)
	 */
	public final Page createPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		if (pageNumber % Page.SIZE == 0) {
			return new SpaceMapPage(pageNumber, buffer, pageStore);
		} else {
			return new ObjectPage(pageNumber, buffer, pageStore);
		}
	}

}
