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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.*;

public class ModelCompareEditorInput extends CompareEditorInput implements ISaveablesSource, IPropertyListener {

	private final ModelSynchronizeParticipant participant;
	private final ICompareInput input;
	private final Saveable model;

	public ModelCompareEditorInput(ModelSynchronizeParticipant participant, ICompareInput input) {
		super(new CompareConfiguration());
		Assert.isNotNull(participant);
		Assert.isNotNull(input);
		this.participant = participant;
		this.input = input;
		this.model = asSaveable(input);
		setDirty(model.isDirty());
	}

	private Saveable asSaveable(ICompareInput input) {
		if (input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			SaveableComparison compareModel = mci.getSaveable();
			if (compareModel != null)
				return compareModel;
		}
		// TODO: Should fail if the input is the wrong type
		return new ResourceSaveableComparison(input, participant, this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (model instanceof SaveableComparison) {
			final SaveableComparison scm = (SaveableComparison) model;
			scm.addPropertyListener(this);
			control.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					scm.removePropertyListener(ModelCompareEditorInput.this);
				}
			});
		}
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched
		// from the server
		setTitle(getTitle());
        monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			ISynchronizationCompareInput adapter = asModelCompareInput(input);
			if (adapter != null) {
				adapter.prepareInput(getCompareConfiguration(), Policy.subMonitorFor(monitor, 100));
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
            monitor.done();
        }
		return input;
	}

	private ISynchronizationCompareInput asModelCompareInput(ICompareInput input) {
		return (ISynchronizationCompareInput)Utils.getAdapter(input, ISynchronizationCompareInput.class);
	}

	/**
	 * Return whether the compare input of this editor input matches the
	 * given object.
	 * @param object the object
	 * @param participant the participant associated with the given object
	 * @return whether the compare input of this editor input matches the
	 * given object
	 */
	public boolean matches(Object object, ISynchronizeParticipant participant) {
		if (participant == this.participant && input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			return mci.isCompareInputFor(object);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (propId == SaveableComparison.PROP_DIRTY) {
			setDirty(model.isDirty());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IFile.class.equals(adapter)) {
			IResource resource = Utils.getResource(input);
			if (resource instanceof IFile) {
				return resource;
			}
		}
		return super.getAdapter(adapter);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitleImage()
	 */
	public Image getTitleImage() {
		if (input != null) {
			Image image = input.getImage();
			if (image != null)
				return image;
		} 
		ImageRegistry reg = TeamUIPlugin.getPlugin().getImageRegistry();
		Image image = reg.get(ITeamUIImages.IMG_SYNC_VIEW);
		if (image == null) {
			image = getImageDescriptor().createImage();
			reg.put(ITeamUIImages.IMG_SYNC_VIEW, image);
		}
		return image;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_title, new String[] { input.getName() }); 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (input != null) {
			Image image = input.getImage();
			if (image != null)
				return ImageDescriptor.createFromImage(image);
		} 
		return TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		String fullPath;
		ISynchronizationCompareInput adapter = asModelCompareInput(input);
		if (adapter != null) {
			fullPath = adapter.getFullPath();
		} else {
			fullPath = getName();
		}
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_tooltip, new String[] { Utils.shortenText(30, participant.getName()), fullPath });
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#findContentViewer(org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.swt.widgets.Composite)
	 */
	public Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent) {
		Viewer newViewer = super.findContentViewer(oldViewer, input, parent);
		boolean isNewViewer= newViewer != oldViewer;
		if (isNewViewer && newViewer instanceof IPropertyChangeNotifier && model instanceof IPropertyChangeListener) {
			// Register the model for change events if appropriate
			final IPropertyChangeNotifier dsp= (IPropertyChangeNotifier) newViewer;
			final IPropertyChangeListener pcl = (IPropertyChangeListener) model;
			dsp.addPropertyChangeListener(pcl);
			Control c= newViewer.getControl();
			c.addDisposeListener(
				new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						dsp.removePropertyChangeListener(pcl);
					}
				}
			);
		}
		return newViewer;
	}

	/**
	 * {@inheritDoc}
	 */
	public Saveable[] getActiveSaveables() {
		return new Saveable[] { model };
	}

	/**
	 * {@inheritDoc}
	 */
	public Saveable[] getSaveables() {
		return getActiveSaveables();
	}

}
