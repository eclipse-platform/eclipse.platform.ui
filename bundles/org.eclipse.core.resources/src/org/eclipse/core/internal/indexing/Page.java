package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public abstract class Page extends Observable implements Referable {

	public static final int SIZE = 8192;

	protected int pageNumber;
	protected int referenceCount;
	protected Buffer pageBuffer;
	protected PageStore pageStore;
	
	/**
	 * Default constructor.
	 */
	protected Page() {
	}
		
	/**
	 * Constructs a new page of the given size.
	 */
	public Page(int pageNumber, PageStore pageStore) {
		this.pageNumber = pageNumber;
		this.pageStore = pageStore;
		this.referenceCount = 0;
	}

	/**
	 * Adds a reference for this entity to track.
	 */
	public int addReference() {
		referenceCount++;
		return referenceCount;
	}

	/**
	 * Returns the page number of the page.
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * Tests for existing references.
	 */
	public boolean hasReferences() {
		return referenceCount > 0;
	}

	/** 
	 * Releases a page back to the store.
	 */
	public void release() {
		pageStore.release(this);
	}

	/**
	 * Removes a reference.
	 */
	public int removeReference() {
		if (referenceCount > 0) referenceCount--;
		return referenceCount;
	}
	
	/**
	 * Writes the contents of the page to a buffer.
	 */
	public abstract void toBuffer(byte[] buffer);

}
