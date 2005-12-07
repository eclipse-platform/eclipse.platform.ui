/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.sourcelookup;

import java.text.MessageFormat;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input for the <code>CommonSourceNotFoundEditor</code>. The editor
 * input can be created on a debug element or breakpoint.
 *
 * @see CommonSourceNotFoundEditor
 * 
 * TODO:  new API, need review
 * 
 * @since 3.2
 */
public class CommonSourceNotFoundEditorInput extends PlatformObject implements IEditorInput {
	
	/**
	 * input element label (cached on creation)
	 */
	protected String fLabel;
	/**
	 * the object that the editor is being brought up for
	 */
	protected Object fObject;
	
	/**
	 * Constructs an editor input for the given debug element
	 * or breakpoint.
	 *
	 * @param object debug element or breakpoint
	 */
	public CommonSourceNotFoundEditorInput(Object object) {
		fObject = object;
		if (object != null) {
			IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation();
			fLabel = pres.getText(object);
			pres.dispose();
		}
		if (fLabel == null) {
			fLabel = "";  //$NON-NLS-1$
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
		return DebugUITools.getDefaultImageDescriptor(fObject);
	}
	
	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return fLabel;		
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
		return MessageFormat.format(SourceLookupUIMessages.addSourceLocation_editorMessage, new String[] { fLabel }); 
	}
		
	/**
	 * Returns the object that was the reason why source was being searched for (i.e., it was clicked on)
	 * @return the object.
	 */
	public Object getObject(){
		return fObject;
	}
	
}
