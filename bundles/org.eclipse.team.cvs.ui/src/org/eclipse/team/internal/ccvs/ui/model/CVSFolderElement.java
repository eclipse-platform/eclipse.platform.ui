package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CVSFolderElement extends CVSResourceElement {
	
	private ICVSFolder folder;
	private boolean includeUnmanaged;
	
	public CVSFolderElement(ICVSFolder folder, boolean includeUnmanaged) {
		this.folder = folder;
		this.includeUnmanaged = includeUnmanaged;
	}
	
	/**
	 * Returns CVSResourceElement instances
	 */
	public Object[] getChildren(final Object o) {
		final Object[][] result = new Object[1][];
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					ICVSResource[] children = folder.fetchChildren(null);
					CVSResourceElement[] elements = new CVSResourceElement[children.length];
					for (int i = 0; i < children.length; i++) {
						ICVSResource resource = children[i];
						if(resource.isFolder()) {
							elements[i] = new CVSFolderElement((ICVSFolder)resource, includeUnmanaged);
						} else {
							elements[i] = new CVSFileElement((ICVSFile)resource);
						}
					}
					result[0] = elements;
				} catch (TeamException e) {
					handle(e);
				}
			}
		});
		return result[0];
	}
	/**
	 * Overridden to append the version name to remote folders which
	 * have version tags and are top-level folders.
	 */
	public String getLabel(Object o) {
		return folder.getName();
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}	
	
	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object o) {
		return new CVSFolderElement(folder.getParent(), includeUnmanaged);
	}
	
	/**
	 * @see CVSResourceElement#getCVSResource()
	 */
	public ICVSResource getCVSResource() {
		return folder ;
	}
}
