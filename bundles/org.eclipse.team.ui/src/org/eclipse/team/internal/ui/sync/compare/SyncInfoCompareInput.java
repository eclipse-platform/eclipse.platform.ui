/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.compare;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.Policy;

public class SyncInfoCompareInput extends CompareEditorInput {

	private SyncInfo sync;
	private SyncInfoDiffNode node;
	
	/* protected */ SyncInfoCompareInput() {
		super(new CompareConfiguration());		
	}
	
	public SyncInfoCompareInput(SyncInfo sync) {
		super(new CompareConfiguration());
		this.sync = sync;
				
		ITypedElement elements[] = SyncInfoDiffNode.getTypedElements(sync);
		this.node = new SyncInfoDiffNode(elements[0], elements[1], elements[2], sync.getKind());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched from the server
		setTitle(getTitle());
		updateLabels();
		return node;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {		
		return node.getName();
	}
	
	protected void updateLabels() {
		CompareConfiguration config = getCompareConfiguration();
		config.setLeftLabel(Policy.bind("SyncInfoCompareInput.localLabel"));
		config.setRightLabel(Policy.bind("SyncInfoCompareInput.remoteLabel"));
		config.setAncestorLabel(Policy.bind("SyncInfoCompareInput.baseLabel"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(other == this) return true;
		if(other instanceof SyncInfoCompareInput) {
			return node.equals(((SyncInfoCompareInput)other).getCompareResult());
		} else if(other instanceof SyncInfoCompareInputFinder) {
			return true;
		}
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		if (node instanceof DiffNode) {
			try {
				commit(pm, (DiffNode) node);
			} finally {
				setDirty(false);
			}
		}
	}
	
	/*
	 * Recursively walks the diff tree and commits all changes.
	 */
	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		ITypedElement left= node.getLeft();
		if (left instanceof LocalResourceTypedElement)
			((LocalResourceTypedElement) left).commit(pm);
			
		ITypedElement right= node.getRight();
		if (right instanceof LocalResourceTypedElement)
			((LocalResourceTypedElement) right).commit(pm);
	}
}
