/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;

import org.xml.sax.InputSource;

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

		/**
		 * {@inheritDoc}
		 */
		public RefactoringDescriptorProxy[] getDescriptors() {
			return NO_PROXIES;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEmpty() {
			return true;
		}
	}

	/** Stack of refactoring descriptors */
	private final class RefactoringDescriptorStack {

		/** Maximal number of refactoring managers */
		private static final int MAX_MANAGERS= 2;

		/** The internal implementation */
		private final LinkedList fImplementation= new LinkedList();

		/**
		 * The refactoring history manager cache (element type:
		 * <code>&lt;IFileStore, RefactoringHistoryManager&gt;</code>)
		 */
		private final Map fManagerCache= new LinkedHashMap(MAX_MANAGERS, 0.75f, true) {

			private static final long serialVersionUID= 1L;

			protected final boolean removeEldestEntry(final Map.Entry entry) {
				return size() > MAX_MANAGERS;
			}
		};

		/**
		 * Returns the cached refactoring history manager for the specified
		 * history location.
		 * 
		 * @param store
		 *            the file store describing the history location
		 * @param name
		 *            the non-empty project name, or <code>null</code>
		 * @return the refactoring history manager
		 */
		private RefactoringHistoryManager getManager(final IFileStore store, final String name) {
			Assert.isNotNull(store);
			Assert.isNotNull(name);
			RefactoringHistoryManager manager= (RefactoringHistoryManager) fManagerCache.get(store);
			if (manager == null) {
				manager= new RefactoringHistoryManager(store, name);
				fManagerCache.put(store, manager);
			}
			return manager;
		}

		/**
		 * Returns the current descriptor on the top of the stack.
		 * 
		 * @return the current descriptor on top
		 * @throws EmptyStackException
		 *             if the stack is empty
		 */
		private RefactoringDescriptor peek() throws EmptyStackException {
			if (!fImplementation.isEmpty())
				return (RefactoringDescriptor) fImplementation.getFirst();
			throw new EmptyStackException();
		}

		/**
		 * Pops the top descriptor off the stack.
		 * 
		 * @throws EmptyStackException
		 *             if the stack is empty
		 */
		private void pop() throws EmptyStackException {
			final RefactoringDescriptor descriptor= peek();
			if (!fImplementation.isEmpty())
				fImplementation.removeFirst();
			else
				throw new EmptyStackException();
			for (int index= 0; index < fHistoryListeners.size(); index++) {
				final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
				Platform.run(new ISafeRunnable() {

					public void handleException(final Throwable throwable) {
						RefactoringCorePlugin.log(throwable);
					}

					public void run() throws Exception {
						listener.historyNotification(new RefactoringHistoryEvent(RefactoringHistoryService.this, RefactoringHistoryEvent.REMOVED, descriptor));
					}
				});
			}
		}

		/**
		 * Pushes the given descriptor onto the stack.
		 * 
		 * @param descriptor
		 *            the descriptor to push onto the stack
		 */
		private void push(final RefactoringDescriptor descriptor) {
			Assert.isNotNull(descriptor);
			fImplementation.addFirst(descriptor);
			final int size= fImplementation.size();
			if (size > MAX_UNDO_STACK)
				fImplementation.removeLast();
			for (int index= 0; index < fHistoryListeners.size(); index++) {
				final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
				Platform.run(new ISafeRunnable() {

					public void handleException(final Throwable throwable) {
						RefactoringCorePlugin.log(throwable);
					}

					public void run() throws Exception {
						listener.historyNotification(new RefactoringHistoryEvent(RefactoringHistoryService.this, RefactoringHistoryEvent.ADDED, descriptor));
					}
				});
			}
		}

		/**
		 * Returns the descriptor the specified proxy points to.
		 * 
		 * @param proxy
		 *            the refactoring descriptor proxy
		 * @param monitor
		 *            the progress monitor to use
		 * @return the associated refactoring descriptor, or <code>null</code>
		 */
		private RefactoringDescriptor requestDescriptor(final RefactoringDescriptorProxy proxy, final IProgressMonitor monitor) {
			Assert.isNotNull(proxy);
			Assert.isNotNull(monitor);
			try {
				monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_resolving_information, 12);
				final long stamp= proxy.getTimeStamp();
				RefactoringDescriptor descriptor= null;
				final IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 4);
				try {
					subMonitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_resolving_information, fImplementation.size());
					for (final Iterator iterator= fImplementation.iterator(); iterator.hasNext();) {
						descriptor= (RefactoringDescriptor) iterator.next();
						subMonitor.worked(1);
						if (descriptor.getTimeStamp() == stamp)
							break;
					}
				} finally {
					subMonitor.done();
				}
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				monitor.worked(1);
				if (descriptor == null) {
					final String name= proxy.getProject();
					final IFileStore store= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER);
					if (name != null && !"".equals(name)) {//$NON-NLS-1$
						try {
							final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
							if (project.isAccessible()) {
								if (hasProjectHistory(project)) {
									final URI uri= project.getLocationURI();
									if (uri != null)
										return getManager(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER), name).requestDescriptor(proxy, new SubProgressMonitor(monitor, 1));
								} else
									return getManager(store.getChild(name), name).requestDescriptor(proxy, new SubProgressMonitor(monitor, 1));
							}
						} catch (CoreException exception) {
							// Do nothing
						}
					} else
						return getManager(store.getChild(NAME_WORKSPACE_PROJECT), null).requestDescriptor(proxy, new SubProgressMonitor(monitor, 1));
				}
				monitor.worked(6);
				return descriptor;
			} finally {
				monitor.done();
			}
		}
	}

	/** Operation history listener for refactoring operation events */
	private final class RefactoringOperationHistoryListener implements IOperationHistoryListener {

		/** The last recently performed refactoring */
		private RefactoringDescriptor fDescriptor= null;

		/**
		 * {@inheritDoc}
		 */
		public void historyNotification(final OperationHistoryEvent event) {
			IUndoableOperation operation= event.getOperation();
			if (operation instanceof TriggeredOperations)
				operation= ((TriggeredOperations) operation).getTriggeringOperation();
			UndoableOperation2ChangeAdapter adapter= null;
			if (operation instanceof UndoableOperation2ChangeAdapter)
				adapter= (UndoableOperation2ChangeAdapter) operation;
			if (adapter != null) {
				final Change change= adapter.getChange();
				switch (event.getEventType()) {
					case OperationHistoryEvent.ABOUT_TO_EXECUTE:
						fDescriptor= change.getRefactoringDescriptor();
						if (fDescriptor != null)
							fireAboutToPerformEvent(fDescriptor);
						else
							fireAboutToPerformEvent(fUnknownDescriptor);
						break;
					case OperationHistoryEvent.DONE:
						handleRefactoringPerformed(fDescriptor);
						if (fDescriptor != null)
							fireRefactoringPerformedEvent(fDescriptor);
						else
							fireRefactoringPerformedEvent(fUnknownDescriptor);
						fDescriptor= null;
						break;
					case OperationHistoryEvent.ABOUT_TO_UNDO:
						fireAboutToUndoEvent(fUndoStack.peek());
						break;
					case OperationHistoryEvent.UNDONE:
						handleChangeUndone();
						fireRefactoringUndoneEvent((RefactoringDescriptor) fRedoQueue.getFirst());
						break;
					case OperationHistoryEvent.ABOUT_TO_REDO:
						fireAboutToRedoEvent((RefactoringDescriptor) fRedoQueue.getFirst());
						break;
					case OperationHistoryEvent.REDONE:
						handleChangeRedone();
						fireRefactoringRedoneEvent(fUndoStack.peek());
						break;
				}
			}
		}
	}

	/** Refactoring descriptor for changes which do not return a descriptor */
	private final class UnknownRefactoringDescriptor extends RefactoringDescriptor {

		/**
		 * Creates a new unknown refactoring descriptor.
		 */
		private UnknownRefactoringDescriptor() {
			super(UNKNOWN_REFACTORING_ID, null, RefactoringCoreMessages.RefactoringHistoryService_unknown_refactoring_description, null, Collections.EMPTY_MAP);
		}
	}

	/** Workspace resource change listener */
	private final class WorkspaceChangeListener implements IResourceChangeListener {

		/**
		 * Removes refactoring descriptors from the global refactoring history.
		 * 
		 * @param event
		 *            the resource change event
		 */
		private void removeDescriptors(final IResourceChangeEvent event) {
			Assert.isNotNull(event);
			final IResource resource= event.getResource();
			if (resource != null) {
				final int type= resource.getType();
				if (type == IResource.PROJECT) {
					final IProject project= (IProject) resource;
					if (project.exists()) {
						try {
							final URI uri= project.getLocationURI();
							if (uri != null)
								EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER).getChild(project.getName()).delete(EFS.NONE, null);
						} catch (CoreException exception) {
							RefactoringCorePlugin.log(exception);
						}
					}
				}
			}
		}

		/**
		 * Resets the refactoring history stacks.
		 * 
		 * @param event
		 *            the resource change event
		 */
		private void resetStacks(final IResourceChangeEvent event) {
			Assert.isNotNull(event);
			final IResource resource= event.getResource();
			if (resource != null) {
				final int type= resource.getType();
				if (type == IResource.PROJECT) {
					if (fUndoStack != null)
						fUndoStack.fImplementation.clear();
					if (fRedoQueue != null)
						fRedoQueue.clear();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void resourceChanged(final IResourceChangeEvent event) {
			final int type= event.getType();
			if ((type & IResourceChangeEvent.PRE_DELETE) != 0) {
				resetStacks(event);
				removeDescriptors(event);
			} else if ((type & IResourceChangeEvent.PRE_CLOSE) != 0)
				resetStacks(event);
		}
	}

	/** The singleton history */
	private static RefactoringHistoryService fInstance= null;

	/** The maximum size of the undo stack */
	private static final int MAX_UNDO_STACK= 5;

	/** The refactoring history file */
	public static final String NAME_HISTORY_FILE= "refactorings.history"; //$NON-NLS-1$

	/** The refactoring history folder */
	public static final String NAME_HISTORY_FOLDER= ".refactorings"; //$NON-NLS-1$

	/** The refactoring history index file name */
	public static final String NAME_INDEX_FILE= "refactorings.index"; //$NON-NLS-1$

	/** The name of the special workspace project */
	static final String NAME_WORKSPACE_PROJECT= ".workspace"; //$NON-NLS-1$

	/** The no history constant */
	private static final NullRefactoringHistory NO_HISTORY= new NullRefactoringHistory();

	/** The unknown refactoring id */
	private static final String UNKNOWN_REFACTORING_ID= "org.eclipse.ltk.core.refactoring.unknown.refactoring"; //$NON-NLS-1$

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

	/** The execution listeners */
	private final List fExecutionListeners= new ArrayList(2);

	/** The history listeners */
	private final List fHistoryListeners= new ArrayList(2);

	/** The operation listener, or <code>null</code> */
	private IOperationHistoryListener fOperationListener= null;

	/** The redo refactoring descriptor queue, or <code>null</code> */
	private LinkedList fRedoQueue= null;

	/** The history reference count */
	private int fReferenceCount= 0;

	/** The resource listener, or <code>null</code> */
	private IResourceChangeListener fResourceListener= null;

	/** The undo refactoring descriptor stack, or <code>null</code> */
	private RefactoringDescriptorStack fUndoStack= null;

	/** The unknown refactoring descriptor */
	private final RefactoringDescriptor fUnknownDescriptor= new UnknownRefactoringDescriptor();

	/**
	 * Creates a new refactoring history.
	 */
	private RefactoringHistoryService() {
		// Not for instantiation
	}

	/**
	 * {@inheritDoc}
	 */
	public void addExecutionListener(final IRefactoringExecutionListener listener) {
		Assert.isNotNull(listener);
		if (!fExecutionListeners.contains(listener))
			fExecutionListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addHistoryListener(final IRefactoringHistoryListener listener) {
		Assert.isNotNull(listener);
		if (!fHistoryListeners.contains(listener))
			fHistoryListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void connect() {
		fReferenceCount++;
		if (fReferenceCount == 1) {
			fOperationListener= new RefactoringOperationHistoryListener();
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(fOperationListener);
			fResourceListener= new WorkspaceChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.POST_CHANGE);
			fUndoStack= new RefactoringDescriptorStack();
			fRedoQueue= new LinkedList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void disconnect() {
		if (fReferenceCount > 0) {
			fUndoStack.fManagerCache.clear();
			fReferenceCount--;
		}
		if (fReferenceCount == 0) {
			if (fOperationListener != null)
				OperationHistoryFactory.getOperationHistory().removeOperationHistoryListener(fOperationListener);
			if (fResourceListener != null)
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fUndoStack= null;
			fRedoQueue= null;
			fOperationListener= null;
		}
	}

	/**
	 * Fires the about to perform event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	void fireAboutToPerformEvent(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		for (int index= 0; index < fExecutionListeners.size(); index++) {
			final IRefactoringExecutionListener listener= (IRefactoringExecutionListener) fExecutionListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public final void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, RefactoringExecutionEvent.ABOUT_TO_PERFORM, descriptor));
				}
			});
		}
	}

	/**
	 * Fires the about to redo event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	void fireAboutToRedoEvent(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		for (int index= 0; index < fExecutionListeners.size(); index++) {
			final IRefactoringExecutionListener listener= (IRefactoringExecutionListener) fExecutionListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, RefactoringExecutionEvent.ABOUT_TO_REDO, descriptor));
				}
			});
		}
	}

	/**
	 * Fires the about to undo event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	void fireAboutToUndoEvent(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		for (int index= 0; index < fExecutionListeners.size(); index++) {
			final IRefactoringExecutionListener listener= (IRefactoringExecutionListener) fExecutionListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, RefactoringExecutionEvent.ABOUT_TO_UNDO, descriptor));
				}
			});
		}
	}

	/**
	 * Fires the refactoring performed event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	void fireRefactoringPerformedEvent(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		for (int index= 0; index < fExecutionListeners.size(); index++) {
			final IRefactoringExecutionListener listener= (IRefactoringExecutionListener) fExecutionListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, RefactoringExecutionEvent.PERFORMED, descriptor));
				}
			});
		}
	}

	/**
	 * Fires the refactoring redone event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	void fireRefactoringRedoneEvent(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		for (int index= 0; index < fExecutionListeners.size(); index++) {
			final IRefactoringExecutionListener listener= (IRefactoringExecutionListener) fExecutionListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, RefactoringExecutionEvent.REDONE, descriptor));
				}
			});
		}
	}

	/**
	 * Fires the refactoring undone event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	void fireRefactoringUndoneEvent(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		for (int index= 0; index < fExecutionListeners.size(); index++) {
			final IRefactoringExecutionListener listener= (IRefactoringExecutionListener) fExecutionListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.executionNotification(new RefactoringExecutionEvent(RefactoringHistoryService.this, RefactoringExecutionEvent.UNDONE, descriptor));
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getProjectHistory(final IProject project, IProgressMonitor monitor) {
		return getProjectHistory(project, 0, Long.MAX_VALUE, monitor);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getProjectHistory(final IProject project, final long start, final long end, IProgressMonitor monitor) {
		Assert.isNotNull(project);
		Assert.isTrue(project.exists());
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= 0);
		if (project.isOpen()) {
			if (monitor == null)
				monitor= new NullProgressMonitor();
			try {
				monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, 12);
				final String name= project.getName();
				if (hasProjectHistory(project)) {
					final URI uri= project.getLocationURI();
					if (uri != null)
						return fUndoStack.getManager(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER), name).readRefactoringHistory(start, end, new SubProgressMonitor(monitor, 12));
				} else
					return fUndoStack.getManager(EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER).getChild(name), name).readRefactoringHistory(start, end, new SubProgressMonitor(monitor, 12));
			} catch (CoreException exception) {
				RefactoringCorePlugin.log(exception);
			} finally {
				monitor.done();
			}
		}
		return NO_HISTORY;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getRefactoringHistory(final IProject[] projects, final IProgressMonitor monitor) {
		return getRefactoringHistory(projects, 0, Long.MAX_VALUE, monitor);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getRefactoringHistory(final IProject[] projects, final long start, final long end, IProgressMonitor monitor) {
		Assert.isNotNull(projects);
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= start);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		try {
			monitor.beginTask(RefactoringCoreMessages.RefactoringHistoryService_retrieving_history, projects.length);
			final List list= new ArrayList();
			for (int index= 0; index < projects.length; index++) {
				final IProject project= projects[index];
				if (project.isAccessible()) {
					final RefactoringDescriptorProxy[] proxies= getProjectHistory(project, start, end, new SubProgressMonitor(monitor, 1)).getDescriptors();
					for (int offset= 0; offset < proxies.length; offset++)
						list.add(proxies[offset]);
				}
			}
			final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[list.size()];
			list.toArray(proxies);
			return new RefactoringHistoryImplementation(proxies);
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getWorkspaceHistory(IProgressMonitor monitor) {
		return getWorkspaceHistory(0, Long.MAX_VALUE, monitor);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getWorkspaceHistory(final long start, final long end, IProgressMonitor monitor) {
		return getRefactoringHistory(ResourcesPlugin.getWorkspace().getRoot().getProjects(), start, end, monitor);
	}

	/**
	 * Handles the change redone event.
	 */
	void handleChangeRedone() {
		fUndoStack.push((RefactoringDescriptor) fRedoQueue.removeFirst());
	}

	/**
	 * Handles the change undone event.
	 */
	void handleChangeUndone() {
		fRedoQueue.addFirst(fUndoStack.peek());
		fUndoStack.pop();
	}

	/**
	 * Handles the refactoring performed event.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor describing the refactoring, or
	 *            <code>null</code>
	 */
	void handleRefactoringPerformed(final RefactoringDescriptor descriptor) {
		if (descriptor != null) {
			descriptor.setTimeStamp(System.currentTimeMillis());
			fUndoStack.push(descriptor);
		} else
			fUndoStack.push(fUnknownDescriptor);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasProjectHistory(final IProject project) {
		Assert.isNotNull(project);
		final IScopeContext[] contexts= new IScopeContext[] { new ProjectScope(project)};
		final String preference= Platform.getPreferencesService().getString(RefactoringCorePlugin.getPluginId(), RefactoringPreferenceConstants.PREFERENCE_ENABLE_PROJECT_REFACTORING_HISTORY, Boolean.FALSE.toString(), contexts);
		if (preference != null)
			return Boolean.valueOf(preference).booleanValue();
		return false;
	}

	/**
	 * Reads refactoring descriptor proxies from the input stream.
	 * 
	 * @param stream
	 *            the input stream
	 * @return the refactoring descriptor proxies
	 * @throws CoreException
	 *             if an error occurs
	 */
	public RefactoringDescriptorProxy[] readRefactoringDescriptorProxies(final InputStream stream) throws CoreException {
		Assert.isNotNull(stream);
		try {
			return RefactoringHistoryManager.readRefactoringDescriptorProxies(stream, null, 0, Long.MAX_VALUE);
		} catch (IOException exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory readRefactoringHistory(final InputStream stream) throws CoreException {
		Assert.isNotNull(stream);
		final RefactoringSessionDescriptor descriptor= new RefactoringHistoryReader().readSession(new InputSource(stream));
		final RefactoringDescriptor[] descriptors= descriptor.getRefactorings();
		final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[descriptors.length];
		for (int index= 0; index < descriptors.length; index++)
			proxies[index]= new RefactoringDescriptorProxyAdapter(descriptors[index]);
		return new RefactoringHistoryImplementation(proxies);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeExecutionListener(final IRefactoringExecutionListener listener) {
		Assert.isNotNull(listener);
		fExecutionListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeHistoryListener(final IRefactoringHistoryListener listener) {
		Assert.isNotNull(listener);
		fHistoryListeners.remove(listener);
	}

	/**
	 * Returns the descriptor the specified proxy points to.
	 * <p>
	 * The refactoring history must be in connected state.
	 * </p>
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * 
	 * @return the associated refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor requestDescriptor(final RefactoringDescriptorProxy proxy, IProgressMonitor monitor) {
		Assert.isNotNull(proxy);
		Assert.isNotNull(fUndoStack);
		if (monitor == null)
			monitor= new NullProgressMonitor();
		return fUndoStack.requestDescriptor(proxy, monitor);
	}

	/**
	 * Returns the descriptor the specified handle points to.
	 * <p>
	 * The refactoring history must be in connected state.
	 * </p>
	 * 
	 * @param source
	 *            the source refactoring descriptor
	 * @param target
	 *            the target refactoring descriptor
	 */
	public void setDependency(final RefactoringDescriptor source, final RefactoringDescriptor target) {
		Assert.isNotNull(source);
		Assert.isNotNull(target);
		Assert.isTrue(!target.isUnknown());

		// TODO: implement
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProjectHistory(final IProject project, final boolean enable) throws CoreException {
		Assert.isNotNull(project);
		Assert.isTrue(project.isAccessible());
		final String name= project.getName();
		final URI uri= project.getLocationURI();
		if (uri != null) {
			if (enable) {
				final IFileStore source= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER).getChild(name);
				if (source.fetchInfo().exists()) {
					IFileStore destination= EFS.getStore(uri).getChild(NAME_HISTORY_FOLDER);
					if (destination.fetchInfo().exists())
						destination.delete(EFS.NONE, null);
					destination.mkdir(EFS.NONE, null);
					source.copy(destination, EFS.OVERWRITE, null);
					source.delete(EFS.NONE, null);
				}
			} else {
				final IFileStore source= EFS.getStore(uri).getChild(NAME_HISTORY_FOLDER);
				if (source.fetchInfo().exists()) {
					IFileStore destination= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(NAME_HISTORY_FOLDER).getChild(name);
					if (destination.fetchInfo().exists())
						destination.delete(EFS.NONE, null);
					destination.mkdir(EFS.NONE, null);
					source.copy(destination, EFS.OVERWRITE, null);
					source.delete(EFS.NONE, null);
				}
			}
		}
	}

	/**
	 * Writes the specified refactoring descriptor proxies to the output stream.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param stream
	 *            the output stream
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void writeRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies, final OutputStream stream) throws CoreException {
		Assert.isNotNull(proxies);
		Assert.isNotNull(stream);
		try {
			connect();
			final List list= new ArrayList(proxies.length);
			for (int index= 0; index < proxies.length; index++) {
				final RefactoringDescriptor descriptor= proxies[index].requestDescriptor(new NullProgressMonitor());
				if (descriptor != null)
					list.add(descriptor);
			}
			final RefactoringDescriptor[] descriptors= new RefactoringDescriptor[list.size()];
			list.toArray(descriptors);
			RefactoringHistoryManager.writeRefactoringDescriptors(stream, descriptors);
		} finally {
			disconnect();
		}
	}
}