/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;


import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.*;
import org.eclipse.team.internal.core.ExceptionCollector;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamUI;

public class CVSLightweightDecorator extends LabelProvider implements ILightweightLabelDecorator, IResourceStateChangeListener, IPropertyChangeListener {

	// Decorator id as defined in the decorator extension point
	public final static String ID = "org.eclipse.team.cvs.ui.decorator"; //$NON-NLS-1$
	
	// Images cached for better performance
	private static ImageDescriptor dirty;
	private static ImageDescriptor checkedIn;
	private static ImageDescriptor noRemoteDir;
	private static ImageDescriptor added;
	private static ImageDescriptor merged;
	private static ImageDescriptor newResource;
	private static ImageDescriptor edited;

	private static ExceptionCollector exceptions = new ExceptionCollector(Policy.bind("CVSDecorator.exceptionMessage"), CVSUIPlugin.ID, IStatus.ERROR, CVSUIPlugin.getPlugin().getLog()); //$NON-NLS-1$;
	
	private static String DECORATOR_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$
	private static SimpleDateFormat decorateFormatter = new SimpleDateFormat(DECORATOR_FORMAT, Locale.getDefault());

	/*
	 * Define a cached image descriptor which only creates the image data once
	 */
	public static class CachedImageDescriptor extends ImageDescriptor {
		ImageDescriptor descriptor;
		ImageData data;
		public CachedImageDescriptor(ImageDescriptor descriptor) {
			Assert.isNotNull(descriptor);
			this.descriptor = descriptor;
		}
		public ImageData getImageData() {
			if (data == null) {
				data = descriptor.getImageData();
			}
			return data;
		}
	}

	/**
	 * TODO: We should not be implementing IDecoration as it is
	 * not speced to be implemented by clients (see bug 72677)
	 */
	public static class Decoration implements IDecoration {
		public String prefix, suffix;
		public ImageDescriptor overlay;
		
		/**
		 * @see org.eclipse.jface.viewers.IDecoration#addPrefix(java.lang.String)
		 */
		public void addPrefix(String prefix) {
			this.prefix = prefix;
		}
		/**
		 * @see org.eclipse.jface.viewers.IDecoration#addSuffix(java.lang.String)
		 */
		public void addSuffix(String suffix) {
			this.suffix = suffix;
		}
		/**
		 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
		 */
		public void addOverlay(ImageDescriptor overlay) {
			this.overlay = overlay;
		}
		/** (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor, int)
		 */
		public void addOverlay(ImageDescriptor overlay, int quadrant) {
		}
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#setForegroundColor(org.eclipse.swt.graphics.Color)
         */
        public void setForegroundColor(Color color) {
            // Ignore for now
            
        }
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#setBackgroundColor(org.eclipse.swt.graphics.Color)
         */
        public void setBackgroundColor(Color color) {
            // Ignore for now
            
        }
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IDecoration#setFont(org.eclipse.swt.graphics.Font)
         */
        public void setFont(Font color) {
            // Ignore for now
        }
	}
	
	static {
		dirty = new CachedImageDescriptor(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DIRTY_OVR));
		checkedIn = new CachedImageDescriptor(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
		added = new CachedImageDescriptor(TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
		merged = new CachedImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_MERGED));
		newResource = new CachedImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_QUESTIONABLE));
		edited = new CachedImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_EDITED));
		noRemoteDir = new CachedImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_NO_REMOTEDIR));
	}

	public CVSLightweightDecorator() {
		ResourceStateChangeListeners.getListener().addResourceStateChangeListener(this);
		TeamUI.addPropertyChangeListener(this);
		CVSUIPlugin.addPropertyChangeListener(this);
		CVSProviderPlugin.broadcastDecoratorEnablementChanged(true /* enabled */);
	}

	public static boolean isDirty(final ICVSResource cvsResource) throws CVSException {
		return !cvsResource.isIgnored() && cvsResource.isModified(null);
	}

	public static boolean isDirty(IResource resource) {

		// No need to decorate non-existant resources
		if (!resource.exists()) return false;

		try {
			return isDirty(CVSWorkspaceRoot.getCVSResourceFor(resource));
		} catch (CVSException e) {
			//if we get an error report it to the log but assume dirty.
			boolean accessible = resource.getProject().isAccessible();
			if (accessible) {
				// We only care about the failure if the project is open
				handleException(e);
			}
			// Return dirty if the project is open and clean otherwise
			return accessible;
		}

	}
	
	/*
	 * Answers null if a provider does not exist or the provider is not a CVS provider. These resources
	 * will be ignored by the decorator.
	 */
	private CVSTeamProvider getCVSProviderFor(IResource resource) {
		RepositoryProvider p =
			RepositoryProvider.getProvider(
				resource.getProject(),
				CVSProviderPlugin.getTypeId());
		if (p == null) {
			return null;
		}
		return (CVSTeamProvider) p;
	}

	/**
	 * Returns the resource for the given input object, or
	 * null if there is no resource associated with it.
	 *
	 * @param object  the object to find the resource for
	 * @return the resource for the given object, or null
	 */
	private IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource) object;
		}
		if (object instanceof IAdaptable) {
			return (IResource) ((IAdaptable) object).getAdapter(
				IResource.class);
		}
		return null;
	}
	/**
	 * This method should only be called by the decorator thread.
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		
		IResource resource = getResource(element);
		if (resource == null || resource.getType() == IResource.ROOT)
			return;
			
		CVSTeamProvider cvsProvider = getCVSProviderFor(resource);
		if (cvsProvider == null)
			return;


		// if the resource is ignored return an empty decoration. This will
		// force a decoration update event and clear the existing CVS decoration.
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		try {
			if (cvsResource.isIgnored()) {
				return;
			}
		} catch (CVSException e) {
			// The was an exception in isIgnored. Don't decorate
			//todo should log this error
			handleException(e);
			return;
		}

		// determine a if resource has outgoing changes (e.g. is dirty).
		boolean isDirty = false;
		boolean computeDeepDirtyCheck = isDeepDirtyCalculationEnabled();
		int type = resource.getType();
		if (type == IResource.FILE || computeDeepDirtyCheck) {
			isDirty = CVSLightweightDecorator.isDirty(resource);
		}
		
		decorateTextLabel(resource, decoration, isDirty, true, null);
		
		ImageDescriptor overlay = getOverlay(resource, isDirty, cvsProvider);
		if(overlay != null) { //actually sending null arg would work but this makes logic clearer
			decoration.addOverlay(overlay);
		}
	}

	private boolean isDeepDirtyCalculationEnabled() {
		IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		return store.getBoolean(ICVSUIConstants.PREF_CALCULATE_DIRTY);
	}

	//todo the showRevisions flag is temp, a better solution is DecoratorStrategy classes which have most the code below
	public static void decorateTextLabel(IResource resource, IDecoration decoration, boolean isDirty, boolean showRevisions, String overrideRevision) {
		try {
			Map bindings = new HashMap(3);
			String format = ""; //$NON-NLS-1$
			IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();

			// if the resource does not have a location then return. This can happen if the resource
			// has been deleted after we where asked to decorate it.
			if(!resource.isAccessible() || resource.getLocation() == null) {
				return;
			}

			int type = resource.getType();

			if (type == IResource.FOLDER) {
				format = store.getString(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION);
			} else if (type == IResource.PROJECT) {
				format = store.getString(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION);
			} else {
				format = store.getString(ICVSUIConstants.PREF_FILETEXT_DECORATION);
			}

			if (isDirty) {
				bindings.put(CVSDecoratorConfiguration.DIRTY_FLAG, store.getString(ICVSUIConstants.PREF_DIRTY_FLAG));
			}
			CVSTag tag = getTagToShow(resource);
			if (tag != null) {
				String name = tag.getName();
				if(tag.getType() == CVSTag.DATE){
					Date date = tag.asDate();
					if(date != null){
						name = decorateFormatter.format(date);
					}
				}
				bindings.put(CVSDecoratorConfiguration.RESOURCE_TAG, name);
			}

			if (type != IResource.FILE) {
				ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor((IContainer) resource);
				FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
				if (folderInfo != null) {
					ICVSRepositoryLocation location = KnownRepositories.getInstance().getRepository(folderInfo.getRoot());
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
					if (fileInfo.isAdded()) {
						bindings.put(CVSDecoratorConfiguration.ADDED_FLAG, store.getString(ICVSUIConstants.PREF_ADDED_FLAG));
					} else {
						if(showRevisions) {
							if(overrideRevision != null)
								bindings.put(CVSDecoratorConfiguration.FILE_REVISION, overrideRevision);
							else 
								bindings.put(CVSDecoratorConfiguration.FILE_REVISION, fileInfo.getRevision());
						}
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
		
		CVSDecoratorConfiguration.decorate(decoration, format, bindings);
			
		} catch (CVSException e) {
			handleException(e);
			return;
		}
	}

	/**
	 * Only show the tag if the resources tag is different than the parents. Or else, tag
	 * names will clutter the text decorations.
	 */
	protected static CVSTag getTagToShow(IResource resource) throws CVSException {
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		CVSTag tag = null;

		// for unmanaged resources don't show a tag since they will be added in
		// the context of their parents tag. For managed resources only show tags
		// if different than parent.
		boolean managed = false;

		if(cvsResource.isFolder()) {
			FolderSyncInfo folderInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
			if(folderInfo != null) {
				tag = folderInfo.getTag();
				managed = true;
			}
		} else {
			ResourceSyncInfo info = ((ICVSFile)cvsResource).getSyncInfo();
			if(info != null) {
				tag = info.getTag();
				managed = true;
			}
		}

		ICVSFolder parent = cvsResource.getParent();
		if(parent != null && managed) {
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
	
	/* Determine and return the overlay icon to use.
	 * We only get to use one, so if many are applicable at once we chose the
	 * one we think is the most important to show.
	 * Return null if no overlay is to be used.
	 */	
	public static ImageDescriptor getOverlay(IResource resource, boolean isDirty, CVSTeamProvider provider) {

		// for efficiency don't look up a pref until its needed
		IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		boolean showNewResources = store.getBoolean(ICVSUIConstants.PREF_SHOW_NEWRESOURCE_DECORATION);

		// show newResource icon
		if (showNewResources) {
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			try {
				if (cvsResource.exists()) {
					boolean isNewResource = false;
					if (cvsResource.isFolder()) {
						if (!((ICVSFolder)cvsResource).isCVSFolder()) {
							isNewResource = true;
						}
					} else if (!cvsResource.isManaged()) {
						isNewResource = true;
					}
					if (isNewResource) {
						return newResource;
					}
				}
			} catch (CVSException e) {
				handleException(e);
				return null;
			}
		}
		
		boolean showDirty = store.getBoolean(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION);

		// show dirty icon
		if(showDirty && isDirty) {
			 return dirty;
		}
				
		boolean showAdded = store.getBoolean(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION);

		if (showAdded && resource.getType() == IResource.FILE) {
			try {
				if (resource.getLocation() != null) {
					ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile) resource);
					ResourceSyncInfo info = cvsFile.getSyncInfo();
					// show merged icon if file has been merged but has not been edited (e.g. on commit it will be ignored)
					if (info != null && info.isNeedsMerge(cvsFile.getTimeStamp())) {
						return merged;
					// show added icon if file has been added locally.
					} else if (info != null && info.isAdded()) {
						// todo
						return added;
					}
				}
			} catch (CVSException e) {
				handleException(e);
				return null;
			}
		}

		// if watch/edit is enabled, show non-read-only files as being edited
		boolean decorateEdited;
		try {
			decorateEdited = provider.isWatchEditEnabled();
		} catch (CVSException e1) {
			handleException(e1);
			decorateEdited = false;
		}
		
		if (decorateEdited && resource.getType() == IResource.FILE && !resource.isReadOnly() && CVSWorkspaceRoot.hasRemote(resource)) {
			return edited;
		}
		
		boolean showHasRemote = store.getBoolean(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION);
		
		// Simplest is that is has remote.
		if (showHasRemote && CVSWorkspaceRoot.hasRemote(resource)) {
			if (resource.getType() != IResource.FILE) {
				// check if the folder is local diectory with no remote
				ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)resource);
				try {
					FolderSyncInfo folderSyncInfo = cvsFolder.getFolderSyncInfo();
					if (folderSyncInfo != null && folderSyncInfo.isVirtualDirectory()) {
						return noRemoteDir;
					}
				} catch (CVSException e) {
					// log the exception and show the shared overlay
					handleException(e);
				}
			}
			return checkedIn;
		}

		//nothing matched
		return null;

	}

	/*
	 * Add resource and its parents to the List
	 */
	 
	private void addWithParents(IResource resource, Set resources) {
		IResource current = resource;

		while (current.getType() != IResource.ROOT) {
			resources.add(current);
			current = current.getParent();
		}
	}
	
	/*
	* Perform a blanket refresh of all CVS decorations
	*/
	public static void refresh() {
		CVSUIPlugin.getPlugin().getWorkbench().getDecoratorManager().update(CVSUIPlugin.DECORATOR_ID);
	}

	/*
	 * Update the decorators for every resource in project
	 */
	 
	public void refresh(IProject project) {
		final List resources = new ArrayList();
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					resources.add(resource);
					return true;
				}
			});
			postLabelEvent(new LabelProviderChangedEvent(this, resources.toArray()));
		} catch (CoreException e) {
			handleException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceSyncInfoChanged(org.eclipse.core.resources.IResource[])
	 */
	public void resourceSyncInfoChanged(IResource[] changedResources) {
		resourceStateChanged(changedResources);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#externalSyncInfoChange(org.eclipse.core.resources.IResource[])
	 */
	public void externalSyncInfoChange(IResource[] changedResources) {
		resourceStateChanged(changedResources);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceModificationStateChanged(org.eclipse.core.resources.IResource[])
	 */
	public void resourceModified(IResource[] changedResources) {
		resourceStateChanged(changedResources);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceStateChanged(org.eclipse.core.resources.IResource[])
	 */
	public void resourceStateChanged(IResource[] changedResources) {
		// add depth first so that update thread processes parents first.
		//System.out.println(">> State Change Event");
		Set resourcesToUpdate = new HashSet();

		boolean showingDeepDirtyIndicators = isDeepDirtyCalculationEnabled();

		for (int i = 0; i < changedResources.length; i++) {
			IResource resource = changedResources[i];

			if(showingDeepDirtyIndicators) {
				addWithParents(resource, resourcesToUpdate);
			} else {
				resourcesToUpdate.add(resource);
			}
		}

		postLabelEvent(new LabelProviderChangedEvent(this, resourcesToUpdate.toArray()));
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectConfigured(org.eclipse.core.resources.IProject)
	 */
	public void projectConfigured(IProject project) {
		refresh(project);
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
	 */
	public void projectDeconfigured(IProject project) {
		refresh(project);
	}

	/**
	 * Post the label event to the UI thread
	 *
	 * @param events  the events to post
	 */
	private void postLabelEvent(final LabelProviderChangedEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(event);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		CVSProviderPlugin.broadcastDecoratorEnablementChanged(false /* disabled */);
		TeamUI.removePropertyChangeListener(this);
		CVSUIPlugin.removePropertyChangeListener(this);
	}
	
	/**
	 * Handle exceptions that occur in the decorator. 
	 */
	private static void handleException(Exception e) {
		exceptions.handleException(e);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (isEventOfInterest(event)) {
		    refresh();
		}	
	}

    private boolean isEventOfInterest(PropertyChangeEvent event) {
        String prop = event.getProperty();
        return prop.equals(TeamUI.GLOBAL_IGNORES_CHANGED) 
        	|| prop.equals(TeamUI.GLOBAL_FILE_TYPES_CHANGED) 
        	|| prop.equals(CVSUIPlugin.P_DECORATORS_CHANGED);
    }
}
