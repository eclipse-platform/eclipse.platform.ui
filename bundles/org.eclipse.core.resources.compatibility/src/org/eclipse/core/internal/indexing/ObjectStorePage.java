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

abstract class ObjectStorePage extends Page {

	public static final int SIZE = 8192;

	public ObjectStorePage(int pageNumber, byte[] buffer, PageStore pageStore) {
		super(pageNumber, pageStore);
		pageBuffer = new Buffer(SIZE);
		pageBuffer.copyFrom(buffer);
		materialize();
	}
	protected abstract void materialize();
}
