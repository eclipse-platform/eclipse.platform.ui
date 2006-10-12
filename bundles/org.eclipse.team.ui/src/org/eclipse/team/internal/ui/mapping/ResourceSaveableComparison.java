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

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.mapping.ISynchronizationCompareInput;
import org.eclipse.team.ui.mapping.SaveableComparison;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

/**
 * A saveable compare model that wraps an {@link IFile} based compare input.
 */
public class ResourceSaveableComparison extends SaveableComparison implements IPropertyChangeListener {

	ICompareInput input;
	ISynchronizeParticipant participant;
	private final CompareEditorInput editorInput;
	private boolean isSaving;
	
	public static class ResourceDiffCompareInput extends DiffNode implements ISynchronizationCompareInput, IAdaptable, IResourceProvider {

		private final IDiff node;

		public ResourceDiffCompareInput(IDiff node) {
			super(getCompareKind(node), getAncestor(node), getLeftContributor(node), getRightContributor(node));
			this.node = node;
		}
		
		/**
		 * We need to make this public so we can fire a change event after
		 * we save 
		 */
		public void fireChange() {
			super.fireChange();
		}
		
		private static int getCompareKind(IDiff node) {
			int kind = 0;
			switch (node.getKind()) {
			case IDiff.CHANGE:
				kind = Differencer.CHANGE;
				break;
			case IDiff.ADD:
				kind = Differencer.ADDITION;
				break;
			case IDiff.REMOVE:
				kind = Differencer.DELETION;
				break;
			}
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				switch (twd.getDirection()) {
				case IThreeWayDiff.INCOMING:
					kind |= Differencer.RIGHT;
					break;
				case IThreeWayDiff.OUTGOING:
					kind |= Differencer.LEFT;
					break;
				case IThreeWayDiff.CONFLICTING:
					kind |= Differencer.CONFLICTING;
					break;
				}
			}
			return kind;
		}
		
		private static ITypedElement getRightContributor(IDiff node) {
			// For a resource diff, use the after state
			if (node instanceof IResourceDiff) {
				IResourceDiff rd = (IResourceDiff) node;
				return asTypedElement(rd.getAfterState(), getLocalEncoding(node));
			}
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
				// If there is a remote change, use the after state
				if (diff != null)
					return getRightContributor(diff);
				// There's no remote change so use the before state of the local
				diff = (IResourceDiff)twd.getLocalChange();
				return asTypedElement(diff.getBeforeState(), getLocalEncoding(node));
				
			}
			return null;
		}

		private static ITypedElement getLeftContributor(final IDiff node) {
			// The left contributor is always the local resource
			return new LocalResourceTypedElement(ResourceDiffTree.getResourceFor(node));
		}

		private static ITypedElement getAncestor(IDiff node) {
			if (node instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) node;
				IResourceDiff diff = (IResourceDiff)twd.getLocalChange();
				if (diff == null)
					diff = (IResourceDiff)twd.getRemoteChange();
				return asTypedElement(diff.getBeforeState(), getLocalEncoding(node));
				
			}
			return null;
		}

		private static String getLocalEncoding(IDiff node) {
			IResource resource = ResourceDiffTree.getResourceFor(node);
			if (resource instanceof IEncodedStorage) {
				IEncodedStorage es = (IEncodedStorage) resource;
				try {
					return es.getCharset();
				} catch (CoreException e) {
					TeamUIPlugin.log(e);
				}
			}
			return null;
		}

		private static ITypedElement asTypedElement(IFileRevision state, String localEncoding) {
			if (state == null)
				return null;
			return new FileRevisionTypedElement(state, localEncoding);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.mapping.IModelCompareInput#prepareInput(org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void prepareInput(CompareConfiguration configuration, IProgressMonitor monitor) throws CoreException {
			Utils.updateLabels(node, configuration, monitor);
			// We need to cache contents here as well
			Object ancestor = getAncestor();
			if (ancestor instanceof FileRevisionTypedElement) {
				FileRevisionTypedElement fste = (FileRevisionTypedElement) ancestor;
				fste.cacheContents(monitor);
			}
			Object right = getRight();
			if (right instanceof FileRevisionTypedElement) {
				FileRevisionTypedElement fste = (FileRevisionTypedElement) right;
				fste.cacheContents(monitor);
			}
		}
		
		private boolean hasSaveConflict() {
			return ((LocalResourceTypedElement)getLeft()).hasSaveConflict();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInput#getSaveable()
		 */
		public SaveableComparison getSaveable() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter == IFile.class || adapter == IResource.class) {
				if (node instanceof IResourceDiff) {
					IResourceDiff rd = (IResourceDiff) node;
					return (IFile)rd.getResource();
				}
				if (node instanceof IThreeWayDiff) {
					IThreeWayDiff twd = (IThreeWayDiff) node;
					IResourceDiff diff = (IResourceDiff)twd.getRemoteChange();
					// If there is a remote change, use the after state
					if (diff != null)
						return (IFile)diff.getResource();
					// There's no remote change so use the before state of the local
					diff = (IResourceDiff)twd.getLocalChange();
					return (IFile)diff.getResource();
					
				}
			}
			return null;
		}

		public String getFullPath() {
			final IResource resource = ResourceDiffTree.getResourceFor(node);
			if (resource != null)
				return resource.getFullPath().toString();
			return getName();
		}

		public boolean isCompareInputFor(Object object) {
			final IResource resource = ResourceDiffTree.getResourceFor(node);
			IResource other = Utils.getResource(object);
			if (resource != null && other != null)
				return resource.equals(other);
			return false;
		}
		
		public boolean checkUpdateConflict() {
			if(hasSaveConflict()) {
				final MessageDialog dialog = 
					new MessageDialog(TeamUIPlugin.getStandardDisplay().getActiveShell(), 
							TeamUIMessages.SyncInfoCompareInput_0,  
							null, 
							TeamUIMessages.SyncInfoCompareInput_1,  
							MessageDialog.QUESTION,
						new String[] {
							TeamUIMessages.SyncInfoCompareInput_2, 
							IDialogConstants.CANCEL_LABEL}, 
						0);
				
				int retval = dialog.open();
				switch(retval) {
					// save
					case 0: 
						return false;
					// cancel
					case 1:
						return true;
				}
			}
			return false;
		}

		public IResource getResource() {
			return ResourceDiffTree.getResourceFor(node);
		}
	}
	
	public ResourceSaveableComparison(ICompareInput input, ISynchronizeParticipant participant, ModelCompareEditorInput editorInput) {
		this.input = input;
		this.participant = participant;
		this.editorInput = editorInput;
		initializeContentChangeListeners();
	}
	
	private void initializeContentChangeListeners() {
		// We need to listen to saves to the input to catch the case
		// where Save was picked from the context menu
		ITypedElement te = input.getLeft();
		if (te instanceof IContentChangeNotifier) {
			((IContentChangeNotifier) te).addContentChangeListener(new IContentChangeListener() {
				public void contentChanged(IContentChangeNotifier source) {
					try {
						if(! isSaving) {
							performSave(new NullProgressMonitor());
						}
					} catch (CoreException e) {
						TeamUIPlugin.log(e);
					}
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SaveableCompareModel#performSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void performSave(IProgressMonitor monitor) throws CoreException {
		if (input instanceof ResourceDiffCompareInput) {
			ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
			if (rdci.checkUpdateConflict())
				return;
		}
		ITypedElement left = input.getLeft();
		if (left instanceof LocalResourceTypedElement) {
			LocalResourceTypedElement te = (LocalResourceTypedElement) left;
			if (te.isConnected()) {
				te.saveDocument(monitor);
				// Saving the document should fire the necessary updates
				return;
			}
		}
		try {
			isSaving = true;
			monitor.beginTask(null, 100);
			// First, we need to flush the viewers so the changes get buffered
			// in the input
			editorInput.saveChanges(Policy.subMonitorFor(monitor, 40));
			// Then we tell the input to commit its changes
			// Only the left is ever saveable
			if (left instanceof LocalResourceTypedElement) {
				LocalResourceTypedElement te = (LocalResourceTypedElement) left;
				te.commit(Policy.subMonitorFor(monitor, 60));
			}
		} finally {
			// Make sure we fire a change for the compare input to update the viewers
			if (input instanceof ResourceDiffCompareInput) {
				ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
				rdci.fireChange();
			}
			setDirty(false);
			isSaving = false;
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SaveableCompareModel#isDirty()
	 */
	public boolean isDirty() {
		// We need to get the dirty state from the compare editor input
		// since it is our only connection to the merge viewer
		return editorInput.isSaveNeeded();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SaveableCompareModel#setDirty(boolean)
	 */
	protected void setDirty(boolean dirty) {
		// We need to set the dirty state on the compare editor input
		// since it is our only connection to the merge viewer
		editorInput.setDirty(dirty);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SaveableCompareModel#performRevert(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void performRevert(IProgressMonitor monitor) {
		// Only the left is ever editable
		ITypedElement left = input.getLeft();
		if (left instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) left).discardBuffer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModel#getName()
	 */
	public String getName() {
		return input.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModel#getToolTipText()
	 */
	public String getToolTipText() {
		String fullPath;
		if (input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			fullPath = mci.getFullPath();
		} else {
			fullPath = getName();
		}
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_tooltip, new String[] { Utils.shortenText(30, participant.getName()), fullPath });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModel#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		Image image = input.getImage();
		if (image != null)
			return ImageDescriptor.createFromImage(image);
		return TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName= e.getProperty();
		if (CompareEditorInput.DIRTY_STATE.equals(propertyName)) {
			boolean changed= false;
			Object newValue= e.getNewValue();
			if (newValue instanceof Boolean)
				changed= ((Boolean)newValue).booleanValue();
			setDirty(changed);
		}			
	}

	public boolean equals(Object object) {
		if (object instanceof ResourceSaveableComparison) {
			ResourceSaveableComparison rscm = (ResourceSaveableComparison) object;
			return rscm.input.equals(input);
		}
		return false;
	}

	public int hashCode() {
		return input.hashCode();
	}
}
