package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.WorkspaceLock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.utils.*;
//
public class WorkManager implements IManager {
	protected int currentOperationId;
	protected ThreadLocal identifiers;
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
	identifiers = new ThreadLocal();
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
public void avoidAutoBuild() {
	getIdentifier().avoidAutoBuild = true;
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
	if (getPreparedOperationDepth() > 0)
		return;
	getWorkspaceLock().release();
}
private void decrementPreparedOperations() {
	getIdentifier().preparedOperations--;
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
private Identifier getIdentifier() {
	Identifier identifier = (Identifier) identifiers.get();
	if (identifier == null) {
		identifier = getNewIdentifier();
		identifiers.set(identifier);
	}
	return identifier;
}
public int getNestedOperationDepth() {
	return getIdentifier().nestedOperations;
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
	return getIdentifier().operationId;
}
public int getPreparedOperationDepth() {
	return getIdentifier().preparedOperations;
}
private WorkspaceLock getWorkspaceLock() throws CoreException {
	if (workspaceLock == null)
		workspaceLock = workspaceLock = new WorkspaceLock(workspace);
	return workspaceLock;
}
/**
 * Returns true if the nested operation depth is the same
 * as the prepared operation depth, and false otherwise.
 */
boolean isBalanced() {
	Identifier identifier = getIdentifier();
	return identifier.nestedOperations == identifier.preparedOperations;
}
void incrementNestedOperations() {
	getIdentifier().nestedOperations++;
}
private void incrementPreparedOperations() {
	getIdentifier().preparedOperations++;
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
public void operationCanceled() {
	getIdentifier().operationCanceled = true;
}
/**
 * Used to make things stable again after an operation has failed between
 * a workspace.prepareOperation() and workspace.beginOperation().
 */
public void rebalanceNestedOperations() {
	Identifier identifier = getIdentifier();
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
	identifiers.set(null);
}
public void setBuild(boolean build) {
	Identifier identifier = getIdentifier();
	if (identifier.preparedOperations == identifier.nestedOperations)
		identifier.shouldBuild = (identifier.shouldBuild || build);
}
public void setWorkspaceLock(WorkspaceLock lock) {
	//if (workspaceLock != null)
		//return;
	Assert.isNotNull(lock);
	workspaceLock = lock;
}
public boolean shouldBuild() {
	Identifier identifier = getIdentifier();
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
