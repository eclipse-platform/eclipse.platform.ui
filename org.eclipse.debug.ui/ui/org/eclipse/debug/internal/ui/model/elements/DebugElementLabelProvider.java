/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 3.3
 */
public class DebugElementLabelProvider extends ElementLabelProvider {

	protected String getLabel(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return debugContext.getModelPresentation().getText(element);
		}
		return DebugElementHelper.getLabel(element);
	}

	protected RGB getBackground(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getBackground(element, debugContext.getModelPresentation());
		}
		return DebugElementHelper.getBackground(element);
	}

	protected FontData getFontData(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getFont(element, debugContext.getModelPresentation());
			
		}
		return DebugElementHelper.getFont(element);
	}

	protected RGB getForeground(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getForeground(element, debugContext.getModelPresentation());	
		}
		return DebugElementHelper.getForeground(element);
	}

	protected ImageDescriptor getImageDescriptor(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getImageDescriptor(element, debugContext.getModelPresentation());	
		}
		return DebugElementHelper.getImageDescriptor(element);
	}
	
	/**
	 * Returns the model presentation for the specified model, or <code>null</code> if none.
	 * 
	 * @param context presentation context
	 * @param modelId debug model identifier
	 * @return debug model presentation or <code>null</code> 
	 */
	protected IDebugModelPresentation getModelPresentation(IPresentationContext context, String modelId) {
		if (context instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) context;
			IDebugModelPresentation presentation = debugContext.getModelPresentation();
			if (presentation instanceof DelegatingModelPresentation) {
				return ((DelegatingModelPresentation)presentation).getPresentation(modelId);
			}
		}
		return null;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#requiresUIJob(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate[])
     */
    protected boolean requiresUIJob(ILabelUpdate[] updates) {
    	if (updates.length > 0) {
	    	ILabelUpdate update = updates[0];
			IPresentationContext context = update.getPresentationContext();
			if (context instanceof DebugModelPresentationContext) {
		    	DebugModelPresentationContext debugContext = (DebugModelPresentationContext) context;
				IDebugModelPresentation presentation = debugContext.getModelPresentation();
				if (presentation instanceof IDebugModelPresentationExtension) {
					IDebugModelPresentationExtension extension = (IDebugModelPresentationExtension) presentation;
					for (int i = 0; i < updates.length; i++) {
						if (extension.requiresUIThread(updates[i].getElement())) {
							return true;
						}
					}
				}
			}
    	}
		return false;
    }	

}
