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
package org.eclipse.ui.internal;

 
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
/**
 * The <code>SystemSummaryEditorInput</code> is the input for the eclipse diagnostics editor.
 */	
public class SystemSummaryEditorInput implements IEditorInput {
	/**
	 * Creates a new input
	 */
	public SystemSummaryEditorInput() {
		super();
	}

	/**
	 * @see IAdaptable.getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}
	
	/**
	 * @see IEditorInput.exists()
	 */
	public boolean exists() {
		return false;
	}
	
	/**
	 * @see IEditorInput.getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	
	/**
	 * @see IEditorInput.getName()
	 */	
	public String getName() {
		return WorkbenchMessages.getString("SystemSummary.name"); //$NON-NLS-1$
	}
	
	/**
	 * @see IEditorInput.getToolTipText()
	 */	
	public String getToolTipText() {
		return WorkbenchMessages.getString("SystemSummary.tooltip"); //$NON-NLS-1$
	}

	/**
	 * @see IEditorInput.getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return new IPersistableElement() {
			public String getFactoryId() {
				return SystemSummaryEditorInputFactory.FACTORY_ID;
			}
			public void saveState(IMemento memento) {
				return;
			}
		};
	}	
}
