package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.Actions.ShowHideAction;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.topics.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
/**
 * EmbeddedHelpView
 */
public class EmbeddedHelpView extends ViewPart {
	public final static String ID = "org.eclipse.help.internal.ui.EmbeddedHelpView";
	/* Constants */
	static final int SASH_WIDTH = 3;
	protected Composite viewContainer = null;
	protected Sash vSash;
	protected int lastSash;
	// this flag captures if the UI of this view has been created successfully
	// (ie: perspective & view)
	protected boolean creationSuccessful = false;
	protected float viewsWidthPercentage = 0.25f;
	protected HTMLHelpViewer htmlViewer = null;
	protected NavigationViewer navigationViewer = null;
	private ITopics topicsToDisplay;
	private String topicHrefToDisplay;
	private Action showHideAction;
	private Action backAction;
	private Action forwardAction;
	private Action printAction;
	private Action copyAction;
	private Action synchronizeAction;
	/**
	 * EmbeddedHelpView constructor comment.
	 */
	public EmbeddedHelpView() {
		super();
	}
	/**
	 * Adds action contributions.
	 */
	private void addContributions() {
		makeActions();
		if (getViewSite() != null) //Embedded Help View
			fillToolbar(getViewSite().getActionBars().getToolBarManager());
		fillContextMenu();
		fillMenu();
	}
	/**
	 * Creates the SWT controls for a part.
	 */
	public void createPartControl(Composite parent) {
		viewContainer = new Composite(parent, SWT.NULL);
		WorkbenchHelp.setHelp(
			viewContainer,
			new String[] { IHelpUIConstants.EMBEDDED_HELP_VIEW });
		String errorMessage = "";
		try {
			if (topicsToDisplay == null) {
				// get first Topics available
				Iterator topicsHrefsIt =
					HelpSystem.getTopicsNavigationManager().getTopicsHrefs().iterator();
				if (topicsHrefsIt.hasNext()) {
					String topicsHref = (String) topicsHrefsIt.next();
					topicsToDisplay = HelpSystem.getTopicsNavigationManager().getTopics(topicsHref);
				}
			}
			// No InfoSets installed at all. Display error dialog, but also handle
			// empty view. Since view is already created, do *not* close for safety. 
			if (topicsToDisplay == null) {
				errorMessage = WorkbenchResources.getString("WW001");
				//Documentation is not installed.
				creationSuccessful = false;
				ErrorUtil.displayErrorDialog(errorMessage);
				return;
			}
			htmlViewer = new HTMLHelpViewer(viewContainer);
			navigationViewer = new NavigationViewer(viewContainer, this);
			// htmlViewer should be updated when selected topics or topic change
			navigationViewer.addSelectionChangedListener(htmlViewer);
			// only add actions for windows.
			// when we have an embedded browser on linux, remove the if()
			if (System.getProperty("os.name").startsWith("Win")) {
				addContributions();
				vSash = new Sash(viewContainer, SWT.VERTICAL);
				vSash.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						if (event.detail != SWT.DRAG) {
							vSash.setBounds(event.x, event.y, event.width, event.height);
							layout();
						}
					}
				});
			}
			viewContainer.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent event) {
					shellResized();
				}
			});
			// show help
			displayHelp(topicsToDisplay, topicHrefToDisplay);
			// if any errors or parsing errors have occurred, display them in a pop-up
			ErrorUtil.displayStatus();
			creationSuccessful = true;
		} catch (HelpWorkbenchException e) {
			// something we know about failed.
			creationSuccessful = false;
			errorMessage = e.getMessage();
		} catch (Exception e) {
			// now handle worst case scenario. Should never be here!			
			creationSuccessful = false;
			errorMessage = WorkbenchResources.getString("WE007");
			//Help View Failed to Launch.
		} finally {
			if (!creationSuccessful) {
				// try creating the view. If anything goes wrong. put up
				// a Text area with the captured error message. 
				viewContainer.dispose();
				Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
				text.setText(errorMessage);
			}
		}
	}
	/**
	 * Show the specified infoset and (optional) topic
	 */
	public void displayHelp(ITopics topics, String topicHref) {
		navigationViewer.setInput(topics);
		if (topicHref != null)
			navigationViewer.setSelection(new StructuredSelection(topicHref));
	}
	/**
	 * Fill the context menu with actions.
	 */
	private void fillContextMenu() {
		// Currently not needed...
		/*
		MenuManager manager = new MenuManager("helpActions");
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				mgr.add(copyAction);
				mgr.add(printAction);
			}
		});
		Menu contextMenu = manager.createContextMenu(viewContainer);
		viewContainer.setMenu(contextMenu);
		*/
	}
	/**
	 * Fill the workbench menu with actions.
	 */
	private void fillMenu() {
		// Currently not needed...
		/*
		IActionBars actionBars = getViewSite().getActionBars();
		if (actionBars != null) {
			actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);
			actionBars.setGlobalActionHandler(
				IWorkbenchActionConstants.M_FILE,
				printAction);
		}
		*/
	}
	/**
	 * Fill the local tool bar with actions.
	 */
	public void fillToolbar(IToolBarManager tbm) {
		tbm.removeAll();
		tbm.add(showHideAction);
		tbm.add(synchronizeAction);
		tbm.add(new Separator());
		tbm.add(backAction);
		tbm.add(forwardAction);
		tbm.add(new Separator());
		// NOTE: when print support is added in the platform
		// move this to File -> Print
		tbm.add(printAction);
		tbm.add(new Separator());
		// require update because toolbar control has been created by this point,
		// but manager does not update it automatically once it has been created
		tbm.update(true);
	}
	NavigationViewer getNavigationViewer() {
		return navigationViewer;
	}
	public Composite getViewComposite() {
		return viewContainer;
	}
	/* (non-Javadoc)
	 * Initializes this view with the given view site.  A memento is passed to
	 * the view which contains a snapshot of the views state from a previous
	 * session.  Where possible, the view should try to recreate that state
	 * within the part controls.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		if (memento == null)
			return;
		if (topicsToDisplay == null && topicHrefToDisplay == null) {
			// Use memento values only if no other values available
			topicsToDisplay =
				HelpSystem.getTopicsNavigationManager().getTopics(
					memento.getString("topicsToDisplay"));
			topicHrefToDisplay = memento.getString("topicHrefToDisplay");
		}
	}
	public boolean isCreationSuccessful() {
		return creationSuccessful;
	}
	/**
	* Layout the list and text widgets according to the new
	* positions of the sashes..events.SelectionEvent
	*/
	void layout() {
		if (vSash == null) // Linux
			return;
		Rectangle viewContainerBounds = viewContainer.getClientArea();
		Rectangle vSashBounds = vSash.getBounds();
		viewsWidthPercentage =
			(float) vSashBounds.x / (viewContainerBounds.width - vSashBounds.width);
		navigationViewer.getControl().setBounds(
			0,
			0,
			vSashBounds.x,
			viewContainerBounds.height);
		htmlViewer.getControl().setBounds(
			vSashBounds.x + vSashBounds.width,
			0,
			viewContainerBounds.width - (vSashBounds.x + vSashBounds.width),
			viewContainerBounds.height);
	}
	/**
	 * Fill the local tool bar with actions.
	 */
	private void makeActions() {
		IBrowser browser = htmlViewer.getWebBrowser();
		if (browser == null)
			return;
		showHideAction = new Actions.ShowHideAction(this);
		showHideAction.setChecked(false);
		backAction = new Actions.BackAction(browser);
		forwardAction = new Actions.ForwardAction(browser);
		synchronizeAction =
			new Actions.SynchronizeAction(
				browser,
				this.getNavigationViewer(),
				(Actions.ShowHideAction) showHideAction);
		copyAction = new Actions.CopyAction(browser);
		printAction = new Actions.PrintAction(browser);
	}
	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento) {
		try {
			if (navigationViewer != null) {
				ITopics topics = (ITopics) navigationViewer.getInput();
				if (topics != null)
					memento.putString("topicsToDisplay", topics.getHref());
				ISelection sel = navigationViewer.getSelection();
				if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
					Object selectedTopic = ((IStructuredSelection) sel).getFirstElement();
					if (selectedTopic != null && selectedTopic instanceof ITopic)
						memento.putString("topicHrefToDisplay", ((ITopic) selectedTopic).getHref());
				}
			}
		} catch (Exception e) {
			// this is to guard agains platform UI exceptions
		}
	}
	/**
	* Handle the shell resized event.
	*/
	void shellResized() {
		/* Get the client area for the shell */
		Rectangle viewContainerBounds = viewContainer.getClientArea();
		if (vSash == null) { // Linux
			navigationViewer.getControl().setBounds(viewContainerBounds);
			return;
		}
		/* Position the sash according to same proportions as before*/
		vSash.setLocation(
			(int) ((viewContainerBounds.width - SASH_WIDTH) * viewsWidthPercentage),
			vSash.getLocation().y);
		/*
		* Make list 1 half the width and half the height of the tab leaving room for the sash.
		* Place list 1 in the top left quadrant of the tab.
		*/
		Rectangle navigationBrowserBounds =
			new Rectangle(0, 0, vSash.getLocation().x, viewContainerBounds.height);
		navigationViewer.getControl().setBounds(
			navigationBrowserBounds.x,
			navigationBrowserBounds.y,
			navigationBrowserBounds.width,
			navigationBrowserBounds.height);
		/*
		* Make list 2 half the width and half the height of the tab leaving room for the sash.
		* Place list 2 in the top right quadrant of the tab.
		*/
		htmlViewer.getControl().setBounds(
			navigationBrowserBounds.width + SASH_WIDTH,
			0,
			viewContainerBounds.width - (navigationBrowserBounds.width + SASH_WIDTH),
			navigationBrowserBounds.height);
		/* Position the sash */
		vSash.setBounds(
			navigationBrowserBounds.width,
			0,
			SASH_WIDTH,
			navigationBrowserBounds.height);
	}
	public boolean toggleNavigation() {
		boolean hidden;
		Rectangle bounds = vSash.getBounds();
		if (bounds.x == 0) {
			bounds.x = lastSash;
			hidden = false;
		} else {
			lastSash = bounds.x;
			bounds.x = 0;
			hidden = true;
		}
		vSash.setBounds(bounds);
		layout();
		return hidden;
	}
	/**
	 * @see WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	/**
	 * Gets the topicsToDisplay.
	 * @return Returns a ITopics
	 */
	public ITopics getTopicsToDisplay() {
		return topicsToDisplay;
	}

}