/*******************************************************************************
 * Copyright (c) 2005 Tobias Widmer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Tobias Widmer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Event object to communicate refactoring history notifications. These include
 * the addition and removal of refactoring descriptors to the global refactoring
 * history index.
 * <p>
 * Refactoring history listeners must be prepared to receive notifications from
 * a background thread. Any UI access occurring inside the implementation must
 * be properly synchronized using the techniques specified by the client's
 * widget library.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public final class RefactoringHistoryEvent {

	/** Event type indicating that a refactoring descriptor has been added (value 1) */
	public static final int ADDED= 1;

	/** Event type indicating that a refactoring descriptor has been removed (value 2) */
	public static final int REMOVED= 2;

	/** The refactoring descriptor */
	private final RefactoringDescriptor fDescriptor;

	/** The refactoring history service */
	private final IRefactoringHistoryService fService;

	/** The event type */
	private final int fType;

	/**
	 * Creates a new refactoring history event.
	 * 
	 * @param service
	 *            the refactoring history service
	 * @param type
	 *            the event type
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	public RefactoringHistoryEvent(final IRefactoringHistoryService service, final int type, final RefactoringDescriptor descriptor) {
		Assert.isNotNull(service);
		Assert.isNotNull(descriptor);
		fService= service;
		fType= type;
		fDescriptor= descriptor;
	}

	/**
	 * Returns the refactoring descriptor.
	 * 
	 * @return the refactoring descriptor
	 */
	public RefactoringDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * Returns the event type.
	 * 
	 * @return the event type
	 */
	public int getEventType() {
		return fType;
	}

	/**
	 * Returns the refactoring history service
	 * 
	 * @return the refactoring history service
	 */
	public IRefactoringHistoryService getHistoryService() {
		return fService;
	}
}
