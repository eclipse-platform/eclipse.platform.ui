package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamUIPlugin;

/**
 * Performs the decoration calculation for elements made available via the decoration notifier.
 */
public class CVSDecorationRunnable implements Runnable {

	// Images cached for better performance
	private ImageDescriptor dirty;
	private ImageDescriptor checkedIn;
	private ImageDescriptor checkedOut;

	// Provides resources to be decorated and is notified when decoration has been calculated
	private IDecorationNotifier notifier;

	/*
	 * Define a cached image descriptor which only creates the image data once
	 */
	public class CachedImageDescriptor extends ImageDescriptor {
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
	}

	/* package */
	CVSDecorationRunnable(IDecorationNotifier notifier) {

		dirty = new CachedImageDescriptor(TeamUIPlugin.getPlugin().getImageDescriptor(ISharedImages.IMG_DIRTY_OVR));
		checkedIn = new CachedImageDescriptor(TeamUIPlugin.getPlugin().getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
		checkedOut = new CachedImageDescriptor(TeamUIPlugin.getPlugin().getImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR));

		this.notifier = notifier;
	}

	/*
	 * @see Runnable#run()
	 */
	public void run() {
		while (true) {
			// will block if there are no resources to be decorated
			IResource resource = notifier.next();

			System.out.println("Decorating: " + resource.getFullPath());

			// it is possible that the resource to be decorated is no longer associated
			// with a CVS provider. This could happen if the team nature was removed
			// between the time the decoration event was posted to the thread and the time
			// the thread processes the decoration.
			ITeamProvider provider = TeamPlugin.getManager().getProvider(resource);
			if(provider==null || !(provider instanceof CVSTeamProvider)) {
				continue;
			}

			// determine a if resource has outgoing changes (e.g. is dirty).
			IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
			boolean isDirty = false;
			boolean computeDeepDirtyCheck = store.getBoolean(ICVSUIConstants.PREF_CALCULATE_DIRTY);
			int type = resource.getType();
			if(type == IResource.FILE || computeDeepDirtyCheck) {
				isDirty = isDirty(resource);
			}
			
			// compute decorations						
			CVSDecoration decoration = computeTextLabelFor(resource, isDirty, provider);
			decoration.setOverlays(computeLabelOverlaysFor(resource, isDirty, provider));
			
			// notify that decoration is ready
			notifier.decorated(resource, decoration);
		}
	}

	private CVSDecoration computeTextLabelFor(IResource resource, boolean isDirty, ITeamProvider provider) {
		Map bindings = new HashMap(3);
		String format;
		IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		int type = resource.getType();
		if(type==IResource.FOLDER) {
			format = store.getString(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION);
		} else if(type==IResource.PROJECT) {
			format = store.getString(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION);
		} else {
			format = store.getString(ICVSUIConstants.PREF_FILETEXT_DECORATION);
		}
		
		if(isDirty) {
			bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, store.getString(ICVSUIConstants.PREF_DIRTY_FLAG));
		}
			
		try {
			switch (type) {
				case IResource.FOLDER : 
				case IResource.PROJECT :
					ICVSFolder folder = new LocalFolder(resource.getLocation().toFile());
					FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
					if (folderInfo != null) {
						CVSTag tag = folderInfo.getTag();
						if(tag!=null) {
							bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, tag.getName());
						}
						ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(folderInfo.getRoot());
						bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_HOST, location.getHost());
						bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_METHOD, location.getMethod().getName());
						bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_USER, location.getUsername());
						bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_ROOT, location.getRootDirectory());
						bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_REPOSITORY, folderInfo.getRepository());
					}
					break;
				case IResource.FILE :
					format = store.getString(ICVSUIConstants.PREF_FILETEXT_DECORATION);
					ICVSFile file = new LocalFile(resource.getLocation().toFile());
					ResourceSyncInfo fileInfo = file.getSyncInfo();
					if (fileInfo != null) {
						CVSTag tag = fileInfo.getTag();
						if(fileInfo.isAdded()) {
							bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, store.getString(ICVSUIConstants.PREF_ADDED_FLAG));
						} else {
							bindings.put(CVSDecoratorConfiguration.FILE_REVISION, fileInfo.getRevision());
						}
						bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD, fileInfo.getKeywordMode());
						if (tag != null && (tag.getType() != CVSTag.HEAD)) {
							bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, tag.getName());
						}
					}
					break;
			}			
			return new CVSDecoration(format, bindings, null);
		} catch (CVSException e) {
			return new CVSDecoration();
		}
	}

	private List computeLabelOverlaysFor(IResource resource, boolean isDirty, ITeamProvider provider) {
		List overlays = new ArrayList(3);
		
		IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		boolean showDirty = store.getBoolean(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION);
		boolean showHasRemote = store.getBoolean(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION);
		boolean showAdded = store.getBoolean(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION);
		
		if(showDirty && isDirty) {
				overlays.add(dirty);
		}
		
		if (showHasRemote && provider.hasRemote(resource)) {
			overlays.add(checkedIn);
		}
		if (showAdded && resource.getType() == IResource.FILE && provider.isCheckedOut(resource)) {
			overlays.add(checkedOut);
		}
		if(overlays.isEmpty()) {
			return null;
		} else {		
			return overlays;
		}
	}

	private boolean isDirty(IResource resource) {
		final CoreException DECORATOR_EXCEPTION = new CoreException(new Status(IStatus.OK, "id", 1, "", null));
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {

					// a project can't be dirty, continue with its children
					if (resource.getType() == IResource.PROJECT) {
						return true;
					}

					ICVSResource cvsResource;
					if (resource.getType() == IResource.FILE) {
						cvsResource = new LocalFile(resource.getLocation().toFile());
					} else {
						cvsResource = new LocalFolder(resource.getLocation().toFile());
					}

					try {
						if (!cvsResource.isManaged()) {
							if (cvsResource.isIgnored() || cvsResource.getParent().isIgnored()) {
								return false;
							} else {
								// new resource, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
						if (!cvsResource.isFolder()) {
							if (((ICVSFile) cvsResource).isModified()) {
								// file has changed, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
					} catch (CVSException e) {
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