/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IDynamicValidationStateChange;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IUndoManagerListener;
import org.eclipse.ltk.core.refactoring.IValidationStateListener;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.ValidationStateChangedEvent;

/**
 * Default implementation of IUndoManager.
 */
public class UndoManager implements IUndoManager {

	private class ValidationStateListener implements IValidationStateListener {
		public void stateChanged(ValidationStateChangedEvent event) {
			validationStateChanged(event.getChange());
		}
	}

	private Stack fUndoChanges;
	private Stack fRedoChanges;
	private Stack fUndoNames;
	private Stack fRedoNames;

	private ListenerList fListeners;

	private ValidationStateListener fValidationListener;

	/**
	 * Creates a new undo manager with an empty undo and redo stack.
	 */
	public UndoManager() {
		flush();
		fValidationListener= new ValidationStateListener();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void addListener(IUndoManagerListener listener) {
		if (fListeners == null)
			fListeners= new ListenerList();
		fListeners.add(listener);
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void removeListener(IUndoManagerListener listener) {
		if (fListeners == null)
			return;
		fListeners.remove(listener);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void aboutToPerformChange(Change change) {
		sendAboutToPerformChange(fUndoChanges, change);
		sendAboutToPerformChange(fRedoChanges, change);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void changePerformed(Change change, Change undo, Exception e) {
		sendChangePerformed(fUndoChanges, undo, change, e);
		sendChangePerformed(fRedoChanges, undo, change, e);
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void aboutToPerformRefactoring() {
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public void refactoringPerformed(boolean success) {
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
			removeValidationStateListener(fUndoChanges);
			sendDispose(fUndoChanges);
		}
		fUndoChanges= new Stack();
		fUndoNames= new Stack();
		fireUndoStackChanged();
	}

	private void flushRedo() {
		if (fRedoChanges != null) {
			removeValidationStateListener(fRedoChanges);
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
		flushRedo();
		addValidationStateListener(change);
		fireUndoStackChanged();
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public RefactoringStatus performUndo(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();

		if (fUndoChanges.empty())
			return result;

		Change change= (Change)fUndoChanges.pop();
		removeValidationStateListener(change);

		Change redo= executeChange(result, change, pm);

		if (!result.hasFatalError()) {
			if (redo != null && !fUndoNames.isEmpty()) {
				addValidationStateListener(redo);
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
		return result;
	}

	/*
	 * (Non-Javadoc) Method declared in IUndoManager.
	 */
	public RefactoringStatus performRedo(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();

		if (fRedoChanges.empty())
			return result;

		Change change= (Change)fRedoChanges.pop();
		removeValidationStateListener(change);

		Change undo= executeChange(result, change, pm);

		if (!result.hasFatalError()) {
			if (undo != null && !fRedoNames.isEmpty()) {
				addValidationStateListener(undo);
				fUndoNames.push(fRedoNames.pop());
				fUndoChanges.push(undo);
				fireRedoStackChanged();
				fireUndoStackChanged();
			}
		} else {
			flush();
		}

		return result;
	}

	private Change executeChange(final RefactoringStatus status, final Change change, IProgressMonitor pm) throws CoreException {
		final Change[] undo= new Change[1];
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				Exception exception= null;
				try {
					monitor.beginTask("", 11); //$NON-NLS-1$
					status.merge(change.isValid(new SubProgressMonitor(monitor, 2)));
					if (status.hasFatalError()) {
						return;
					}

					ResourcesPlugin.getWorkspace().checkpoint(false);
					aboutToPerformChange(change);
					undo[0]= change.perform(new SubProgressMonitor(monitor, 8));
					try {
						change.dispose();
					} finally {
						if (undo[0] != null) {
							undo[0].initializeValidationData(new SubProgressMonitor(monitor, 1));
						}
					}
				} catch (RuntimeException e) {
					exception= e;
					throw e;
				} catch (CoreException e) {
					exception= e;
					throw e;
				} finally {
					ResourcesPlugin.getWorkspace().checkpoint(false);
					changePerformed(change, undo[0], exception);
					monitor.done();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, pm);
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
	
	private void sendAboutToPerformChange(Collection collection, Change change) {
		for (Iterator iter= collection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IDynamicValidationStateChange) {
				((IDynamicValidationStateChange)element).aboutToPerformChange(change);
			}
			
		}
	}
	
	private void sendChangePerformed(Collection collection, Change change, Change undo, Exception e) {
		for (Iterator iter= collection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IDynamicValidationStateChange) {
				((IDynamicValidationStateChange)element).changePerformed(change, undo, e);
			}
			
		}
	}
	
	private void sendDispose(Collection collection) {
		for (Iterator iter= collection.iterator(); iter.hasNext();) {
			Change change= (Change)iter.next();
			change.dispose();
		}
	}
	
	private void removeValidationStateListener(Change change) {
		if (change instanceof IDynamicValidationStateChange)
			((IDynamicValidationStateChange)change).removeValidationStateListener(fValidationListener);
	}
	
	private void addValidationStateListener(Change change) {
		if (change instanceof IDynamicValidationStateChange)
			((IDynamicValidationStateChange)change).addValidationStateListener(fValidationListener);
	}
	
	private void removeValidationStateListener(Collection collection) {
		for (Iterator iter= collection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IDynamicValidationStateChange) {
				((IDynamicValidationStateChange)element).removeValidationStateListener(fValidationListener);
			}
			
		}
	}

	private void validationStateChanged(Change change) {
		try {
			if (!change.isValid(new NullProgressMonitor()).isOK())
				flush();
		} catch (CoreException e) {
			flush();
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
