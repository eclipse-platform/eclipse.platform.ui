/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.text.MessageFormat;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * The editor input that can be shown by the <code>SourceNotFoundEditor</code>
 * The editor message tells the user that source wasn't found.
 * 
 * May be subclassed if a debugger requires additional buttons on the editor. For example,
 * a button may be added if the user has the additional option of using generated source
 * for debugging.
 * 
 * @see CommonSourceNotFoundEditor
 * 
 * @since 3.0
 */
public class CommonSourceNotFoundEditorInput extends PlatformObject implements IEditorInput {
	
	/**
	 * Associated stack frame
	 */
	private IStackFrame fFrame;
	
	/**
	 * Stack frame text (cached on creation)
	 */
	protected String fFrameText;
	/**
	 * the object that the editor is being brought up for
	 */
	protected Object fObject;
	
	/**
	 * Constructs an editor input for the given stack frame,
	 * to indicate source could not be found.
	 * 
	 * @param frame the stack frame associated
	 * @param object the object that the input is for
	 */
	public CommonSourceNotFoundEditorInput(IStackFrame frame, Object object) {
		fObject = object;
		if(frame == null)
			fFrameText = "";  //$NON-NLS-1$
		else
		{
			fFrame = frame;
			IDebugModelPresentation pres =
				DebugUITools.newDebugModelPresentation(frame.getModelIdentifier());
			fFrameText = pres.getText(frame);
			pres.dispose();
		}
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
		return fFrameText;		
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
		return MessageFormat.format(SourceLookupUIMessages.getString("addSourceLocation.editorMessage"), new String[] { fFrameText }); //$NON-NLS-1$
	}
	
	/**
	 * Returns the stack frame that is associated with this source editor. May be null.
	 * @return the stack frame
	 */
	public IStackFrame getStackFrame(){
		return fFrame;
	}
	
	/**
	 * Returns the object that was the reason why source was being searched for (i.e., it was clicked on)
	 * @return the object.
	 */
	public Object getObject(){
		return fObject;
	}
	
}
