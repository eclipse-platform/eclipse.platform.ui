package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamUIPlugin;

/**
 * Performs the decoration calculation for elements made available via the decoration notifier.
 */
public class CVSDecorationRunnable implements Runnable {

	// Images cached for better performance
	private static ImageDescriptor dirty;
	private static ImageDescriptor checkedIn;
	private static ImageDescriptor checkedOut;
	private static ImageDescriptor merged;

	// Provides resources to be decorated and is notified when decoration has been calculated
	private IDecorationNotifier notifier;

	// Remember the non posted decorated resources
	List resources = new ArrayList();
	List decorations = new ArrayList();
	private final static int NUM_TO_BATCH = 50;

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
		merged = new CachedImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_MERGED));
		this.notifier = notifier;
	}

	/*
	 * @see Runnable#run()
	 */
	public void run() {
		while (true) {
			// will block if there are no resources to be decorated
			IResource resource = notifier.next();
			
			// if next() returned null, we are done and should shut down.
			if (resource == null) {
				return;
			}
			
			CVSDecoration decoration = decorate(resource);
			
			// notify that decoration is ready
			if(decoration!=null) {
				resources.add(resource);
				decorations.add(decoration);
				if(!resources.isEmpty() && (notifier.remaining()==0 || resources.size() >= NUM_TO_BATCH)) {
					notifier.decorated((IResource[])resources.toArray(new IResource[resources.size()]), 
									   (CVSDecoration[])decorations.toArray(new CVSDecoration[decorations.size()]));
					resources.clear();
					decorations.clear();
				}
			}
		}
	}

	public CVSDecoration decorate(IResource resource) {
		// it is possible that the resource to be decorated is no longer associated
		// with a CVS provider. This could happen if the team nature was removed
		// between the time the decoration event was posted to the thread and the time
		// the thread processes the decoration.
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
		if(!resource.exists() || provider==null) {
			return null;
		}
		
		// if the resource is ignored return an empty decoration. This will 
		// force a decoration update event and clear the existing CVS decoration.
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if(cvsResource.isIgnored()) {
			return new CVSDecoration();
		}
			
		// determine a if resource has outgoing changes (e.g. is dirty).
		IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		boolean isDirty = false;
		boolean computeDeepDirtyCheck = store.getBoolean(ICVSUIConstants.PREF_CALCULATE_DIRTY);
		int type = resource.getType();
		if(type == IResource.FILE || computeDeepDirtyCheck) {
			isDirty = CVSDecorator.isDirty(resource);
		}

		// compute decorations						
		CVSDecoration decoration = computeTextLabelFor(resource, isDirty);
		decoration.setOverlays(computeLabelOverlaysFor(resource, isDirty, (CVSTeamProvider)provider));
		return decoration;
	}
	
	public static CVSDecoration computeTextLabelFor(IResource resource, boolean isDirty) {
		try {
			Map bindings = new HashMap(3);
			String format = ""; //$NON-NLS-1$
			IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
			
			IPath resourceLocation = resource.getLocation();
			int type = resource.getType();
			
			// if the resource does not have a location then return. This can happen if the resource
			// has been deleted after we where asked to decorate it.
			if(resourceLocation==null) {
				return new CVSDecoration(format, bindings, null);
			}
			
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
	
			CVSTag tag = getTagToShow(resource);
			if(tag != null) {
				bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, tag.getName());
			}
			
			if(type != IResource.FILE) {
				ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor((IContainer) resource);
				FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
				if (folderInfo != null) {
					ICVSRepositoryLocation location = CVSProviderPlugin.getProvider().getRepository(folderInfo.getRoot());
					bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_HOST, location.getHost());
					bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_METHOD, location.getMethod().getName());
					bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_USER, location.getUsername());
					bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_ROOT, location.getRootDirectory());
					bindings.put(CVSDecoratorConfiguration.REMOTELOCATION_REPOSITORY, folderInfo.getRepository());
				}
			} else {
				format = store.getString(ICVSUIConstants.PREF_FILETEXT_DECORATION);
				ICVSFile file = CVSWorkspaceRoot.getCVSFileFor((IFile) resource);
				ResourceSyncInfo fileInfo = file.getSyncInfo();
				if (fileInfo != null) {
					if(fileInfo.isAdded()) {
						bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, store.getString(ICVSUIConstants.PREF_ADDED_FLAG));
					} else {
						bindings.put(CVSDecoratorConfiguration.FILE_REVISION, fileInfo.getRevision());
					}
					KSubstOption option = fileInfo.getKeywordMode() != null ?
						fileInfo.getKeywordMode() :
						KSubstOption.fromFile((IFile) resource);
					bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD, option.getShortDisplayText());
				} else {
					// only show the type that cvs will use when comitting the file
					KSubstOption option = KSubstOption.fromFile((IFile) resource);
					bindings.put(CVSDecoratorConfiguration.FILE_KEYWORD, option.getShortDisplayText());
				}
			}			
			return new CVSDecoration(format, bindings, null);
		} catch (CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return new CVSDecoration();
		}
	}
	
	/**
	 * Only show the tag if the resources tag is different than the parents. Or else, tag
	 * names will clutter the text decorations.
	 */
	protected static CVSTag getTagToShow(IResource resource) throws CVSException {
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		CVSTag tag = null;
		if(cvsResource.isFolder()) {
			FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
			if(folderInfo != null) {
				tag = folderInfo.getTag();
			}  
		} else {
			ResourceSyncInfo info = ((ICVSFile)cvsResource).getSyncInfo();
			if(info != null) {
				tag = info.getTag();
			}
		}
		
		ICVSFolder parent = cvsResource.getParent();
		if(parent != null && tag != null) {
			FolderSyncInfo parentInfo = parent.getFolderSyncInfo();
			if(parentInfo != null) {												
				CVSTag parentTag = parentInfo.getTag();
				parentTag = (parentTag == null ? CVSTag.DEFAULT : parentTag);
				tag = (tag == null ? CVSTag.DEFAULT : tag);
				// must compare tags by name because CVS doesn't do a good job of
				// using  T and N prefixes for folders and files. 
				if( parentTag.getName().equals(tag.getName())) {
					tag = null;
				}
			}
		}
		return tag;
	}
		
	public static List computeLabelOverlaysFor(IResource resource, boolean isDirty, CVSTeamProvider provider) {
		List overlays = new ArrayList(3);
		
		IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		boolean showDirty = store.getBoolean(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION);
		boolean showHasRemote = store.getBoolean(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION);
		boolean showAdded = store.getBoolean(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION);
		
		if (showAdded && resource.getType() == IResource.FILE) {
			try {
				IPath location = resource.getLocation();
				if(location!=null) {
					ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile) resource);
					ResourceSyncInfo info = cvsFile.getSyncInfo();
					// show merged icon if file has been merged but has not been edited (e.g. on commit it will be ignored)
					if(info!=null && info.isNeedsMerge(cvsFile.getTimeStamp())) {
						overlays.add(merged);
					// show added icon if file has been added locally.
					} else if(info!=null && info.isAdded()) {
						overlays.add(checkedOut);
					}					
				}
			} catch (CVSException e) {
				CVSUIPlugin.log(e.getStatus());
				return null;				
			}
		}
		
		// show outgoing arrow
		if(showDirty && isDirty) {
				overlays.add(dirty);
		}
		
		// show remote icon
		if (showHasRemote && provider.hasRemote(resource)) {
			overlays.add(checkedIn);
		}
				
		if(overlays.isEmpty()) {
			return null;
		} else {		
			return overlays;
		}
	}
}