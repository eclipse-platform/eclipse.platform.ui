package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.MultiStatus;

/**
 * Generic problem handler. Used to report status from specific
 * install operations. The methods implemented by this interface
 * are callbacks from the update support to the caller of the update
 * methods.
 * 
 * @since 2.0
 */
public interface IProblemHandler {

	/**
	 * Report problem.
	 * 
	 * @param problemText problem text
	 * @return <code>true</code> if the operation should continue,
	 * <code>false</code> if the operation should be cancelled
	 * @since 2.0 
	 */
	boolean reportProblem(String problemText);

	/**
	 * Report problem.
	 * 
	 * @param problemText problem text
	 * @return <code>true</code> if the operation should continue,
	 * <code>false</code> if the operation should be cancelled	 
	 * @since 2.0 
	 */
	boolean reportProblem(String problemText, MultiStatus status);

}