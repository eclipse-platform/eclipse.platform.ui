package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * An IFactory should implement a create(...) method with parameters appropriate
 * to the context in which it is used.
 */

public interface IPageFactory {
	
	public Page create(PageStore store, int pageNumber);
}
