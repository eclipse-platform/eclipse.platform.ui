/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
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

/**
 * A list selection dialog with two panes. Duplicated entries will be folded
 * together and are displayed in the lower pane (qualifier).
 * 
 * @since 2.0
 */
public class TwoPaneElementSelector extends AbstractElementListSelectionDialog {
    private String fUpperListLabel;

    private String fLowerListLabel;

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
        int index = getSelectionIndex();
        fLowerList.removeAll();
        if (index >= 0) {
	        fQualifierElements = getFoldedElements(index);
	        if (fQualifierElements == null) {
				updateLowerListWidget(new Object[] {});
			} else {
				updateLowerListWidget(fQualifierElements);
			}
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
        TwoArrayQuickSorter sorter = new TwoArrayQuickSorter(isCaseIgnored());
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
}
