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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorHandle;
import org.eclipse.ltk.core.refactoring.RefactoringPreferenceConstants;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Default implementation of a refactoring history service.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryService implements IRefactoringHistoryService {

	/** Stack of refactoring descriptors which notified participants */
	private final class ParticipatingRefactoringDescriptorStack extends RefactoringDescriptorStack {

		void pop() throws EmptyStackException {
			final RefactoringDescriptor descriptor= peek();
			super.pop();
			for (int index= 0; index < fHistoryListeners.size(); index++) {
				final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
				Platform.run(new ISafeRunnable() {

					public final void handleException(final Throwable throwable) {
						RefactoringCorePlugin.log(throwable);
					}

					public void run() throws Exception {
						listener.descriptorRemoved(descriptor);
					}
				});
			}
		}

		void push(final RefactoringDescriptor descriptor) {
			super.push(descriptor);
			for (int index= 0; index < fHistoryListeners.size(); index++) {
				final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
				Platform.run(new ISafeRunnable() {

					public final void handleException(final Throwable throwable) {
						RefactoringCorePlugin.log(throwable);
					}

					public void run() throws Exception {
						listener.descriptorAdded(descriptor);
					}
				});
			}
		}
	}

	/** Stack of refactoring descriptors */
	private static class RefactoringDescriptorStack {

		/** The internal implementation */
		private final LinkedList fImplementation= new LinkedList();

		/** The refactoring history manager */
		private RefactoringHistoryManager fManager= new RefactoringHistoryManager(EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_REFACTORINGS_FOLDER).toURI());

		/**
		 * Frees the cache from the specified number of descriptors.
		 * 
		 * @param count
		 *            the positive count of descriptors to free
		 */
		void freeCache(final int count) {
			Assert.isTrue(count > 0);
			int number= 0;
			for (final ListIterator iterator= fImplementation.listIterator(fImplementation.size()); iterator.hasPrevious() && number < count; number++) {
				iterator.previous();
				iterator.remove();
			}
		}

		/**
		 * Does the descriptor stack need recaching?
		 * 
		 * @return <code>true</code> if the stack needs recaching,
		 *         <code>false</code> otherwise
		 */
		boolean needsRecaching() {
			try {
				return fImplementation.isEmpty() && !fManager.isEmpty();
			} catch (CoreException exception) {
				return true;
			}
		}

		/**
		 * Returns the current descriptor on the top of the stack.
		 * 
		 * @return the current descriptor on top
		 * @throws EmptyStackException
		 *             if the stack is empty
		 */
		RefactoringDescriptor peek() throws EmptyStackException {
			if (!fImplementation.isEmpty())
				return (RefactoringDescriptor) fImplementation.getFirst();
			else
				populateCache();
			if (fImplementation.isEmpty())
				throw new EmptyStackException();
			return (RefactoringDescriptor) fImplementation.getFirst();
		}

		/**
		 * Pops the top descriptor off the stack.
		 * 
		 * @throws EmptyStackException
		 *             if the stack is empty
		 */
		void pop() throws EmptyStackException {
			try {
				final boolean pop= RefactoringCorePlugin.getDefault().getPluginPreferences().getBoolean(RefactoringPreferenceConstants.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY);
				if (pop) {
					final RefactoringDescriptor descriptor= (RefactoringDescriptor) fImplementation.getFirst();
					if (!descriptor.isUnknown()) {
						final long stamp= descriptor.getTimeStamp();
						if (stamp >= 0)
							fManager.removeDescriptor(stamp);
					}
				}
			} catch (CoreException exception) {
				RefactoringCorePlugin.log(exception);
			}
			if (!fImplementation.isEmpty()) {
				fImplementation.removeFirst();
				return;
			} else
				populateCache();
			if (fImplementation.isEmpty())
				throw new EmptyStackException();
			fImplementation.removeFirst();
		}

		/**
		 * Populates the memory cache.
		 */
		void populateCache() {
			Assert.isTrue(fImplementation.isEmpty());
			final RefactoringDescriptor[] descriptors= fManager.readDescriptors(MAX_UNDO_STACK - 1);
			Arrays.sort(descriptors, new Comparator() {

				public final int compare(final Object first, final Object second) {
					return (int) (((RefactoringDescriptor) second).getTimeStamp() - ((RefactoringDescriptor) first).getTimeStamp());
				}
			});
			for (int index= 0; index < descriptors.length; index++)
				fImplementation.addFirst(descriptors[index]);
		}

		/**
		 * Pushes the given descriptor onto the stack.
		 * 
		 * @param descriptor
		 *            the descriptor to push onto the stack
		 */
		void push(final RefactoringDescriptor descriptor) {
			Assert.isNotNull(descriptor);
			try {
				final boolean push= RefactoringCorePlugin.getDefault().getPluginPreferences().getBoolean(RefactoringPreferenceConstants.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY);
				if (push && !descriptor.isUnknown())
					fManager.addDescriptor(descriptor);
			} catch (CoreException exception) {
				RefactoringCorePlugin.log(exception);
			}
			fImplementation.addFirst(descriptor);
			final int size= fImplementation.size();
			if (size > MAX_UNDO_STACK)
				freeCache(size - MAX_UNDO_STACK);
		}

		/**
		 * Returns the descriptor the specified handle points to.
		 * 
		 * @param handle
		 *            the refactoring descriptor handle
		 * @return the associated refactoring descriptor, or <code>null</code>
		 */
		RefactoringDescriptor resolveDescriptor(final RefactoringDescriptorHandle handle) {

			if (needsRecaching())
				populateCache();

			final long timestamp= handle.getTimeStamp();
			RefactoringDescriptor descriptor= null;
			for (final Iterator iterator= fImplementation.iterator(); iterator.hasNext();) {
				descriptor= (RefactoringDescriptor) iterator.next();
				if (descriptor.getTimeStamp() == timestamp)
					break;
			}

			if (descriptor == null)
				descriptor= fManager.resolveDescriptor(handle);

			return descriptor;
		}
	}

	/** Operation history listener for refactoring operation events */
	private final class RefactoringOperationListener implements IOperationHistoryListener {

		/** The last recently performed refactoring */
		private RefactoringDescriptor fDescriptor= null;

		public void historyNotification(final OperationHistoryEvent event) {
			IUndoableOperation operation= event.getOperation();
			if (operation instanceof TriggeredOperations)
				operation= ((TriggeredOperations) operation).getTriggeringOperation();
			UndoableOperation2ChangeAdapter adapter= null;
			if (operation instanceof UndoableOperation2ChangeAdapter) {
				adapter= (UndoableOperation2ChangeAdapter) operation;
			}
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
						fireRefactoringUndoneEvent((RefactoringDescriptor) fRedoCache.getFirst());
						break;
					case OperationHistoryEvent.ABOUT_TO_REDO:
						fireAboutToRedoEvent((RefactoringDescriptor) fRedoCache.getFirst());
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

		public void resourceChanged(final IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
				final IResource resource= event.getResource();
				if (resource != null && resource.getType() == IResource.PROJECT) {
					final IProject project= (IProject) resource;
					if (fUndoStack != null)
						RefactoringHistoryService.removeDescriptors(project, fUndoStack.fImplementation);
					if (fRedoCache != null)
						RefactoringHistoryService.removeDescriptors(project, fRedoCache);
				}
			}
		}
	}

	/** The singleton history */
	private static RefactoringHistoryService fInstance= null;

	/** The maximum size of the undo stack */
	private static final int MAX_UNDO_STACK= 25;

	/** The refactoring history file */
	public static final String NAME_REFACTORING_HISTORY= "refactorings.history"; //$NON-NLS-1$

	/** The refactoring history folder */
	public static final String NAME_REFACTORINGS_FOLDER= ".refactorings"; //$NON-NLS-1$

	/** The unknown refactoring id */
	private static final String UNKNOWN_REFACTORING_ID= "org.eclipse.ltk.core.refactoring.unknown.refactoring"; //$NON-NLS-1$

	/**
	 * Returns the singleton instance of the refactoring history.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @return the singleton instance
	 */
	public static RefactoringHistoryService getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringHistoryService();
		return fInstance;
	}

	/**
	 * Removes all descriptors for the specified project.
	 * 
	 * @param project
	 *            the project to remove its descriptors
	 * @param implementation
	 *            the list implementation of the stack
	 */
	private static void removeDescriptors(final IProject project, final List implementation) {
		Assert.isNotNull(project);
		Assert.isNotNull(implementation);
		final String name= project.getName();

		String current= null;
		RefactoringDescriptor descriptor= null;
		for (final Iterator iterator= implementation.listIterator(); iterator.hasNext();) {

			descriptor= (RefactoringDescriptor) iterator.next();
			current= descriptor.getProject();
			if (name.equals(current))
				iterator.remove();
		}
	}

	/** The execution listeners */
	private final List fExecutionListeners= new ArrayList(2);

	/** The history listeners */
	private final List fHistoryListeners= new ArrayList(2);

	/** The operation listener, or <code>null</code> */
	private IOperationHistoryListener fOperationListener= null;

	/** The redo refactoring descriptor cache, or <code>null</code> */
	private LinkedList fRedoCache= null;

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
		if (!fHistoryListeners.contains(listener)) {
			fHistoryListeners.add(listener);
			listener.connect();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void connect() {
		fReferenceCount++;
		if (fReferenceCount == 1) {
			fOperationListener= new RefactoringOperationListener();
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(fOperationListener);
			fResourceListener= new WorkspaceChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceListener, IResourceChangeEvent.PRE_DELETE);
			fUndoStack= new ParticipatingRefactoringDescriptorStack();
			fRedoCache= new LinkedList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void disconnect() {
		if (fReferenceCount > 0)
			fReferenceCount--;
		if (fReferenceCount == 0) {
			if (fOperationListener != null)
				OperationHistoryFactory.getOperationHistory().removeOperationHistoryListener(fOperationListener);
			if (fResourceListener != null)
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceListener);
			fUndoStack= null;
			fRedoCache= null;
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
					listener.aboutToPerformRefactoring(RefactoringHistoryService.this, descriptor);
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
					listener.aboutToRedoRefactoring(RefactoringHistoryService.this, descriptor);
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
					listener.aboutToUndoRefactoring(RefactoringHistoryService.this, descriptor);
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
					listener.refactoringPerformed(RefactoringHistoryService.this, descriptor);
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
					listener.refactoringRedone(RefactoringHistoryService.this, descriptor);
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
					listener.refactoringUndone(RefactoringHistoryService.this, descriptor);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getProjectHistory(final IProject project) {
		Assert.isNotNull(project);
		Assert.isTrue(project.exists());
		if (fUndoStack.needsRecaching())
			fUndoStack.populateCache();
		return new RefactoringHistoryManager(project.getLocationURI()).readHistory(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getProjectHistory(final IProject project, final long start, final long end) {
		Assert.isNotNull(project);
		Assert.isTrue(project.exists());
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= 0);
		if (fUndoStack.needsRecaching())
			fUndoStack.populateCache();
		return new RefactoringHistoryManager(project.getLocationURI()).readHistory(start, end);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getWorkspaceHistory() {
		if (fUndoStack.needsRecaching())
			fUndoStack.populateCache();
		return fUndoStack.fManager.readHistory(Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringHistory getWorkspaceHistory(final long start, final long end) {
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= 0);
		if (fUndoStack.needsRecaching())
			fUndoStack.populateCache();
		return fUndoStack.fManager.readHistory(start, end);
	}

	/**
	 * Handles the change redone event.
	 */
	void handleChangeRedone() {
		fUndoStack.push((RefactoringDescriptor) fRedoCache.removeFirst());
	}

	/**
	 * Handles the change undone event.
	 */
	void handleChangeUndone() {
		fRedoCache.addFirst(fUndoStack.peek());
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
	public RefactoringHistory readRefactoringHistory(final InputStream stream) throws CoreException {
		Assert.isNotNull(stream);
		final RefactoringSessionDescriptor descriptor= new XmlRefactoringSessionReader().readSession(new InputSource(stream));
		final RefactoringDescriptor[] descriptors= descriptor.getRefactorings();
		final RefactoringDescriptorHandle[] handles= new RefactoringDescriptorHandle[descriptors.length];
		for (int index= 0; index < descriptors.length; index++)
			handles[index]= new RefactoringDescriptorHandleAdapter(descriptors[index]);
		return new RefactoringHistoryImplementation(handles);
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
		if (fHistoryListeners.remove(listener))
			listener.disconnect();
	}

	/**
	 * Returns the descriptor the specified handle points to.
	 * <p>
	 * The refactoring history must be in connected state.
	 * </p>
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param handle
	 *            the refactoring descriptor handle
	 * @return the associated refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor resolveDescriptor(final RefactoringDescriptorHandle handle) {
		return fUndoStack.resolveDescriptor(handle);
	}

	/**
	 * Returns the descriptor the specified handle points to.
	 * <p>
	 * The refactoring history must be in connected state.
	 * </p>
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
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
	public void writeRefactoringHistory(final RefactoringDescriptorHandle[] handles, final OutputStream stream) throws CoreException {
		Assert.isNotNull(handles);
		Assert.isNotNull(stream);
		final IRefactoringSessionTransformer transformer= new XmlRefactoringSessionTransformer();
		try {
			transformer.beginSession(null);
			for (int index= 0; index < handles.length; index++) {
				final RefactoringDescriptor descriptor= handles[index].resolveDescriptor();
				if (descriptor != null) {
					try {
						transformer.beginRefactoring(descriptor.getID(), -1, descriptor.getProject(), descriptor.getDescription(), descriptor.getComment());
						for (final Iterator iterator= descriptor.getArguments().entrySet().iterator(); iterator.hasNext();) {
							final Map.Entry entry= (Entry) iterator.next();
							transformer.createArgument((String) entry.getKey(), (String) entry.getValue());
						}
					} finally {
						transformer.endRefactoring();
					}
				}
			}
		} finally {
			transformer.endSession();
		}
		final Object result= transformer.getResult();
		if (result instanceof Node) {
			try {
				final Transformer transform= TransformerFactory.newInstance().newTransformer();
				transform.setOutputProperty(OutputKeys.METHOD, IRefactoringSerializationConstants.OUTPUT_METHOD);
				transform.setOutputProperty(OutputKeys.ENCODING, IRefactoringSerializationConstants.OUTPUT_ENCODING);
				transform.transform(new DOMSource((Node) result), new StreamResult(stream));
			} catch (TransformerConfigurationException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, 0, exception.getLocalizedMessage(), exception));
			} catch (TransformerFactoryConfigurationError exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, 0, exception.getLocalizedMessage(), exception));
			} catch (TransformerException exception) {
				final Throwable throwable= exception.getException();
				if (throwable instanceof IOException)
					throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, 0, throwable.getLocalizedMessage(), throwable));
				RefactoringCorePlugin.log(exception);
			}
		}
	}
}