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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.AndSyncInfoFilter;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.RemoteLogOperation;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.progress.UIJob;

/**
 * Disclamer:
 * This is a prototype layout using *internal* team classes. It is not meant
 * to be an example or sanctioned use of team. These classes and the classes
 * references here may change or be deleted in the future.
 * 
 * This provider groups changes into commit sets and fetches the log history for
 * files in the background. Changes that can't be grouped into commit sets (e.g. outgoing 
 * changes) are shown in a flat list.
 * 
 * @since 3.0
 */
public class ChangeLogModelProvider extends CompositeModelProvider implements ICommitSetChangeListener {
	// Log operation that is used to fetch revision histories from the server. It also
	// provides caching so we keep it around.
	private RemoteLogOperation logOperation;
	
	// Job that builds the layout in the background.
	private boolean shutdown = false;
	private FetchLogEntriesJob fetchLogEntriesJob;
	
	// Tag ranges for fetching revision histories. If no tags are specified then
	// the history for the remote revision in the sync info is used.
	private CVSTag tag1;
	private CVSTag tag2;
	
	// The id of the sub-provider
	private final String id;
	
	private Set queuedAdditions = new HashSet(); // Set of SyncInfo
	
	private Map rootToProvider = new HashMap(); // Maps ISynchronizeModelElement -> AbstractSynchronizeModelProvider
	
	private int sortCriteria = ChangeLogModelSorter.DATE;
	
	private ViewerSorter embeddedSorter;
	
	// Constants for persisting sorting options
	private final static String COMMIT_SET_GROUP = "commit_set"; //$NON-NLS-1$
	private static final String P_LAST_COMMENTSORT = TeamUIPlugin.ID + ".P_LAST_COMMENT_SORT"; //$NON-NLS-1$
	
    public static final AndSyncInfoFilter OUTGOING_FILE_FILTER = new AndSyncInfoFilter(new FastSyncInfoFilter[] {
            new FastSyncInfoFilter() {
                public boolean select(SyncInfo info) {
                    return info.getLocal().getType() == IResource.FILE;
                }
            },
            new SyncInfoDirectionFilter(new int[] { SyncInfo.OUTGOING, SyncInfo.CONFLICTING })
    });
	
	/* *****************************************************************************
	 * Action that allows changing the model providers sort order.
	 */
	private class ToggleSortOrderAction extends Action {
		private int criteria;
		protected ToggleSortOrderAction(String name, int criteria) {
			super(name, Action.AS_RADIO_BUTTON);
			this.criteria = criteria;
			update();		
		}

		public void run() {
			if (isChecked() && sortCriteria != criteria) {
			    sortCriteria = criteria;
				String key = getSettingsKey();
				IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
				if(pageSettings != null) {
					pageSettings.put(key, criteria);
				}
				update();
				ChangeLogModelProvider.this.firePropertyChange(P_VIEWER_SORTER, null, null);
			}
		}
		
		public void update() {
		    setChecked(criteria == sortCriteria);
		}
		
		protected String getSettingsKey() {
		    return P_LAST_COMMENTSORT;
		}
	}

	private class CreateCommitSetAction extends SynchronizeModelAction {
	    
        public CreateCommitSetAction(ISynchronizePageConfiguration configuration) {
            super(Policy.bind("ChangeLogModelProvider.0"), configuration); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSyncInfoFilter()
         */
        protected FastSyncInfoFilter getSyncInfoFilter() {
            return OUTGOING_FILE_FILTER;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSubscriberOperation(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration, org.eclipse.compare.structuremergeviewer.IDiffElement[])
         */
        protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
            return new SynchronizeModelOperation(configuration, elements) {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    syncExec(new Runnable() {
                        public void run() {
                            try {
                                IResource[] resources = Utils.getResources(getSelectedDiffElements());
                                CommitSet set = CommitSetManager.getInstance().createCommitSet(Policy.bind("ChangeLogModelProvider.1"), null); //$NON-NLS-1$
                        		CommitSetDialog dialog = new CommitSetDialog(getConfiguration().getSite().getShell(), set, resources,
                        		        Policy.bind("ChangeLogModelProvider.2"), Policy.bind("ChangeLogModelProvider.3")); //$NON-NLS-1$ //$NON-NLS-2$
                        		dialog.open();
                        		if (dialog.getReturnCode() != InputDialog.OK) return;
                        		set.addFiles(resources);
                	            CommitSetManager.getInstance().add(set);
                            } catch (CVSException e) {
                                CVSUIPlugin.openError(getConfiguration().getSite().getShell(),
                                        Policy.bind("ChangeLogModelProvider.4"), Policy.bind("ChangeLogModelProvider.5"), e); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    });
                }
            };
        }
	}

	private abstract class CommitSetAction extends BaseSelectionListenerAction {
        private final ISynchronizePageConfiguration configuration;

        public CommitSetAction(String title, ISynchronizePageConfiguration configuration) {
            super(title);
            this.configuration = configuration;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
         */
        protected boolean updateSelection(IStructuredSelection selection) {
            return getSelectedSet() != null;
        }

        protected CommitSet getSelectedSet() {
            IStructuredSelection selection = getStructuredSelection();
            if (selection.size() == 1) {
                Object first = selection.getFirstElement();
                if (first instanceof CommitSetDiffNode) {
                    return ((CommitSetDiffNode)first).getSet();
                }
            }
            return null;
        }
	}
	
	private class EditCommitSetAction extends CommitSetAction {

        public EditCommitSetAction(ISynchronizePageConfiguration configuration) {
            super(Policy.bind("ChangeLogModelProvider.6"), configuration); //$NON-NLS-1$
        }
        
        public void run() {
            CommitSet set = getSelectedSet();
            if (set == null) return;
    		CommitSetDialog dialog = new CommitSetDialog(getConfiguration().getSite().getShell(), set, set.getFiles(),
    		        Policy.bind("ChangeLogModelProvider.7"), Policy.bind("ChangeLogModelProvider.8")); //$NON-NLS-1$ //$NON-NLS-2$
    		dialog.open();
    		if (dialog.getReturnCode() != InputDialog.OK) return;
    		// Nothing to do here as the set was updated by the dialog
        }
        
	}
	
	private class MakeDefaultCommitSetAction extends CommitSetAction {

        public MakeDefaultCommitSetAction(ISynchronizePageConfiguration configuration) {
            super(Policy.bind("ChangeLogModelProvider.9"), configuration); //$NON-NLS-1$
        }
        
        public void run() {
            CommitSet set = getSelectedSet();
            if (set == null) return;
    		CommitSetManager.getInstance().makeDefault(set);
        }
	    
	}
	
	private class AddToCommitSetAction extends SynchronizeModelAction {
	 
        private final CommitSet set;
	    
        public AddToCommitSetAction(ISynchronizePageConfiguration configuration, CommitSet set, ISelection selection) {
            super(set.getTitle(), configuration);
            this.set = set;
            selectionChanged(selection);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSyncInfoFilter()
         */
        protected FastSyncInfoFilter getSyncInfoFilter() {
            return OUTGOING_FILE_FILTER;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSubscriberOperation(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration, org.eclipse.compare.structuremergeviewer.IDiffElement[])
         */
        protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
            return new SynchronizeModelOperation(configuration, elements) {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    try {
                        set.addFiles(Utils.getResources(getSelectedDiffElements()));
                    } catch (CVSException e) {
                        CVSUIPlugin.openError(getConfiguration().getSite().getShell(),
                                Policy.bind("ChangeLogModelProvider.10"), Policy.bind("ChangeLogModelProvider.11"), e); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            };
        }
	}
	
	/* *****************************************************************************
	 * Action group for this layout. It is added and removed for this layout only.
	 */
	public class ChangeLogActionGroup extends SynchronizePageActionGroup {
		private MenuManager sortByComment;
		private CreateCommitSetAction createCommitSet;
		private MenuManager addToCommitSet;
        private EditCommitSetAction editCommitSet;
        private MakeDefaultCommitSetAction makeDefault;
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			sortByComment = new MenuManager(Policy.bind("ChangeLogModelProvider.0"));	 //$NON-NLS-1$
			addToCommitSet = new MenuManager(Policy.bind("ChangeLogModelProvider.12")); //$NON-NLS-1$
			addToCommitSet.setRemoveAllWhenShown(true);
			addToCommitSet.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    addCommitSets(manager);
                }
            });
			createCommitSet = new CreateCommitSetAction(configuration);
			addToCommitSet.add(createCommitSet);
			addToCommitSet.add(new Separator());
			editCommitSet = new EditCommitSetAction(configuration);
			makeDefault = new MakeDefaultCommitSetAction(configuration);
			
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					ISynchronizePageConfiguration.SORT_GROUP, 
					sortByComment);
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					COMMIT_SET_GROUP, 
					addToCommitSet);
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					COMMIT_SET_GROUP, 
					editCommitSet);
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					COMMIT_SET_GROUP, 
					makeDefault);
			
			ChangeLogModelProvider.this.initialize(configuration);
			
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.1"), ChangeLogModelSorter.COMMENT)); //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.2"), ChangeLogModelSorter.DATE)); //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.3"), ChangeLogModelSorter.USER)); //$NON-NLS-1$
		}
		
        protected void addCommitSets(IMenuManager manager) {
            CommitSet[] sets = CommitSetManager.getInstance().getSets();
            ISelection selection = getContext().getSelection();
            createCommitSet.selectionChanged(selection);
			addToCommitSet.add(createCommitSet);
			addToCommitSet.add(new Separator());
            for (int i = 0; i < sets.length; i++) {
                CommitSet set = sets[i];
                AddToCommitSetAction action = new AddToCommitSetAction(getConfiguration(), set, selection);
                manager.add(action);
            }
        }

        /* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#dispose()
		 */
		public void dispose() {
			sortByComment.dispose();
			addToCommitSet.dispose();
			sortByComment.removeAll();
			addToCommitSet.removeAll();
			super.dispose();
		}
		
		
        public void updateActionBars() {
            editCommitSet.selectionChanged((IStructuredSelection)getContext().getSelection());
            makeDefault.selectionChanged((IStructuredSelection)getContext().getSelection());
            super.updateActionBars();
        }
	}
	
	/* *****************************************************************************
	 * Special sync info that has its kind already calculated.
	 */
	public class CVSUpdatableSyncInfo extends CVSSyncInfo {
		public int kind;
		public CVSUpdatableSyncInfo(int kind, IResource local, IResourceVariant base, IResourceVariant remote, Subscriber s) {
			super(local, base, remote, s);
			this.kind = kind;
		}

		protected int calculateKind() throws TeamException {
			return kind;
		}
	}
	
	/* *****************************************************************************
	 * Action group for this layout. It is added and removed for this layout only.
	 */
	
	private class FetchLogEntriesJob extends Job {
		private Set syncSets = new HashSet();
		public FetchLogEntriesJob() {
			super(Policy.bind("ChangeLogModelProvider.4"));  //$NON-NLS-1$
			setUser(false);
		}
		public boolean belongsTo(Object family) {
			return family == ISynchronizeManager.FAMILY_SYNCHRONIZE_OPERATION;
		}
		public IStatus run(IProgressMonitor monitor) {
			
				if (syncSets != null && !shutdown) {
					// Determine the sync sets for which to fetch comment nodes
					SyncInfoSet[] updates;
					synchronized (syncSets) {
						updates = (SyncInfoSet[]) syncSets.toArray(new SyncInfoSet[syncSets.size()]);
						syncSets.clear();
					}
					for (int i = 0; i < updates.length; i++) {
						calculateRoots(updates[i], monitor);
					}
					refreshViewer();
				}
				return Status.OK_STATUS;
		
		}
		public void add(SyncInfoSet set) {
			synchronized(syncSets) {
				syncSets.add(set);
			}
			schedule();
		}
		public boolean shouldRun() {
			return !syncSets.isEmpty();
		}
	};
	
	/* *****************************************************************************
	 * Descriptor for this model provider
	 */
	public static class ChangeLogModelProviderDescriptor implements ISynchronizeModelProviderDescriptor {
		public static final String ID = TeamUIPlugin.ID + ".modelprovider_cvs_changelog"; //$NON-NLS-1$
		public String getId() {
			return ID;
		}		
		public String getName() {
			return Policy.bind("ChangeLogModelProvider.5"); //$NON-NLS-1$
		}		
		public ImageDescriptor getImageDescriptor() {
			return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_CHANGELOG);
		}
	};
	private static final ChangeLogModelProviderDescriptor descriptor = new ChangeLogModelProviderDescriptor();
	
	public ChangeLogModelProvider(ISynchronizePageConfiguration configuration, SyncInfoSet set, CVSTag tag1, CVSTag tag2, String id) {
		super(configuration, set);
		this.tag1 = tag1;
		this.tag2 = tag2;
        this.id = id;
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, COMMIT_SET_GROUP);
		if (configuration.getComparisonType() == ISynchronizePageConfiguration.THREE_WAY) {
		    CommitSetManager.getInstance().addListener(this);
		}
		initialize(configuration);
	}
	
    private void initialize(ISynchronizePageConfiguration configuration) {
		try {
			IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
			if(pageSettings != null) {
				sortCriteria = pageSettings.getInt(P_LAST_COMMENTSORT);
			}
		} catch(NumberFormatException e) {
			// ignore and use the defaults.
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#createActionGroup()
     */
    protected SynchronizePageActionGroup createActionGroup() {
        return new ChangeLogActionGroup();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.ISynchronizeModelProvider#getDescriptor()
	 */
	public ISynchronizeModelProviderDescriptor getDescriptor() {
		return descriptor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.HierarchicalModelProvider#buildModelObjects(org.eclipse.compare.structuremergeviewer.DiffNode)
	 */
	protected IDiffElement[] buildModelObjects(ISynchronizeModelElement node) {
		if (node == getModelRoot()) {
			// Cancel any existing fetching jobs
			try {
				if (fetchLogEntriesJob != null && fetchLogEntriesJob.getState() != Job.NONE) {
					fetchLogEntriesJob.cancel();
					fetchLogEntriesJob.join();
				}
			} catch (InterruptedException e) {
			}

			// Start building the model from scratch
			startUpdateJob(getSyncInfoSet());
		}
		return new IDiffElement[0];
	}

	private void startUpdateJob(SyncInfoSet set) {
		if(fetchLogEntriesJob == null) {
			fetchLogEntriesJob = new FetchLogEntriesJob();
		}
		fetchLogEntriesJob.add(set);
	}
	
	private void refreshViewer() {
		UIJob updateUI = new UIJob("") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				BusyIndicator.showWhile(getDisplay(), new Runnable() {
					public void run() {
						StructuredViewer tree = getViewer();	
						tree.refresh();
						ISynchronizeModelElement root = getModelRoot();
						if(root instanceof SynchronizeModelElement)
							((SynchronizeModelElement)root).fireChanges();
					}
				});

				return Status.OK_STATUS;
			}
		};
		updateUI.setSystem(true);
		updateUI.schedule();
	}
	
	private void calculateRoots(SyncInfoSet set, IProgressMonitor monitor) {
		try {
			monitor.beginTask(null, 100);
			// Decide which nodes we have to fetch log histories
			SyncInfo[] infos = set.getSyncInfos();
			ArrayList remoteChanges = new ArrayList();
			ArrayList localChanges = new ArrayList();
			for (int i = 0; i < infos.length; i++) {
				SyncInfo info = infos[i];
				boolean handled = false;
				if(isRemoteChange(info)) {
					remoteChanges.add(info);
					handled = true;
				}
				if (isLocalChange(info) || !handled) {
					localChanges.add(info);
				}
			}	
			handleLocalChanges((SyncInfo[]) localChanges.toArray(new SyncInfo[localChanges.size()]), monitor);
			handleRemoteChanges((SyncInfo[]) remoteChanges.toArray(new SyncInfo[remoteChanges.size()]), monitor);
		} catch (CVSException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Fetch the log histories for the remote changes and use this information
	 * to add each resource to an appropriate commit set.
     */
    private void handleRemoteChanges(final SyncInfo[] infos, final IProgressMonitor monitor) throws CVSException, InterruptedException {
        final RemoteLogOperation logs = getSyncInfoComment(infos, Policy.subMonitorFor(monitor, 80));
        runViewUpdate(new Runnable() {
            public void run() {
                addLogEntries(infos, logs, Policy.subMonitorFor(monitor, 10));
            }
        }, monitor);
    }

    /**
     * Use the commit set manager to determine the commit set that each local
     * change belongs to.
     */
    private void handleLocalChanges(final SyncInfo[] infos, IProgressMonitor monitor) {
        runViewUpdate(new Runnable() {
            public void run() {
    	        if (infos.length != 0) {
    		        // Show elements that don't need their log histories retrieved
    		        for (int i = 0; i < infos.length; i++) {
    		            SyncInfo info = infos[i];
    		            addLocalChange(info);
    		        }
    		        //refreshViewer(); // TODO: Why do we do a refresh viewer here?
    	        }
            }
        }, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#runViewUpdate(java.lang.Runnable)
     */
    protected void runViewUpdate(final Runnable runnable, final IProgressMonitor monitor) {
        super.runViewUpdate(new Runnable() {
            public void run() {
                ISynchronizeModelProvider[] providers = null;
                try {
                    providers = beginInput();
        	        runnable.run();
                } finally {
                    endInput(providers, monitor);
                }
            }
        });
    }
    
    /**
	 * Add the following sync info elements to the viewer. It is assumed that these elements have associated
	 * log entries cached in the log operation.
	 */
	private void addLogEntries(SyncInfo[] commentInfos, RemoteLogOperation logs, IProgressMonitor monitor) {
		try {
			monitor.beginTask(null, commentInfos.length * 10);
			if (logs != null) {
				for (int i = 0; i < commentInfos.length; i++) {
					addSyncInfoToCommentNode(commentInfos[i], logs);
					monitor.worked(10);
				}
				// Don't cache log entries when in two way mode.
				if (getConfiguration().getComparisonType().equals(ISynchronizePageConfiguration.TWO_WAY)) {
					logs.clearEntries();
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Create a node for the given sync info object. The logs should contain the log for this info.
	 * 
	 * @param info the info for which to create a node in the model
	 * @param log the cvs log for this node
	 */
	private void addSyncInfoToCommentNode(SyncInfo info, RemoteLogOperation logs) {
		ICVSRemoteResource remoteResource = getRemoteResource((CVSSyncInfo)info);
		if(tag1 != null && tag2 != null) {
			addMultipleRevisions(info, logs, remoteResource);
		} else {
			addSingleRevision(info, logs, remoteResource);
		}
	}
	
	/**
	 * Add multiple log entries to the model.
	 * 
	 * @param info
	 * @param logs
	 * @param remoteResource
	 */
	private void addMultipleRevisions(SyncInfo info, RemoteLogOperation logs, ICVSRemoteResource remoteResource) {
		ILogEntry[] logEntries = logs.getLogEntries(remoteResource);
		if(logEntries == null || logEntries.length == 0) {
			// If for some reason we don't have a log entry, try the latest
			// remote.
			addRemoteChange(info, null, null);
		} else {
			for (int i = 0; i < logEntries.length; i++) {
				ILogEntry entry = logEntries[i];
				addRemoteChange(info, remoteResource, entry);
			}
		}
	}

	/**
	 * Add a single log entry to the model.
	 * 
	 * @param info
	 * @param logs
	 * @param remoteResource
	 */
	private void addSingleRevision(SyncInfo info, RemoteLogOperation logs, ICVSRemoteResource remoteResource) {
		ILogEntry logEntry = logs.getLogEntry(remoteResource);
		// For incoming deletions grab the comment for the latest on the same branch
		// which is now in the attic.
		try {
			String remoteRevision = ((ICVSRemoteFile) remoteResource).getRevision();
			if (isDeletedRemotely(info)) {
				ILogEntry[] logEntries = logs.getLogEntries(remoteResource);
				for (int i = 0; i < logEntries.length; i++) {
					ILogEntry entry = logEntries[i];
					String revision = entry.getRevision();
					if (entry.isDeletion() && ResourceSyncInfo.isLaterRevision(revision, remoteRevision)) {
						logEntry = entry;
					}
				}
			}
		} catch (TeamException e) {
			// continue and skip deletion checks
		}
		addRemoteChange(info, remoteResource, logEntry);
	}

	private boolean isDeletedRemotely(SyncInfo info) {
		int kind = info.getKind();
		if(kind == (SyncInfo.INCOMING | SyncInfo.DELETION)) return true;
		if(SyncInfo.getDirection(kind) == SyncInfo.CONFLICTING && info.getRemote() == null) return true;
		return false;
	}
	
	/*
     * Add the local change to the appropriate outgoing commit set
     */
    private void addLocalChange(SyncInfo info) {
        CommitSet set = getCommitSetFor(info);
        if (set == null) {
            addToCommitSetProvider(info, getModelRoot());
        } else {
	        CommitSetDiffNode node = getDiffNodeFor(set);
	        if (node == null) {
	            node = new CommitSetDiffNode(getModelRoot(), set);
	            addToViewer(node);
	        }
	        addToCommitSetProvider(info, node);
        }
    }

    /*
     * Add the remote change to an incoming commit set
     */
    private void addRemoteChange(SyncInfo info, ICVSRemoteResource remoteResource, ILogEntry logEntry) {
        if(remoteResource != null && logEntry != null && isRemoteChange(info)) {
	        ChangeLogDiffNode changeRoot = getChangeLogDiffNodeFor(logEntry);
	        if (changeRoot == null) {
	        	changeRoot = new ChangeLogDiffNode(getModelRoot(), logEntry);
	        	addToViewer(changeRoot);
	        }
	        if(requiresCustomSyncInfo(info, remoteResource, logEntry)) {
	        	info = new CVSUpdatableSyncInfo(info.getKind(), info.getLocal(), info.getBase(), (RemoteResource)logEntry.getRemoteFile(), ((CVSSyncInfo)info).getSubscriber());
	        	try {
	        		info.init();
	        	} catch (TeamException e) {
	        		// this shouldn't happen, we've provided our own calculate kind
	        	}
	        }
	        addToCommitSetProvider(info, changeRoot);
        } else {
            // The info was not retrieved for the remote change for some reason.
            // Add the node to the root
            addToCommitSetProvider(info, getModelRoot());
        }
    }

    /*
     * Add the info to the commit set rooted at the given node.
     */
    private void addToCommitSetProvider(SyncInfo info, ISynchronizeModelElement parent) {
        ISynchronizeModelProvider provider = getProviderRootedAt(parent);
        if (provider == null) {
            // TODO: Will not get event batching for new providers
            provider = createProviderRootedAt(parent);
        }
        provider.getSyncInfoSet().add(info);
    }

    private ISynchronizeModelProvider createProviderRootedAt(ISynchronizeModelElement parent) {
        ISynchronizeModelProvider provider = createModelProvider(parent, id);
        addProvider(provider);
        rootToProvider.put(parent, provider);
        return provider;
    }

    private ISynchronizeModelProvider getProviderRootedAt(ISynchronizeModelElement parent) {
        return (ISynchronizeModelProvider)rootToProvider.get(parent);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#removeProvider(org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider)
     */
    protected void removeProvider(ISynchronizeModelProvider provider) {
        rootToProvider.remove(provider.getModelRoot());
        super.removeProvider(provider);
    }

    private boolean requiresCustomSyncInfo(SyncInfo info, ICVSRemoteResource remoteResource, ILogEntry logEntry) {
		// Only interested in non-deletions
		if (logEntry.isDeletion() || !(info instanceof CVSSyncInfo)) return false;
		// Only require a custom sync info if the remote of the sync info
		// differs from the remote in the log entry
		IResourceVariant remote = info.getRemote();
		if (remote == null) return true;
		return !remote.equals(remoteResource);
	}

	/*
	 * Find an existing comment set
	 * TODO: we could do better than a linear lookup?
	 */
	private ChangeLogDiffNode getChangeLogDiffNodeFor(ILogEntry entry) {
		IDiffElement[] elements = getModelRoot().getChildren();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement element = elements[i];
			if(element instanceof ChangeLogDiffNode) {
				ChangeLogDiffNode other = (ChangeLogDiffNode)element;
				ILogEntry thisLog = other.getComment();
				if(thisLog.getComment().equals(entry.getComment()) && thisLog.getAuthor().equals(entry.getAuthor())) {
					return other;
				}
			}
		}
		return null;
	}
	
	/*
	 * Find an existing comment set
	 * TODO: we could do better than a linear lookup?
	 */
    private CommitSetDiffNode getDiffNodeFor(CommitSet set) {
        if (set == null) return null;
		IDiffElement[] elements = getModelRoot().getChildren();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement element = elements[i];
			if(element instanceof CommitSetDiffNode) {
			    CommitSetDiffNode node = (CommitSetDiffNode)element;
				if(node.getSet() == set) {
					return node;
				}
			}
		}
		return null;
    }
	
	/*
	 * Find an existing comment set
	 * TODO: we could do better than a linear lookup?
	 * TODO: can a file be in multiple sets?
	 */
    private CommitSet getCommitSetFor(SyncInfo info) {
        CommitSet[] sets = CommitSetManager.getInstance().getSets();
        for (int i = 0; i < sets.length; i++) {
            CommitSet set = sets[i];
            if (set.contains(info.getLocal())) {
                return set;
            }
        }
        return null;
    }
    
	/*
	 * Return if this sync info should be considered as part of a remote change
	 * meaning that it can be placed inside an incoming commit set (i.e. the
	 * set is determined using the comments from the log entry of the file).
	 * 
	 */
	private boolean isRemoteChange(SyncInfo info) {
		int kind = info.getKind();
		if(info.getLocal().getType() != IResource.FILE) return false;
		if(info.getComparator().isThreeWay()) {
			return (kind & SyncInfo.DIRECTION_MASK) != SyncInfo.OUTGOING;
		}
		return true;
	}
	
	/*
	 * Return if this sync info is an outgoing change.
	 */
	private boolean isLocalChange(SyncInfo info) {
		return (info.getLocal().getType() == IResource.FILE
		        && info.getComparator().isThreeWay() 
		        && (info.getKind() & SyncInfo.DIRECTION_MASK) != SyncInfo.INCOMING);
	}

	/**
	 * How do we tell which revision has the interesting log message? Use the later
	 * revision, since it probably has the most up-to-date comment.
	 */
	private RemoteLogOperation getSyncInfoComment(SyncInfo[] infos, IProgressMonitor monitor) throws CVSException, InterruptedException {
		List remotes = new ArrayList();
		for (int i = 0; i < infos.length; i++) {
			CVSSyncInfo info = (CVSSyncInfo)infos[i];
			if (info.getLocal().getType() != IResource.FILE) {
				continue;
			}	
			ICVSRemoteResource remote = getRemoteResource(info);
			if(remote != null) {
				remotes.add(remote);
			}
		}
		ICVSRemoteResource[] remoteResources = (ICVSRemoteResource[]) remotes.toArray(new ICVSRemoteResource[remotes.size()]);
		if(logOperation == null) {
			logOperation = new RemoteLogOperation(null, remoteResources, tag1, tag2);
		}
		if(! remotes.isEmpty()) {
			logOperation.setRemoteResources(remoteResources);
			logOperation.execute(monitor);
		}
		return logOperation;
	}
	
	private ICVSRemoteResource getRemoteResource(CVSSyncInfo info) {
		try {
			ICVSRemoteResource remote = (ICVSRemoteResource) info.getRemote();
			ICVSRemoteResource local = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());
			if(local == null) {
				local = (ICVSRemoteResource)info.getBase();
			}

			String remoteRevision = getRevisionString(remote);
			String localRevision = getRevisionString(local);
			
			boolean useRemote = true;
			if (local != null && remote != null) {
				useRemote = ResourceSyncInfo.isLaterRevision(remoteRevision, localRevision);
			} else if (remote == null) {
				useRemote = false;
			}
			if (useRemote) {
				return remote;
			} else if (local != null) {
				return local;
			}
			return null;
		} catch (CVSException e) {
			CVSUIPlugin.log(e);
			return null;
		}
	}
	
	private String getRevisionString(ICVSRemoteResource remoteFile) {
		if(remoteFile instanceof RemoteFile) {
			return ((RemoteFile)remoteFile).getRevision();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.views.HierarchicalModelProvider#dispose()
	 */
	public void dispose() {
		shutdown = true;
		if(fetchLogEntriesJob != null && fetchLogEntriesJob.getState() != Job.NONE) {
			fetchLogEntriesJob.cancel();
		}
		CommitSetManager.getInstance().removeListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider#getViewerSorter()
	 */
	public ViewerSorter getViewerSorter() {
		return new ChangeLogModelSorter(this, sortCriteria);
	}

	/* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#handleChanges(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void handleChanges(ISyncInfoTreeChangeEvent event, IProgressMonitor monitor) {
        super.handleChanges(event, monitor);
        SyncInfoSet syncInfoSet;
        synchronized (queuedAdditions) {
            syncInfoSet = new SyncInfoSet((SyncInfo[]) queuedAdditions.toArray(new SyncInfo[queuedAdditions.size()]));
            queuedAdditions.clear();
        }
        startUpdateJob(syncInfoSet);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#nodeRemoved(org.eclipse.team.ui.synchronize.ISynchronizeModelElement, org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider)
     */
    protected void nodeRemoved(ISynchronizeModelElement node, AbstractSynchronizeModelProvider provider) {
        super.nodeRemoved(node, provider);
        // TODO: This should be done using the proper API
		if (node instanceof SyncInfoModelElement) {
			CVSSyncInfo info = (CVSSyncInfo) ((SyncInfoModelElement) node).getSyncInfo();
			if (info != null) {
				ICVSRemoteResource remote = getRemoteResource(info);
				logOperation.clearEntriesFor(remote);
			}
		}
		if (provider.getSyncInfoSet().isEmpty() && provider.getModelRoot() != getModelRoot()) {
		    // The provider is empty so remove it 
		    // (but keep it if it is a direct child of the root
		    // since that's where we get the sorter and action group)
		    removeProvider(provider);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.subscriber.ICommitSetChangeListener#setAdded(org.eclipse.team.internal.ccvs.ui.subscriber.CommitSet)
     */
    public void setAdded(CommitSet set) {
        refresh(set.getFiles(), true /* we may not be in the UI thread */);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.subscriber.ICommitSetChangeListener#setRemoved(org.eclipse.team.internal.ccvs.ui.subscriber.CommitSet)
     */
    public void setRemoved(CommitSet set) {
        refresh(set.getFiles(), true /* we may not be in the UI thread */);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.subscriber.ICommitSetChangeListener#titleChanged(org.eclipse.team.internal.ccvs.ui.subscriber.CommitSet)
     */
    public void titleChanged(CommitSet set) {
        refreshNode(getDiffNodeFor(set));
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.subscriber.ICommitSetChangeListener#filesChanged(org.eclipse.team.internal.ccvs.ui.subscriber.CommitSet, org.eclipse.core.resources.IFile[])
     */
    public void filesChanged(CommitSet set, IFile[] files) {
        refresh(files, true /* we may not be in the UI thread */);
    }
    
    private void refresh(final IResource[] resources, boolean performSyncExec) {
        Runnable runnable = new Runnable() {
            public void run() {
                List infos = new ArrayList();
                for (int i = 0; i < resources.length; i++) {
                    IResource resource = resources[i];
                    SyncInfo info = getSyncInfoSet().getSyncInfo(resource);
                    if (info != null) {
                        infos.add(info);
                        // There is no need to batch these removals as there
                        // is at most one change per sub-provider
        				handleRemoval(resource);
                    }
        		}
        		startUpdateJob(new SyncInfoSet((SyncInfo[]) infos.toArray(new SyncInfo[infos.size()])));
            }
        };
        if (performSyncExec) {
            syncExec(runnable);
        } else {
            runnable.run();
        }
    }

    private void syncExec(final Runnable runnable) {
		final Control ctrl = getViewer().getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
					    runnable.run();
					}
				}
			});
		}
    }
    
    private void refreshNode(final DiffNode node) {
        if (node != null) {
            syncExec(new Runnable() {
                public void run() {
                    getViewer().refresh(node);
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(CommitSetManager.DEFAULT_SET)) {
            CommitSet oldValue = (CommitSet)event.getOldValue();
            refreshNode(getDiffNodeFor(oldValue));
            CommitSet newValue = (CommitSet)event.getNewValue();
            refreshNode(getDiffNodeFor(newValue));
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#handleAdditions(org.eclipse.team.core.synchronize.SyncInfo[])
     */
    protected void handleAddition(SyncInfo info) {
        synchronized (queuedAdditions) {
	        queuedAdditions.add(info);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#clearModelObjects(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
     */
    protected void clearModelObjects(ISynchronizeModelElement node) {
        super.clearModelObjects(node);
        if (node == getModelRoot()) {
            rootToProvider.clear();
            // Throw away the embedded sorter
            embeddedSorter = null;
            createRootProvider();
        }
    }

    /*
     * Create the root subprovider which is used to display resources
     * that are not in a commit set. This provider is created even if
     * it is empty so we can have access to the appropriate sorter 
     * and action group 
     */
    private void createRootProvider() {
        // Recreate the sub-provider at the root and use it's viewer sorter and action group
        final ISynchronizeModelProvider provider = createProviderRootedAt(getModelRoot());
        embeddedSorter = provider.getViewerSorter();
        if (provider instanceof AbstractSynchronizeModelProvider) {
            SynchronizePageActionGroup actionGroup = ((AbstractSynchronizeModelProvider)provider).getActionGroup();
            if (actionGroup != null) {
                // This action group will be disposed when the provider is disposed
                getConfiguration().addActionContribution(actionGroup);
                provider.addPropertyChangeListener(new IPropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getProperty().equals(P_VIEWER_SORTER)) {
                            embeddedSorter = provider.getViewerSorter();
                            ChangeLogModelProvider.this.firePropertyChange(P_VIEWER_SORTER, null, null);
                        }
                    }
                });
            }
        }
    }

    /**
     * Return the id of the sub-provider used by the commit set provider.
     * @return the id of the sub-provider used by the commit set provider
     */
    public String getSubproviderId() {
        return id;
    }

    /**
     * Return the sorter associated with the sub-provider being used.
     * @return the sorter associated with the sub-provider being used
     */
    public ViewerSorter getEmbeddedSorter() {
        return embeddedSorter;
    }
    
}
