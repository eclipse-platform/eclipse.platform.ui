/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

 
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.ISaveableWorkbenchPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;

public class CVSCompareRevisionsInput extends CompareEditorInput implements ISaveableWorkbenchPart {
	IFile resource;
	ILogEntry[] editions;
	TableViewer viewer;
	Action getRevisionAction;	
	Action getContentsAction;
	Shell shell;
	
	// Provide the widget for the history table
	private HistoryTableProvider historyTableProvider;
	
	/**
	 * Provide a wrapper for a resource node that doesn't buffer. Changes are saved directly to the
	 * underlying file.
	 */
	class TypedBufferedContent extends ResourceNode {
		public TypedBufferedContent(IFile resource) {
			super(resource);
		}
		protected InputStream createStream() throws CoreException {
			return ((IFile)getResource()).getContents();
		}
		public void setContent(byte[] contents) {
			if (contents == null) contents = new byte[0];
			final InputStream is = new ByteArrayInputStream(contents);
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IFile file = resource;
						if (is != null) {
							if (!file.exists()) {
								file.create(is, false, monitor);
							} else {
								file.setContents(is, false, true, monitor);
							}
						} else {
							file.delete(false, true, monitor);
						}
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			try {
				new ProgressMonitorDialog(shell).run(false, false, runnable);
			} catch (InvocationTargetException e) {
				CVSUIPlugin.openError(CVSUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell(), Policy.bind("TeamFile.saveChanges", resource.getName()), null, e); //$NON-NLS-1$
			} catch (InterruptedException e) {
				// Ignore
			}
			fireContentChanged();
		}	
		public ITypedElement replace(ITypedElement child, ITypedElement other) {
			return null;
		}
		public void fireChange() {
			fireContentChanged();
		}
	}
	
	/**
	 * This class is an edition node which knows the log entry it came from.
	 */
	class ResourceRevisionNode extends ResourceEditionNode {	
		ILogEntry entry;
		public ResourceRevisionNode(ILogEntry entry) {
			super(entry.getRemoteFile());
			this.entry = entry;
		}
		public ILogEntry getLogEntry() {
			return entry;
		}
		public String getName() {
			String revisionName = entry.getRevision();
			if (revisionName != null) {
				IResource resource = CVSCompareRevisionsInput.this.resource;
				try {
					ICVSRemoteFile currentEdition = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(resource);
					if (currentEdition != null && currentEdition.getRevision().equals(revisionName)) {
						Policy.bind("currentRevision", revisionName); //$NON-NLS-1$
					} else {
						return revisionName;
					}
				} catch (TeamException e) {
					handle(e);
				}
			}
			return super.getName();
		}
	}
	
	/**
	 * A compare node that gets its label from the right element
	 */
	class VersionCompareDiffNode extends DiffNode implements IAdaptable {
		public VersionCompareDiffNode(ITypedElement left, ITypedElement right) {
			super(left, right);
		}
		public String getName() {
			return getRight().getName();
		}
		public Object getAdapter(Class adapter) {
			if (adapter == ILogEntry.class) {
				return ((ResourceRevisionNode)getRight()).getLogEntry();
			}
			return null;
		}
		public void fireContentChanges() {
			fireChange();
		}
	}
	/**
	 * A content provider which knows how to get the children of the diff container
	 */
	class VersionCompareContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof DiffContainer) {
				return ((DiffContainer)inputElement).getChildren();
			}
			return null;
		}
	}
	
	public CVSCompareRevisionsInput(IFile resource, ILogEntry[] editions) {
		super(new CompareConfiguration());
		this.resource = resource;
		this.editions = editions;
		updateCurrentEdition();
		initializeActions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		return c;
	}
	
	public Viewer createDiffViewer(Composite parent) {
		this.shell = parent.getShell();
		viewer = getHistoryTableProvider().createTable(parent);
		Table table = viewer.getTable();
		table.setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle()); //$NON-NLS-1$

		viewer.setContentProvider(new VersionCompareContentProvider());

		MenuManager mm = new MenuManager();
		mm.setRemoveAllWhenShown(true);
		mm.addMenuListener(
			new IMenuListener() {
				public void menuAboutToShow(IMenuManager mm) {
					mm.add(getContentsAction);
					mm.add(getRevisionAction);
				}
			}
		);
		table.setMenu(mm.createContextMenu(table));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!(selection instanceof IStructuredSelection)) {
					getRevisionAction.setEnabled(false);
					getContentsAction.setEnabled(false);
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				getRevisionAction.setEnabled(ss.size() == 1);
				getContentsAction.setEnabled(ss.size() == 1);
			}	
		});
		
		// Add F1 help.
		WorkbenchHelp.setHelp(table, IHelpContextIds.COMPARE_REVISIONS_VIEW);
		return viewer;
	}
	
	private void initLabels() {
		CompareConfiguration cc = getCompareConfiguration();
		cc.setLeftEditable(true);
		cc.setRightEditable(false);
		String resourceName = resource.getName();
		String leftLabel = Policy.bind("CVSCompareRevisionsInput.workspace", new Object[] {resourceName}); //$NON-NLS-1$
		cc.setLeftLabel(leftLabel);
		String rightLabel = Policy.bind("CVSCompareRevisionsInput.repository", new Object[] {resourceName}); //$NON-NLS-1$
		cc.setRightLabel(rightLabel);
	}
	private void initializeActions() {		
		getRevisionAction = new Action(Policy.bind("HistoryView.getRevisionAction")) { //$NON-NLS-1$
			public void run() {
				try {
					new ProgressMonitorDialog(shell).run(false, true, new WorkspaceModifyOperation(null) {
						protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
							if (selection.size() != 1) return;
							VersionCompareDiffNode node = (VersionCompareDiffNode)selection.getFirstElement();
							ResourceEditionNode right = (ResourceEditionNode)node.getRight();
							ICVSRemoteResource edition = right.getRemoteResource();
							// Do the load. This just consists of setting the local contents. We don't
							// actually want to change the base.
							try {
								CVSTag revisionTag = new CVSTag(((ICVSRemoteFile)edition).getRevision(), CVSTag.VERSION);
								if(CVSAction.checkForMixingTags(shell, new IResource[] {resource}, revisionTag)) {
									new UpdateOperation(
											null, 
											new IResource[] {resource},
											new Command.LocalOption[] {Update.IGNORE_LOCAL_CHANGES}, 
											revisionTag)
												.run(monitor);
									getHistoryTableProvider().setFile((ICVSFile)edition);
								}
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InterruptedException e) {
					// Do nothing
					return;
				} catch (InvocationTargetException e) {
					handle(e);
				}
				// fire change
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (selection.size() != 1) return;
				VersionCompareDiffNode node = (VersionCompareDiffNode)selection.getFirstElement();
				TypedBufferedContent left = (TypedBufferedContent)node.getLeft();
				left.fireChange();
				// recompute the labels on the viewer
				viewer.refresh();
			}
		};
		getContentsAction = new Action(Policy.bind("HistoryView.getContentsAction")) { //$NON-NLS-1$
			public void run() {
				try {
					replaceLocalWithCurrentlySelectedRevision();
				} catch (CoreException e) {
					Utils.handle(e);
				}
			}
		};
	}
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		DiffNode diffRoot = new DiffNode(Differencer.NO_CHANGE);
		ITypedElement left = new TypedBufferedContent(resource);
		for (int i = 0; i < editions.length; i++) {		
			ITypedElement right = new ResourceRevisionNode(editions[i]);
			diffRoot.add(new VersionCompareDiffNode(left, right));
		}
		return diffRoot;		
	}
	private void updateCurrentEdition() {
		try {
			getHistoryTableProvider().setFile((ICVSFile) CVSWorkspaceRoot.getRemoteResourceFor(resource));
		} catch (TeamException e) {
			handle(e);
		}
	}
	private void handle(Exception e) {
		setMessage(CVSUIPlugin.openError(shell, null, null, e, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS).getMessage());
	}
	/**
	 * Returns the historyTableProvider.
	 * @return HistoryTableProvider
	 */
	public HistoryTableProvider getHistoryTableProvider() {
		if (historyTableProvider == null) {
			historyTableProvider = new HistoryTableProvider();
		}
		return historyTableProvider;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
	}
	
	public void replaceLocalWithCurrentlySelectedRevision() throws CoreException {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (selection.size() != 1) return;
		VersionCompareDiffNode node = (VersionCompareDiffNode)selection.getFirstElement();
		ResourceRevisionNode right = (ResourceRevisionNode)node.getRight();
		TypedBufferedContent left = (TypedBufferedContent)node.getLeft();
		left.setContent(Utils.readBytes(right.getContents()));
	}
	
	public Viewer getViewer() {
		return viewer;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return Policy.bind("CVSCompareRevisionsInput.compareResourceAndVersions", new Object[] {resource.getFullPath().toString()}); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		try {
			saveChanges(monitor);
		} catch (CoreException e) {
			Utils.handle(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return isSaveNeeded();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		createContents(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getSite()
	 */
	public IWorkbenchPartSite getSite() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitleToolTip()
	 */
	public String getTitleToolTip() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#removePropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		// noop
	}
}
