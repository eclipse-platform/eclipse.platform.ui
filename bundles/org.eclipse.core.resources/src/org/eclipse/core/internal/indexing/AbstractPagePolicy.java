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
package org.eclipse.core.internal.indexing;

public abstract class AbstractPagePolicy {
	
	/**
	 * Returns a page given a page number, a buffer, and the page store.  The
	 * buffer should be copied in the implementations as it may be reused later.
	 * The buffer is extended or truncated to the page size during this copy.
	 * Both the buffer contents and the page number can be used to determine
	 * the type of page to create.
	 */
	public abstract Page createPage(int pageNumber, byte[] buffer, PageStore pageStore);

}
