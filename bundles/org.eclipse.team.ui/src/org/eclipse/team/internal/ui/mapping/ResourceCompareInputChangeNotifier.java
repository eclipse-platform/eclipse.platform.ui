/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffChangeEvent;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;

/**
 * A change notifier for resource-based compare inputs.
 */
public class ResourceCompareInputChangeNotifier extends CompareInputChangeNotifier {

	static final String RESOURCE_CHANGE_NOTIFIER_PROPERTY = "org.eclipse.team.ui.ResourceChangeNotifier"; //$NON-NLS-1$

	private class CompareInputLabelProvider extends BaseLabelProvider implements ICompareInputLabelProvider {

		public Image getAncestorImage(Object input) {
			// No image desired
			return null;
		}

		public String getAncestorLabel(Object input) {
			if (input instanceof ResourceDiffCompareInput) {
				ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
				ITypedElement element = rdci.getAncestor();
				if (element != null) {
					final IFileRevision revision = ((FileRevisionTypedElement)element).getFileRevision();
					if (revision != null) {
						if (TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SHOW_AUTHOR_IN_COMPARE_EDITOR)) {
							String author = ((FileRevisionTypedElement)element).getAuthor();
							if (author != null) {
								return NLS.bind(TeamUIMessages.Utils_20, new String[] { revision.getContentIdentifier(), author });
							} else if (revision.isPropertyMissing()) {
								fetchAuthors(input);
							}
						}
						return NLS.bind(TeamUIMessages.SyncInfoCompareInput_baseLabelExists, new String[] { revision.getContentIdentifier() }); 
					} else {
						return TeamUIMessages.SyncInfoCompareInput_baseLabel; 
					}
				}
			}
			return null;
		}

		public Image getLeftImage(Object input) {
			// No image desired
			return null;
		}

		public String getLeftLabel(Object input) {
			if (input instanceof ResourceDiffCompareInput) {
				ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
				String localContentId = rdci.getLocalContentId();
				if (localContentId != null) {
					return NLS.bind(TeamUIMessages.SyncInfoCompareInput_localLabelExists, new String[] { localContentId }); 
				} else {
					return TeamUIMessages.SyncInfoCompareInput_localLabel; 
				}
			}
			return null;
		}

		public Image getRightImage(Object input) {
			// No image desired
			return null;
		}

		public String getRightLabel(Object input) {
			if (input instanceof ResourceDiffCompareInput) {
				ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
				ITypedElement element = rdci.getRight();
				if (element != null) {
					final IFileRevision revision = ((FileRevisionTypedElement)element).getFileRevision();
					if (revision != null) {
						if (TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SHOW_AUTHOR_IN_COMPARE_EDITOR)) {
							String author = ((FileRevisionTypedElement)element).getAuthor();
							if (author != null) {
								return NLS.bind(TeamUIMessages.Utils_21, new String[] { revision.getContentIdentifier(), author });
							} else if (revision.isPropertyMissing()) {
								fetchAuthors(input);
							}
						}
						return NLS.bind(TeamUIMessages.SyncInfoCompareInput_remoteLabelExists, new String[] { revision.getContentIdentifier() }); 
					} else {
						return TeamUIMessages.SyncInfoCompareInput_remoteLabel; 
					}
				}
			}
			return null;
		}

		public Image getImage(Object element) {
			if (element instanceof ICompareInput) {
				ICompareInput ci = (ICompareInput) element;
				return ci.getImage();
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof ICompareInput) {
				ICompareInput ci = (ICompareInput) element;
				return ci.getName();
			}
			return null;
		}
		
		public void fireChangeEvent(final Object element) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					fireLabelProviderChanged(new LabelProviderChangedEvent(CompareInputLabelProvider.this, element));
				}
			
			});
		}
	}
	
	/**
	 * Return a compare input change notifier that will detect changes in the synchronization context and
	 * translate them into compare input change events by calling {@link ResourceDiffCompareInput#update()}.
	 * @param context  the synchronization context
	 * @return a compare input change notifier
	 */
	public static ResourceCompareInputChangeNotifier getChangeNotifier(ISynchronizationContext context) {
		ResourceCompareInputChangeNotifier notifier = (ResourceCompareInputChangeNotifier)context.getCache().get(ResourceCompareInputChangeNotifier.RESOURCE_CHANGE_NOTIFIER_PROPERTY);
		if (notifier == null) {
			notifier = new ResourceCompareInputChangeNotifier(context);
			context.getCache().put(ResourceCompareInputChangeNotifier.RESOURCE_CHANGE_NOTIFIER_PROPERTY, notifier);
		}
		return notifier;
	}

	private final CompareInputLabelProvider labelProvider = new CompareInputLabelProvider();
	
	/**
	 * Create a notifier
	 * @param context a synchronization context
	 */
	public ResourceCompareInputChangeNotifier(ISynchronizationContext context) {
		super(context);
	}
	
	protected void handleDispose() {
		super.handleDispose();
		labelProvider.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		List changedInputs = new ArrayList();
		ICompareInput[] inputs = getConnectedInputs();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			IResource resource = getResource(input);
			if (resource != null) {
				IResourceDelta delta = event.getDelta().findMember(resource.getFullPath());
				if (delta != null) {
					if ((delta.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) > 0
							|| (delta.getKind() & (IResourceDelta.CHANGED)) > 0 
								&& (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.REPLACED)) > 0) {
						changedInputs.add(input);
					}
				}
			}
		}
		if (!changedInputs.isEmpty())
			handleInputChanges((ICompareInput[]) changedInputs.toArray(new ICompareInput[changedInputs.size()]), true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#diffsChanged(org.eclipse.team.core.diff.IDiffChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		Set changedInputs = new HashSet();
		IDiff[] added = event.getAdditions();
		for (int i = 0; i < added.length; i++) {
			IDiff diff = added[i];
			ICompareInput input = findInput(ResourceDiffTree.getResourceFor(diff));
			if (input != null)
				changedInputs.add(input);
		}
		IDiff[] changed = event.getChanges();
		for (int i = 0; i < changed.length; i++) {
			IDiff diff = changed[i];
			ICompareInput input = findInput(ResourceDiffTree.getResourceFor(diff));
			if (input != null)
				changedInputs.add(input);
		}
		IPath[] paths = event.getRemovals();
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			ICompareInput input = findInput(path);
			if (input != null)
				changedInputs.add(input);
		}
		
		if (!changedInputs.isEmpty())
			handleInputChanges((ICompareInput[]) changedInputs.toArray(new ICompareInput[changedInputs.size()]), false);
	}
	
	private IResource getResource(ICompareInput input) {
		if (input instanceof IResourceProvider) {
			IResourceProvider rp = (IResourceProvider) input;
			return rp.getResource();
		}
		return Utils.getResource(input);
	}
	
	private ICompareInput findInput(IPath path) {
		ICompareInput[] inputs = getConnectedInputs();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			IResource inputResource = getResource(input);
			if (inputResource != null && inputResource.getFullPath().equals(path)) {
				return input;
			}
		}
		return null;
	}

	private ICompareInput findInput(IResource resource) {
		ICompareInput[] inputs = getConnectedInputs();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			IResource inputResource = getResource(input);
			if (inputResource != null && inputResource.equals(resource)) {
				return input;
			}
		}
		return null;
	}

	/**
	 * Handle the input changes by notifying any listeners of the changed inputs.
	 * @param inputs the changed inputs
	 */
	private void handleInputChanges(ICompareInput[] inputs, boolean force) {
		List realChanges = getRealChanges(inputs, force);
		if (! realChanges.isEmpty())
			inputsChanged((ICompareInput[]) realChanges.toArray(new ICompareInput[realChanges.size()]));
	}
	
	private List getRealChanges(ICompareInput[] inputs, boolean force) {
		List realChanges = new ArrayList();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			if (input instanceof ResourceDiffCompareInput) {
				ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
				if (force || rdci.needsUpdate()) {
					realChanges.add(rdci);
				}
			}
		}
		return realChanges;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.CompareInputChangeNotifier#fireChange(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	protected void fireChange(ICompareInput input) {
		if (input instanceof ResourceDiffCompareInput) {
			ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
			rdci.update();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.CompareInputChangeNotifier#prepareInput(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void prepareInput(ICompareInput input, IProgressMonitor monitor) {
		if (input instanceof ResourceDiffCompareInput) {
			ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
			IResource resource = rdci.getResource();
			IDiff diff = getContext().getDiffTree().getDiff(resource);
			try {
				ResourceDiffCompareInput.ensureContentsCached(diff, monitor);
			} catch (CoreException e) {
				// Log the exception and continue
				TeamUIPlugin.log(e);
			}
		}
		super.prepareInput(input, monitor);
	}

	/**
	 * Return the label provider for resource compare inputs.
	 * @return the label provider for resource compare inputs
	 */
	public ICompareInputLabelProvider getLabelProvider() {
		return labelProvider;
	}

	public void fetchAuthors(final Object input) {
		runInBackground(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				fetchAuthors(input, monitor);
			}
		});
	}

	protected void fetchAuthors(Object input, IProgressMonitor monitor) throws CoreException {
		if (input instanceof ResourceDiffCompareInput) {
			ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
			if (rdci.updateAuthorInfo(monitor)) {
				fireLabelProviderChange(input);
			}
		}
	}

	private void fireLabelProviderChange(Object input) {
		labelProvider.fireChangeEvent(input);
	}
}
