package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An IFactory should implement a create(...) method with parameters appropriate
 * to the context in which it is used.
 */

public interface IPageFactory {
	
	public Page create(PageStore store, int pageNumber);
}
