package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;

public class Page extends Buffer implements IReferable {
	public static final int Size = 8192;
	private static IPageFactory pageFactory = new IPageFactory() {
		public Page create(PageStore store, int pageNumber) {
			Page p = new Page();
			p.initialize(store, pageNumber);
			return p;
		}
	};	
	private static LinkedList bufferPool = new LinkedList();
	protected int pageNumber;
	protected PageStore store;
	protected int referenceCount;
		
	/**
	 * Default constructor -- must be followed by initialize(...)
	 */
	protected Page() {
	}
	/**
	 * Acquires a new page from a page store.
	 */
	public static Page acquire(PageStore store) throws PageStoreException {
		return store.acquire(pageFactory);
	}
	/**
	 * Acquires an existing page from a page store.
	 */
	public static Page acquire(PageStore store, int pageNumber) throws PageStoreException {
		return store.acquire(pageFactory, pageNumber);
	}
	/**
	 * Adds a reference for this entity to track.
	 */
	public int addReference() {
		referenceCount++;
		return referenceCount;
	}
	/**
	 * Called when the page is put back into the store.
	 */
	protected void dematerialize () {
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
 * Initializes an instance of this page.
 */
protected void initialize(PageStore store, int pageNumber) {
	if (bufferPool.isEmpty())
		contents = new byte[Page.Size];
	else
		contents = (byte[]) bufferPool.removeFirst();
	this.pageNumber = pageNumber;
	this.store = store;
}
	/**
	 * Called when the page is read from the store.
	 */
	protected void materialize() {
	}
	/**
	 * Called when this page is modified.
	 */
	protected void modified() {
		store.modified(this);
	}
	/** 
	 * Releases a page back to the store.
	 */
	public void release() throws PageStoreException {
		store.release(this);
	}
	/**
	 * Removes a reference.
	 */
	public int removeReference() {
		if (referenceCount > 0) referenceCount--;
		return referenceCount;
	}
}
