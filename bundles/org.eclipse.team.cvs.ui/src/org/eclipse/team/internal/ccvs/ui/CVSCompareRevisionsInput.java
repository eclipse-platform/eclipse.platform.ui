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
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;

public class CVSCompareRevisionsInput extends CompareEditorInput {
	IFile resource;
	ILogEntry[] editions;
	TableViewer viewer;
	Action getContentsAction;
	Action getRevisionAction;
	Shell shell;
	
	private HistoryTableProvider historyTableProvider;
	
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
			ICVSRemoteResource edition = getRemoteResource();
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
	};
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

	};
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
	};
	
	public CVSCompareRevisionsInput(IFile resource, ILogEntry[] editions) {
		super(new CompareConfiguration());
		this.resource = resource;
		this.editions = editions;
		updateCurrentEdition();
		initializeActions();
	}

	public Viewer createDiffViewer(Composite parent) {
		this.shell = parent.getShell();
		viewer = getHistoryTableProvider().createTable(parent);
		Table table = viewer.getTable();
		table.setData(CompareUI.COMPARE_VIEWER_TITLE, Policy.bind("CVSCompareRevisionsInput.structureCompare")); //$NON-NLS-1$
	
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
					getContentsAction.setEnabled(false);
					getRevisionAction.setEnabled(false);
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				getContentsAction.setEnabled(ss.size() == 1);
				getRevisionAction.setEnabled(ss.size() == 1);
			}	
		});
		
		// Add F1 help.
		WorkbenchHelp.setHelp(table, IHelpContextIds.COMPARE_REVISIONS_VIEW);
		WorkbenchHelp.setHelp(getContentsAction, IHelpContextIds.GET_FILE_CONTENTS_ACTION);
		WorkbenchHelp.setHelp(getRevisionAction, IHelpContextIds.GET_FILE_REVISION_ACTION);
		
		return viewer;
	}

	private void initLabels() {
		CompareConfiguration cc = (CompareConfiguration)getCompareConfiguration();
		String resourceName = resource.getName();	
//		if (editions[0].isTeamStreamResource()) {
//			setTitle(Policy.bind("CompareResourceEditorInput.compareResourceAndStream", new Object[] {resourceName, editions[0].getTeamStream().getName()}));
//		} else {
//			setTitle(Policy.bind("CompareResourceEditorInput.compareResourceAndVersions", new Object[] {resourceName}));
//		}
		setTitle(Policy.bind("CVSCompareRevisionsInput.compareResourceAndVersions", new Object[] {resourceName})); //$NON-NLS-1$
		cc.setLeftEditable(true);
		cc.setRightEditable(false);
		
		String leftLabel = Policy.bind("CVSCompareRevisionsInput.workspace", new Object[] {resourceName}); //$NON-NLS-1$
		cc.setLeftLabel(leftLabel);
		String rightLabel = Policy.bind("CVSCompareRevisionsInput.repository", new Object[] {resourceName}); //$NON-NLS-1$
		cc.setRightLabel(rightLabel);
	}
	private void initializeActions() {
		getContentsAction = new Action(Policy.bind("HistoryView.getContentsAction"), null) { //$NON-NLS-1$
			public void run() {
				try {
					new ProgressMonitorDialog(shell).run(false, true, new WorkspaceModifyOperation() {
						protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
							if (selection.size() != 1) return;
							VersionCompareDiffNode node = (VersionCompareDiffNode)selection.getFirstElement();
							ResourceEditionNode right = (ResourceEditionNode)node.getRight();
							ICVSRemoteResource edition = right.getRemoteResource();
							// Do the load. This just consists of setting the local contents. We don't
							// actually want to change the base.
							try {
								monitor.beginTask(null, 100);
								InputStream in = edition.getContents(new SubProgressMonitor(monitor, 50));
								resource.setContents(in, false, true, new SubProgressMonitor(monitor, 50));
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							} finally {
								monitor.done();
							}
						}
					});
				} catch (InterruptedException e) {
					// Do nothing
					return;
				} catch (InvocationTargetException e) {
					handle(e);
				}
				// recompute the labels on the viewer
				updateCurrentEdition();
				viewer.refresh();
			}
		};
		
		getRevisionAction = new Action(Policy.bind("HistoryView.getRevisionAction"), null) { //$NON-NLS-1$
			public void run() {
				try {
					new ProgressMonitorDialog(shell).run(false, true, new WorkspaceModifyOperation() {
						protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
							if (selection.size() != 1) return;
							VersionCompareDiffNode node = (VersionCompareDiffNode)selection.getFirstElement();
							ResourceEditionNode right = (ResourceEditionNode)node.getRight();
							ICVSRemoteResource edition = right.getRemoteResource();
							// Do the load. This just consists of setting the local contents. We don't
							// actually want to change the base.
							try {
								CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resource.getProject());
								CVSTag revisionTag = new CVSTag(((ICVSRemoteFile)edition).getRevision(), CVSTag.VERSION);
								if(CVSAction.checkForMixingTags(shell, new IResource[] {resource}, revisionTag)) {							
									provider.update(new IResource[] {resource}, new Command.LocalOption[] {Update.IGNORE_LOCAL_CHANGES}, 
								 				    revisionTag, true /*create backups*/, monitor);
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
				// recompute the labels on the viewer
				viewer.refresh();
			}
		};
	}
	public boolean isSaveNeeded() {
		return false;
	}
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		DiffNode diffRoot = new DiffNode(Differencer.NO_CHANGE);
		for (int i = 0; i < editions.length; i++) {		
			ITypedElement left = new TypedBufferedContent(resource);
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

}
