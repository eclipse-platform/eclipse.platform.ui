/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Dialog which allows user to modify all settings of an org.eclipse.ui.views.MarkerFilter object.
 * Not intended to be subclassed or instantiated by clients.
 */
public abstract class DialogMarkerFilter extends Dialog {
    /**
     * button IDs
     */
    static final int RESET_ID = IDialogConstants.CLIENT_ID;

    static final int SELECT_WORKING_SET_ID = IDialogConstants.CLIENT_ID + 1;

    static final int SELECT_ALL_ID = IDialogConstants.CLIENT_ID + 2;

    static final int DESELECT_ALL_ID = IDialogConstants.CLIENT_ID + 3;

    private class TypesLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof MarkerType)) {
                return ""; //$NON-NLS-1$
            }
            MarkerType type = (MarkerType) element;
            if (columnIndex == 0) {
                return type.getLabel();
            }
            if (columnIndex == 1) {
                String superTypes = ""; //$NON-NLS-1$
                MarkerType[] supers = type.getSupertypes();
                for (int i = 0; i < supers.length; i++) {
                    superTypes += supers[i].getLabel();
                    if (i < supers.length - 1) {
                        superTypes += "; "; //$NON-NLS-1$
                    }
                }
                return superTypes;
            }
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Creates and manages a group of widgets for selecting a working 
     * set marker filter.
     */
    private class WorkingSetGroup {
        private Button button;

        private Button selectButton;

        /**
         * Creates the working set filter selection widgets.
         * 
         * @param parent the parent composite of the working set widgets
         */
        WorkingSetGroup(Composite parent) {
            // radio button has to be part of main radio button group
            button = createRadioButton(parent, Messages
                    .getString("filtersDialog.noWorkingSet")); //$NON-NLS-1$
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            button.setLayoutData(data);

            Composite composite = new Composite(parent, SWT.NONE);
            composite.setFont(parent.getFont());
            GridLayout layout = new GridLayout();
            Button radio = new Button(parent, SWT.RADIO);
            layout.marginWidth = radio.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            layout.marginHeight = 0;
            radio.dispose();
            composite.setLayout(layout);
            selectButton = createButton(composite, SELECT_WORKING_SET_ID,
                    Messages.getString("filtersDialog.workingSetSelect"), false); //$NON-NLS-1$
        }

        /**
         * Returns wether or not a working set filter should be used
         * 
         * @return 
         * 	true=a working set filter should be used
         * 	false=a working set filter should not be used
         */
        boolean getSelection() {
            return button.getSelection();
        }

        /**
         * Returns the selected working set filter or null if none 
         * is selected.
         * 
         * @return the selected working set filter or null if none 
         * 	is selected.
         */
        IWorkingSet getWorkingSet() {
            return (IWorkingSet) button.getData();
        }

        /**
         * Sets the working set filter selection.
         * 
         * @param selected true=a working set filter should be used
         * 	false=no working set filter should be used
         */
        void setSelection(boolean selected) {
            button.setSelection(selected);
            if (selected) {
                anyResourceButton.setSelection(false);
                anyResourceInSameProjectButton.setSelection(false);
                selectedResourceButton.setSelection(false);
                selectedResourceAndChildrenButton.setSelection(false);
            }
        }

        /**
         * Opens the working set selection dialog.
         */
        void selectPressed() {
            IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench()
                    .getWorkingSetManager().createWorkingSetSelectionDialog(
                            getShell(), false);
            IWorkingSet workingSet = getWorkingSet();

            if (workingSet != null) {
                dialog.setSelection(new IWorkingSet[] { workingSet });
            }
            if (dialog.open() == Window.OK) {
                markDirty();
                IWorkingSet[] result = dialog.getSelection();
                if (result != null && result.length > 0) {
                    setWorkingSet(result[0]);
                } else {
                    setWorkingSet(null);
                }
                if (getSelection() == false) {
                    setSelection(true);
                }
            }
        }

        /**
         * Sets the specified working set.
         * 
         * @param workingSet the working set 
         */
        void setWorkingSet(IWorkingSet workingSet) {
            button.setData(workingSet);
            if (workingSet != null) {
                button.setText(Messages.format("filtersDialog.workingSet", //$NON-NLS-1$
                        new Object[] { workingSet.getName() }));
            } else {
                button
                        .setText(Messages
                                .getString("filtersDialog.noWorkingSet")); //$NON-NLS-1$
            }
        }

        void setEnabled(boolean enabled) {
            button.setEnabled(enabled);
            selectButton.setEnabled(enabled);
        }
    }

    private MarkerFilter[] filters;

    private CheckboxTableViewer typesViewer;

    private Button filterEnabledButton;

    private Button filterOnMarkerLimit;

    private Button anyResourceButton;

    private Button anyResourceInSameProjectButton;

    private Button selectedResourceButton;

    private Button selectedResourceAndChildrenButton;

    private Button selectAllButton;

    private Button deselectAllButton;

    private WorkingSetGroup workingSetGroup;

    private Text markerLimit;

    private boolean dirty = false;

    protected SelectionListener selectionListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            DialogMarkerFilter.this.widgetSelected(e);
        }
    };

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {
        public void checkStateChanged(CheckStateChangedEvent event) {
            DialogMarkerFilter.this.checkStateChanged(event);
        }
    };

	private ListViewer filtersList;

	private Composite selectedComposite;

	private MarkerFilter[] selectedFilters;

    /**
     * Create a new instance of the receiver.
     * @param parentShell
     * @param filtersList
     */
    DialogMarkerFilter(Shell parentShell, MarkerFilter[] filtersList) {
        super(parentShell);
        this.filters = filtersList;
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == RESET_ID) {
            resetPressed();
            markDirty();
        } else if (buttonId == SELECT_WORKING_SET_ID) {
            workingSetGroup.selectPressed();
        } else if (buttonId == SELECT_ALL_ID) {
            typesViewer.setAllChecked(true);
        } else if (buttonId == DESELECT_ALL_ID) {
            typesViewer.setAllChecked(false);
        } else {
            super.buttonPressed(buttonId);
        }
    }

    /**
     * This method is called when a button is checked or unchecked. It updates the enablement
     * state of all widgets and marks the dialog as dirty.
     * @param event
     */
    void checkStateChanged(CheckStateChangedEvent event) {
        updateEnabledState();
        markDirty();
    }

    /**
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("filtersDialog.title")); //$NON-NLS-1$
    }

    protected void createResetArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        Button reset = new Button(composite, SWT.PUSH);
        reset.setText(Messages.getString("restoreDefaults.text")); //$NON-NLS-1$
        reset.setData(new Integer(RESET_ID));

        reset.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });

        reset.setFont(composite.getFont());
        setButtonLayoutData(reset);
    }

    /**
     * Creates a check box button with the given parent and text.
     *
     * @param parent the parent composite
     * @param text the text for the check box
     * @param grabRow <code>true</code>to grab the remaining horizontal space, <code>false</code> otherwise
     * @return the check box button
     */
    protected Button createCheckbox(Composite parent, String text,
            boolean grabRow) {
        Button button = new Button(parent, SWT.CHECK);
        if (grabRow) {
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            button.setLayoutData(gridData);
        }
        button.setText(text);
        button.addSelectionListener(selectionListener);
        button.setFont(parent.getFont());
        return button;
    }

    /**
     * Creates a combo box with the given parent, items, and selection
     *
     * @param parent the parent composite
     * @param items the items for the combo box
     * @param selectionIndex the index of the item to select
     * @return the combo box
     */
    protected Combo createCombo(Composite parent, String[] items,
            int selectionIndex) {
        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        combo.setFont(parent.getFont());
        combo.setItems(items);
        combo.select(selectionIndex);
        combo.addSelectionListener(selectionListener);
        return combo;
    }

    /**
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        
        dialogArea.setLayout(new GridLayout(2,false));
        
        createFiltersArea(dialogArea);
        
        createSelectedFilterArea(dialogArea);

        updateUIFromFilter();
        
        filtersList.setSelection(new StructuredSelection(filters[0]));

        return dialogArea;
    }
    
    /**
     * Create the list in the receiver.
     * @param dialogArea
     */
    /**
     * @param dialogArea
     */
    private void createFiltersArea(Composite dialogArea) {
    	
    	Composite listArea = new Composite(dialogArea,SWT.NONE);
    	listArea.setLayoutData(
				new GridData(SWT.FILL,SWT.FILL,false,true));
    	listArea.setLayout(new GridLayout());
    	
    	Label title = new Label(listArea,SWT.NONE);
    	title.setText(Messages.getString("MarkerFilter.filtersTitle"));//$NON-NLS-1$
		filtersList = new ListViewer(listArea);
		filtersList.setContentProvider(new IStructuredContentProvider(){
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return filters;
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				//Do nothing
			}
			
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				//Do nothing
			}
		});
		
		filtersList.setLabelProvider(new LabelProvider(){
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return((MarkerFilter) element).getName();
			}
		});
		
		selectedFilters = new MarkerFilter[]{filters[0]};
		filtersList.setSelection(new StructuredSelection(selectedFilters));
		
		filtersList.addSelectionChangedListener(new ISelectionChangedListener(){
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				updateFilterFromUI(getSelectedFilter());
				setSelectedFilter(event);
				
			}
		});
		
		filtersList.setInput(this);
		
		filtersList.getControl().setLayoutData(
				new GridData(SWT.FILL,SWT.FILL,false,true));
		
		Composite buttons = new Composite(listArea,SWT.NONE);
		buttons.setLayout(new GridLayout(2,false));
		
		
		Button addNew = new Button(buttons,SWT.PUSH);
		addNew.setText(Messages.getString("MarkerFilter.addFilterName"));//$NON-NLS-1$)
		addNew.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				InputDialog newDialog =
					new InputDialog(
							getShell(),
							Messages.getString("MarkerFilterDialog.title"),//$NON-NLS-1$
							Messages.getString("MarkerFilterDialog.message"),//$NON-NLS-1$
							Messages.getString("MarkerFilter.defaultFilterName"),//$NON-NLS-1$
							new IInputValidator(){
								/* (non-Javadoc)
								 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
								 */
								public String isValid(String newText) {
									if (newText.length() == 0)
										return Messages.getString("MarkerFilterDialog.emptyMessage");//$NON-NLS-1$
									return null;
								}
							}
				);
				newDialog.open();
				String newName = newDialog.getValue();
				if(newName != null){
					createNewFilter(newName);					
				}
			}
		});
		
		Button remove = new Button(buttons,SWT.PUSH);
		remove.setText(Messages.getString("MarkerFilter.deleteSelectedName"));//$NON-NLS-1$)
		remove.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				removeFilters(filtersList.getSelection());
			}
		});
	}

    /**
     * Set the selected filter from event.
     * @param event
     */
    protected void setSelectedFilter(SelectionChangedEvent event) {
		
		ISelection selection = event.getSelection();
		if(selection instanceof IStructuredSelection){
			Collection list =  ((IStructuredSelection)selection).toList();
			MarkerFilter[] selected = new MarkerFilter[list.size()];
			list.toArray(selected);
			selectedFilters =  selected;
			
		}
		else
			selectedFilters = new MarkerFilter[0];
		updateUIFromFilter();
		
	}

	/**
     * Remove the filters in selection.
     * @param selection
     */
    protected void removeFilters(ISelection selection){
    	if(selection instanceof IStructuredSelection){
    		List toRemove = ((IStructuredSelection) selection).toList();
    		MarkerFilter[] newFilters = new MarkerFilter[filters.length - toRemove.size()];
    		int index = 0;
    		for (int i = 0; i < filters.length; i++) {
				if(toRemove.contains(filters[i]))
					continue;
				newFilters[index] = filters[i];
				index ++;
			}
    		
    		filters = newFilters;
    		filtersList.refresh();
    	}
    }

	/**
     * Create a new filter called newName.
     * @param newName
     */
	private void createNewFilter(String newName){
		MarkerFilter[] newFilters = new MarkerFilter[filters.length + 1];
		System.arraycopy(filters,0,newFilters,0,filters.length);
		MarkerFilter filter = newFilter(newName);
		filter.resetState();
		newFilters[filters.length] = filter;
		filters = newFilters;
		filtersList.refresh();
	}

	/**
	 * Crate a newFilter called newName
	 * @param newName
	 * @return MarkerFilter
	 */
	protected abstract MarkerFilter newFilter(String newName);

	/**
     * Create the area for the selected filter.
     * @param composite
     */
    private void createSelectedFilterArea(Composite composite){
    	
    	selectedComposite = new Composite(composite,SWT.NONE);
    	selectedComposite.setLayout(new GridLayout());
    	selectedComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
    	
    	createOnOffArea(selectedComposite);
        createMarkerLimitArea(selectedComposite);
        createTypesArea(selectedComposite);
        createResourceArea(selectedComposite);
        createAttributesArea(selectedComposite);
        createResetArea(selectedComposite);
        createSeparatorLine(selectedComposite);
    	
    }

    /**
     * Creates a separator line above the OK/Cancel buttons bar
     * 
     * @param parent the parent composite
     */
    protected void createSeparatorLine(Composite parent) {
        // Build the separator line
        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        separator.setLayoutData(gd);
    }

    /**
     * Creates a radio button with the given parent and text.
     *
     * @param parent the parent composite
     * @param text the text for the check box
     * @return the radio box button
     */
    protected Button createRadioButton(Composite parent, String text) {
        Button button = new Button(parent, SWT.RADIO);
        button.setText(text);
        button.setFont(parent.getFont());
        button.addSelectionListener(selectionListener);
        return button;
    }

    /**
     * Creates the area showing which resources should be considered.
     *
     * @param parent the parent composite
     */
    protected void createResourceArea(Composite parent) {
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout());
        group.setFont(parent.getFont());
        anyResourceButton = createRadioButton(group, Messages
                .getString("filtersDialog.anyResource")); //$NON-NLS-1$
        anyResourceInSameProjectButton = createRadioButton(group, Messages
                .getString("filtersDialog.anyResourceInSameProject")); //$NON-NLS-1$ // added by cagatayk@acm.org
        selectedResourceButton = createRadioButton(group, Messages
                .getString("filtersDialog.selectedResource")); //$NON-NLS-1$
        selectedResourceAndChildrenButton = createRadioButton(group, Messages.getString("filtersDialog.selectedAndChildren")); //$NON-NLS-1$
        workingSetGroup = new WorkingSetGroup(group);
    }

    /**
     * Creates the area showing which marker types should be included.
     *
     * @param parent the parent composite
     */
    protected void createTypesArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("filtersDialog.showItemsOfType")); //$NON-NLS-1$
        label.setFont(font);

        Table table = new Table(composite, SWT.CHECK | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        tableLayout.addColumnData(new ColumnWeightData(40, true));
        TableColumn tc = new TableColumn(table, SWT.NONE, 0);
        tc.setText(Messages.getString("filtersDialog.type.columnHeader")); //$NON-NLS-1$
        tableLayout.addColumnData(new ColumnWeightData(60, true));
        tc = new TableColumn(table, SWT.NONE, 1);
        tc.setText(Messages.getString("filtersDialog.superTypecolumnHeader")); //$NON-NLS-1$
        typesViewer = new CheckboxTableViewer(table);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 105;
        gridData.widthHint = 350;
        typesViewer.getTable().setFont(font);
        typesViewer.getControl().setLayoutData(gridData);
        typesViewer.setContentProvider(getContentProvider());
        typesViewer.setLabelProvider(getLabelProvider());
        typesViewer.setSorter(getSorter());
        typesViewer.addCheckStateListener(checkStateListener);
        typesViewer.setInput(getSelectedFilter().getRootTypes().toArray());

        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout buttonLayout = new GridLayout();
        buttonLayout.marginWidth = 0;
        buttonComposite.setLayout(buttonLayout);
        selectAllButton = createButton(buttonComposite, SELECT_ALL_ID,Messages.getString("filtersDialog.selectAll"), //$NON-NLS-1$ 
                false);
        deselectAllButton = createButton(buttonComposite, DESELECT_ALL_ID,
               Messages.getString("filtersDialog.deselectAll"), //$NON-NLS-1$ 
                false);
    }

    
    /**
     * Get the currently selected marker filter if there is only one
     * selection.
     * @return MarkerFilter or <code>null</code>.
     */
    protected MarkerFilter getSelectedFilter() {
    	
    	if(selectedFilters.length == 1)
    		return selectedFilters[0];
    	return null;
	}

	private IStructuredContentProvider getContentProvider() {
        return new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
            	MarkerFilter selected = getSelectedFilter();
            	if(selected == null)
            		return new Object[0];
            	
                List roots = selected.getRootTypes();
                List elements = new ArrayList();
                for (int i = 0; i < roots.size(); i++) {
                    Object obj = roots.get(i);
                    if (obj instanceof MarkerType) {
                        elements.add(obj);
                        MarkerType[] subTypes = ((MarkerType) obj)
                                .getAllSubTypes();
                        for (int j = 0; j < subTypes.length; j++) {
                            MarkerType subType = subTypes[j];
                            if (!elements.contains(subType)) {
                                elements.add(subType);
                            }
                        }
                    }
                }
                return elements.toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        };
    }

    /**
     * Creates the filter enablement area.
     * 
     * @param parent the parent composite
     */
    protected void createOnOffArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);
        composite.setLayout(new GridLayout());
        filterEnabledButton = createCheckbox(composite, Messages.getString("filtersDialog.onOff"), //$NON-NLS-1$
                false);
        filterEnabledButton.setFont(composite.getFont());
        filterEnabledButton.setLayoutData(new GridData());
        filterEnabledButton.addSelectionListener(selectionListener);
    }

    /**
     * Creates the area where the user can specify a maximum number of items
     * to display in the table.
     * 
     * @param parent the parent composite
     */
    protected void createMarkerLimitArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        filterOnMarkerLimit = createCheckbox(composite, Messages.getString("filtersDialog.limitVisibleMarkersTo"), //$NON-NLS-1$
                false);
        filterOnMarkerLimit.setFont(composite.getFont());
        filterOnMarkerLimit.setLayoutData(new GridData());
        filterOnMarkerLimit.addSelectionListener(selectionListener);
        markerLimit = new Text(composite, SWT.SINGLE | SWT.BORDER);
        markerLimit.setTextLimit(6);
        GridData gridData = new GridData();
        gridData.widthHint = convertWidthInCharsToPixels(10);
        markerLimit.setLayoutData(gridData);
        markerLimit.setFont(font);
        markerLimit.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                markDirty();
            }
        });
    }

    /**
     * This method is intended to be overridden by subclasses of FiltersDialog. The
     * attributes area will be created just above the Restore Defaults button.
     * 
     * @param parent the parent Composite
     */
    protected void createAttributesArea(Composite parent) {
    }

    private ILabelProvider getLabelProvider() {
        return new TypesLabelProvider();
    }

    /**
     * Returns the selected marker types.
     *
     * @return the selected marker types
     */
    protected List getSelectedTypes() {
        return Arrays.asList(typesViewer.getCheckedElements());
    }

    protected ViewerSorter getSorter() {
        return new ViewerSorter() {
            public int compare(Viewer viewer, Object e1, Object e2) {
                MarkerType t1 = (MarkerType) e1;
                MarkerType t2 = (MarkerType) e2;
                return collator.compare(t1.getLabel(), t2.getLabel());
            }
        };
    }

    /**
     * Updates the filter from the UI state.
     * Must be done here rather than by extending open()
     * because after super.open() is called, the widgetry is disposed.
     */
    protected void okPressed() {
        try {
            int markerLimit = Integer.parseInt(this.markerLimit.getText());

            if (markerLimit < 1) {
                throw new NumberFormatException();
            }

            updateFilterFromUI();
            super.okPressed();
        } catch (NumberFormatException eNumberFormat) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.OK
                    | SWT.APPLICATION_MODAL | SWT.ICON_ERROR);
            messageBox.setText(Messages
                    .getString("filtersDialog.titleMarkerLimitInvalid")); //$NON-NLS-1$
            messageBox.setMessage(Messages
                    .getString("filtersDialog.messageMarkerLimitInvalid")); //$NON-NLS-1$
            messageBox.open();

            if (markerLimit.forceFocus()) {
                markerLimit.setSelection(0, markerLimit.getCharCount());
                markerLimit.showSelection();
            }
        }
    }

    /**
     * Handles a press of the Reset button.
     * Updates the UI state to correspond to a reset filter,
     * but doesn't actually reset our filter.
     */
    protected void resetPressed() {
        filterEnabledButton
                .setSelection(MarkerFilter.DEFAULT_ACTIVATION_STATUS);
        filterOnMarkerLimit
                .setSelection(MarkerFilter.DEFAULT_FILTER_ON_MARKER_LIMIT);
        markerLimit.setText(String.valueOf(MarkerFilter.DEFAULT_MARKER_LIMIT));
        typesViewer.setAllChecked(true);
        int onResource = MarkerFilter.DEFAULT_ON_RESOURCE;
        anyResourceButton
                .setSelection(onResource == MarkerFilter.ON_ANY_RESOURCE);
        anyResourceInSameProjectButton
                .setSelection(onResource == MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        selectedResourceButton
                .setSelection(onResource == MarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        selectedResourceAndChildrenButton
                .setSelection(onResource == MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        workingSetGroup.setSelection(onResource == MarkerFilter.ON_WORKING_SET);
        updateEnabledState();
    }

    /**
     * Sets the selected marker types.
     *
     * @param markerTypes
     */
    void setSelectedTypes(List markerTypes) {
        typesViewer.setCheckedElements(new Object[0]);
        for (int i = 0; i < markerTypes.size(); i++) {
            Object obj = markerTypes.get(i);
            if (obj instanceof MarkerType) {
                typesViewer.setChecked(obj, true);
            }
        }
    }

    /**
     * Updates the enabled state of the widgetry.
     */
    protected void updateEnabledState() {
        filterOnMarkerLimit.setEnabled(isFilterEnabled());
        markerLimit.setEnabled(isFilterEnabled()
                && filterOnMarkerLimit.getSelection());

        typesViewer.getTable().setEnabled(isFilterEnabled());
        selectAllButton.setEnabled(isFilterEnabled()
                && typesViewer.getTable().getItemCount() > 0);
        deselectAllButton.setEnabled(isFilterEnabled()
                && typesViewer.getTable().getItemCount() > 0);

        anyResourceButton.setEnabled(isFilterEnabled());
        anyResourceInSameProjectButton.setEnabled(isFilterEnabled());
        selectedResourceButton.setEnabled(isFilterEnabled());
        selectedResourceAndChildrenButton.setEnabled(isFilterEnabled());
        workingSetGroup.setEnabled(isFilterEnabled());
    }

    /**
     * Updates the given filter from the UI state.
     */
    protected void updateFilterFromUI() {
    	
    	MarkerFilter filter = getSelectedFilter();
    	
    	if(filter == null){
    		selectedComposite.setEnabled(false);
    		return;
    	}
    		
    	if(!selectedComposite.isEnabled())
    		selectedComposite.setEnabled(true);
    	
        updateFilterFromUI(filter);
    }

    /**
     * Update the selected filter from the UI.
     * @param filter
     */
	private void updateFilterFromUI(MarkerFilter filter) {
		filter.setEnabled(filterEnabledButton.getSelection());

        filter.setSelectedTypes(getSelectedTypes());

        if (selectedResourceButton.getSelection())
            filter.setOnResource(MarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        else if (selectedResourceAndChildrenButton.getSelection())
            filter
                    .setOnResource(MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        else if (anyResourceInSameProjectButton.getSelection())
            filter.setOnResource(MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        else if (workingSetGroup.getSelection())
            filter.setOnResource(MarkerFilter.ON_WORKING_SET);
        else
            filter.setOnResource(MarkerFilter.ON_ANY_RESOURCE);

        filter.setWorkingSet(workingSetGroup.getWorkingSet());

        int markerLimit = MarkerFilter.DEFAULT_MARKER_LIMIT;

        try {
            markerLimit = Integer.parseInt(this.markerLimit.getText());
        } catch (NumberFormatException e) {
        }

        filter.setMarkerLimit(markerLimit);
        filter.setFilterOnMarkerLimit(filterOnMarkerLimit.getSelection());
	}

    /**
     * Updates the UI state from the given filter.
     */
    protected void updateUIFromFilter() {
    	
    	MarkerFilter filter = getSelectedFilter();
    	
    	if(filter == null){
    		selectedComposite.setEnabled(false);
    		return;
    	}
    		
    	if(!selectedComposite.isEnabled())
    		selectedComposite.setEnabled(true);
    	
        filterEnabledButton.setSelection(filter.isEnabled());

        setSelectedTypes(filter.getSelectedTypes());

        int on = filter.getOnResource();
        anyResourceButton.setSelection(on == MarkerFilter.ON_ANY_RESOURCE);
        anyResourceInSameProjectButton
                .setSelection(on == MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        selectedResourceButton
                .setSelection(on == MarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        selectedResourceAndChildrenButton
                .setSelection(on == MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        workingSetGroup.setSelection(on == MarkerFilter.ON_WORKING_SET);
        workingSetGroup.setWorkingSet(filter.getWorkingSet());

        markerLimit.setText("" + filter.getMarkerLimit()); //$NON-NLS-1$
        filterOnMarkerLimit.setSelection(filter.getFilterOnMarkerLimit());

        updateEnabledState();
    }

    /**
     * Handles selection on a check box or combo box.
     * @param e
     */
    protected void widgetSelected(SelectionEvent e) {
        updateEnabledState();
        markDirty();
    }

    /**
     * @return <code>true</code> if the dirty flag has been set otherwise
     * <code>false</code>.
     */
    boolean isDirty() {
        return dirty;
    }

    /**
     * Marks the dialog as dirty.
     */
    void markDirty() {
        dirty = true;
    }

    /**
     * Set the marker filter.
     * @param newFilter
     */
    public void setFilter(MarkerFilter newFilter) {
        filters = new MarkerFilter[] {newFilter};
        updateUIFromFilter();
    }

    /**
     * @return the MarkerFilter associated with the dialog.
     */
    public MarkerFilter[] getFilters() {
        return filters;
    }

    /**
     * @return <code>true</code> if the filter's enablement button is checked 
     * otherwise <code>false</code>.
     */
    protected boolean isFilterEnabled() {
        return (filterEnabledButton == null)
                || filterEnabledButton.getSelection();
    }

}
