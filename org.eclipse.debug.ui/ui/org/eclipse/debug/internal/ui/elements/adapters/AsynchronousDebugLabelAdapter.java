/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.elements.adapters;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.viewers.AsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Asynchronous label adapter for debug elements.
 * 
 * @since 3.2
 */
public class AsynchronousDebugLabelAdapter extends AsynchronousLabelAdapter {
	      
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#computeLabels(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext, org.eclipse.debug.ui.viewers.ILabelRequestMonitor)
	 */
	protected void computeLabels(Object element, IPresentationContext context, ILabelRequestMonitor monitor) {
    	DelegatingModelPresentation presentation = DebugElementHelper.getPresentation();
    	// Honor view specific settings in a debug view by copying model presentation settings
    	// into the debug element helper's presentation before we get the label. This allows
    	// for qualified name and type name settings to remain in tact.
    	if (element instanceof IDebugElement && context.getPart() instanceof IDebugView) {
    		IWorkbenchPart part = context.getPart();
    		if (part instanceof IDebugView) {
    			IDebugModelPresentation pres = ((IDebugView)part).getPresentation(((IDebugElement)element).getModelIdentifier());
    			Map settings = null;
	    		synchronized (presentation) {
	    			if (pres instanceof DelegatingModelPresentation) {
	    				settings = ((DelegatingModelPresentation)pres).getAttributes();
	    			} else if (pres instanceof LazyModelPresentation) {
	    				settings = ((LazyModelPresentation)pres).getAttributes();
	    			}
	    			if (settings != null) {
			    		Iterator iterator = settings.entrySet().iterator();
			    		while (iterator.hasNext()) {
			    			Map.Entry entry = (Entry) iterator.next();
			    			presentation.setAttribute((String) entry.getKey(), entry.getValue());
			    		}
			        	super.computeLabels(element, context, monitor);
			        	return;
	    			}
	    		}
	    	}
		}
    	super.computeLabels(element, context, monitor);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getLabels(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected String[] getLabels(Object element, IPresentationContext context) throws CoreException {
		return new String[] {DebugElementHelper.getLabel(element)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getImageDescriptors(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected ImageDescriptor[] getImageDescriptors(Object element, IPresentationContext context) throws CoreException {
		return new ImageDescriptor[] {DebugElementHelper.getImageDescriptor(element)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getFontDatas(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected FontData[] getFontDatas(Object element, IPresentationContext context) throws CoreException {
		return new FontData[] {DebugElementHelper.getFont(element)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getForegrounds(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected RGB[] getForegrounds(Object element, IPresentationContext context) throws CoreException {
		return new RGB[] {DebugElementHelper.getForeground(element)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousLabelAdapter#getBackgrounds(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	protected RGB[] getBackgrounds(Object element, IPresentationContext context) throws CoreException {
		return new RGB[] {DebugElementHelper.getBackground(element)};
	}

}
