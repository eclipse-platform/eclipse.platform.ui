package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.Shell;

/**
 */
public interface IWorkingSetDialog {
	public static final int OK = 0;
	public static final int CANCEL = 1;	

	public IWorkingSet getWorkingSet();
	public void init(Shell shell);
	public void init(Shell shell, IWorkingSet initialSelection);
	public int open();
}
