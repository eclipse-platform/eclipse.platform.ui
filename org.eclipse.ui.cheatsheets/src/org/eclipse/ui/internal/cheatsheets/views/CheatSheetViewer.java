package org.eclipse.ui.internal.cheatsheets.views;

import java.net.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
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
	private boolean isStarted = false;

	private CheatSheetParser parser;
	private CheatSheet cheatSheet;
	private CheatSheetManager manager;
	private CheatSheetSaveHelper saveHelper;

	private Properties savedProps = null;

	private CheatSheetExpandRestoreAction expandRestoreAction;

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
	public CheatSheetViewer() {
		currentItemNum = -1;
		saveHelper = new CheatSheetSaveHelper();
	}

	/*package*/ void advanceIntroItem() {
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
//FIXME: ???
//		if (nextItem.contentItem.isDynamic()) {
//							((CoreItem) nextItem).handleLazyButtons();
//		}
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
			nextItem = (ViewItem) viewItemList.get(index);
			currentItemNum = index;
			if (nextItem != null) {
				//Handle lazy button instantiation here.
//FIXME: ???
//				if (nextItem.contentItem.isDynamic()) {
//					((CoreItem) nextItem).handleLazyButtons();
//				}
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
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_COMPLETED);
		}

		saveCurrentSheet();
	}

	/*package*/ void advanceSubItem(ImageHyperlink mylabel, boolean markAsCompleted, int subItemIndex) {
		//		System.out.println("Advancing a sub item!! Item Number: " + subItemIndex);
//		String subItemID = null;
		Label l = null;
		ArrayList list = null;
		SubItemCompositeHolder sich = null;
		CoreItem ciws = null;

		currentItem = (ViewItem) mylabel.getData();

		if (currentItem instanceof CoreItem)
			ciws = (CoreItem) currentItem;

		if (ciws != null) {
//			Item item = ((ViewItem) ciws).item;
//			if (item.getSubItems() != null && item.getSubItems().size()>0)
//				subItemID = ((ItemWithSubItems) ci).getSubItem(subItemIndex).getID();
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

	private void checkDynamicModel() {
		Properties props = saveHelper.loadState(currentID);
		if (props == null)
			return;

		savedProps = props;

		ArrayList dynamicItemDataList = null; //TODO (ArrayList) props.get(IParserTags.DYNAMICDATA);
		ArrayList dynamicSubItemDataList = null; //TODO (ArrayList) props.get(IParserTags.DYNAMICSUBITEMDATA);

//FIXME: Is this needed?
//		if (dynamicItemDataList != null)
//			for (int i = 0; i < dynamicItemDataList.size(); i++) {
//				Properties p = (Properties) dynamicItemDataList.get(i);
//				String itemid = (String) p.get(IParserTags.ITEM);
///* TODO: Remove this! */
////				String buttonCodes = (String) p.get(IParserTags.ACTIONPHRASE);
//				String aclass = (String) p.get(IParserTags.CLASS);
//				String actionpid = (String) p.get(IParserTags.PLUGINID);
//				String[] actionParams = (String[]) p.get(IParserTags.ACTIONPARAM);
//				AbstractItem abItem = getItemWithID(itemid);
//				if (abItem == null) {
//					continue;
//				} else {
//					if (abItem instanceof Item) {
//						Item c = (Item) abItem;
//						if (c.isDynamic()) {
//							c.setActionClass(aclass);
//							c.setActionPluginID(actionpid);
//							c.setActionParams(actionParams);
///* TODO: Remove this! */
////							c.setButtonCodes(buttonCodes);
//						}
//					}
//				}
//			}

//FIXME: Is this needed?
//		//Re-Set the dynamic item sub item data if there was any stored.
//		if (dynamicSubItemDataList != null)
//			for (int i = 0; i < dynamicSubItemDataList.size(); i++) {
//				Properties p = (Properties) dynamicSubItemDataList.get(i);
//				String itemid = (String) p.get(IParserTags.ITEM);
//				String subitemid = (String) p.get(IParserTags.SUBITEM);
///* TODO: Remove this! */				
////				String buttonCodes = (String) p.get(IParserTags.ACTIONPHRASE);
//				String aclass = (String) p.get(IParserTags.CLASS);
//				String actionpid = (String) p.get(IParserTags.PLUGINID);
//				String sublabel = (String) p.get(IParserTags.SUBITEMLABEL);
//				String[] actionParams = (String[]) p.get(IParserTags.ACTIONPARAM);
//				AbstractItem abItem = getItemWithID(itemid);
//				if (abItem == null) {
//					continue;
//				} else {
//					if (abItem instanceof Item) {
//						Item c = (Item) abItem;
//						if (c.isDynamic()) {
//							ItemWithSubItems ciws = convertThisIItem(c);
//							replaceThisContentItem(c, ciws);
///* TODO: Remove this! */
////							SubItem subItem = createASubItem(subitemid, buttonCodes, actionpid, aclass, actionParams, sublabel);
////							ciws.addSubItem(subItem);
//						}
//					} else if (abItem instanceof ItemWithSubItems) {
//						boolean handled = false;
//						ItemWithSubItems c = (ItemWithSubItems) abItem;
//						if (c.isDynamic()) {
//							SubItem[] subs = c.getSubItems();
//							sublabel : for (int j = 0; j < subs.length; j++) {
//								SubItem s = subs[j];
//								if (s.getID().equals(subitemid)) {
//									s.setActionClass(aclass);
//									s.setActionPluginID(actionpid);
//									s.setActionParams(actionParams);
///* TODO: Remove this! */
////									s.setButtonCodes(buttonCodes);
//									s.setLabel(sublabel);
//									handled = true;
//									break sublabel;
//								}
//							}
//							if (!handled) {
///* TODO: Remove this! */
////								SubItem subItem = createASubItem(subitemid, buttonCodes, actionpid, aclass, actionParams, sublabel);
////								c.addSubItem(subItem);
//								handled = true;
//							}
//						}
//					}
//				}
//			}

	}

	private void checkSavedState() {
		Properties props = null;
		if (savedProps == null) {
			props = saveHelper.loadState(currentID);
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

		manager = new CheatSheetManager(currentID, this);
		manager.setData(csmData);

		if (itemNum >= 0) {
			clearBackgrounds(getViewItemArray());
			currentItemNum = itemNum;
			
			currentItem = (ViewItem) viewItemList.get(itemNum);

			for (int i = 0; i < viewItemList.size(); i++) {

				ViewItem item = (ViewItem) viewItemList.get(i);
//FIXME: ???
//				if (i > 0 && ((CoreItem) item).contentItem.isDynamic() && i <= currentItemNum)
//					 ((CoreItem) item).handleLazyButtons();

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
			} else {
				currentItem.setAsCurrentActiveItem();
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

//FIXME: Is this needed?
//	private ItemWithSubItems convertThisIItem(Item item) {
//		Item cc = (Item) item;
//		ItemWithSubItems itemws = new ItemWithSubItems();
//		itemws.setContent(cc.getContent());
//		itemws.setID(cc.getID());
//		return itemws;
//	}

//FIXME: Is this needed?
//	private SubItem createASubItem(String subid, String actionCodes, String actionPID, String actionClass, String[] params, String label) {
//		SubItem subItem = new SubItem();
//		subItem.setActionClass(actionClass);
///* TODO: Remove this! */
////		subItem.setButtonCodes(actionCodes);
//		subItem.setActionParams(params);
//		subItem.setLabel(label);
//		subItem.setActionPluginID(actionPID);
//		subItem.setID(subid);
//		return subItem;
//	}

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

//TODO: this was only for when a memento was present! Now what?
//		initCheatSheetView();
		if(contentElement != null) {
			initCheatSheetView();
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if(manager != null)
			manager.fireEvent(ICheatSheetEvent.CHEATSHEET_CLOSED);

		saveCurrentSheet();

		callDisposeOnViewElements();

		if (busyCursor != null)
			busyCursor.dispose();

		if (parent != null)
			parent.dispose();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetViewer#getCheatSheetID()
	 */
	public String getCheatSheetID() {
		return getContent().getID();
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
	/*package*/ CheatSheetElement getContent() {
		return contentElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetViewer#getControl()
	 */
	public Control getControl() {
		if(errorPage != null) {
			return errorPage.getControl();
		}
		if(cheatSheetPage != null) {
			return cheatSheetPage.getControl();
		}
		return null;
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

//FIXME: Is this needed?
//	private AbstractItem getItemWithID(String id) {
//		try {
//			//Check to see if that item with that id is dynamic.
//			//If it is not dynamic, return null for it cannot be modified.
//			ArrayList contentItems = cheatSheet.getItems();
//			for (int i = 0; i < contentItems.size(); i++) {
//				AbstractItem contentItem = (AbstractItem) contentItems.get(i);
//				if (contentItem.getID().equals(id)) {
//					//return contentItem;
//					if (contentItem instanceof IContainsContent) {
//						IContainsContent cc = (IContainsContent) contentItem;
//						if (cc.isDynamic())
//							return contentItem;
//					}
//					return null;
//				}
//
//			}
//			return null;
//		} catch (Exception e) {
//			return null;
//		}
//	}

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
	
	private void initCheatSheetView() {
		// Confirm that we have content to render, if not return.
		if(contentURL == null) {
			return;
		}

		//Re-initialize list to store items collapsed by expand/restore action on c.s. toolbar.
		expandRestoreList = new ArrayList();

		// re set that action to turned off.
		if(expandRestoreAction != null)
			expandRestoreAction.setCollapsed(false);

		//reset current item to be null; next item too.
		currentItem = null;
		nextItem = null;

		currentItemNum = 0;
		viewItemList = new ArrayList();

		// Reset the page variables 
		errorPage = null;
		cheatSheetPage = null;
		
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
		
		cheatSheetPage = new CheatSheetPage(cheatSheet, viewItemList, this);
		cheatSheetPage.createPart(parent);
		hascontent = true;
		listOfContentItems = cheatSheet.getItems();

		checkSavedState();

		manager.fireEvent(ICheatSheetEvent.CHEATSHEET_OPENED);

		parent.layout(true);

		if (currentItem != null && !currentItem.isCompleted())
			currentItem.getMainItemComposite().setFocus();
	}
	
	private void killDynamicData(ViewItem[] myitems){
//		getCheatsheetManager().removeAllData();
		manager = new CheatSheetManager(currentID, this);
		
//FIXME: Is this needed?
//		for (int i=0; i<myitems.length; i++){
//			if(myitems[i].contentItem.isDynamic()){
//				((CoreItem)myitems[i]).setButtonsHandled(false);
//				if(myitems[i].contentItem instanceof ItemWithSubItems)
//					((ItemWithSubItems)myitems[i].contentItem).addSubItems(null);
//			}
//					
//		}
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

//FIXME: Is this needed?
//	private boolean replaceThisContentItem(Item ci, ItemWithSubItems ciws) {
//		ArrayList list = cheatSheet.getItems();
//		for (int i = 0; i < list.size(); i++) {
//			AbstractItem oci = (AbstractItem) list.get(i);
//			if (oci.getID().equals(ci.getID())) {
//				list.set(i, ciws);
//				return true;
//			}
//		}
//		return false;
//	}

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
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_RUNNING_ACTION), null, status);

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
				if (ciws.item.getSubItems() != null && ciws.item.getSubItems().size()>0) {
					SubItem isi = (SubItem)ciws.item.getSubItems().get(subItemIndex);
					if(isi.getAction() != null) {
						String[] params = isi.getAction().getParams();
						if ((ciws.runAction(isi.getAction().getPluginID(), isi.getAction().getActionClass(), params, getCheatsheetManager()) == ViewItem.VIEWITEM_ADVANCE)) { 
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
			}
		} catch (RuntimeException e) {
		} finally {
			mylabel.setCursor(null);
		}
	}

	/*package*/ void saveCurrentSheet() {
		if(currentID != null) {
			boolean expandRestoreActionState = false;
			if(expandRestoreAction != null)
				expandRestoreActionState = expandRestoreAction.isCollapsed();			
			saveHelper.saveState(currentItemNum, getViewItemArray(), expandRestoreActionState, expandRestoreList, currentID, getCheatsheetManager());
		}
	}

	private void setContent(CheatSheetElement element) {

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

		// Initialize the view with the new contents
		if (parent != null) {
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
