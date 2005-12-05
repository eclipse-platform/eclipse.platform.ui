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

 
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Represents the context for a single instruction pointer.  This is a convenience class
 * used to store the three objects that comprise an instruction pointer 'context' so it
 * can be stored in collections.
 */
public class InstructionPointerContext {

	/**
	 * The text editor for this context.
	 */
	private ITextEditor fTextEditor;
	
	/**
	 * The vertical ruler annotation for this context.
	 */
	private Annotation fAnnotation;

	public InstructionPointerContext(ITextEditor textEditor, Annotation annotation) {
		setTextEditor(textEditor);
		setAnnotation(annotation);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof InstructionPointerContext) {
			InstructionPointerContext otherContext = (InstructionPointerContext) other;
			return getAnnotation().equals(otherContext.getAnnotation());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getAnnotation().hashCode();
	}

	private void setTextEditor(ITextEditor textEditor) {
		fTextEditor = textEditor;
	}

	public ITextEditor getTextEditor() {
		return fTextEditor;
	}

	private void setAnnotation(Annotation annotation) {
		fAnnotation = annotation;
	}

	public Annotation getAnnotation() {
		return fAnnotation;
	}
}
