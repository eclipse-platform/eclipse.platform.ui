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
package org.eclipse.core.tests.internal.indexing;

import org.eclipse.core.internal.indexing.*;

public class TestPagePolicy extends AbstractPagePolicy {


	/**
	 * @see PageStorePolicy#createPage(int, byte[], PageStore)
	 */
	public Page createPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		return new TestPage(pageNumber, buffer, pageStore);
	}


}
