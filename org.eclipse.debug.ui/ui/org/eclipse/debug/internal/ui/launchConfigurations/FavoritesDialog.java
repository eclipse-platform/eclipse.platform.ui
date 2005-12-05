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
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DialogSettingsHelper;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * Dialog for organizing favorite launch configurations
 */
public class FavoritesDialog extends Dialog {
	
	/**
	 * Table of favorite launch configurations
	 */
	private TableViewer fFavoritesTable;	

	// history being organized
	private LaunchHistory fHistory;
	
	// favorites collection under edit
	private List fFavorites;

	// buttons
	protected Button fAddFavoriteButton;
	protected Button fRemoveFavoritesButton;
	protected Button fMoveUpButton;
	protected Button fMoveDownButton;

	// button action handler
	/**
	 * Listener that delegates when a button is pressed
	 */
	private SelectionAdapter fButtonListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			if (button == fAddFavoriteButton) {
				handleAddConfigButtonSelected();
			} else if (button == fRemoveFavoritesButton) {
				removeSelectedFavorites();
			} else if (button == fMoveUpButton) {
				handleMoveUpButtonSelected();
			} else if (button == fMoveDownButton) {
				handleMoveDownButtonSelected();
			}
		}
	};	
	
	/**
	 * Listener that delegates when the selection changes in a table
	 */
	private ISelectionChangedListener fSelectionChangedListener= new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			handleFavoriteSelectionChanged();
		}
	};	
	
	/**
	 * Listener that delegates when a key is pressed in a table
	 */
	private KeyListener fKeyListener= new KeyAdapter() {
		public void keyPressed(KeyEvent event) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				removeSelectedFavorites(); 
			}
		}
	};
	
	/**
	 * Content provider for favorites table
	 */
	protected class FavoritesContentProvider implements IStructuredContentProvider {
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			ILaunchConfiguration[] favorites= (ILaunchConfiguration[]) getFavorites().toArray(new ILaunchConfiguration[0]);
			return LaunchConfigurationManager.filterConfigs(favorites);
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * Content provider for recent table
	 */	
	protected class LaunchConfigurationContentProvider extends FavoritesContentProvider {
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			ILaunchConfiguration[] all = null;
			try {
				all = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
				return new ILaunchConfiguration[0];
			}
			List list = new ArrayList(all.length);
			ViewerFilter filter = new LaunchGroupFilter(getLaunchHistory().getLaunchGroup());
			for (int i = 0; i < all.length; i++) {
				if (filter.select(null, null, all[i])) {
					list.add(all[i]);
				}
			}
			list.removeAll(getFavorites());
			Object[] objs = list.toArray();
			new WorkbenchViewerSorter().sort(getFavoritesTable(), objs);
			return objs;
		}

	}	
	
	/**
	 * Constructs a favorites dialog.
	 * 
	 * @param parentShell shell to open the dialog on
	 * @param history launch history to edit
	 */
	public FavoritesDialog(Shell parentShell, LaunchHistory history) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fHistory = history;
	}

	/**
	 * The 'add config' button has been pressed
	 */
	protected void handleAddConfigButtonSelected() {
		
		ListSelectionDialog dialog = new ListSelectionDialog(fFavoritesTable.getControl().getShell(),
			getMode(), new LaunchConfigurationContentProvider(), DebugUITools.newDebugModelPresentation(),
			LaunchConfigurationsMessages.FavoritesDialog_7); 
		dialog.setTitle(MessageFormat.format(LaunchConfigurationsMessages.FavoritesDialog_0, new String[]{getModeLabel()})); 
		dialog.open();
		Object[] selection = dialog.getResult();
		if (selection != null) {
			for (int i = 0; i < selection.length; i++) {
				getFavorites().add(selection[i]);
			}
			updateStatus();
		}
	}	
	
	/**
	 * The 'remove favorites' button has been pressed
	 */
	protected void removeSelectedFavorites() {
		IStructuredSelection sel = (IStructuredSelection)getFavoritesTable().getSelection();
		Iterator iter = sel.iterator();
		while (iter.hasNext()) {
			Object config = iter.next();
			getFavorites().remove(config);
		}
		getFavoritesTable().refresh();		
	}	
	
	/**
	 * The 'move up' button has been pressed
	 */
	protected void handleMoveUpButtonSelected() {
		handleMove(-1);
	}	
	
	/**
	 * The 'move down' button has been pressed
	 */
	protected void handleMoveDownButtonSelected() {
		handleMove(1);
	}	
	
	protected void handleMove(int direction) {
		IStructuredSelection sel = (IStructuredSelection)getFavoritesTable().getSelection();
		List selList= sel.toList();
		Object[] movedFavs= new Object[getFavorites().size()];
		int i;
		for (Iterator favs = selList.iterator(); favs.hasNext();) {
			Object config = favs.next();
			i= getFavorites().indexOf(config);
			movedFavs[i + direction]= config;
		}
		
		getFavorites().removeAll(selList);
			
		for (int j = 0; j < movedFavs.length; j++) {
			Object config = movedFavs[j];
			if (config != null) {
				getFavorites().add(j, config);		
			}
		}
		
		getFavoritesTable().refresh();	
		handleFavoriteSelectionChanged();	
	}
	
	/**
	 * Returns the table of favorite launch configurations.
	 * 
	 * @return table viewer
	 */
	protected TableViewer getFavoritesTable() {
		return fFavoritesTable;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		getShell().setText(MessageFormat.format(LaunchConfigurationsMessages.FavoritesDialog_1, new String[]{getModeLabel()})); 
		createFavoritesArea(composite);
		return composite;
	}

	/**
	 * Returns a label to use for launch mode with accelerators removed.
	 * 
     * @return label to use for launch mode with accelerators removed
     */
    private String getModeLabel() {
        return DebugUIPlugin.removeAccelerators(fHistory.getLaunchGroup().getLabel());
    }

    protected void createFavoritesArea(Composite parent) {
		Composite topComp = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		topComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		topComp.setFont(parent.getFont());
	
		// Create "favorite config" area
		createLabel(topComp, LaunchConfigurationsMessages.FavoritesDialog_2); 
		fFavoritesTable = createTable(topComp, new FavoritesContentProvider());
		Composite buttonComp = createButtonComposite(topComp);
		fAddFavoriteButton = createPushButton(buttonComp,LaunchConfigurationsMessages.FavoritesDialog_3); 
		fAddFavoriteButton.setEnabled(true);
		fRemoveFavoritesButton = createPushButton(buttonComp, LaunchConfigurationsMessages.FavoritesDialog_4); 
		fMoveUpButton = createPushButton(buttonComp, LaunchConfigurationsMessages.FavoritesDialog_5); 
		fMoveDownButton = createPushButton(buttonComp, LaunchConfigurationsMessages.FavoritesDialog_6);  
	}
	
	/**
	 * Creates a fully configured table with the given content provider
	 */
	private TableViewer createTable(Composite parent, IContentProvider contentProvider) {
		TableViewer tableViewer= new TableViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setLabelProvider(DebugUITools.newDebugModelPresentation());
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setInput(DebugUIPlugin.getDefault());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 100;
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.getTable().setFont(parent.getFont());
		tableViewer.addSelectionChangedListener(fSelectionChangedListener);
		tableViewer.getControl().addKeyListener(fKeyListener);
		return tableViewer; 
	}	
	
	/**
	 * Creates and returns a fully configured push button in the given paren with the given label.
	 */
	private Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(parent.getFont());
		setButtonLayoutData(button);
		button.addSelectionListener(fButtonListener);
		button.setEnabled(false);
		return button;
	}
	
	/**
	 * Creates a fully configured composite to add buttons to.
	 */
	private Composite createButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
		return composite;
	}
	
	/**
	 * Creates a fully configured label with the given text
	 */
	private Label createLabel(Composite parent, String labelText) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(labelText);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setFont(parent.getFont());
		return label;
	}	
	
	/**
	 * Returns the current list of favorites.
	 */
	protected List getFavorites() {
		if (fFavorites == null) {
			ILaunchConfiguration[] favs = getInitialFavorites();
			fFavorites = new ArrayList(favs.length);
			addAll(favs, fFavorites);
		}
		return fFavorites;
	}	
	
	protected LaunchHistory getLaunchHistory() {
		return fHistory;
	}	
	
	/**
	 * Returns the initial content for the favorites list
	 */
	protected ILaunchConfiguration[] getInitialFavorites() {
		return getLaunchHistory().getFavorites();
	}	
	
	/**
	 * Returns the mode of this page - run or debug.
	 */
	protected String getMode() {
		return getLaunchHistory().getLaunchGroup().getMode();
	}
			
	/**
	 * Copies the array into the list
	 */
	protected void addAll(Object[] array, List list) {
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
	}
	
	/**
	 * Refresh all tables and buttons
	 */
	protected void updateStatus() {
		getFavoritesTable().refresh();
		handleFavoriteSelectionChanged();				
	}	

	/**
	 * The selection in the favorites list has changed
	 */
	protected void handleFavoriteSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection)getFavoritesTable().getSelection();
		List favs = getFavorites();
		boolean notEmpty = !selection.isEmpty();
		Iterator elements= selection.iterator();
		boolean first= false;
		boolean last= false;
		int lastFav= favs.size() - 1;
		while (elements.hasNext()) {
			Object element = elements.next();
			if(!first && favs.indexOf(element) == 0) {
				first= true;
			}
			if (!last && favs.indexOf(element) == lastFav) {
				last= true;
			}
		}
		
		fRemoveFavoritesButton.setEnabled(notEmpty);
		fMoveUpButton.setEnabled(notEmpty && !first);
		fMoveDownButton.setEnabled(notEmpty && !last);
	}
	
	/**
	 * Method performOK. Uses scheduled Job format.
	 * @since 3.2
	 */
	public void saveFavorites() {
		
		final Job job = new Job(LaunchConfigurationsMessages.FavoritesDialog_8) {
			protected IStatus run(IProgressMonitor monitor) {
				ILaunchConfiguration[] initial = getInitialFavorites();
				List current = getFavorites();
				String groupId = getLaunchHistory().getLaunchGroup().getIdentifier();
				
				int taskSize = Math.abs(initial.length-current.size());//get task size
				monitor.beginTask(LaunchConfigurationsMessages.FavoritesDialog_8, taskSize);//and set it
				
				// removed favorites
				for (int i = 0; i < initial.length; i++) {
					ILaunchConfiguration configuration = initial[i];
					if (!current.contains(configuration)) {
						// remove fav attributes
						try {
							ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
							workingCopy.setAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, (String)null);
							workingCopy.setAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, (String)null);
							List groups = workingCopy.getAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, (List)null);
							if (groups != null) {
								groups.remove(groupId);
								if (groups.isEmpty()) {
									groups = null;	
								}
								workingCopy.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, groups);
							}
							workingCopy.doSave();
						} catch (CoreException e) {
							DebugUIPlugin.log(e);
							return Status.CANCEL_STATUS;
						} 
					}
					monitor.worked(1);
				}
				
				// update added favorites
				Iterator favs = current.iterator();
				while (favs.hasNext()) {
					ILaunchConfiguration configuration = (ILaunchConfiguration)favs.next();
					try {
						List groups = configuration.getAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, (List)null);
						if (groups == null) {
							groups = new ArrayList();
						}
						if (!groups.contains(groupId)) {
							groups.add(groupId);
							ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
							workingCopy.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, groups);
							workingCopy.doSave();
						}
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
						return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
				}
				 
				fHistory.setFavorites(getArray(current));
				monitor.done();
				return Status.OK_STATUS;
			}			
		};
		job.setPriority(Job.LONG);
		PlatformUI.getWorkbench().getProgressService().showInDialog(getParentShell(), job);
		job.schedule();
			
	}	
	
	protected ILaunchConfiguration[] getArray(List list) {
		return (ILaunchConfiguration[])list.toArray(new ILaunchConfiguration[list.size()]);
	} 
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		saveFavorites();
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		DialogSettingsHelper.persistShellGeometry(getShell(), getDialogSettingsSectionName());
		return super.close();
	}		
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point initialLocation= DialogSettingsHelper.getInitialLocation(getDialogSettingsSectionName());
		if (initialLocation != null) {
			return initialLocation;
		}
		return super.getInitialLocation(initialSize);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		return DialogSettingsHelper.getInitialSize(getDialogSettingsSectionName(), size);
	}
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	private String getDialogSettingsSectionName() {
		return "FAVORITES_DIALOG_SECTION"; //$NON-NLS-1$
	}
}
