package org.eclipse.core.internal.dtree;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
/**
 * The result of doing a lookup() in a data tree.
 */
public class DataTreeLookup {
	public IPath key;
	public boolean isPresent;
	public Object data;
	public boolean foundInFirstDelta;
public DataTreeLookup(IPath nodeKey, boolean isPresent, Object data) {
	this.key = nodeKey;
	this.isPresent = isPresent;
	this.data = data;
}
public DataTreeLookup(IPath nodeKey, boolean isPresent, Object data, boolean foundInFirstDelta) {
	this.key = nodeKey;
	this.isPresent = isPresent;
	this.data = data;
	this.foundInFirstDelta = foundInFirstDelta;
}
}
