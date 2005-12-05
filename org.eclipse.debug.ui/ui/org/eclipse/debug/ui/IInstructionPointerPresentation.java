/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IEditorPart;

/**
 * A debug model presentation may implement this interface to override
 * standard annotations used to display instruction pointers for stack frames.
 * 
 * @since 3.2
 */
public interface IInstructionPointerPresentation extends IDebugModelPresentation {
	/**
	 * Returns an annotation used for the specified stack frame in the specified
	 * editor, or <code>null</code> if a default annotation should be used.
	 * This method is called when the debugger has opened an editor to display
	 * source for the given stack frame. The annotation will be positioned based
	 * on stack frame line number and character ranges.
	 * <p>
	 * By default, the debug platform uses different annotations for top stack
	 * frames and non-top stack frames in a thread. The default platform annotations
	 * are contributed as <code>markerAnnotationSpecification</code> extensions with
	 * the identifiers {@link IDebugUIConstants.ANNOTATION_INSTRUCTION_POINTER_CURRENT}
	 * and @link {@link IDebugUIConstants.ANNOTAION_INSTRUCTION_POINTER_SECONDARY}.
	 * </p>
	 * @param editorPart the editor the debugger has opened
	 * @param frame the stack frame for which the debugger is displaying
	 *  source
	 */
	public Annotation getInstructionPointerAnnotation(IEditorPart editorPart, IStackFrame frame);
	
}
