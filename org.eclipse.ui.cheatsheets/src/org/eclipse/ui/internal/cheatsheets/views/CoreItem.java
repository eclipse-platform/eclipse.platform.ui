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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;

public class CoreItem extends ViewItem {
	protected Composite bodyWrapperComposite;
	protected boolean buttonsHandled = false;
	protected ImageHyperlink completeButton;
	protected Image completeImage;
	private ArrayList listOfSubItemCompositeHolders;
	protected Image restartImage;
	protected ImageHyperlink skipButton;
	protected Image skipImage;
	protected ImageHyperlink startButton;
	protected Image startImage;


	/**
	 * Constructor for CoreItem.
	 * @param parent
	 * @param contentItem
	 */
	public CoreItem(FormToolkit toolkit, ScrolledForm form, IContainsContent contentItem, Color itemColor, CheatSheetView theview) {
		super(toolkit, form, contentItem, itemColor, theview);
	}

	/**
	 * @see org.eclipse.ui.internal.cheatsheets.views.ViewItem#disposeOfStuff()
	 */
	public void dispose() {
		super.dispose();

		if (startImage != null)
			startImage.dispose();
		if (skipImage != null)
			skipImage.dispose();
		if (completeImage != null)
			completeImage.dispose();
		if (restartImage != null)
			restartImage.dispose();
	}

	public ArrayList getListOfSubItemCompositeHolders() {
		return listOfSubItemCompositeHolders;
	}

	/**
	 * @see org.eclipse.ui.internal.cheatsheets.data.ViewItem#handleButtons(Composite)
	 */
	/*package*/ void handleButtons(Composite bodyWrapperComposite) {
		if (contentItem instanceof ItemWithSubItems) {
			try{
				handleSubButtons(bodyWrapperComposite);
			}catch(Exception e){
				//Need to log exception here. 
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.LESS_THAN_2_SUBITEMS), e);
				CheatSheetPlugin.getPlugin().getLog().log(status);
				org.eclipse.jface.dialogs.ErrorDialog.openError(theview.getViewSite().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.LESS_THAN_2_SUBITEMS), null, status);
			}
			return;
		}
		
		if (buttonsHandled)
			return;
		
		buttonComposite = toolkit.createComposite(bodyWrapperComposite);
		GridLayout buttonlayout = new GridLayout(4, false);
		buttonlayout.marginHeight = 2;
		buttonlayout.marginWidth = 2;
		buttonlayout.verticalSpacing = 2;

		TableWrapData buttonData = new TableWrapData(TableWrapData.FILL);

		buttonComposite.setLayout(buttonlayout);
		buttonComposite.setLayoutData(buttonData);
		buttonComposite.setBackground(itemColor);

		Label spacer = toolkit.createLabel(buttonComposite, null);
		spacer.setBackground(itemColor);
		GridData spacerData = new GridData();
		spacerData.widthHint = 16;
		spacer.setLayoutData(spacerData);

		if (((Item)contentItem).isPerform()) {
			startButton = createButton(buttonComposite, startImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
			toolkit.adapt(startButton, true, true);
			startButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					theview.runPerformAction(startButton);
				}
			});
		}
		if (((Item)contentItem).isSkip()) {
			skipButton = createButton(buttonComposite, skipImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.SKIP_TASK_TOOLTIP));
			toolkit.adapt(skipButton, true, true);
			skipButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					theview.advanceItem(skipButton, false);
				}
			});
		}
		if (((Item)contentItem).isComplete()) {
			completeButton = createButton(buttonComposite, completeImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.COMPLETE_TASK_TOOLTIP));
			toolkit.adapt(completeButton, true, true);
			completeButton.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					theview.advanceItem(completeButton, true);
				}
			});
		}
		buttonCompositeList.add(buttonComposite);
		buttonsHandled = true;
	}

	/*package*/ void handleLazyButtons() {
		handleButtons(bodyWrapperComposite);
	}

	/**
		 * @see org.eclipse.ui.internal.cheatsheets.data.ViewItem#handleButtons(Composite)
		 */
	private void handleSubButtons(Composite bodyWrapperComposite) throws Exception {
		if (buttonsHandled)
			return;
		//Instantiate the list to store the sub item composites.
		listOfSubItemCompositeHolders = new ArrayList(20);

		SubItem[] sublist = ((ItemWithSubItems) contentItem).getSubItems();
		
		if(sublist.length<=1)
			throw new Exception(ICheatSheetResource.LESS_THAN_2_SUBITEMS);
		
		
		buttonComposite = toolkit.createComposite(bodyWrapperComposite);
		GridLayout xbuttonlayout = new GridLayout(6, false);
		xbuttonlayout.marginHeight = 2;
		xbuttonlayout.marginWidth = 2;
		xbuttonlayout.verticalSpacing = 2;

		TableWrapData xbuttonData = new TableWrapData(TableWrapData.FILL);

		buttonComposite.setLayout(xbuttonlayout);
		buttonComposite.setLayoutData(xbuttonData);
		buttonComposite.setBackground(itemColor);

		buttonCompositeList.add(buttonComposite);
		//loop throught the number of sub items, make a new composite for each sub item.
		//Add the spacer, the label, then the buttons that are applicable for each sub item.
		for (int i = 0; i < sublist.length; i++) {
			int added = 0;
			//Get the sub item to add.
			SubItem sub = sublist[i];

			//Spacer label added.
			Label checkDoneLabel = toolkit.createLabel(buttonComposite, null);
			checkDoneLabel.setBackground(itemColor);
			GridData checkDoneData = new GridData();
			checkDoneData.widthHint = 16;
			checkDoneLabel.setLayoutData(checkDoneData);
			added++;

			//Now add the label.
			Label label = toolkit.createLabel(buttonComposite, sub.getLabel());
			label.setBackground(itemColor);
			added++;

			final int fi = i;

			if (sub.isPerform()) {
				added++;
				startButton = createButton(buttonComposite, startImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
				toolkit.adapt(startButton, true, true);
				startButton.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						theview.runSubItemPerformAction(startButton, fi);
					}
				});
			}
			if (sub.isSkip()) {
				added++;
				skipButton = createButton(buttonComposite, skipImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.SKIP_TASK_TOOLTIP));
				toolkit.adapt(skipButton, true, true);
				skipButton.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						theview.advanceSubItem(skipButton, false, fi);
					}
				});
			}
			if (sub.isComplete()) {
				added++;
				completeButton = createButton(buttonComposite, completeImage, this, itemColor, CheatSheetPlugin.getResourceString(ICheatSheetResource.COMPLETE_TASK_TOOLTIP));
				toolkit.adapt(completeButton, true, true);
				completeButton.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						theview.advanceSubItem(completeButton, true, fi);
					}
				});
			}

			while (added < 6) {
				// Add filler labels as needed to complete the row
				Label filler = toolkit.createLabel(buttonComposite, null);
				filler.setBackground(itemColor);
				added++;
			}
			listOfSubItemCompositeHolders.add(new SubItemCompositeHolder(checkDoneLabel, startButton));
		}
		buttonsHandled = true;
	}

	protected void init() {
		super.init();

		IPluginDescriptor mydesc = CheatSheetPlugin.getPlugin().getDescriptor();

		String imageFileName = "icons/full/clcl16/start_task.gif"; //$NON-NLS-1$
		URL imageURL = mydesc.find(new Path(imageFileName));
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		startImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/skip_task.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		skipImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/complete_task.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		completeImage = imageDescriptor.createImage();

		imageFileName = "icons/full/clcl16/restart_task.gif"; //$NON-NLS-1$
		imageURL = mydesc.find(new Path(imageFileName));
		imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		restartImage = imageDescriptor.createImage();
	}

	/**
	 * @param composite
	 */
	/*package*/ void setBodyWrapperComposite(Composite composite) {
		bodyWrapperComposite = composite;
	}
	
	/*package*/void setButtonsHandled(boolean handled){
		buttonsHandled = handled;
	}
	
	/*package*/ void setIncomplete() {
		super.setIncomplete();
			
		//check for sub items and reset their icons.
		ArrayList l = getListOfSubItemCompositeHolders();
		if(l != null){
			for(int j=0; j<l.size(); j++){
				SubItemCompositeHolder s = (SubItemCompositeHolder)l.get(j);
				if(s.isCompleted() || s.isSkipped())
					s.getIconLabel().setImage(null);
				if(s.startButton != null)
					s.getStartButton().setImage(startImage);	
					
			}					
		}	
	}

	/*package*/ void setRestartImage() {
		if (startButton != null) {
			startButton.setImage(restartImage);
			startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTART_TASK_TOOLTIP));
		}
	}

	/*package*/ void setStartImage() {
		if (startButton != null) {
			startButton.setImage(startImage);
			startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
		}
	}

}
