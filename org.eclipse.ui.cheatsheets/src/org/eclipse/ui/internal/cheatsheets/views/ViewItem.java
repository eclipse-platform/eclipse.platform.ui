/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelp;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;

public abstract class ViewItem {

	public final static byte VIEWITEM_ADVANCE = 0;
	public final static byte VIEWITEM_DONOT_ADVANCE = 1;
	private Composite bodyComp;

	protected StyledText bodyText;
	private Composite bodyWrapperComposite;
	private Cursor busyCursor;
	protected Composite buttonComposite;

	protected ArrayList buttonCompositeList;
	private boolean buttonExpanded = true;
	private Composite checkAndMainItemComposite;
	private Label checkDoneLabel;
	private Image collapseImage;
	private boolean completed = false;

	private Image completeImage;
	protected IContainsContent contentItem;

	protected Color darkGrey;
	private final RGB darkGreyRGB = new RGB(160, 192, 208);

	protected boolean expanded = true;
	private Image expandImage;

	private ToggleButton expandToggle;
	private Image helpImage;
	private final RGB HIGHLIGHT_RGB = new RGB(230, 230, 230);
	private boolean isSkipped = false;
	protected Color itemColor;
	protected Color lightGrey;
	private Composite mainItemComposite;

	private Composite parent;
	private Image skipImage;
	protected CheatSheetView theview;
	private Composite titleComposite;
	protected StyledText titleText;
	protected Color white;
	private boolean wizardLaunched;

	/**
	 * Constructor for ViewItem.
	 */
	public ViewItem(Composite parent, IContainsContent contentItem, Color itemColor, CheatSheetView theview) {
		super();
		this.parent = parent;
		this.contentItem = contentItem;
		this.itemColor = itemColor;
		this.theview = theview;
		lightGrey = new Color(parent.getDisplay(), HIGHLIGHT_RGB);
		darkGrey = new Color(parent.getDisplay(), darkGreyRGB);
		buttonCompositeList = new ArrayList(10);
		// Initialize the item...
		init();

		IPluginDescriptor mydesc = CheatSheetPlugin.getPlugin().getDescriptor();

		String imageFileName = "icons/full/obj16/skip_status.gif"; //$NON-NLS-1$
		URL imageURL = mydesc.find(new Path(imageFileName));
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		skipImage = imageDescriptor.createImage();

		imageFileName = "icons/full/obj16/collapse_state.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		collapseImage = imageDescriptor.createImage();

		imageFileName = "icons/full/obj16/complete_status.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		completeImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/linkto_help.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		helpImage = imageDescriptor.createImage();

		imageFileName = "icons/full/obj16/expand_state.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		expandImage = imageDescriptor.createImage();

		busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);

		addItem();
	}

	//Adds the item to the main composite.
	private void addItem() {

		Display display = parent.getDisplay();
		Color bg = JFaceColors.getBannerBackground(display);
		white = bg;

		//		Get the images.****************************************************************

		//		Set up the main composite for the item.******************************************
		checkAndMainItemComposite = new Composite(parent, SWT.NULL);
		GridLayout checklayout = new GridLayout(2, false);
		GridData checkdata = new GridData(GridData.FILL_HORIZONTAL);
		checkdata.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;

		checklayout.marginHeight = 0;
		checklayout.marginWidth = 0;
		checklayout.verticalSpacing = 0;
		checkAndMainItemComposite.setBackground(white);
		checkAndMainItemComposite.setLayout(checklayout);
		checkAndMainItemComposite.setLayoutData(checkdata);

		checkDoneLabel = new Label(checkAndMainItemComposite, SWT.NULL);
		checkDoneLabel.setText(" "); //$NON-NLS-1$

		GridData checkdonelabeldata = new GridData();
		checkdonelabeldata.verticalAlignment = GridData.BEGINNING;
		checkdonelabeldata.grabExcessVerticalSpace = true;
		checkdonelabeldata.widthHint = Math.max(completeImage.getBounds().width, skipImage.getBounds().width);
		checkDoneLabel.setLayoutData(checkdonelabeldata);
		checkDoneLabel.setBackground(white);

		mainItemComposite = new Composite(checkAndMainItemComposite, SWT.NULL);
		GridLayout mylayout = new GridLayout(1, true);
		GridData mydata = new GridData(GridData.FILL_HORIZONTAL);
		mylayout.marginHeight = 0;
		mylayout.marginWidth = 0;
		mylayout.verticalSpacing = 0;
		mainItemComposite.setBackground(itemColor);

		mainItemComposite.setLayout(mylayout);
		mainItemComposite.setLayoutData(mydata);

		//check number of extensions for this item.  adjust layout accordingly.
		int number = 3;
		ArrayList al = contentItem.getItemExtensions();
		if (al != null)
			for (int g = 0; g < al.size(); g++) {
				AbstractItemExtensionElement[] eea = (AbstractItemExtensionElement[]) al.get(g);
				number += eea.length;
			}

		//		Set up the title composite for the item.*****************************************
		titleComposite = new Composite(mainItemComposite, SWT.NULL);
		mylayout = new GridLayout(number, false);
		mydata = new GridData(GridData.FILL_BOTH);

		titleComposite.setLayout(mylayout);
		titleComposite.setLayoutData(mydata);
		mylayout.marginWidth = 0;
		mylayout.marginHeight = 0;
		mylayout.verticalSpacing = 0;
		titleComposite.setBackground(itemColor);

		//		Set the title bar composite content *********************************************
		expandToggle = new ToggleButton(titleComposite, SWT.NULL, expandImage, collapseImage);
		expandToggle.setFAccessibleDescription(contentItem.getText());
		expandToggle.setFAccessibleName(contentItem.getTitle() + " " + contentItem.getText()); //$NON-NLS-1$
		expandToggle.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (expandToggle.isCollapsed())
					setCollapsed();
				else
					setExpanded();
				theview.saveCurrentSheet();
			}
		});
		expandToggle.setBackground(itemColor);

		titleText = new StyledText(titleComposite, SWT.BOLD | SWT.WRAP | SWT.READ_ONLY);
		String title = contentItem.getTitle();
		if (title != null) {
			titleText.setText(title);
		} else {
			titleText.setText(" "); //$NON-NLS-1$
		}
		titleText.getCaret().setVisible(false);
		GridData egridData = new GridData();
		egridData.verticalAlignment = GridData.BEGINNING;
		egridData.horizontalAlignment = GridData.FILL;
		egridData.grabExcessHorizontalSpace = true;
		titleText.setLayoutData(egridData);
		titleText.setBackground(itemColor);
		titleText.setEnabled(false);

		// handle item extensions here.
		ArrayList itemExts = contentItem.getItemExtensions();
		if (itemExts != null)
			for (int x = 0; x < itemExts.size(); x++) {
				AbstractItemExtensionElement[] xe = (AbstractItemExtensionElement[]) itemExts.get(x);
				for (int g = 0; g < xe.length; g++) {
					xe[g].createControl(titleComposite, itemColor);
				}
			}

		//don't add the help icon unless there is a help link.
		if (contentItem.getHref() != null) {
			Button helpButton = new Button(titleComposite, SWT.NULL, helpImage);
			helpButton.setBackground(itemColor);

			helpButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.HELP_BUTTON_TOOLTIP));
			helpButton.setFAccessibleName(helpButton.getToolTipText());
			helpButton.setFAccessibleDescription(helpButton.getToolTipText());

			helpButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Button helpButton = (Button) e.widget;
					helpButton.setCursor(busyCursor);
					openHelpTopic(contentItem.getHref());
					helpButton.setCursor(null);
				}
			});
		}

		//Body wrapper here.   this composite will be hidden and shown as appropriate.
		bodyWrapperComposite = new Composite(mainItemComposite, SWT.NULL);
		GridLayout wrapperLayout = new GridLayout();
		wrapperLayout.marginHeight = 0;
		wrapperLayout.marginWidth = 0;
		wrapperLayout.verticalSpacing = 0;
		GridData wrapperData = new GridData(GridData.FILL_HORIZONTAL);
		bodyWrapperComposite.setLayout(wrapperLayout);
		bodyWrapperComposite.setLayoutData(wrapperData);
		bodyWrapperComposite.setBackground(itemColor);

		//		Set up the body composite, for the body of the item text.
		bodyComp = new Composite(bodyWrapperComposite, SWT.NULL);
		GridLayout bodylayout = new GridLayout(1, false);
		bodylayout.marginHeight = 0;
		bodylayout.marginWidth = 0;
		bodylayout.verticalSpacing = 0;

		GridData bodyData = new GridData(GridData.FILL_HORIZONTAL);
		bodyData.grabExcessHorizontalSpace = true;

		bodyComp.setLayout(bodylayout);
		bodyComp.setLayoutData(bodyData);
		bodyComp.setBackground(itemColor);

		bodyText = new StyledText(bodyComp, SWT.WRAP | SWT.READ_ONLY | SWT.NULL);

		String btext = contentItem.getText();
		if (btext != null) {
			bodyText.setText(btext);
		} else {
			bodyText.setText(" "); //$NON-NLS-1$
		}

		//Set up the body text portion here.		
		GridData bgridData = new GridData();
		bgridData.verticalAlignment = GridData.BEGINNING;
		bgridData.horizontalAlignment = GridData.FILL;
		bgridData.grabExcessHorizontalSpace = true;
		bodyText.setLayoutData(bgridData);
		bodyText.setEnabled(false);
		bodyText.setBackground(itemColor);

		//Handle the sub-steps and regular buttons here.
		//First Check to see if there is sub steps.  If there is, don't create the button comp,
		//As it will be handled by the CoreItemWithSubs.
		//If there is no sub steps, create a button composite and Pass it to CoreItem using the handleButtons.

		if (contentItem.isDynamic()) {
			((CoreItem) this).setBodyWrapperComposite(bodyWrapperComposite);
		} else {
			handleButtons(bodyWrapperComposite);
		}

		mainItemComposite.pack();
		mainItemComposite.layout(true);

		setButtonsCollapsed();
		setCollapsed();
	}

	/*package*/
	void boldTitle() {
		StyleRange r = new StyleRange(0, titleText.getText().length(), null, null, SWT.BOLD);
		titleText.setStyleRange(r);

	}

	public void dispose() {
		if (busyCursor != null)
			busyCursor.dispose();
		if (lightGrey != null)
			lightGrey.dispose();
		if (darkGrey != null)
			darkGrey.dispose();
		if (checkAndMainItemComposite != null)
			checkAndMainItemComposite.dispose();
		if (checkDoneLabel != null)
			checkDoneLabel.dispose();
		if (titleText != null)
			titleText.dispose();
		if (expandToggle != null)
			expandToggle.dispose();
		if (bodyText != null)
			bodyText.dispose();
		if (buttonComposite != null)
			buttonComposite.dispose();
		if (bodyComp != null)
			bodyComp.dispose();
		if (bodyWrapperComposite != null)
			bodyWrapperComposite.dispose();
		if (mainItemComposite != null)
			mainItemComposite.dispose();
		if (completeImage != null)
			completeImage.dispose();
		if (collapseImage != null)
			collapseImage.dispose();
		if (expandImage != null)
			expandImage.dispose();
		if (helpImage != null)
			helpImage.dispose();
		if (skipImage != null)
			skipImage.dispose();
		if (white != null)
			white.dispose();
		if (titleComposite != null)
			titleComposite.dispose();

	}
	/**
	 * Returns the checkAndMainItemComposite.
	 * @return Composite
	 */
	/*package*/
	Composite getCheckAndMainItemComposite() {
		return checkAndMainItemComposite;
	}

	/**
	 * @return
	 */
	/*package*/
	Image getCompleteImage() {
		return completeImage;
	}

	/**
	 * @return
	 */
	public IContainsContent getContentItem() {
		return contentItem;
	}

	/**
	 * Returns the expandToggle.
	 * @return Label
	 */
	/*package*/
	ToggleButton getExpandToggle() {
		return expandToggle;
	}

	/**
	 * Returns the mainItemComposite.
	 * @return Composite
	 */
	/*package*/
	Composite getMainItemComposite() {
		return mainItemComposite;
	}

	/**
	 * @return
	 */
	/*package*/
	Image getSkipImage() {
		return skipImage;
	}

	//Adds the buttons to the buttonComposite.
	/*package*/
	abstract void handleButtons(Composite buttonComposite);

	protected void init() {

	}

	/**
	 * Returns the completed.
	 * @return boolean
	 */
	public boolean isCompleted() {
		return completed;
	}

	public boolean isExpanded() {
		return expanded;
	}

	/*package*/
	boolean isSkipped() {
		return isSkipped;
	}

	private void layoutBody() {
		bodyComp.layout(true);
	}

	/**
	* Open a help topic
	*/
	private void openHelpTopic(String href) {
		IHelp helpSupport = WorkbenchHelp.getHelpSupport();
		if (helpSupport != null) {
			if (href != null)
				helpSupport.displayHelpResource(href);

		}
	}

	/*package*/
	byte runAction(CheatSheetManager csm) {
		return runAction(((ContentItem) contentItem).getActionPluginID(), ((ContentItem) contentItem).getActionClass(), ((ContentItem) contentItem).getActionParams(), csm);
	}

	/**
	 * Run an action
	 */
	/*package*/
	byte runAction(String pluginId, String className, String[] params, CheatSheetManager csm) {
		IPluginDescriptor desc = Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		if (desc == null) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_FINDING_PLUGIN_FOR_ACTION), null);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(new Shell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_FINDING_PLUGIN_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}
		Class actionClass;
		IAction action;
		try {
			actionClass = desc.getPluginClassLoader().loadClass(className);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(new Shell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}
		try {
			action = (IAction) actionClass.newInstance();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(new Shell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), null, status);

			//logActionLinkError(pluginId, className);
			return VIEWITEM_DONOT_ADVANCE;
		}

		wizardLaunched = false;
		ShellListener shellListener = new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				wizardLaunched = true;
			}
		};

		// Add ShellListener to Workbench window, so we can detemine if a wizard is launched.
		theview.getViewSite().getWorkbenchWindow().getShell().addShellListener(shellListener);

		// Run the action for this ViewItem
		if (action instanceof ICheatSheetAction) {
			((ICheatSheetAction) action).run(params, csm);
		} else
			action.run();

		// Remove the ShellListener
		theview.getViewSite().getWorkbenchWindow().getShell().removeShellListener(shellListener);

		if (wizardLaunched) {
			int returnCode = WizardReturnCodeHack.getLastWizardReturnCode();
			if (returnCode == Window.OK) {
				//System.out.println("OK");
				return VIEWITEM_ADVANCE;
			} else if (returnCode == Window.CANCEL) {
				//System.out.println("Cancel");
				return VIEWITEM_DONOT_ADVANCE;
			}
		}

		return VIEWITEM_ADVANCE;
	}

	/*package*/
	void setAsCurrentActiveItem() {
		setColorAsCurrent(true);
		if (!buttonExpanded)
			setButtonsExpanded();
		setExpanded();
		boldTitle();
		layoutBody();
		getExpandToggle().setFocus();
	}

	/*package*/
	void setAsNormalCollapsed() {
		setColorAsCurrent(false);
		if (expanded)
			setCollapsed();
		unboldTitle();
	}

	/*package*/
	void setAsNormalNonCollapsed() {
		setColorAsCurrent(false);
		unboldTitle();
	}

	private void setBodyColor(Color color) {
		mainItemComposite.setBackground(color);
		bodyWrapperComposite.setBackground(color);
		bodyComp.setBackground(color);

		if (buttonComposite != null)
			buttonComposite.setBackground(color);

		Control[] bodyChildren = bodyComp.getChildren();
		for (int i = 0; i < bodyChildren.length; i++) {
			bodyChildren[i].setBackground(color);
		}

		if (buttonComposite != null)
			for (int j = 0; j < buttonCompositeList.size(); j++) {
				Composite c = (Composite) buttonCompositeList.get(j);
				c.setBackground(color);
				bodyChildren = c.getChildren();
				for (int i = 0; i < bodyChildren.length; i++) {
					bodyChildren[i].setBackground(color);
				}
			}

	}

	//collapses the item
	/*package*/
	void setButtonsCollapsed() {

		if (buttonComposite != null)
			if (buttonExpanded) {
				//		System.out.println("Buttons collapsing!!!");

				buttonComposite.setVisible(false);
				GridData mydata = (GridData) buttonComposite.getLayoutData();
				mydata.heightHint = 0;

				buttonExpanded = false;
				parent.getParent().layout(true);
				parent.layout(true);
				mainItemComposite.layout(true);
				bodyWrapperComposite.layout(true);
				theview.layout();
			}

	}

	//expands the item
	/*package*/
	void setButtonsExpanded() {

		if (!buttonExpanded) {
			//		System.out.println("Buttons Expanding!!");
			buttonComposite.setVisible(true);

			GridData mydata = (GridData) buttonComposite.getLayoutData();
			mydata.heightHint = SWT.DEFAULT;

			buttonExpanded = true;

			parent.getParent().layout(true);
			parent.layout(true);
			mainItemComposite.layout(true);
			bodyWrapperComposite.layout(true);
			theview.layout();
			theview.updateScrolledComposite();
			theview.scrollIfNeeded();
		}
	}

	//collapses the item
	/*package*/
	void setCollapsed() {
		if (expanded) {
			bodyWrapperComposite.setVisible(false);
			expandToggle.setCollapsed(true);

			GridData mydata = (GridData) bodyWrapperComposite.getLayoutData();
			mydata.heightHint = 0;
			expanded = false;
			parent.getParent().layout(true);
			mainItemComposite.layout(true);
			bodyWrapperComposite.layout(true);
			theview.layout();

			theview.updateScrolledComposite();
			theview.scrollIfNeeded();
		}
	}

	private void setColorAsCurrent(boolean active) {
		if (active) {
			setTitleColor(darkGrey);
			setBodyColor(darkGrey);
		} else {
			setTitleColor(itemColor);
			setBodyColor(itemColor);
		}
	}

	//marks the item as complete.
	/*package*/
	void setComplete() {
		try {
			completed = true;
			checkDoneLabel.setImage(completeImage);
			checkAndMainItemComposite.layout(true);
		} catch (Exception e) {
			//			System.out.println("in set complete");	
		}

	}

	//expands the item
	/*package*/
	void setExpanded() {
		// System.out.println("Item is already expanded? : " + expanded);
		if (!expanded) {
			bodyWrapperComposite.setVisible(true);
			expandToggle.setCollapsed(false);

			GridData mydata = (GridData) bodyWrapperComposite.getLayoutData();
			mydata.heightHint = SWT.DEFAULT;
			expanded = true;

			parent.getParent().layout(true);
			mainItemComposite.layout(true);
			bodyWrapperComposite.layout(true);
			theview.layout();
			theview.updateScrolledComposite();
			theview.scrollIfNeeded();
			//theview.saveCurrentSheet();
		}
	}

	/*package*/
	void setIncomplete() {
		checkDoneLabel.setImage(null);
		checkAndMainItemComposite.layout(true);
		completed = false;
		setStartImage();
		//setCollapsed();
	}

	/**
	 * Sets the itemColor.
	 * @param itemColor The itemColor to set
	 */
	/*package*/
	void setItemColor(Color itemColor) {
		this.itemColor = itemColor;
	}

	/*package*/
	void setOriginalColor() {
		setTitleColor(itemColor);
		setBodyColor(itemColor);
	}

	/*package*/
	abstract void setRestartImage();

	/*package*/
	void setSkipped() {
		try {
			isSkipped = true;
			checkDoneLabel.setImage(skipImage);
			checkAndMainItemComposite.layout(true);
		} catch (Exception e) {

		}
	}
	/*package*/
	abstract void setStartImage();

	private void setTitleColor(Color color) {
		titleComposite.setBackground(color);

		Control[] titlechildren = titleComposite.getChildren();
		for (int i = 0; i < titlechildren.length; i++) {
			titlechildren[i].setBackground(color);
		}
	}

	/*package*/
	void unboldTitle() {
		StyleRange r = new StyleRange(0, titleText.getText().length(), null, null, SWT.NULL);
		titleText.setStyleRange(r);
	}

}
