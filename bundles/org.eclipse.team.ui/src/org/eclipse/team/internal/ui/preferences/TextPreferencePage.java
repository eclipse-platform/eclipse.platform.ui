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

 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.core.IFileContentManager;
import org.eclipse.team.core.IStringMapping;
import org.eclipse.team.core.Team;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.PixelConverter;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * This preference page displays all patterns which determine whether a resource
 * is to be treated as a text file or not. The page allows the user to add or
 * remove entries from this table, and change their values from Text to Binary.
 */
public class TextPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private static final class ExtensionValidator implements IInputValidator {
		public String isValid(String newText) {
			if (newText.trim().length() == 0)
				return ""; //$NON-NLS-1$
			newText = strip(newText);
			if (newText.indexOf('*') >= 0)
				return Policy.bind("TextPreferencePage.2"); //$NON-NLS-1$
			if (newText.indexOf('.') >= 0)
				return Policy.bind("TextPreferencePage.3"); //$NON-NLS-1$
			return null;
		}

		/**
		 * @param newText
		 * @return
		 */
		public String strip(String newText) {
			newText= newText.trim();
			if (newText.startsWith("*")) //$NON-NLS-1$
				newText= newText.substring(1);
			if (newText.startsWith(".")) //$NON-NLS-1$
				newText= newText.substring(1);
			return newText;
		}
	}
	
	private static final class FilenameValidator implements IInputValidator {
		public String isValid(String newText) {
			if (newText.trim().length() == 0)
				return ""; //$NON-NLS-1$
			if (newText.indexOf('*') >= 0)
				return Policy.bind("TextPreferencePage.5"); //$NON-NLS-1$
			return null;
		}
	}
	
	// The input for the table viewer
	private final List fItems;
	
	// Widgets
	private Button fRemoveButton;
	private Button fChangeButton;

    protected FileTypeTable fTable;

	private Set fPluginNames;
	private Set fPluginExtensions;
    
    public TextPreferencePage() {
        fItems= new ArrayList();
        initializeItems();
    }
    
    private void initializeItems() {
        
        fItems.clear();
        
        final IFileContentManager manager= Team.getFileContentManager();

	    final IStringMapping [] extensionInfoArray= manager.getExtensionMappings();
        final IStringMapping [] nameInfoArray= manager.getNameMappings();
        
        fPluginNames= makeSetOfStrings(manager.getDefaultNameMappings());
        fPluginExtensions= makeSetOfStrings(manager.getDefaultExtensionMappings());
        
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
    
    private static Set makeSetOfStrings(IStringMapping [] mappings) {
    	final Set set= new HashSet(mappings.length);
    	for (int i = 0; i < mappings.length; i++) {
			set.add(mappings[i].getString());
		}
    	return set;
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
		
		final PixelConverter converter= SWTUtils.createDialogPixelConverter(parent);
		
		final Composite composite= SWTUtils.createHVFillComposite(parent, SWTUtils.MARGINS_NONE, 2);
		
		fTable= new FileTypeTable(composite, fItems, false);

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
		buttonsComposite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
		
		final Button addExtensionButton = new Button(buttonsComposite, SWT.PUSH);
		addExtensionButton.setText(Policy.bind("TextPreferencePage.add")); //$NON-NLS-1$
		final Button addNameButton = new Button(buttonsComposite, SWT.PUSH);
		addNameButton.setText(Policy.bind("TextPreferencePage.0"));  //$NON-NLS-1$
		fChangeButton = new Button(buttonsComposite, SWT.PUSH);
		fChangeButton.setText(Policy.bind("TextPreferencePage.change")); //$NON-NLS-1$
		fRemoveButton= new Button(buttonsComposite, SWT.PUSH);
		fRemoveButton.setText(Policy.bind("TextPreferencePage.remove")); //$NON-NLS-1$
		
		SWTUtils.createLabel(composite, Policy.bind("TextPreferencePage.1"), 2); //$NON-NLS-1$
		
		/**
		 * Calculate and set the button size 
		 */
		applyDialogFont(composite);
		final int buttonWidth= SWTUtils.calculateControlSize(converter, new Button [] { addExtensionButton, addNameButton, fChangeButton, fRemoveButton });
		addExtensionButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		addNameButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		fChangeButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		fRemoveButton.setLayoutData(SWTUtils.createGridData(buttonWidth, SWT.DEFAULT, SWT.FILL, SWT.FILL, false, false));
		
		addExtensionButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addExtension();
			}
		});
		addNameButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addName();
			}
		});

		fChangeButton.setEnabled(false);
		fChangeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				changePattern();
			}
		});
		
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
		final ExtensionValidator validator = new ExtensionValidator();
		final InputDialog dialog = new InputDialog(getShell(), Policy.bind("TextPreferencePage.enterExtensionShort"), Policy.bind("TextPreferencePage.enterExtensionLong"), null, validator); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() != Window.OK) 
			return;
		
		final String extension = validator.strip(dialog.getValue());
		
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
		final InputDialog dialog = new InputDialog(getShell(), Policy.bind("TextPreferencePage.6"), Policy.bind("TextPreferencePage.7"), null, new FilenameValidator()); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog.open() != Window.OK) 
			return;
		
		final String name = dialog.getValue();
		
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
			if (item instanceof FileTypeTable.Extension && fPluginExtensions.contains(item.name))
				continue;
			if (item instanceof FileTypeTable.Name && fPluginNames.contains(item.name))
				continue;
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
}
