/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - bug 128526, bug 128529
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * The Show View dialog.
 */
public class ShowViewDialog extends Dialog implements
        ISelectionChangedListener, IDoubleClickListener {

    private static final String DIALOG_SETTING_SECTION_NAME = "ShowViewDialog"; //$NON-NLS-1$

    private static final int LIST_HEIGHT = 300;

    private static final int LIST_WIDTH = 250;

    private static final String STORE_EXPANDED_CATEGORIES_ID = DIALOG_SETTING_SECTION_NAME
            + ".STORE_EXPANDED_CATEGORIES_ID"; //$NON-NLS-1$    

    private static final String STORE_SELECTED_VIEW_ID = DIALOG_SETTING_SECTION_NAME
            + ".STORE_SELECTED_VIEW_ID"; //$NON-NLS-1$    

    private FilteredTree filteredTree;

    private Button okButton;

    private IViewDescriptor[] viewDescs = new IViewDescriptor[0];

    private IViewRegistry viewReg;

	private IWorkbenchWindow window;

	private Color dimmedForeground;

    /**
     * Constructs a new ShowViewDialog.
     * 
     * @param window the workbench window
     * @param viewReg the view registry
     */
    public ShowViewDialog(IWorkbenchWindow window, IViewRegistry viewReg) {
        super(window.getShell());
        this.window = window;
        this.viewReg = viewReg;
    }

    /**
     * This method is called if a button has been pressed.
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
			saveWidgetValues();
		}
        super.buttonPressed(buttonId);
    }

    /**
     * Notifies that the cancel button of this dialog has been pressed.
     */
    protected void cancelPressed() {
        viewDescs = new IViewDescriptor[0];
        super.cancelPressed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(WorkbenchMessages.ShowView_shellTitle);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.SHOW_VIEW_DIALOG);
    }

    /**
     * Adds buttons to this dialog's button bar.
     * <p>
     * The default implementation of this framework method adds standard ok and
     * cancel buttons using the <code>createButton</code> framework method.
     * Subclasses may override.
     * </p>
     * 
     * @param parent the button bar composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        updateButtons();
    }

    /**
     * Creates and returns the contents of the upper part of this dialog (above
     * the button bar).
     * 
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        // Run super.
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setFont(parent.getFont());

        createFilteredTreeViewer(composite);

        layoutTopControl(filteredTree);
        
        // Use F2... label
        Label label = new Label(composite, SWT.WRAP);
        label.setText(WorkbenchMessages.ShowView_selectViewHelp);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Restore the last state
        restoreWidgetValues();

		applyDialogFont(composite);

		// Return results.
        return composite;
    }

	/**
	 * Blends c1 and c2 based in the provided ratio.
	 * 
	 * @param c1
	 *            first color
	 * @param c2
	 *            second color
	 * @param ratio
	 *            percentage of the first color in the blend (0-100)
	 * @return the RGB value of the blended color
	 * 
	 * copied from FormColors.java
	 */
	private static RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

	private static int blend(int v1, int v2, int ratio) {
		int b = (ratio * v1 + (100 - ratio) * v2) / 100;
		return Math.min(255, b);
	}

	/**
     * Create a new filtered tree viewer in the parent.
     * 
     * @param parent the parent <code>Composite</code>.
     */
    private void createFilteredTreeViewer(Composite parent) {
		PatternFilter filter = new ViewPatternFilter();
		int styleBits = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		filteredTree = new FilteredTree(parent, styleBits, filter);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		
		TreeViewer treeViewer = filteredTree.getViewer();
		Control treeControl = treeViewer.getControl();
		RGB dimmedRGB = blend(treeControl.getForeground().getRGB(), treeControl.getBackground().getRGB(), 60);
		dimmedForeground = new Color(treeControl.getDisplay(), dimmedRGB);
		treeControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dimmedForeground.dispose();
			}
		});
		
		treeViewer.setLabelProvider(new ViewLabelProvider(window, dimmedForeground));
		treeViewer.setContentProvider(new ViewContentProvider());
		treeViewer.setComparator(new ViewComparator((ViewRegistry) viewReg));
		treeViewer.setInput(viewReg);
		treeViewer.addSelectionChangedListener(this);
		treeViewer.addDoubleClickListener(this);
		treeViewer.addFilter(new CapabilityFilter());
		treeViewer.getControl().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleTreeViewerKeyPressed(e);
            }
        });

		// if the tree has only one or zero views, disable the filter text control
		if (hasAtMostOneView(filteredTree.getViewer())) {
			Text filterText = filteredTree.getFilterControl();
			if (filterText != null) {
				filterText.setEnabled(false);
			}
		}
	}

	/**
	 * Return whether or not there are less than two views in the list.
	 * 
	 * @param tree
	 * @return <code>true</code> if there are less than two views in the list.
	 */
	private boolean hasAtMostOneView(TreeViewer tree) {
		ITreeContentProvider contentProvider = (ITreeContentProvider) tree
				.getContentProvider();
		Object[] children = contentProvider.getElements(tree.getInput());

		if (children.length <= 1) {
			if (children.length == 0) {
				return true;
			}
			return !contentProvider.hasChildren(children[0]);
		}
		return false;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection s = (IStructuredSelection) event.getSelection();
        Object element = s.getFirstElement();
        if (filteredTree.getViewer().isExpandable(element)) {
            filteredTree.getViewer().setExpandedState(element, !filteredTree.getViewer().getExpandedState(element));
        } else if (viewDescs.length > 0) {
            saveWidgetValues();
            setReturnCode(OK);
            close();
        }
    }

    /**
     * Return the dialog store to cache values into
     */
    protected IDialogSettings getDialogSettings() {
        IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings section = workbenchSettings
                .getSection(DIALOG_SETTING_SECTION_NAME);
        if (section == null) {
			section = workbenchSettings
                    .addNewSection(DIALOG_SETTING_SECTION_NAME);
		}
        return section;
    }

    /**
     * Returns the descriptors for the selected views.
     * 
     * @return the descriptors for the selected views
     */
    public IViewDescriptor[] getSelection() {
        return viewDescs;
    }

    /**
     * Layout the top control.
     * 
     * @param control the control.
     */
    private void layoutTopControl(Control control) {
        GridData spec = new GridData(GridData.FILL_BOTH);
        spec.widthHint = LIST_WIDTH;
        spec.heightHint = LIST_HEIGHT;
        control.setLayoutData(spec);
    }

    /**
     * Use the dialog store to restore widget values to the values that they
     * held last time this dialog was used to completion.
     */
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();

        String[] expandedCategoryIds = settings
                .getArray(STORE_EXPANDED_CATEGORIES_ID);
        if (expandedCategoryIds == null) {
			return;
		}

        ViewRegistry reg = (ViewRegistry) viewReg;
        ArrayList categoriesToExpand = new ArrayList(expandedCategoryIds.length);
        for (int i = 0; i < expandedCategoryIds.length; i++) {
            IViewCategory category = reg.findCategory(expandedCategoryIds[i]);
            if (category != null) {
				categoriesToExpand.add(category);
			}
        }

        if (!categoriesToExpand.isEmpty()) {
			filteredTree.getViewer().setExpandedElements(categoriesToExpand.toArray());
		}
        
        String selectedViewId = settings.get(STORE_SELECTED_VIEW_ID);
        if (selectedViewId != null) {
            IViewDescriptor viewDesc = reg.find(selectedViewId);
            if (viewDesc != null) {
                filteredTree.getViewer().setSelection(new StructuredSelection(viewDesc), true);
            }
        }
    }

    /**
     * Since OK was pressed, write widget values to the dialog store so that
     * they will persist into the next invocation of this dialog
     */
    protected void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();

        // Collect the ids of the all expanded categories
        Object[] expandedElements = filteredTree.getViewer().getExpandedElements();
        String[] expandedCategoryIds = new String[expandedElements.length];
        for (int i = 0; i < expandedElements.length; ++i) {
			expandedCategoryIds[i] = ((IViewCategory) expandedElements[i]).getId();
		}

        // Save them for next time.
        settings.put(STORE_EXPANDED_CATEGORIES_ID, expandedCategoryIds);
        
        String selectedViewId = ""; //$NON-NLS-1$
        if (viewDescs.length > 0) {
            // in the case of a multi-selection, it's probably less confusing
            // to store just the first rather than the whole multi-selection
            selectedViewId = viewDescs[0].getId();
        }
        settings.put(STORE_SELECTED_VIEW_ID, selectedViewId);
    }

    /**
     * Notifies that the selection has changed.
     * 
     * @param event event object describing the change
     */
    public void selectionChanged(SelectionChangedEvent event) {
        updateSelection(event);
        updateButtons();
    }

    /**
     * Update the button enablement state.
     */
    protected void updateButtons() {
        if (okButton != null) {
            okButton.setEnabled(getSelection().length > 0);
        }
    }

    /**
     * Update the selection object.
     */
    protected void updateSelection(SelectionChangedEvent event) {
        ArrayList descs = new ArrayList();
        IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        for (Iterator i = sel.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof IViewDescriptor) {
                descs.add(o);
            }
        }
        viewDescs = new IViewDescriptor[descs.size()];
        descs.toArray(viewDescs);
    }

    
	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
     * 
     * @since 3.4
     */
	protected IDialogSettings getDialogBoundsSettings() {
        return getDialogSettings();
	}
    void handleTreeViewerKeyPressed(KeyEvent event) {
		// popup the description for the selected view
		if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			ITreeSelection selection = (ITreeSelection) filteredTree
					.getViewer().getSelection();
			// only show description if one view is selected
			if (selection.size() == 1) {
				Object o = selection.getFirstElement();
				if (o instanceof IViewDescriptor) {
					String description = ((IViewDescriptor) o).getDescription();
					if (description.length() == 0)
						description = WorkbenchMessages.ShowView_noDesc;
					popUp(description);
				}
			}
		}
	}

	private void popUp(final String description) {
		new PopupDialog(filteredTree.getShell(), PopupDialog.HOVER_SHELLSTYLE,
				true, false, false, false, null, null) {
			private static final int CURSOR_SIZE = 15;

			protected Point getInitialLocation(Point initialSize) {
				//show popup relative to cursor
				Display display = getShell().getDisplay();
				Point location = display.getCursorLocation();
				location.x += CURSOR_SIZE;
				location.y += CURSOR_SIZE;
				return location;
			}

			protected Control createDialogArea(Composite parent) {
				Label label = new Label(parent, SWT.WRAP);
				label.setText(description);
				label.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent event) {
						close();
					}
				});
				// Use the compact margins employed by PopupDialog.
				GridData gd = new GridData(GridData.BEGINNING
						| GridData.FILL_BOTH);
				gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
				gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
				label.setLayoutData(gd);
				return label;
			}
		}.open();
	}  
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
    	return true;
    }
}
