/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de>: 
 *         Fix for Bug 77336 [Markers] [Dialogs] TableSortDialog does not use dialog font
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The TableSortDialog is the dialog that allows sort order to
 * be selected.
 */
public class TableSortDialog extends TrayDialog {

    private TableComparator sorter;

    private Combo[] priorityCombos;

    private String[] propertyText;

    private IField[] properties;

    private Button[] ascendingButtons;

    private Button[] descendingButtons;

    private boolean dirty;

    private final Comparator columnComparator = new Comparator() {
        public int compare(Object arg0, Object arg1) {
            int index0 = -1;
            int index1 = -1;
            for (int i = 0; i < propertyText.length; i++) {
                if (propertyText[i].equals(arg0)) {
					index0 = i;
				}
                if (propertyText[i].equals(arg1)) {
					index1 = i;
				}
            }
            return index0 - index1;
        }
    };

    /**
     * Create a new instance of the receiver.
     * @param parentShell
     * @param sorter
     */
    public TableSortDialog(IShellProvider parentShell, TableComparator sorter) {
        super(parentShell);
        this.sorter = sorter;
        dirty = false;
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(MarkerMessages.sortDialog_title);
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        if (sorter == null) {
			return composite;
		}

        initializeDialogUnits(composite);
        
        createPrioritiesArea(composite);
        createRestoreDefaultsButton(composite);
        createSeparatorLine(composite);
        
        Dialog.applyDialogFont(composite);

        return composite;
    }

    /**
     * Create the proirities area.
     * @param parent
     */
    private void createPrioritiesArea(Composite parent) {
        Composite prioritiesArea = new Composite(parent, SWT.NULL);
        prioritiesArea.setLayout(new GridLayout(3, false));
        
        int[] priorities = sorter.getPriorities();

        ascendingButtons = new Button[priorities.length];
        descendingButtons = new Button[priorities.length];
        priorityCombos = new Combo[Math.min(priorities.length,
                TableComparator.MAX_DEPTH)];
        initPriotityText();

        Label sortByLabel = new Label(prioritiesArea, SWT.NULL);
        sortByLabel.setText(MarkerMessages.sortDialog_label); 
        GridData data = new GridData();
        data.horizontalSpan = 3;
        sortByLabel.setLayoutData(data);

        for (int i = 0; i < priorityCombos.length; i++) {
            final int index = i;
            Label numberLabel = new Label(prioritiesArea, SWT.NULL);
            numberLabel
                    .setText(NLS
                            .bind(MarkerMessages.sortDialog_columnLabel,new Integer(i + 1))); 

            priorityCombos[i] = new Combo(prioritiesArea, SWT.READ_ONLY);
            priorityCombos[i].setLayoutData(new GridData(
                    GridData.FILL_HORIZONTAL));

            Composite directionGroup = new Composite(prioritiesArea, SWT.NONE);
            directionGroup.setLayout(new GridLayout(2, false));
            
            ascendingButtons[i] = new Button(directionGroup, SWT.RADIO);
            ascendingButtons[i].setText(getAscendingText(i));
            ascendingButtons[i].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    markDirty();
                }
            });
            descendingButtons[i] = new Button(directionGroup, SWT.RADIO);
            descendingButtons[i].setText(getDescendingText(i));
            descendingButtons[i].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    markDirty();
                }
            });

            if (i < priorityCombos.length - 1) {
                priorityCombos[i].addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        int oldSelectionDirection = TableComparator.ASCENDING;
                        if (descendingButtons[index].getSelection()) {
							oldSelectionDirection = TableComparator.DESCENDING;
						}
                        ArrayList oldSelectionList = new ArrayList(Arrays
                                .asList(priorityCombos[index].getItems()));
                        oldSelectionList.removeAll(Arrays
                                .asList(priorityCombos[index + 1].getItems()));
                        if (oldSelectionList.size() != 1) {
							return;
						}
                        String oldSelection = (String) oldSelectionList.get(0);
                        String newSelection = priorityCombos[index]
                                .getItem(priorityCombos[index]
                                        .getSelectionIndex());
                        if (oldSelection.equals(newSelection)) {
                            return;
                        }
                        for (int j = index + 1; j < priorityCombos.length; j++) {
                            int newSelectionIndex = priorityCombos[j]
                                    .indexOf(newSelection);
                            //this combo's current selection is equal to newSelection
                            if (priorityCombos[j].getSelectionIndex() == newSelectionIndex) {
                                priorityCombos[j].remove(newSelection);
                                int insertionPoint = -1
                                        - Arrays.binarySearch(priorityCombos[j]
                                                .getItems(), oldSelection,
                                                columnComparator);
                                if (insertionPoint >= 0
                                        && insertionPoint <= priorityCombos[j]
                                                .getItemCount()) {
									priorityCombos[j].add(oldSelection,
                                            insertionPoint);
								} else {
									priorityCombos[j].add(oldSelection);
								}
                                priorityCombos[j].select(priorityCombos[j]
                                        .indexOf(oldSelection));
                                ascendingButtons[index]
                                        .setSelection(ascendingButtons[j]
                                                .getSelection());
                                descendingButtons[index]
                                        .setSelection(descendingButtons[j]
                                                .getSelection());
                                ascendingButtons[j]
                                        .setSelection(oldSelectionDirection == TableComparator.ASCENDING);
                                descendingButtons[j]
                                        .setSelection(oldSelectionDirection == TableComparator.DESCENDING);
                            }
                            //this combo contains newSelection
                            else if (newSelectionIndex >= 0) {
                                priorityCombos[j].remove(newSelection);
                                int insertionPoint = -1
                                        - Arrays.binarySearch(priorityCombos[j]
                                                .getItems(), oldSelection,
                                                columnComparator);
                                if (insertionPoint >= 0
                                        && insertionPoint <= priorityCombos[j]
                                                .getItemCount()) {
									priorityCombos[j].add(oldSelection,
                                            insertionPoint);
								} else {
									priorityCombos[j].add(oldSelection);
								}
                            }
                        }
                        markDirty();
                    }
                });
            } else {
                priorityCombos[i].addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        markDirty();
                    }
                });
            }
        }
        updateUIFromSorter();
    }

    /**
     * Get the descending label for the Descending field
     * at i. Use the index to determine the mnemonic.
     * @param index
     * @return String
     */
	private String getDescendingText(int index) {
		switch (index) {
		case 1:
			return MarkerMessages.sortDirectionDescending_text2;
		case 2:
			return MarkerMessages.sortDirectionDescending_text3;
		case 3:
			return MarkerMessages.sortDirectionDescending_text4;
		default:
			return MarkerMessages.sortDirectionDescending_text;
	}
	
}

    /**
     * Get the ascending label for the Ascending field
     * at i. Use the index to determine the mnemonic.
     * @param index
     * @return String
     */
	private String getAscendingText(int index) {
		switch (index) {
			case 1:
				return MarkerMessages.sortDirectionAscending_text2;
			case 2:
				return MarkerMessages.sortDirectionAscending_text3;
			case 3:
				return MarkerMessages.sortDirectionAscending_text4;
			default:
				return MarkerMessages.sortDirectionAscending_text;
		}
		
	}

    /**
     * Create the restore defaults button.
     * @param parent
     */
    private void createRestoreDefaultsButton(Composite parent) {
        Button defaultsButton = new Button(parent, SWT.PUSH);
        defaultsButton.setText(MarkerMessages.restoreDefaults_text);
        setButtonSize(defaultsButton, new GridData(
                GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        defaultsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                restoreDefaults();
                markDirty();
            }
        });
    }

    private void createSeparatorLine(Composite parent) {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.VERTICAL_ALIGN_CENTER));
        //		separator.setForeground(new Color(parent.getDisplay(), 150, 150, 150));
        //		separator.setBackground(new Color(parent.getDisplay(), 150, 150, 150));
    }

    private void restoreDefaults() {
        updateUI(sorter.getDefaultPriorities(), sorter.getDefaultDirections());
    }

    private void updateUIFromSorter() {
        updateUI(sorter.getPriorities(), sorter.getDirections());
    }

    private void updateUI(int[] priorities, int[] directions) {
        ArrayList availablePriorities = new ArrayList(Arrays
                .asList(propertyText));

        for (int i = 0; i < priorityCombos.length; i++) {
            priorityCombos[i].removeAll();
            for (int j = 0; j < availablePriorities.size(); j++) {
				priorityCombos[i].add((String) availablePriorities.get(j));
			}
            priorityCombos[i].select(priorityCombos[i]
                    .indexOf(propertyText[priorities[i]]));
            availablePriorities.remove(propertyText[priorities[i]]);

            ascendingButtons[i]
                    .setSelection(directions[priorities[i]] == TableComparator.ASCENDING);
            descendingButtons[i]
                    .setSelection(directions[priorities[i]] == TableComparator.DESCENDING);
        }
    }

    protected void okPressed() {
        if (isDirty()) {
            for (int i = priorityCombos.length - 1; i >= 0; i--) {
                String column = priorityCombos[i].getItem(priorityCombos[i]
                        .getSelectionIndex());
                int index = -1;
                for (int j = 0; j < propertyText.length && index == -1; j++) {
                    if (propertyText[j].equals(column)) {
						index = j;
					}
                }
                if (index == -1) {
                    sorter.resetState();
                    return;
                }
                sorter.setTopPriority(properties[index]);
                int direction = TableComparator.ASCENDING;
                if (descendingButtons[i].getSelection()) {
					direction = TableComparator.DESCENDING;
				}
                sorter.setTopPriorityDirection(direction);
            }
        }
        super.okPressed();
    }

    /**
     * @return boolean
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag to true.
     */
    public void markDirty() {
        dirty = true;
    }

    /**
     * Set the layout data of the button to a GridData with 
     * appropriate heights and widths.
     * @param button
     */
    private void setButtonSize(Button button, GridData buttonData) {
    	button.setFont(button.getParent().getFont());
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        buttonData.widthHint = Math.max(widthHint, button.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x);
        button.setLayoutData(buttonData);
    }

    private void initPriotityText() {
        IField[] unorderedProperties = sorter.getFields();
        properties = new IField[unorderedProperties.length];
        System.arraycopy(unorderedProperties, 0, properties, 0,
                properties.length);
        propertyText = new String[properties.length];
        for (int i = 0; i < properties.length; i++) {
            propertyText[i] = properties[i].getDescription();
        }
    }

    /**
     * Return the sorter for the receiver.
     * @return TableSorter
     */
    public TableComparator getSorter() {
        return sorter;
    }
}
