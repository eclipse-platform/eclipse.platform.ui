package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * This class is used to cache <code>Page</code> objects for quick retrieval.  
 * The cache is of a fixed size, so if it is full and a <code>Page</code> is 
 * <code>put</code> into it, then the least recently used <code>Page</code> is discarded.
 *
 *
 *
 */
import java.util.Hashtable;
 
public class PageCache {
	protected Hashtable fCache;
	protected int fCacheSize;
	protected PageCacheElement fMRU;
	protected PageCacheElement fLRU;
	public final static int UNBOUND_SIZE = -1;
	public final static int MAX_SIZE = 1500296794;
/**
 * Default Constructor.
 * Constructs an open ended <code>PageCache</code>.
 */
public PageCache() {
	this(UNBOUND_SIZE);
}
/**
 * Constructs a <code>PageCache</code> with a maximum capacity of <code>cacheSize</code> Pages.
 * If the argument is -1 an open ended cache will be created.
 *
 *
 * @param cacheSize int  the maximum capacity of the <code>PageCache</code>.
 */
public PageCache(int cacheSize) {
	if (cacheSize < UNBOUND_SIZE || cacheSize > MAX_SIZE) {
		throw new IllegalArgumentException();
	}
	if (cacheSize == UNBOUND_SIZE) {
		cacheSize = 357;
		this.fCacheSize = MAX_SIZE;
	} else {
		this.fCacheSize = cacheSize;
	}
	float tempLoad = (float)0.7;
	int tempSize = (int)(cacheSize / tempLoad);
	if ((tempSize % 2) == 0) {
		tempSize++;
	}
	if (cacheSize != (int)(tempSize * tempLoad)) {
		tempLoad = ((float) cacheSize) / ((float) tempSize);
	}
	fCache = new Hashtable(tempSize, tempLoad);
	this.fLRU = null;
	this.fMRU = null;
}
/**
 * Method for retrieving a <code>Page</code> from the <code>PageCache</code>.
 * 
 *
 * @return 
 * @param pageNumber int  the specification for which <code>Page</code> to be returned.
 */
public Page get(int pageNumber) {
	if (pageNumber < 0) {
		throw new IllegalArgumentException();
	}
	PageCacheElement temp = (PageCacheElement)fCache.get(new Integer(pageNumber));
	if (temp != null) {
		if (temp != fMRU) {
			insert(remove(temp));
		}
		return temp.getPage();
	}
	return null;
}
/**
 * Insert the method's description here.
 *
 * @return int
 */
public int getCount() {
	return fCache.size();
}
/**
 * Insert the method's description here.
 *
 * @return int[]
 */
public int[] getPageNumbers() {
	int[] temp = new int[fCache.size()];
	int i = 0;
	for (PageCacheElement pce = fLRU; pce != null; pce = pce.getPrevious()) {
		temp[i++] = pce.getPage().getPageNumber();
	}
	return temp;
}
/**
 * Inserts a <code>PageCacheElement</code> into the queue for least recently used.
 *
 * @param c 
 */
protected void insert(PageCacheElement c) {
	if (fMRU == null) {
		fLRU = c;
	} else {
		fMRU.setPrevious(c);
		c.setNext(fMRU);
	}
	fMRU = c;
}
/**
 * Method to insert a <code>Page</code> into the <code>PageCache</code>.
 *
 *
 * @param page the <code>Page</code> to be inserted.
 */
public void put(Page page) {
	if (fCacheSize > 0) {
		if (page == null) {
			throw new NullPointerException();
		}
		boolean isDuplicate = (fCache.containsKey(new Integer(page.getPageNumber()))), isFull = fCache.size() == fCacheSize;
		PageCacheElement temp = new PageCacheElement(page, null, null), go;
		if (isDuplicate) {
			go = (PageCacheElement)fCache.remove(new Integer(page.getPageNumber()));
			remove(go);
		} else if (isFull) {
			go = (PageCacheElement)fCache.remove(new Integer(fLRU.getPage().getPageNumber()));
			remove(go);
		}
		insert(temp);
		fCache.put(new Integer(page.getPageNumber()), temp);		
	}
}
/**
 * Removes a <code>PageCacheElement</code> from the queue for least recently used.
 *
 * @param c 
 */
protected PageCacheElement remove(PageCacheElement c) {
	if (c != fLRU) {
		c.getNext().setPrevious(c.getPrevious());
	} else {
		fLRU = fLRU.getPrevious();
	}
	if (c != fMRU) {
		c.getPrevious().setNext(c.getNext());
	} else {
		fMRU = fMRU.getNext();
	}
	c.setNext(null);
	c.setPrevious(null);
	return c;
}
/**
 * Removes the least recently used element from the queue.
 *
 */
protected PageCacheElement removeLast() {
	return remove(fLRU);
}
/**
 * Truncates the <code>PageCache</code> to the cutoff.  This does not
 * change the size of the cache, it simply removes the least recently
 * used <code>Page</code>'s one at a time until there are as many
 * in the cache as the cutoff. (useful for <code>PageCache</code>'s with
 * an open ended size)
 *
 *
 * @param cutoff int
 */
public void truncate(int cutoff) {
	if (cutoff < fCache.size()) {
		for (int i = cutoff, size = fCache.size(); i < size; i++) {
			fCache.remove(new Integer(fLRU.getPage().getPageNumber()));
			removeLast();
		}
	}
}
}
