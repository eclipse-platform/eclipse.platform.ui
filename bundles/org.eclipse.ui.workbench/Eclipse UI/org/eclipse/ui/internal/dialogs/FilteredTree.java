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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A simple control that provides a text widget and a tree viewer. The contents
 * of the text widget are used to drive a PatternFilter that is on the viewer.
 * 
 * @see org.eclipse.ui.internal.dialogs.PatternFilter
 * @since 3.0
 */
public class FilteredTree extends Composite {

    private Text filterField;

    private ToolBarManager filterToolBar;

    private TreeViewer treeViewer;

    private Composite filterParent;

    private PatternFilter patternFilter;

    private FocusListener listener;

    private static final String CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEAR_ICON"; //$NON-NLS-1$

    private static final String DCLEAR_ICON = "org.eclipse.ui.internal.dialogs.DCLEAR_ICON"; //$NON-NLS-1$

    private String initialText = ""; //$NON-NLS-1$

    static {
        ImageDescriptor descriptor = AbstractUIPlugin
                .imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,
                        "icons/full/etool16/delete_edit.gif"); //$NON-NLS-1$
        if (descriptor != null) {
            JFaceResources.getImageRegistry().put(CLEAR_ICON, descriptor);
        }
        descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                PlatformUI.PLUGIN_ID, "icons/full/dtool16/delete_edit.gif"); //$NON-NLS-1$
        if (descriptor != null) {
            JFaceResources.getImageRegistry().put(DCLEAR_ICON, descriptor);
        }
    }

    /**
     * Create a new instance of the receiver. It will be created with a default
     * pattern filter.
     * 
     * @param parent
     *            the parent composite
     * @param treeStyle
     *            the SWT style bits to be passed to the tree viewer
     */
    public FilteredTree(Composite parent, int treeStyle) {
        this(parent, treeStyle, new PatternFilter());
    }

    /**
     * Create a new instance of the receiver.
     * 
     * @param parent
     *            parent <code>Composite</code>
     * @param treeStyle
     *            the style bits for the <code>Tree</code>
     * @param filter
     *            the filter to be used
     */
    public FilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
        super(parent, SWT.NONE);
        patternFilter = filter;
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        filterParent = new Composite(this, SWT.NONE);
        GridLayout filterLayout = new GridLayout();
        filterLayout.numColumns = 2;
        filterLayout.marginHeight = 0;
        filterLayout.marginWidth = 0;
        filterParent.setLayout(filterLayout);

        filterParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        filterField = new Text(filterParent, SWT.SINGLE | SWT.BORDER);
        filterField.addKeyListener(new KeyAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            public void keyReleased(KeyEvent e) {
                textChanged();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL);
        filterField.setLayoutData(data);

        ToolBar toolBar = new ToolBar(filterParent, SWT.FLAT | SWT.HORIZONTAL);
        filterToolBar = new ToolBarManager(toolBar);

        createClearText(filterToolBar);

        filterToolBar.update(false);
        // initially there is no text to clear
        filterToolBar.getControl().setEnabled(false);

        treeViewer = new TreeViewer(this, treeStyle);
        data = new GridData(GridData.FILL_BOTH);
        treeViewer.getControl().setLayoutData(data);
        treeViewer.addFilter(patternFilter);
    }

    /**
     * update the receiver after the text has changed
     */
    private void textChanged() {
        String filterText = filterField.getText();
        boolean initial = initialText != null && filterText.equals(initialText); 
        if (initial) {
            patternFilter.setPattern(null);
        } else {
            patternFilter.setPattern(filterField.getText());
        }        
        treeViewer.refresh(false);
        
        if (filterText.length() > 0 && !initial) {
            treeViewer.expandAll();
            // enabled toolbar is a hint that there is text to clear
            // and the list is currently being filtered
            filterToolBar.getControl().setEnabled(true);
        } else {
            // disabled toolbar is a hint that there is no text to clear
            // and the list is currently not filtered
            filterToolBar.getControl().setEnabled(false);
        }
    }

    /**
     * Set the background for the widgets that support the filter text area
     * 
     * @param background
     */
    public void setBackground(Color background) {
        super.setBackground(background);
        filterParent.setBackground(background);
        filterField.setBackground(background);
        filterToolBar.getControl().setBackground(background);
    }

    /**
     * Create the button that clears the text.
     * 
     * @param filterToolBar
     */
    private void createClearText(ToolBarManager filterToolBar) {

        IAction clearTextAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.action.Action#run()
             */
            public void run() {
                clearText();
            }
        };

        clearTextAction.setToolTipText(WorkbenchMessages
                .getString("FilteredTree.ClearToolTip")); //$NON-NLS-1$
        clearTextAction.setImageDescriptor(JFaceResources.getImageRegistry()
                .getDescriptor(CLEAR_ICON));
        clearTextAction.setDisabledImageDescriptor(JFaceResources
                .getImageRegistry().getDescriptor(DCLEAR_ICON));

        filterToolBar.add(clearTextAction);
    }

    /**
     * clear the text in the filter text widget
     */
    protected void clearText() {
        filterField.setText(""); //$NON-NLS-1$
        textChanged();
    }

    /**
     * Get the tree viewer associated with this control.
     * 
     * @return the tree viewer
     */
    public TreeViewer getViewer() {
        return treeViewer;
    }

    /**
     * Get the filter text field associated with this contro.
     * 
     * @return the text field
     */
    public Text getFilterField() {
        return filterField;
    }

    /**
     * Set the text that will be shown until the first focus.
     * 
     * @param text
     */
    public void setInitialText(String text) {
        initialText = text;
        resetText();
    }

    /**
     * Set the text in the filter field back to the intial text 
     */
    public void resetText() {
        filterField.setText(initialText);
        textChanged();
        listener = new FocusListener() {
            public void focusGained(FocusEvent event) {
                filterField.setText(""); //$NON-NLS-1$
                filterField.removeFocusListener(listener);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
             */
            public void focusLost(FocusEvent e) {
            }
        };
        filterField.addFocusListener(listener);
    }
}