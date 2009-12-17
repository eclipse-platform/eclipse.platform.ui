/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 * 	 Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *    font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;


/**
 * A list selection dialog with two panes. Duplicated entries will be folded
 * together and are displayed in the lower pane (qualifier).
 * 
 * @since 2.0
 */
public class TwoPaneElementSelector extends AbstractElementListSelectionDialog {
    private String fUpperListLabel;

    private String fLowerListLabel;
    
    /**
     * The comparator used to sort the list in the lower pane.
     * @since 3.5
     */
    private Comparator fLowerListComparator = null;

    private ILabelProvider fQualifierRenderer;

    private Object[] fElements = new Object[0];

    private Table fLowerList;

    private Object[] fQualifierElements;

    /**
     * Creates the two pane element selector.
     * 
     * @param parent
     *            the parent shell.
     * @param elementRenderer
     *            the element renderer.
     * @param qualifierRenderer
     *            the qualifier renderer.
     */
    public TwoPaneElementSelector(Shell parent, ILabelProvider elementRenderer,
            ILabelProvider qualifierRenderer) {
        super(parent, elementRenderer);
        setSize(50, 15);
        setAllowDuplicates(false);
        fQualifierRenderer = qualifierRenderer;
    }

    /**
     * Sets the upper list label. If the label is <code>null</code> (default),
     * no label is created.
     * 
     * @param label
     */
    public void setUpperListLabel(String label) {
        fUpperListLabel = label;
    }

    /**
     * Sets the lower list label.
     * 
     * @param label
     *            String or <code>null</code>. If the label is
     *            <code>null</code> (default), no label is created.
     */
    public void setLowerListLabel(String label) {
        fLowerListLabel = label;
    }

    /**
     * Sets the comparator used to sort the list in the lower pane.
     * <p>
     * Note: the comparator might want to honor
     * {@link AbstractElementListSelectionDialog#isCaseIgnored()}.
     * </p>
     * 
     * @param comparator
     *            a Comparator or <code>null</code> if <code>String</code>'s
     *            comparison methods should be used
     * @since 3.5
     */
    public void setLowerListComparator(Comparator comparator) {
    	fLowerListComparator = comparator;
    }

    /**
     * Sets the elements to be displayed.
     * 
     * @param elements
     *            the elements to be displayed.
     */
    public void setElements(Object[] elements) {
        fElements = elements;
    }

    /*
     * @see Dialog#createDialogArea(Composite)
     */
    public Control createDialogArea(Composite parent) {
        Composite contents = (Composite) super.createDialogArea(parent);
        createMessageArea(contents);
        createFilterText(contents);
        createLabel(contents, fUpperListLabel);
        createFilteredList(contents);
        createLabel(contents, fLowerListLabel);
        createLowerList(contents);
        setListElements(fElements);
        List initialSelections = getInitialElementSelections();
        if (!initialSelections.isEmpty()) {
            Object element = initialSelections.get(0);
            setSelection(new Object[] { element });
            setLowerSelectedElement(element);
        }
        return contents;
    }

    /**
     * Creates a label if name was not <code>null</code>.
     * 
     * @param parent
     *            the parent composite.
     * @param name
     *            the name of the label.
     * @return returns a label if a name was given, <code>null</code>
     *         otherwise.
     */
    protected Label createLabel(Composite parent, String name) {
        if (name == null) {
			return null;
		}
        Label label = new Label(parent, SWT.NONE);
        label.setText(name);
        label.setFont(parent.getFont());
        return label;
    }

    /**
     * Creates the list widget and sets layout data.
     * 
     * @param parent
     *            the parent composite.
     * @return returns the list table widget.
     */
    protected Table createLowerList(Composite parent) {
        Table list = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        list.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event evt) {
                handleLowerSelectionChanged();
            }
        });
        list.addListener(SWT.MouseDoubleClick, new Listener() {
            public void handleEvent(Event evt) {
                handleDefaultSelected();
            }
        });
        list.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                fQualifierRenderer.dispose();
            }
        });
        GridData data = new GridData();
        data.widthHint = convertWidthInCharsToPixels(50);
        data.heightHint = convertHeightInCharsToPixels(5);
        data.grabExcessVerticalSpace = true;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        list.setLayoutData(data);
        list.setFont(parent.getFont());
        fLowerList = list;
        return list;
    }

    /**
     * @see SelectionStatusDialog#computeResult()
     */
    protected void computeResult() {
        Object[] results = new Object[] { getLowerSelectedElement() };
        setResult(Arrays.asList(results));
    }

    /**
     * @see AbstractElementListSelectionDialog#handleDefaultSelected()
     */
    protected void handleDefaultSelected() {
        if (validateCurrentSelection() && (getLowerSelectedElement() != null)) {
			buttonPressed(IDialogConstants.OK_ID);
		}
    }

    /**
     * @see AbstractElementListSelectionDialog#handleSelectionChanged()
     */
    protected void handleSelectionChanged() {
        handleUpperSelectionChanged();
    }

    private void handleUpperSelectionChanged() {
        int indices[] = getSelectionIndices();
        fLowerList.removeAll();
		int elementCount = 0;
		List elements= new ArrayList(indices.length * 5);
        for (int i= 0; i < indices.length; i++) {
        	Object[] foldedElements= getFoldedElements(indices[i]);
			if (foldedElements != null) {
				elementCount = elementCount + foldedElements.length;
				elements.add(getFoldedElements(indices[i]));
			}
		}
		if (elementCount > 0) {
			fQualifierElements = new Object[elementCount];
			int destPos = 0;
			Iterator iterator = elements.iterator();
			while (iterator.hasNext()) {
				Object[] objects= (Object[])iterator.next();
				System.arraycopy(objects, 0, fQualifierElements, destPos, objects.length);
				destPos = destPos + objects.length;
			}
			updateLowerListWidget(fQualifierElements);
		} else {
			fQualifierElements = null;
			updateLowerListWidget(new Object[] {});
		}

        validateCurrentSelection();
    }

    private void handleLowerSelectionChanged() {
        validateCurrentSelection();
    }

    /**
     * Selects an element in the lower pane.
     * @param element
     */
    protected void setLowerSelectedElement(Object element) {
        if (fQualifierElements == null) {
			return;
		}
        // find matching index
        int i;
        for (i = 0; i != fQualifierElements.length; i++) {
			if (fQualifierElements[i].equals(element)) {
				break;
			}
		}
        // set selection
        if (i != fQualifierElements.length) {
			fLowerList.setSelection(i);
		}
    }

    /**
     * Returns the selected element from the lower pane.
     * @return Object
     */
    protected Object getLowerSelectedElement() {
        int index = fLowerList.getSelectionIndex();
        if (index >= 0) {
			return fQualifierElements[index];
		}
        return null;
    }

    private void updateLowerListWidget(Object[] elements) {
        int length = elements.length;
        String[] qualifiers = new String[length];
        for (int i = 0; i != length; i++){
        	String text = fQualifierRenderer.getText(elements[i]);
        	if(text == null) {
				text = ""; //$NON-NLS-1$
			}
            qualifiers[i] = text;
        }
        
        TwoArrayQuickSorter sorter;
        if (fLowerListComparator == null) {
        	sorter = new TwoArrayQuickSorter(isCaseIgnored());
        } else {
        	sorter = new TwoArrayQuickSorter(fLowerListComparator);
        }
        
        sorter.sort(qualifiers, elements);
        for (int i = 0; i != length; i++) {
            TableItem item = new TableItem(fLowerList, SWT.NONE);
            item.setText(qualifiers[i]);
            item.setImage(fQualifierRenderer.getImage(elements[i]));
        }
        if (fLowerList.getItemCount() > 0) {
			fLowerList.setSelection(0);
		}
    }

    /*
     * @see AbstractElementListSelectionDialog#handleEmptyList()
     */
    protected void handleEmptyList() {
        super.handleEmptyList();
        fLowerList.setEnabled(false);
    }
    
    /**
     * @see AbstractElementListSelectionDialog#validateCurrentSelection()
     * @since 3.5
     */
    protected boolean validateCurrentSelection() {
    	ISelectionStatusValidator validator = getValidator();
    	Object lowerSelection = getLowerSelectedElement();
    	
    	if (validator != null && lowerSelection != null) {
    		IStatus status = validator.validate(new Object [] {lowerSelection});
    		updateStatus(status);
    		return status.isOK();
    	}
        return super.validateCurrentSelection();
     }
}
