/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The FilteredTextTree is a filtered tree that uses an editable text.
 */
public class FilteredTextTree extends FilteredTree {
	// A list contains all strings in search history
	private List searchHistory;
	
	//A popup shell to hold the currentSeachTable 
	private Shell popupShell;

	// A table which contains only strings begin with typed strings
	private Table currentSeachTable;
	
	//The minimum width for popupShell to make sure to show all
	//text in the horizonal space 
    private int minPopupShellWidth;
    
    //Identify whether or not the text area was resized.
    private boolean resizedFlag = false;
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 * @param treeStyle
	 */
	public FilteredTextTree(Composite parent, int treeStyle) {
		super(parent, treeStyle);
	}

	/**
	 * Create a new instance of the receiver with a supplied filter.
	 * 
	 * @param parent
	 * @param treeStyle
	 * @param filter
	 * @param searchKey 
	 */
	public FilteredTextTree(Composite parent, int treeStyle,
			PatternItemFilter filter, String searchKey) {
		super(parent, treeStyle, filter);
		searchHistory = getPreferenceSearchHistory(searchKey);
        treeViewer.getControl().addFocusListener(new FocusAdapter(){
			/* Each time the tree gains focus, the current text in text area is saved as search history
			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				String newText = filterText.getText();				
				Object[] textValues = searchHistory.toArray();
				
				if((newText.equals(""))||(newText.equals(initialText)))//$NON-NLS-1$
					return;
		
				for (int i = 0; i < textValues.length; i++) {
					if(((String)textValues[i]).equals(newText))
						return;					
				}	
				searchHistory.add(newText);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.FilteredTree#createFilterControl(org.eclipse.swt.widgets.Composite)
	 */
	protected void createFilterControl(final Composite parent) {
		filterText = new Text(parent, SWT.BORDER | SWT.MULTI);
		filterText.setFont(parent.getFont());		
		
		popupShell = new Shell(parent.getShell(), SWT.NO_TRIM);
		popupShell
				.setBackground(parent.getDisplay().getSystemColor(
						SWT.COLOR_WHITE));
		GridLayout shellGL = new GridLayout();
		shellGL.marginHeight = 0;
		shellGL.marginWidth = 0;
		popupShell.setLayout(shellGL);
		popupShell.setLayoutData(new GridData(
				(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL)));

		currentSeachTable = new Table(popupShell, SWT.SINGLE | SWT.BORDER);
		currentSeachTable.setLayoutData(new GridData(
				(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL)));
		
		//Make sure the popup shell show whole words without scrollable horizontally
		Font font = parent.getFont();	
		currentSeachTable.setFont(new Font
						(parent.getDisplay(),font.getFontData()[0].getName(),
						 font.getFontData()[0].getHeight()-1,font.getFontData()[0].getStyle()));
		
		filterText.addTraverseListener(new TraverseListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
			 */
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
					popupShell.setVisible(false);
					if (getViewer().getTree().getItemCount() == 0) {
						Display.getCurrent().beep();
						setFilterText(""); //$NON-NLS-1$
					} else {
						getViewer().getTree().setFocus();
					}
				}
			}
		});
		
				
		filterText.addFocusListener(new FocusAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				filterText.selectAll();
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
				filterText.setSelection(0,0);
			}
		});
		
		
		popupShell.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if ((e.detail == SWT.TRAVERSE_RETURN)||(e.detail == SWT.TRAVERSE_ESCAPE)) {
					e.doit = false;
					if(e.detail == SWT.TRAVERSE_RETURN){
						setFilterText(currentSeachTable.getSelection()[0].getText());
						textChanged();
					}
					popupShell.setVisible(false);
					getViewer().getTree().setFocus();
				}
			}
		});

		currentSeachTable.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				setFilterText(currentSeachTable.getSelection()[0].getText()); //$NON-NLS-1$
				textChanged();
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {

				if (e.keyCode == SWT.ARROW_DOWN) {
					if (currentSeachTable.isVisible()) {
						// Make selection at popup table
						if (currentSeachTable.getSelectionCount() < 1){
							currentSeachTable.setSelection(0);
							setFilterText(currentSeachTable.getSelection()[0].getText()); //$NON-NLS-1$
							textChanged();
						}
						currentSeachTable.setFocus();
					} else if(getViewer().getTree().getItemCount() > 0)						 	
							//Make selection be on the left tree
							treeViewer.getTree().setFocus();
				} else {
					textChanged();
					List result = new ArrayList();
					result = reduceSearch(searchHistory, filterText.getText());
					updateTable(currentSeachTable, result);

					if (currentSeachTable.getItemCount() > 0) {
						Rectangle textBounds = filterText.getBounds();
												
						setShellLocationAndSize(parent,textBounds);

						if (popupShell.isDisposed())
							popupShell.open();

						if (!popupShell.getVisible()) {
							popupShell.setVisible(true);
							filterText.setFocus();
						}

					} else
						popupShell.setVisible(false);
				}

			}
		});

		parent.getDisplay().addFilter(SWT.MouseDown, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Events)
			 */
			public void handleEvent(Event event) {
				if (!popupShell.isDisposed())
					popupShell.setVisible(false);
			}
		});

		getShell().addControlListener(new ControlAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlMoved(ControlEvent e) {
				popupShell.setVisible(false);
			}
		});
		
		filterText.addControlListener(new ControlAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlResized(ControlEvent e) {
				int initialTextWidth = filterText.getBounds().width;
				if((initialTextWidth >0)&& (resizedFlag ==false)){
					minPopupShellWidth=initialTextWidth;
					resizedFlag = true;
				}
			}
		});
		
		filterText.getAccessible().addAccessibleListener(
				getAccessibleListener());

	}

	/**
	 * Find all items which start with typed words list the list contains all
	 * strings of the search history
	 * 
	 * @param list
	 *            the list to search
	 * @param wordsEntered
	 *            String
	 * @return a list in which all strings start from the typed letter(s)
	 */
	private List reduceSearch(List list, String wordsEntered) {
		List result = new ArrayList();
		if (list == null)
			return result;
		for (int i = 0; i < list.size(); i++) {
			if (filterText.getText() == "") //$NON-NLS-1$
						return result;
		            String historyString = (String) list.get(i);
				    String typedString = wordsEntered;
				    if (historyString.toLowerCase().startsWith(typedString.toLowerCase()))
					    result.add(historyString);
				}
		
				return result;
		 	}
     /**
	 * Calculate and set the position and size of the popup shell
	 * @param parent
	 * @param textBounds
	 */
	private void setShellLocationAndSize(Composite parent, Rectangle  textBounds){
		
		//Caculate size of the popup shell
		int space = currentSeachTable.getItemHeight();
		int tableHeight = currentSeachTable
			    .getItemHeight()* currentSeachTable.getItemCount() + space;
		int tableWidth = textBounds.width;	
		
		//Make sure the width of popupShell be at least minPopupShellWidth
		if(tableWidth <= minPopupShellWidth)
			popupShell.setSize(minPopupShellWidth,tableHeight);
		else
		    popupShell.setSize(tableWidth,tableHeight);
	   	    	
		//Caculate x,y coordinator of the popup shell
		Point point = getDisplay().map(parent, null,
				textBounds.x, textBounds.y);	
		final int xCoord = point.x;
		final int yCoord = point.y + textBounds.height;
		
		final Point location = new Point(xCoord, yCoord);
		
		//Try to show whole popup shell through relocating its x and y coordinator
		final Display display = popupShell.getDisplay();		
		final Rectangle displayBounds = display.getClientArea();
		Rectangle popupShellBounds = popupShell.getBounds();
		final int displayRightEdge = displayBounds.x + displayBounds.width;
		
		if (location.x <0) 
			location.x = 0;
		if ((location.x + popupShellBounds.width) > displayRightEdge) 
			location.x = displayRightEdge - popupShellBounds.width;
		
		final int displayBottomEdge = displayBounds.y + displayBounds.height;	
		if ((location.y + tableHeight) > displayBottomEdge)
			location.y = displayBottomEdge - tableHeight;
		
		// Set the location.
		popupShell.setLocation(location);		
	}

	/**
	 * Copy all elements from a list to a table
	 * 
	 * @param table
	 * @param list
	 */
	private void updateTable(Table table, List list) {
		table.removeAll();
		if (list.size() > 0) {
			TableItem newItem;
			for (int i = 0; i < list.size(); i++) {
				newItem = new TableItem(table, SWT.NULL, i);
				newItem.setText((String) list.get(i));

			}
		}

	}

	/**
	 * Return a dialog setting section for this dialog
	 * 
	 * @return IDialogSettings
	 */
	IDialogSettings getDialogSettings(){
		IDialogSettings settings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings thisSettings = settings
				.getSection(getClass().getName());
		if (thisSettings == null)
			thisSettings = settings.addNewSection(getClass().getName());

		return thisSettings;
	}

	/**
	 * Get the preferences search history for this eclipse's start, Note that
	 * this history will not be cleared until this eclipse closes
	 * 
	 * @return a list
	 */
	private List getPreferenceSearchHistory(String key) {

		List searchList = new ArrayList();
		IDialogSettings settings = getDialogSettings();
		String[] search = settings.getArray(key);

		if (search != null) {
			for (int i = 0; i < search.length; i++)
				searchList.add(search[i]);
		}
		return searchList;

	}
	
	/**Set the preference value matching searchkey
	 * @param settings
	 * @param searchKey
	 */
	public void setPreferenceSearchHistory(IDialogSettings settings, String searchKey) { 		
		String[] search = settings.getArray(searchKey); //$NON-NLS-1$ 
 		if (search != null) {
 			for (int i = 0; i < search.length; i++)
 				searchHistory.add(search[i]);
 		}
	}

	/**
	 * Saves the search history.
	 * @param settings 
	 * @param key 
	 */
	public void saveDialogSettings(IDialogSettings settings,String key){
		 
		// If the settings contains the same key, the previous value will be
		// replaced by new one
		String[] result = new String[searchHistory.size()];
		listToArray(searchHistory, result);
		settings.put(key, result);

	}

	private void listToArray(List list, String[] string) {
		int size = list.size();
		for (int i = 0; i < size; i++)
			string[i] = (String) list.get(i);
	}

}
