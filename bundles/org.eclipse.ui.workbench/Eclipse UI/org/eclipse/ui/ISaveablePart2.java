/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * Workbench parts implement or adapt to this interface to participate
 * in actions that require a prompt for the user to provide input on 
 * what to do with unsaved data when the part is closed or the Workbench
 * is shut down.
 * <p>
 * Note that if a part implements this interface, it is excluded from the
 * common "prompt to save" dialog, and instead opens its own dialog.  This may
 * cause multiple prompts to the end user during a single user operation.
 * Implementors should be aware that this may lead to a less than optimal
 * user experience.
 * </p>
 *  
 * @since 3.1
 */
public interface ISaveablePart2 extends ISaveablePart {
	
	/**
	 * Standard return code constant (value 0) indicating that the part
	 * needs to be saved.
	 */
	public static final int YES = 0;
	
	/**
	 * Standard return code constant (value 1) indicating that the part
	 * does not need to be saved and the part should be closed.
	 */
	public static final int NO = 1;
	
	/**
	 * Standard return code constant (value 2) indicating that the part
	 * does not need to be saved and the part should not be closed.
	 */
	public static final int CANCEL = 2;
	
	/**
	 * Standard return code constant (value 3) indicating that the default
	 * behavior for prompting the user to save will be use.
	 */
	public static final int DEFAULT = 3;
		
    /**
     * Prompts the user for input on what to do with unsaved data.  
     * This method is only called when the part is closed or when
     * the Workbench is shutting down.
     * <p>
     * Implementors are expected to open a custom dialog where the 
     * user will be able to determine what to do with the unsaved data.
     * Implementors may also return a result of <code>DEFAULT</code> 
     * to get the default prompt handling from the Workbench.
     * </p>
     * 
	 * @return the return code, must be either <code>YES</code>, 
	 * <code>NO</code>, <code>CANCEL</code> or <code>DEFAULT</code>.
	 */
	public int promptToSaveOnClose();

}
