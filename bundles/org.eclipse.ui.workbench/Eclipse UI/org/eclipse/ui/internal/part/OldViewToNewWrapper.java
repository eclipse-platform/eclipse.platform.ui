/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.part.services.IPartDescriptor;

/**
 * @since 3.1
 */
public class OldViewToNewWrapper extends OldPartToNewWrapper {
	
	private IViewSite site;
    private IPartDescriptor descriptor;
    
    private IViewPart part;
    private IEditorInput editorInput;
    
    public OldViewToNewWrapper(IViewPart part, StandardWorkbenchServices services) throws ComponentException {
        super(services);
    	this.part = part;
        
        descriptor = services.getDescriptor();
        
        editorInput = services.getEditorInput();
        site = new CompatibilityPartSite(
                services, part, null);
				
        try {
			part.init(site, services.getState());
		} catch (PartInitException e) {
			throw new ComponentException(part.getClass(), e);
		}

		part.createPartControl(services.getParentComposite());
		
		setPart(part);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getContentDescription()
     */
    public String getContentDescription() {
        IWorkbenchPart part = getPart();
        if (part instanceof IWorkbenchPart2) {
            IWorkbenchPart2 wbp2 = (IWorkbenchPart2)part;
            
            return wbp2.getContentDescription();
        }
        
        String title = getTitle();
        if (title.equals(getPartName())) {
            return ""; //$NON-NLS-1$
        }
        return getTitle();
    }
    
    public String getTitle() {
        return getPart().getTitle();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        return editorInput;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getPartName()
     */
    public String getPartName() {
        IWorkbenchPart part = getPart();
        if (part instanceof IWorkbenchPart2) {
            IWorkbenchPart2 wbp2 = (IWorkbenchPart2)part;
            
            return wbp2.getPartName();
        }
        
        return descriptor.getLabel();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#isDirty()
     */
    public boolean isDirty() {
        return false;
    }
}
