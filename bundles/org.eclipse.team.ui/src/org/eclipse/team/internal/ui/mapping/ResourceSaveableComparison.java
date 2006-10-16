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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

/**
 * A saveable compare model that wraps an {@link IFile} based compare input. This saveable is
 * created by the {@link ModelCompareEditorInput} instead of the {@link ResourceDiffCompareInput}
 * because it needs access to the compare editor input in order to flush the viewers. Other model-based
 * compare inputs do not need this since the compare input and viewers should be provided by the same model.
 */
public class ResourceSaveableComparison extends SaveableComparison implements IPropertyChangeListener {

	private ICompareInput input;
	private final ISynchronizeParticipant participant;
	private final CompareEditorInput editorInput;
	private boolean isSaving;
	private int hashCode;
	private boolean hashCodeSet = false;
	private IContentChangeListener contentChangeListener;
	
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
			if (contentChangeListener == null) {
				contentChangeListener = new IContentChangeListener() {
								public void contentChanged(IContentChangeNotifier source) {
									try {
										if(! isSaving) {
											performSave(new NullProgressMonitor());
										}
									} catch (CoreException e) {
										TeamUIPlugin.log(e);
									}
								}
							};
			}
			((IContentChangeNotifier) te).addContentChangeListener(contentChangeListener);
		}
	}
	
	private void removeContentChangeListeners() {
		if (contentChangeListener != null) {
			ITypedElement te = input.getLeft();
			if (te instanceof IContentChangeNotifier) {
				((IContentChangeNotifier) te).removeContentChangeListener(contentChangeListener);
			}
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
		// We want to remember the hash code so it never changes.
		if (!hashCodeSet) {
			hashCode = input.hashCode();
			hashCodeSet = true;
		}
		return hashCode;
	}

	/**
	 * Return the compare input that is managed by this saveable.
	 * @return the compare input that is managed by this saveable
	 */
	public ICompareInput getInput() {
		return input;
	}

	/**
	 * Set the compare input managed by this saveable. This method is
	 * only intended to work for inputs that represent the same comparison
	 * but have a state change in one of the contributors.
	 * @param input the compare input
	 */
	public void setInput(ICompareInput input) {
		if (input != this.input) {
			removeContentChangeListeners();
			this.input = input;
			initializeContentChangeListeners();
		}
	}
}
