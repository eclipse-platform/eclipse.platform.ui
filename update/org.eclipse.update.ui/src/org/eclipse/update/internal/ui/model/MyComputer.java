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

import java.io.File;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.ISite;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.search.*;

public class MyComputer extends UIModelObject implements IWorkbenchAdapter {
	private static final String KEY_LABEL = "MyComputer.label";
	private Object[] children;
	public MyComputer() {
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public String getName() {
		return UpdateUI.getString(KEY_LABEL);
	}

	public String toString() {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		BusyIndicator
			.showWhile(
				UpdateUI.getActiveWorkbenchShell().getDisplay(),
				new Runnable() {
			public void run() {
				IVolume[] volumes = LocalSystemInfo.getVolumes();
				if (volumes != null && volumes.length > 0) {
					children = new MyComputerDirectory[volumes.length];
					for (int i = 0; i < children.length; i++) {
						children[i] =
							new MyComputerDirectory(
								MyComputer.this,
								volumes[i].getFile(),
								volumes[i]);
					}
				} else
					children = new Object[0];
			}
		});
		return children;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return UpdateUIImages.DESC_COMPUTER_OBJ;
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

	public void collectSites(
		Vector sites,
		MyComputerSearchSettings settings,
		IProgressMonitor monitor) {
		IVolume[] volumes = LocalSystemInfo.getVolumes();
		for (int i = 0; i < volumes.length; i++) {
			File drive = volumes[i].getFile();
			if (monitor.isCanceled())
				return;
			DriveSearchSettings ds = settings.getDriveSettings(drive.getPath());
			if (ds.isChecked()) {
				collectSites(drive, true, sites, ds, monitor);
			}
		}
	}

	private void collectSites(
		File dir,
		boolean root,
		Vector sites,
		DriveSearchSettings driveSettings,
		IProgressMonitor monitor) {
			
		if (root) {
			// See if the root itself is the site.
			SiteBookmark rootSite = MyComputerDirectory.createSite(dir, true);
			if (rootSite!=null) {
				sites.add(rootSite);
				return;
			}
		}

		File[] children = dir.listFiles();
		if (children == null)
			return;

		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (monitor.isCanceled())
				return;
			if (child.isDirectory()) {
				monitor.subTask(child.getPath());
				SiteBookmark bookmark = MyComputerDirectory.createSite(child, false);
				if (bookmark != null) {
					ISite site = bookmark.getSite(false, null);
					if (site != null)
						sites.add(bookmark);
				} else
					collectSites(child, false, sites, driveSettings, monitor);
			}
		}
	}
}
