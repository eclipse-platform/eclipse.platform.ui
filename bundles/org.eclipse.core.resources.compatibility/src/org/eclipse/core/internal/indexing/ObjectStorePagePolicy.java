/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

public class ObjectStorePagePolicy extends AbstractPagePolicy {

	/**
	 * @see AbstractPagePolicy#createPage(int, byte[], PageStore)
	 */
	public final Page createPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		if (pageNumber % Page.SIZE == 0) {
			return new SpaceMapPage(pageNumber, buffer, pageStore);
		}
		return new ObjectPage(pageNumber, buffer, pageStore);
	}
}
