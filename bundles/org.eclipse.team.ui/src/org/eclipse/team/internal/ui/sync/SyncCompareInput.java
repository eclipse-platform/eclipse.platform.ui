package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IViewSite;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * Performs a catchup or release operation on an array of resources.
 */
public abstract class SyncCompareInput extends CompareEditorInput {
	private IRemoteSyncElement[] trees;
	private CatchupReleaseViewer catchupReleaseViewer;
	private DiffNode diffRoot;
	private Shell shell;
	private IViewSite viewSite;
	private int granularity;
	
	private ICompareInputChangeListener listener = new ICompareInputChangeListener() {
		public void compareInputChanged(ICompareInput source) {
			SyncCompareInput.this.compareInputChanged(source);
		}
	};

	/**
	 * Subclasses may override but must call super.
	 */
	protected void compareInputChanged(ICompareInput source) {
		catchupReleaseViewer.update(source, new String[] {CatchupReleaseViewer.PROP_KIND});
		updateStatusLine();
	}
	
	/**
	 * Creates a new catchup or release operation.
	 */
	public SyncCompareInput(int granularity) {
		super(new CompareConfiguration());
		this.granularity = granularity;
	}
	
	/**
	 * Creates a new catchup or release operation.
	 */
	public SyncCompareInput() {
		this(ILocalSyncElement.GRANULARITY_TIMESTAMP);
	}

	protected abstract IRemoteSyncElement[] createSyncElements(IProgressMonitor monitor) throws TeamException;
	
	/*
	 * @see CompareEditorInput#createContents
	 */
	public Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		initialSelectionAndExpansionState();
		return result;
	}

	/**
	 * Subclasses must create and return a new CatchupReleaseViewer, and set the viewer
	 * using setViewer().
	 */
	public abstract Viewer createDiffViewer(Composite parent);
	
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

	protected Shell getShell() {
		return shell;
	}
	
	/**
	 * Returns the name of this operation.
	 * It is dipslayed in the CompareEditor's title bar.
	 */
	public String getTitle() {
		return Policy.bind("SyncCompareInput.synchronize"); //$NON-NLS-1$
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
		SyncSet set = new SyncSet(new StructuredSelection(diffRoot.getChildren()));
		return set.hasIncomingChanges() || set.hasConflicts();
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
			pm.beginTask(Policy.bind("SyncCompareInput.taskTitle"), 100); //$NON-NLS-1$
			
			// Estimate 70% of the time is creating the sync elements
			this.trees = createSyncElements(Policy.subMonitorFor(pm, 70));
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
						doServerDelta(monitor);
					} catch (InterruptedException e) {
						exceptions[0] = e;
					}
				}
			};
			if (pm.isCanceled()) {
				throw new InterruptedException();
			}
			// Estimate 30% of the time is doing the server delta
			ResourcesPlugin.getWorkspace().run(runnable, Policy.subMonitorFor(pm, 30));
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
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	void doServerDelta(IProgressMonitor pm) throws InterruptedException {
		pm.beginTask(null, trees.length * 1000);
		pm.setTaskName(Policy.bind("SyncCompareInput.taskTitle")); //$NON-NLS-1$
		for (int i = 0; i < trees.length; i++) {
			IRemoteSyncElement tree = trees[i];
			IProgressMonitor monitor = Policy.subMonitorFor(pm, 1000);
			monitor.beginTask(null, 1000);
			IDiffElement localRoot = collectResourceChanges(null, tree, monitor);
			monitor.done();
			makeParents(localRoot);
		}
	}
	
	/*
	 * This method expects to be past a monitor that has already had it's beginTask invoked
	 * and has enough ticks to allow 1 unit of work per resource in the tree and an additional
	 * unit for each folder.
	 */
	protected IDiffElement collectResourceChanges(IDiffContainer parent, IRemoteSyncElement tree, IProgressMonitor pm) {
		int type = tree.getSyncKind(getSyncGranularity(), Policy.subMonitorFor(pm, 1));
		MergeResource mergeResource = new MergeResource(tree);
	
		if (tree.isContainer()) {
			IDiffContainer element = new ChangedTeamContainer(parent, mergeResource, type);
			try {				
				ILocalSyncElement[] children = tree.members(Policy.subMonitorFor(pm, 1));
				for (int i = 0; i < children.length; i++) {
					collectResourceChanges(element, (IRemoteSyncElement)children[i], pm);
				}
			} catch (TeamException e) {
				TeamUIPlugin.log(e.getStatus());
			}
			return element;
		} else {
			TeamFile file = new TeamFile(parent, mergeResource, type, shell);
			file.addCompareInputChangeListener(listener);
			return file;
		}
	}
	
	public int getSyncGranularity() {
		return granularity;
	}
	
	public void setSyncGranularity(int granularity) {
		this.granularity = granularity;
		refresh();
	}

	
	/**
	 * Builds a DiffFolder tree under the given root for the given resource.
	 */
	private DiffContainer buildPath(DiffContainer root, IContainer resource) {
		DiffContainer parent = root;
		if (resource.getType() == IResource.ROOT) {
			return root;
		}
		if (resource.getType() != IResource.PROJECT) {
			parent = buildPath(root, resource.getParent());
		}
	
		DiffContainer c = (DiffContainer)parent.findChild(resource.getName());
		if (c == null) {
			c = new UnchangedTeamContainer(parent, resource);
		}
		return c;
	}
	
	void makeParents(IDiffElement element) {
		IContainer parent = ((ITeamNode)element).getResource().getParent();
		DiffContainer container = buildPath(diffRoot, parent);
		container.add(element);
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
			run(op, Policy.bind("SyncCompareInput.refresh")); //$NON-NLS-1$
		} catch (InterruptedException e) {
			return;
		}
		
		catchupReleaseViewer.setInput(input[0]);
		if (input[0] == null) {
			MessageDialog.openInformation(shell, Policy.bind("nothingToSynchronize"), Policy.bind("SyncCompareInput.nothingText")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void run(IRunnableWithProgress op, String problemMessage) throws InterruptedException {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, op);
		} catch (InvocationTargetException e) {
			Throwable throwable = e.getTargetException();
			IStatus error = null;
			if (throwable instanceof CoreException) {
				error = ((CoreException)throwable).getStatus();
			} else {
				error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("simpleInternal") , throwable); //$NON-NLS-1$
			}
			ErrorDialog.openError(shell, problemMessage, error.getMessage(), error);
			TeamUIPlugin.log(error);
		}
	}

	public void setViewSite(IViewSite viewSite) {
		this.viewSite = viewSite;
		this.shell = viewSite.getShell();
	}

	public void setViewer(CatchupReleaseViewer viewer) {
		this.catchupReleaseViewer = viewer;
	}

	/**
	 * Updates the status line.
	 */
	public void updateStatusLine() {
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
						SyncSet set = new SyncSet(new StructuredSelection(diffRoot.getChildren()));
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
	
	public boolean saveIfNecessary() {
		if (! isSaveNeeded()) return true;
		
		final boolean[] result = new boolean[] { false };
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					result[0] = MessageDialog.openQuestion(getShell(), Policy.bind("SyncView.saveTitle"),//$NON-NLS-1$
						Policy.bind("SyncView.saveMessage"));//$NON-NLS-1$
					if (result[0]) {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								saveChanges(monitor);
							}
						}, new NullProgressMonitor());
					}
				} catch (CoreException e) {
					IStatus status = e.getStatus();
					ErrorDialog.openError(getShell(), status.getMessage(), Policy.bind("SyncView.errorSaving"), status);//$NON-NLS-1$
					result[0] = false;
				}
			}
		});
		return result[0];
	}

	/*	
	 * HACK until Compare fixes dirtyness PR#14378
	 */
	public void saveChanges(IProgressMonitor monitor) throws CoreException {
		super.saveChanges(monitor);
		setDirty(false);
	}
}
