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

class TestPage extends Page {

	/**
	 * Creates a new page.
	 */
	protected TestPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		super(pageNumber, pageStore);
		pageBuffer = new Buffer(Page.SIZE);
		pageBuffer.copyFrom(buffer);
	}

	public boolean check(byte b) {
		for (int i = 0; i < pageBuffer.length(); i++) {
			if (pageBuffer.getByte(i) == b) continue;
			return false;
		}			
		return true;
	}

	public void fill(byte b) {
		for (int i = 0; i < pageBuffer.length(); i++) {
			pageBuffer.put(i, b);
		}
		setChanged();
		notifyObservers();
	}

	public byte value() {
		return pageBuffer.getByte(0);
	}
	
	public void toBuffer(byte[] buffer) {
		pageBuffer.copyTo(buffer);
	}
}
