/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.layout.CellData;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

import com.ibm.icu.text.Collator;

/**
 * Implements a dialog showing all opened editors in the workbench
 * and the recent closed editors
 */
public class WorkbenchEditorsDialog extends SelectionDialog {

    private IWorkbenchWindow window;

    private Table editorsTable;

    private Button saveSelected;

    private Button closeSelected;

    private Button selectClean;

    private Button invertSelection;

    private Button allSelection;

    private boolean showAllPersp = false;

    private int sortColumn;

    private List elements = new ArrayList();

    private HashMap imageCache = new HashMap(11);

    private HashMap disabledImageCache = new HashMap(11);

    private boolean reverse = false;

    private Collator collator = Collator.getInstance();

    private Rectangle bounds;

    private int columnsWidth[];

    private static final String SORT = "sort"; //$NON-NLS-1$

    private static final String ALLPERSP = "allPersp"; //$NON-NLS-1$

    private static final String BOUNDS = "bounds"; //$NON-NLS-1$

    private static final String COLUMNS = "columns"; //$NON-NLS-1$

    private SelectionListener headerListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            int index = editorsTable.indexOf((TableColumn) e.widget);
            if (index == sortColumn) {
				reverse = !reverse;
			} else {
				sortColumn = index;
			}
            updateItems();
        }
    };

    /**
     * Constructor for WorkbenchEditorsDialog.
     * 
     * @param window the window
     */
    public WorkbenchEditorsDialog(IWorkbenchWindow window) {
        super(window.getShell());
        this.window = window;
        setTitle(WorkbenchMessages.WorkbenchEditorsDialog_title); 

        IDialogSettings s = getDialogSettings();
        if (s.get(ALLPERSP) == null) {
            sortColumn = 0;
        } else {
            showAllPersp = s.getBoolean(ALLPERSP);
            sortColumn = s.getInt(SORT);
            String[] array = s.getArray(BOUNDS);
            if (array != null) {
                bounds = new Rectangle(0, 0, 0, 0);
                bounds.x = new Integer(array[0]).intValue();
                bounds.y = new Integer(array[1]).intValue();
                bounds.width = new Integer(array[2]).intValue();
                bounds.height = new Integer(array[3]).intValue();
            }
            array = s.getArray(COLUMNS);
            if (array != null) {
                columnsWidth = new int[array.length];
                for (int i = 0; i < columnsWidth.length; i++) {
					columnsWidth[i] = new Integer(array[i]).intValue();
				}
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				IWorkbenchHelpContextIds.WORKBENCH_EDITORS_DIALOG);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // Typically we would use the parent's createButtonsForButtonBar.
        // However, we only want a Cancel button and not an OK button.  The
        // OK button will be used later (in createDialogArea) to activate
        // the selected editor.
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        Button button = getButton(IDialogConstants.CANCEL_ID);
        if (button != null) {
			button.setText(WorkbenchMessages.WorkbenchEditorsDialog_close);
		}

    }

    /**
     * Initialize the dialog bounds with the bounds saved
     * from the settings.
     */
    protected void initializeBounds() {
        if (bounds != null) {
            getShell().setBounds(bounds);
        } else {
            super.initializeBounds();
        }
    }

    /**
     * Creates the contents of this dialog, initializes the
     * listener and the update thread.
     */
    protected Control createDialogArea(Composite parent) {

        initializeDialogUnits(parent);

        Font font = parent.getFont();

        Composite dialogArea = new Composite(parent, SWT.NONE);
        CellLayout dialogAreaLayout = new CellLayout(1)
                .setMargins(
                        convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN),
                        convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN))
                .setSpacing(
                        convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
                        convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING))
                .setRow(1, Row.growing());
        dialogArea.setLayout(dialogAreaLayout);
        dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        //Label over the table
        Label l = new Label(dialogArea, SWT.NONE);
        l.setText(WorkbenchMessages.WorkbenchEditorsDialog_label); 
        l.setFont(font);
        l.setLayoutData(new CellData().align(SWT.FILL, SWT.CENTER));
        //Table showing the editors name, full path and perspective
        editorsTable = new Table(dialogArea, SWT.MULTI | SWT.BORDER
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        editorsTable.setLinesVisible(true);
        editorsTable.setHeaderVisible(true);
        editorsTable.setFont(font);

        final int height = 16 * editorsTable.getItemHeight();
        final int width = (int) (2.5 * height);

        CellData tableData = new CellData().align(SWT.FILL, SWT.FILL).setHint(
                CellData.OVERRIDE, width, height);

        editorsTable.setLayoutData(tableData);
        editorsTable.setLayout(new Layout() {
            protected Point computeSize(Composite composite, int wHint,
                    int hHint, boolean flushCache) {
                return new Point(width, height);
            }

            protected void layout(Composite composite, boolean flushCache) {
                TableColumn c[] = editorsTable.getColumns();
                if (columnsWidth == null) {
                    int w = editorsTable.getClientArea().width;
                    c[0].setWidth(w * 1 / 3);
                    c[1].setWidth(w - c[0].getWidth());
                } else {
                    c[0].setWidth(columnsWidth[0]);
                    c[1].setWidth(columnsWidth[1]);
                }
                editorsTable.setLayout(null);
            }
        });
        //Name column
        TableColumn tc = new TableColumn(editorsTable, SWT.NONE);
        tc.setResizable(true);
        tc.setText(WorkbenchMessages.WorkbenchEditorsDialog_name);
        tc.addSelectionListener(headerListener);
        //Full path column
        tc = new TableColumn(editorsTable, SWT.NONE);
        tc.setResizable(true);
        tc.setText(WorkbenchMessages.WorkbenchEditorsDialog_path); 
        tc.addSelectionListener(headerListener);

        // A composite for selection option buttons
        Composite selectionButtons = new Composite(dialogArea, SWT.NULL);
        Label compLabel = new Label(selectionButtons, SWT.NULL);
        compLabel.setFont(font);
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        selectionButtons.setLayout(layout);

        //Select clean editors button
        selectClean = new Button(selectionButtons, SWT.PUSH);
        selectClean.setText(WorkbenchMessages.WorkbenchEditorsDialog_selectClean); 
        selectClean.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editorsTable.setSelection(selectClean(editorsTable.getItems()));
                updateButtons();
            }
        });
        selectClean.setFont(font);
        setButtonLayoutData(selectClean);

        //Invert selection button
        invertSelection = new Button(selectionButtons, SWT.PUSH);
        invertSelection.setText(WorkbenchMessages.WorkbenchEditorsDialog_invertSelection); 
        invertSelection.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editorsTable.setSelection(invertedSelection(editorsTable
                        .getItems(), editorsTable.getSelection()));
                updateButtons();
            }
        });
        invertSelection.setFont(font);
        setButtonLayoutData(invertSelection);

        //Select all button
        allSelection = new Button(selectionButtons, SWT.PUSH);
        allSelection.setText(WorkbenchMessages.WorkbenchEditorsDialog_allSelection); 
        allSelection.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editorsTable.setSelection(editorsTable.getItems());
                updateButtons();
            }
        });
        allSelection.setFont(font);
        setButtonLayoutData(allSelection);

        // A composite for selected editor action buttons
        Composite actionButtons = new Composite(dialogArea, SWT.NULL);
        Label actLabel = new Label(actionButtons, SWT.NULL);
        actLabel.setFont(font);
        GridLayout actLayout = new GridLayout();
        actLayout.numColumns = 4;
        actionButtons.setLayout(actLayout);

        // Activate selected editor button
        createButton(actionButtons, IDialogConstants.OK_ID, WorkbenchMessages.WorkbenchEditorsDialog_activate, 
                true);

        //Close selected editors button
        closeSelected = new Button(actionButtons, SWT.PUSH);
        closeSelected.setText(WorkbenchMessages.WorkbenchEditorsDialog_closeSelected);
        closeSelected.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                closeItems(editorsTable.getSelection());
            }
        });
        closeSelected.setFont(font);
        setButtonLayoutData(closeSelected);

        //Save selected editors button
        saveSelected = new Button(actionButtons, SWT.PUSH);
        saveSelected.setText(WorkbenchMessages.WorkbenchEditorsDialog_saveSelected); 
        saveSelected.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                saveItems(editorsTable.getSelection());
            }
        });
        saveSelected.setFont(font);
        setButtonLayoutData(saveSelected);

        //Show only active perspective button
        final Button showAllPerspButton = new Button(dialogArea, SWT.CHECK);
        showAllPerspButton.setText(WorkbenchMessages.WorkbenchEditorsDialog_showAllPersp);
        showAllPerspButton.setSelection(showAllPersp);
        showAllPerspButton.setFont(font);
        setButtonLayoutData(showAllPerspButton);
        showAllPerspButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showAllPersp = showAllPerspButton.getSelection();
                updateItems();
            }
        });
        //Create the items and update buttons state
        updateItems();
        updateButtons();

        editorsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                okPressed();
            }
        });
        editorsTable.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                for (Iterator images = imageCache.values().iterator(); images
                        .hasNext();) {
                    Image i = (Image) images.next();
                    i.dispose();
                }
                for (Iterator images = disabledImageCache.values().iterator(); images
                        .hasNext();) {
                    Image i = (Image) images.next();
                    i.dispose();
                }
            }
        });
        editorsTable.setFocus();
        applyDialogFont(dialogArea);
        return dialogArea;
    }

    /**
     * Updates the button state (enabled/disabled)
     */
    private void updateButtons() {
        TableItem selectedItems[] = editorsTable.getSelection();
        boolean hasDirty = false;
        for (int i = 0; i < selectedItems.length; i++) {
            Adapter editor = (Adapter) selectedItems[i].getData();
            if (editor.isDirty()) {
                hasDirty = true;
                break;
            }
        }
        saveSelected.setEnabled(hasDirty);

        TableItem allItems[] = editorsTable.getItems();
        boolean hasClean = false;
        for (int i = 0; i < allItems.length; i++) {
            Adapter editor = (Adapter) allItems[i].getData();
            if (!editor.isDirty()) {
                hasClean = true;
                break;
            }
        }
        selectClean.setEnabled(hasClean);
        invertSelection.setEnabled(allItems.length > 0);
        closeSelected.setEnabled(selectedItems.length > 0);

        Button ok = getOkButton();
        if (ok != null) {
			ok.setEnabled(selectedItems.length == 1);
		}
    }

    /**
     * Closes the specified editors
     */
    private void closeItems(TableItem items[]) {
        if (items.length == 0) {
			return;
		}
        for (int i = 0; i < items.length; i++) {
            Adapter e = (Adapter) items[i].getData();
            e.close();
        }
        updateItems();
    }

    /**
     * Saves the specified editors
     */
    private void saveItems(TableItem items[]) {
        if (items.length == 0) {
			return;
		}
        ProgressMonitorDialog pmd = new ProgressMonitorJobsDialog(getShell());
        pmd.open();
        for (int i = 0; i < items.length; i++) {
            Adapter editor = (Adapter) items[i].getData();
            editor.save(pmd.getProgressMonitor());
            updateItem(items[i], editor);
        }
        pmd.close();
        updateItems();
    }

    /**
     * Returns all clean editors from items[];
     */
    private TableItem[] selectClean(TableItem items[]) {
        if (items.length == 0) {
			return new TableItem[0];
		}
        ArrayList cleanItems = new ArrayList(items.length);
        for (int i = 0; i < items.length; i++) {
            Adapter editor = (Adapter) items[i].getData();
            if (!editor.isDirty()) {
				cleanItems.add(items[i]);
			}
        }
        TableItem result[] = new TableItem[cleanItems.size()];
        cleanItems.toArray(result);
        return result;
    }

    /**
     * Returns all clean editors from items[];
     */
    private TableItem[] invertedSelection(TableItem allItems[],
            TableItem selectedItems[]) {
        if (allItems.length == 0) {
			return allItems;
		}
        ArrayList invertedSelection = new ArrayList(allItems.length
                - selectedItems.length);
        outerLoop: for (int i = 0; i < allItems.length; i++) {
            for (int j = 0; j < selectedItems.length; j++) {
                if (allItems[i] == selectedItems[j]) {
					continue outerLoop;
				}
            }
            invertedSelection.add(allItems[i]);
        }
        TableItem result[] = new TableItem[invertedSelection.size()];
        invertedSelection.toArray(result);
        return result;
    }

    /**
     * Updates the specified item
     */
    private void updateItem(TableItem item, Adapter editor) {
        item.setData(editor);
        item.setText(editor.getText());
        Image images[] = editor.getImage();
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null) {
				item.setImage(i, images[i]);
			}
        }
    }

    /**
     * Adds all editors to elements
     */
    private void updateEditors(IWorkbenchPage[] pages) {
        for (int j = 0; j < pages.length; j++) {
            IEditorReference editors[] = pages[j].getEditorReferences();
            for (int k = 0; k < editors.length; k++) {
                elements.add(new Adapter(editors[k]));
            }
        }
    }

    /**
     * Updates all items in the table
     */
    private void updateItems() {
        editorsTable.removeAll();
        elements = new ArrayList();
        if (showAllPersp) {
            IWorkbenchWindow windows[] = window.getWorkbench()
                    .getWorkbenchWindows();
            for (int i = 0; i < windows.length; i++) {
				updateEditors(windows[i].getPages());
			}
        } else {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                updateEditors(new IWorkbenchPage[] { page });
            }
        }
        sort();
        Object selection = null;
        if (window.getActivePage() != null) {
			selection = window.getActivePage().getActiveEditor();
		}
        for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
            Adapter e = (Adapter) iterator.next();
            TableItem item = new TableItem(editorsTable, SWT.NULL);
            updateItem(item, e);
            if ((selection != null) && (selection == e.editorRef)) {
				editorsTable.setSelection(new TableItem[] { item });
			}
        }
        // update the buttons, because the selection may have changed
        updateButtons();
    }

    /**
     * Sorts all the editors according to the table header
     */
    private void sort() {
        //Backward compatible. Table used to have 3 columns.
        if (sortColumn > (editorsTable.getColumnCount() - 1)) {
			sortColumn = 0;
		}
        Adapter a[] = new Adapter[elements.size()];
        elements.toArray(a);
        Arrays.sort(a);
        elements = Arrays.asList(a);
    }

    /**
     * The user has selected a resource and the dialog is closing.
     */
    protected void okPressed() {
        TableItem items[] = editorsTable.getSelection();
        if (items.length != 1) {
            super.okPressed();
            return;
        }

        saveDialogSettings();

        Adapter selection = (Adapter) items[0].getData();
        //It would be better to activate before closing the
        //dialog but it does not work when the editor is in other
        //window. Must investigate.
        super.okPressed();
        selection.activate();
    }

    /**
     * Saves the dialog settings.
     */
    private void saveDialogSettings() {
        IDialogSettings s = getDialogSettings();
        s.put(ALLPERSP, showAllPersp);
        s.put(SORT, sortColumn);
        bounds = getShell().getBounds();
        String array[] = new String[4];
        array[0] = String.valueOf(bounds.x);
        array[1] = String.valueOf(bounds.y);
        array[2] = String.valueOf(bounds.width);
        array[3] = String.valueOf(bounds.height);
        s.put(BOUNDS, array);
        array = new String[editorsTable.getColumnCount()];
        for (int i = 0; i < array.length; i++) {
			array[i] = String.valueOf(editorsTable.getColumn(i).getWidth());
		}
        s.put(COLUMNS, array);
    }

    /**
     * Return a dialog setting section for this dialog
     */
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = WorkbenchPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings thisSettings = settings
                .getSection(getClass().getName());
        if (thisSettings == null) {
			thisSettings = settings.addNewSection(getClass().getName());
		}
        return thisSettings;
    }

    /**
     * A helper inner class to adapt EditorHistoryItem and IEditorPart
     * in the same type.
     */
    private class Adapter implements Comparable {
        IEditorReference editorRef;

        IEditorInput input;

        IEditorDescriptor desc;

        String text[];

        Image images[];

        Adapter(IEditorReference ref) {
            editorRef = ref;
        }

        Adapter(IEditorInput input, IEditorDescriptor desc) {
            this.input = input;
            this.desc = desc;
        }

        boolean isDirty() {
            if (editorRef == null) {
				return false;
			}
            return editorRef.isDirty();
        }

        boolean isOpened() {
            return editorRef != null;
        }

        void close() {
            if (editorRef == null) {
				return;
			}
            WorkbenchPage p = ((WorkbenchPartReference) editorRef).getPane()
                    .getPage();
            p.closeEditor(editorRef, true);
        }

        void save(IProgressMonitor monitor) {
            if (editorRef == null) {
				return;
			}
            IEditorPart editor = (IEditorPart) editorRef.getPart(true);
            if (editor != null) {
				editor.doSave(monitor);
			}
        }

        String[] getText() {
            if (text != null) {
				return text;
			}
            text = new String[2];
            if (editorRef != null) {
                if (editorRef.isDirty()) {
					text[0] = "*" + editorRef.getTitle(); //$NON-NLS-1$
				} else {
					text[0] = editorRef.getTitle();
				}
                text[1] = editorRef.getTitleToolTip();
            } else {
                text[0] = input.getName();
                text[1] = input.getToolTipText();
            }
            return text;
        }

        Image[] getImage() {
            if (images != null) {
				return images;
			}
            images = new Image[2];
            if (editorRef != null) {
                images[0] = editorRef.getTitleImage();
                WorkbenchPage p = ((WorkbenchPartReference) editorRef)
                        .getPane().getPage();
                IPerspectiveDescriptor persp = p.getPerspective();
                ImageDescriptor image = persp.getImageDescriptor();
                if (image == null) {
					image = WorkbenchImages
                            .getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
				}
            } else {
                ImageDescriptor image = null;
                if (desc != null) {
					image = desc.getImageDescriptor();
				}
                if (image == null) {
                    IEditorRegistry registry = WorkbenchPlugin.getDefault()
                            .getEditorRegistry();
                    image = registry.getImageDescriptor(input.getName());
					//TODO: how can this honour content types?  Guessing at the content type perhaps?
					
                    if (image == null) {
                        // @issue what should be the default image?
                        // image = registry.getDefaultEditor().getImageDescriptor();
                    }
                }
                if (image != null) {
                    images[0] = (Image) disabledImageCache.get(image);
                    if (images[0] == null) {
                        Image enabled = image.createImage();
                        Image disabled = new Image(editorsTable.getDisplay(),
                                enabled, SWT.IMAGE_DISABLE);
                        enabled.dispose();
                        disabledImageCache.put(image, disabled);
                        images[0] = disabled;
                    }
                }
            }
            return images;
        }

        private void activate() {
            if (editorRef != null) {
                IEditorPart editor = editorRef.getEditor(true);
                WorkbenchPage p = (WorkbenchPage) editor.getEditorSite()
                        .getPage();
                Shell s = p.getWorkbenchWindow().getShell();
                if (s.getMinimized()) {
					s.setMinimized(false);
				}
                s.moveAbove(null);
                p.getWorkbenchWindow().setActivePage(p);
                p.activate(editor);
            } else {
                IWorkbenchPage p = window.getActivePage();
                if (p != null) {
                    try {
                        p.openEditor(input, desc.getId(), true);
                    } catch (PartInitException e) {
                    }
                }
            }
        }

        public int compareTo(Object another) {
            Adapter adapter = (Adapter) another;
            int result = collator.compare(getText()[sortColumn], adapter
                    .getText()[sortColumn]);
            if (result == 0) {
                int column = sortColumn == 0 ? 1 : 0;
                result = collator.compare(getText()[column],
                        adapter.getText()[column]);
            }
            if (reverse) {
				return result * -1;
			}
            return result;
        }
    }
}
