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
package org.eclipse.ui.internal.misc;

import org.eclipse.ui.*;

/**
 * Prints out page listener events.
 */
public class TestPageListener implements IPageListener {
/**
 * TestPageListener constructor comment.
 */
public TestPageListener() {
	super();
}
/**
 * Notifies this listener that the given page has been activated.
 *
 * @param page the page that was activated
 * @see IWorkbenchWindow#setActivePage
 */
public void pageActivated(IWorkbenchPage page) {
	System.out.println("pageActivated(" + page + ")");//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Notifies this listener that the given page has been closed.
 *
 * @param page the page that was closed
 * @see IWorkbenchPage#close
 */
public void pageClosed(IWorkbenchPage page) {
	System.out.println("pageClosed(" + page + ")");//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Notifies this listener that the given page has been opened.
 *
 * @param page the page that was opened
 * @see IWorkbenchWindow#openPage
 */
public void pageOpened(IWorkbenchPage page) {
	System.out.println("pageOpened(" + page + ")");//$NON-NLS-2$//$NON-NLS-1$
}
}
