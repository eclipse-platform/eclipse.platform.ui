package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IUpdateModelChangedListener {
	public void objectsAdded(Object parent, Object [] children);
	public void objectsRemoved(Object parent, Object [] children);
	public void objectChanged(Object object, String property);
}