package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Performs a catchup or release operation on an array of resources.
 */
public class SyncCompareInput extends CompareEditorInput implements ICompareInputChangeListener {
	private IRemoteSyncElement[] trees;
	private CatchupReleaseViewer catchupReleaseViewer;
	private DiffNode diffRoot;
	private Shell shell;
	private IViewSite viewSite;

	/**
	 * Creates a new catchup operation.  This constructor is invoked by subclasses
	 * that only support subscription, not releasing.
	 */
	protected SyncCompareInput(Shell shell, IRemoteSyncElement[] trees) {
		super(new CompareConfiguration());
		this.trees = trees;
		this.shell = shell;
	}
	
	/**
	 * Creates a new catchup or release operation.
	 */
	protected SyncCompareInput(IViewSite viewSite, IRemoteSyncElement[] trees) {
		super(new CompareConfiguration());
		this.trees = trees;
		this.shell = viewSite.getShell();
		this.viewSite = viewSite;
	}
	
	/*
	 * Method declared on ICompareInputChangeListener
	 */
	public void compareInputChanged(ICompareInput source) {
		catchupReleaseViewer.update(source, new String[] {CatchupReleaseViewer.PROP_KIND});
		updateStatusLine();
	}
	
	/**
	 * Returns true if the model contains the given resource, and false otherwise.
	 */
	private boolean containsResource(IResource resource) {
		String[] paths = resource.getFullPath().segments();
		IDiffElement element = diffRoot;
		i: for (int i = 0; i < paths.length; i++) {
			if (element instanceof IDiffContainer) {
				IDiffElement[] children = ((IDiffContainer)element).getChildren();
				for (int j = 0; j < children.length; j++) {
					if (children[j].getName().equals(paths[i])) {
						element = children[j];
						continue i;
					}
				}
			}
			return false;
		}
		return element != null;
	}
	
	/*
	 * @see CompareEditorInput#createContents
	 */
	public Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		initialSelectionAndExpansionState();
		return result;
	}

	/**
	 * Overridden to create a custom DiffTreeViewer in the top left pane of the CompareProvider.
	 */
	public Viewer createDiffViewer(Composite parent) {
		catchupReleaseViewer = new CatchupReleaseViewer(parent, this);
		return catchupReleaseViewer;
	}
	
	/**
	 * Returns the root node of the diff tree.
	 */
	public DiffNode getDiffRoot() {
		return diffRoot;
	}

	/**
	 * Returns the first diff element that is still unresolved in the
	 * subtree rooted at the given root element.
	 * Returns null if everything is resolved.
	 */
	private IDiffElement getFirstChange(IDiffElement root) {
		if (root instanceof ITeamNode) {
			ITeamNode node = (ITeamNode)root;
			if (node instanceof TeamFile) {
				return node;
			}
		}
		if (root instanceof IDiffContainer) {
			IDiffElement[] children = ((IDiffContainer)root).getChildren();
			IDiffElement result = null;
			for (int i = 0; i < children.length; i++) {
				result = getFirstChange(children[i]);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns an appropriate error message for the given severity.
	 * @see IStatus#getSeverity.
	 */
	private String getProblemMessage(int severity) {
		if (severity == IStatus.OK) {
			return Policy.bind("SyncCompareInput.ok");
		} else if (severity == IStatus.INFO) {
			return Policy.bind("SyncCompareInput.info");
		} else {
			return Policy.bind("SyncCompareInput.failure");
		}
	}

	/**
	 * Returns the title for any problem dialog that needs to popup
	 * during catchup/release.
	 */
	private String getProblemTitle() {
		return Policy.bind("SyncCompareInput.problemsDuringSync");
	}

	/**
	 * Returns the name of this operation.
	 * It is dipslayed in the CompareEditor's title bar.
	 */
	public String getTitle() {
		return Policy.bind("SyncCompareInput.synchronize");
	}

	/**
	 * Returns the compare viewer;
	 */
	public CatchupReleaseViewer getViewer() {
		return catchupReleaseViewer;
	}

	/**
	 * Returns the view site, or null if this is a merge.
	 */
	public IViewSite getViewSite() {
		return viewSite;
	}

	/**
	 * Returns true if the model has incoming or conflicting changes.
	 */
	boolean hasIncomingChanges() {
		if (diffRoot == null) {
			return false;
		}
		SyncSet set = new SyncSet(new StructuredSelection(diffRoot.getChildren()), 0);
		return set.hasIncomingChanges();
	}

	/**
	 * Set an appropriate initial selection and expansion state.
	 */
	private void initialSelectionAndExpansionState() {
		// Select the next change
		IDiffElement next = getFirstChange(diffRoot);
		if (next != null) {
			catchupReleaseViewer.setSelection(new StructuredSelection(next), true);
		} else {
			catchupReleaseViewer.collapseAll();
			catchupReleaseViewer.setSelection(new StructuredSelection());
		}
	}

	/**
	 * Returns true if we have unsaved changes.
	 */
	public boolean isSaveNeeded() {
		// All changes take effect immediately, so never need to save.
		return diffRoot.hasChildren();
	}

	/**
	 * Performs a catchup or release on the given set of ITeamNodes.  Returns the set
	 * of nodes that were actually loaded or released, or null if the user canceled.
	 */
	private SyncSet performSync(SyncSet syncSet, int kind, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		ITeamNode[] changed = syncSet.getChangedNodes();
		switch (kind) {
			case MergeAction.CHECKIN:
				for (int i = 0; i < changed.length; i++) {
					ITeamProvider provider = TeamPlugin.getManager().getProvider(changed[i].getResource().getProject());
					try {
						provider.checkin(new IResource[] {changed[i].getResource()}, IResource.DEPTH_INFINITE, new NullProgressMonitor());
					} catch (TeamException e) {
						// remove the change from the set, add an error
					}
				}
				break;
			case MergeAction.GET:
				for (int i = 0; i < changed.length; i++) {
					ITeamProvider provider = TeamPlugin.getManager().getProvider(changed[i].getResource().getProject());
					try {
						provider.get(new IResource[] {changed[i].getResource()}, IResource.DEPTH_INFINITE, new NullProgressMonitor());
					} catch (TeamException e) {
						// remove the change from the set, add an error
					}
				}
				break;
			case MergeAction.DELETE_REMOTE:
				for (int i = 0; i < changed.length; i++) {
					ITeamProvider provider = TeamPlugin.getManager().getProvider(changed[i].getResource().getProject());
					try {
						provider.delete(new IResource[] {changed[i].getResource()}, new NullProgressMonitor());
					} catch (TeamException e) {
						// remove the change from the set, add an error
					}
				}		
				break;
			case MergeAction.DELETE_LOCAL:
				for (int i = 0; i < changed.length; i++) {
					changed[i].getResource().delete(false, new NullProgressMonitor());
				}		
				break;
		}
		
		//display low-level warnings, if any.
	/*	if (!result.isOK()) {
			if (shell != null && !shell.isDisposed()) {
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						ErrorDialog.openError(shell, 
							Policy.bind("SyncCompareInput.problemsDuringSync"),
							Policy.bind("SyncCompareInput.info"),
							result);
					}
				});
			}
		}*/
		if (monitor.isCanceled()) {
			return null;
		}
		return syncSet;
	}

	/**
	 * Performs a compare on the given selection.
	 * This method is called before the CompareEditor has been opened.
	 * If the result of the diff is empty (or an error has occured)
	 * no CompareEditor is opened but an Alert is shown.
	 */
	public Object prepareInput(final IProgressMonitor pm) throws InterruptedException, InvocationTargetException {
		if (pm.isCanceled()) {
			throw new InterruptedException();
		}
	
		try {
			setMessage(null);
			if (trees.length == 0) {
				return null;
			}
			final InterruptedException[] exceptions = new InterruptedException[1];
			
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// collect changes and build the diff tree
					diffRoot = new DiffNode(0);
					try {
						doServerDelta(pm);
					} catch (InterruptedException e) {
						exceptions[0] = e;
					}
				}
			};
			ResourcesPlugin.getWorkspace().run(runnable, null);
			if (exceptions[0] != null) throw exceptions[0];
			if (pm.isCanceled()) {
				throw new InterruptedException();
			}
				
			if (!diffRoot.hasChildren()) {
				diffRoot = null;
			}
	
			updateStatusLine();
				
			return diffRoot;
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	void doServerDelta(IProgressMonitor pm) throws InterruptedException {
		for (int i = 0; i < trees.length; i++) {
			IRemoteSyncElement tree = trees[i];
			IDiffElement localRoot = collectResourceChanges(null, tree, new NullProgressMonitor());
			makeParents(localRoot);
		}
	}
	
	IDiffElement collectResourceChanges(IDiffContainer parent, IRemoteSyncElement tree, IProgressMonitor pm) {
		int type = tree.getSyncKind(IRemoteSyncElement.GRANULARITY_TIMESTAMP, new NullProgressMonitor());
		MergeResource mergeResource = new MergeResource(tree);
	
		if (tree.isContainer()) {
			IDiffContainer element = new ChangedTeamContainer(this, parent, mergeResource, type);
			try {
				ILocalSyncElement[] children = tree.members(pm);
				for (int i = 0; i < children.length; i++) {
					collectResourceChanges(element, (IRemoteSyncElement)children[i], pm);
				}
			} catch (TeamException e) {
				TeamUIPlugin.log(e.getStatus());
			}
			return element;
		} else {
			TeamFile file = new TeamFile(parent, mergeResource, type);
			file.addCompareInputChangeListener(this);
			return file;
		}
	}
	
	void makeParents(IDiffElement element) {
		ITeamNode node = (ITeamNode)element;
	
		IResource resource = node.getResource().getParent();
		while (resource.getType() != IResource.ROOT) {
			UnchangedTeamContainer container = new UnchangedTeamContainer(this, null, resource);	
			if (diffRoot.findChild(container.getName()) == null) {
				container.add(node);
			}
			node = container;
			resource = resource.getParent();
		}
		diffRoot.add(node);
	}
	
	/**
	 * Performs a refresh, with progress and cancelation.
	 */
	public void refresh() {
		final Object[] input = new Object[1];
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				input[0] = prepareInput(monitor);
			}
		};
		try {
			run(op, Policy.bind("SyncCompareInput.refresh"));
		} catch (InterruptedException e) {
			return;
		}
		
		catchupReleaseViewer.setInput(input[0]);
		if (input[0] == null) {
			MessageDialog.openInformation(shell, Policy.bind("nothingToSynchronize"), Policy.bind("SyncCompareInput.nothingText"));
		}
	}
	
	/**
	 * The given nodes have been synchronized.  Remove them from
	 * the view.
	 */
	private void removeNodes(final ITeamNode[] nodes) {
		// Update the model
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getClass() == UnchangedTeamContainer.class) {
				// Unchanged containers get removed automatically when all
				// children are removed
				continue;
			}
			if (nodes[i].getClass() == ChangedTeamContainer.class) {
				// If this node still has children, convert to an
				// unchanged container, then it will disappear when
				// all children have been removed.
				ChangedTeamContainer container = (ChangedTeamContainer)nodes[i];
				IDiffElement[] children = container.getChildren();
				if (children.length > 0) {
					IDiffContainer parent = container.getParent();
					parent.removeToRoot(container);
					UnchangedTeamContainer unchanged = new UnchangedTeamContainer(this, parent, container.getResource());
					for (int j = 0; j < children.length; j++) {
						unchanged.add(children[j]);
					}
					continue;
				}
				// No children, it will get removed below.
			}
			nodes[i].getParent().removeToRoot(nodes[i]);
			
		}
		
		// Update the view
		if (diffRoot.hasChildren()) {
			catchupReleaseViewer.refresh();
		} else {
			catchupReleaseViewer.setInput(null);
		}
		
		// Update the status line
		updateStatusLine();
	}
	
	private void run(IRunnableWithProgress op, String problemMessage) throws InterruptedException {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, op);
		} catch (InvocationTargetException e) {
			Throwable throwable = e.getTargetException();
			IStatus error = null;
			if (throwable instanceof CoreException) {
				error = ((CoreException)throwable).getStatus();
			} else {
				error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("simpleInternal") , throwable);
			}
			ErrorDialog.openError(shell, problemMessage, null, error);
			TeamUIPlugin.log(error);
		}
	}

	/**
	 * Peforms a catchup or release on the given set of nodes.
	 */
	void sync(final SyncSet nodes, final int kind) {
		final SyncSet[] result = new SyncSet[1];
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					result[0] = performSync(nodes, kind, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			run(op, getProblemTitle());
		} catch (InterruptedException e) {
		}
		if (result[0] != null) {
			removeNodes(result[0].getChangedNodes());
		}
	}

	/**
	 * Updates the status line.
	 */
	private void updateStatusLine() {
		if (viewSite != null && !shell.isDisposed()) {
			Runnable update = new Runnable() {
				public void run() {
					if (!shell.isDisposed()) {
						IStatusLineManager statusLine = viewSite.getActionBars().getStatusLineManager();
						if (diffRoot == null) {
							statusLine.setMessage(null);
							statusLine.setErrorMessage(null);
							return;
						}
						SyncSet set = new SyncSet(new StructuredSelection(diffRoot.getChildren()), 0);
						if (set.hasConflicts()) {
							statusLine.setMessage(null);
							statusLine.setErrorMessage(set.getStatusLineMessage());
						} else {
							statusLine.setErrorMessage(null);
							statusLine.setMessage(set.getStatusLineMessage());
						}
						viewSite.getActionBars().updateActionBars();
					}
				}
			};
			// Post or run the update
			if (shell.getDisplay() != Display.getCurrent()) {
				shell.getDisplay().asyncExec(update);
			} else {
				update.run();
			}
		}
	}
}
