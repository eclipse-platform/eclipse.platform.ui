/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import java.io.*;

import org.eclipse.jface.resource.*;
import org.eclipse.ui.*;
import org.eclipse.ui.model.*;

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
