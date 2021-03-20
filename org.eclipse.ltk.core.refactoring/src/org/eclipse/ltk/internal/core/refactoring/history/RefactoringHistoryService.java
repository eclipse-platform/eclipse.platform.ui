/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xml.sax.InputSource;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;
import org.eclipse.ltk.internal.core.refactoring.IRefactoringSerializationConstants;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.internal.core.refactoring.RefactoringSessionReader;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;

/**
 * Default implementation of a refactoring history service.
 *
 * @since 3.2
 */
public final class RefactoringHistoryService implements IRefactoringHistoryService {

	/** The null refactoring history */
	private static final class NullRefactoringHistory extends RefactoringHistory {

		/** The no proxies constant */
		private static final RefactoringDescriptorProxy[] NO_PROXIES= {};

		@Override
		public RefactoringDescriptorProxy[] getDescriptors() {
			return NO_PROXIES;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public RefactoringHistory removeAll(final RefactoringHistory history) {
			return this;
		}
	}

	/** The singleton history */
	private static RefactoringHistoryService fInstance= null;

	/** The refactoring history file */
	public static final String NAME_HISTORY_FILE= "refactorings.history"; //$NON-NLS-1$

	/** The refactoring history folder */
	public static final String NAME_HISTORY_FOLDER= ".refactorings"; //$NON-NLS-1$

	/** The refactoring history index file name */
	public static final String NAME_INDEX_FILE= "refactorings.index"; //$NON-NLS-1$

	/** The name of the special workspace project */
	public static final String NAME_WORKSPACE_PROJECT= ".workspace"; //$NON-NLS-1$

	/** The no history constant */
	private static final NullRefactoringHistory NO_HISTORY= new NullRefactoringHistory();

	/**
	 * Filters the given array of refactoring proxies and returns the result in
	 * the specified refactoring descriptor proxy set.
	 * <p>
	 * Clients wishing to benefit from the resolving of refactoring descriptors
	 * to determine its flags can set resolve to <code>true</code> if they
	 * would like to have resolved refactoring descriptor proxies as result.
	 * </p>
	 *
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param set
	 *            the result set
	 * @param resolve
	 *            <code>true</code> to return the filtered refactoring
	 *            descriptors as resolved refactoring proxies,
	 *            <code>false</code> otherwise
	 * @param flags
	 *            the refactoring descriptor flags which must be present in
	 *            order to be returned in the refactoring history object
	 * @param monitor
	 *            the progress monitor to use
	 */
	private static void filterRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, final Set<RefactoringDescriptorProxy> set, final boolean resolve, final int flags, final IProgressMonitor monitor) {
		Assert.isTrue(flags > RefactoringDescriptor.NONE);
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, proxies.length);
			for (RefactoringDescriptorProxy proxy : proxies) {
				final RefactoringDescriptor descriptor= proxy.requestDescriptor(new SubProgressMonitor(monitor, 1));
				if (descriptor != null) {
					final int filter= descriptor.getFlags();
					if ((filter | flags) == filter) {
						if (resolve) {
							set.add(new RefactoringDescriptorProxyAdapter(descriptor));
						} else {
							set.add(proxy);
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the singleton instance of the refactoring history.
	 *
	 * @return the singleton instance
	 */
	public static RefactoringHistoryService getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringHistoryService();
		return fInstance;
	}

	/**
	 * Returns whether a project has a shared refactoring history.
	 *
	 * @param project
	 *            the project to test
	 * @return <code>true</code> if the project has a shared project history,
	 *         <code>false</code> otherwise
	 */
	public static boolean hasSharedRefactoringHistory(final IProject project) {
		Assert.isNotNull(project);
		final IScopeContext[] contexts= new IScopeContext[] { new ProjectScope(project)};
		final String preference= Platform.getPreferencesService().getString(RefactoringCorePlugin.getPluginId(), RefactoringPreferenceConstants.PREFERENCE_SHARED_REFACTORING_HISTORY, Boolean.FALSE.toString(), contexts);
		if (preference != null)
			return Boolean.parseBoolean(preference);
		return false;
	}

	/**
	 * Determines whether a project has a shared refactoring history.
	 * <p>
	 * If a shared refactoring history is enabled, refactorings executed on that
	 * particular project are stored in a hidden refactoring history folder of
	 * the project folder. If no shared refactoring history is enabled, all
	 * refactorings are tracked as well, but persisted internally in a
	 * plugin-specific way without altering the project.
	 * </p>
	 * <p>
	 * Note: this method simply copies the content of the refactoring history
	 * folder to the location corresponding to the shared history setting.
	 * Clients wishing to programmatically change the refactoring history
	 * location have to update the preference
	 * {@link RefactoringPreferenceConstants#PREFERENCE_SHARED_REFACTORING_HISTORY}
	 * located in the preference store of the
	 * <code>org.eclipse.ltk.core.refactoring</code> plugin accordingly.
	 * </p>
	 *
	 * @param project
	 *            the project to set the shared refactoring history property
	 * @param enable
	 *            <code>true</code> to enable a shared refactoring history,
	 *            <code>false</code> otherwise
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while changing the shared refactoring
	 *             history property. Reasons include:
	 *             <ul>
	 *             <li>An I/O error occurs while changing the shared
	 *             refactoring history property.</li>
	 *             </ul>
	 */
	public static void setSharedRefactoringHistory(final IProject project, final boolean enable, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(project);
		Assert.isTrue(project.isAccessible());
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask("", 300); //$NON-NLS-1$
			final String name= project.getName();
			final URI uri= project.getLocationURI();
			if (uri != null) {
				try {
					final IFileStore history= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER);
					if (enable) {
						final IFileStore source= history.getChild(name);
						if (source.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20)).exists()) {
							IFileStore destination= EFS.getStore(uri).getChild(NAME_HISTORY_FOLDER);
							if (destination.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20)).exists())
								destination.delete(EFS.NONE, new SubProgressMonitor(monitor, 20));
							destination.mkdir(EFS.NONE, new SubProgressMonitor(monitor, 20));
							source.copy(destination, EFS.OVERWRITE, new SubProgressMonitor(monitor, 20));
							source.delete(EFS.NONE, new SubProgressMonitor(monitor, 20));
						}
					} else {
						final IFileStore source= EFS.getStore(uri).getChild(NAME_HISTORY_FOLDER);
						if (source.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20)).exists()) {
							IFileStore destination= history.getChild(name);
							if (destination.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 20)).exists())
								destination.delete(EFS.NONE, new SubProgressMonitor(monitor, 20));
							destination.mkdir(EFS.NONE, new SubProgressMonitor(monitor, 20));
							source.copy(destination, EFS.OVERWRITE, new SubProgressMonitor(monitor, 20));
							source.delete(EFS.NONE, new SubProgressMonitor(monitor, 20));
						}
					}
				} finally {
					if (enable)
						project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 30));
					else {
						final IFolder folder= project.getFolder(NAME_HISTORY_FOLDER);
						if (folder.exists())
							folder.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 30));
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/** The execution listeners */
	private final ListenerList<IRefactoringExecutionListener> fExecutionListeners= new ListenerList<>(ListenerList.EQUALITY);

	/** The history listeners */
	private final ListenerList<IRefactoringHistoryListener> fHistoryListeners= new ListenerList<>(ListenerList.EQUALITY);

	/** The operation listener, or <code>null</code> */
	private IOperationHistoryListener fOperationListener= null;

	/** The override time stamp */
	private long fOverrideTimeStamp= -1;

	/** The history reference count */
	private int fReferenceCount= 0;

	/** The resource listener, or <code>null</code> */
	private IResourceChangeListener fResourceListener= null;

	/** Maximal number of refactoring managers */
	private static final int MAX_MANAGERS= 2;

	/** The refactoring history manager cache */
	private final Map<IFileStore, RefactoringHistoryManager> fManagerCache= new LinkedHashMap<IFileStore, RefactoringHistoryManager>(MAX_MANAGERS, 0.75f, true) {

		private static final long serialVersionUID= 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<IFileStore, RefactoringHistoryManager> entry) {
			return size() > MAX_MANAGERS;
		}
	};

	/**
	 * Creates a new refactoring history.
	 */
	private RefactoringHistoryService() {
		// Do nothing
	}

	@Override
	public void addExecutionListener(final IRefactoringExecutionListener listener) {
		Assert.isNotNull(listener);
		fExecutionListeners.add(listener);
	}

	@Override
	public void addHistoryListener(final IRefactoringHistoryListener listener) {
		Assert.isNotNull(listener);
		fHistoryListeners.add(listener);
	}

	/**
	 * Adds the specified refactoring descriptor to the corresponding
	 * refactoring history.
	 * <p>
	 * If a descriptor with the same timestamp already exists, nothing happens.
	 * </p>
	 *
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 */
	public void addRefactoringDescriptor(final RefactoringDescriptorProxy proxy, IProgressMonitor monitor) {
		Assert.isNotNull(proxy);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			fireRefactoringHistoryEvent(proxy, RefactoringHistoryEvent.ADDED);
		} finally {
			monitor.done();
		}
	}

	@Override
	public void connect() {
		fReferenceCount++;
		if (fReferenceCount == 1) {
			fOperationListener= event -> performHistoryNotification(event);
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(fOperationListener);

			fResourceListener= event -> peformResourceChanged(event);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.POST_CHANGE);
		}
	}

	/**
	 * Deletes the specified refactoring descriptors from their associated
	 * refactoring histories.
	 *
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while deleting the refactoring
	 *             descriptors. Reasons include:
	 *             <ul>
	 *             <li>The refactoring history has an illegal format, contains
	 *             illegal arguments or otherwise illegal information.</li>
	 *             <li>An I/O error occurs while deleting the refactoring
	 *             descriptors from the refactoring history.</li>
	 *             </ul>
	 *
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_FORMAT_ERROR
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 */
	public void deleteRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(proxies);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings, proxies.length + 300);
			final Map<String, Collection<RefactoringDescriptorProxy>> projects= new HashMap<>();
			for (RefactoringDescriptorProxy proxy : proxies) {
				String project= proxy.getProject();
				if (project == null || "".equals(project)) //$NON-NLS-1$
					project= RefactoringHistoryService.NAME_WORKSPACE_PROJECT;
				Collection<RefactoringDescriptorProxy> collection= projects.get(project);
				if (collection == null) {
					collection= new ArrayList<>();
					projects.put(project, collection);
				}
				collection.add(proxy);
				monitor.worked(1);
			}
			final SubProgressMonitor subMonitor= new SubProgressMonitor(monitor, 300);
			try {
				final Set<Entry<String, Collection<RefactoringDescriptorProxy>>> entries= projects.entrySet();
				subMonitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings, entries.size());
				for (Entry<String, Collection<RefactoringDescriptorProxy>> entry : entries) {
					final Collection<RefactoringDescriptorProxy> collection= entry.getValue();
					String project= entry.getKey();
					if (RefactoringHistoryService.NAME_WORKSPACE_PROJECT.equals(project))
						project= null;
					final RefactoringHistoryManager manager= getManager(project);
					if (manager != null)
						manager.removeRefactoringDescriptors(collection.toArray(new RefactoringDescriptorProxy[collection.size()]), new SubProgressMonitor(subMonitor, 1), RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings);
					else
						subMonitor.worked(1);
				}
			} finally {
				subMonitor.done();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Deletes the specified refactoring descriptors from their associated
	 * refactoring histories.
	 *
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param query
	 *            the refactoring descriptor delete query to use
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while deleting the refactoring
	 *             descriptors. Reasons include:
	 *             <ul>
	 *             <li>The refactoring history has an illegal format, contains
	 *             illegal arguments or otherwise illegal information.</li>
	 *             <li>An I/O error occurs while deleting the refactoring
	 *             descriptors from the refactoring history.</li>
	 *             </ul>
	 *
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_FORMAT_ERROR
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 */
	public void deleteRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, final IRefactoringDescriptorDeleteQuery query, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(proxies);
		Assert.isNotNull(query);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings, proxies.length + 300);
			final Set<RefactoringDescriptorProxy> set= new HashSet<>(proxies.length);
			for (RefactoringDescriptorProxy proxy : proxies) {
				if (query.proceed(proxy).isOK()) {
					set.add(proxy);
				}
				monitor.worked(1);
			}
			if (!set.isEmpty()) {
				final RefactoringDescriptorProxy[] delete= set.toArray(new RefactoringDescriptorProxy[set.size()]);
				deleteRefactoringDescriptors(delete, new SubProgressMonitor(monitor, 300));
				for (RefactoringDescriptorProxy d : delete) {
					fireRefactoringHistoryEvent(d, RefactoringHistoryEvent.DELETED);
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Deletes the refactoring history of a project. Refactorings associated
	 * with the workspace are not deleted.
	 * <p>
	 * If a refactoring history is deleted, all files stored in the hidden
	 * refactoring history folder of the project folder are removed. If no
	 * shared refactoring history is enabled, the refactoring history
	 * information is removed from the internal workspace refactoring history.
	 * </p>
	 *
	 * @param project
	 *            the project to delete its history
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while deleting the refactoring history.
	 *             Reasons include:
	 *             <ul>
	 *             <li>An I/O error occurs while deleting the refactoring
	 *             history.</li>
	 *             </ul>
	 */
	public void deleteRefactoringHistory(final IProject project, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(project);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_deleting_refactorings, 100);
			final String name= project.getName();
			final IFileStore stateStore= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation());
			if (NAME_WORKSPACE_PROJECT.equals(name)) {
				final IFileStore metaStore= stateStore.getChild(NAME_HISTORY_FOLDER).getChild(name);
				metaStore.delete(EFS.NONE, new SubProgressMonitor(monitor, 100));
			} else {
				final URI uri= project.getLocationURI();
				if (uri != null && project.isAccessible()) {
					try {
						final IFileStore metaStore= stateStore.getChild(NAME_HISTORY_FOLDER).getChild(name);
						metaStore.delete(EFS.NONE, new SubProgressMonitor(monitor, 20));
						final IFileStore projectStore= EFS.getStore(uri).getChild(NAME_HISTORY_FOLDER);
						projectStore.delete(EFS.NONE, new SubProgressMonitor(monitor, 20));
					} finally {
						project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 60));
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public void disconnect() {
		if (fReferenceCount > 0) {
			fManagerCache.clear();
			fReferenceCount--;
		}
		if (fReferenceCount == 0) {
			if (fOperationListener != null)
				OperationHistoryFactory.getOperationHistory().removeOperationHistoryListener(fOperationListener);
			if (fResourceListener != null)
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fOperationListener= null;
		}
	}

	private void fireRefactoringExecutionEvent(final RefactoringDescriptorProxy proxy, final int eventType) {
		Assert.isNotNull(proxy);
		for (final IRefactoringExecutionListener listener : fExecutionListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				@Override
				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, eventType, proxy));
				}
			});
		}
	}

	private void fireRefactoringHistoryEvent(final RefactoringDescriptorProxy proxy, final int eventType) {
		Assert.isNotNull(proxy);
		for (final IRefactoringHistoryListener listener : fHistoryListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				@Override
				public void run() throws Exception {
					listener.historyNotification(new RefactoringHistoryEvent(RefactoringHistoryService.this, eventType, proxy));
				}
			});
		}
	}

	private boolean checkDescriptor(RefactoringDescriptor descriptor, IUndoableOperation operation) {
		Assert.isNotNull(descriptor);
		try {
			final Map<String, String> arguments= RefactoringHistoryManager.getArgumentMap(descriptor);
			if (arguments != null)
				RefactoringHistoryManager.checkArgumentMap(arguments);
		} catch (CoreException exception) {
			final IStatus status= exception.getStatus();
			if (status.getCode() == IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR) {
				final String time= DateFormat.getDateTimeInstance().format(new Date(descriptor.getTimeStamp()));
				final String message= "The refactoring executed at " + time + " contributed a refactoring descriptor with invalid format:"; //$NON-NLS-1$//$NON-NLS-2$
				final IStatus comment= new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), descriptor.getComment());
				RefactoringCorePlugin.log(new MultiStatus(RefactoringCorePlugin.getPluginId(), 0, new IStatus[] { comment}, message, null));
			}
			RefactoringCorePlugin.log(exception);

			if (operation instanceof TriggeredOperations) {
				operation= ((TriggeredOperations) operation).getTriggeringOperation();
			}
			if (operation instanceof UndoableOperation2ChangeAdapter) {
				((UndoableOperation2ChangeAdapter) operation).setChangeDescriptor(null);
			}
			return false;
		}
		return true;
	}

	@Override
	public RefactoringHistory getProjectHistory(final IProject project, IProgressMonitor monitor) {
		return getProjectHistory(project, 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, monitor);
	}

	@Override
	public RefactoringHistory getProjectHistory(final IProject project, final long start, final long end, final int flags, IProgressMonitor monitor) {
		Assert.isNotNull(project);
		Assert.isTrue(project.exists());
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= 0);
		Assert.isTrue(flags >= RefactoringDescriptor.NONE);
		if (project.isOpen()) {
			if (monitor == null)
				monitor= new NullProgressMonitor();
			try {
				monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 120);
				final String name= project.getName();
				final RefactoringHistoryManager manager= getManager(name);
				if (manager != null) {
					RefactoringHistory history= manager.readRefactoringHistory(start, end, new SubProgressMonitor(monitor, 20));
					if (flags > RefactoringDescriptor.NONE) {
						final Set<RefactoringDescriptorProxy> set= new HashSet<>();
						filterRefactoringDescriptors(history.getDescriptors(), set, false, flags, new SubProgressMonitor(monitor, 100));
						history= new RefactoringHistoryImplementation(set.toArray(new RefactoringDescriptorProxy[set.size()]));
					}
					return history;
				}
			} finally {
				monitor.done();
			}
		}
		return NO_HISTORY;
	}

	@Override
	public RefactoringHistory getRefactoringHistory(final IProject[] projects, final IProgressMonitor monitor) {
		return getRefactoringHistory(projects, 0, Long.MAX_VALUE, RefactoringDescriptor.NONE, monitor);
	}

	@Override
	public RefactoringHistory getRefactoringHistory(final IProject[] projects, final long start, final long end, final int flags, IProgressMonitor monitor) {
		Assert.isNotNull(projects);
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= start);
		Assert.isTrue(flags >= RefactoringDescriptor.NONE);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 3 * projects.length);
			final Set<RefactoringDescriptorProxy> set= new HashSet<>();
			if (flags > RefactoringDescriptor.NONE) {
				for (IProject project : projects) {
					if (project.isAccessible()) {
						final RefactoringDescriptorProxy[] proxies= getProjectHistory(project, start, end, flags, new SubProgressMonitor(monitor, 1)).getDescriptors();
						filterRefactoringDescriptors(proxies, set, false, flags, new SubProgressMonitor(monitor, 2));
					}
				}
			} else {
				for (IProject project : projects) {
					if (project.isAccessible()) {
						final RefactoringDescriptorProxy[] proxies= getProjectHistory(project, start, end, RefactoringDescriptor.NONE, new SubProgressMonitor(monitor, 3)).getDescriptors();
						set.addAll(Arrays.asList(proxies));
					}
				}
			}
			final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[set.size()];
			set.toArray(proxies);
			return new RefactoringHistoryImplementation(proxies);
		} finally {
			monitor.done();
		}
	}

	@Override
	public RefactoringHistory getWorkspaceHistory(IProgressMonitor monitor) {
		return getWorkspaceHistory(0, Long.MAX_VALUE, monitor);
	}

	@Override
	public RefactoringHistory getWorkspaceHistory(final long start, final long end, IProgressMonitor monitor) {
		return getRefactoringHistory(ResourcesPlugin.getWorkspace().getRoot().getProjects(), start, end, RefactoringDescriptor.NONE, monitor);
	}

	/**
	 * Reads refactoring descriptor proxies from the input stream.
	 * <p>
	 * Note that calling this method with a flag argument unequal to
	 * <code>RefactoringDescriptor#NONE</code> may result in a performance
	 * degradation, since the actual descriptors have to be eagerly resolved.
	 * This in turn results in faster execution of any subsequent calls to
	 * {@link RefactoringDescriptorProxy#requestDescriptor(IProgressMonitor)}
	 * which try to request a descriptor from the returned refactoring history.
	 * </p>
	 *
	 * @param stream
	 *            the input stream to read from
	 * @return the refactoring descriptor proxies
	 * @throws CoreException
	 *             if an error occurs while reading the refactoring descriptor
	 *             proxies
	 */
	public RefactoringDescriptorProxy[] readRefactoringDescriptorProxies(final InputStream stream) throws CoreException {
		Assert.isNotNull(stream);
		try {
			return RefactoringHistoryManager.readRefactoringDescriptorProxies(stream, null, 0, Long.MAX_VALUE);
		} catch (IOException exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
		}
	}

	@Override
	public RefactoringHistory readRefactoringHistory(final InputStream stream, final int flags) throws CoreException {
		Assert.isNotNull(stream);
		Assert.isTrue(flags >= RefactoringDescriptor.NONE);
		final List<RefactoringDescriptor> list= new ArrayList<>();
		final RefactoringSessionDescriptor descriptor= new RefactoringSessionReader(false, null).readSession(new InputSource(stream));
		if (descriptor != null) {
			final RefactoringDescriptor[] descriptors= descriptor.getRefactorings();
			if (flags > RefactoringDescriptor.NONE) {
				for (RefactoringDescriptor d : descriptors) {
					final int current= d.getFlags();
					if ((current | flags) == current) {
						list.add(d);
					}
				}
			} else
				list.addAll(Arrays.asList(descriptors));
		}
		final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[list.size()];
		for (int index= 0; index < list.size(); index++)
			proxies[index]= new RefactoringDescriptorProxyAdapter(list.get(index));
		return new RefactoringHistoryImplementation(proxies);
	}

	@Override
	public void removeExecutionListener(final IRefactoringExecutionListener listener) {
		Assert.isNotNull(listener);
		fExecutionListeners.remove(listener);
	}

	@Override
	public void removeHistoryListener(final IRefactoringHistoryListener listener) {
		Assert.isNotNull(listener);
		fHistoryListeners.remove(listener);
	}

	/**
	 * Returns the resolved refactoring descriptor associated with the specified
	 * proxy.
	 * <p>
	 * The refactoring history must be in connected state.
	 * </p>
	 *
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 *
	 * @return the resolved refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor requestDescriptor(final RefactoringDescriptorProxy proxy, IProgressMonitor monitor) {
		Assert.isNotNull(proxy);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			final RefactoringHistoryManager manager= getManager(proxy.getProject());
			if (manager != null)
				return manager.requestDescriptor(proxy, monitor);
		} finally {
			monitor.done();
		}
		return null;
	}

	/**
	 * Sets the override time stamp for the next refactoring performed.
	 *
	 * @param stamp
	 *            the override time stamp, or <code>-1</code> to clear it
	 */
	public void setOverrideTimeStamp(final long stamp) {
		Assert.isTrue(stamp == -1 || stamp >= 0);
		fOverrideTimeStamp= stamp;
	}

	@Override
	public void writeRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, final OutputStream stream, final int flags, final boolean time, IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(proxies);
		Assert.isNotNull(stream);
		Assert.isTrue(flags >= RefactoringDescriptor.NONE);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask("", 100 * proxies.length); //$NON-NLS-1$
			connect();
			final List<RefactoringDescriptor> list= new ArrayList<>(proxies.length);
			for (RefactoringDescriptorProxy proxy : proxies) {
				final RefactoringDescriptor descriptor= proxy.requestDescriptor(new SubProgressMonitor(monitor, 100));
				if (descriptor != null) {
					final int current= descriptor.getFlags();
					if ((current | flags) == current)
						list.add(descriptor);
				}
			}
			final RefactoringDescriptor[] descriptors= new RefactoringDescriptor[list.size()];
			list.toArray(descriptors);
			RefactoringHistoryManager.writeRefactoringSession(stream, new RefactoringSessionDescriptor(descriptors, IRefactoringSerializationConstants.CURRENT_VERSION, null), time);
		} finally {
			disconnect();
		}
	}

	@Override
	public void writeRefactoringSession(final RefactoringSessionDescriptor descriptor, final OutputStream stream, final boolean time) throws CoreException {
		Assert.isNotNull(descriptor);
		Assert.isNotNull(stream);
		RefactoringHistoryManager.writeRefactoringSession(stream, descriptor, time);
	}

	/**
	 * Moves the project history from the old project to the new one.
	 *
	 * @param oldProject
	 *            the old project, which does not exist anymore
	 * @param newProject
	 *            the new project, which already exists
	 * @param monitor
	 *            the progress monitor to use
	 */
	private void moveHistory(final IProject oldProject, final IProject newProject, final IProgressMonitor monitor) {
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_updating_history, 60);
			final IFileStore historyStore= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER);
			final String oldName= oldProject.getName();
			final String newName= newProject.getName();
			final IFileStore oldStore= historyStore.getChild(oldName);
			if (oldStore.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists()) {
				final IFileStore newStore= historyStore.getChild(newName);
				if (newStore.fetchInfo(EFS.NONE, new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)).exists())
					newStore.delete(EFS.NONE, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
				oldStore.move(newStore, EFS.OVERWRITE, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			}
		} catch (CoreException exception) {
			RefactoringCorePlugin.log(exception);
		} finally {
			monitor.done();
		}
	}

	private void peformResourceChanged(final IResourceChangeEvent event) {
		final int type= event.getType();
		if ((type & IResourceChangeEvent.POST_CHANGE) != 0) {
			final IResourceDelta delta= event.getDelta();
			if (delta != null) {
				final IResourceDelta[] deltas= delta.getAffectedChildren();
				if (deltas.length == 2) {
					final IPath toPath= deltas[0].getMovedToPath();
					final IPath fromPath= deltas[1].getMovedFromPath();
					if (fromPath != null && toPath != null) {
						final IResource oldResource= deltas[0].getResource();
						final IResource newResource= deltas[1].getResource();
						if (oldResource.getType() == IResource.PROJECT && newResource.getType() == IResource.PROJECT)
							moveHistory((IProject) oldResource, (IProject) newResource, new NullProgressMonitor());
					} else {
						if (deltas[0].getKind() == IResourceDelta.ADDED && deltas[1].getKind() == IResourceDelta.REMOVED) {
							final IResource newResource= deltas[0].getResource();
							final IResource oldResource= deltas[1].getResource();
							if (oldResource.getType() == IResource.PROJECT && newResource.getType() == IResource.PROJECT)
								moveHistory((IProject) oldResource, (IProject) newResource, new NullProgressMonitor());
						}
					}
				}
			}
		}
	}

	private RefactoringDescriptor getRefactoringDescriptor(IUndoableOperation operation) {
		if (operation instanceof TriggeredOperations) {
			operation= ((TriggeredOperations) operation).getTriggeringOperation();
		}
		if (operation instanceof UndoableOperation2ChangeAdapter) {
			ChangeDescriptor changeDescriptor= ((UndoableOperation2ChangeAdapter) operation).getChangeDescriptor();
			if (changeDescriptor instanceof RefactoringChangeDescriptor) {
				return ((RefactoringChangeDescriptor) changeDescriptor).getRefactoringDescriptor();
			}
		}
		return null;
	}

	private void performHistoryNotification(final OperationHistoryEvent event) {
		RefactoringDescriptor descriptor= getRefactoringDescriptor(event.getOperation());
		if (descriptor != null) {
			RefactoringDescriptorProxyAdapter proxy= new RefactoringDescriptorProxyAdapter(descriptor);
			switch (event.getEventType()) {
				case OperationHistoryEvent.ABOUT_TO_EXECUTE: {
					if (checkDescriptor(descriptor, event.getOperation())) {
						fireRefactoringExecutionEvent(proxy, RefactoringExecutionEvent.ABOUT_TO_PERFORM);
					}
					break;
				}
				case OperationHistoryEvent.DONE: {
					if (!RefactoringDescriptor.ID_UNKNOWN.equals(descriptor.getID())) {
						long timeStamp= fOverrideTimeStamp >= 0 ? fOverrideTimeStamp : System.currentTimeMillis();
						descriptor.setTimeStamp(timeStamp);
					}

					fireRefactoringHistoryEvent(proxy, RefactoringHistoryEvent.PUSHED);
					fireRefactoringExecutionEvent(proxy, RefactoringExecutionEvent.PERFORMED);
					break;
				}
				case OperationHistoryEvent.ABOUT_TO_UNDO: {
					fireRefactoringExecutionEvent(proxy, RefactoringExecutionEvent.ABOUT_TO_UNDO);
					break;
				}
				case OperationHistoryEvent.UNDONE: {
					fireRefactoringHistoryEvent(proxy, RefactoringHistoryEvent.POPPED);
					fireRefactoringExecutionEvent(proxy, RefactoringExecutionEvent.UNDONE);
					break;
				}
				case OperationHistoryEvent.ABOUT_TO_REDO: {
					fireRefactoringExecutionEvent(proxy, RefactoringExecutionEvent.ABOUT_TO_REDO);
					break;
				}
				case OperationHistoryEvent.REDONE: {
					fireRefactoringHistoryEvent(proxy, RefactoringHistoryEvent.PUSHED);
					fireRefactoringExecutionEvent(proxy, RefactoringExecutionEvent.REDONE);
					break;
				}
			}
		}
	}

	/**
	 * Returns the refactoring history manager corresponding to the project
	 * with the specified name.
	 *
	 * @param name
	 *            the name of the project, or <code>null</code> for the
	 *            workspace
	 * @return the refactoring history manager, or <code>null</code>
	 */
	private RefactoringHistoryManager getManager(final String name) {
		final IFileStore store= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER);
		if (name != null && !"".equals(name)) {//$NON-NLS-1$
			try {
				final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (project.isAccessible()) {
					if (hasSharedRefactoringHistory(project)) {
						final URI uri= project.getLocationURI();
						if (uri != null)
							return getManager(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER), name);
					} else
						return getManager(store.getChild(name), name);
				}
			} catch (CoreException exception) {
				// Do nothing
			}
		} else
			return getManager(store.getChild(NAME_WORKSPACE_PROJECT), null);
		return null;
	}

	/**
	 * Returns the cached refactoring history manager for the specified
	 * history location.
	 *
	 * @param store
	 *            the file store describing the history location
	 * @param name
	 *            the non-empty project name, or <code>null</code> for the
	 *            workspace
	 * @return the refactoring history manager
	 */
	private RefactoringHistoryManager getManager(final IFileStore store, final String name) {
		Assert.isNotNull(store);
		RefactoringHistoryManager manager= fManagerCache.get(store);
		if (manager == null) {
			manager= new RefactoringHistoryManager(store, name);
			fManagerCache.put(store, manager);
		}
		return manager;
	}

}
