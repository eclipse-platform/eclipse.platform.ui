/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.core.internal.resources.mapping.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class Utils {

	/**
	 * The SortOperation takes a collection of objects and returns a sorted
	 * collection of these objects. Concrete instances of this class provide
	 * the criteria for the sorting of the objects based on the type of the
	 * objects.
	 */
	static public abstract class Sorter {

		/**
		 * Returns true is elementTwo is 'greater than' elementOne This is the
		 * 'ordering' method of the sort operation. Each subclass overides this
		 * method with the particular implementation of the 'greater than'
		 * concept for the objects being sorted.
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
	 * @param Exception
	 *            the exception containing the error
	 * @param title
	 *            the title of the error dialog
	 * @param message
	 *            the message for the error dialog
	 * @param shell
	 *            the shell to open the error dialog in
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
		Display display = Display.getDefault();
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

	/**
	 * Initialize the given Action from a ResourceBundle.
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
	private static IResource[] getResources(Object[] elements, List nonResources, boolean isContributed) {
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
                isResource = true;
                getResources((ResourceMapping)element, resources);
			} else {
                Object adapted;
                if (isContributed) {
                    adapted = LegacyResourceSupport.getAdaptedResource(element);
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
                        adapted = LegacyResourceSupport.getAdaptedContributorResourceMapping(element);
                    } else {
                        adapted = getAdapter(element, ResourceMapping.class);
                    }
                    if (adapted instanceof ResourceMapping) {
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
		getResources(elements, nonResources, false);
		return nonResources.toArray();
	}
	
	public static IResource[] getResources(Object[] element) {
		return getResources(element, null, false);
	}
    
    public static IResource[] getContributedResources(Object[] elements) {
        return getResources(elements, null, true);
    }
	
	public static Object getAdapter(Object element, Class adapter) {
		if (element instanceof IAdaptable) {
			return ((IAdaptable) element).getAdapter(adapter);
		} 
		return null;
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
	 */
	public static String shortenText(int maxWidth, String textValue) {
		int length = textValue.length();
		if(length < maxWidth) return textValue;
		int ellipsisWidth = 3;
		int pivot = length / 2;
		int start = pivot;
		int end = pivot + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			int l1 = s1.length();
			int l2 = s2.length();
			if (l1 + ellipsisWidth + l2 < maxWidth) {
				return s1 + "..." + s2; //$NON-NLS-1$
			}
			start--;
			end++;
		}
		return textValue;
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

    public static SyncInfo getSyncInfo(ISynchronizeModelElement node) {
        if (node instanceof IAdaptable) {
            return (SyncInfo)((IAdaptable)node).getAdapter(SyncInfo.class);
        }
        return null;
    }
}
