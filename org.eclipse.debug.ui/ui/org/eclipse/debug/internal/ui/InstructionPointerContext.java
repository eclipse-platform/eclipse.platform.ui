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
package org.eclipse.debug.internal.ui;

 
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Represents the context for a single instruction pointer.  This is a convenience class
 * used to store the three objects that comprise an instruction pointer 'context' so it
 * can be stored in collections.
 */
public class InstructionPointerContext {

	/**
	 * The stack frame for this context.
	 */
	private IStackFrame fStackFrame;
	
	/**
	 * The text editor for this context.
	 */
	private ITextEditor fTextEditor;
	
	/**
	 * The vertical ruler annotation for this context.
	 */
	private InstructionPointerAnnotation fAnnotation;

	public InstructionPointerContext(IStackFrame stackFrame, ITextEditor textEditor, InstructionPointerAnnotation annotation) {
		setStackFrame(stackFrame);
		setTextEditor(textEditor);
		setAnnotation(annotation);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof InstructionPointerContext) {
			InstructionPointerContext otherContext = (InstructionPointerContext) other;
			return getStackFrame().equals(otherContext.getStackFrame());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getStackFrame().hashCode();
	}

	private void setStackFrame(IStackFrame stackFrame) {
		fStackFrame = stackFrame;
	}

	public IStackFrame getStackFrame() {
		return fStackFrame;
	}

	private void setTextEditor(ITextEditor textEditor) {
		fTextEditor = textEditor;
	}

	public ITextEditor getTextEditor() {
		return fTextEditor;
	}

	private void setAnnotation(InstructionPointerAnnotation annotation) {
		fAnnotation = annotation;
	}

	public InstructionPointerAnnotation getAnnotation() {
		return fAnnotation;
	}
}
