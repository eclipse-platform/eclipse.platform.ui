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
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.actions.CheatSheetMenu;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;

/**
 * View for displaying a cheatsheet.
 * Cheatsheet content is plugged into the workspace using the following 
 * extension point:
 * <extension point="org.eclipse.ui.cheatsheets.cheatSheetContent">
 *		<category
 *           name="Cheat Sheet Category"
 *           id="org.eclipse.ui.cheatsheets.cheatSheetsCat">
 *       </category>
 *       <cheatSheet
 *           name="Sample cheat Sheet"
 *           category="org.eclipse.ui.cheatsheets.cheatSheetsCat"
 *           id="org.eclipse.ui.cheatsheets.samples.sampleSheet"
 *           contentFile="testCheatSheet.xml">
 *        <description>
 *           This is the description of the open cheat sheet.
 *        </description>
 *     </cheatSheet>
 *</extension>
 * 
 */

public class CheatSheetView extends ViewPart {

	//booleans
	private boolean actionBarContributed = false;
	private boolean allCollapsed = false;

	//CS Elements
	private CheatSheetElement contentElement;
	private URL contentURL;
	private float csversion = 1.0f;
	private String currentID;
	private int currentItemNum;
	private boolean hascontent = false;
	private boolean isStarted = false;

	private CheatSheetDomParser parser;
	private CheatSheetManager manager;
	private CheatSheetSaveHelper saveHelper;

	private CheatSheetExpandRestoreAction hideFields;

	private Properties savedProps = null;

	private IMemento memento;

	//ITEMS
	private ViewItem currentItem;
	private ViewItem nextItem;

	//Lists
	private ArrayList expandRestoreList = new ArrayList();
	private ArrayList listOfContentItems;
	private ArrayList viewItemList = new ArrayList();

	//Composites
	private Composite parent;

	private Cursor busyCursor;
	
	private ErrorPage errorPage;
	private CheatSheetPage cheatSheetPage;

	/**
	 * The constructor.
	 */
	public CheatSheetView() {
		setTitle(CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEETS));
		currentItemNum = -1;
		saveHelper = new CheatSheetSaveHelper();
	}

	/*package*/ void advanceIntroItem() {
		expandRestoreList = new ArrayList();
		hideFields.setChecked(false);
		allCollapsed = false;
		hideFields.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP));

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
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_RESTARTED);
		else
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_STARTED);

		isStarted = true;
		IntroItem introItem = (IntroItem) viewItemList.get(0);
		introItem.setAsNormalCollapsed();
		introItem.setComplete();
		introItem.setRestartImage();
		/* LP-item event */
//		fireManagerItemEvent(ICheatSheetItemEvent.ITEM_DEACTIVATED, introItem);

		ViewItem nextItem = (ViewItem) viewItemList.get(1);
		if (nextItem.contentItem.isDynamic()) {
							((CoreItem) nextItem).handleLazyButtons();
		}
		nextItem.setAsCurrentActiveItem();
		/* LP-item event */
//		fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, nextItem);
		collapseAllButCurrent(false);
		
		saveHelper.removeState(contentURL.toString());
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
			nextItem = (ViewItem) viewItemList.get(index);
			currentItemNum = index;
			if (nextItem != null) {
				//Handle lazy button instantiation here.
				if (nextItem.contentItem.isDynamic()) {
					((CoreItem) nextItem).handleLazyButtons();
				}
				nextItem.setAsCurrentActiveItem();
				/* LP-item event */
//				fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, nextItem);
				currentItem = nextItem;
			}

			scrollIfNeeded();
		} else if (index == viewItemList.size()) {
			saveCurrentSheet();
			getViewItemArray()[0].setExpanded();
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_COMPLETED);
		}

		saveCurrentSheet();
	}

	/*package*/ void advanceSubItem(ImageHyperlink mylabel, boolean markAsCompleted, int subItemIndex) {
		//		System.out.println("Advancing a sub item!! Item Number: " + subItemIndex);
		String subItemID = null;
		Label l = null;
		ArrayList list = null;
		SubItemCompositeHolder sich = null;
		CoreItem ciws = null;

		currentItem = (ViewItem) mylabel.getData();

		if (currentItem instanceof CoreItem)
			ciws = (CoreItem) currentItem;

		if (ciws != null) {
			IContainsContent ci = ((ViewItem) ciws).contentItem;
			if (ci instanceof ItemWithSubItems)
				subItemID = ((ItemWithSubItems) ci).getSubItem(subItemIndex).getID();
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

		scrollIfNeeded();
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

	private void checkDynamicModel() {
		Properties props = saveHelper.getSavedStateProperties(contentURL, csversion);
		if (props == null)
			return;

		savedProps = props;

		ArrayList dynamicItemDataList = null; //TODO (ArrayList) props.get(IParserTags.DYNAMICDATA);
		ArrayList dynamicSubItemDataList = null; //TODO (ArrayList) props.get(IParserTags.DYNAMICSUBITEMDATA);

		if (dynamicItemDataList != null)
			for (int i = 0; i < dynamicItemDataList.size(); i++) {
				Properties p = (Properties) dynamicItemDataList.get(i);
				String itemid = (String) p.get(IParserTags.ITEM);
/* TODO: Remove this! */
//				String buttonCodes = (String) p.get(IParserTags.ACTIONPHRASE);
				String aclass = (String) p.get(IParserTags.CLASS);
				String actionpid = (String) p.get(IParserTags.PLUGINID);
				String[] actionParams = (String[]) p.get(IParserTags.ACTIONPARAM);
				AbstractItem abItem = getItemWithID(itemid);
				if (abItem == null) {
					continue;
				} else {
					if (abItem instanceof Item) {
						Item c = (Item) abItem;
						if (c.isDynamic()) {
							c.setActionClass(aclass);
							c.setActionPluginID(actionpid);
							c.setActionParams(actionParams);
/* TODO: Remove this! */
//							c.setButtonCodes(buttonCodes);
						}
					}
				}
			}

		//Re-Set the dynamic item sub item data if there was any stored.
		if (dynamicSubItemDataList != null)
			for (int i = 0; i < dynamicSubItemDataList.size(); i++) {
				Properties p = (Properties) dynamicSubItemDataList.get(i);
				String itemid = (String) p.get(IParserTags.ITEM);
				String subitemid = (String) p.get(IParserTags.SUBITEM);
/* TODO: Remove this! */				
//				String buttonCodes = (String) p.get(IParserTags.ACTIONPHRASE);
				String aclass = (String) p.get(IParserTags.CLASS);
				String actionpid = (String) p.get(IParserTags.PLUGINID);
				String sublabel = (String) p.get(IParserTags.SUBITEMLABEL);
				String[] actionParams = (String[]) p.get(IParserTags.ACTIONPARAM);
				AbstractItem abItem = getItemWithID(itemid);
				if (abItem == null) {
					continue;
				} else {
					if (abItem instanceof Item) {
						Item c = (Item) abItem;
						if (c.isDynamic()) {
							ItemWithSubItems ciws = convertThisIItem(c);
							replaceThisContentItem(c, ciws);
/* TODO: Remove this! */
//							SubItem subItem = createASubItem(subitemid, buttonCodes, actionpid, aclass, actionParams, sublabel);
//							ciws.addSubItem(subItem);
						}
					} else if (abItem instanceof ItemWithSubItems) {
						boolean handled = false;
						ItemWithSubItems c = (ItemWithSubItems) abItem;
						if (c.isDynamic()) {
							SubItem[] subs = c.getSubItems();
							sublabel : for (int j = 0; j < subs.length; j++) {
								SubItem s = subs[j];
								if (s.getID().equals(subitemid)) {
									s.setActionClass(aclass);
									s.setActionPluginID(actionpid);
									s.setActionParams(actionParams);
/* TODO: Remove this! */
//									s.setButtonCodes(buttonCodes);
									s.setLabel(sublabel);
									handled = true;
									break sublabel;
								}
							}
							if (!handled) {
/* TODO: Remove this! */
//								SubItem subItem = createASubItem(subitemid, buttonCodes, actionpid, aclass, actionParams, sublabel);
//								c.addSubItem(subItem);
								handled = true;
							}
						}
					}
				}
			}

	}

	private void checkSavedState() {
		Properties props = null;
		if (savedProps == null) {
			props = saveHelper.getSavedStateProperties(contentURL, csversion);
		} else {
			props = savedProps;
		}
//TODO: moved this down further
//		manager = new CheatSheetManager(currentID, this);
		if (props == null) {
			manager = new CheatSheetManager(currentID, this);
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
		String cid = (String) props.get(IParserTags.CHEATSHEETID);
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

		manager = new CheatSheetManager(currentID, this);
		manager.setData(csmData);

		if (itemNum >= 0) {
			clearBackgrounds(getViewItemArray());
			currentItemNum = itemNum;
			
			currentItem = (ViewItem) viewItemList.get(itemNum);

			for (int i = 0; i < viewItemList.size(); i++) {

				ViewItem item = (ViewItem) viewItemList.get(i);
				if (i > 0 && ((CoreItem) item).contentItem.isDynamic() && i <= currentItemNum)
					 ((CoreItem) item).handleLazyButtons();

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
							if (s != null) {
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
				allCollapsed = true;
				hideFields.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTORE_ALL_TOOLTIP));
				saveCurrentSheet();
				hideFields.setChecked(true);
			}
			currentItem.setAsCurrentActiveItem();
			/* LP-item event */
//			fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, currentItem);
		} else {
			getViewItemArray()[0].setAsCurrentActiveItem();
			/* LP-item event */
//			fireManagerItemEvent(ICheatSheetItemEvent.ITEM_ACTIVATED, getViewItemArray()[0]);
		}

		scrollIfNeeded();
		savedProps = null;
	}

	private void clearBackgrounds(ViewItem[] myview) {
		for (int i = 0; i < myview.length; i++) {
			myview[i].setOriginalColor();
			myview[i].unboldTitle();
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

	private void contributeToActionBars() {
		//here you have to assemble the same list as the list added to the help menu bar.
		//so an external class should do it so it can be shared with something that
		//both these classes can use.  I will call it CheatSheetActionGetter.
		//	System.out.println("Inside of contribute to action bars!!!!");

		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menuManager = bars.getMenuManager();
		IToolBarManager tbmanager = bars.getToolBarManager();

		// fields
		IPluginDescriptor mydesc = CheatSheetPlugin.getPlugin().getDescriptor();
		String skipfileName = "icons/full/elcl16/collapse_expand_all.gif"; //$NON-NLS-1$
		URL skipurl = mydesc.find(new Path(skipfileName));
		ImageDescriptor skipTask = ImageDescriptor.createFromURL(skipurl);

		hideFields = new CheatSheetExpandRestoreAction(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP), false, this);
		hideFields.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP));
		hideFields.setImageDescriptor(skipTask);
		tbmanager.add(hideFields);

		CheatSheetMenu cheatsheetMenuMenuItem = new CheatSheetMenu();
		menuManager.add(cheatsheetMenuMenuItem);
	}

	private ItemWithSubItems convertThisIItem(Item item) {
		Item cc = (Item) item;
		ItemWithSubItems itemws = new ItemWithSubItems();
		itemws.setContent(cc.getContent());
		itemws.setID(cc.getID());
		return itemws;
	}

	private SubItem createASubItem(String subid, String actionCodes, String actionPID, String actionClass, String[] params, String label) {
		SubItem subItem = new SubItem();
		subItem.setActionClass(actionClass);
/* TODO: Remove this! */
//		subItem.setButtonCodes(actionCodes);
		subItem.setActionParams(params);
		subItem.setLabel(label);
		subItem.setActionPluginID(actionPID);
		subItem.setID(subid);
		return subItem;
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
		this.parent = parent;
		Display display = parent.getDisplay();

		busyCursor = new Cursor(display, SWT.CURSOR_WAIT);

		if (!actionBarContributed) {
			contributeToActionBars();
			actionBarContributed = true;
		}
		if (memento != null) {
			restoreState(memento);
			initCheatSheetView();
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if(manager != null)
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_CLOSED);

		super.dispose();

		saveCurrentSheet();

		callDisposeOnViewElements();

		if (busyCursor != null)
			busyCursor.dispose();

		if (parent != null)
			parent.dispose();

	}

	private CheatSheetManager getCheatsheetManager() {
		if (manager == null) {
			manager = new CheatSheetManager(currentID, this);
		}
		return manager;
	}

	/**
	 * Returns the current content.
	 *
	 * @return CheatSheetElement
	 */
	public CheatSheetElement getContent() {
		return contentElement;
	}

	/**
	 * @return
	 */
	public float getCsversion() {
		return csversion;
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

	private AbstractItem getItemWithID(String id) {
		try {
			//Check to see if that item with that id is dynamic.
			//If it is not dynamic, return null for it cannot be modified.
			ArrayList contentItems = parser.getItems();
			for (int i = 0; i < contentItems.size(); i++) {
				AbstractItem contentItem = (AbstractItem) contentItems.get(i);
				if (contentItem.getID().equals(id)) {
					//return contentItem;
					if (contentItem instanceof IContainsContent) {
						IContainsContent cc = (IContainsContent) contentItem;
						if (cc.isDynamic())
							return contentItem;
					}
					return null;
				}

			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return
	 */
	/*package*/ ArrayList getListOfContentItems() {
		return listOfContentItems;
	}

	/*package*/ ViewItem[] getViewItemArray() {
		return (ViewItem[]) viewItemList.toArray(new ViewItem[viewItemList.size()]);
	}

	private ViewItem getViewItemAtIndex(int index) {
		return (ViewItem) viewItemList.get(index);
	}

	/* (non-Javadoc)
	 * Initializes this view with the given view site.  A memento is passed to
	 * the view which contains a snapshot of the views state from a previous
	 * session.  Where possible, the view should try to recreate that state
	 * within the part controls.
	 * <p>
	 * This implementation will ignore the memento and initialize the view in
	 * a fresh state.  Subclasses may override the implementation to perform any
	 * state restoration as needed.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}
	
	private void initCheatSheetView() {

		//Re-initialize list to store items collapsed by expand/restore action on c.s. toolbar.
		expandRestoreList = new ArrayList();

		// re set that action to turned off.
		hideFields.setChecked(false);
		allCollapsed = false;
		hideFields.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP));

		//reset current item to be null; next item too.
		currentItem = null;
		nextItem = null;

		currentItemNum = -1;
		viewItemList = new ArrayList();

		// read our contents;
		// add checker here in case file could not be parsed.  No file parsed, no composite should
		// be created.
		boolean parsedOK = readFile();
		if(!parsedOK){
			// Exception thrown during parsing.
			// Something is wrong with the Cheat sheet content file at the xml level.
			errorPage = new ErrorPage();
			errorPage.createPart(parent);
			hascontent = true;
			parent.layout(true);
			return;
		}
		
		cheatSheetPage = new CheatSheetPage(parser, viewItemList, this);
		cheatSheetPage.createPart(parent);
		hascontent = true;
		listOfContentItems = parser.getItems();

		checkSavedState();

		manager.fireEvent(ICheatSheetEvent.CHEATSHEET_OPENED);

		parent.layout(true);

		if (currentItem != null)
			currentItem.getMainItemComposite().setFocus();
	}
	
	private void killDynamicData(ViewItem[] myitems){
//		getCheatsheetManager().removeAllData();
		manager = new CheatSheetManager(currentID, this);
		
		for (int i=0; i<myitems.length; i++){
			if(myitems[i].contentItem.isDynamic()){
				((CoreItem)myitems[i]).setButtonsHandled(false);
				if(myitems[i].contentItem instanceof ItemWithSubItems)
					((ItemWithSubItems)myitems[i].contentItem).addSubItems(null);
			}
					
		}
	}

	/**
	* Read the contents of the welcome page
	*/
	private boolean readFile() {
		parser = new CheatSheetDomParser(contentURL);
		boolean retBool = parser.parse();
		csversion = parser.getCsversion();
		return retBool;
	}

	private boolean replaceThisContentItem(Item ci, ItemWithSubItems ciws) {
		ArrayList list = parser.getItems();
		for (int i = 0; i < list.size(); i++) {
			AbstractItem oci = (AbstractItem) list.get(i);
			if (oci.getID().equals(ci.getID())) {
				list.set(i, ciws);
				return true;
			}
		}
		return false;
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

	/**
	 * Restore the view state
	 */
	private void restoreState(IMemento memento) {
		IMemento contentMemento = memento.getChild(ICheatSheetResource.URL_MEMENTO);
		if (contentMemento == null) {
			
		} else {
				
			try {
				URL fileURL = new URL(contentMemento.getString(ICheatSheetResource.URL_ID));
				contentURL = fileURL;
			} catch (MalformedURLException mue) {
			}
		}
	}

	/*package*/ void runPerformAction(ImageHyperlink mylabel) {
		CoreItem mycore = null;
		mylabel.setCursor(busyCursor);
		currentItem = (ViewItem) mylabel.getData();
		mycore = (CoreItem) currentItem;

		try {
			if (mycore != null) {
				if ((mycore.runAction(getCheatsheetManager()) == ViewItem.VIEWITEM_ADVANCE)){// && !(mycore.isCompleted())) {
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
			org.eclipse.jface.dialogs.ErrorDialog.openError(new Shell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_RUNNING_ACTION), null, status);

		} finally {
			mylabel.setCursor(null);
		}
	}

	/*package*/ void runSubItemPerformAction(ImageHyperlink mylabel, int subItemIndex) {
		currentItem = (ViewItem) mylabel.getData();

		//		CoreItem c = null;
		CoreItem ciws = null;

		if (currentItem instanceof CoreItem)
			ciws = (CoreItem) currentItem;

		try {
			if (ciws != null) {
				if (ciws.contentItem instanceof ItemWithSubItems) {
					ItemWithSubItems contentWithSubs = (ItemWithSubItems) ciws.contentItem;
					SubItem isi = contentWithSubs.getSubItem(subItemIndex);
					String[] params = isi.getActionParams();
					if ((ciws.runAction(isi.getActionPluginID(), isi.getActionClass(), params, getCheatsheetManager()) == ViewItem.VIEWITEM_ADVANCE)) { 
						//set that item as complete.
						ArrayList l = ciws.getListOfSubItemCompositeHolders();
						SubItemCompositeHolder s = (SubItemCompositeHolder) l.get(subItemIndex);
						if (s != null) {
							s.getStartButton().setImage(ciws.restartImage);
							s.getStartButton().redraw();
						}
						advanceSubItem(mylabel, true, subItemIndex);
						saveCurrentSheet();
					}
				}
			}
		} catch (RuntimeException e) {
		} finally {
			mylabel.setCursor(null);
		}
	}

	/*package*/ void saveCurrentSheet() {
		if(currentID != null)
			saveHelper.saveThisState(contentURL, currentItemNum, getViewItemArray(), allCollapsed, expandRestoreList, currentID, getCheatsheetManager());
	}

	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento) {
		//System.out.println("Saving the state of the cheat Sheet view!!!");
		if (contentURL != null) {
			IMemento contentMemento = memento.createChild(ICheatSheetResource.URL_MEMENTO);
			contentMemento.putString(ICheatSheetResource.URL_ID, contentURL.toString());
			//System.out.println("The memento got the string.");
			//System.out.println("Here is teh memento String saved: "+contentMemento.getString("contentURL"));
			//Get the plugin save location:
			//			IPath savePath = Platform.getPluginStateLocation(CheatSheetPlugin.getPlugin());
			saveCurrentSheet();
		}
	}

	/**
	 * Scroll the our contents to keep the current entry on the screen.  Should be
	 * called each time currentEntry is changed.
	 */
	/*package*/ void scrollIfNeeded() {
/*
		//		System.out.println("Scrolling if needed!!!");
		// First thing, decide on our target widget - for an InfoAreaEntry, this will 
		// be its composite, for a closing item, it will be the StyledText.  Doesn't matter,
		// so long as it's whatever widget has 'infoArea' as a parent.
		Composite targetComposite = null;
		//if(currentItemNum>0 && currentItem == null) {
		// We are at the end, our target widget is the closing text
		//	targetComposite = closingText;
		//} else 
		if (nextItem != null) {
			targetComposite = nextItem.getCheckAndMainItemComposite();
		} else if (currentItem != null) {
			targetComposite = currentItem.getCheckAndMainItemComposite();
		}
		//		System.out.println("The target composite beginning "+targetComposite);
		if (targetComposite == null) {
			//			System.out.println("The target composite was null!!!");
			return;
		}
		// In an effort to make everything more readable, we will extract out all the
		// values of relevance into seperate variables, and alias all the relevant composites

		//		Composite infoArea = targetComposite.getParent();
		//		System.out.println("The info area is "+infoArea);

		//		ScrolledComposite scrolledComposite = (ScrolledComposite) infoArea.getParent();
		//		System.out.println("The sc is "+scrolledComposite);

		// The scrollbar will need to be updated as well.  We use its increment because it's
		// probably better than some arbitrary number
		//		System.out.println("Getting vertical bar");
		ScrollBar vBar = scrolledComposite.getVerticalBar();
		int increment = (vBar.getIncrement()) * 4;
		//		int increment = (vBar.getIncrement());
		// When scrolling, the situation is that infoArea is *larger* than scrolledComposite,
		// which encloses it.  Thus the viewable area of infoArea is a question of how far off
		// the top of scrolledComposite it begins, and how much of it scrolledComposite can display.
		int topOfInfoArea = infoArea.getBounds().y;
		int bottomOfInfoArea = infoArea.getBounds().height;
		int firstPixelViewable = -topOfInfoArea;
		int lastPixelViewable = scrolledComposite.getSize().y + firstPixelViewable;
		int firstPixelOfTarget = targetComposite.getLocation().y;
		//		int geoffPixelofTarget = targetComposite.getBounds().y;
		//	int otherPixelofTarget = targetComposite.get

		int lastPixelOfTarget = firstPixelOfTarget + targetComposite.getSize().y;
		// If target is on screen already, don't do anything
		if ((firstPixelOfTarget >= firstPixelViewable) && (lastPixelOfTarget <= lastPixelViewable)) {
			//			System.out.println("Doing nothing!!!");
			return;
		}

		//	    System.out.println("CHECK 1");

		int totalinc = 0;
		int myint = infoArea.getBounds().y;
		// Whether we're scrolling up or down, the principle is the same, get the top of the
		// current entry to within 'increment' pixels of the top of the viewable area.  
		//UPDATED: 05/31/2002 : keeps track of the increment until it has figured out the
		//scroll value.  Then the target area is redrawn.  The result is no flicker,
		//just one refresh redrawn.  NICE.

		scrolledComposite.setVisible(false);

		if (firstPixelOfTarget < firstPixelViewable) {
			while (firstPixelOfTarget < firstPixelViewable) {
				// We are scrolling up, target is above viewable
				// Don't scroll past top though
				//				System.err.println("Scrolling UP");
				if (topOfInfoArea >= firstPixelViewable)
					break;
				vBar.setSelection(vBar.getSelection() - increment);
				totalinc -= increment;
				myint += increment;
				// Update relevant indices			
				topOfInfoArea = myint;
				firstPixelViewable = -topOfInfoArea;
				lastPixelViewable = scrolledComposite.getSize().y + firstPixelViewable;
			}
			infoArea.setLocation(infoArea.getLocation().x, infoArea.getLocation().y - totalinc);
		} else if (firstPixelOfTarget > (firstPixelViewable + increment)) {
			while (firstPixelOfTarget > (firstPixelViewable + increment)) {
				//				System.err.println("Scrolling Down");
				// We are scrolling down, target is below viewable.  
				// Don't scroll past bottom though
				if (bottomOfInfoArea <= lastPixelViewable)
					break;
				vBar.setSelection(vBar.getSelection() + increment + 5);
				totalinc += increment;
				myint -= increment;
				// Update relevant indices			
				topOfInfoArea = myint;
				firstPixelViewable = -topOfInfoArea;
				lastPixelViewable = scrolledComposite.getSize().y + firstPixelViewable;
			}
			infoArea.setLocation(infoArea.getLocation().x, infoArea.getLocation().y - totalinc);
		}
		//		System.out.println("Total increment to incrememt = "+totalinc);
		//Set the info area location last.
		scrolledComposite.setVisible(true);
// TODO: LP - Needed? 20040321 
//		scrolledComposite.layout(true);
//		infoArea.layout(true);
*/
	}

	public void setContent(CheatSheetElement element) {

		if (element == null || element.equals(contentElement))
			return;

		if (contentURL != null)
			saveCurrentSheet();

		// Cleanup previous contents
		if (getHasContent()) {
			callDisposeOnViewElements();
		}

		// Set the current content to new content
		this.contentElement = element;
		currentID = element.getID();

		//		System.out.println("The cheatsheet id loaded is: " + currentID); //$NON-NLS-1$

		IPluginDescriptor desc = null;
		if(element != null)
			try{
			desc = element.getConfigurationElement().getDeclaringExtension().getDeclaringPluginDescriptor();
			}catch(Exception e){}

		if(desc != null)
		this.contentURL = desc.find(new Path("$nl$").append(element.getContentFile())); //$NON-NLS-1$		

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

		// Initialize the view with the new contents
		if (parent != null) {
			initCheatSheetView();
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		//need this to have view refreshed and redrawn nicely.
//		if (infoArea != null && !infoArea.isDisposed())
//			infoArea.setFocus();
		//need this to have current item selected. (Assumes that when you reactivate the view you will work with current item.)
		if (currentItem != null)
			currentItem.getMainItemComposite().setFocus();
	}


	/*package*/ void toggleExpandRestore() {
		if (allCollapsed) {
			restoreExpandStates();
			allCollapsed = false;
			hideFields.setToolTipText(CheatSheetPlugin.getResourceString(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP)));
			saveCurrentSheet();
		} else {
			collapseAllButCurrent(true);
			allCollapsed = true;
			hideFields.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTORE_ALL_TOOLTIP));
			saveCurrentSheet();
		}

	}
}