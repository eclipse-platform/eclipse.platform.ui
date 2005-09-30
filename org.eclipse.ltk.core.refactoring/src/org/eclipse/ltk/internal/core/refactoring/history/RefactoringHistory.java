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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;

/**
 * Global workspace refactoring history.
 * <p>
 * Clients may obtain call only methods from interface
 * {@link org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory}.
 * </p>
 * 
 * @since 3.2
 */
public final class RefactoringHistory implements IRefactoringHistory {

	/** Stack of refactoring descriptors which notified participants */
	private final class ParticipatingRefactoringDescriptorStack extends RefactoringDescriptorStack {

		void pop() throws EmptyStackException {
			final RefactoringDescriptor descriptor= peek();
			super.pop();
			for (int index= 0; index < fHistoryParticipants.size(); index++) {
				final IRefactoringHistoryParticipant participant= (IRefactoringHistoryParticipant) fHistoryParticipants.get(index);
				Platform.run(new ISafeRunnable() {

					public final void handleException(final Throwable throwable) {
						RefactoringCorePlugin.log(throwable);
					}

					public void run() throws Exception {
						participant.pop(descriptor);
					}
				});
			}
		}

		void push(final RefactoringDescriptor descriptor) {
			super.push(descriptor);
			for (int index= 0; index < fHistoryParticipants.size(); index++) {
				final IRefactoringHistoryParticipant participant= (IRefactoringHistoryParticipant) fHistoryParticipants.get(index);
				Platform.run(new ISafeRunnable() {

					public final void handleException(final Throwable throwable) {
						RefactoringCorePlugin.log(throwable);
					}

					public void run() throws Exception {
						participant.push(descriptor);
					}
				});
			}
		}
	}

	/** Stack of refactoring descriptors */
	private static class RefactoringDescriptorStack {

		/** The cached capacity of the descriptor stack */
		private int fCapacity= MAX_UNDO_STACK;

		/** The internal implementation */
		private final LinkedList fImplementation= new LinkedList();

		/**
		 * Flushes the given number of eldest descriptors to the disk cache.
		 * 
		 * @param count
		 *            the positive count of descriptors to flush
		 */
		private void flushMemoryCache(final int count) {
			Assert.isTrue(count > 0);

			// TODO: implement
		}

		/**
		 * Returns whether the disk cache is empty.
		 * 
		 * @return <code>true</code> if the disk cache is empty,
		 *         <code>false</code> otherwise
		 */
		private boolean isDiskCacheEmpty() {
			// TODO: implement

			return true;
		}

		/**
		 * Is the refactoring descriptor stack empty?
		 * 
		 * @return <code>true</code> if the stack is empty, <code>false</code>
		 *         otherwise
		 */
		boolean isEmpty() {
			if (!fImplementation.isEmpty())
				return false;
			return isDiskCacheEmpty();
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
			readDiskCache();
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
			if (!fImplementation.isEmpty()) {
				fImplementation.removeFirst();
				return;
			}
			readDiskCache();
			if (fImplementation.isEmpty())
				throw new EmptyStackException();
			fImplementation.removeFirst();
		}

		/**
		 * Pushes the given descriptor onto the stack.
		 * 
		 * @param descriptor
		 *            the descriptor to push onto the stack
		 */
		void push(final RefactoringDescriptor descriptor) {
			Assert.isNotNull(descriptor);
			fImplementation.addFirst(descriptor);
			final int size= fImplementation.size();
			if (size > fCapacity)
				flushMemoryCache(size - fCapacity);
		}

		/**
		 * Reads a certain amount of descriptors from the disk cache.
		 */
		private void readDiskCache() {
			// TODO: implement
		}

		/**
		 * Removes all descriptors for the specified project.
		 * 
		 * @param project
		 *            the project to remove its descriptors
		 */
		void removeDescriptors(final IProject project) {
			Assert.isNotNull(project);
			final String name= project.getName();

			// TODO: implement

			String current= null;
			RefactoringDescriptor descriptor= null;
			for (final Iterator iterator= fImplementation.listIterator(); iterator.hasNext();) {
				descriptor= (RefactoringDescriptor) iterator.next();
				current= descriptor.getProject();
				if (name.equals(current))
					iterator.remove();
			}
		}

		/**
		 * Returns the descriptor the specified handle points to.
		 * 
		 * @param handle
		 *            the refactoring descriptor handle
		 * @return the associated refactoring descriptor, or <code>null</code>
		 */
		RefactoringDescriptor resolveDescriptor(final RefactoringDescriptorHandle handle) {

			// TODO: implement

			final long timestamp= handle.getTimeStamp();
			RefactoringDescriptor descriptor= null;
			for (final Iterator iterator= fImplementation.iterator(); iterator.hasNext();) {
				descriptor= (RefactoringDescriptor) iterator.next();
				if (descriptor.getTimeStamp() == timestamp)
					break;
			}
			return descriptor;
		}

		/**
		 * Sets the capacity of this stack.
		 * 
		 * @param capacity
		 *            the capacity to set
		 */
		void setCapacity(final int capacity) {
			Assert.isTrue(capacity >= 1);
			fCapacity= capacity;
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
							fireAboutToPerformEvent(fNullDescriptor);
						break;
					case OperationHistoryEvent.DONE:
						handleRefactoringPerformed(fDescriptor);
						if (fDescriptor != null)
							fireRefactoringPerformedEvent(fDescriptor);
						else
							fireRefactoringPerformedEvent(fNullDescriptor);
						fDescriptor= null;
						break;
					case OperationHistoryEvent.ABOUT_TO_UNDO:
						fireAboutToUndoEvent(fUndoStack.peek());
						break;
					case OperationHistoryEvent.UNDONE:
						handleChangeUndone();
						fireRefactoringUndoneEvent(fRedoStack.peek());
						break;
					case OperationHistoryEvent.ABOUT_TO_REDO:
						fireAboutToRedoEvent(fRedoStack.peek());
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
	private final class UnknownRefactoringDescriptor extends RefactoringDescriptor implements Serializable {

		/** The serial version UID */
		private static final long serialVersionUID= 1L;

		/**
		 * Creates a new unknown refactoring descriptor.
		 */
		private UnknownRefactoringDescriptor() {
			super(UNKNOWN_REFACTORING_ID, null, RefactoringCoreMessages.RefactoringHistory_unknown_refactoring_description, null, Collections.EMPTY_MAP);
		}

		private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
			// Do nothing
		}

		private void writeObject(final ObjectOutputStream stream) throws IOException {
			// Do nothing
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
						fUndoStack.removeDescriptors(project);
					if (fRedoStack != null)
						fRedoStack.removeDescriptors(project);
				}
			}
		}
	}

	/** Workspace save participant */
	private final class WorkspaceSaveParticipant implements ISaveParticipant {

		public void doneSaving(final ISaveContext context) {
			// Do nothing
		}

		/**
		 * Returns the name of the history file.
		 * 
		 * @param number
		 *            the save context number
		 * @return the name of the history file
		 */
		private String getHistoryName(final int number) {
			final StringBuffer buffer= new StringBuffer(32);
			buffer.append(NAME_HISTORY_FILE);
			buffer.append("_"); //$NON-NLS-1$
			buffer.append(number);
			buffer.append('.');
			buffer.append(EXTENSION_HISTORY_FILE);
			return buffer.toString();
		}

		public void prepareToSave(final ISaveContext context) throws CoreException {
			// Do nothing
		}

		public void rollback(final ISaveContext context) {
			// Do nothing
		}

		public void saving(final ISaveContext context) throws CoreException {
			if (RefactoringCorePlugin.getDefault().getPluginPreferences().getBoolean(RefactoringHistory.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY)) {
				final IPath path= RefactoringCorePlugin.getDefault().getStateLocation();
				final int number= context.getSaveNumber();
				String name= getHistoryName(number);
				File file= path.append(name).toFile();
				boolean successful= false;
				if (file != null && fUndoStack != null && fRedoStack != null) {
					try {
						writeWorkspaceHistory(file);
						successful= true;
					} catch (IOException exception) {
						RefactoringCorePlugin.log(exception);
					} finally {
						context.map(new Path(NAME_HISTORY_FILE), new Path(name));
						context.needSaveNumber();
					}
					if (successful) {
						name= getHistoryName(number - 1);
						file= path.append(name).toFile();
						if (file != null)
							file.delete();
					}
				}
			}
		}
	}

	/** The history file extension */
	private static final String EXTENSION_HISTORY_FILE= "dat"; //$NON-NLS-1$

	/** The singleton history */
	private static RefactoringHistory fInstance= null;

	/** The maximum size of the redo stack */
	private static final int MAX_REDO_STACK= 5;

	/** The maximum size of the undo stack */
	private static final int MAX_UNDO_STACK= 25;

	/** The history file name */
	public static final String NAME_HISTORY_FILE= "refactoring_history"; //$NON-NLS-1$

	/** The project refactoring history preference */
	public static final String PREFERENCE_ENABLE_PROJECT_REFACTORING_HISTORY= "org.eclipse.ltk.core.refactoring.enable.project.refactoring.history"; //$NON-NLS-1$

	/** The workspace refactoring history preference */
	public static final String PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY= "org.eclipse.ltk.core.refactoring.enable.workspace.refactoring.history"; //$NON-NLS-1$

	/** The unknown refactoring id */
	private static final String UNKNOWN_REFACTORING_ID= RefactoringCorePlugin.getDefault().getBundle().getSymbolicName() + ".unknown.refactoring"; //$NON-NLS-1$

	/**
	 * Returns the singleton instance of the refactoring history.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @return the singleton instance
	 */
	public static RefactoringHistory getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringHistory();
		return fInstance;
	}

	/** The history listeners */
	private final List fHistoryListeners= new ArrayList(2);

	/** The history participants */
	private final List fHistoryParticipants= new ArrayList(2);

	/** The null refactoring descriptor */
	private final RefactoringDescriptor fNullDescriptor= new UnknownRefactoringDescriptor();

	/** The operation listener, or <code>null</code> */
	private IOperationHistoryListener fOperationListener= null;

	/** The redo refactoring descriptor stack, or <code>null</code> */
	private RefactoringDescriptorStack fRedoStack= null;

	/** The history reference count */
	private int fReferenceCount= 0;

	/** The resource listener, or <code>null</code> */
	private IResourceChangeListener fResourceListener= null;

	/** The undo refactoring descriptor stack, or <code>null</code> */
	private RefactoringDescriptorStack fUndoStack= null;

	/**
	 * Creates a new refactoring history.
	 */
	private RefactoringHistory() {
		// Not for instantiation
	}

	/**
	 * @inheritDoc
	 */
	public void addHistoryListener(final IRefactoringHistoryListener listener) {
		Assert.isNotNull(listener);
		if (!fHistoryListeners.contains(listener))
			fHistoryListeners.add(listener);
	}

	/**
	 * @inheritDoc
	 */
	public void addHistoryParticipant(final IRefactoringHistoryParticipant participant) {
		Assert.isNotNull(participant);
		if (!fHistoryParticipants.contains(participant)) {
			fHistoryParticipants.add(participant);
			participant.connect();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void connect() {
		fReferenceCount++;
		if (fReferenceCount == 1) {
			fOperationListener= new RefactoringOperationListener();
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(fOperationListener);
			fResourceListener= new WorkspaceChangeListener();
			final IWorkspace workspace= ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(fResourceListener, IResourceChangeEvent.PRE_DELETE);
			fUndoStack= new ParticipatingRefactoringDescriptorStack();
			fUndoStack.setCapacity(MAX_UNDO_STACK);
			fRedoStack= new RefactoringDescriptorStack();
			fRedoStack.setCapacity(MAX_REDO_STACK);
			try {
				final ISavedState state= workspace.addSaveParticipant(RefactoringCorePlugin.getDefault(), new WorkspaceSaveParticipant());
				if (state != null) {
					final IPath path= state.lookup(new Path(NAME_HISTORY_FILE));
					if (path != null) {
						final File file= RefactoringCorePlugin.getDefault().getStateLocation().append(path).toFile();
						if (file != null && file.canRead())
							readWorkspaceHistory(file);
					}
				}
			} catch (IllegalStateException exception) {
				RefactoringCorePlugin.log(exception);
			} catch (CoreException exception) {
				RefactoringCorePlugin.log(exception);
			} catch (IOException exception) {
				RefactoringCorePlugin.log(exception);
			}
		}
	}

	/**
	 * @inheritDoc
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
			fRedoStack= null;
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
		for (int index= 0; index < fHistoryListeners.size(); index++) {
			final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public final void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.aboutToPerformRefactoring(RefactoringHistory.this, descriptor);
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
		for (int index= 0; index < fHistoryListeners.size(); index++) {
			final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.aboutToRedoRefactoring(RefactoringHistory.this, descriptor);
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
		for (int index= 0; index < fHistoryListeners.size(); index++) {
			final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.aboutToUndoRefactoring(RefactoringHistory.this, descriptor);
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
		for (int index= 0; index < fHistoryListeners.size(); index++) {
			final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.refactoringPerformed(RefactoringHistory.this, descriptor);
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
		for (int index= 0; index < fHistoryListeners.size(); index++) {
			final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.refactoringRedone(RefactoringHistory.this, descriptor);
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
		for (int index= 0; index < fHistoryListeners.size(); index++) {
			final IRefactoringHistoryListener listener= (IRefactoringHistoryListener) fHistoryListeners.get(index);
			Platform.run(new ISafeRunnable() {

				public void handleException(final Throwable throwable) {
					RefactoringCorePlugin.log(throwable);
				}

				public void run() throws Exception {
					listener.refactoringUndone(RefactoringHistory.this, descriptor);
				}
			});
		}
	}

	/**
	 * @inheritDoc
	 */
	public RefactoringDescriptorHandle[] getProjectHistory(final IProject project) {
		Assert.isNotNull(project);
		Assert.isTrue(project.exists());

		// TODO: implement

		final String name= project.getName();
		final List handles= new ArrayList(fUndoStack.fImplementation.size());
		for (final Iterator iterator= fUndoStack.fImplementation.iterator(); iterator.hasNext();) {
			final RefactoringDescriptor descriptor= (RefactoringDescriptor) iterator.next();
			if (descriptor != fNullDescriptor && name.equals(descriptor.getProject()))
				handles.add(new RefactoringDescriptorHandle(descriptor.getDescription(), descriptor.getTimeStamp()));
		}
		return (RefactoringDescriptorHandle[]) handles.toArray(new RefactoringDescriptorHandle[handles.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public RefactoringDescriptorHandle[] getProjectHistory(final IProject project, final long start, final long end) {
		Assert.isNotNull(project);
		Assert.isTrue(project.exists());
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= 0);

		// TODO: implement

		final String name= project.getName();
		final List handles= new ArrayList(fUndoStack.fImplementation.size());
		for (final Iterator iterator= fUndoStack.fImplementation.iterator(); iterator.hasNext();) {
			final RefactoringDescriptor descriptor= (RefactoringDescriptor) iterator.next();
			final long stamp= descriptor.getTimeStamp();
			if (descriptor != fNullDescriptor && stamp >= start && stamp <= end && name.equals(descriptor.getProject()))
				handles.add(new RefactoringDescriptorHandle(descriptor.getDescription(), stamp));
		}
		return (RefactoringDescriptorHandle[]) handles.toArray(new RefactoringDescriptorHandle[handles.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public RefactoringDescriptorHandle[] getWorkspaceHistory() {

		// TODO: implement

		final List handles= new ArrayList(fUndoStack.fImplementation.size());
		for (final Iterator iterator= fUndoStack.fImplementation.iterator(); iterator.hasNext();) {
			final RefactoringDescriptor descriptor= (RefactoringDescriptor) iterator.next();
			if (descriptor != fNullDescriptor)
				handles.add(new RefactoringDescriptorHandle(descriptor.getDescription(), descriptor.getTimeStamp()));
		}
		return (RefactoringDescriptorHandle[]) handles.toArray(new RefactoringDescriptorHandle[handles.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public RefactoringDescriptorHandle[] getWorkspaceHistory(final long start, final long end) {
		Assert.isTrue(start >= 0);
		Assert.isTrue(end >= 0);

		// TODO: implement

		final List handles= new ArrayList(fUndoStack.fImplementation.size());
		for (final Iterator iterator= fUndoStack.fImplementation.iterator(); iterator.hasNext();) {
			final RefactoringDescriptor descriptor= (RefactoringDescriptor) iterator.next();
			final long stamp= descriptor.getTimeStamp();
			if (descriptor != fNullDescriptor && stamp >= start && stamp <= end)
				handles.add(new RefactoringDescriptorHandle(descriptor.getDescription(), stamp));
		}
		return (RefactoringDescriptorHandle[]) handles.toArray(new RefactoringDescriptorHandle[handles.size()]);
	}

	/**
	 * Handles the change redone event.
	 */
	void handleChangeRedone() {
		final RefactoringDescriptor descriptor= fRedoStack.peek();
		fRedoStack.pop();
		fUndoStack.push(descriptor);
	}

	/**
	 * Handles the change undone event.
	 */
	void handleChangeUndone() {
		final RefactoringDescriptor descriptor= fUndoStack.peek();
		fUndoStack.pop();
		fRedoStack.push(descriptor);
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
			fUndoStack.push(descriptor);
			descriptor.setTimeStamp(System.currentTimeMillis());
		} else
			fUndoStack.push(fNullDescriptor);
	}

	/**
	 * Reads the workspace history from disk.
	 * 
	 * @param file
	 *            the history file
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private void readWorkspaceHistory(final File file) throws IOException {
		Assert.isTrue(file.canRead());
		Assert.isNotNull(fUndoStack);
		Assert.isNotNull(fRedoStack);

		// TODO: implement

		final ObjectInputStream stream= new ObjectInputStream(new FileInputStream(file));
		try {
			List list= (List) stream.readObject();
			for (final Iterator iterator= list.iterator(); iterator.hasNext();)
				fUndoStack.fImplementation.add(iterator.next());
			list= (List) stream.readObject();
			for (final Iterator iterator= list.iterator(); iterator.hasNext();)
				fRedoStack.fImplementation.add(iterator.next());
		} catch (ClassNotFoundException exception) {
			RefactoringCorePlugin.log(exception);
		} finally {
			stream.close();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void removeHistoryListener(final IRefactoringHistoryListener listener) {
		Assert.isNotNull(listener);
		fHistoryListeners.remove(listener);
	}

	/**
	 * @inheritDoc
	 */
	public void removeHistoryParticipant(final IRefactoringHistoryParticipant participant) {
		Assert.isNotNull(participant);
		if (fHistoryParticipants.remove(participant))
			participant.disconnect();
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
		Assert.isNotNull(handle);

		// TODO: implement

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
	 * Writes the workspace history to disk.
	 * 
	 * @param file
	 *            the history file
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	private void writeWorkspaceHistory(final File file) throws IOException {
		Assert.isNotNull(file);
		Assert.isNotNull(fUndoStack);
		Assert.isNotNull(fRedoStack);

		// TODO: implement

		final ObjectOutputStream stream= new ObjectOutputStream(new FileOutputStream(file));
		try {
			stream.writeObject(new ArrayList(fUndoStack.fImplementation));
			stream.writeObject(new ArrayList(fRedoStack.fImplementation));
		} finally {
			try {
				stream.close();
			} catch (IOException exception) {
				RefactoringCorePlugin.log(exception);
			}
		}
	}
}
