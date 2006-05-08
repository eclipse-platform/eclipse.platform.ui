/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;


import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input for a stack frame for which source could not be located.
 * 
 * @since 2.1
 */
public class SourceNotFoundEditorInput extends PlatformObject implements IEditorInput {
	
	/**
	 * Associated stack frame
	 */
	private IStackFrame fFrame;
	
	/**
	 * Stack frame text (cached on creation)
	 */
	private String fFrameText;

	/**
	 * Constructs an editor input for the given stack frame,
	 * to indicate source could not be found.
	 * 
	 * @param frame stack frame
	 */
	public SourceNotFoundEditorInput(IStackFrame frame) {
		fFrame = frame;
		IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation(frame.getModelIdentifier());
		fFrameText = pres.getText(frame);
		pres.dispose();
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getDefaultImageDescriptor(fFrame);
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		try {
			return fFrame.getName();
		} catch (DebugException e) {
			return DebugUIViewsMessages.SourceNotFoundEditorInput_Source_Not_Found_1; 
		}
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return MessageFormat.format(DebugUIViewsMessages.SourceNotFoundEditorInput_Source_not_found_for__0__2,new String[] {fFrameText}); 
	}

}
