package org.eclipse.update.internal.ui.model;
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
import org.eclipse.update.internal.ui.*;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.swt.graphics.Image;

public class ExtensionRoot extends UIModelObject implements IWorkbenchAdapter {
	private UIModelObject parent;
	private File root;

	public ExtensionRoot(UIModelObject parent, File root) {
		this.parent = parent;
		this.root = root;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	public String getName() {
		return root.getName();
	}
	
	public String toString() {
		return getName();
	}
	
	public File getInstallableDirectory() {
		return root;
	}
	
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	
	public Object[] getChildren(Object parent) {
		return new Object[0];
	}

	public static boolean isExtensionRoot(File directory) {
		File marker = new File(directory, ".eclipseextension");
		if (!marker.exists() || marker.isDirectory()) return false;
		return true;
	}
	
	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(getName());
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
		return parent;
	}
}