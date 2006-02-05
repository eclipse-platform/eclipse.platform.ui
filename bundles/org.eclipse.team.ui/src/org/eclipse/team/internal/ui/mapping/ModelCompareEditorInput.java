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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.mapping.IModelCompareInput;
import org.eclipse.team.ui.mapping.ISaveableCompareModel;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.*;

public class ModelCompareEditorInput extends CompareEditorInput implements ISaveableModelSource, IPropertyListener {

	private final ModelSynchronizeParticipant participant;
	private final ICompareInput input;
	private final ISaveableModel model;

	public ModelCompareEditorInput(ModelSynchronizeParticipant participant, ICompareInput input) {
		super(new CompareConfiguration());
		Assert.isNotNull(participant);
		Assert.isNotNull(input);
		this.participant = participant;
		this.input = input;
		this.model = asSaveableModel(input);
		setDirty(model.isDirty());
	}

	private ISaveableModel asSaveableModel(ICompareInput input) {
		if (input instanceof IModelCompareInput) {
			IModelCompareInput mci = (IModelCompareInput) input;
			ISaveableCompareModel compareModel = mci.getCompareModel();
			if (compareModel != null)
				return compareModel;
		}
		return new ISaveableModel() {
			public boolean isDirty() {
				return isSaveNeeded();
			}
			public void doSave(IProgressMonitor monitor) {
				try {
					saveChanges(monitor);
				} catch (CoreException e) {
					// TODO See bug 126082
					e.printStackTrace();
				}
			}
			public ImageDescriptor getImageDescriptor() {
				return ImageDescriptor.createFromImage(getTitleImage());
			}
			public String getToolTipText() {
				return ModelCompareEditorInput.this.getToolTipText();
			}
			public String getName() {
				return ModelCompareEditorInput.this.getTitle();
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (model instanceof ISaveableCompareModel) {
			final ISaveableCompareModel scm = (ISaveableCompareModel) model;
			scm.addPropertyListener(this);
			control.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					scm.removePropertyListener(ModelCompareEditorInput.this);
				}
			});
		}
		return control;
	}

	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched
		// from the server
		setTitle(getTitle());
        monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			IModelCompareInput adapter = asModelCompareInput(input);
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

	private IModelCompareInput asModelCompareInput(ICompareInput input) {
		return (IModelCompareInput)Utils.getAdapter(input, IModelCompareInput.class);
	}

	/**
	 * Return whether the compare input of this editor input matches the
	 * given object.
	 * @param object the object
	 * @return whether the compare input of this editor input matches the
	 * given object
	 */
	public boolean matches(Object object) {
		// TODO it would be faster to ask the input it it matched the given object
		// but this would require additional API
		ICompareInput input = participant.asCompareInput(object);
		if (input != null)
			return input.equals(this.input);
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other instanceof ModelCompareEditorInput) {
			ModelCompareEditorInput otherInput = (ModelCompareEditorInput) other;
			return input.equals(otherInput.input);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModelSource#getModels()
	 */
	public ISaveableModel[] getModels() {
		return new ISaveableModel[] { model };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModelSource#getActiveModels()
	 */
	public ISaveableModel[] getActiveModels() {
		return new ISaveableModel[] { model };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (propId == ISaveableCompareModel.PROP_DIRTY) {
			setDirty(model.isDirty());
		}
	}
	
	public Object getAdapter(Class adapter) {
		if (IFile.class.equals(adapter)) {
			IResource resource = Utils.getResource(input);
			if (resource instanceof IFile) {
				return resource;
			}
		}
		return super.getAdapter(adapter);
	}

}
