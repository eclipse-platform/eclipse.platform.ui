/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Represents a composite change. Composite changes can be marked
 * as synthetic. A synthetic composite changes might not be rendered
 * in the refactoring preview tree to save display real-estate.
 * <p>
 * Clients may subclass this class.
 * </p>
 *
 * @see Change
 *
 * @since 3.0
 */
public class CompositeChange extends Change {

	private String fName;
	private List<Change> fChanges;
	private boolean fIsSynthetic;
	private Change fUndoUntilException;

	/**
	 * Creates a new composite change with the given name.
	 *
	 * @param name the human readable name of the change. Will
	 *  be used to display the change in the user interface
	 */
	public CompositeChange(String name) {
		this(name, new ArrayList<Change>(2));
	}

	/**
	 * Creates a new composite change with the given name and array
	 * of children.
	 *
	 * @param name the human readable name of the change. Will
	 *  be used to display the change in the user interface
	 * @param children the initial array of children
	 */
	public CompositeChange(String name, Change[] children) {
		this(name, new ArrayList<Change>(children.length));
		addAll(children);
	}

	private CompositeChange(String name, List<Change> changes) {
		Assert.isNotNull(name);
		Assert.isNotNull(changes);
		fName= name;
		fChanges= changes;
	}

	/**
	 * Returns whether this change is synthetic or not.
	 *
	 * @return <code>true</code>if this change is synthetic; otherwise
	 *  <code>false</code>
	 */
	public boolean isSynthetic() {
		return fIsSynthetic;
	}

	/**
	 * Marks this change as synthetic.
	 */
	public void markAsSynthetic() {
		fIsSynthetic= true;
	}

	@Override
	public String getName() {
		return fName;
	}

	/**
	 * Adds the given change to the list of children. The change to be added
	 * can be <code>null</code>. Adding a "null" change does nothing.
	 *
	 * @param change the change to add
	 */
	public void add(Change change) {
		if (change != null) {
			Assert.isTrue(change.getParent() == null);
			fChanges.add(change);
			change.setParent(this);
		}
	}

	/**
	 * Adds all changes in the given array to the list of children.
	 *
	 * @param changes the changes to add
	 */
	public void addAll(Change[] changes) {
		for (Change change : changes) {
			add(change);
		}
	}

	/**
	 * Merges the children of the given composite change into this
	 * change. This means the changes are removed from the given
	 * composite change and added to this change.
	 *
	 * @param change the change to merge
	 */
	public void merge(CompositeChange change) {
		Change[] others= change.getChildren();
		for (Change other : others) {
			change.remove(other);
			add(other);
		}
	}

	/**
	 * Removes the given change from the list of children.
	 *
	 * @param change the change to remove
	 *
	 * @return <code>true</code> if the change contained the given
	 *  child; otherwise <code>false</code> is returned
	 */
	public boolean remove(Change change) {
		Assert.isNotNull(change);
		boolean result= fChanges.remove(change);
		if (result) {
			change.setParent(null);
		}
		return result;

	}

	/**
	 * Removes all changes from this composite change.
	 *
	 * @return the list of changes removed from this composite
	 * change
	 *
	 * @since 3.1
	 */
	public Change[] clear() {
		Change[] result= fChanges.toArray(new Change[fChanges.size()]);
		fChanges.clear();
		return result;
	}

	/**
	 * Returns the children managed by this composite change.
	 *
	 * @return the children of this change or an empty array if no
	 *  children exist
	 */
	public Change[] getChildren() {
		return fChanges.toArray(new Change[fChanges.size()]);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The composite change sends <code>setEnabled</code> to all its children.
	 * </p>
	 * <p>
	 * Client are allowed to extend this method.
	 * </p>
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Change change : fChanges) {
			change.setEnabled(enabled);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The composite change sends <code>initializeValidationData</code> to all its
	 * children.
	 * </p>
	 * <p>
	 * Client are allowed to extend this method.
	 * </p>
	 */
	@Override
	public void initializeValidationData(IProgressMonitor pm) {
		pm.beginTask("", fChanges.size()); //$NON-NLS-1$
		for (Change change : fChanges) {
			change.initializeValidationData(new SubProgressMonitor(pm, 1));
			pm.worked(1);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The composite change sends <code>isValid</code> to all its children
	 * until the first one returns a status with a severity of <code>FATAL
	 * </code>. If one of the children throws an exception the remaining children
	 * will not receive the <code>isValid</code> call.
	 * </p>
	 * <p>
	 * Client are allowed to extend this method.
	 * </p>
	 */
	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		pm.beginTask("", fChanges.size()); //$NON-NLS-1$
		for (Iterator<Change> iter= fChanges.iterator(); iter.hasNext() && !result.hasFatalError();) {
			Change change= iter.next();
			if (change.isEnabled())
				result.merge(change.isValid(new SubProgressMonitor(pm, 1)));
			else
				pm.worked(1);
			if (pm.isCanceled())
				throw new OperationCanceledException();
		}
		pm.done();
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The composite change sends <code>perform</code> to all its <em>enabled</em>
	 * children. If one of the children throws an exception the remaining children
	 * will not receive the <code>perform</code> call. In this case the method <code>
	 * getUndoUntilException</code> can be used to get an undo object containing the
	 * undo objects of all executed children.
	 * </p>
	 * <p>
	 * Client are allowed to extend this method.
	 * </p>
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		fUndoUntilException= null;
		List<Change> undos= new ArrayList<>(fChanges.size());
		pm.beginTask("", fChanges.size()); //$NON-NLS-1$
		pm.setTaskName(RefactoringCoreMessages.CompositeChange_performingChangesTask_name);
		Change change= null;
		boolean canceled= false;
		try {
			for (Iterator<Change> iter= fChanges.iterator(); iter.hasNext();) {
				change= iter.next();
				if (canceled && !internalProcessOnCancel(change))
					continue;

				if (change.isEnabled()) {
					Change undoChange= null;
					try {
						undoChange= change.perform(new SubProgressMonitor(pm, 1));
					} catch(OperationCanceledException e) {
						canceled= true;
						if (!internalContinueOnCancel())
							throw e;
						undos= null;
					}
					if (undos != null) {
						if (undoChange == null) {
							undos= null;
						} else {
							undos.add(undoChange);
						}
					}
				}
				// remove the change from the list of children to give
				// the garbage collector the change to collect the change. This
				// ensures that the memory consumption doesn't go up when
				// producing the undo change tree.
				iter.remove();
				// Make sure we dispose the change since it will now longer be
				// in the list of children when call CompositeChange#dispose()
				final Change changeToDispose= change;
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						changeToDispose.dispose();
					}
					@Override
					public void handleException(Throwable exception) {
						RefactoringCorePlugin.log(exception);
					}
				});
			}
			if (canceled)
				throw new OperationCanceledException();
			if (undos != null) {
				Collections.reverse(undos);
				return createUndoChange(undos.toArray(new Change[undos.size()]));
			} else {
				return null;
			}
		} catch (CoreException | RuntimeException e) {
			handleUndos(change, undos);
			internalHandleException(change, e);
			throw e;
		}
	}

	private void handleUndos(Change failedChange, List<Change> undos) {
		if (undos == null) {
			fUndoUntilException= null;
			return;
		}
		if (failedChange instanceof CompositeChange) {
			Change partUndoChange= ((CompositeChange)failedChange).getUndoUntilException();
			if (partUndoChange != null) {
				undos.add(partUndoChange);
			}
		}
		if (undos.isEmpty()) {
			fUndoUntilException= null;
			return;
		}
		Collections.reverse(undos);
		fUndoUntilException= createUndoChange(undos.toArray(new Change[undos.size()]));
	}

	/**
	 * Note: this is an internal method and should not be overridden outside of
	 * the refactoring framework.
	 * <p>
	 * The method gets called if one of the changes managed by this
	 * composite change generates an exception when performed.
	 * </p>
	 *
	 * @param change the change that caused the exception
	 * @param t the exception itself
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void internalHandleException(Change change, Throwable t) {
		// do nothing
	}

	/**
	 * Note: this is an internal method and should not be overridden outside of
	 * the refactoring framework.
	 * <p>
	 * The method gets called if one of the changes managed by this
	 * composite change generates an operation canceled exception when
	 * performed.
	 * </p>
	 *
	 * @return <code>true</code> if performing the change should
	 *  continue on cancel; otherwise <code>false</code>
     *
     * @since 3.1
     *
     * @noreference This method is not intended to be referenced by clients.
	 */
	protected boolean internalContinueOnCancel() {
		return false;
	}

	/**
	 * Note: this is an internal method and should not be overridden outside of
	 * the refactoring framework.
	 * <p>
	 * The method gets called if the execution of this change got canceled,
	 * but <code>internalContinueOnCancel</code> returned true.
	 * </p>
	 *
	 * @param change the change to perform
	 *
	 * @return <code>true</code> if the given change should be performed although
	 *  the execution got canceled; otherwise <code>false</code>
	 *
	 * @since 3.1
	 *
     * @noreference This method is not intended to be referenced by clients.
	 */
	protected boolean internalProcessOnCancel(Change change) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The composite change sends <code>dispose</code> to all its children. It is guaranteed
	 * that all children receive the <code>dispose</code> call.
	 * </p>
	 */
	@Override
	public void dispose() {
		for (Change change : fChanges) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					change.dispose();
				}
				@Override
				public void handleException(Throwable exception) {
					RefactoringCorePlugin.log(exception);
				}
			});
		}
	}

	/**
	 * Returns the undo object containing all undo changes of those children that got successfully
	 * executed while performing this change. Returns <code>null</code> if all changes were executed
	 * successfully or if there's nothing to undo.
	 * <p>
	 * This method is not intended to be overridden or extended.
	 * </p>
	 *
	 * @return the undo object containing all undo changes of those children that got successfully
	 *         executed while performing this change, or <code>null</code> if all changes were
	 *         executed successfully or if there's nothing to undo.
	 */
	public Change getUndoUntilException() {
		return fUndoUntilException;
	}

	/**
	 * Hook to create an undo change. The method should be overridden
	 * by clients which provide their own composite change to create
	 * a corresponding undo change.
	 *
	 * @param childUndos the child undo. The undo edits appear in the
	 *  list in the reverse order of their execution. So the first
	 *  change in the array is the undo change of the last change
	 *  that got executed.
	 *
	 * @return the undo change
	 */
	protected Change createUndoChange(Change[] childUndos) {
		return new CompositeChange(getName(), childUndos);
	}

	@Override
	public Object[] getAffectedObjects() {
		if (fChanges.isEmpty())
			return new Object[0];
		List<Object> result= new ArrayList<>();
		for (Change change : fChanges) {
			Object[] affectedObjects= change.getAffectedObjects();
			if (affectedObjects == null)
				return null;
			result.addAll(Arrays.asList(affectedObjects));
		}
		return result.toArray();
	}

	@Override
	public Object getModifiedElement() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
	@Override
	public ChangeDescriptor getDescriptor() {
		for (Change change : fChanges) {
			final ChangeDescriptor descriptor= change.getDescriptor();
			if (descriptor != null)
				return descriptor;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder buff= new StringBuilder();
		buff.append(getName());
		buff.append("\n"); //$NON-NLS-1$
		for (Change change : fChanges) {
			buff.append("<").append(change.toString()).append("/>\n"); //$NON-NLS-2$ //$NON-NLS-1$
		}
		return buff.toString();
	}

	/**
	 * @return Amount of changed files
	 * @since 3.13
	 */
	public int getFilenumber() {
		if(fChanges.size()>0) {
			if (fChanges.get(0) instanceof CompositeChange) {
				CompositeChange obj= (CompositeChange) fChanges.get(0);
				return obj.getAmount();
			}
			return 1;
		}
		return 0;
	}

	private int getAmount() {
		return fChanges.size();
	}

}
