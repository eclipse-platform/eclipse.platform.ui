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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IUndoManagerListener;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class UndoManager2 implements IUndoManager {
	
	private class OperationHistroyListener implements IOperationHistoryListener {
		public void historyNotification(OperationHistoryEvent event) {
			IUndoableOperation op= event.getOperation();
			if (!(op instanceof UndoableOperation2ChangeAdapter)) 
				return;
			Change change= ((UndoableOperation2ChangeAdapter)op).getChange();
			switch(event.getEventType()) {
				case OperationHistoryEvent.ABOUT_TO_EXECUTE:
				case OperationHistoryEvent.ABOUT_TO_UNDO:
				case OperationHistoryEvent.ABOUT_TO_REDO:
					fireAboutToPerformChange(change);
					break;
				case OperationHistoryEvent.DONE:
				case OperationHistoryEvent.UNDONE:
				case OperationHistoryEvent.REDONE:
					fireChangePerformed(change);
					fireUndoStackChanged();
					fireRedoStackChanged();
					break;
				case OperationHistoryEvent.OPERATION_NOT_OK:					
					fireChangePerformed(change);
					break;
				case OperationHistoryEvent.OPERATION_ADDED:
					// would be better to have different events for this
					fireUndoStackChanged();
					fireRedoStackChanged();
					break;
				case OperationHistoryEvent.OPERATION_REMOVED:
					// would be better to have different events for this
					fireUndoStackChanged();
					fireRedoStackChanged();
					break;
			}
		}
	}
	
	private static class NullQuery implements IValidationCheckResultQuery {
		public boolean proceed(RefactoringStatus status) {
			return true;
		}
		public void stopped(RefactoringStatus status) {
			// do nothing
		}
	}
	
	private static class QueryAdapter implements IAdaptable {
		private IValidationCheckResultQuery fQuery;
		public QueryAdapter(IValidationCheckResultQuery query) {
			fQuery= query;
		}
		public Object getAdapter(Class adapter) {
			if (IValidationCheckResultQuery.class.equals(adapter))
				return fQuery;
			return null;
		}
	}
	
	private IOperationHistory fOperationHistroy;
	private IOperationHistoryListener fOperationHistoryListener;
	
	private boolean fIsOpen;
	private UndoableOperation2ChangeAdapter fActiveOperation;
	
	private ListenerList fListeners;

	public UndoManager2() {
		fOperationHistroy= OperationHistoryFactory.getOperationHistory();
	}
	
	public void addListener(IUndoManagerListener listener) {
		if (fListeners == null) {
			fListeners= new ListenerList();
			fOperationHistoryListener= new OperationHistroyListener();
			fOperationHistroy.addOperationHistoryListener(fOperationHistoryListener);
		}
		fListeners.add(listener);
	}

	public void removeListener(IUndoManagerListener listener) {
		if (fListeners == null)
			return;
		fListeners.remove(listener);
		if (fListeners.size() == 0) {
			fOperationHistroy.removeOperationHistoryListener(fOperationHistoryListener);
			fListeners= null;
			fOperationHistoryListener= null;
		}
	}

	public void aboutToPerformChange(Change change) {
		fActiveOperation= new UndoableOperation2ChangeAdapter(change);
		fActiveOperation.addContext(RefactoringCorePlugin.getUndoContext());
    	fOperationHistroy.openOperation(fActiveOperation);
    	fIsOpen= true;
	}

	public void changePerformed(Change change) {
		if (fIsOpen && fActiveOperation != null) {
			fOperationHistroy.closeOperation();
	        fIsOpen= false;
		}
	}

	public void addUndo(String name, Change change) {
		if (fActiveOperation != null) {
			fActiveOperation.setUndoChange(change);
			// No need to add the operation to the undo history. It already
			// got added via closing the operation.
			fActiveOperation= null;
			// But we have to fire an undo stack changed here
			fireUndoStackChanged();
		}
	}

	public boolean anythingToUndo() {
		return fOperationHistroy.canUndo(RefactoringCorePlugin.getUndoContext());
	}

	public String peekUndoName() {
		IUndoableOperation op= fOperationHistroy.getUndoOperation(RefactoringCorePlugin.getUndoContext());
		if (op == null)
			return null;
		return op.getLabel();
	}

	public void performUndo(IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException {
		IUndoableOperation op= fOperationHistroy.getUndoOperation(RefactoringCorePlugin.getUndoContext());
		if (!(op instanceof UndoableOperation2ChangeAdapter)) 
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(),
				IStatus.ERROR, "Top most undoable operation doesn't represent a refactoring change", null));
		if (query == null)
			query= new NullQuery();
		// TODO handle exception
		// fOperationHistroy.undoOperation(op, pm, new QueryAdapter(query));
	}

	public boolean anythingToRedo() {
		return fOperationHistroy.canRedo(RefactoringCorePlugin.getUndoContext());
	}

	public String peekRedoName() {
		IUndoableOperation op= fOperationHistroy.getRedoOperation(RefactoringCorePlugin.getUndoContext());
		if (op == null)
			return null;
		return op.getLabel();
	}

	public void performRedo(IValidationCheckResultQuery query, IProgressMonitor pm) throws CoreException {
		IUndoableOperation op= fOperationHistroy.getRedoOperation(RefactoringCorePlugin.getUndoContext());
		if (!(op instanceof UndoableOperation2ChangeAdapter)) 
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(),
				IStatus.ERROR, "Top most redoable operation doesn't represent a refactoring change", null));
		if (query == null)
			query= new NullQuery();
		// TODO handle exception
		// fOperationHistroy.redoOperation(op, pm, new QueryAdapter(query));
	}

	public void flush() {
		if (fIsOpen && fActiveOperation != null) {
			fOperationHistroy.closeOperation();
		}
		fActiveOperation= null;
		fIsOpen= false;
		fOperationHistroy.dispose(RefactoringCorePlugin.getUndoContext(), true, true);
	}

	public void shutdown() {
		// nothing to do since we have a shared undo manager anyways.
	}
	
	//---- event fireing methods -------------------------------------------------
	
	private void fireAboutToPerformChange(final Change change) {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			final IUndoManagerListener listener= (IUndoManagerListener)listeners[i];
			Platform.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.aboutToPerformChange(UndoManager2.this, change);
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});			
		}
	}
	
	private void fireChangePerformed(final Change change) {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			final IUndoManagerListener listener= (IUndoManagerListener)listeners[i];
			Platform.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.changePerformed(UndoManager2.this, change);
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});
		}
	}
	
	private void fireUndoStackChanged() {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			final IUndoManagerListener listener= (IUndoManagerListener)listeners[i];
			Platform.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.undoStackChanged(UndoManager2.this);
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});
		}
	}

	private void fireRedoStackChanged() {
		if (fListeners == null)
			return;
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			final IUndoManagerListener listener= (IUndoManagerListener)listeners[i];
			Platform.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.redoStackChanged(UndoManager2.this);
				}
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});
		}
	}
}
