/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.registry.*;
import org.osgi.framework.Bundle;

public class CheatSheetViewer implements ICheatSheetViewer {

	//CS Elements
	private CheatSheetElement contentElement;
	private URL contentURL;
	private String currentID;
	private int currentItemNum;
	private boolean hascontent = false;
	// Use tp indicate if an invalid cheat sheet id was specified via setInput.
	private boolean invalidCheatSheetId = false;

	private CheatSheetParser parser;
	private CheatSheet cheatSheet;
	private CheatSheetManager manager;
	private CheatSheetSaveHelper saveHelper;

	private Properties savedProps = null;

	private CheatSheetExpandRestoreAction expandRestoreAction;

	//ITEMS
	private ViewItem currentItem;

	//Lists
	private ArrayList expandRestoreList = new ArrayList();
	private ArrayList listOfContentItems;
	private ArrayList viewItemList = new ArrayList();

	//Composites
	private Composite control;

	private Cursor busyCursor;
	
	private ErrorPage errorPage;
	private CheatSheetPage cheatSheetPage;
	private Label howToBegin;

	/**
	 * The constructor.
	 */
	public CheatSheetViewer() {
		currentItemNum = -1;
		saveHelper = new CheatSheetSaveHelper();
	}

	/*package*/ void advanceIntroItem() {
		IntroItem introItem = (IntroItem) viewItemList.get(0);
		boolean isStarted = introItem.isCompleted();

		expandRestoreList = new ArrayList();
		if(expandRestoreAction != null)
			expandRestoreAction.setCollapsed(false);

		clearBackgrounds(getViewItemArray());
		clearIcons(getViewItemArray());
		collapseAllButtons(getViewItemArray());
		if(isStarted)
			killDynamicData(getViewItemArray());

		currentItemNum = 1;
		
		for (int i = 0; i < viewItemList.size(); i++) {
			ViewItem vitem = (ViewItem) viewItemList.get(i);
			if (vitem instanceof CoreItem) {
				CoreItem c = (CoreItem) vitem;
				ArrayList l = c.getListOfSubItemCompositeHolders();
				if (l != null)
					for (int j = 0; j < l.size(); j++) {
						((SubItemCompositeHolder) l.get(j)).setSkipped(false);
						((SubItemCompositeHolder) l.get(j)).setCompleted(false);
					}
			}
		}


		if (isStarted)
			getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_RESTARTED);
		else
			getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_STARTED);

		isStarted = true;
		introItem.setAsNormalCollapsed();
		introItem.setComplete();
		introItem.setRestartImage();
		/* LP-item event */
//		fireManagerItemEvent(ICheatSheetItemEvent.ITEM_DEACTIVATED, introItem);

		ViewItem nextItem = (ViewItem) viewItemList.get(1);
		if (nextItem.item.isDynamic()) {
			nextItem.handleButtons();
		}
		nextItem.setAsCurrentActiveItem();
		/* LP-item event */
//		fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, nextItem);
		collapseAllButCurrent(false);
		
//		saveHelper.removeState(contentURL.toString());
		saveCurrentSheet();


	}

	/*package*/ void advanceItem(ImageHyperlink mylabel, boolean markAsCompleted) {
		currentItem = (ViewItem) mylabel.getData();
		int index = getIndexOfItem(currentItem);

		if (index < currentItemNum) {
			ViewItem vi = getViewItemAtIndex(currentItemNum);
			vi.setAsNormalNonCollapsed();
		}
		if (currentItem != null) {
			//set that item to it's original color.
			currentItem.setAsNormalCollapsed();
			//set that item as complete.
			if (markAsCompleted) {
				currentItem.setComplete();
				/* LP-item event */
//				fireManagerItemEvent(ICheatSheetItemEvent.ITEM_COMPLETED, currentItem);
//				fireManagerItemEvent(ICheatSheetItemEvent.ITEM_DEACTIVATED, currentItem);
			} else {
				currentItem.setSkipped();
				/* LP-item event */
//				fireManagerItemEvent(ICheatSheetItemEvent.ITEM_SKIPPED, currentItem);
//				fireManagerItemEvent(ICheatSheetItemEvent.ITEM_DEACTIVATED, currentItem);
			}
		}
		if (index < viewItemList.size()) {
			ViewItem nextItem = (ViewItem) viewItemList.get(index);
			currentItemNum = index;
			if (nextItem != null) {
				//Handle lazy button instantiation here.
				if (nextItem.item.isDynamic()) {
					((CoreItem) nextItem).handleButtons();
				}
				nextItem.setAsCurrentActiveItem();
				/* LP-item event */
//				fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, nextItem);
				currentItem = nextItem;
			}

			FormToolkit.ensureVisible(currentItem.getMainItemComposite());
		} else if (index == viewItemList.size()) {
			saveCurrentSheet();
			getViewItemArray()[0].setExpanded();
			getViewItemArray()[0].getMainItemComposite().setFocus();
			getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_COMPLETED);
		}

		saveCurrentSheet();
	}

	/*package*/ void advanceSubItem(ImageHyperlink mylabel, boolean markAsCompleted, int subItemIndex) {
		Label l = null;
		ArrayList list = null;
		SubItemCompositeHolder sich = null;
		CoreItem ciws = null;

		currentItem = (ViewItem) mylabel.getData();

		if (currentItem instanceof CoreItem)
			ciws = (CoreItem) currentItem;

		if (ciws != null) {
			list = ciws.getListOfSubItemCompositeHolders();
			sich = (SubItemCompositeHolder) list.get(subItemIndex);
			l = sich.getIconLabel();
		}

		if (l != null) {
			if (markAsCompleted) {
				l.setImage(((ViewItem) ciws).getCompleteImage());
				sich.setCompleted(true);
				sich.setSkipped(false);
				/* LP-subitem event */
//				fireManagerSubItemEvent(ICheatSheetItemEvent.ITEM_COMPLETED, ciws, subItemID);
			} else {
				l.setImage(((ViewItem) ciws).getSkipImage());
				sich.setSkipped(true);
				sich.setCompleted(false);
				/* LP-subitem event */
//				fireManagerSubItemEvent(ICheatSheetItemEvent.ITEM_SKIPPED, ciws, subItemID);
			}
		}

		boolean allAttempted = checkAllAttempted(list);
		boolean anySkipped = checkContainsSkipped(list);

		if (allAttempted && !anySkipped) {
			advanceItem(mylabel, true);
			return;
		} else if (allAttempted && anySkipped) {
			advanceItem(mylabel, false);
			return;
		}

		FormToolkit.ensureVisible(currentItem.getMainItemComposite());
		saveCurrentSheet();
	}

	private void callDisposeOnViewElements() {

		ViewItem[] myitems = getViewItemArray();
		for (int i = 0; i < myitems.length; i++) {
			myitems[i].dispose();
		}

		if(errorPage != null) {
			errorPage.dispose();
		}

		if(cheatSheetPage != null) {
			cheatSheetPage.dispose();
		}
	}

	private boolean checkAllAttempted(ArrayList list) {
		for (int i = 0; i < list.size(); i++) {
			SubItemCompositeHolder s = (SubItemCompositeHolder) list.get(i);
			if (s.isCompleted() || s.isSkipped()) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean checkContainsSkipped(ArrayList list) {
		for (int i = 0; i < list.size(); i++) {
			SubItemCompositeHolder s = (SubItemCompositeHolder) list.get(i);
			if (s.isSkipped()) {
				return true;
			}
		}
		return false;
	}

	private void checkSavedState() {
		Properties props = null;
		if (savedProps == null) {
			props = saveHelper.loadState(currentID);
		} else {
			props = savedProps;
		}

		clearBackgrounds(getViewItemArray());

		if (props == null) {
			getViewItemArray()[0].setAsCurrentActiveItem();
			/* LP-item event */
//			fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, getViewItemArray()[0]);
			return;
		}

		boolean buttonIsDown = (Integer.parseInt((String) props.get(IParserTags.BUTTON)) == 0) ? false : true;
		int itemNum = Integer.parseInt((String) props.get(IParserTags.CURRENT));
		ArrayList completedStatesList = (ArrayList) props.get(IParserTags.COMPLETED);
		ArrayList expandedStatesList = (ArrayList) props.get(IParserTags.EXPANDED);
		expandRestoreList = (ArrayList) props.get(IParserTags.EXPANDRESTORE);
		String cid = (String) props.get(IParserTags.ID);
		Hashtable completedSubItems = (Hashtable) props.get(IParserTags.SUBITEMCOMPLETED);
		Hashtable skippedSubItems = (Hashtable) props.get(IParserTags.SUBITEMSKIPPED);
		Hashtable csmData = (Hashtable) props.get(IParserTags.MANAGERDATA);

		ArrayList completedSubItemsItemList = new ArrayList();
		ArrayList skippedSubItemsItemList = new ArrayList();

		Enumeration e = completedSubItems.keys();
		while (e.hasMoreElements())
			completedSubItemsItemList.add(e.nextElement());

		Enumeration e2 = skippedSubItems.keys();
		while (e2.hasMoreElements())
			skippedSubItemsItemList.add(e2.nextElement());

		if (cid != null)
			currentID = cid;

		getManager().setData(csmData);

		if (itemNum >= 0) {
			currentItemNum = itemNum;
			
			currentItem = (ViewItem) viewItemList.get(itemNum);

			for (int i = 0; i < viewItemList.size(); i++) {

				ViewItem item = (ViewItem) viewItemList.get(i);
				if (i > 0 && item.item.isDynamic() && i <= currentItemNum)
					 item.handleButtons();

				if (completedStatesList.contains(Integer.toString(i))) {
					item.setComplete();
					item.setRestartImage();
				} else {
					if (i < currentItemNum) {
						item.setSkipped();
					}
				}
				if (expandedStatesList.contains(Integer.toString(i))) {
					if (i <= currentItemNum) {
						item.setButtonsExpanded();
					} else {
						item.setButtonsCollapsed();
					}
					item.setExpanded();
				} else {
					item.setCollapsed();
					if (i > currentItemNum) {
						item.setButtonsCollapsed();
					} else {
						item.setButtonsExpanded();
					}
				}
				if (expandRestoreList.contains(Integer.toString(i))) {
					item.setCollapsed();
				}
				if (completedSubItemsItemList.contains(Integer.toString(i))) {
					String subItemNumbers = (String) completedSubItems.get(Integer.toString(i));
					StringTokenizer st = new StringTokenizer(subItemNumbers, ","); //$NON-NLS-1$
					if (item instanceof CoreItem) {
						CoreItem coreitemws = (CoreItem) item;
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							((SubItemCompositeHolder) coreitemws.getListOfSubItemCompositeHolders().get(Integer.parseInt(token))).setCompleted(true);
							((SubItemCompositeHolder) coreitemws.getListOfSubItemCompositeHolders().get(Integer.parseInt(token))).getIconLabel().setImage((item).getCompleteImage());
							ArrayList l = coreitemws.getListOfSubItemCompositeHolders();
							SubItemCompositeHolder s = (SubItemCompositeHolder) l.get(Integer.parseInt(token));
							if (s != null && s.getStartButton() != null) {
								s.getStartButton().setImage(coreitemws.restartImage);
								s.getStartButton().redraw();
							}

						}
					}
				}
				if (skippedSubItemsItemList.contains(Integer.toString(i))) {
					String subItemNumbers = (String) skippedSubItems.get(Integer.toString(i));
					StringTokenizer st = new StringTokenizer(subItemNumbers, ","); //$NON-NLS-1$
					if (item instanceof CoreItem) {
						CoreItem coreitemws = (CoreItem) item;
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							((SubItemCompositeHolder) coreitemws.getListOfSubItemCompositeHolders().get(Integer.parseInt(token))).setSkipped(true);
							((SubItemCompositeHolder) coreitemws.getListOfSubItemCompositeHolders().get(Integer.parseInt(token))).getIconLabel().setImage((item).getSkipImage());
						}
					}
				}
			}
			if (buttonIsDown) {
				if(expandRestoreAction != null)
					expandRestoreAction.setCollapsed(true);
				saveCurrentSheet();
			}
			
			// If the last item is the current one and it is complete then
			// we should collapse the last item and set the focus on intro.
			// For all other cases, set the current item as the active item.
			if(viewItemList.size()-1 == itemNum && currentItem.isCompleted()) {
				currentItem.setCollapsed();
				getViewItemArray()[0].getMainItemComposite().setFocus();
				
				// The cheat sheet has been restored but is also completed so fire both events
				getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_RESTORED);
				getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_COMPLETED);
			} else {
				currentItem.setAsCurrentActiveItem();

				// If the intro item is completed, than the cheat sheet has been restored.
				if(getViewItemArray()[0].isCompleted())
					getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_RESTORED);
			}

			/* LP-item event */
//			fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, currentItem);
		} else {
			getViewItemArray()[0].setAsCurrentActiveItem();
			/* LP-item event */
//			fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, getViewItemArray()[0]);
		}

		FormToolkit.ensureVisible(currentItem.getMainItemComposite());
		savedProps = null;
	}

	private void clearBackgrounds(ViewItem[] myview) {
		for (int i = 0; i < myview.length; i++) {
			myview[i].setBold(false);
			myview[i].setOriginalColor();
		}
	}

	private void clearIcons(ViewItem[] myview) {
		for (int i = 0; i < myview.length; i++) {
			if (myview[i].isCompleted() || myview[i].isExpanded() || myview[i].isSkipped())
				if (i > 0)
					 ((CoreItem) myview[i]).setIncomplete();
				else
					myview[i].setIncomplete();
		}
	}

	private void collapseAllButCurrent(boolean fromAction) {
		expandRestoreList = new ArrayList();
		ViewItem[] items = getViewItemArray();
		try {
			for (int i = items.length - 1; i >= 0; i--) {

				if (i != currentItemNum && items[i].isExpanded()) {
					items[i].setCollapsed();
					if (fromAction)
						expandRestoreList.add(Integer.toString(i));
				}

			}
		} catch (Exception e) {
		}
	}

	private void collapseAllButtons(ViewItem[] myview) {
		for (int i = 1; i < myview.length; i++) {
			myview[i].setButtonsCollapsed();
		}
	}

	/**
	 * Creates the SWT controls for this workbench part.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 * <p>
	 * For implementors this is a multi-step process:
	 * <ol>
	 *   <li>Create one or more controls within the parent.</li>
	 *   <li>Set the parent layout as needed.</li>
	 *   <li>Register any global actions with the <code>IActionService</code>.</li>
	 *   <li>Register any popup menus with the <code>IActionService</code>.</li>
	 *   <li>Register a selection provider with the <code>ISelectionService</code>
	 *     (optional). </li>
	 * </ol>
	 * </p>
	 *
	 * @param parent the parent control
	 */
	public void createPartControl(Composite parent) {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 1;
		control.setLayout(layout);

		control.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});

		howToBegin = new Label(control, SWT.WRAP);
		howToBegin.setText(CheatSheetPlugin.getResourceString(ICheatSheetResource.INITIAL_VIEW_DIRECTIONS));
		howToBegin.setLayoutData(new GridData(GridData.FILL_BOTH));

		Display display = parent.getDisplay();

		busyCursor = new Cursor(display, SWT.CURSOR_WAIT);

		if(contentElement != null) {
			initCheatSheetView();
		}
	}

	/**
	 * Disposes of this cheat sheet viewer.
	 */
	private void dispose() {
		if(manager != null)
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_CLOSED);

		saveCurrentSheet();

		callDisposeOnViewElements();

		if (busyCursor != null)
			busyCursor.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetViewer#getCheatSheetID()
	 */
	public String getCheatSheetID() {
		return getContent().getID();
	}

	/**
	 * Returns the current content.
	 *
	 * @return CheatSheetElement
	 */
	/*package*/ CheatSheetElement getContent() {
		return contentElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetViewer#getControl()
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Returns the hascontent.
	 * true if the cheatsheet has content loaded and displayed.
	 * @return boolean
	 */
	private boolean getHasContent() {
		return hascontent;
	}

	private int getIndexOfItem(ViewItem item) {
		duckbreak : for (int i = 1; i < viewItemList.size(); i++) {
			if (currentItem == viewItemList.get(i)) {
				int index = i + 1;
				return index;
			}
		}
		return 0;
	}

	/**
	 * @return
	 */
	/*package*/ ArrayList getListOfContentItems() {
		return listOfContentItems;
	}

	/*package*/ CheatSheetManager getManager() {
		if (manager == null) {
			manager = new CheatSheetManager(contentElement);
		}
		return manager;
	}

	/*package*/ ViewItem[] getViewItemArray() {
		return (ViewItem[]) viewItemList.toArray(new ViewItem[viewItemList.size()]);
	}

	private ViewItem getViewItemAtIndex(int index) {
		return (ViewItem) viewItemList.get(index);
	}
	
	private void initCheatSheetView() {
		//Re-initialize list to store items collapsed by expand/restore action on c.s. toolbar.
		expandRestoreList = new ArrayList();

		// re set that action to turned off.
		if(expandRestoreAction != null)
			expandRestoreAction.setCollapsed(false);

		//reset current item to be null; next item too.
		currentItem = null;
		currentItemNum = 0;
		viewItemList = new ArrayList();

		// Reset the page variables 
		errorPage = null;
		cheatSheetPage = null;
		
		if(howToBegin != null) {
			howToBegin.dispose();
			howToBegin = null;
		}
		
		// read our contents;
		// add checker here in case file could not be parsed.  No file parsed, no composite should
		// be created.
		boolean parsedOK = readFile();
		if(!parsedOK){
			// Exception thrown during parsing.
			// Something is wrong with the Cheat sheet content file at the xml level.
			if(invalidCheatSheetId) {
				errorPage = new ErrorPage(CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CHEATSHEET_DOESNOT_EXIST));
			} else {
				errorPage = new ErrorPage();
			}
			errorPage.createPart(control);
			hascontent = true;
			control.layout(true);
			return;
		}
		
		cheatSheetPage = new CheatSheetPage(cheatSheet, viewItemList, this);
		cheatSheetPage.createPart(control);
		hascontent = true;
		listOfContentItems = cheatSheet.getItems();

		getManager().fireEvent(ICheatSheetEvent.CHEATSHEET_OPENED);

		checkSavedState();

		control.layout(true);

		if (currentItem != null && !currentItem.isCompleted())
			currentItem.getMainItemComposite().setFocus();
	}
	
	private void killDynamicData(ViewItem[] myitems){
		//TODO: perhaps this would be better?
		// what to do when restarting cheatsheet vs. starting new cheatsheet?
//		getCheatsheetManager().removeAllData();
		manager = new CheatSheetManager(contentElement);
	}

	/**
	* Read the contents of the welcome page
	*/
	private boolean readFile() {
		if(parser == null)
			parser = new CheatSheetParser();
		cheatSheet = parser.parse(contentURL);
		return cheatSheet == null ? false : true;
	}

	private void restoreExpandStates() {
		ViewItem[] items = getViewItemArray();
		try {
			for (int i = 0; i < expandRestoreList.size(); i++) {
				int index = Integer.parseInt(((String) expandRestoreList.get(i)));
				if (!items[index].isExpanded()) {
					items[index].setExpanded();
				}
			}
			expandRestoreList = null;
		} catch (Exception e) {
		}
	}

	/*package*/ void runPerformAction(ImageHyperlink mylabel) {
		CoreItem mycore = null;
		mylabel.setCursor(busyCursor);
		currentItem = (ViewItem) mylabel.getData();
		mycore = (CoreItem) currentItem;

		try {
			if (mycore != null) {
				if ((mycore.runAction(getManager()) == ViewItem.VIEWITEM_ADVANCE)){// && !(mycore.isCompleted())) {
					/* LP-item event */
//					fireManagerItemEvent(ICheatSheetItemEvent.ITEM_PERFORMED, currentItem);
					mycore.setRestartImage();
					//set that item as complete.
					advanceItem(mylabel, true);
					saveCurrentSheet();
				}
			}
		} catch (RuntimeException e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_RUNNING_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_RUNNING_ACTION), null, status);
		} finally {
			mylabel.setCursor(null);
		}
	}

	/*package*/ void runSubItemPerformAction(ImageHyperlink mylabel, int subItemIndex) {
		CoreItem coreItem = null;
		mylabel.setCursor(busyCursor);
		currentItem = (ViewItem) mylabel.getData();
		coreItem = (CoreItem) currentItem;

		try {
			if (coreItem != null) {
				if (coreItem.runSubItemAction(getManager(), subItemIndex) == ViewItem.VIEWITEM_ADVANCE) {
					ArrayList l = coreItem.getListOfSubItemCompositeHolders();
					SubItemCompositeHolder s = (SubItemCompositeHolder) l.get(subItemIndex);
					s.getStartButton().setImage(coreItem.restartImage);
					advanceSubItem(mylabel, true, subItemIndex);
					saveCurrentSheet();
				}
			}
		} catch (RuntimeException e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_RUNNING_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_RUNNING_ACTION), null, status);
		} finally {
			mylabel.setCursor(null);
		}
	}

	/*package*/ void saveCurrentSheet() {
		if(currentID != null) {
			boolean expandRestoreActionState = false;
			if(expandRestoreAction != null)
				expandRestoreActionState = expandRestoreAction.isCollapsed();			
			saveHelper.saveState(currentItemNum, getViewItemArray(), expandRestoreActionState, expandRestoreList, currentID, getManager());
		}
	}

	private void setContent(CheatSheetElement element) {

		if (element != null && element.equals(contentElement))
			return;

		if (contentURL != null)
			saveCurrentSheet();

		// Cleanup previous contents
		if (getHasContent()) {
			callDisposeOnViewElements();
		}

		// Set the current content to new content
		this.contentElement = element;
		if (element == null) {
			currentID = null;
			this.contentURL = null;
		} else {
			currentID = element.getID();
	
			Bundle bundle = null;
			if(element != null)
				try{
					String pluginId = element.getConfigurationElement().getDeclaringExtension().getNamespace();
					bundle = Platform.getBundle(pluginId);
				} catch (Exception e) {
					// do nothing
				}
			if (bundle != null) {
				this.contentURL = Platform.find(bundle, new Path("$nl$").append(element.getContentFile())); //$NON-NLS-1$
			}
	
			if (contentURL == null) {
				URL checker;
				try {
					checker = new URL(element.getContentFile());
					if (checker.getProtocol().equals("http")) { //$NON-NLS-1$
						this.contentURL = checker;
					}
				} catch (MalformedURLException mue) {
				}
			}
		}

		// Initialize the view with the new contents
		if (control != null) {
			initCheatSheetView();
		}
	}
	
	/*package*/ void setExpandRestoreAction(CheatSheetExpandRestoreAction action) {
		expandRestoreAction = action;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//need this to have current item selected. (Assumes that when you reactivate the view you will work with current item.)
		if (currentItem != null)
			currentItem.getMainItemComposite().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetViewer#setInput(java.lang.String)
	 */
	public void setInput(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		CheatSheetElement element = CheatSheetRegistryReader.getInstance().findCheatSheet(id);
		if(element == null) {
			String message = CheatSheetPlugin.formatResourceString(ICheatSheetResource.ERROR_INVALID_CHEATSHEET_ID, new Object[] {id});
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, null);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			invalidCheatSheetId = true;
		} else {
			invalidCheatSheetId = false;
		}
		setContent(element);

		// Update most recently used cheat sheets list.
		CheatSheetPlugin.getPlugin().getCheatSheetHistory().add(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetViewer#setInput(java.lang.String, java.lang.String, java.net.URL)
	 */
	public void setInput(String id, String name, URL url) {
		if (id == null || name == null || url == null) {
			throw new IllegalArgumentException();
		}
		CheatSheetElement element = new CheatSheetElement(name);
		element.setID(id);
		element.setContentFile(url.toString());

		invalidCheatSheetId = false;
		setContent(element);
	}


	/*package*/ void toggleExpandRestore() {
		if(expandRestoreAction == null)
			return;

		if (expandRestoreAction.isCollapsed()) {
			restoreExpandStates();
			expandRestoreAction.setCollapsed(false);
			saveCurrentSheet();
		} else {
			collapseAllButCurrent(true);
			expandRestoreAction.setCollapsed(true);
			saveCurrentSheet();
		}

	}
}
