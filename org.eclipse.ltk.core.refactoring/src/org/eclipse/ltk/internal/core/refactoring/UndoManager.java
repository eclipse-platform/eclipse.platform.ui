/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IUndoManagerListener;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Default implementation of IUndoManager.
 */
public class UndoManager implements IUndoManager {

	private Stack fUndoChanges;
	private Stack fRedoChanges;
	private Stack fUndoNames;
	private Stack fRedoNames;

	private ListenerList fListeners;
	// Maximum numbers of undos on the refactoring undo stack.
	private static final int MAX_UNDO_REDOS= 5;

	private static class NullQuery implements IValidationCheckResultQuery {
		public boolean proceed(RefactoringStatus status) {
			return true;
		}
		public void stopped(RefactoringStatus status) {
			// do nothing
		}
	}

	/**
	 * Creates a new undo manager with an empty undo and redo stack.
	 */
	public UndoManager() {
		flush();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void addListener(IUndoManagerListener listener) {
		if (fListeners == null)
			fListeners= new ListenerList(ListenerList.IDENTITY);
		fListeners.add(listener);
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void removeListener(IUndoManagerListener listener) {
		if (fListeners == null)
			return;
		fListeners.remove(listener);
		if (fListeners.size() == 0)
			fListeners= null;
	}

	public void aboutToPerformChange(final Change change) {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			final IUndoManagerListener listener= (IUndoManagerListener)listeners[i];
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.aboutToPerformChange(UndoManager.this, change);
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});
		}
	}

	/**
	 * @deprecated use #changePerformed(Change, boolean) instead
	 */
	public void changePerformed(final Change change) {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			final IUndoManagerListener listener= (IUndoManagerListener)listeners[i];
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.changePerformed(UndoManager.this, change);
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});
		}
	}

	public void changePerformed(Change change, boolean successful) {
		// the listeners don't care about success or not.
		changePerformed(change);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IUndoManager#shutdown()
	 */
	public void shutdown() {
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void flush() {
		flushUndo();
		flushRedo();
	}

	private void flushUndo() {
		if (fUndoChanges != null) {
			sendDispose(fUndoChanges);
		}
		fUndoChanges= new Stack();
		fUndoNames= new Stack();
		fireUndoStackChanged();
	}

	private void flushRedo() {
		if (fRedoChanges != null) {
			sendDispose(fRedoChanges);
		}
		fRedoChanges= new Stack();
		fRedoNames= new Stack();
		fireRedoStackChanged();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void addUndo(String refactoringName, Change change) {
		Assert.isNotNull(refactoringName, "refactoring"); //$NON-NLS-1$
		Assert.isNotNull(change, "change"); //$NON-NLS-1$
		fUndoNames.push(refactoringName);
		fUndoChanges.push(change);
		if (fUndoChanges.size() > MAX_UNDO_REDOS) {
			Change removedChange= (Change)fUndoChanges.remove(0);
			fUndoNames.remove(0);
			removedChange.dispose();
		}
		flushRedo();
		fireUndoStackChanged();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void performUndo(IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		RefactoringStatus result= new RefactoringStatus();

		if (fUndoChanges.empty())
			return;

		Change change= (Change)fUndoChanges.pop();
		if (query == null)
			query= new NullQuery();
		Change redo;
		try {
			redo= executeChange(result, change, query, pm);
		} catch (InterruptedException e) {
			fUndoChanges.push(change);
			return;
		}
		if (!result.hasFatalError()) {
			if (redo != null && !fUndoNames.isEmpty()) {
				fRedoNames.push(fUndoNames.pop());
				fRedoChanges.push(redo);
				fireUndoStackChanged();
				fireRedoStackChanged();
			} else {
				flush();
			}
		} else {
			flush();
		}
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void performRedo(IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		RefactoringStatus result= new RefactoringStatus();

		if (fRedoChanges.empty())
			return;

		Change change= (Change)fRedoChanges.pop();
		if (query == null)
			query= new NullQuery();
		Change undo;
		try {
			undo= executeChange(result, change, query, pm);
		} catch (InterruptedException e) {
			fRedoChanges.push(change);
			return;
		}
		if (!result.hasFatalError()) {
			if (undo != null && !fRedoNames.isEmpty()) {
				fUndoNames.push(fRedoNames.pop());
				fUndoChanges.push(undo);
				fireRedoStackChanged();
				fireUndoStackChanged();
			}
		} else {
			flush();
		}
	}

	private Change executeChange(final RefactoringStatus status, final Change change, final IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException, InterruptedException {
		final Change[] undo= new Change[1];
		final boolean[] interrupted= new boolean[1];
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				boolean undoInitialized= false;
				try {
					monitor.beginTask("", 11); //$NON-NLS-1$
					status.merge(change.isValid(new SubProgressMonitor(monitor, 2)));
					if (status.hasFatalError()) {
						query.stopped(status);
						change.dispose();
						return;
					}
					if (!status.isOK() && !query.proceed(status)) {
						interrupted[0]= true;
						return;
					}
					ResourcesPlugin.getWorkspace().checkpoint(false);
					boolean successful= false;
					try {
						aboutToPerformChange(change);
						undo[0]= change.perform(new SubProgressMonitor(monitor, 8));
						successful= true;
						ResourcesPlugin.getWorkspace().checkpoint(false);
					} finally {
						changePerformed(change, successful);
					}
					change.dispose();
					if (undo[0] != null) {
						undo[0].initializeValidationData(new SubProgressMonitor(monitor, 1));
						undoInitialized= true;
					}
				} catch (CoreException e) {
					flush();
					if (undo[0] != null && undoInitialized) {
						Change ch= undo[0];
						undo[0]= null;
						ch.dispose();
					} else {
						undo[0]= null;
					}
					throw e;
				} catch (RuntimeException e) {
					flush();
					if (undo[0] != null && undoInitialized) {
						Change ch= undo[0];
						undo[0]= null;
						ch.dispose();
					} else {
						undo[0]= null;
					}
					throw e;
				} finally {
					monitor.done();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, pm);
		if (interrupted[0])
			throw new InterruptedException();
		return undo[0];
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public boolean anythingToRedo() {
		return !fRedoChanges.empty();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public boolean anythingToUndo() {
		return !fUndoChanges.empty();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public String peekUndoName() {
		if (fUndoNames.size() > 0)
			return (String)fUndoNames.peek();
		return null;
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public String peekRedoName() {
		if (fRedoNames.size() > 0)
			return (String)fRedoNames.peek();
		return null;
	}

	private void fireUndoStackChanged() {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IUndoManagerListener)listeners[i]).undoStackChanged(this);
		}
	}

	private void fireRedoStackChanged() {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IUndoManagerListener)listeners[i]).redoStackChanged(this);
		}
	}

	private void sendDispose(Collection collection) {
		for (Iterator iter= collection.iterator(); iter.hasNext();) {
			final Change change= (Change)iter.next();
			ISafeRunnable r= new ISafeRunnable() {
				public void run() {
					change.dispose();
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			};
			SafeRunner.run(r);
		}
	}

	//---- testing methods ---------------------------------------------

	public boolean testHasNumberOfUndos(int number) {
		return fUndoChanges.size() == number;
	}

	public boolean testHasNumberOfRedos(int number) {
		return fRedoChanges.size() == number;
	}
}
