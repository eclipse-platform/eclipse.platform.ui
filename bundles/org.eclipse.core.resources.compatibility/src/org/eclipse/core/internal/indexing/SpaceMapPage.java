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

class SpaceMapPage extends ObjectStorePage {
	private static int[] SpaceClassSize = {7616, 6906, 6196, 5486, 4776, 4066, 3356, 2646, 1936, 1226, 516, 258, 129, 64, 32, 0};

	/**
	 * Returns the guaranteed amount of free space available for a given space class.
	 */
	public static int freeSpaceForClass(int spaceClass) {
		return SpaceClassSize[spaceClass];
	}

	/**
	 * Creates a new page from a buffer.
	 */
	public SpaceMapPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		super(pageNumber, buffer, pageStore);
	}

	/**
	 * Writes the contents of the page to a buffer.
	 */
	public void toBuffer(byte[] buffer) {
		int n = Math.min(buffer.length, pageBuffer.length());
		System.arraycopy(pageBuffer.getByteArray(), 0, buffer, 0, n);
	}

	/**
	 * Searches a space map page in the page file for an object page
	 * that has at least "bytesNeeded" bytes free.  Returns 0 if there is no
	 * object page in this space map page that meets this criteria.  0 is not a valid
	 * object page number.  All page numbers that are 0 mod 8192 are space map pages.
	 */
	//	public int findObjectPageNumberForSize(int bytesNeeded) {
	//		for (int i = 1; i < SIZE; i++) {		// begin at 1, 0 is the space map page
	//			int spaceClass = pageBuffer.getByte(i);
	//			int freeSpace = freeSpaceForClass(spaceClass);
	//			if (freeSpace >= bytesNeeded) return pageNumber + i;
	//		}
	//		return 0;
	//	}
	/**
	 * Returns the guaranteed amount of free space on a page.
	 * If the page number is a space map page number, 0 is returned.
	 */
	public int getFreeSpace(int pageNumber) {
		int slot = pageNumber - this.pageNumber;
		if (slot < 1 || slot >= SIZE)
			return 0;
		int spaceClass = pageBuffer.getByte(slot);
		int freeSpace = freeSpaceForClass(spaceClass);
		return freeSpace;
	}

	/**
	 * Sets the spaceClass for a given object page.
	 */
	public void setFreeSpace(int pageNumber, int freeSpace) {
		int slot = pageNumber - this.pageNumber;
		if (slot < 1 || slot >= SIZE)
			return;
		byte spaceClass = 0;
		while (SpaceClassSize[spaceClass] > freeSpace)
			spaceClass++;
		pageBuffer.put(slot, spaceClass);
		setChanged();
		notifyObservers();
	}

	protected void materialize() {
		// do nothing
	}
}
