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
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.AbstractCompareInput;
import org.eclipse.team.internal.ui.mapping.CompareInputChangeNotifier;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public class CompareFileRevisionEditorInput extends SaveableCompareEditorInput {
	
	private ITypedElement left;
	private ITypedElement right;
	CompareInputChangeNotifier notifier = new CompareInputChangeNotifier() {
		protected IResource[] getResources(ICompareInput input) {
			IResource resource = getResource(input);
			if (resource == null)
				return new IResource[0];
			return new IResource[] { resource };
		}
	};
	
	public class MyDiffNode extends AbstractCompareInput {
		public MyDiffNode(ITypedElement left, ITypedElement right) {
			super(Differencer.CHANGE, null, left, right);
		}
		public void fireChange() {
			super.fireChange();
		}
		protected CompareInputChangeNotifier getChangeNotifier() {
			return notifier;
		}
		public boolean needsUpdate() {
			// The remote never changes
			return false;
		}
		public void update() {
			fireChange();
		}
	}
	
	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 * @param left 
	 * @param right 
	 * @param page 
	 */
	public CompareFileRevisionEditorInput(FileRevisionTypedElement left, FileRevisionTypedElement right, IWorkbenchPage page) {
		super(new CompareConfiguration(), page);
		this.left = left;
		this.right = right;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#internalPrepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ICompareInput internalPrepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		ICompareInput input = createCompareInput();
		getCompareConfiguration().setLeftEditable(isLeftEditable(input));
		getCompareConfiguration().setRightEditable(false);
		initLabels(input);
		return input;
	}

	private boolean isLeftEditable(ICompareInput input) {
		Object left = input.getLeft();
		if (left instanceof IEditableContent) {
			return ((IEditableContent) left).isEditable();
		}
		return false;
	}

	private IResource getResource(ICompareInput input) {
		Object left = input.getLeft();
		if (left instanceof IResourceProvider) {
			return ((IResourceProvider) left).getResource();
		}
		return null;
	}

	private ICompareInput createCompareInput() {
		IFile resource = getLocalFile(left);
		if (resource == null) {
			resource = getLocalFile(right);
			if (resource != null) {
				right = left;
			}
		}
		if (resource != null) {
			left = SaveableCompareEditorInput.createFileElement((IFile)resource);
		}
		MyDiffNode input = new MyDiffNode(left,right);
		return input;
	}

	private IFile getLocalFile(ITypedElement element) {
		IFileRevision revision = ((FileRevisionTypedElement)element).getFileRevision();
		if (revision instanceof LocalFileRevision) {
			LocalFileRevision local = (LocalFileRevision) revision;
			return local.getFile();
		}
		return null;
	}

	private void initLabels(ICompareInput input) {
		CompareConfiguration cc = getCompareConfiguration();
		if (input.getLeft() instanceof FileRevisionTypedElement) {
			String leftLabel = getFileRevisionLabel((FileRevisionTypedElement) input.getLeft());
			cc.setLeftLabel(leftLabel);
		} else if (getResource(input) != null) {
			String label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_workspace, new Object[]{ input.getLeft().getName() });
			cc.setLeftLabel(label);
		}
		String rightLabel = getFileRevisionLabel((FileRevisionTypedElement) input.getRight());
		cc.setRightLabel(rightLabel);
	}

	private String getFileRevisionLabel(FileRevisionTypedElement element) {
		Object fileObject = element.getFileRevision();
		if (fileObject instanceof LocalFileRevision){
			IFileState state = ((LocalFileRevision) fileObject).getState();
			if (state != null) {
				return NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_localRevision, new Object[]{element.getName(), element.getTimestamp()});
			} 
		} else {
			return NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_repository, new Object[]{ element.getName(), element.getContentIdentifier(), element.getAuthor()});
		}
		return ""; //$NON-NLS-1$
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
			return getResource(getCompareInput());
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

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#contentsCreated()
	 */
	protected void contentsCreated() {
		super.contentsCreated();
		notifier.initialize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
		notifier.dispose();
	}
}
