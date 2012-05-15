/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind Rvier Systems - added support for columns (bug 235646)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.viewers.model.ViewerAdapterService;
import org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

/**
 * @since 3.3
 */
public class ExpressionContentProvider extends VariableContentProvider {

    /**
     * @since 3.6
     * Element object used to wrap the expression error message.  It displays 
     * the error message only in the first column if columns are visible.
     */
    private static class ErrorMessageElement implements IElementLabelProvider {

        public ErrorMessageElement(String message) {
            fMessage = message;
        }
        
        private final String fMessage;
        
        public void update(ILabelUpdate[] updates) {
            for (int i = 0; i < updates.length; i++) {
                String[] columnIds = updates[i].getColumnIds();
                if (columnIds == null) {
                    updateLabel(updates[i], 0);
                } else {
                    for (int j = 0; j < columnIds.length; j++) {
                        if (IDebugUIConstants.COLUMN_ID_VARIABLE_NAME.equals(columnIds[j])) {
                            updateLabel(updates[i], j);
                        } else {
                            updates[i].setLabel(IInternalDebugCoreConstants.EMPTY_STRING, j);
                        }
                    }
                }
                    
                updates[i].done();
            }
        }

        private void updateLabel(ILabelUpdate update, int columnIndex) {
            update.setLabel(fMessage, columnIndex);
            FontData fontData = JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0];
            fontData.setStyle(SWT.ITALIC);                
            
        }
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate[])
	 */
	public void update(IChildrenCountUpdate[] updates) {
		// See if we can delegate to a model specific content provider
		Map delegateMap = new HashMap();
		List notDelegated = new ArrayList();
		findDelegates(delegateMap, notDelegated, updates);
		
		// Batch the updates and send them to the delegates
		for (Iterator iterator = delegateMap.keySet().iterator(); iterator.hasNext();) {
			IElementContentProvider delegate = (IElementContentProvider) iterator.next();
			List updateList = (List)delegateMap.get(delegate);
			delegate.update((IChildrenCountUpdate[])updateList.toArray(new IChildrenCountUpdate[updateList.size()]));
		}
		if (notDelegated.size() > 0){
			super.update((IChildrenCountUpdate[])notDelegated.toArray(new IChildrenCountUpdate[notDelegated.size()]));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate[])
	 */
	public void update(IHasChildrenUpdate[] updates) {
		// See if we can delegate to a model specific content provider
		Map delegateMap = new HashMap();
		List notDelegated = new ArrayList();
		findDelegates(delegateMap, notDelegated, updates);
		
		// Batch the updates and send them to the delegates
		for (Iterator iterator = delegateMap.keySet().iterator(); iterator.hasNext();) {
			IElementContentProvider delegate = (IElementContentProvider) iterator.next();
			List updateList = (List)delegateMap.get(delegate);
			delegate.update((IHasChildrenUpdate[])updateList.toArray(new IHasChildrenUpdate[updateList.size()]));
		}
		if (notDelegated.size() > 0){
			super.update((IHasChildrenUpdate[])notDelegated.toArray(new IHasChildrenUpdate[notDelegated.size()]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate[])
	 */
	public void update(IChildrenUpdate[] updates) {
		// See if we can delegate to a model specific content provider
		Map delegateMap = new HashMap();
		List notDelegated = new ArrayList();
		findDelegates(delegateMap, notDelegated, updates);
		
		// Batch the updates and send them to the delegates
		for (Iterator iterator = delegateMap.keySet().iterator(); iterator.hasNext();) {
			IElementContentProvider delegate = (IElementContentProvider) iterator.next();
			List updateList = (List)delegateMap.get(delegate);
			delegate.update((IChildrenUpdate[])updateList.toArray(new IChildrenUpdate[updateList.size()]));
		}
		if (notDelegated.size() > 0){
			super.update((IChildrenUpdate[])notDelegated.toArray(new IChildrenUpdate[notDelegated.size()]));
		}
	}
	
	/**
	 * Finds all possibly delegate content providers for the given set of updates.  Found delegates are added
	 * to the given map as the key while the list of updates to be sent to that delegate are set as the value.
	 * Any updates that are not to be delegated are put in the notDelegated list.
	 * 
	 * @param delegateMap map to add delegates to
	 * @param notDelegated list of updates that should not be delegated
	 * @param updates array of updates that can be handled by delegates
	 * @since 3.4
	 */
	private void findDelegates(Map delegateMap, List notDelegated, IViewerUpdate[] updates){
		for (int i = 0; i < updates.length; i++) {
			if (updates[i] instanceof ViewerUpdateMonitor && !((ViewerUpdateMonitor)updates[i]).isDelegated() && updates[i].getElement() instanceof IExpression){
				IElementContentProvider delegate = ViewerAdapterService.getContentProvider(((IExpression)updates[i].getElement()).getValue());
				if (delegate != null){
					List updateList = (List)delegateMap.get(delegate);
					if (updateList == null){
						updateList = new ArrayList();
						delegateMap.put(delegate, updateList);
					}
					((ViewerUpdateMonitor)updates[i]).setDelegated(true);
					updateList.add(updates[i]);
					continue;
				}
			}
			notDelegated.add(updates[i]);
		}
	}


	protected Object[] getAllChildren(Object parent, IPresentationContext context) throws CoreException {
       if (parent instanceof IErrorReportingExpression) {
            IErrorReportingExpression expression = (IErrorReportingExpression) parent;
            if (expression.hasErrors()) {
                String[] messages = expression.getErrorMessages();
                LinkedHashSet set = new LinkedHashSet(messages.length);
                for (int i = 0; i < messages.length; i++) {
					set.add(new ErrorMessageElement(messages[i]));
				}
                return set.toArray();
            }
        }
        if (parent instanceof IExpression) {
            IExpression expression = (IExpression) parent;
            IValue value = expression.getValue();
            if (value != null) {
                return getValueChildren(expression, value, context);
            }
        }
        return EMPTY;	
	}
	
	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (element instanceof IErrorReportingExpression) {
			IErrorReportingExpression expression = (IErrorReportingExpression) element;
			if (expression.hasErrors()) {
				return true;
			}
		}
		IValue value = ((IExpression)element).getValue();
		if (value == null) {
			return false;
		}
		return value.hasVariables();
	}	
}
