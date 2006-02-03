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

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.core.LocalFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.ISaveableWorkbenchPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;

public class CompareFileRevisionEditorInput extends CompareEditorInput implements ISaveableWorkbenchPart {

	//constants for indicating which side contains the 
	//current version of the resource
	final static int NO_CURRENT = -1;
	final static int LEFT = 0;
	final static int RIGHT = 1;
	
	private ITypedElement left;
	private ITypedElement right;
	private IFile resource;
	private int currentSide;
	
	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 */
	public CompareFileRevisionEditorInput(FileRevisionTypedElement left, FileRevisionTypedElement right) {
		super(new CompareConfiguration());
		this.left = left;
		this.right = right;
		this.resource = null;
		this.currentSide = NO_CURRENT;
	}

	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		//if one of the sides contain the current version of the file, enable it for editing
		if(resource != null){
			switch (currentSide){
				case LEFT:
				left = new TypedBufferedContent(resource);
				getCompareConfiguration().setLeftEditable(true);
				break;
				
				case RIGHT:
				//switch left and right panes to get current version on the left side
				CompareConfiguration cc = getCompareConfiguration();
				String leftLabel = cc.getLeftLabel(left);
				String rightLabel = cc.getRightLabel(right);
				ITypedElement tempElement = left;
				left = new TypedBufferedContent(resource);
				right = tempElement;
				cc.setLeftEditable(true);
				cc.setLeftLabel(rightLabel);
				cc.setRightLabel(leftLabel);
				break;
			}
			
		}
		return new DiffNode(left,right);
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

	public void doSave(IProgressMonitor monitor) {
		try {
			saveChanges(monitor);
		} catch (CoreException e) {
			Utils.handle(e);
		}
	}

	public void doSaveAs() {
		// noop
	}

	public boolean isDirty() {
		return isSaveNeeded();
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	public void addPropertyListener(IPropertyListener listener) {
		// noop
	}

	public void createPartControl(Composite parent) {
		createContents(parent);
	}

	public void dispose() {	
	}

	public IWorkbenchPartSite getSite() {
		return null;
	}

	public String getTitleToolTip() {
		return null;
	}

	public void removePropertyListener(IPropertyListener listener) {
		// noop
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		//
		return NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_compareResourceAndVersions, (new Object[]{left.getName()} ));
		//return NLS.bind(CVSUIMessages.CVSCompareRevisionsInput_compareResourceAndVersions, (new Object[] {resource.getFullPath().toString()})); 
	}

}
