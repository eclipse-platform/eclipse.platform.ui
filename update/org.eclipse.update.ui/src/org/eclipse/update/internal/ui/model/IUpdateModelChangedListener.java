package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IUpdateModelChangedListener {
	public void objectAdded(Object parent, Object child);
	public void objectRemoved(Object parent, Object child);
	public void objectChanged(Object object, String property);
}