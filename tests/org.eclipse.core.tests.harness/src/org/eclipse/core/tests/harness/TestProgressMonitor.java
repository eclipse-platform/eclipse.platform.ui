package org.eclipse.core.tests.harness;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class TestProgressMonitor implements IProgressMonitor {
/**
 * @see IProgressMonitor#beginTask
 */
public void beginTask(String name, int totalWork) {
}
/**
 * @see IProgressMonitor#done
 */
public void done() {
}
public void internalWorked(double work) {
}
/**
 * @see IProgressMonitor#isCanceled
 */
public boolean isCanceled() {
	return false;
}
/**
 * @see IProgressMonitor#setCanceled
 */
public void setCanceled(boolean b) {
}
/**
 * @see IProgressMonitor#setTaskName
 */
public void setTaskName(String name) {
}
/**
 * @see IProgressMonitor#subTask
 */
public void subTask(String name) {
}
/**
 * @see IProgressMonitor#worked
 */
public void worked(int work) {
}
}
