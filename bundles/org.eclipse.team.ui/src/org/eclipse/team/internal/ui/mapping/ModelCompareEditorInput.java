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
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizeView;
import org.eclipse.team.ui.mapping.ISynchronizationCompareInput;
import org.eclipse.team.ui.mapping.SaveableComparison;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.Saveable;

/**
 * A saveable based compare editor input for compare inputs from a {@link ModelSynchronizeParticipant}.
 */
public class ModelCompareEditorInput extends SaveableCompareEditorInput {

	private final ModelSynchronizeParticipant participant;
	private final ICompareInput input;
	private final ICacheListener contextListener;

	public ModelCompareEditorInput(ModelSynchronizeParticipant participant, ICompareInput input, IWorkbenchPage page) {
		super(new CompareConfiguration(), page);
		Assert.isNotNull(participant);
		Assert.isNotNull(input);
		this.participant = participant;
		this.input = input;
		contextListener = new ICacheListener() {
			public void cacheDisposed(ICache cache) {
				closeEditor(true);
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#contentsCreated()
	 */
	protected void contentsCreated() {
		super.contentsCreated();
		participant.getContext().getCache().addCacheListener(contextListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
		participant.getContext().getCache().removeCacheListener(contextListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#createSaveable()
	 */
	protected Saveable createSaveable() {
		if (input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			SaveableComparison compareModel = mci.getSaveable();
			if (compareModel != null)
				return compareModel;
		}
		return super.createSaveable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ICompareInput internalPrepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
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
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_tooltip, new String[] { Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, participant.getName()), fullPath });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#fireInputChange()
	 */
	protected void fireInputChange() {
		if (input instanceof ResourceDiffCompareInput) {
			ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) input;
			rdci.fireChange();
		}
	}
}
