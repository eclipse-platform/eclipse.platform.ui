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

import java.net.URL;
import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.osgi.framework.Bundle;

public abstract class ViewItem {

	public final static byte VIEWITEM_ADVANCE = 0;
	public final static byte VIEWITEM_DONOT_ADVANCE = 1;
	private Composite bodyComp;

	protected FormText bodyText;
	private Composite bodyWrapperComposite;
	protected Composite buttonComposite;

	protected ArrayList buttonCompositeList;
	private boolean buttonExpanded = true;
	private Label checkDoneLabel;
	private boolean completed = false;

	private Image completeImage;
	protected Item item;

	protected Color darkGrey;
	private final RGB darkGreyRGB = new RGB(160, 192, 208);

	private Image helpImage;
	private final RGB HIGHLIGHT_RGB = new RGB(230, 230, 230);
	private boolean isSkipped = false;
	protected Color itemColor;
	protected Color lightGrey;
	private ExpandableComposite mainItemComposite;

	private Composite parent;
	private Image skipImage;
	protected CheatSheetViewer viewer;
	private Composite titleComposite;
	protected Color white;
	protected FormToolkit toolkit;
	protected ScrolledForm form;

	private boolean bold = true;
	private Font boldFont;
	private Font regularFont;

	/**
	 * Constructor for ViewItem.
	 */
	public ViewItem(FormToolkit toolkit, ScrolledForm form, Item item, Color itemColor, CheatSheetViewer viewer) {
		super();
		this.toolkit = toolkit;
		this.form = form;
		this.parent = form.getBody();
		this.item = item;
		this.itemColor = itemColor;
		this.viewer = viewer;
		lightGrey = new Color(parent.getDisplay(), HIGHLIGHT_RGB);
		darkGrey = new Color(parent.getDisplay(), darkGreyRGB);
		buttonCompositeList = new ArrayList(10);
		// Initialize the item...
		init();

		String imageFileName = "icons/full/obj16/skip_status.gif"; //$NON-NLS-1$
		URL imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		skipImage = imageDescriptor.createImage();

		imageFileName = "icons/full/obj16/complete_status.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		completeImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/linkto_help.gif"; //$NON-NLS-1$
		imageURL = CheatSheetPlugin.getPlugin().find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		helpImage = imageDescriptor.createImage();

		addItem();
	}

	//Adds the item to the main composite.
	private void addItem() {

		Display display = parent.getDisplay();
		Color bg = JFaceColors.getBannerBackground(display);
		white = bg;

		//		Set up the main composite for the item.******************************************
		checkDoneLabel = toolkit.createLabel(parent, " "); //$NON-NLS-1$
		checkDoneLabel.setBackground(white);
//		TableWrapData checkdonelabeldata = new TableWrapData();
//		checkdonelabeldata.widthHint = Math.max(completeImage.getBounds().width, skipImage.getBounds().width);


		mainItemComposite = toolkit.createExpandableComposite(parent, ExpandableComposite.TREE_NODE);
		mainItemComposite.setBackground(itemColor);
		mainItemComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		String title = item.getTitle();
		if (title != null) {
			mainItemComposite.setText(title);
		}

		
		mainItemComposite.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		

		//		Set up the title composite for the item.*****************************************
		titleComposite = toolkit.createComposite(mainItemComposite);
		titleComposite.setBackground(itemColor);

		// handle item extensions here
		// check number of extensions for this item and adjust layout accordingly
		int number = 0;
		ArrayList itemExts = item.getItemExtensions();
		if (itemExts != null) {
			for (int g = 0; g < itemExts.size(); g++) {
				AbstractItemExtensionElement[] eea = (AbstractItemExtensionElement[]) itemExts.get(g);
				number += eea.length;
				for (int x = 0; x < eea.length; x++) {
					eea[x].createControl(titleComposite);
				}
			}
		}

		// don't add the help icon unless there is a help link
		if (item.getHref() != null) {
			// adjust the layout count
			number++;
			ImageHyperlink helpButton = createButton(titleComposite, helpImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.HELP_BUTTON_TOOLTIP));
			toolkit.adapt(helpButton, true, true);
			helpButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					openHelpTopic(item.getHref());
				}
			});
		}

		if(number > 0) {
			mainItemComposite.setTextClient(titleComposite);
			GridLayout mylayout = new GridLayout(number, false);
			GridData mydata = new GridData(GridData.FILL_BOTH);
	
			titleComposite.setLayout(mylayout);
			titleComposite.setLayoutData(mydata);
			mylayout.marginWidth = 0;
			mylayout.marginHeight = 0;
			mylayout.verticalSpacing = 0;
		}

		//Body wrapper here.   this composite will be hidden and shown as appropriate.
		bodyWrapperComposite = toolkit.createComposite(mainItemComposite);
		mainItemComposite.setClient(bodyWrapperComposite);
		TableWrapLayout wrapperLayout = new TableWrapLayout();
		bodyWrapperComposite.setLayout(wrapperLayout);
		bodyWrapperComposite.setBackground(itemColor);

//		bodyText = toolkit.createLabel(bodyWrapperComposite, item.getDescription(), SWT.WRAP);
		bodyText = toolkit.createFormText(bodyWrapperComposite, false);
		bodyText.setText(item.getDescription(), item.getDescription().startsWith(IParserTags.FORM_START_TAG), false);

		//Set up the body text portion here.		
		bodyText.setBackground(itemColor);
		bodyText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		//Handle the sub-steps and regular buttons here.
		//First Check to see if there is sub steps.  If there is, don't create the button comp,
		//As it will be handled by the CoreItemWithSubs.
		//If there is no sub steps, create a button composite and Pass it to CoreItem using the handleButtons.

//FIXME: What now? 
//		if (contentItem.isDynamic()) {
//			((CoreItem) this).setBodyWrapperComposite(bodyWrapperComposite);
//		} else {
			handleButtons(bodyWrapperComposite);
//		}

		setButtonsCollapsed();
		setCollapsed();

		boldFont = mainItemComposite.getFont();
		FontData[] fontDatas = boldFont.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(fontDatas[i].getStyle() ^ SWT.BOLD);
		}
		regularFont = new Font(mainItemComposite.getDisplay(), fontDatas);
		
		setBold(false);
	}

	protected ImageHyperlink createButton(Composite parent, Image image, ViewItem item, Color color, String toolTipText) {
		ImageHyperlink button = new ImageHyperlink(parent, SWT.NULL);
		toolkit.adapt(button, true, true);
		button.setImage(image);
		button.setData(item);
		button.setBackground(color);
		button.setToolTipText(toolTipText);
//		button.setFAccessibleDescription(bodyText.getText());
//		button.setFAccessibleName(button.getToolTipText());

		return button;
	}

	public void dispose() {
		if (lightGrey != null)
			lightGrey.dispose();
		if (darkGrey != null)
			darkGrey.dispose();
		if (checkDoneLabel != null)
			checkDoneLabel.dispose();
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
		if (helpImage != null)
			helpImage.dispose();
		if (skipImage != null)
			skipImage.dispose();
		if (white != null)
			white.dispose();
		if (titleComposite != null)
			titleComposite.dispose();
//		if (boldFont != null)
//			boldFont.dispose();
		if (regularFont != null)
			regularFont.dispose();

		ArrayList itemExts = item.getItemExtensions();
		if (itemExts != null) {
			for (int g = 0; g < itemExts.size(); g++) {
				AbstractItemExtensionElement[] eea = (AbstractItemExtensionElement[]) itemExts.get(g);
				for (int x = 0; x < eea.length; x++) {
					eea[x].dispose();
				}
			}
		}
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
	public Item getItem() {
		return item;
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

	/*package*/
	boolean isBold() {
		return bold;
	}

	/**
	 * Returns the completed.
	 * @return boolean
	 */
	public boolean isCompleted() {
		return completed;
	}

	public boolean isExpanded() {
		return mainItemComposite.isExpanded();
	}

	/*package*/
	boolean isSkipped() {
		return isSkipped;
	}

	/**
	* Open a help topic
	*/
	private void openHelpTopic(String href) {
		if (href != null) {
			WorkbenchHelp.displayHelpResource(href);
		}
	}

	/*package*/
	byte runAction(CheatSheetManager csm) {
		Action action = item.getAction();
		if(action == null) {
			return VIEWITEM_ADVANCE;
		} else {
			return runAction(action.getPluginID(), action.getActionClass(), action.getParams(), csm);
		}
	}

	/**
	 * Run an action
	 */
	/*package*/
	byte runAction(String pluginId, String className, String[] params, CheatSheetManager csm) {
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_FINDING_PLUGIN_FOR_ACTION), null);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_FINDING_PLUGIN_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}
		Class actionClass;
		IAction action;
		try {
			actionClass = bundle.loadClass(className);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), null, status);
			return VIEWITEM_DONOT_ADVANCE;
		}
		try {
			action = (IAction) actionClass.newInstance();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), null, status);

			//logActionLinkError(pluginId, className);
			return VIEWITEM_DONOT_ADVANCE;
		}

		final boolean[] listenerFired = { false };
		final boolean[] listenerResult = { false };
		IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if(event.getNewValue() instanceof Boolean) {
					listenerFired[0] = true;
					listenerResult[0] = ((Boolean)event.getNewValue()).booleanValue();
				}
			}
		};

		// Add PropertyChangeListener to the action, so we can detemine if a action was succesfull
		action.addPropertyChangeListener(propertyChangeListener);

		// Run the action for this ViewItem
		if (action instanceof ICheatSheetAction) {
			// Prepare parameters
			String[] clonedParams = new String[params.length];
			System.arraycopy(params, 0, clonedParams, 0, params.length);
			for (int i = 0; i < clonedParams.length; i++) {
				String param = clonedParams[i];
				if(param != null && param.startsWith("${") && param.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
					param = param.substring(2,param.length()-1);
					String value = csm.getData(param);
					clonedParams[i] = value == null ? "" : value; //$NON-NLS-1$
				}
			}
			
			((ICheatSheetAction) action).run(clonedParams, csm);
		} else
			action.run();

		// Remove the PropertyChangeListener
		action.removePropertyChangeListener(propertyChangeListener);

		if (listenerFired[0]) {
			if (listenerResult[0]) {
				return VIEWITEM_ADVANCE;
			} else {
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
		setBold(true);
		mainItemComposite.setFocus();
//		getExpandToggle().setFocus();
	}

	/*package*/
	void setAsNormalCollapsed() {
		setBold(false);
		setColorAsCurrent(false);
		if (mainItemComposite.isExpanded())
			setCollapsed();
	}

	/*package*/
	void setAsNormalNonCollapsed() {
		setColorAsCurrent(false);
		setBold(false);
	}

	/*package*/
	void setBold(boolean value) {
		if(value) {
			mainItemComposite.setFont(boldFont);
			mainItemComposite.layout();
			parent.layout();
		} else {
			mainItemComposite.setFont(regularFont);
			mainItemComposite.layout();
			parent.layout();
		}
		bold = value;
	}
	
	private void setBodyColor(Color color) {
		mainItemComposite.setBackground(color);
		bodyWrapperComposite.setBackground(color);

		if (buttonComposite != null)
			buttonComposite.setBackground(color);

		Control[] bodyChildren = bodyWrapperComposite.getChildren();
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
//				GridData mydata = (GridData) buttonComposite.getLayoutData();
//				mydata.heightHint = 0;

				buttonExpanded = false;
				parent.getParent().layout(true);
				parent.layout(true);
				mainItemComposite.layout(true);
				bodyWrapperComposite.layout(true);
			}

	}

	//expands the item
	/*package*/
	void setButtonsExpanded() {

		if (!buttonExpanded) {
			//		System.out.println("Buttons Expanding!!");
			buttonComposite.setVisible(true);

//			GridData mydata = (GridData) buttonComposite.getLayoutData();
//			mydata.heightHint = SWT.DEFAULT;

			buttonExpanded = true;

			parent.getParent().layout(true);
			parent.layout(true);
			mainItemComposite.layout(true);
			bodyWrapperComposite.layout(true);
			FormToolkit.ensureVisible(getMainItemComposite());
		}
	}

	//collapses the item
	/*package*/
	void setCollapsed() {
		if (mainItemComposite.isExpanded()) {
			mainItemComposite.setExpanded(false);
			form.reflow(true);

//			GridData mydata = (GridData) bodyWrapperComposite.getLayoutData();
//			mydata.heightHint = 0;
//			expanded = false;
			parent.getParent().layout(true);
			mainItemComposite.layout(true);
			bodyWrapperComposite.layout(true);

			FormToolkit.ensureVisible(getMainItemComposite());
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
		completed = true;
		checkDoneLabel.setImage(completeImage);

	}

	//expands the item
	/*package*/
	void setExpanded() {
		// System.out.println("Item is already expanded? : " + expanded);
		if (!mainItemComposite.isExpanded()) {
			mainItemComposite.setExpanded(true);
			form.reflow(true);

			parent.getParent().layout(true);
			mainItemComposite.layout(true);
			bodyWrapperComposite.layout(true);
			FormToolkit.ensureVisible(getMainItemComposite());
		}
	}

	/*package*/
	void setIncomplete() {
		checkDoneLabel.setImage(null);
		completed = false;
		setStartImage();
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
}
