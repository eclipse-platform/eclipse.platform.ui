/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.part.components.services.IActionBarContributor;
import org.eclipse.ui.internal.part.services.NullActionBars;

/**
 * @since 3.1
 */
public class OldEditorToNewWrapper extends OldPartToNewWrapper {
	private CompatibilityPartSite site;
	
	private IEditorPart part;
	
    private IActionBarContributor actionBarContributor;
    
	public OldEditorToNewWrapper(IEditorPart part, StandardWorkbenchServices services) throws CoreException, ComponentException {
        super(services);
        
		this.part = part;
        actionBarContributor = services.getActionBarContributorFactory().getContributor(services.getDescriptor());
        
        IActionBars actionBars = (IActionBars)Components.getAdapter(actionBarContributor, IActionBars.class);
        
        if (actionBars == null) {
            actionBars = new NullActionBars();
        }
        
		site = new CompatibilityPartSite(
                services, part, 
                (IEditorActionBarContributor)Components.getAdapter(actionBarContributor, IEditorActionBarContributor.class), 
                actionBars);
				
		try {
			part.init(site, services.getEditorInput());
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
        
        return ""; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        IEditorPart part = (IEditorPart)getPart();
        return part.getEditorInput();
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
        
        return ""; //$NON-NLS-1$    
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#isDirty()
     */
    public boolean isDirty() {
        IEditorPart part = (IEditorPart)getPart();
        return part.isDirty();
    }
    
    public void dispose() {
        super.dispose();
        
        actionBarContributor.dispose();
    }
}
