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
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.data.*;
import org.eclipse.ui.internal.cheatsheets.data.Item;

public class CoreItem extends ViewItem {
	protected Composite bodyWrapperComposite;
	protected boolean buttonsHandled = false;
	protected boolean complete;
	protected Button completeButton;
	protected Image completeImage;
	private ArrayList listOfSubItemCompositeHolders;
	protected Image restartImage;
	protected boolean skip;
	protected Button skipButton;
	protected Image skipImage;
	protected boolean start;
	protected Button startButton;
	protected Image startImage;


	/**
	 * Constructor for CoreItem.
	 * @param parent
	 * @param contentItem
	 */
	public CoreItem(Composite parent, IContainsContent contentItem, Color itemColor, CheatSheetView theview) {
		super(parent, contentItem, itemColor, theview);
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

	private int[] getActionCodeArray(String actionPhrase) {
		int[] actionCodes = { -1, -1, -1 };
		StringTokenizer mytoken = new StringTokenizer(actionPhrase, ","); //$NON-NLS-1$

		for (int j = 0; j < 3; j++) {
			if (mytoken.hasMoreTokens()) {
				String mytokenstring = mytoken.nextToken();
				//System.out.println("Token"+mytokenstring);
				try {
					actionCodes[j] = Integer.parseInt(mytokenstring);
				} catch (Exception e) {
				}
			}
		}

		return actionCodes;
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
				org.eclipse.jface.dialogs.ErrorDialog.openError(new Shell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.LESS_THAN_2_SUBITEMS), null, status);
			}
			return;
		}
		
		if (buttonsHandled)
			return;
		
		buttonComposite = new Composite(bodyWrapperComposite, SWT.NONE);
		GridLayout buttonlayout = new GridLayout(4, false);
		buttonlayout.marginHeight = 2;
		buttonlayout.marginWidth = 2;
		buttonlayout.verticalSpacing = 2;

		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		buttonData.grabExcessHorizontalSpace = true;

		buttonComposite.setLayout(buttonlayout);
		buttonComposite.setLayoutData(buttonData);
		buttonComposite.setBackground(itemColor);
		Label filllabel = new Label(buttonComposite, SWT.NULL);
		filllabel.setBackground(itemColor);
		GridData filldata = new GridData();
		filldata.widthHint = 16;
		filllabel.setLayoutData(filldata);


		start = false;
		complete = false;
		skip = false;
		int[] actionCodes = null;

		//Get the action codes to display buttons for.

		actionCodes = getActionCodeArray(((Item) contentItem).getButtonCodes());


		

		//Add the icons in order.  Check each one when you add it.
		for (int i = 0; i < actionCodes.length; i++) {
			if (actionCodes[i] == 0) {
				//perform
				start = true;
			} else if (actionCodes[i] == 1) {
				//continue/skip
				skip = true;
			} else if (actionCodes[i] == 2) {
				//done	
				complete = true;
			}
		}

		if (start) {
			startButton = new Button(buttonComposite, SWT.NULL, startImage);
			startButton.setData(this);
			startButton.setBackground(itemColor);
			startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
			startButton.setFAccessibleDescription(bodyText.getText());
			startButton.setFAccessibleName(startButton.getToolTipText());

			startButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					theview.runPerformAction(startButton);
				}
			});
		}
		if (skip) {
			skipButton = new Button(buttonComposite, SWT.NULL, skipImage);
			skipButton.setData(this);
			skipButton.setBackground(itemColor);
			skipButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.SKIP_TASK_TOOLTIP));
			skipButton.setFAccessibleName(skipButton.getToolTipText());
			skipButton.setFAccessibleDescription(bodyText.getText());

			skipButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					theview.advanceItem(skipButton, false);
				}
			});
		}
		if (complete) {
			completeButton = new Button(buttonComposite, SWT.NULL, completeImage);
			completeButton.setData(this);
			completeButton.setBackground(itemColor);
			completeButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COMPLETE_TASK_TOOLTIP));
			completeButton.setFAccessibleName(completeButton.getToolTipText());
			completeButton.setFAccessibleDescription(bodyText.getText());

			completeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
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
		
		
		buttonComposite = new Composite(bodyWrapperComposite, SWT.NULL);
		GridLayout xbuttonlayout = new GridLayout(1, false);
		xbuttonlayout.marginHeight = 2;
		xbuttonlayout.marginWidth = 2;
		xbuttonlayout.verticalSpacing = 2;
		GridData xbuttonData = new GridData(GridData.FILL_HORIZONTAL);
		xbuttonData.grabExcessHorizontalSpace = true;
		buttonComposite.setLayout(xbuttonlayout);
		buttonComposite.setLayoutData(xbuttonData);
		buttonComposite.setBackground(itemColor);

		buttonCompositeList.add(buttonComposite);
		//loop throught the number of sub items, make a new composite for each sub item.
		//Add the spacer, the label, then the buttons that are applicable for each sub item.
		for (int i = 0; i < sublist.length; i++) {
			int added = 0;
			//Get the sub item to add.
			SubItem sub = (SubItem) sublist[i];

			//composite added.
			Composite b = new Composite(buttonComposite, SWT.NULL);

			GridLayout buttonlayout = new GridLayout(6, false);
			buttonlayout.marginHeight = 2;
			buttonlayout.marginWidth = 2;
			buttonlayout.verticalSpacing = 2;
			GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
			buttonData.grabExcessHorizontalSpace = true;

			b.setLayout(buttonlayout);
			b.setLayoutData(buttonData);
			b.setBackground(itemColor);

			//Spacer label added.
			Label filllabel = new Label(b, SWT.NULL);
			filllabel.setBackground(itemColor);
			GridData filldata = new GridData();
			filldata.widthHint = 16;
			filllabel.setLayoutData(filldata);
			added++;

			//Now add the label.
			StyledText label = new StyledText(b, SWT.NULL);
			label.setText(sub.getLabel());
			GridData labelData = new GridData();
			label.setLayoutData(labelData);
			label.setBackground(itemColor);
			label.setEnabled(false);
			label.getCaret().setVisible(false);
			added++;

			//Bold the title.
			//			StyleRange r = new StyleRange(0, label.getText().length(), null, null, SWT.BOLD);
			//			label.setStyleRange(r);

			start = false;
			complete = false;
			skip = false;

			//Get the action codes to display buttons for.
			int[] actionCodes = getActionCodeArray(sub.getButtonCodes());

			//Add the icons in order.  Check each one when you add it.
			for (int j = 0; j < actionCodes.length; j++) {
				if (actionCodes[j] == 0) {
					//perform
					start = true;
				} else if (actionCodes[j] == 1) {
					//continue/skip
					skip = true;
				} else if (actionCodes[j] == 2) {
					//done	
					complete = true;
				}
			}
			final int fi = i;

			if (start) {
				added++;
				startButton = new Button(b, SWT.NULL, startImage);
				startButton.setData(this);
				startButton.setBackground(itemColor);
				startButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.PERFORM_TASK_TOOLTIP));
				startButton.setFAccessibleDescription(bodyText.getText());
				startButton.setFAccessibleName(startButton.getToolTipText());

				startButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						theview.runSubItemPerformAction(startButton, fi);
					}
				});
			}
			if (skip) {
				added++;
				skipButton = new Button(b, SWT.NULL, skipImage);
				skipButton.setData(this);
				skipButton.setBackground(itemColor);
				skipButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.SKIP_TASK_TOOLTIP));
				skipButton.setFAccessibleName(skipButton.getToolTipText());
				skipButton.setFAccessibleDescription(bodyText.getText());

				skipButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						theview.advanceSubItem(skipButton, false, fi);
					}
				});
			}
			if (complete) {
				added++;
				completeButton = new Button(b, SWT.NULL, completeImage);
				completeButton.setData(this);
				completeButton.setBackground(itemColor);
				completeButton.setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COMPLETE_TASK_TOOLTIP));
				completeButton.setFAccessibleName(completeButton.getToolTipText());
				completeButton.setFAccessibleDescription(bodyText.getText());

				completeButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						theview.advanceSubItem(completeButton, true, fi);
					}
				});
			}

			while (added < 6) {
				//Spacer label added.
				Label xfilllabel = new Label(b, SWT.NULL);
				xfilllabel.setBackground(itemColor);
				GridData xfilldata = new GridData(GridData.FILL_HORIZONTAL);
				xfilllabel.setLayoutData(xfilldata);
				added++;
			}
			buttonCompositeList.add(b);
			listOfSubItemCompositeHolders.add(new SubItemCompositeHolder(filllabel, startButton));
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
	 * Returns the complete.
	 * @return boolean
	 */
	//	public boolean isComplete() {
	//		//System.out.println("in isComplete()	"+hashCode()+"	"+titleText.getText()+"	"+start+"	"+skip+"	"+complete);
	//		return complete;
	//	}

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
