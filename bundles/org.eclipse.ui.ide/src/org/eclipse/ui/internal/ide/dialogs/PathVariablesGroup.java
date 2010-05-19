/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 *     Helena Halperin (IBM) - bug #299212
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A widget group that displays path variables. 
 * Includes buttons to edit, remove existing variables and create new ones.
 * 
 * @since 2.1
 */
public class PathVariablesGroup {
    /**
     * Simple data structure that holds a path variable name/value pair.
     */
    public static class PathVariableElement {
        /**
         * The name of the element.
         */
        public String name;

        /**
         * The path of the element.
         */
        public IPath path;
    }

    // sizing constants
    private static final int SIZING_SELECTION_PANE_WIDTH = 400;

    // parent shell
    private Shell shell;

    private Label variableLabel;

    private TableViewer variableTable;

    private Button addButton;

    private Button editButton;

    private Button removeButton;

    // used to compute layout sizes
    private FontMetrics fontMetrics;

    // create a multi select table
    private boolean multiSelect;

    // IResource.FILE and/or IResource.FOLDER
    private int variableType;

    // External listener called when the table selection changes
    private Listener selectionListener;

    // temporary collection for keeping currently defined variables
    private SortedMap tempPathVariables;

    // set of removed variables' names
    private Set removedVariableNames;

    // reference to the workspace's path variable manager
    private IPathVariableManager pathVariableManager;

    // if set to true, variables will be saved after each change
    private boolean saveVariablesOnChange = false;
    
    // file image
    private final Image FILE_IMG = PlatformUI.getWorkbench().getSharedImages()
            .getImage(ISharedImages.IMG_OBJ_FILE);

    // folder image
    private final Image FOLDER_IMG = PlatformUI.getWorkbench()
            .getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

    private final Image BUILTIN_IMG = PlatformUI.getWorkbench()
            .getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    // unknown (non-existent) image. created locally, dispose locally
    private Image imageUnkown;

    // current project for which the variables are being edited.
    // If null, the workspace variables are being edited instead.
    private IResource currentResource = null;

    private final static String PARENT_VARIABLE_NAME = "PARENT"; //$NON-NLS-1$
    
	/**
     * Creates a new PathVariablesGroup.
     *
     * @param multiSelect create a multi select tree
     * @param variableType the type of variables that are displayed in 
     * 	the widget group. <code>IResource.FILE</code> and/or <code>IResource.FOLDER</code>
     * 	logically ORed together.
     */
    public PathVariablesGroup(boolean multiSelect, int variableType) {
        this.multiSelect = multiSelect;
        this.variableType = variableType;
        pathVariableManager = ResourcesPlugin.getWorkspace()
                .getPathVariableManager();
        removedVariableNames = new HashSet();
        tempPathVariables = new TreeMap();
        // initialize internal model
        initTemporaryState();
    }

    /**
     * Creates a new PathVariablesGroup.
     *
     * @param multiSelect create a multi select tree
     * @param variableType the type of variables that are displayed in 
     * 	the widget group. <code>IResource.FILE</code> and/or <code>IResource.FOLDER</code>
     * 	logically ORed together.
     * @param selectionListener listener notified when the selection changes
     * 	in the variables list.
     */
    public PathVariablesGroup(boolean multiSelect, int variableType,
            Listener selectionListener) {
        this(multiSelect, variableType);
        this.selectionListener = selectionListener;
    }

    /**
     * Opens a dialog for creating a new variable.
     */
    private void addNewVariable() {
        // constructs a dialog for editing the new variable's current name and value
        PathVariableDialog dialog = new PathVariableDialog(shell,
                PathVariableDialog.NEW_VARIABLE, variableType,
                pathVariableManager, tempPathVariables.keySet());

        dialog.setResource(currentResource);
        // opens the dialog - just returns if the user cancels it
        if (dialog.open() == Window.CANCEL) {
			return;
		}

        // otherwise, adds the new variable (or updates an existing one) in the
        // temporary collection of currently defined variables
        String newVariableName = dialog.getVariableName();
        IPath newVariableValue = new Path(dialog.getVariableValue());
        tempPathVariables.put(newVariableName, newVariableValue);

        // the UI must be updated
        updateWidgetState();
        saveVariablesIfRequired();
    }

    /**
     * Creates the widget group.
     * Callers must call <code>dispose</code> when the group is no 
     * longer needed.
     * 
     * @param parent the widget parent
     * @return container of the widgets 
     */
    public Control createContents(Composite parent) {
        Font font = parent.getFont();

        if (imageUnkown == null) {
            ImageDescriptor descriptor = AbstractUIPlugin
                    .imageDescriptorFromPlugin(
                            IDEWorkbenchPlugin.IDE_WORKBENCH,
                            "$nl$/icons/full/obj16/warning.gif"); //$NON-NLS-1$
            imageUnkown = descriptor.createImage();
        }
        initializeDialogUnits(parent);
        shell = parent.getShell();

        // define container & its layout
        Composite pageComponent = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        pageComponent.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_SELECTION_PANE_WIDTH;
        pageComponent.setLayoutData(data);
        pageComponent.setFont(font);

        // layout the table & its buttons
        variableLabel = new Label(pageComponent, SWT.LEFT);
        if (currentResource == null)
            variableLabel.setText(IDEWorkbenchMessages.PathVariablesBlock_variablesLabel);
        else
            variableLabel.setText(NLS.bind(
                                    IDEWorkbenchMessages.PathVariablesBlock_variablesLabelForResource,
                                    currentResource.getName()));

        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 2;
        variableLabel.setLayoutData(data);
        variableLabel.setFont(font);

        int tableStyle = SWT.BORDER | SWT.FULL_SELECTION;
        if (multiSelect) {
            tableStyle |= SWT.MULTI;
        }
        
		Composite tableComposite = new Composite(pageComponent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		tableComposite.setLayoutData(data);

		variableTable = new TableViewer(tableComposite, tableStyle);
        variableTable.getTable().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateEnabledState();
                if (selectionListener != null) {
					selectionListener.handleEvent(new Event());
				}
            }
        });
        
        variableTable.getTable().setFont(font);
		ColumnViewerToolTipSupport.enableFor(variableTable, ToolTip.NO_RECREATE);

		TableViewerColumn nameColumn = new TableViewerColumn(variableTable, SWT.NONE);
		nameColumn.setLabelProvider(new NameLabelProvider());
		nameColumn.getColumn().setText(IDEWorkbenchMessages.PathVariablesBlock_nameColumn);

        TableViewerColumn valueColumn = new TableViewerColumn(variableTable, SWT.NONE);
        valueColumn.setLabelProvider(new ValueLabelProvider());
        valueColumn.getColumn().setText(IDEWorkbenchMessages.PathVariablesBlock_valueColumn);
        
        TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout( tableLayout );

		tableLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(150));
		tableLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(280));

		variableTable.getTable().setHeaderVisible(true);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = variableTable.getTable().getItemHeight() * 7;
        variableTable.getTable().setLayoutData(data);
        variableTable.getTable().setFont(font);

        variableTable.getTable().addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
		        int itemsSelectedCount = variableTable.getTable().getSelectionCount();
		        if (itemsSelectedCount == 1 && canChangeSelection())
		        	editSelectedVariable();
			}
			public void mouseDown(MouseEvent e) { }
			public void mouseUp(MouseEvent e) { }
        });
        variableTable.getTable().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
		        updateEnabledState();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
		        updateEnabledState();
			}
        });
        
        variableTable.getTable().setToolTipText(null);
        variableTable.setContentProvider(new ContentProvider());
        variableTable.setInput(this);
        createButtonGroup(pageComponent);
        return pageComponent;
    }

    class NameLabelProvider extends CellLabelProvider
    {
		public String getToolTipText(Object element) {
            return null;
		}

		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}

		public int getToolTipDisplayDelayTime(Object object) {
			return 0;
		}

		public int getToolTipTimeDisplayed(Object object) {
			return 15000;
		}

		public void update(ViewerCell cell) {
			String varName = (String) cell.getElement();
			cell.setText(varName);
			IPath value = (IPath) tempPathVariables.get(varName);
        	URI resolvedURI = pathVariableManager.resolveURI(URIUtil.toURI(value));
        	IPath resolvedValue = URIUtil.toPath(resolvedURI);
            IFileInfo file = IDEResourceInfoUtils.getFileInfo(resolvedValue);
            if (!isBuiltInVariable(varName))
            	cell.setImage(file.exists() ? (file.isDirectory() ? FOLDER_IMG : FILE_IMG) : imageUnkown);
            else
            	cell.setImage(BUILTIN_IMG);
		}
    	
    }
    
    class ValueLabelProvider extends CellLabelProvider
    {
		public String getToolTipText(Object element) {
            IPath value = (IPath) tempPathVariables.get(element);
        	URI resolvedURI = pathVariableManager.resolveURI(URIUtil.toURI(value));
        	IPath resolvedValue = URIUtil.toPath(resolvedURI);
            return TextProcessor.process(resolvedValue.toOSString());
		}

		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}

		public int getToolTipDisplayDelayTime(Object object) {
			return 0;
		}

		public int getToolTipTimeDisplayed(Object object) {
			return 15000;
		}

		public void update(ViewerCell cell) {
            IPath value = (IPath) tempPathVariables.get(cell.getElement());
			cell.setText(TextProcessor.process(removeParentVariable(value.toOSString())));
		}
    	
    }

	/**
     * Disposes the group's resources. 
     */
    public void dispose() {
        if (imageUnkown != null) {
            imageUnkown.dispose();
            imageUnkown = null;
        }
    }

    /**
     * Opens a dialog for editing an existing variable.
     *
     * @see PathVariableDialog
     */
    private void editSelectedVariable() {
        // retrieves the name and value for the currently selected variable
        TableItem item = variableTable.getTable().getItem(variableTable.getTable()
                .getSelectionIndex());
        String variableName = (String) item.getData();
        IPath variableValue = (IPath) tempPathVariables.get(variableName);

        // constructs a dialog for editing the variable's current name and value
        PathVariableDialog dialog = new PathVariableDialog(shell,
                PathVariableDialog.EXISTING_VARIABLE, variableType,
                pathVariableManager, tempPathVariables.keySet());
        dialog.setVariableName(variableName);
        dialog.setVariableValue(variableValue.toOSString());
        dialog.setResource(currentResource);

        // opens the dialog - just returns if the user cancels it
        if (dialog.open() == Window.CANCEL) {
			return;
		}

        // the name can be changed, so we remove the current variable definition...
        removedVariableNames.add(variableName);
        tempPathVariables.remove(variableName);

        String newVariableName = dialog.getVariableName();
        IPath newVariableValue = new Path(dialog.getVariableValue());

        // and add it again (maybe with a different name)
        tempPathVariables.put(newVariableName, newVariableValue);

        // now we must refresh the UI state
        updateWidgetState();
        saveVariablesIfRequired();
    }

    /**
     * Returns the enabled state of the group's widgets.
     * Returns <code>true</code> if called prior to calling 
     * <code>createContents</code>.
     * 
     * @return boolean the enabled state of the group's widgets.
     * 	 <code>true</code> if called prior to calling <code>createContents</code>.
     */
    public boolean getEnabled() {
        if (variableTable != null && !variableTable.getTable().isDisposed()) {
            return variableTable.getTable().getEnabled();
        }
        return true;
    }

    /**
     * Automatically save the path variable list when new variables
     * are added, changed, or removed by the user. 
     * @param value 
     *
     */
    public void setSaveVariablesOnChange(boolean value) {
    	saveVariablesOnChange = value;
    }
    
    private void saveVariablesIfRequired() {
    	if (saveVariablesOnChange) {
    		performOk();
    	}
    }
    /**
     * Returns the selected variables.
     *  
     * @return the selected variables. Returns an empty array if 
     * 	the widget group has not been created yet by calling 
     * 	<code>createContents</code>
     */
    public PathVariableElement[] getSelection() {
        if (variableTable == null) {
            return new PathVariableElement[0];
        }
        TableItem[] items = variableTable.getTable().getSelection();
        PathVariableElement[] selection = new PathVariableElement[items.length];

        for (int i = 0; i < items.length; i++) {
            String name = (String) items[i].getData();
            selection[i] = new PathVariableElement();
            selection[i].name = name;
            selection[i].path = (IPath) tempPathVariables.get(name);
        }
        return selection;
    }

    /**
     * Creates the add/edit/remove buttons
     * 
     * @param parent the widget parent
     */
    private void createButtonGroup(Composite parent) {
        Font font = parent.getFont();
        Composite groupComponent = new Composite(parent, SWT.NULL);
        GridLayout groupLayout = new GridLayout();
        groupLayout.marginWidth = 0;
        groupLayout.marginHeight = 0;
        groupComponent.setLayout(groupLayout);
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        groupComponent.setLayoutData(data);
        groupComponent.setFont(font);

        addButton = new Button(groupComponent, SWT.PUSH);
        addButton.setText(IDEWorkbenchMessages.PathVariablesBlock_addVariableButton);
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addNewVariable();
            }
        });
        addButton.setFont(font);
        setButtonLayoutData(addButton);

        editButton = new Button(groupComponent, SWT.PUSH);
        editButton.setText(IDEWorkbenchMessages.PathVariablesBlock_editVariableButton);
        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editSelectedVariable();
            }
        });
        editButton.setFont(font);
        setButtonLayoutData(editButton);

        removeButton = new Button(groupComponent, SWT.PUSH);
        removeButton.setText(IDEWorkbenchMessages.PathVariablesBlock_removeVariableButton);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeSelectedVariables();
            }
        });
        removeButton.setFont(font);
        setButtonLayoutData(removeButton);
        updateEnabledState();
    }

    /**
     * Initializes the computation of horizontal and vertical dialog units
     * based on the size of current font.
     * <p>
     * This method must be called before <code>setButtonLayoutData</code> 
     * is called.
     * </p>
     *
     * @param control a control from which to obtain the current font
     */
    protected void initializeDialogUnits(Control control) {
        // Compute and store a font metric
        GC gc = new GC(control);
        gc.setFont(control.getFont());
        fontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

    /**
     * (Re-)Initialize collections used to mantain temporary variable state.
     */
    private void initTemporaryState() {
        String[] varNames = pathVariableManager.getPathVariableNames();

        tempPathVariables.clear();
        for (int i = 0; i < varNames.length; i++) {
        	// hide the PARENT variable
        	if (varNames[i].equals(PARENT_VARIABLE_NAME))
        		continue;
            try {
				URI uri = pathVariableManager.getURIValue(varNames[i]);
				// the value may not exist any more
				if (uri != null) {
				    IPath value = URIUtil.toPath(uri);
				    if (value != null) {
				        boolean isFile = value.toFile().isFile();
				        if ((isFile && (variableType & IResource.FILE) != 0)
				                || (isFile == false && (variableType & IResource.FOLDER) != 0)) {

				            tempPathVariables.put(varNames[i], value);
				        }
				    }
				}
			} catch (Exception e) {
				// do not present the variable
			}
        }
        removedVariableNames.clear();
    }

    /**
     * Updates button enabled state, depending on the number of currently selected
     * variables in the table.
     */
    private void updateEnabledState() {
        int itemsSelectedCount = variableTable.getTable().getSelectionCount();
        editButton.setEnabled(itemsSelectedCount == 1 && canChangeSelection());
        removeButton.setEnabled(itemsSelectedCount > 0 && canChangeSelection());
    }

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return tempPathVariables.keySet().toArray();
		}
		
		public void dispose() { }
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
	}

	/**
     * Converts the ${PARENT-COUNT-VAR} format to "VAR/../../" format
     * @param value
     * @return the converted value
     */
    private String removeParentVariable(String value) {
    	return pathVariableManager.convertToUserEditableFormat(value, false);
    }
    
    /**
     * Commits the temporary state to the path variable manager in response to user
     * confirmation.
     * @return boolean <code>true</code> if there were no problems.
     * @see IPathVariableManager#setValue(String, IResource, URI)
     */
    public boolean performOk() {
        try {
            // first process removed variables  
            for (Iterator removed = removedVariableNames.iterator(); removed
                    .hasNext();) {
                String removedVariableName = (String) removed.next();
                // only removes variables that have not been added again
                if (!tempPathVariables.containsKey(removedVariableName)) {
					pathVariableManager.setURIValue(removedVariableName, null);
				}
            }

            // then process the current collection of variables, adding/updating them
            for (Iterator current = tempPathVariables.entrySet().iterator(); current
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) current.next();
                String variableName = (String) entry.getKey();
                IPath variableValue = (IPath) entry.getValue();
                if (!isBuiltInVariable(variableName))
                    pathVariableManager.setURIValue(variableName, URIUtil.toURI(variableValue));
            }
            // re-initialize temporary state
            initTemporaryState();

            // performOk accepted
            return true;
        } catch (CoreException ce) {
            ErrorDialog.openError(shell, null, null, ce.getStatus());
        }
        return false;
    }

    /**
     * Removes the currently selected variables.
     */
    private void removeSelectedVariables() {
        // remove each selected element
        int[] selectedIndices = variableTable.getTable().getSelectionIndices();
        for (int i = 0; i < selectedIndices.length; i++) {
            TableItem selectedItem = variableTable.getTable().getItem(selectedIndices[i]);
            String varName = (String) selectedItem.getData();
            removedVariableNames.add(varName);
            tempPathVariables.remove(varName);
        }
        updateWidgetState();
        saveVariablesIfRequired();
    }

    private boolean canChangeSelection() {
        int[] selectedIndices = variableTable.getTable().getSelectionIndices();
        for (int i = 0; i < selectedIndices.length; i++) {
            TableItem selectedItem = variableTable.getTable().getItem(selectedIndices[i]);
            String varName = (String) selectedItem.getData();
            if (isBuiltInVariable(varName))
                return false;
        }
        return true;
    }

    /**
     * @param varName
     *            the variable name to test
     */
    private boolean isBuiltInVariable(String varName) {
        if (currentResource != null) {
        	return !pathVariableManager.isUserDefined(varName);
        }
        return false;
    }

    /**
     * Sets the <code>GridData</code> on the specified button to
     * be one that is spaced for the current dialog page units. The
     * method <code>initializeDialogUnits</code> must be called once
     * before calling this method for the first time.
     *
     * @param button the button to set the <code>GridData</code>
     * @return the <code>GridData</code> set on the specified button
     */
    private GridData setButtonLayoutData(Button button) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
                IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        return data;
    }

    /**
     * Sets the enabled state of the group's widgets.
     * Does nothing if called prior to calling <code>createContents</code>.
     * 
     * @param enabled the new enabled state of the group's widgets
     */
    public void setEnabled(boolean enabled) {
        if (variableTable != null && !variableTable.getTable().isDisposed()) {
            variableLabel.setEnabled(enabled);
            variableTable.getTable().setEnabled(enabled);
            addButton.setEnabled(enabled);
            if (enabled) {
				updateEnabledState();
			} else {
                editButton.setEnabled(enabled);
                removeButton.setEnabled(enabled);
            }
        }
    }

    /**
     * Updates the widget's current state: refreshes the table with the current 
     * defined variables, selects the item corresponding to the given variable 
     * (selects the first item if <code>null</code> is provided) and updates 
     * the enabled state for the Add/Remove/Edit buttons.
     * 
     */
    private void updateWidgetState() {
    	variableTable.refresh();
        updateEnabledState();
    }

    /**
     * @param resource
     */
    public void setResource(IResource resource) {
    	currentResource = resource;
        if (resource != null)
        	pathVariableManager = resource.getPathVariableManager();
        else
        	pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
        removedVariableNames = new HashSet();
        tempPathVariables = new TreeMap();
        // initialize internal model
        initTemporaryState();
    }

	/**
	 * Reloads the path variables from the project description.
	 */
	public void reloadContent() {
        removedVariableNames = new HashSet();
        tempPathVariables = new TreeMap();
		initTemporaryState();
		if (variableTable != null)
	        updateWidgetState();
	}
}
