package org.eclipse.update.ui.model;

public interface IUpdateModelChangedListener {
	public void objectAdded(Object parent, Object child);
	public void objectRemoved(Object parent, Object child);
	public void objectChanged(Object object);
}