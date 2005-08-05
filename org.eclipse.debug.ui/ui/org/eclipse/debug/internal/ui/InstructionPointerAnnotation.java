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
package org.eclipse.debug.internal.ui;

 
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jface.text.source.Annotation;

/**
 * An annotation for the vertical ruler in text editors that shows one of two
 * images for the current instruction pointer when debugging (one for the top
 * stack frame, one for all others).
 */
public class InstructionPointerAnnotation extends Annotation {

	/**
	 * The frame for this instruction pointer annotation.  This is necessary only so that
	 * instances of this class can be distinguished by equals().
	 */
	private IStackFrame fStackFrame;
	
	/**
	 * Construct an instruction pointer annotation for the given stack frame.
	 * 
	 * @param stackFrame frame to create an instruction pointer annotation for
	 * @param isTopFrame whether the given frame is the top stack frame in its thread 
	 */
	public InstructionPointerAnnotation(IStackFrame stackFrame, boolean isTopFrame) {
		super(isTopFrame ? IInternalDebugUIConstants.ANN_INSTR_POINTER_CURRENT: IInternalDebugUIConstants.ANN_INSTR_POINTER_SECONDARY,
						 false,
						 isTopFrame ? DebugUIMessages.InstructionPointerAnnotation_0 : DebugUIMessages.InstructionPointerAnnotation_1); // 
		fStackFrame = stackFrame;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof InstructionPointerAnnotation) {
			return getStackFrame().equals(((InstructionPointerAnnotation)other).getStackFrame());			
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getStackFrame().hashCode();
	}

	/**
	 * Returns the stack frame associated with this annotation
	 * 
	 * @return the stack frame associated with this annotation
	 */
	private IStackFrame getStackFrame() {
		return fStackFrame;
	}

}
