package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.ITeamDecorator;
import org.eclipse.team.ui.TeamUIPlugin;

public class CVSDecorator implements ITeamDecorator {

	ImageDescriptor dirty;
	ImageDescriptor checkedIn;
	ImageDescriptor checkedOut;

	//a dummy core exception used to short-circuit a resource visit
	private static final CoreException DECORATOR_EXCEPTION = new CoreException(new Status(IStatus.OK, CVSProviderPlugin.ID, 1, "", null));


	/**
	 * Define a cached image descriptor which only creates the image data once
	 */
	class CachedImageDescriptor extends ImageDescriptor {
		ImageDescriptor descriptor;
		ImageData data;
		
		public CachedImageDescriptor(ImageDescriptor descriptor) {
			this.descriptor = descriptor;
		}
		public ImageData getImageData() {
			if (data == null) {
				data = descriptor.getImageData();
			}
			return data;
		}
	};
		
	List listeners = new ArrayList(3);
	private static final CoreException CORE_EXCEPTION = new CoreException(new Status(IStatus.OK, "id", 1, "", null));

	public CVSDecorator() {
		dirty = new CachedImageDescriptor(TeamUIPlugin.getPlugin().getImageDescriptor(ISharedImages.IMG_DIRTY_OVR));
		checkedIn = new CachedImageDescriptor(TeamUIPlugin.getPlugin().getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
		checkedOut = new CachedImageDescriptor(TeamUIPlugin.getPlugin().getImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR));
	}

	/*
	 * @see ITeamDecorator#getText(String, IResource)
	 */
	public String getText(String text, IResource resource) {
		ITeamProvider p = TeamPlugin.getManager().getProvider(resource);
		if (p == null) {
			return text;
		}
		
		try {	
			switch (resource.getType()) {
				case IResource.PROJECT:
					ICVSFolder folder = new LocalFolder(resource.getLocation().toFile());
					FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
					if(folderInfo!=null) {
						return Policy.bind("CVSDecorator.projectDecoration", text, folderInfo.getRoot());
					}
					break;
				case IResource.FILE:
					ICVSFile file = new LocalFile(resource.getLocation().toFile());
					ResourceSyncInfo fileInfo = file.getSyncInfo();
					if(fileInfo!=null) {
						CVSTag tag = fileInfo.getTag();
						if (tag == null || (tag!=null && tag.getType() == CVSTag.HEAD)) {
							return Policy.bind("CVSDecorator.fileDecorationNoTag", text, fileInfo.getRevision());
						} else {
							return Policy.bind("CVSDecorator.fileDecorationWithTag", new Object[] {text, tag.getName(), fileInfo.getRevision()});
						}
					}
			}	
		} catch (CVSException e) {
			return text;
		}
		return text;
	}

	/*
	 * @see ITeamDecorator#getImage(IResource)
	 */
	public ImageDescriptor[][] getImage(IResource resource) {
		List overlays = new ArrayList(5);
		CVSTeamProvider p = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource);
		if(p!=null) {
			if(isChildDirty(resource)) {
				overlays.add(dirty);
			}
			if(p.hasRemote(resource)) {
				overlays.add(checkedIn);
			} 
			if(p.isCheckedOut(resource)) {
				overlays.add(checkedOut);
			}
		}
		return new ImageDescriptor[][] {(ImageDescriptor[])overlays.toArray(new ImageDescriptor[overlays.size()])};
	}
	
	private boolean isChildDirty(IResource resource) {
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					
					// a project can't be dirty, continue with its children
					if(resource.getType() == IResource.PROJECT) {
						return true;
					}
										
					ICVSResource cvsResource;
					if(resource.getType() == IResource.FILE) {
						cvsResource = new LocalFile(resource.getLocation().toFile());
					} else {
						cvsResource = new LocalFolder(resource.getLocation().toFile());
					}
					
					try {
						if (!cvsResource.isManaged()) {
							if (cvsResource.isIgnored()) {
								return false;
							} else {
								// new resource, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
						if (!cvsResource.isFolder()) {
							if(((ICVSFile)cvsResource).isModified()) {
								// file has changed, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
					} catch(CVSException e) {
						return true;
					}
					// no change -- keep looking in children
					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			//if our exception was caught, we know there's a dirty child
			return e == DECORATOR_EXCEPTION;
		}
		return false;
	}
}