package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;

public class CVSOperationCancelledException extends CVSException {
	/**
	 * Constructor for CVSOperationCancelledException
	 */
	public CVSOperationCancelledException() {
		this(new CVSStatus(IStatus.INFO, Policy.bind("CVSOperationCancelledException.operationCancelled")));
	}
	/**
	 * Constructor for CVSOperationCancelledException
	 */
	public CVSOperationCancelledException(String message) {
		this(new CVSStatus(IStatus.INFO, message));
	}
	/**
	 * Constructor for CVSOperationCancelledException
	 */
	public CVSOperationCancelledException(IStatus status) {
		super(status);
	}
}
