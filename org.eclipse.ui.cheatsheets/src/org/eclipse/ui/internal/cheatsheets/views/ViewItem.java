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

import java.util.ArrayList;
import org.eclipse.help.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.IParserTags;
import org.eclipse.ui.internal.cheatsheets.data.Item;

public abstract class ViewItem {

	public final static byte VIEWITEM_ADVANCE = 0;
	public final static byte VIEWITEM_DONOT_ADVANCE = 1;
	private Composite bodyComp;

	protected FormText bodyText;
//	protected Label bodyText;
	protected Composite bodyWrapperComposite;
	protected Composite buttonComposite;

	private boolean buttonExpanded = true;
	private Label checkDoneLabel;
	private boolean completed = false;

	protected Item item;

	// Colors
	// Active color's RGB value
	protected final RGB activeRGB = new RGB(232, 242, 254);
	protected Color activeColor;
	// Alternating color's RGB value
	protected final RGB alternateRGB = new RGB(244, 244, 244);
	protected Color alternateColor;

	protected Color itemColor;
	protected Color white;

	private boolean isSkipped = false;
	private ExpandableComposite mainItemComposite;

	private Composite parent;
	protected CheatSheetViewer viewer;
	private Composite titleComposite;
	protected FormToolkit toolkit;
	protected ScrolledForm form;

	private boolean bold = true;
	private Font boldFont;
	private Font regularFont;
	private boolean initialized = false;

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
		activeColor = new Color(parent.getDisplay(), activeRGB);
		alternateColor = new Color(parent.getDisplay(), alternateRGB);

		addItem();
	}

	//Adds the item to the main composite.
	private void addItem() {
		CheatSheetStopWatch.startStopWatch("ViewItem.addItem()"); //$NON-NLS-1$

		Display display = parent.getDisplay();
		Color bg = JFaceColors.getBannerBackground(display);
		white = bg;
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after getBannerBackground: "); //$NON-NLS-1$ //$NON-NLS-2$

		//		Set up the main composite for the item.******************************************
		checkDoneLabel = toolkit.createLabel(parent, " "); //$NON-NLS-1$
		checkDoneLabel.setBackground(white);
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after create checkDoneLabel: "); //$NON-NLS-1$ //$NON-NLS-2$

		mainItemComposite = toolkit.createExpandableComposite(parent, ExpandableComposite.TREE_NODE);
		mainItemComposite.setBackground(itemColor);
		mainItemComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		String title = item.getTitle();
		if (title != null) {
			mainItemComposite.setText(title);
		}
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after create mainItemComposite: "); //$NON-NLS-1$ //$NON-NLS-2$

		
		mainItemComposite.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after addExpansionListener: "); //$NON-NLS-1$ //$NON-NLS-2$

		// handle item extensions here
		// check number of extensions for this item and adjust layout accordingly
		int number = 0;
		ArrayList itemExts = item.getItemExtensions();

		if((itemExts != null && itemExts.size() > 0) || item.getContextId() != null || item.getHref() != null) {
			// Set up the title composite for the item.
			titleComposite = toolkit.createComposite(mainItemComposite);
			titleComposite.setBackground(itemColor);
		}

		if(itemExts != null) {
			for (int g = 0; g < itemExts.size(); g++) {
				AbstractItemExtensionElement[] eea = (AbstractItemExtensionElement[]) itemExts.get(g);
				number += eea.length;
				for (int x = 0; x < eea.length; x++) {
					eea[x].createControl(titleComposite);
				}
			}
		}
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after create item extensions: "); //$NON-NLS-1$ //$NON-NLS-2$

		// don't add the help icon unless there is a context id or help link
		if(item.getContextId() != null || item.getHref() != null) {
			// adjust the layout count
			number++;
			ImageHyperlink helpButton = createButton(titleComposite, CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_ITEM_HELP), this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.HELP_BUTTON_TOOLTIP));
			toolkit.adapt(helpButton, true, true);
			helpButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					// If we have a context id, handle this first and ignore an hrefs
					if(item.getContextId() != null) {
						openInfopop(e.widget);
					} else {
						// We only have an href, so let's open it in the help system
						openHelpTopic();
					}
				}
			});
		}
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after create help button: "); //$NON-NLS-1$ //$NON-NLS-2$

		if(number > 0) {
			mainItemComposite.setTextClient(titleComposite);
			GridLayout layout = new GridLayout(number, false);
			GridData data = new GridData(GridData.FILL_BOTH);
	
			titleComposite.setLayout(layout);
			titleComposite.setLayoutData(data);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
		}
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after setTextClient: "); //$NON-NLS-1$ //$NON-NLS-2$

		//Body wrapper here.   this composite will be hidden and shown as appropriate.
		bodyWrapperComposite = toolkit.createComposite(mainItemComposite);
		mainItemComposite.setClient(bodyWrapperComposite);
		TableWrapLayout wrapperLayout = new TableWrapLayout();
		bodyWrapperComposite.setLayout(wrapperLayout);
		bodyWrapperComposite.setBackground(itemColor);
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after create bodyWrapperComposite: "); //$NON-NLS-1$ //$NON-NLS-2$

		bodyText = toolkit.createFormText(bodyWrapperComposite, false);
//		bodyText = toolkit.createLabel(bodyWrapperComposite, item.getDescription(), SWT.WRAP);
		bodyText.setText(item.getDescription(), item.getDescription().startsWith(IParserTags.FORM_START_TAG), false);

		//Set up the body text portion here.		
		bodyText.setBackground(itemColor);
		bodyText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after create FormText: "); //$NON-NLS-1$ //$NON-NLS-2$

		//Handle the sub-steps and regular buttons here.
		//First Check to see if there is sub steps.  If there is, don't create the button comp,
		//As it will be handled by the CoreItemWithSubs.
		//If there is no sub steps, create a button composite and Pass it to CoreItem using the handleButtons.

		if(!item.isDynamic()) {
			handleButtons();
		}
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after handleButtons(): "); //$NON-NLS-1$ //$NON-NLS-2$

		setButtonsCollapsed();
		setCollapsed();
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after setting buttons and item collapsed: "); //$NON-NLS-1$ //$NON-NLS-2$

		boldFont = mainItemComposite.getFont();
		FontData[] fontDatas = boldFont.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(fontDatas[i].getStyle() ^ SWT.BOLD);
		}
		regularFont = new Font(mainItemComposite.getDisplay(), fontDatas);
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after font initlization: "); //$NON-NLS-1$ //$NON-NLS-2$
		
		setBold(false);
		CheatSheetStopWatch.printLapTime("ViewItem.addItem()", "Time in addItem() after setBold: "); //$NON-NLS-1$ //$NON-NLS-2$
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
		if (alternateColor != null)
			alternateColor.dispose();
		if (activeColor != null)
			activeColor.dispose();
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
		if (white != null)
			white.dispose();
		if (titleComposite != null)
			titleComposite.dispose();
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
		return CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_ITEM_COMPLETE);
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
		return CheatSheetPlugin.getPlugin().getImage(ICheatSheetResource.CHEATSHEET_ITEM_SKIP);
	}

	//Adds the buttons to the buttonComposite.
	/*package*/
	abstract void handleButtons();

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
	private void openHelpTopic() {
		if (item == null || item.getHref() == null) {
			return;
		}

		WorkbenchHelp.displayHelpResource(item.getHref());
	}

	/**
	 * Open an infopop
	 */
	private void openInfopop(Widget widget) {
		if (item == null || item.getContextId() == null) {
			return;
		}

		IContext context = HelpSystem.getContext(item.getContextId());

		if (context != null) {
			// determine a location in the upper right corner of the widget
			Point point = widget.getDisplay().getCursorLocation();
			point = new Point(point.x + 15, point.y);
			// display the help
			WorkbenchHelp.displayContext(context, point.x, point.y);
		}
	}

	/*package*/
	void setAsCurrentActiveItem() {
		setColorAsCurrent(true);
		if (!buttonExpanded)
			setButtonsExpanded();
		setExpanded();
		setBold(true);
		mainItemComposite.setFocus();
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

	private void setBodyColor(Color color) {
		mainItemComposite.setBackground(color);
		bodyWrapperComposite.setBackground(color);

		if (buttonComposite != null)
			buttonComposite.setBackground(color);

		Control[] bodyChildren = bodyWrapperComposite.getChildren();
		for (int i = 0; i < bodyChildren.length; i++) {
			bodyChildren[i].setBackground(color);
		}

		if (buttonComposite != null) {
			buttonComposite.setBackground(color);
			bodyChildren = buttonComposite.getChildren();
			for (int i = 0; i < bodyChildren.length; i++) {
				bodyChildren[i].setBackground(color);
			}
		}
	}

	/*package*/
	void setBold(boolean value) {
		if(value) {
			mainItemComposite.setFont(boldFont);
			if(initialized)
				mainItemComposite.layout();
		} else {
			mainItemComposite.setFont(regularFont);
			if(initialized)
				mainItemComposite.layout();
		}
		bold = value;
	}
	
	//collapses the item
	/*package*/
	void setButtonsCollapsed() {

		if (buttonComposite != null)
			if (buttonExpanded) {
				buttonComposite.setVisible(false);

				buttonExpanded = false;
			}

	}

	//expands the item
	/*package*/
	void setButtonsExpanded() {

		if (!buttonExpanded) {
			buttonComposite.setVisible(true);

			buttonExpanded = true;

			if(initialized) {
				FormToolkit.ensureVisible(getMainItemComposite());
			}
		}
	}

	//collapses the item
	/*package*/
	void setCollapsed() {
		if (mainItemComposite.isExpanded()) {
			mainItemComposite.setExpanded(false);
			if(initialized) {
				form.reflow(true);
				FormToolkit.ensureVisible(getMainItemComposite());
			}
		}
	}

	private void setColorAsCurrent(boolean active) {
		if (active) {
			setTitleColor(activeColor);
			setBodyColor(activeColor);
		} else {
			setTitleColor(itemColor);
			setBodyColor(itemColor);
		}
	}

	//marks the item as complete.
	/*package*/
	void setComplete() {
		completed = true;
		checkDoneLabel.setImage(getCompleteImage());

		if(initialized) {
			checkDoneLabel.getParent().layout();
		}
	}

	//expands the item
	/*package*/
	void setExpanded() {
		if (!mainItemComposite.isExpanded()) {
			mainItemComposite.setExpanded(true);
			if(initialized) {
				form.reflow(true);
				FormToolkit.ensureVisible(getMainItemComposite());
			}
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
		setBold(false);
	}

	/*package*/
	abstract void setRestartImage();

	/*package*/
	void setSkipped() {
		isSkipped = true;
		checkDoneLabel.setImage(getSkipImage());

		if(initialized) {
			checkDoneLabel.getParent().layout();
		}
	}
	/*package*/
	abstract void setStartImage();

	private void setTitleColor(Color color) {
		if(titleComposite != null) {
			titleComposite.setBackground(color);

			Control[] titlechildren = titleComposite.getChildren();
			for (int i = 0; i < titlechildren.length; i++) {
				titlechildren[i].setBackground(color);
			}
		}
	}

	public void initialized() {
		initialized = true;
	}
}
