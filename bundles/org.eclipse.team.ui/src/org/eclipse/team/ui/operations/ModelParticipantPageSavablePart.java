/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.synchronize.*;

/**
 * A savable part that can be used with model based participants.
 * <p>
 * This class is not intended to be subclasses by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class ModelParticipantPageSavablePart extends
		ParticipantPageSaveablePart {

	/**
	 * Create a savable part for the given model participant
	 * @param shell the parent shell
	 * @param cc the compare configuration
	 * @param pageConfiguration the page configuration
	 * @param participant the model participant
	 */
	public ModelParticipantPageSavablePart(Shell shell, CompareConfiguration cc, ISynchronizePageConfiguration pageConfiguration, ModelSynchronizeParticipant participant) {
		super(shell, cc, pageConfiguration, participant);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ParticipantPageSaveablePart#getCompareInput(org.eclipse.jface.viewers.ISelection)
	 */
	protected ICompareInput getCompareInput(ISelection selection) {
		ICompareInput input = super.getCompareInput(selection);
		if (input != null)
			return input;
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o = ss.getFirstElement();
				return ((ModelSynchronizePage)getPageConfiguration().getPage()).asCompareInput(o);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ParticipantPageSaveablePart#findStructureViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	protected Viewer findStructureViewer(Composite parent, Viewer oldViewer, ICompareInput input) {
		Viewer viewer = ((ModelSynchronizePage)getPageConfiguration().getPage()).findStructureViewer(parent, oldViewer, input, getCompareConfiguration());
		if (viewer != null)
			return viewer;
		return super.findStructureViewer(parent, oldViewer, input);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ParticipantPageSaveablePart#findContentViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	protected Viewer findContentViewer(Composite parent, Viewer oldViewer, ICompareInput input) {
		Viewer viewer = ((ModelSynchronizePage)getPageConfiguration().getPage()).findContentViewer(parent, oldViewer, input, getCompareConfiguration());
		if (viewer != null)
			return viewer;
		return super.findContentViewer(parent, oldViewer, input);
	}
}
