package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
 /**
  *  Handles any revert problem
  */
public interface IProblemHandler {
	
	/**
	 * 
	 * @since 2.0 
	 */

	boolean reportProblem(String problemText);
		
}

