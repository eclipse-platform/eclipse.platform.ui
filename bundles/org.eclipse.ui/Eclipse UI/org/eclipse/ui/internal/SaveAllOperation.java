package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;

/**
 * This is a strategy for Save All.  It can be invoked on a
 * page or a window, and maybe even an entire workbench.
 *
 * See:
 *	1GD0B8M: ITPUI:ALL - Incorrect behavior with close all perspective & dirty editor
 *
 * This class is work in progress.  Please do not delete.
 */
public class SaveAllOperation {
/**
 * Construct a new operation on the page.
 */
public SaveAllOperation() {
}
/**
 * Run save all on a workbench.
 *
 * Returns <code>true</code> if the save was completed, or
 * <code>cancel</code> if the user cancelled the operation.
 */
public boolean run(Shell shell, IWorkbench wb) {
	return false;
}
/**
 * Run save all on a page.
 *
 * Returns <code>true</code> if the save was completed, or
 * <code>cancel</code> if the user cancelled the operation.
 */
public boolean run(Shell shell, IWorkbenchPage page) {
	return false;
}
/**
 * Run save all on a window.
 *
 * Returns <code>true</code> if the save was completed, or
 * <code>cancel</code> if the user cancelled the operation.
 */
public boolean run(Shell shell, IWorkbenchWindow window) {
	return false;
}
}
