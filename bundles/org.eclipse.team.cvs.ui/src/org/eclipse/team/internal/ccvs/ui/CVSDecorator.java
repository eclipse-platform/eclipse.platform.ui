package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.ITeamDecorator;
import org.eclipse.team.ui.TeamUIPlugin;

public class CVSDecorator implements ITeamDecorator {

	ImageDescriptor dirty;
	ImageDescriptor checkedIn;
	ImageDescriptor checkedOut;

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
					IManagedFolder project = Client.getManagedFolder(resource.getLocation().toFile());
					FolderProperties folderInfo = project.getFolderInfo();
					return Policy.bind("CVSDecorator.projectDecoration", text, folderInfo.getRoot());
				case IResource.FILE:
					IManagedResource file = Client.getManagedResource(resource.getLocation().toFile());
					FileProperties fileInfo =  file.getParent().getFile(resource.getName()).getFileInfo();
					String tag = "";
					if (file.showManaged()) {
						tag = fileInfo.getTag();
					} else {
						return text;
					}
					if (tag.equals("")) {
						return Policy.bind("CVSDecorator.fileDecorationNoTag", text, fileInfo.getVersion());
					} else {
						return Policy.bind("CVSDecorator.fileDecorationWithTag", new Object[] {text, tag, fileInfo.getVersion()});
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
			if(p.isDirty(resource)) {
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
}