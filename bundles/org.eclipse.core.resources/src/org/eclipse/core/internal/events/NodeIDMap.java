package org.eclipse.core.internal.events;

import org.eclipse.core.runtime.IPath;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * A specialized map that maps Node IDs to their old and new paths.
 * Used for calculating moves during resource change notification.
 */
public class NodeIDMap {
	protected static final int MINIMUM_SIZE = 10;
	protected int elementCount = 0;
	protected long[] ids;
	protected IPath[] oldPaths;
	protected IPath[] newPaths;
/**
 * Creates a new node ID map of default capacity.
 */
public NodeIDMap() {
	this(MINIMUM_SIZE);
}
/**
 * Creates a new node ID map with the given capacity.
 */
public NodeIDMap(int capacity) {
	super();
	int size = Math.max(MINIMUM_SIZE, capacity * 2);
	this.ids = new long[size];
	this.oldPaths = new IPath[size];
	this.newPaths = new IPath[size];
}
/**
 * Returns true if the given element is contained in the map,
 * and false otherwise.
 */
public boolean contains(long id) {
	return getIndex(id) >= 0;
}
/**
 * The array isn't large enough so double its size and rehash
 * all its current values.
 */
protected void expand() {
	int newLength = ids.length * 2;
	long[] grownIds = new long[newLength];
	IPath[] grownOldPaths = new IPath[newLength];
	IPath[] grownNewPaths = new IPath[newLength];
	int maxArrayIndex = newLength-1;
	for (int i = 0; i < ids.length; i++) {
		long id = ids[i];
		if (id != 0) {
			int hash = hashFor(id) % newLength;
			while (grownIds[hash] != 0) {
				hash++;
				if (hash > maxArrayIndex)
					hash = 0;
			}
			grownIds[hash] = id;
			grownOldPaths[hash] = oldPaths[i];
			grownNewPaths[hash] = newPaths[i];
		}
	}
	ids = grownIds;
	oldPaths = grownOldPaths;
	newPaths = grownNewPaths;
}
/**
 * Returns the index of the given element in the map.  If not
 * found, returns -1.
 */
private int getIndex(long searchID) {
	int hash = hashFor(searchID) % ids.length;

	// search the last half of the array
	for (int i = hash; i < ids.length; i++) {
		if (ids[i] == searchID)
			return i;
	}

	// search the beginning of the array
	for (int i = 0; i < hash - 1; i++) {
		if (ids[i] == searchID)
			return i;
	}

	// marker info not found so return null
	return -1;
}
/**
 * Returns the new path location for the given ID, or null
 * if no new path is available.
 */
public IPath getNewPath(long nodeID) {
	int index = getIndex(nodeID);
	if (index == -1)
		return null;
	return newPaths[index];
}
/**
 * Returns the old path location for the given ID, or null
 * if no old path is available.
 */
public IPath getOldPath(long nodeID) {
	int index = getIndex(nodeID);
	if (index == -1)
		return null;
	return oldPaths[index];
}
	
private int hashFor(long id) {
	return Math.abs((int) id);
}
/**
 * Returns true if there are no elements in the map, and
 * false otherwise.
 */
public boolean isEmpty() {
	return elementCount == 0;
}
/**
 * Adds the given path mappings to the map.  If either oldPath
 * or newPath is null, they are ignored (old map values are not overwritten).
 */
private void put(long id, IPath oldPath, IPath newPath) {
	if (oldPath == null && newPath == null)
		return;
	int hash = hashFor(id) % ids.length;

	// search for an empty slot at the end of the array
	for (int i = hash; i < ids.length; i++) {
		if (ids[i] == id) {
			//replace value for existing entry
			if (oldPath != null)
				oldPaths[i] = oldPath;
			if (newPath != null)
				newPaths[i] = newPath;
			return;
		}
		if (ids[i] == 0) {
			//add a new entry to the map
			ids[i] = id;
			if (oldPath != null)
				oldPaths[i] = oldPath;
			if (newPath != null)
				newPaths[i] = newPath;
			elementCount++;
			// grow if necessary
			if (shouldGrow())
				expand();
			return;
		}
	}

	// search for an empty slot at the beginning of the array
	for (int i = 0; i < hash - 1; i++) {
		if (ids[i] == id) {
			//replace value for existing entry
			if (oldPath != null)
				oldPaths[i] = oldPath;
			if (newPath != null)
				newPaths[i] = newPath;
			return;
		}
		if (ids[i] == 0) {
			//add a new entry to the map
			ids[i] = id;
			if (oldPath != null)
				oldPaths[i] = oldPath;
			if (newPath != null)
				newPaths[i] = newPath;
			elementCount++;
			// grow if necessary
			if (shouldGrow())
				expand();
			return;
		}
	}

	// if we didn't find a free slot, then try again with the expanded set
	expand();
	put(id, oldPath, newPath);
}
/**
 * Adds an entry for a node's old path
 */
public void putOldPath(long id, IPath path) {
	put(id, path, null);
}
/**
 * Adds an entry for a node's old path
 */
public void putNewPath(long id, IPath path) {
	put(id, null, path);
}
/**
 * The element at the given index has been removed so move
 * elements to keep the set properly hashed.
 */
protected void rehashTo(int anIndex) {

	int target = anIndex;
	int index = anIndex + 1;
	if (index >= ids.length)
		index = 0;
	long id = ids[index];
	IPath oldPath = oldPaths[index];
	IPath newPath = newPaths[index];
	while (id != 0) {
		int hashIndex = hashFor(id) % ids.length;
		boolean match;
		if (index < target)
			match = !(hashIndex > target || hashIndex <= index);
		else
			match = !(hashIndex > target && hashIndex <= index);
		if (match) {
			ids[target] = id;
			oldPaths[target] = oldPath;
			newPaths[target] = newPath;
			target = index;
		}
		index++;
		if (index >= ids.length)
			index = 0;
		id = ids[index];
		oldPath = oldPaths[index];
		newPath = newPaths[index];
	}
	ids[target] = 0;
	oldPaths[target] = null;
	newPaths[target] = null;
}
/**
 * Removes the entry from the map with the given node ID. Does
 * nothing if no such node exists in the map.
 */
public void remove(long idToRemove) {
	int indexToRemove = getIndex(idToRemove);
	if (indexToRemove < 0)
		return;
	rehashTo(indexToRemove);
	elementCount--;
}
private boolean shouldGrow() {
	return elementCount > ids.length * 0.75;
}
/**
 * Returns the number of elements currently stored in the map.
 */
public int size() {
	return elementCount;
}
}