package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class PageCacheElement {
	protected Page fPage;
	protected PageCacheElement fNext;
	protected PageCacheElement fPrevious;
/**
 * Insert the method's description here.
 *
 * @param page 
 * @param lru int
 */
public PageCacheElement(Page page, PageCacheElement next, PageCacheElement previous) {
	this.fPrevious = previous;
	this.fNext = next;
	this.fPage = page;
}
/**
 * Insert the method's description here.
 *
 * @return int
 */
public PageCacheElement getNext() {
	return fNext;
}
/**
 * Insert the method's description here.
 *
 * @return 
 */
public Page getPage() {
	return fPage;
}
/**
 * Insert the method's description here.
 *
 * @return 
 */
public PageCacheElement getPrevious() {
	return fPrevious;
}
/**
 * Insert the method's description here.
 *
 * @param next 
 */
public void setNext(PageCacheElement next) {
	fNext = next;
}
/**
 * Insert the method's description here.
 *
 * @param previous 
 */
public void setPrevious(PageCacheElement previous) {
	fPrevious = previous;
}
}
