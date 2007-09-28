/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.mapping.CompoundResourceTraversal;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.ISynchronizationCompareAdapter;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IContributorResourceAdapter2;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

public class Utils {

	/**
	 * Constant used to indicate that tests are being run. This field
	 * should be the same as the corresponding field on 
	 * org.eclipse.compare.internal.Utilities
	 */
	public static boolean RUNNING_TESTS = false;

	/**
	 * Constant used while testing the indicate that changes should be flushed
	 * when the compare input changes and a viewer is dirty. This field
	 * should be the same as the corresponding field on 
	 * org.eclipse.compare.internal.Utilities
	 */
	public static boolean TESTING_FLUSH_ON_COMPARE_INPUT_CHANGE = false;	
	
	/**
	 * The SortOperation takes a collection of objects and returns a sorted
	 * collection of these objects. Concrete instances of this class provide
	 * the criteria for the sorting of the objects based on the type of the
	 * objects.
	 */
	static public abstract class Sorter {

		/**
		 * Returns true is elementTwo is 'greater than' elementOne This is the
		 * 'ordering' method of the sort operation. Each subclass overrides this
		 * method with the particular implementation of the 'greater than'
		 * concept for the objects being sorted.
		 * @param elementOne element 1
		 * @param elementTwo element 2
		 * @return whether element 2 is greater that element 1
		 */
		public abstract boolean compare(Object elementOne, Object elementTwo);

		/**
		 * Sort the objects in sorted collection and return that collection.
		 */
		private Object[] quickSort(Object[] sortedCollection, int left, int right) {
			int originalLeft = left;
			int originalRight = right;
			Object mid = sortedCollection[(left + right) / 2];
			do {
				while (compare(sortedCollection[left], mid))
					left++;
				while (compare(mid, sortedCollection[right]))
					right--;
				if (left <= right) {
					Object tmp = sortedCollection[left];
					sortedCollection[left] = sortedCollection[right];
					sortedCollection[right] = tmp;
					left++;
					right--;
				}
			} while (left <= right);
			if (originalLeft < right)
				sortedCollection = quickSort(sortedCollection, originalLeft, right);
			if (left < originalRight)
				sortedCollection = quickSort(sortedCollection, left, originalRight);
			return sortedCollection;
		}

		/**
		 * Return a new sorted collection from this unsorted collection. Sort
		 * using quick sort.
		 * @param unSortedCollection the original collection
		 * @return the sorted collection
		 */
		public Object[] sort(Object[] unSortedCollection) {
			int size = unSortedCollection.length;
			Object[] sortedCollection = new Object[size];
			//copy the array so can return a new sorted collection
			System.arraycopy(unSortedCollection, 0, sortedCollection, 0, size);
			if (size > 1)
				quickSort(sortedCollection, 0, size - 1);
			return sortedCollection;
		}
	}

	public static final Comparator resourceComparator = new Comparator() {
		public boolean equals(Object obj) {
			return false;
		}
		public int compare(Object o1, Object o2) {
				IResource resource0 = (IResource) o1;
				IResource resource1 = (IResource) o2;
				return resource0.getFullPath().toString().compareTo(resource1.getFullPath().toString());
		}
	};
	
	/**
	 * Shows the given errors to the user.
	 * @param shell
	 *            the shell to open the error dialog in
	 * @param exception
	 *            the exception containing the error
	 * @param title
	 *            the title of the error dialog
	 * @param message
	 *            the message for the error dialog
	 */
	public static void handleError(Shell shell, Exception exception, String title, String message) {
		IStatus status = null;
		boolean log = false;
		boolean dialog = false;
		Throwable t = exception;
		if (exception instanceof TeamException) {
			status = ((TeamException) exception).getStatus();
			log = false;
			dialog = true;
		} else if (exception instanceof InvocationTargetException) {
			t = ((InvocationTargetException) exception).getTargetException();
			if (t instanceof TeamException) {
				status = ((TeamException) t).getStatus();
				log = false;
				dialog = true;
			} else if (t instanceof CoreException) {
				status = ((CoreException) t).getStatus();
				log = true;
				dialog = true;
			} else if (t instanceof InterruptedException) {
				return;
			} else {
				status = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, TeamUIMessages.TeamAction_internal, t); 
				log = true;
				dialog = true;
			}
		}
		if (status == null)
			return;
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1) {
					toShow = children[0];
				}
			}
			if (title == null) {
				title = status.getMessage();
			}
			if (message == null) {
				message = status.getMessage();
			}
			if (dialog && shell != null) {
				ErrorDialog.openError(shell, title, message, toShow);
			}
			if (log || shell == null) {
				TeamUIPlugin.log(toShow.getSeverity(), message, t);
			}
		}
	}

	public static void runWithProgress(Shell parent, boolean cancelable, final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		boolean createdShell = false;
		try {
			if (parent == null || parent.isDisposed()) {
				Display display = Display.getCurrent();
				if (display == null) {
					// cannot provide progress (not in UI thread)
					runnable.run(new NullProgressMonitor());
					return;
				}
				// get the active shell or a suitable top-level shell
				parent = display.getActiveShell();
				if (parent == null) {
					parent = new Shell(display);
					createdShell = true;
				}
			}
			// pop up progress dialog after a short delay
			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {

				public void run() {
					try {
						runnable.run(new NullProgressMonitor());
					} catch (InvocationTargetException e) {
						holder[0] = e;
					} catch (InterruptedException e) {
						holder[0] = e;
					}
				}
			});
			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else {
					throw (InterruptedException) holder[0];
				}
			}
			//new TimeoutProgressMonitorDialog(parent, TIMEOUT).run(true
			// /*fork*/, cancelable, runnable);
		} finally {
			if (createdShell)
				parent.dispose();
		}
	}

	public static Shell getShell(IWorkbenchSite site) {
		return getShell(site, false);
	}
	
	public static Shell getShell(IWorkbenchSite site, boolean syncIfNecessary) {
		if(site != null) {
			Shell shell = site.getShell();
			if (!shell.isDisposed())
				return shell;
		}
		IWorkbench workbench = TeamUIPlugin.getPlugin().getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {
				return window.getShell();
			}
		}
		// Fallback to using the display
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
			if (display.isDisposed()) return null;
			if (syncIfNecessary) {
				final Shell[] result = new Shell[] { null };
				Runnable r = new Runnable() {
					public void run() {
						result[0] = new Shell(Display.getDefault());
					}
				};
				display.syncExec(r);
				return result[0];
			}
		}
		if (display.isDisposed()) return null;
		return new Shell(display);
	}
	/*
	 * This method is only for use by the Target Management feature (see bug
	 * 16509). @param t
	 */
	public static void handle(final Throwable exception) {
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IStatus error = null;
				Throwable t = exception;
				if (t instanceof InvocationTargetException) {
					t = ((InvocationTargetException) t).getTargetException();
				}
				if (t instanceof CoreException) {
					error = ((CoreException) t).getStatus();
				} else if (t instanceof TeamException) {
					error = ((TeamException) t).getStatus();
				} else {
					error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, TeamUIMessages.simpleInternal, t); 
				}
				Shell shell = new Shell(Display.getDefault());
				if (error.getSeverity() == IStatus.INFO) {
					MessageDialog.openInformation(shell, TeamUIMessages.information, error.getMessage()); 
				} else {
					ErrorDialog.openError(shell, TeamUIMessages.exception, null, error); 
				}
				shell.dispose();
				// Let's log non-team exceptions
				if (!(t instanceof TeamException)) {
					TeamUIPlugin.log(error.getSeverity(), error.getMessage(), t);
				}
			}
		});
	}

	public static Shell findShell() {
		Display display = TeamUIPlugin.getStandardDisplay();
		Shell activeShell = display.getActiveShell();
		if (activeShell != null)
			return activeShell;
		// worst case, just create our own.
		return new Shell(display);
	}

	public static void initAction(IAction a, String prefix) {
		Utils.initAction(a, prefix, Policy.getActionBundle());
	}
	
	public static void initAction(IAction a, String prefix, ResourceBundle bundle) {
		Utils.initAction(a, prefix, bundle, null);
	}
	
	public static void updateLabels(SyncInfo sync, CompareConfiguration config) {
		final IResourceVariant remote = sync.getRemote();
		final IResourceVariant base = sync.getBase();
		String localContentId = sync.getLocalContentIdentifier();
		if (localContentId != null) {
			config.setLeftLabel(NLS.bind(TeamUIMessages.SyncInfoCompareInput_localLabelExists, new String[] { localContentId })); 
		} else {
			config.setLeftLabel(TeamUIMessages.SyncInfoCompareInput_localLabel); 
		}
		if (remote != null) {
			config.setRightLabel(NLS.bind(TeamUIMessages.SyncInfoCompareInput_remoteLabelExists, new String[] { remote.getContentIdentifier() })); 
		} else {
			config.setRightLabel(TeamUIMessages.SyncInfoCompareInput_remoteLabel); 
		}
		if (base != null) {
			config.setAncestorLabel(NLS.bind(TeamUIMessages.SyncInfoCompareInput_baseLabelExists, new String[] { base.getContentIdentifier() })); 
		} else {
			config.setAncestorLabel(TeamUIMessages.SyncInfoCompareInput_baseLabel); 
		}
	}

	public static String getLocalContentId(IDiff diff) {
		if (diff instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) diff;
			diff = twd.getLocalChange();
			if (diff == null)
				diff = twd.getRemoteChange();
		}
		if (diff instanceof IResourceDiff) {
			IResourceDiff rd = (IResourceDiff) diff;
			IResource resource = rd.getResource();
			IFileHistoryProvider provider = getHistoryProvider(resource);
			if (provider != null) {
				IFileRevision revision = provider.getWorkspaceFileRevision(resource);
				if (revision != null)
					return revision.getContentIdentifier();
			}
		}
		return null;
	}

	public static IFileHistoryProvider getHistoryProvider(IResource resource) {
		RepositoryProvider rp = RepositoryProvider.getProvider(resource.getProject());
		if (rp != null)
			return rp.getFileHistoryProvider();
		return null;
	}

	public static IFileRevision getBase(IDiff diff) {
		if (diff instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) diff;
			IDiff remoteChange = twd.getRemoteChange();
			if (remoteChange instanceof IResourceDiff) {
				IResourceDiff rd = (IResourceDiff) remoteChange;
				return rd.getBeforeState();
			}
			IDiff localChange = twd.getLocalChange();
			if (localChange instanceof IResourceDiff) {
				IResourceDiff ld = (IResourceDiff) localChange;
				return ld.getBeforeState();
			}
		}
		return null;
	}

	public static IFileRevision getRemote(IDiff diff) {
		if (diff instanceof IResourceDiff) {
			IResourceDiff rd = (IResourceDiff) diff;
			return rd.getAfterState();
		}
		if (diff instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) diff;
			IDiff remoteChange = twd.getRemoteChange();
			if (remoteChange instanceof IResourceDiff) {
				IResourceDiff rd = (IResourceDiff) remoteChange;
				return rd.getAfterState();
			}
			IDiff localChange = twd.getLocalChange();
			if (localChange instanceof IResourceDiff) {
				IResourceDiff ld = (IResourceDiff) localChange;
				return ld.getBeforeState();
			}
		}
		return null;
	}

	/**
	 * Initialize the given Action from a ResourceBundle.
	 * @param a the action
	 * @param prefix the bundle key prefix
	 * @param bundle the bundle
	 * @param bindings additional input to the action label
	 */
	public static void initAction(IAction a, String prefix, ResourceBundle bundle, String[] bindings) {
		String labelKey = "label"; //$NON-NLS-1$
		String tooltipKey = "tooltip"; //$NON-NLS-1$
		String imageKey = "image"; //$NON-NLS-1$
		String descriptionKey = "description"; //$NON-NLS-1$
		if (prefix != null && prefix.length() > 0) {
			labelKey = prefix + labelKey;
			tooltipKey = prefix + tooltipKey;
			imageKey = prefix + imageKey;
			descriptionKey = prefix + descriptionKey;
		}
		String s = null;
		if(bindings != null) {
			s = NLS.bind(getString(labelKey, bundle), bindings);
		} else {
			s = getString(labelKey, bundle);
		}
		if (s != null)
			a.setText(s);
		s = getString(tooltipKey, bundle);
		if (s != null)
			a.setToolTipText(s);
		s = getString(descriptionKey, bundle);
		if (s != null)
			a.setDescription(s);
		String relPath = getString(imageKey, bundle);
		if (relPath != null && !relPath.equals(imageKey) && relPath.trim().length() > 0) {
			String dPath;
			String ePath;
			if (relPath.indexOf("/") >= 0) { //$NON-NLS-1$
				String path = relPath.substring(1);
				dPath = 'd' + path;
				ePath = 'e' + path;
			} else {
				dPath = "dlcl16/" + relPath; //$NON-NLS-1$
				ePath = "elcl16/" + relPath; //$NON-NLS-1$
			}
			ImageDescriptor id = TeamImages.getImageDescriptor(dPath);
			if (id != null)
				a.setDisabledImageDescriptor(id);
			id = TeamUIPlugin.getImageDescriptor(ePath);
			if (id != null)
				a.setImageDescriptor(id);
		}
	}
    
    public static String getString(String key, ResourceBundle b) {
        try {
            return b.getString(key);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

	public static String modeToString(int mode) {
		switch (mode) {
			case ISynchronizePageConfiguration.INCOMING_MODE :
				return TeamUIMessages.Utils_22; 
			case ISynchronizePageConfiguration.OUTGOING_MODE :
				return TeamUIMessages.Utils_23; 
			case ISynchronizePageConfiguration.BOTH_MODE :
				return TeamUIMessages.Utils_24; 
			case ISynchronizePageConfiguration.CONFLICTING_MODE :
				return TeamUIMessages.Utils_25; 
		}
		return TeamUIMessages.Utils_26; 
	}

	/**
	 * Returns the list of resources contained in the given elements.
	 * @param elements
	 * @return the list of resources contained in the given elements.
	 */
	private static IResource[] getResources(Object[] elements, List nonResources, boolean isContributed, boolean includeMappingResources) {
		List resources = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			boolean isResource = false;
			if (element instanceof IResource) {
				resources.add(element);
                isResource = true;
			} else if (element instanceof ISynchronizeModelElement){
                IResource resource = ((ISynchronizeModelElement) element).getResource();
                if (resource != null) {
                    resources.add(resource);
                    isResource = true;
                }
            } else if (element instanceof ResourceMapping) {
            	if (includeMappingResources) {
	                isResource = true;
	                getResources((ResourceMapping)element, resources);
            	}
			} else if (element != null) {
                Object adapted;
                if (isContributed) {
                    adapted = getResource(element);
                } else {
                    adapted = getAdapter(element, IResource.class);
                }
                if (adapted instanceof IResource) {
                    IResource resource = (IResource) adapted;
                    isResource = true;
                    if (resource.getType() != IResource.ROOT) {
                        resources.add(resource);
                    }
                } else {
                    if (isContributed) {
                        adapted = getResourceMapping(element);
                    } else {
                        adapted = getAdapter(element, ResourceMapping.class);
                    }
                    if (adapted instanceof ResourceMapping && includeMappingResources) {
                        isResource = true;
                        getResources((ResourceMapping) adapted, resources);
                    }
                }
			}
			if (!isResource) {
				if(nonResources != null)
					nonResources.add(element);
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

    private static void getResources(ResourceMapping element, List resources) {
        try {
            ResourceTraversal[] traversals = element.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, null);
            for (int k = 0; k < traversals.length; k++) {
                ResourceTraversal traversal = traversals[k];
                IResource[] resourceArray = traversal.getResources();
                for (int j = 0; j < resourceArray.length; j++) {
                    IResource resource = resourceArray[j];
                    resources.add(resource);
                }
            }
        } catch (CoreException e) {
            TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, "Error traversing resource mapping", e)); //$NON-NLS-1$
        }
    }
	
	public static Object[] getNonResources(Object[] elements) {
		List nonResources = new ArrayList();
		getResources(elements, nonResources, false, false);
		return nonResources.toArray();
	}
	
	public static IResource[] getResources(Object[] element) {
		return getResources(element, null, false /* isContributed */, false /* includeMappingResources */);
	}
    
    public static IResource[] getContributedResources(Object[] elements) {
        return getResources(elements, null, true /* isContributed */, true /* isIncudeMappings */);
    }
	
	public static Object getAdapter(Object element, Class adapterType, boolean load) {
		if (adapterType.isInstance(element))
			return element;
		if (element instanceof IAdaptable) {
			Object adapted = ((IAdaptable) element).getAdapter(adapterType);
			if (adapterType.isInstance(adapted))
				return adapted;
		}
		if (load) {
			Object adapted = Platform.getAdapterManager().loadAdapter(element, adapterType.getName());
			if (adapterType.isInstance(adapted))
				return adapted;
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(element, adapterType);
			if (adapterType.isInstance(adapted))
				return adapted;
		}
		return null;
	}
	
	public static Object getAdapter(Object element, Class adapterType) {
		return getAdapter(element, adapterType, false);
	}
	
	/**
	 * Return whether any sync nodes in the given selection or their
	 * descendants match the given filter.
	 * @param selection a selection
	 * @param filter a sync info filter
	 * @return whether any sync nodes in the given selection or their
	 * descendants match the given filter
	 */
	public static boolean hasMatchingDescendant(IStructuredSelection selection, FastSyncInfoFilter filter) {
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof ISynchronizeModelElement) {
				if (hasMatchingDescendant((ISynchronizeModelElement)o, filter)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean hasMatchingDescendant(ISynchronizeModelElement element, FastSyncInfoFilter filter) {
		if (element.getKind() != SyncInfo.IN_SYNC && element instanceof SyncInfoModelElement) {
			SyncInfo info = ((SyncInfoModelElement) element).getSyncInfo();
			if (info != null && filter.select(info)) {
				return true;
			}
		}
		IDiffElement[] children = element.getChildren();
		for (int i = 0; i < children.length; i++) {
			IDiffElement child = children[i];
			if (child instanceof ISynchronizeModelElement) {
				if (hasMatchingDescendant((ISynchronizeModelElement)child, filter)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method returns all out-of-sync SyncInfos that are in the current
	 * selection.
	 * @param selected the selected objects
	 * 
	 * @return the list of selected sync infos
	 */
	public static IDiffElement[] getDiffNodes(Object[] selected) {
		Set result = new HashSet();
		for (int i = 0; i < selected.length; i++) {
			Object object = selected[i];
			if(object instanceof IDiffElement) {
				collectAllNodes((IDiffElement)object, result);
			}
		}
		return (IDiffElement[]) result.toArray(new IDiffElement[result.size()]);
	}
	
	private static void collectAllNodes(IDiffElement element, Set nodes) {
		if(element.getKind() != SyncInfo.IN_SYNC) {
			nodes.add(element);
		}
		if(element instanceof IDiffContainer) {
			IDiffElement[] children = ((IDiffContainer)element).getChildren();
			for (int i = 0; i < children.length; i++) {
				collectAllNodes(children[i], nodes);				
			}
		}
	}
	
	public static void schedule(Job job, IWorkbenchSite site) {
		if (site != null) {
			IWorkbenchSiteProgressService siteProgress = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
			if (siteProgress != null) {
				siteProgress.schedule(job, 0, true /* use half-busy cursor */);
				return;
			}
		}
		job.schedule();
	}
	
	public static byte[] readBytes(InputStream in) {
		ByteArrayOutputStream bos= new ByteArrayOutputStream();
		try {		
			while (true) {
				int c= in.read();
				if (c == -1)
					break;
				bos.write(c);
			}
					
		} catch (IOException ex) {
			return null;
		
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException x) {
					// silently ignored
				}
			}
			try {
				bos.close();
			} catch (IOException x) {
				// silently ignored
			}
		}	
		return bos.toByteArray();
	}
	
	public static boolean equalObject(Object o1, Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 == null || o2 == null) return false;
		return o1.equals(o2);
	}

	public static String getKey(String id, String secondaryId) {
	    return secondaryId == null ? id : id + '/' + secondaryId;
	}
	
	public static String convertSelection(IResource[] resources) {
		StringBuffer  buffer = new StringBuffer();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if(i > 0) buffer.append(", "); //$NON-NLS-1$
			buffer.append(resource.getFullPath());
		}
		return buffer.toString();
	}
	
	/**
	 * Shorten the given text <code>t</code> so that its length
	 * doesn't exceed the given width. This implementation
	 * replaces characters in the center of the original string with an
	 * ellipsis ("...").
	 * @param maxWidth the maximum length for the text
	 * @param textValue the text to be shortened
	 * @return the shortened text
	 */
	public static String shortenText(int maxWidth, String textValue) {
		int length = textValue.length();
		if (length < maxWidth)
			return textValue;
		String ellipsis = "..."; //$NON-NLS-1$
		int subStrLen = (maxWidth - ellipsis.length()) / 2;
		int addtl = (maxWidth - ellipsis.length()) % 2;

		StringBuffer sb = new StringBuffer();
		sb.append(textValue.substring(0, subStrLen));
		sb.append(ellipsis);
		sb.append(textValue.substring(length - subStrLen - addtl));
		return sb.toString();
	}
	
	public static String getTypeName(ISynchronizeParticipant participant) {
		ISynchronizeManager manager = TeamUI.getSynchronizeManager();
		return manager.getParticipantDescriptor(participant.getId()).getName();
	}

    /**
     * The viewer will only be updated if the viewer is not null, the control is not disposed, and
     * this code is being run from the UI thread.
     * @param viewer the viewer to be updated
     * @return whether it is safe to update the viewer
     */
    public static boolean canUpdateViewer(StructuredViewer viewer) {
		if(viewer == null || viewer.getControl().isDisposed()) return false;
		Display display = viewer.getControl().getDisplay();
		if (display == null) return false;
		if (display.getThread() != Thread.currentThread ()) return false;
		return true;
    }
    
    public static void asyncExec(final Runnable r, StructuredViewer v) {
		if(v == null) return;
		final Control ctrl = v.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
						BusyIndicator.showWhile(ctrl.getDisplay(), r);
					}
				}
			});
		}
	}

    public static void syncExec(final Runnable r, StructuredViewer v) {
		if(v == null) return;
		final Control ctrl = v.getControl();
		syncExec(r, ctrl);
    }

	public static void syncExec(final Runnable r, final Control ctrl) {
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
						BusyIndicator.showWhile(ctrl.getDisplay(), r);
					}
				}
			});
		}
	}
	
	public static void asyncExec(final Runnable r, final Control ctrl) {
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
						BusyIndicator.showWhile(ctrl.getDisplay(), r);
					}
				}
			});
		}
	}

    public static SyncInfo getSyncInfo(ISynchronizeModelElement node) {
        if (node instanceof IAdaptable) {
            return (SyncInfo)((IAdaptable)node).getAdapter(SyncInfo.class);
        }
        return null;
    }

	public static ISynchronizationCompareAdapter getCompareAdapter(Object element) {
		ModelProvider provider = getModelProvider(element);
		if (provider != null) {
			Object o = provider.getAdapter(ISynchronizationCompareAdapter.class);
			if (o instanceof ISynchronizationCompareAdapter) {
				return (ISynchronizationCompareAdapter) o;
			}
		}
		return null;
	}

	public static ModelProvider getModelProvider(Object o) {
		if (o instanceof ModelProvider) {
			return (ModelProvider) o;	
		}
		ResourceMapping mapping = getResourceMapping(o);
		if (mapping != null)
			return mapping.getModelProvider();
		return null;
	}
	
	public static IResource getResource(Object o) {
		IResource resource = null;
		if (o instanceof IResource) {
			resource = (IResource) o;
		} else if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			resource = (IResource)adaptable.getAdapter(IResource.class);
			if (resource == null) {
				IContributorResourceAdapter adapter = (IContributorResourceAdapter)adaptable.getAdapter(IContributorResourceAdapter.class);
				if (adapter != null)
					resource = adapter.getAdaptedResource(adaptable);
			}
		}
		return resource;
	}
	
	
	public static ResourceMapping getResourceMapping(Object o) {
		if (o instanceof ResourceMapping) {
			return (ResourceMapping) o;
		}
		if (o instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) o;
			Object adapted = adaptable.getAdapter(ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return(ResourceMapping) adapted;
			}
			adapted = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (adapted instanceof IContributorResourceAdapter2) {
				IContributorResourceAdapter2 cra = (IContributorResourceAdapter2) adapted;
				return cra.getAdaptedResourceMapping(adaptable);
			}
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(o, ResourceMapping.class);
			if (adapted instanceof ResourceMapping) {
				return(ResourceMapping) adapted;
			}
		}
		return null;
	}

	public static ResourceMapping[] getResourceMappings(Object[] objects) {
		List result = new ArrayList();
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			ResourceMapping mapping = getResourceMapping(object);
			if (mapping != null)
				result.add(mapping);
		}
		return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
	}

	public static String getLabel(ResourceMapping mapping) {
		ModelProvider provider = mapping.getModelProvider();
		ISynchronizationCompareAdapter adapter = getCompareAdapter(provider);
		if (adapter == null)
			return ""; //$NON-NLS-1$
		String pathString = adapter.getPathString(mapping);
		if (pathString == null || pathString.length() == 0)
			return adapter.getName(mapping);
		return pathString;
	}
	
	public static String getLabel(ModelProvider provider) {
		ResourceMapping mapping = Utils.getResourceMapping(provider);
		if (mapping != null) {
			String base = Utils.getLabel(mapping);
			if (base != null && base.length() > 0)
				return base;
		}
		return provider.getDescriptor().getLabel();
	}

	public static String getScopeDescription(ISynchronizationScope scope) {
		ResourceMapping[] mappings = scope.getInputMappings();
		if (mappings.length == 1) {
			String label = getLabel(mappings[0]);
			if (label == null)
				return TeamUIMessages.Utils_19;
			else
				return label;
		}
		String desc = convertSelection(mappings);
		if (desc.length() > 0)
			return shortenText(30, desc);
		return NLS.bind(TeamUIMessages.Utils_18, new Integer(mappings.length));
	}
	
	public static String convertSelection(ResourceMapping[] mappings) {
		StringBuffer  buffer = new StringBuffer();
		boolean hadOne = false;
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping resourceMapping = mappings[i];
			String label = getLabel(resourceMapping);
			if (label != null) {
				if(hadOne) buffer.append(", "); //$NON-NLS-1$
				hadOne = true;
				buffer.append(label);
			}
		}
		return buffer.toString();
	}

	public static ResourceTraversal[] getTraversals(Object[] elements) throws CoreException {
		CompoundResourceTraversal traversal = new CompoundResourceTraversal();
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			ResourceMapping mapping = getResourceMapping(object);
			if (mapping != null) {
				traversal.addTraversals(mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, null));
			}
		}
		return traversal.asTraversals();
	}

	/**
	 * Return whether the editor associated with a descriptor is a text editor
	 * (i.e. an instance of AbstractDecoratedTextEditor).
	 * See bug 99568 for a request to move the createEditor method to IEditorDescriptor.
	 * @param descriptor
	 * @return whether the editor associated with a descriptor is a text editor
	 * @throws CoreException
	 */
	public static boolean isTextEditor(IEditorDescriptor descriptor)
			throws CoreException {
		if (descriptor instanceof EditorDescriptor) {
			EditorDescriptor desc = (EditorDescriptor) descriptor;
			IEditorPart editor= desc.createEditor();
			editor.dispose();
			return editor instanceof AbstractDecoratedTextEditor;
		}
		return false;
	}
	
	public static IEditorPart openEditor(IWorkbenchPage page, IFileRevision revision, IProgressMonitor monitor) throws CoreException {
		IStorage file = revision.getStorage(monitor);
		if (file instanceof IFile) {
			//if this is the current workspace file, open it
			return IDE.openEditor(page, (IFile) file);
		} else {
			FileRevisionEditorInput fileRevEditorInput = FileRevisionEditorInput.createEditorInputFor(revision, monitor);
			IEditorPart part = findEditor(page, fileRevEditorInput);
			if (part == null)
				part = openEditor(page, fileRevEditorInput);
			return part;
		}
	}
	
	public static IEditorPart openEditor(IWorkbenchPage page, FileRevisionEditorInput editorInput) throws PartInitException {
		String id = getEditorId(editorInput);
		try {
			IEditorPart part = page.openEditor(editorInput, id);
			// See bug 90582 for the reasons behind this discouraged access
			if (part instanceof ErrorEditorPart) {
				page.closeEditor(part, false);
				part = null;
			}
			if (part == null) {
				throw new PartInitException(NLS.bind(TeamUIMessages.Utils_17, id));
			}
			return part;
		} catch (PartInitException e) {
			if (id.equals("org.eclipse.ui.DefaultTextEditor")) { //$NON-NLS-1$
				throw e;
			} else {
				return page.openEditor(editorInput,"org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			}
		}
	}

	private static String getEditorId(FileRevisionEditorInput editorInput) {
		String id = getEditorId(editorInput.getFileRevision().getName(), getContentType(editorInput));
		return id;
	}

	private static IContentType getContentType(FileRevisionEditorInput editorInput) {
		IContentType type = null;
		try {
			InputStream contents = editorInput.getStorage().getContents();
			try {
				type = getContentType(editorInput.getFileRevision().getName(), contents);
			} finally {
				try {
					contents.close();
				} catch (IOException e) {
					// ignore
				}
			}
		} catch (CoreException e) {
			TeamUIPlugin.log(IStatus.ERROR, NLS.bind("An error occurred reading the contents of file {0}", new String[] { editorInput.getName() }), e); //$NON-NLS-1$
		}
		return type;
	}
	
	private static IContentType getContentType(String fileName, InputStream contents) {
		IContentType type = null;
		if (contents != null) {
			try {
				type = Platform.getContentTypeManager().findContentTypeFor(contents, fileName);
			} catch (IOException e) {
				TeamUIPlugin.log(IStatus.ERROR, NLS.bind("An error occurred reading the contents of file {0}", fileName), e); //$NON-NLS-1$
			}
		}
		if (type == null) {
			type = Platform.getContentTypeManager().findContentTypeFor(fileName);
		}
		return type;
	}

	private static String getEditorId(String fileName, IContentType type) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(fileName, type);
		String id;
		if (descriptor == null || descriptor.isOpenExternal()) {
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}
		return id;
	}
	
	private static IEditorPart findEditor(IWorkbenchPage page, FileRevisionEditorInput input) {
		IEditorReference[] editorRefs = page.getEditorReferences();	
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorPart part = editorRefs[i].getEditor(false);
			if(part != null 
			   && part.getEditorInput() instanceof FileRevisionEditorInput) {
				IFileRevision inputRevision = (IFileRevision) input.getAdapter(IFileRevision.class);
				IFileRevision editorRevision = (IFileRevision) part.getEditorInput().getAdapter(IFileRevision.class);
				
				if (inputRevision.equals(editorRevision)){
					//make the editor that already contains the revision current
					page.activate(part);
					return part;
				}
			}
		}
		return null;
	}

}
