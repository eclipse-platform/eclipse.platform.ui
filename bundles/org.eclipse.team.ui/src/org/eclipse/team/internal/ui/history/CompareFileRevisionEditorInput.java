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
package org.eclipse.team.internal.ui.history;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput;

public class CompareFileRevisionEditorInput extends LocalResourceCompareEditorInput {

	//constants for indicating which side contains the 
	//current version of the resource
	final static int NO_CURRENT = -1;
	final static int LEFT = 0;
	final static int RIGHT = 1;
	
	private ITypedElement left;
	private ITypedElement right;
	private IFile resource;
	private int currentSide;
	
	public class MyDiffNode extends DiffNode {
		public MyDiffNode(ITypedElement left, ITypedElement right) {
			super(left, right);
		}
		public void fireChange() {
			super.fireChange();
		}
	}
	
	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 * @param left 
	 * @param right 
	 */
	public CompareFileRevisionEditorInput(FileRevisionTypedElement left, FileRevisionTypedElement right) {
		super(new CompareConfiguration(), null);
		this.left = left;
		this.right = right;
		this.resource = null;
		this.currentSide = NO_CURRENT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#internalPrepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ICompareInput internalPrepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		//if one of the sides contain the current version of the file, enable it for editing
		if(resource != null){
			switch (currentSide){
				case LEFT:
				left = new LocalResourceTypedElement(resource);
				getCompareConfiguration().setLeftEditable(true);
				break;
				
				case RIGHT:
				//switch left and right panes to get current version on the left side
				CompareConfiguration cc = getCompareConfiguration();
				String leftLabel = cc.getLeftLabel(left);
				String rightLabel = cc.getRightLabel(right);
				ITypedElement tempElement = left;
				left = new LocalResourceTypedElement(resource);
				right = tempElement;
				cc.setLeftEditable(true);
				cc.setLeftLabel(rightLabel);
				cc.setRightLabel(leftLabel);
				break;
			}
			
		}
		MyDiffNode input = new MyDiffNode(left,right);
		return input;
	}

	private void initLabels() {
		CompareConfiguration cc = getCompareConfiguration();
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
		String leftLabel = getFileRevisionLabel((FileRevisionTypedElement) left);
		cc.setLeftLabel(leftLabel);
		String rightLabel = getFileRevisionLabel((FileRevisionTypedElement) right);
		cc.setRightLabel(rightLabel);
	}

	private String getFileRevisionLabel(FileRevisionTypedElement element) {
		String label = null;
		Object fileObject = element.getFileRevision();
	
		if (fileObject instanceof LocalFileRevision){
			try {
				IStorage storage = ((LocalFileRevision) fileObject).getStorage(new NullProgressMonitor());
				if (Utils.getAdapter(storage, IFileState.class) != null){
					//local revision
					label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_localRevision, new Object[]{element.getName(), element.getTimestamp()});
				} else if (Utils.getAdapter(storage, IFile.class) != null) {
					//current revision
					label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_workspace, new Object[]{ element.getName(), element.getTimestamp()});
					resource = (IFile) storage;
					currentSide = (element == left ? LEFT : RIGHT);
				}
			} catch (CoreException e) {
			}
		} else {
			label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_repository, new Object[]{ element.getName(), element.getContentIdentifier()});
		}
	
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		Object[] titleObject = new Object[3];
		titleObject[0] = getLongName(left);
		titleObject[1] = getContentIdentifier(left);
		titleObject[2] = getContentIdentifier(right);
		return NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_compareResourceAndVersions, titleObject);	 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		Object[] titleObject = new Object[3];
		titleObject[0] = getShortName(left);
		titleObject[1] = getContentIdentifier(left);
		titleObject[2] = getContentIdentifier(right);
		return NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_compareResourceAndVersions, titleObject);	 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IFile.class || adapter == IResource.class)
			return resource;
		return super.getAdapter(adapter);
	}
	
	private String getShortName(ITypedElement element) {
		if (element instanceof FileRevisionTypedElement){
			FileRevisionTypedElement fileRevisionElement = (FileRevisionTypedElement) element;
			return fileRevisionElement.getName();
		}
		else if (element instanceof LocalResourceTypedElement){
			LocalResourceTypedElement typedContent = (LocalResourceTypedElement) element;
			return typedContent.getResource().getName();
		}
		return ""; //$NON-NLS-1$
	}
	
	private String getLongName(ITypedElement element) {
		if (element instanceof FileRevisionTypedElement){
			FileRevisionTypedElement fileRevisionElement = (FileRevisionTypedElement) element;
			return fileRevisionElement.getPath();
		}
		else if (element instanceof LocalResourceTypedElement){
			LocalResourceTypedElement typedContent = (LocalResourceTypedElement) element;
			return typedContent.getResource().getFullPath().toString();
		}
		return ""; //$NON-NLS-1$
	}
	
	private String getContentIdentifier(ITypedElement element){
		if (element instanceof FileRevisionTypedElement){
			FileRevisionTypedElement fileRevisionElement = (FileRevisionTypedElement) element;
			Object fileObject = fileRevisionElement.getFileRevision();
			if (fileObject instanceof LocalFileRevision){
				try {
					IStorage storage = ((LocalFileRevision) fileObject).getStorage(new NullProgressMonitor());
					if (Utils.getAdapter(storage, IFileState.class) != null){
						//local revision
						return TeamUIMessages.CompareFileRevisionEditorInput_0;
					} else if (Utils.getAdapter(storage, IFile.class) != null) {
						//current revision
						return TeamUIMessages.CompareFileRevisionEditorInput_1;
					}
				} catch (CoreException e) {
				}
			} else {
				return fileRevisionElement.getContentIdentifier();
			}
		}
		else if (element instanceof LocalResourceTypedElement){
			return TeamUIMessages.CompareFileRevisionEditorInput_2;
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#fireInputChange()
	 */
	protected void fireInputChange() {
		((MyDiffNode)getCompareResult()).fireChange();
	}

}
