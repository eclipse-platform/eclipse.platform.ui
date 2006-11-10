/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 3.3
 */
public class DebugElementLabelProvider extends ElementLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getLabel(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return debugContext.getModelPresentation().getText(element);
		}
		return DebugElementHelper.getLabel(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getBackground(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected RGB getBackground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getBackground(element, debugContext.getModelPresentation());
		}
		return DebugElementHelper.getBackground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getFontDatas(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected FontData getFontData(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getFont(element, debugContext.getModelPresentation());
			
		}
		return DebugElementHelper.getFont(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getForeground(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected RGB getForeground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getForeground(element, debugContext.getModelPresentation());	
		}
		return DebugElementHelper.getForeground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getImageDescriptor(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected ImageDescriptor getImageDescriptor(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		if (presentationContext instanceof DebugModelPresentationContext) {
			DebugModelPresentationContext debugContext = (DebugModelPresentationContext) presentationContext;
			return DebugElementHelper.getImageDescriptor(element, debugContext.getModelPresentation());	
		}
		return DebugElementHelper.getImageDescriptor(element);
	}
	


}
