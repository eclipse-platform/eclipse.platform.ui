package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
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
	public Object[] getChildren(Object o) {
		final Object[][] result = new Object[1][];
		try {
			CVSUIPlugin.runWithProgress(null, true /*cancelable*/, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						ICVSResource[] children = folder.fetchChildren(monitor);
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
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			return new Object[0];
		} catch (InvocationTargetException e) {
			handle(e.getTargetException());
			return new Object[0];
		}
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
