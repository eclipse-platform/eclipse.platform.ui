/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.Hashtable;

import org.eclipse.core.resources.WorkspaceLock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.utils.*;
//
public class WorkManager implements IManager {
	protected int currentOperationId;
	// we use a Hashtable for the identifiers to avoid concurrency problems
	protected Hashtable identifiers;
	protected int nextId;
	protected Workspace workspace;
	protected WorkspaceLock workspaceLock;
	protected Queue operations;
	protected Thread currentOperationThread;

	class Identifier {
		int operationId = OPERATION_EMPTY;
		int preparedOperations = 0;
		int nestedOperations = 0;
		// Indication for running auto build. It is computated based
		// on the parameters passed to Workspace.endOperation().
		boolean shouldBuild = false;
		// Enables or disables a condition based on the shouldBuild field.
		boolean avoidAutoBuild = false;
		boolean operationCanceled = false;
	}

	public static final int OPERATION_NONE = -1;
	public static final int OPERATION_EMPTY = 0;
public WorkManager(Workspace workspace) {
	currentOperationId = OPERATION_NONE;
	identifiers = new Hashtable(10);
	nextId = 0;
	this.workspace = workspace;
	operations = new Queue();
}
/**
 * Returns null if acquired and a Semaphore object otherwise.
 */
public synchronized Semaphore acquire() {
	if (isCurrentOperation())
		return null;
	if (currentOperationId == OPERATION_NONE && operations.isEmpty()) {
		updateCurrentOperation(getOperationId());
		return null;
	}
	return enqueue(new Semaphore(Thread.currentThread()));
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
public void avoidAutoBuild() {
	getIdentifier(currentOperationThread).avoidAutoBuild = true;
}
/**
 * An operation calls this method and it only returns when the operation
 * is free to run.
 */
public void checkIn() throws CoreException {
	try {
		boolean acquired = false;
		while (!acquired) {
			try {
				acquired = getWorkspaceLock().acquire();
			} catch (InterruptedException e) {
			}
		}
	} finally {
		incrementPreparedOperations();
	}
}
/**
 * Inform that an operation has finished.
 */
public synchronized void checkOut() throws CoreException {
	decrementPreparedOperations();
	rebalanceNestedOperations();
	// if this is a nested operation, just return
	// and do not release the lock
	if (getPreparedOperationDepth() > 0)
		return;
	getWorkspaceLock().release();
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
private void decrementPreparedOperations() {
	getIdentifier(currentOperationThread).preparedOperations--;
}
/**
 * If there is another semaphore with the same runnable in the
 * queue, the other is returned and the new one is not added.
 */
private synchronized Semaphore enqueue(Semaphore newSemaphore) {
	Semaphore semaphore = (Semaphore) operations.get(newSemaphore);
	if (semaphore == null) {
		operations.add(newSemaphore);
		return newSemaphore;
	}
	return semaphore;
}
public synchronized Thread getCurrentOperationThread() {
	return currentOperationThread;
}
private Identifier getIdentifier(Thread key) {
	Assert.isNotNull(key, "The thread should never be null.");//$NON-NLS-1$
	Identifier identifier = (Identifier) identifiers.get(key);
	if (identifier == null) {
		identifier = getNewIdentifier();
		identifiers.put(key, identifier);
	}
	return identifier;
}
private Identifier getNewIdentifier() {
	Identifier identifier = new Identifier();
	identifier.operationId = getNextOperationId();
	return identifier;
}
private int getNextOperationId() {
	return ++nextId;
}
private int getOperationId() {
	return getIdentifier(Thread.currentThread()).operationId;
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
public int getPreparedOperationDepth() {
	return getIdentifier(currentOperationThread).preparedOperations;
}
private WorkspaceLock getWorkspaceLock() throws CoreException {
	if (workspaceLock == null)
		workspaceLock = workspaceLock = new WorkspaceLock(workspace);
	return workspaceLock;
}
/**
 * Returns true if the nested operation depth is the same
 * as the prepared operation depth, and false otherwise.
 *
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
boolean isBalanced() {
	Identifier identifier = getIdentifier(currentOperationThread);
	return identifier.nestedOperations == identifier.preparedOperations;
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
void incrementNestedOperations() {
	getIdentifier(currentOperationThread).nestedOperations++;
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
private void incrementPreparedOperations() {
	getIdentifier(currentOperationThread).preparedOperations++;
}
/**
 * This method is synchronized with checkIn() and checkOut() that use blocks
 * like synchronized (this) { ... }.
 */
public synchronized boolean isCurrentOperation() {
	return currentOperationId == getOperationId();
}
public synchronized boolean isNextOperation(Runnable runnable) {
	Semaphore next = (Semaphore) operations.peek();
	return (next != null) && (next.getRunnable() == runnable);
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
public void operationCanceled() {
	getIdentifier(currentOperationThread).operationCanceled = true;
}
/**
 * Used to make things stable again after an operation has failed between
 * a workspace.prepareOperation() and workspace.beginOperation().
 * 
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
public void rebalanceNestedOperations() {
	Identifier identifier = getIdentifier(currentOperationThread);
	identifier.nestedOperations = identifier.preparedOperations;
}
public synchronized void release() {
	resetOperationId();
	Semaphore next = (Semaphore) operations.peek();
	updateCurrentOperation(OPERATION_NONE);
	if (next != null)
		next.release();
}
private void resetOperationId() {
	// ensure the operation cache on this thread is null
	identifiers.remove(currentOperationThread);
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
public void setBuild(boolean build) {
	Identifier identifier = getIdentifier(currentOperationThread);
	if (identifier.preparedOperations == identifier.nestedOperations)
		identifier.shouldBuild = (identifier.shouldBuild || build);
}
public void setWorkspaceLock(WorkspaceLock lock) {
	//if (workspaceLock != null)
		//return;
	Assert.isNotNull(lock);
	workspaceLock = lock;
}
/**
 * This method can only be safelly called from inside a workspace
 * operation. Should NOT be called from outside a 
 * prepareOperation/endOperation block.
 */
public boolean shouldBuild() {
	Identifier identifier = getIdentifier(currentOperationThread);
	if (!identifier.avoidAutoBuild && identifier.shouldBuild) {
		if (identifier.operationCanceled)
			return Policy.buildOnCancel;
		return true;
	}
	return false;
}
public void shutdown(IProgressMonitor monitor) {
	currentOperationId = OPERATION_NONE;
	identifiers = null;
	nextId = 0;
}
public void startup(IProgressMonitor monitor) {
}
public synchronized void updateCurrentOperation() {
	operations.remove();
	updateCurrentOperation(getOperationId());
}
private void updateCurrentOperation(int newID) {
	currentOperationId = newID;
	if (newID == OPERATION_NONE)
		currentOperationThread = null;
	else
		currentOperationThread = Thread.currentThread();
}

public boolean isTreeLocked() {
	return workspace.isTreeLocked();
}
}
