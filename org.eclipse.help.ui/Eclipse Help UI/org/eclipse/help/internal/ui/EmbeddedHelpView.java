package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.events.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.help.*;
import org.eclipse.help.internal.contributions.InfoSet;
import org.eclipse.help.internal.contributions.Contribution;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.navigation.*;

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

	private String lastInfosetId;
	private String lastTopicUrl;

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
		fillToolbar();
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
			new String[] {IHelpUIConstants.EMBEDDED_HELP_VIEW});

		try {
			// get proper InfoSet. 
			InfoSet infoSet = null;
			
			if (lastInfosetId != null) // mememto was saved
				{
				infoSet = HelpSystem.getNavigationManager().getInfoSet(lastInfosetId);
				if (infoSet != null) // plugin still exists
					HelpSystem.getNavigationManager().setCurrentInfoSet(lastInfosetId);
			}
			
			infoSet = HelpSystem.getNavigationManager().getCurrentInfoSet();

			// no InsoSets installed at all. Display error dialog, but also handle
			// empty view. Since view is already created, do *not* close for safety. 
			if (infoSet == null) {
				String msg= WorkbenchResources.getString("WW001");
				Util.displayErrorDialog(msg);
				viewContainer.dispose();
				Text text = new Text(parent, SWT.BORDER | SWT.MULTI 
										| SWT.READ_ONLY | SWT.WRAP);
				text.setText(msg);

				// capture that the view was not created successfully
				creationSuccessful = false;
				return;
			}

			// try creating the view. If anything goes wrong. put up
			// a Text area with the captured error message. 

			htmlViewer = new HTMLHelpViewer(viewContainer);
			navigationViewer = new NavigationViewer(viewContainer);
			// htmlViewer should be updated when selected topic changes 
			navigationViewer.addSelectionChangedListener(htmlViewer);

			// only add actions for windows.
			// when we have an embedded browser on linux, remove the if()
			if (System.getProperty("os.name").startsWith("Win"))
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
			viewContainer.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent event) {
					shellResized();
				}
			});

			// update parts
			navigationViewer.setInput(infoSet);

			// navigate to specific topic if known from memento
			if (lastTopicUrl != null)
				navigationViewer.setSelection(new StructuredSelection(lastTopicUrl));

			// if any errors or parsing errors have occurred, display them in a pop-up
			Util.displayStatus();

			creationSuccessful = true;

		} catch (HelpWorkbenchException e) {
			// something we know about failed.
			viewContainer.dispose();
			Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP );
			text.setText(e.getMessage());

			// capture that the view was not created successfully
			creationSuccessful = false;

		} catch (Exception e) {
			// now handle worst case scenario. Should never be here!
			viewContainer.dispose();
			Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP );
			text.setText("Help View Failed to Launch");

			// capture that the view was not created successfully
			creationSuccessful = false;
		}
	}
	/**
	 * Shows the related links, and current information set
	 */
	public void displayHelp(IHelpTopic[] relatedTopics, IHelpTopic topic) {
		InfoSet infoSet = HelpSystem.getNavigationManager().getCurrentInfoSet();
		if (infoSet == null)
			return;

		HelpInfoView relatedTopicsTree = new HelpInfoView(null);
		relatedTopicsTree.setRawLabel(
			WorkbenchResources.getString("RelatedTopics_viewLabel"));
		HelpTopic selectedChild = null;
		for (int i = 0; i < relatedTopics.length; i++) {
			HelpTopic child = new HelpTopicRef((HelpTopic) relatedTopics[i]);
			relatedTopicsTree.addChild(child);
			if (relatedTopics[i] == topic) {
				selectedChild = child;
			}
		}
		Contribution contributions[] = new Contribution[2];
		// We create another tree that will populate additional "Links" tab.
		contributions[0] = relatedTopicsTree;
		contributions[1] = infoSet;

		// populate navigation viewer
		navigationViewer.setInput(contributions);

		// select topic
		if (selectedChild != null) {
			navigationViewer.setSelection(new StructuredSelection(selectedChild));
		}

		// if any errors or parsing errors have occurred, display them in a pop-up
		Util.displayStatus();
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
	private void fillToolbar() {

		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.removeAll();

		tbm.add(showHideAction);
		tbm.add(new Separator());
		tbm.add(backAction);
		tbm.add(forwardAction);
		tbm.add(new Separator());
		tbm.add(synchronizeAction);
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

		lastInfosetId = memento.getString("lastInfoSet");
		lastTopicUrl = memento.getString("lastTopicUrl");
	}
	public boolean isCreationSuccessful() {
		return creationSuccessful;
	}
	/**
	* Layout the list and text widgets according to the new
	* positions of the sashes..events.SelectionEvent
	*/
	void layout() {

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
			new Actions.SynchronizeAction(browser, this.getNavigationViewer(), (Actions.ShowHideAction)showHideAction);
		copyAction = new Actions.CopyAction(browser);
		printAction = new Actions.PrintAction(browser);

	}
	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento) {
		try {
			if (navigationViewer != null) {
				InfoSet infoSet = (InfoSet) navigationViewer.getInput();
				if (infoSet != null)
					memento.putString("lastInfoSet", infoSet.getID());

				ISelection sel = navigationViewer.getSelection();
				if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
					Object selectedTopic = ((IStructuredSelection) sel).getFirstElement();
					if (selectedTopic != null && selectedTopic instanceof IHelpTopic)
						memento.putString("lastTopicUrl", ((IHelpTopic) selectedTopic).getHref());
				}
			}
		} catch (Exception e) {
			// this is to guard agains platform UI exceptions
		}
	}
	/**
	 * Asks the part to take focus within the workbench.
	 */
	public void setFocus() {
		try {
			InfoSet infoSet = (InfoSet) navigationViewer.getInput();
			if (infoSet != null)
				// set global infoset and navigation model
				HelpSystem.getNavigationManager().setCurrentInfoSet(infoSet.getID());
		} catch (Exception e) {
			// unexpected error, it should not happen.
			// This is to guard agains platform UI bugs.
		}
	}
	/**
	* Handle the shell resized event.
	*/
	void shellResized() {

		/* Get the client area for the shell */
		Rectangle viewContainerBounds = viewContainer.getClientArea();

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
}
