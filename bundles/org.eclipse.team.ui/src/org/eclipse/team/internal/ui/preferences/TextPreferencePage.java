/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.preferences;

 
import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.IStringMapping;
import org.eclipse.team.core.Team;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * This preference page displays all patterns which determine whether a resource
 * is to be treated as a text file or not. The page allows the user to add or
 * remove entries from this table, and change their values from Text to Binary.
 */
public class TextPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, FileTypeTable.PixelConverter {
    
	// The input for the table viewer
	private final List fItems;
	
	// Widgets
	private Button fRemoveButton;
	private Button fChangeButton;

    protected FileTypeTable fTable;
    
    public TextPreferencePage() {
        fItems= new ArrayList();
        initializeItems();
    }
    
    private void initializeItems() {
        
        fItems.clear();

	    final IStringMapping [] extensionInfoArray= Team.getFileContentManager().getExtensionMappings();
        final IStringMapping [] nameInfoArray= Team.getFileContentManager().getNameMappings();
        
        for (int i = 0; i < extensionInfoArray.length; i++) {
            final IStringMapping info= extensionInfoArray[i];
            final FileTypeTable.Extension extension= new FileTypeTable.Extension(info.getString());
            extension.mode= info.getType();
            fItems.add(extension);
        }
        
        for (int i = 0; i < nameInfoArray.length; i++) {
            final IStringMapping info= nameInfoArray[i];
            final FileTypeTable.Name name= new FileTypeTable.Name(info.getString());
            name.mode= info.getType();
            fItems.add(name);
        }

    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		
		// set F1 help
		WorkbenchHelp.setHelp(parent, IHelpContextIds.FILE_TYPE_PREFERENCE_PAGE);
		initializeDialogUnits(parent);	

		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(SWTUtils.createGridLayout(2, 0, 0));
		
		fTable= new FileTypeTable(composite, this, fItems, false);
		

		fTable.getViewer().getControl().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleSelection();
			}
		});

		fTable.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				final ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					return;
				}
				fTable.getViewer().editElement(((IStructuredSelection)selection).getFirstElement(), 1);
			}
		});

		final Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		buttonsComposite.setLayout(SWTUtils.createGridLayout(1, 0, 0));
		
		final int buttonWidth= (3 *convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH)) / 2;
		
		final Button addExtensionButton = new Button(buttonsComposite, SWT.PUSH);
		addExtensionButton.setText(Policy.bind("TextPreferencePage.add")); //$NON-NLS-1$
		addExtensionButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		addExtensionButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addExtension();
			}
		});
		
		final Button addNameButton = new Button(buttonsComposite, SWT.PUSH);
		addNameButton.setText(Policy.bind("TextPreferencePage.0"));  //$NON-NLS-1$
		addNameButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		addNameButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addName();
			}
		});

		
		fChangeButton = new Button(buttonsComposite, SWT.PUSH);
		fChangeButton.setText(Policy.bind("TextPreferencePage.change")); //$NON-NLS-1$
		fChangeButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		fChangeButton.setEnabled(false);
		fChangeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				changePattern();
			}
		});
		
		fRemoveButton= new Button(buttonsComposite, SWT.PUSH);
		fRemoveButton.setText(Policy.bind("TextPreferencePage.remove")); //$NON-NLS-1$
		fRemoveButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		fRemoveButton.setEnabled(false);
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removePattern();
			}
		});
		
		Dialog.applyDialogFont(parent);
		return composite;
	}
	
	protected void performDefaults() {
		super.performDefaults();
		initializeItems();
		if (fTable != null)
		    fTable.getViewer().refresh();
	}
	
	/**
	 * Do anything necessary because the OK button has been pressed.
	 *
	 * @return whether it is okay to close the preference page
	 */
	public boolean performOk() {
	    final ArrayList extensionsList= new ArrayList();
	    final ArrayList extensionsModesList= new ArrayList();
	    
	    final ArrayList namesList= new ArrayList();
	    final ArrayList namesModesList= new ArrayList();
	    
	    for (final Iterator iter = fItems.iterator(); iter.hasNext();) {
            final FileTypeTable.Item item= (FileTypeTable.Item) iter.next();
            
            if (item instanceof FileTypeTable.Extension) {
                extensionsList.add(item.name);
                extensionsModesList.add(new Integer(item.mode));
            } else if (item instanceof FileTypeTable.Name) {
                namesList.add(item.name);
                namesModesList.add(new Integer(item.mode));
            }
        }
	    
	    final String [] extensions= (String [])extensionsList.toArray(new String [extensionsList.size()]);
	    final String [] names= (String [])namesList.toArray(new String [namesList.size()]);
	    
	    final int [] extensionsModes= integerListToIntArray(extensionsModesList);
	    final int [] namesModes= integerListToIntArray(namesModesList);
	    
	    Team.getFileContentManager().setExtensionMappings(extensions, extensionsModes);
	    Team.getFileContentManager().setNameMappings(names, namesModes);
	    
		TeamUIPlugin.broadcastPropertyChange(new PropertyChangeEvent(this, TeamUI.GLOBAL_FILE_TYPES_CHANGED, null, null));

		return true;
	}

	private static int [] integerListToIntArray(List integers) {
	    final int [] array= new int [integers.size()];
	    int index= 0; 
	    for (Iterator iter = integers.iterator(); iter.hasNext();)
            array[index++]= ((Integer)iter.next()).intValue();
	    return array;
	}

	/**
	 * Add a new item to the table with the default type of Text.
	 */
	void addExtension() {
		final InputDialog dialog = new InputDialog(getShell(), Policy.bind("TextPreferencePage.enterExtensionShort"), Policy.bind("TextPreferencePage.enterExtensionLong"), null, null); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.open();
		if (dialog.getReturnCode() != InputDialog.OK) return;
		
		final String extension = dialog.getValue().trim().replaceAll("\\*\\.", "");  //$NON-NLS-1$ //$NON-NLS-2$
		if (extension.equals(""))  //$NON-NLS-1$
		    return;
		
		// Check if the item already exists
		final Iterator it = fItems.iterator();
		while (it.hasNext()) {
			final FileTypeTable.Item item= (FileTypeTable.Item)it.next();
			if (item instanceof FileTypeTable.Extension && item.name.equals(extension)) {
				MessageDialog.openWarning(getShell(), Policy.bind("TextPreferencePage.extensionExistsShort"), Policy.bind("TextPreferencePage.extensionExistsLong")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		final FileTypeTable.Item item= new FileTypeTable.Extension(extension);
		fItems.add(item);
		fTable.getViewer().refresh();
	}
	
	/**
	 * Add a new item to the table with the default type of Text.
	 */
	void addName() {
		final InputDialog dialog = new InputDialog(getShell(), "New File Type", "Enter a file name:", null, null); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.open();
		if (dialog.getReturnCode() != InputDialog.OK) return;
		
		final String name = dialog.getValue();
		if (name.length() == 0 || name.indexOf(" ") >= 0)  //$NON-NLS-1$
		    return; //$NON-NLS-1$
		
		// Check if the item already exists
		final Iterator it = fItems.iterator();
		while (it.hasNext()) {
			final FileTypeTable.Item item= (FileTypeTable.Item)it.next();
			if (item instanceof FileTypeTable.Name && item.name.equals(name)) {
				MessageDialog.openWarning(getShell(), Policy.bind("TextPreferencePage.extensionExistsShort"), Policy.bind("TextPreferencePage.extensionExistsLong")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		final FileTypeTable.Item item= new FileTypeTable.Name(name);
		fItems.add(item);
		fTable.getViewer().refresh();
	}
	
	/**
	 * Remove the selected items from the table
	 */
	void removePattern() {
		final IStructuredSelection selection = fTable.getSelection();
		if (selection == null)
			return;
		
		for (final Iterator it = selection.iterator(); it.hasNext(); ) {
			final FileTypeTable.Item item= (FileTypeTable.Item)it.next();
			fItems.remove(item);
		}
		fTable.getViewer().refresh();
	}
	/**
	 * Toggle the selected items' content types
	 */
	void changePattern() {
	    final IStructuredSelection selection = fTable.getSelection();
		if (selection == null)
			return;

		for (final Iterator it = selection.iterator(); it.hasNext(); ) {
			final FileTypeTable.Item item= (FileTypeTable.Item)it.next();
			item.mode= item.mode == Team.TEXT ? Team.BINARY : Team.TEXT;
			fTable.getViewer().refresh(item);
		}
	}
	
	/**
	 * The table viewer selection has changed. Update the remove and change button enablement.
	 */
	void handleSelection() {
		final boolean empty = fTable.getSelection().isEmpty();
		fRemoveButton.setEnabled(!empty);
		fChangeButton.setEnabled(!empty);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#convertWidthInCharsToPixels(int)
     */
    public int convertWidthInCharsToPixels(int chars) {
        return super.convertWidthInCharsToPixels(chars);
    }
}
