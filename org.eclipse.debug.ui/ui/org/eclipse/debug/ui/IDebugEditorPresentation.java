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
import org.eclipse.swt.graphics.Image;

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

}
