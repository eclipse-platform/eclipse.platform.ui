/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * This adapter class provides default implementations for the methods described
 * by the <code>IPerspectiveListener</code> interface and its extension
 * interfaces.
 * <p>
 * Classes that wish to deal with events which occur as perspectives are added,
 * removed, activated and changed, can extend this class and override only the
 * methods which they are interested in.
 * </p>
 * 
 * @see org.eclipse.ui.IPerspectiveListener
 * @see org.eclipse.ui.IPerspectiveListener2
 * @see org.eclipse.ui.IPerspectiveListener3
 * @see org.eclipse.ui.IPerspectiveListener4
 * @since 3.1
 */
public class PerspectiveAdapter implements IPerspectiveListener4 {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveOpened(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveOpened(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveClosed(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveClosed(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor,
	 *      org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveDeactivated(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveDeactivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveSavedAs(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.ui.IPerspectiveDescriptor,
	 *      org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveSavedAs(IWorkbenchPage page,
			IPerspectiveDescriptor oldPerspective,
			IPerspectiveDescriptor newPerspective) {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 * @since 3.2
	 */
	public void perspectivePreDeactivate(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// do nothing
	}
}
