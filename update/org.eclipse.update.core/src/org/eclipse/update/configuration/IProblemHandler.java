package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
/**
 * Generic problem handler. Used to report status from specific
 * install operations. The methods implemented by this interface
 * are callbacks from the update support to the caller of the update
 * methods.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
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
	 * <p>
	 * <b>Note:</b> This method is part of an interim API that is still under development and expected to
	 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
	 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
	 * (repeatedly) as the API evolves.
	 * </p>
	 */
	boolean reportProblem(String problemText);

}