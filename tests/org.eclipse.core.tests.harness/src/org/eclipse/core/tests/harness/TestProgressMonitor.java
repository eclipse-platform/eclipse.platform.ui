/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.harness;

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
