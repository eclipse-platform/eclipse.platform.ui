/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * Tab for favorite and recent history lists
 */
public class LaunchHistoryPreferenceTab {
		
	/**
	 * Table of favorite launch configurations
	 */
	private TableViewer fFavoritesTable;

	/**
	 * Table of recent launch configurations
	 */
	private TableViewer fRecentTable;
	
	/**
	 * Favorite Buttons
	 */
	private Button fAddFavoriteButton;
	private Button fRemoveFavoritesButton;
	private Button fMoveUpButton;
	private Button fMoveDownButton;
	private Button fMakeRecentButton;
	/**
	 * Recent Buttons
	 */
	private Button fMakeFavoritesButton;
	private Button fRemoveRecentButton;
	
	/**
	 * Current collection of favorites and recent launch configs
	 */
	private List fFavorites;
	private List fRecents;
	
	/**
	 * Launch group.
	 */
	private LaunchHistory fLaunchHistory;
	
	/**
	 * Tab image
	 */
	private Image fImage;
	
	/**
	 * The enclosing preference page
	 */
	private LaunchHistoryPreferencePage fPreferencePage;
	
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
			} else if (button == fMakeRecentButton) {
				handleMakeRecentButtonSelected();
			} else if (button == fMakeFavoritesButton) {
				handleMakeFavoriteButtonSelected();
			} else if (button == fRemoveRecentButton) {
				removeSelectedRecent();
			}
		}
	};
	
	/**
	 * Listener that delegates when the selection changes in a table
	 */
	private ISelectionChangedListener fSelectionChangedListener= new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			TableViewer table = (TableViewer) event.getSource();
			if (table == getRecentTable()) {
				handleRecentSelectionChanged();
			} else if (table == getFavoritesTable()) {
				handleFavoriteSelectionChanged();
			}
		}
	};
	
	/**
	 * Listener that delegates when a key is pressed in a table
	 */
	private KeyListener fKeyListener= new KeyAdapter() {
		public void keyPressed(KeyEvent event) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				if (event.widget == getRecentTable().getTable()) {
					removeSelectedRecent();
				} else if (event.widget == getFavoritesTable().getTable()) {
					removeSelectedFavorites();
				} 
			}
		}
	};
	
	/**
	 * Constructs a launch history preference tab for the given launch history
	 * 
	 * @param history
	 */
	public LaunchHistoryPreferenceTab(LaunchHistory history, LaunchHistoryPreferencePage page) {
		fLaunchHistory = history;
		fPreferencePage= page;
	}
	
	protected LaunchHistory getLaunchHistory() {
		return fLaunchHistory;
	}
	
	/**
	 * Creates the control for this tab
	 */
	protected Control createControl(Composite parent) {
		Composite topComp = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		topComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		topComp.setFont(parent.getFont());
	
		// Create "favorite config" area
		createLabel(topComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Fa&vorites__1")); //$NON-NLS-1$
		fFavoritesTable = createTable(topComp, new FavoritesContentProvider());
		Composite buttonComp = createButtonComposite(topComp);
		fAddFavoriteButton = createPushButton(buttonComp,DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Add_&Config_1")); //$NON-NLS-1$
		fAddFavoriteButton.setEnabled(true);
		fRemoveFavoritesButton = createPushButton(buttonComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Re&move_2")); //$NON-NLS-1$
		fMoveUpButton = createPushButton(buttonComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.U&p_3")); //$NON-NLS-1$
		fMoveDownButton = createPushButton(buttonComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Do&wn_4")); //$NON-NLS-1$
		fMakeRecentButton = createPushButton(buttonComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Ma&ke_Recent_2")); //$NON-NLS-1$
	
		createSpacer(topComp, 1);

		// Create "recent config" area
		createLabel(topComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.&Launch_History__3")); //$NON-NLS-1$
		fRecentTable = createTable(topComp, new RecentContentProvider());
		buttonComp = createButtonComposite(topComp);
		fMakeFavoritesButton = createPushButton(buttonComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Make_&Favorite_5")); //$NON-NLS-1$
		fRemoveRecentButton = createPushButton(buttonComp, DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Remo&ve_6")); //$NON-NLS-1$	
				
		return topComp;
	}
	
	/**
	 * Creates a fully configured composite to add buttons to.
	 */
	private Composite createButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
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
		fPreferencePage.setButtonLayoutData(button);
		button.addSelectionListener(fButtonListener);
		button.setEnabled(false);
		return button;
	}

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	/**
	 * Returns the table of favorite launch configurations.
	 * 
	 * @return table viewer
	 */
	protected TableViewer getFavoritesTable() {
		return fFavoritesTable;
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
		fMakeRecentButton.setEnabled(notEmpty);
		fMoveUpButton.setEnabled(notEmpty && !first);
		fMoveDownButton.setEnabled(notEmpty && !last);
	}
	
	/**
	 * Returns the table of recent launch configurations.
	 * 
	 * @return table viewer
	 */
	protected TableViewer getRecentTable() {
		return fRecentTable;
	}

	/**
	 * The selection in the recent list has changed
	 */
	protected void handleRecentSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection)getRecentTable().getSelection();
		boolean notEmpty = !selection.isEmpty();
		
		fRemoveRecentButton.setEnabled(notEmpty);
		fMakeFavoritesButton.setEnabled(notEmpty);
	}	
	
	/**
	 * The 'add config' button has been pressed
	 */
	protected void handleAddConfigButtonSelected() {
		
		ListSelectionDialog dialog = new ListSelectionDialog(fFavoritesTable.getControl().getShell(),
			getMode(), new LaunchConfigurationContentProvider(), DebugUITools.newDebugModelPresentation(),
			DebugPreferencesMessages.getString("LaunchHistoryPreferenceTab.Select_Launch_Configurations_7")); //$NON-NLS-1$
		dialog.open();
		Object[] selection = dialog.getResult();
		if (selection != null) {
			for (int i = 0; i < selection.length; i++) {
				getFavorites().add(selection[i]);
				getRecents().remove(selection[i]);
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
	 * The 'remove recent' button has been pressed
	 */
	protected void removeSelectedRecent() {
		IStructuredSelection sel = (IStructuredSelection)getRecentTable().getSelection();
		Iterator iter = sel.iterator();
		while (iter.hasNext()) {
			Object config = iter.next();
			getRecents().remove(config);
		}
		getRecentTable().refresh();		
	}	
	
	/**
	 * The 'add recent to favorites' button has been pressed
	 */
	protected void handleMakeFavoriteButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection)getRecentTable().getSelection();
		Iterator iter = sel.iterator();
		while (iter.hasNext()) {
			Object config = iter.next();
			getFavorites().add(config);
			getRecents().remove(config);
		}
		updateStatus();
	}	
	
	/**
	 * The 'add favorite to recents' button has been pressed
	 */
	protected void handleMakeRecentButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection)getFavoritesTable().getSelection();
		Iterator iter = sel.iterator();
		while (iter.hasNext()) {
			Object config = iter.next();
			getRecents().add(config);
			getFavorites().remove(config);
		}
		updateStatus();
	}
	
	/**
	 * Returns the mode of this page - run or debug.
	 */
	protected String getMode() {
		return getLaunchHistory().getLaunchGroup().getMode();
	}
		
	/**
	 * Returns the initial content for the favorites list
	 */
	protected ILaunchConfiguration[] getInitialFavorites() {
		return getLaunchHistory().getFavorites();
	}
	
	/**
	 * Returns the initial content for the recent table
	 */
	protected ILaunchConfiguration[] getInitialRecents() {
		return getLaunchHistory().getHistory();
	}	
	
	/**
	 * Content provider for favorites table
	 */
	protected class FavoritesContentProvider implements IStructuredContentProvider {
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getFavorites().toArray();
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
	protected class RecentContentProvider extends FavoritesContentProvider {
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getRecents().toArray();
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
	
	/**
	 * Returns the current list of recents.
	 */
	protected List getRecents() {
		if (fRecents == null) {
			ILaunchConfiguration[] recent = getInitialRecents();
			fRecents = new ArrayList(recent.length);
			addAll(recent, fRecents);
		}
		return fRecents;
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
	 * Restores defaults
	 */
	protected void performDefaults() {
		fFavorites = null;
		fRecents = null;
		updateStatus();
	}
	
	/**
	 * Refresh all tables and buttons
	 */
	protected void updateStatus() {
		getFavoritesTable().refresh();
		getRecentTable().refresh();
		handleFavoriteSelectionChanged();
		handleRecentSelectionChanged();				
	}
	
	/**
	 * Method performOK.
	 */
	public void performOK() {
		ILaunchConfiguration[] initial = getInitialFavorites();
		List current = getFavorites();
		String groupId = getLaunchHistory().getLaunchGroup().getIdentifier();
		
		// removed favorites
		for (int i = 0; i < initial.length; i++) {
			ILaunchConfiguration configuration = initial[i];
			if (current.contains(configuration)) {
			} else {
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
				} 
			}
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
			}
		}
		 
		fLaunchHistory.setFavorites(getArray(current));		
		fLaunchHistory.setHistory(getArray(getRecents()));
	}
	
	protected ILaunchConfiguration[] getArray(List list) {
		return (ILaunchConfiguration[])list.toArray(new ILaunchConfiguration[list.size()]);
	} 
	
	protected void setImage(Image image) {
		fImage = image;
	}
	
	protected void dispose() {
		if (fImage != null) {
			fImage.dispose();
		}
	}
}
