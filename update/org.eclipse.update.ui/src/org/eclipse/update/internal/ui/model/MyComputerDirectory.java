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
import java.net.*;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;
import org.eclipse.ui.model.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;

public class MyComputerDirectory
	extends UIModelObject
	implements IWorkbenchAdapter {
	private UIModelObject parent;
	private File file;
	private IVolume volume;
	Object[] children;

	public MyComputerDirectory(
		UIModelObject parent,
		File file,
		IVolume volume) {
		this.parent = parent;
		this.file = file;
		this.volume = volume;
	}

	public MyComputerDirectory(UIModelObject parent, File file) {
		this(parent, file, null);
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public IVolume getVolume() {
		return volume;
	}
	
	public String getName() {
		return file.getPath();
	}

	public File getFile() {
		return file;
	}

	public String toString() {
		return getName();
	}

	public boolean hasChildren(Object parent) {
		if (file.isDirectory()) {
			final boolean[] result = new boolean[1];
			BusyIndicator
				.showWhile(
					UpdateUI.getActiveWorkbenchShell().getDisplay(),
					new Runnable() {
				public void run() {
					File[] children = file.listFiles();
					result[0] = children != null && children.length > 0;
				}
			});
			return result[0];
		}
		return false;
	}

	public Object[] getChildren(
		Object parent,
		final boolean showExtensionRoots,
		final boolean showSites) {
		BusyIndicator
			.showWhile(UpdateUI.getActiveWorkbenchShell().getDisplay(), new Runnable() {
			public void run() {
				File[] files = file.listFiles();
				if (files == null) {
					children = new Object[0];
					return;
				}

				if (volume != null && showSites) {
					// This is volume.
					// Test if the volume itself is a site
					SiteBookmark rootSite = createSite(getFile(), true);
					if (rootSite != null) {
						children = new Object[1];
						children[0] = rootSite;
						return;
					}
				}

				children = new Object[files.length];
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (!file.isDirectory()) {
						children[i] = new MyComputerFile(MyComputerDirectory.this, file);
					} else {
						children[i] =
							new MyComputerDirectory(MyComputerDirectory.this, file);
						if (showSites) {
							SiteBookmark site = createSite(file, false);
							if (site != null)
								children[i] = site;
						}
						if (showExtensionRoots && ExtensionRoot.isExtensionRoot(file)) {
							children[i] =
								new ExtensionRoot(MyComputerDirectory.this, file);
						}
					}
				}
			}
		});
		return children;
	}
	
	public Object[] getChildren(Object parent) {
		return getChildren(parent, true, true);
	}

	static SiteBookmark createSite(File file, boolean root) {
		try {
			File siteXML = new File(file, "site.xml"); //$NON-NLS-1$
			if (siteXML.exists() == false)
				return null;
			URL url =
				new URL("file:" + file.getAbsolutePath() + File.separator); //$NON-NLS-1$
			String siteName = root?file.getAbsolutePath():file.getName();
			SiteBookmark site = new SiteBookmark(siteName, url, false);
			site.setLocal(true);
			return site;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
			ISharedImages.IMG_OBJ_FOLDER);
	}

	public Image getImage(Object obj) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(
			ISharedImages.IMG_OBJ_FOLDER);
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return file.getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return parent;
	}
}
