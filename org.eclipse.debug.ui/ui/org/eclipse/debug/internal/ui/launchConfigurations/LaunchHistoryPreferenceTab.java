package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import sun.security.action.GetLongAction;

/**
 * Tab for favorite and recent history lists
 */
public abstract class LaunchHistoryPreferenceTab {
		
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
	private Button fRemoveFavoritesButton;
	private Button fMoveUpButton;
	private Button fMoveDownButton;
	
	/**
	 * Recent Buttons
	 */
	private Button fAddToFavoritesButton;
	private Button fRemoveRecentButton;
	
	/**
	 * Current collection of favorites and recent launch configs
	 */
	private List fFavorites;
	private List fRecents;
	
	/**
	 * Creates the control for this tab
	 */
	protected Control createControl(Composite parent) {
		Composite topComp = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		topComp.setLayout(layout);
		GridData gd;
	
		Label favoritesLabel = new Label(topComp, SWT.LEFT);
		favoritesLabel.setText(getFavoritesLabel());
		gd = new GridData();
		gd.horizontalSpan = 2;
		favoritesLabel.setLayoutData(gd);
	
		setFavoritesTable(new TableViewer(topComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION));
		getFavoritesTable().setContentProvider(new FavoritesContentProvider());
		getFavoritesTable().setLabelProvider(DebugUITools.newDebugModelPresentation());
		getFavoritesTable().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				handleFavoriteSelectionChanged();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		getFavoritesTable().getTable().setLayoutData(gd);
		getFavoritesTable().setInput(DebugUIPlugin.getDefault());
		
		Composite buttonComp = new Composite(topComp, SWT.NONE);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComp.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 1;
		buttonComp.setLayout(layout);
		
		Button addFav = new Button(buttonComp, SWT.PUSH);
		addFav.setText("Add &Config...");
		addFav.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleAddFavoriteButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		addFav.setLayoutData(gd);
		addFav.setEnabled(DebugUIPlugin.getDefault().usingConfigurationStyleLaunching());		
		
		fRemoveFavoritesButton = new Button(buttonComp, SWT.PUSH);
		fRemoveFavoritesButton.setText("Re&move");
		fRemoveFavoritesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleRemoveFavoriteButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		fRemoveFavoritesButton.setLayoutData(gd);
		fRemoveFavoritesButton.setEnabled(false);
		
		fMoveUpButton = new Button(buttonComp, SWT.PUSH);
		fMoveUpButton.setText("U&p");
		fMoveUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleMoveUpButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		fMoveUpButton.setLayoutData(gd);
		fMoveUpButton.setEnabled(false);
		
		fMoveDownButton = new Button(buttonComp, SWT.PUSH);
		fMoveDownButton.setText("Do&wn");
		fMoveDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleMoveDownButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		fMoveDownButton.setLayoutData(gd);
		fMoveDownButton.setEnabled(false);					
	
		createSpacer(topComp, 1);
		createSpacer(topComp, 2);
	
		Label recent = new Label(topComp, SWT.LEFT);
		recent.setText(getRecentLabel());
		gd = new GridData();
		gd.horizontalSpan = 2;
		recent.setLayoutData(gd);
	
		setRecentTable(new TableViewer(topComp, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION));
		getRecentTable().setContentProvider(new RecentContentProvider());
		getRecentTable().setLabelProvider(DebugUITools.newDebugModelPresentation());
		getRecentTable().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				handleRecentSelectionChanged();
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		getRecentTable().getTable().setLayoutData(gd);
		getRecentTable().setInput(DebugUIPlugin.getDefault());
		
		buttonComp = new Composite(topComp, SWT.NONE);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonComp.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 1;
		buttonComp.setLayout(layout);
		
		fAddToFavoritesButton = new Button(buttonComp, SWT.PUSH);
		fAddToFavoritesButton.setText("Make &Favorite");
		fAddToFavoritesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleMakeFavoriteButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		fAddToFavoritesButton.setLayoutData(gd);
		fAddToFavoritesButton.setEnabled(false);
		
		fRemoveRecentButton = new Button(buttonComp, SWT.PUSH);
		fRemoveRecentButton.setText("Remo&ve");
		fRemoveRecentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleRemoveRecentButtonSelected();
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		fRemoveRecentButton.setLayoutData(gd);
		fRemoveRecentButton.setEnabled(false);				
				
		return topComp;
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
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
	 * Sets the table of favorite launch configurations.
	 * 
	 * @param favoritesTable table viewer
	 */
	private void setFavoritesTable(TableViewer favoritesTable) {
		fFavoritesTable = favoritesTable;
	}

	/**
	 * The selection in the favorites list has changed
	 */
	protected void handleFavoriteSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection)getFavoritesTable().getSelection();
		List favs = getFavorites();
		boolean notEmpty = !selection.isEmpty();
		boolean single = selection.size() == 1;
		boolean first = single && (favs.indexOf(selection.getFirstElement()) == 0);
		boolean last = single && (favs.indexOf(selection.getFirstElement()) == (favs.size() - 1));
		
		fRemoveFavoritesButton.setEnabled(notEmpty);
		fMoveUpButton.setEnabled(single && !first);
		fMoveDownButton.setEnabled(single && !last);
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
	 * Sets the table of recent launch configurations.
	 * 
	 * @param table table viewer
	 */
	private void setRecentTable(TableViewer table) {
		fRecentTable = table;
	}

	/**
	 * The selection in the recent list has changed
	 */
	protected void handleRecentSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection)getRecentTable().getSelection();
		boolean notEmpty = !selection.isEmpty();
		
		fRemoveRecentButton.setEnabled(notEmpty);
		fAddToFavoritesButton.setEnabled(notEmpty);
	}	
	
	/**
	 * The 'add favorites' button has been pressed
	 */
	protected void handleAddFavoriteButtonSelected() {
		
		
		ListSelectionDialog dialog = new ListSelectionDialog(fFavoritesTable.getControl().getShell(),
		 getMode(), new LaunchConfigurationContentProvider(), DebugUITools.newDebugModelPresentation(),
		 "Select Launch Configurations");
		dialog.open();
		Object[] selection = dialog.getResult();
		if (selection != null) {
			for (int i = 0; i < selection.length; i++) {
				getFavorites().add(selection[i]);
				getRecents().remove(selection[i]);
			}
		}
		updateStatus();
	}	
	
	/**
	 * The 'remove favorites' button has been pressed
	 */
	protected void handleRemoveFavoriteButtonSelected() {
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
		IStructuredSelection sel = (IStructuredSelection)getFavoritesTable().getSelection();
		Object config = sel.getFirstElement();
		int index = getFavorites().indexOf(config);
		getFavorites().remove(config);
		getFavorites().add(index - 1,config);
		getFavoritesTable().refresh();	
		handleFavoriteSelectionChanged();	
	}	
	
	/**
	 * The 'move up' button has been pressed
	 */
	protected void handleMoveDownButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection)getFavoritesTable().getSelection();
		Object config = sel.getFirstElement();
		int index = getFavorites().indexOf(config);
		getFavorites().remove(config);
		getFavorites().add(index + 1,config);
		getFavoritesTable().refresh();			
		handleFavoriteSelectionChanged();
	}	
	
	/**
	 * The 'remove recent' button has been pressed
	 */
	protected void handleRemoveRecentButtonSelected() {
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
		getFavoritesTable().refresh();
		getRecentTable().refresh();
	}		

	/**
	 * Returns the mode of this page - run or debug.
	 */
	protected abstract String getMode();
		
	/**
	 * Returns the label for the favorites table.
	 */
	protected abstract String getFavoritesLabel();
		
	/**
	 * Returns the initial content for the favorites list
	 */
	protected abstract ILaunchConfiguration[] getInitialFavorites();
	
	/**
	 * Returns the label for the recent launch table.
	 */
	protected abstract String getRecentLabel();	
	
	/**
	 * Returns the initial content for the recent table
	 */
	protected abstract ILaunchConfiguration[] getInitialRecents();	
	
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
				DebugUIPlugin.log(e.getStatus());
				return new ILaunchConfiguration[0];
			}
			List list = new ArrayList(all.length);
			String mode = (String)inputElement;
			for (int i = 0; i < all.length; i++) {
				try {
					if (all[i].getType().supportsMode(mode)) {
						list.add(all[i]);
					}
				} catch (CoreException e) {
					// ignore
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
}
