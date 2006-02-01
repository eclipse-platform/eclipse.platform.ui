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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class AsynchronousTableViewerContentManager implements Listener {

    private Table fTable;

    private int fItemCount = 0;

    private Object[] fElements = new Object[50];

    private AsynchronousTableViewer fViewer;

    public AsynchronousTableViewerContentManager(AsynchronousTableViewer viewer) {
        fViewer = viewer;
        fTable = viewer.getTable();
        fTable.addListener(SWT.SetData, this);
    }

    public void handleEvent(Event event) {
        TableItem item = (TableItem) event.item;
        int index = fTable.indexOf(item);
        Object element = fElements[index];
        fViewer.map(element, item);
        fViewer.updateLabel(element, item);
    }

    public void setElements(Object[] newElements) {
        growElementArray(newElements.length);
        int oldItemCount = fItemCount;
        
        setItemCount(newElements.length);

        int topIndex = fTable.getTopIndex();
        int visibleItems = getVisibleItemCount(topIndex);
        int bottomIndex = topIndex + visibleItems;

        int index = 0;
        for (int i = 0; i < newElements.length; i++) {
            Object element = newElements[i];
            fElements[index] = element;
            TableItem item = fTable.getItem(index);
            if (index >= topIndex && index <= bottomIndex) {
                if (item.getData() != null) {
                    fViewer.remap(fElements[index], item);
                } else {
                    // item is visible, but data is null??
                    fViewer.map(fElements[index], item);
                }
                fViewer.updateLabel(fElements[index], item);
            } else {
                fViewer.unmap(fElements[i], item);
                fTable.clear(index);
            }
            index++;
        }
        
        while (index < oldItemCount && fTable.getItemCount() > index) {
            TableItem item = fTable.getItem(index);
            fViewer.unmap(fElements[index], item);
            fElements[index] = null;
            index++;
        }
    }

    private void setItemCount(int itemCount) {
        fItemCount = itemCount;
        fTable.setItemCount(fItemCount);
        fTable.redraw();
    }

    private void growElementArray(int size) {
        if (size > fElements.length) {
            Object[] elements = new Object[size];
            System.arraycopy(fElements, 0, elements, 0, fElements.length);
            fElements = elements;
        }
    }

    public int getVisibleItemCount(int top) {
        int itemCount = fTable.getItemCount();
        return Math.min((fTable.getBounds().height / fTable.getItemHeight()) + 2, itemCount - top);
    }

    public void dispose() {
        fElements = null;
        fViewer = null;
    }

    public Object[] getElements() {
        Object[] elements = new Object[fItemCount];
        System.arraycopy(fElements, 0, elements, 0, fItemCount);
        return elements;
    }

    public void add(Object[] elements) {
        growElementArray(fItemCount + elements.length);
        for (int index = fItemCount; index < fItemCount + elements.length; index++) {
            fElements[index] = elements[index - fItemCount];
        }
        setItemCount(fItemCount + elements.length);
    }

    public void remove(Object[] elements) {
        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            remove(element);
        }
    }

    private void remove(Object element) {
        for (int i = 0; i < fItemCount; i++) {
            Object obj = fElements[i];
            if (element.equals(obj)) {
                System.arraycopy(fElements, i + 1, fElements, i, fItemCount - 1);
                TableItem item = fTable.getItem(i);
                fViewer.unmap(element, item);
                item.dispose();
                fItemCount --;
            }
        } 
    }

    public void insert(Object[] elements, int index) {
        growElementArray(fItemCount + elements.length);
        System.arraycopy(fElements, index, fElements, index + elements.length, fItemCount - index);

        int topIndex = fTable.getTopIndex();
        int visibleItems = getVisibleItemCount(topIndex);
        int bottomIndex = topIndex + visibleItems;

        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            fElements[index + i] = element;
        }

        setItemCount(fItemCount + elements.length);

        for (int i = index; i < fItemCount; i++) {
            Object element = fElements[i];

            TableItem item = fTable.getItem(i);
            Object data = item != null ? item.getData() : null;

            if (i >= topIndex && i <= bottomIndex) {
                if (data != null) {
                    fViewer.remap(element, item);
                } else {
                    fViewer.map(element, item);
                }
                fViewer.updateLabel(element, item);
            } else {
                if (data != null) {
                    fViewer.unmap(element, item);
                }
                fTable.clear(i);
            }
        }
    }

    public void replace(Object element, Object replacement) {
        for (int i = 0; i < fItemCount; i++) {
            Object obj = fElements[i];
            if (obj.equals(element)) {
                TableItem item = fTable.getItem(i);
                Object data = item.getData();

                int topIndex = fTable.getTopIndex();
                int visibleItems = getVisibleItemCount(topIndex);
                int bottomIndex = topIndex + visibleItems;

                if (i >= topIndex && i <= bottomIndex) {
                    if (data != null) {
                        fViewer.remap(replacement, item);
                    } else {
                        fViewer.map(replacement, item);
                    }
                    fViewer.updateLabel(replacement, item);
                } else {
                    if (data != null) {
                        fViewer.unmap(element, item);
                    }
                    fTable.clear(i);
                }

                fElements[i] = replacement;
                return;
            }
        }
    }
    
    public Object getElement(int index)
    {
    	if (index >= 0 && index < fElements.length)
    		return fElements[index];
    	
    	return null;
    }
    
    public Object getElement(TableItem item)
    {
    	int i = fTable.indexOf(item);
    	if (i > 0 && i<fElements.length)
    		return fElements[i];
    	
    	return null;
    }
    
    public int indexOfElement(Object element)
    {
    	for (int i=0; i<fElements.length; i++)
    	{
    		if (fElements[i] == element)
    			return i;
    	}
    	return -1;
    }
    
    protected AsynchronousTableViewer getTableViewer()
    {
    	return fViewer;
    }
    
    
}
