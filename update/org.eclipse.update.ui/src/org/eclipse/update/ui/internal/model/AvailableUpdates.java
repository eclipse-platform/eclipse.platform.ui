package org.eclipse.update.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;

public class AvailableUpdates extends ModelObject implements IWorkbenchAdapter {
	private Vector updates = new Vector();
	public AvailableUpdates() {
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	public String getName() {
		return "Available Updates";
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return updates.toArray();
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return UpdateUIPluginImages.DESC_UPDATES_OBJ;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return null;
	}
}