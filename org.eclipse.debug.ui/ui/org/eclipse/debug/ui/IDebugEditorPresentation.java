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
package org.eclipse.debug.ui;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

/**
 * A debug model presentation may implement this interface to provide
 * editor related decorations for editors opened by the debugger.
 * 
 * @since 3.0
 */
public interface IDebugEditorPresentation {
	
	/**
	 * Returns the image used to annotate a line of source code in an editor's
	 * ruler corresponding to the given stack frame, or <code>null</code> if the
	 * default image should be used.
	 *  
	 * @param frame stack frame
	 * @return image used to annotate a line of source code in an editor's
	 * ruler corresponding to the given stack frame, or <code>null</code> if the
	 * default image should be used
	 */
	public Image getInstructionPointerImage(IStackFrame frame);
	
	/**
	 * Provides this editor presentation with a chance to position the given editor to
	 * the appropriate location for the given stack frame. Returns whether this 
	 * editor presentation has performed the select and reveal. When <code>false</code>
	 * is returned, the debugger will perform the select and reveal.
	 *  
	 * @param editorPart the editor the debugger has opened
	 * @param frame the stack frame that corresponds to the editor
	 * @return whether this presentation has performed the select and reveal
	 */
	public boolean selectAndReveal(IEditorPart editorPart, IStackFrame frame);
	
	/**
	 * Removes any debug related decorations in the given editor. This method is
	 * called when the debugger clears the source selection in an editor opened
	 * by the debugger when a debug session is resumed or terminated.
	 *   
	 * @param editorPart an editor that was decorated 
	 * @param thread the thread the editor was decorated for
	 */
	public void removeDecorations(IEditorPart editorPart, IThread thread);
	
	/**
	 * Provides this editor presentation with an opportunity to decorate the given
	 * editor in the context of the given stack frame. This method is called after
	 * <code>selectAndReveal</code>.
	 * 
	 * @param editorPart the editor the debugger has opened
	 * @param frame the stack frame that corresponds to the editor
	 */
	public void decorateEditor(IEditorPart editorPart, IStackFrame frame);

}
