package org.eclipse.core.internal.dtree;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
