package org.eclipse.core.internal.dtree;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
/**
 * The result of doing a lookup() in a data tree.  Uses an instance
 * pool that assumes no more than POOL_SIZE instance will ever be
 * needed concurrently.  Reclaims and reuses instances on an LRU basis.
 */
public class DataTreeLookup {
	public IPath key;
	public boolean isPresent;
	public Object data;
	public boolean foundInFirstDelta;
	private static final int POOL_SIZE = 100;
	/**
	 * The array of lookup instances available for use.
	 */
	private static DataTreeLookup[] instancePool;
	/**
	 * The index of the next available lookup instance.
	 */
	private static int nextFree = 0;
	static {
		instancePool = new DataTreeLookup[POOL_SIZE];
		//fill the pool with objects
		for (int i = 0; i < POOL_SIZE; i++) {
			instancePool[i] = new DataTreeLookup();
		}
	}
/**
 * Constructors for internal use only.  Use factory methods.
 */
private DataTreeLookup() {
}
/**
 * Factory method for creating a new lookup object.
 */
public static DataTreeLookup newLookup(IPath nodeKey, boolean isPresent, Object data) {
	DataTreeLookup instance;
	synchronized (instancePool) {
		instance = instancePool[nextFree];
		nextFree = ++nextFree % POOL_SIZE;
	}
	instance.key = nodeKey;
	instance.isPresent = isPresent;
	instance.data = data;
	instance.foundInFirstDelta = false;
	return instance;
}
/**
 * Factory method for creating a new lookup object.
 */
public static DataTreeLookup newLookup(IPath nodeKey, boolean isPresent, Object data, boolean foundInFirstDelta) {
	DataTreeLookup instance;
	synchronized (instancePool) {
		instance = instancePool[nextFree];
		nextFree = ++nextFree % POOL_SIZE;
	}
	instance.key = nodeKey;
	instance.isPresent = isPresent;
	instance.data = data;
	instance.foundInFirstDelta = foundInFirstDelta;
	return instance;
}
}