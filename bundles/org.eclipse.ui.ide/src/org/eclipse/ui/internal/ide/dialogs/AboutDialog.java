/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * Displays information about the product.
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class AboutDialog extends ProductInfoDialog {
	private final static int MAX_IMAGE_WIDTH_FOR_TEXT = 250;
	private final static int FEATURES_ID = IDialogConstants.CLIENT_ID + 1;
	private final static int PLUGINS_ID = IDialogConstants.CLIENT_ID + 2;
	private final static int INFO_ID = IDialogConstants.CLIENT_ID + 3;

	/**
	 * About info for the primary feature.
	 * Private field used in inner class.
	 * @issue org.eclipse.ui.internal.AboutInfo - illegal reference to generic workbench internals
	 */
	/* package */ AboutInfo primaryInfo;

	/**
	 * About info for the all features.
	 * Private field used in inner class.
	 * @issue org.eclipse.ui.internal.AboutInfo - illegal reference to generic workbench internals
	 */
	/* package */ AboutInfo[] featureInfos;
	private Image image; //image to display on dialog

	private ArrayList images = new ArrayList();
	private StyledText text;

	/**
	 * Create an instance of the AboutDialog
	 */
	public AboutDialog(
		IWorkbenchWindow window,
		AboutInfo primaryInfo,
		AboutInfo[] featureInfos) {
		super(window.getShell());
		this.primaryInfo = primaryInfo;
		this.featureInfos = featureInfos;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case FEATURES_ID :
				new AboutFeaturesDialog(getShell(), primaryInfo, featureInfos)
					.open();
				return;
			case PLUGINS_ID :
				new AboutPluginsDialog(getShell(), primaryInfo).open();
				return;
			case INFO_ID :
				new SystemSummaryDialog(getShell()).open();
				return;
		}

		super.buttonPressed(buttonId);
	}

	public boolean close() {
		//get rid of the image that was displayed on the left-hand side of the Welcome dialog
		if (image != null) {
			image.dispose();
		}
		for (int i = 0; i < images.size(); i++) {
			((Image) images.get(i)).dispose();
		}
		return super.close();
	}
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String name = null;
		if (primaryInfo != null) {
			name = primaryInfo.getProductName();
		}
		if (name != null) {
			newShell.setText(IDEWorkbenchMessages.format("AboutDialog.shellTitle", new Object[] { name })); //$NON-NLS-1$
		}
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.ABOUT_DIALOG);
	}
	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses should override.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, FEATURES_ID, IDEWorkbenchMessages.getString("AboutDialog.featureInfo"), false); //$NON-NLS-1$
		createButton(parent, PLUGINS_ID, IDEWorkbenchMessages.getString("AboutDialog.pluginInfo"), false); //$NON-NLS-1$
		createButton(parent, INFO_ID, IDEWorkbenchMessages.getString("AboutDialog.systemInfo"), false); //$NON-NLS-1$

		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		Button b =
			createButton(
				parent,
				IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL,
				true);
		b.setFocus();
	}
	/**
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 *
	 * Subclasses should overide.
	 *
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		setHandCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_HAND));
		setBusyCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT));
		getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (getHandCursor() != null) {
					getHandCursor().dispose();
				}
				if (getBusyCursor() != null) {
					getBusyCursor().dispose();
				}
			}
		});

		ImageDescriptor imageDescriptor = null;
		if (primaryInfo != null) {
			imageDescriptor = primaryInfo.getAboutImage(); // may be null
		}
		if (imageDescriptor != null) {
			image = imageDescriptor.createImage();
		}
		if (image == null
			|| image.getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
			// show text
			String aboutText = null;
			if (primaryInfo != null) {
				aboutText = primaryInfo.getAboutText(); // may be null
			}
			if (aboutText != null) {
				// get an about item
				setItem(scan(aboutText));
			}
		}

		// page group
		Composite outer = (Composite) super.createDialogArea(parent);
		outer.setSize(outer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		GridLayout layout = new GridLayout();
		outer.setLayout(layout);
		outer.setLayoutData(new GridData(GridData.FILL_BOTH));

		// the image & text	
		Composite topContainer = new Composite(outer, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = (image == null || getItem() == null ? 1 : 2);
		layout.marginWidth = 0;
		topContainer.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		topContainer.setLayoutData(data);

		//image on left side of dialog
		if (image != null) {
			Label imageLabel = new Label(topContainer, SWT.NONE);
			data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.verticalAlignment = GridData.BEGINNING;
			data.grabExcessHorizontalSpace = false;
			imageLabel.setLayoutData(data);
			imageLabel.setImage(image);
		}

		if (getItem() != null) {
			// text on the right
			text = new StyledText(topContainer, SWT.MULTI | SWT.READ_ONLY);
			text.setCaret(null);
			text.setFont(parent.getFont());
			data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.verticalAlignment = GridData.BEGINNING;
			data.grabExcessHorizontalSpace = true;
			text.setText(getItem().getText());
			text.setLayoutData(data);
			text.setCursor(null);
			text.setBackground(topContainer.getBackground());
			setLinkRanges(text, getItem().getLinkRanges());
			addListeners(text);
		}

		// horizontal bar
		Label bar = new Label(outer, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		bar.setLayoutData(data);

		// feature images
		Composite featureContainer = new Composite(outer, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = true;
		featureContainer.setLayout(rowLayout);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		featureContainer.setLayoutData(data);

		final AboutInfo[] infoArray = getFeaturesWithImages();
		for (int i = 0; i < infoArray.length; i++) {
			ImageDescriptor desc = infoArray[i].getFeatureImage();
			Image featureImage = null;
			if (desc != null) {
				Button button =
					new Button(featureContainer, SWT.FLAT | SWT.PUSH);
				button.setData(infoArray[i]);
				featureImage = desc.createImage();
				images.add(featureImage);
				button.setImage(featureImage);
				String name = infoArray[i].getProviderName();
				if (name == null) {
					name = ""; //$NON-NLS-1$
				}
				button.setToolTipText(name);
				button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						AboutFeaturesDialog d =
							new AboutFeaturesDialog(
								getShell(),
								primaryInfo,
								featureInfos);
						d.setInitialSelection(
							(AboutInfo) event.widget.getData());
						d.open();
					}
				});
			}
		}

		// spacer
		bar = new Label(outer, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		bar.setLayoutData(data);

		return outer;
	}

	/**
	 * Returns the feature info for non-primary features with a feature image.
	 * If several features share the same image bitmap, include only one per
	 * provider.
	 */
	private AboutInfo[] getFeaturesWithImages() {
		// quickly exclude any that do not have a provider name and image
		List infoList = new ArrayList(featureInfos.length + 1);

		// make sure primary is first
		if (primaryInfo != null && primaryInfo.getProviderName() != null
				&& primaryInfo.getFeatureImageName() != null) {
			infoList.add(primaryInfo);
		}

		for (int i = 0; i < featureInfos.length; i++) {
			if (featureInfos[i].getProviderName() != null
				&& featureInfos[i].getFeatureImageName() != null) {
				infoList.add(featureInfos[i]);
			}
		}
		List keepers = new ArrayList(infoList.size());

		// precompute CRCs of all the feature images
		long[] featureImageCRCs = new long[infoList.size()];
		for (int i = 0; i < infoList.size(); i++) {
			AboutInfo info = (AboutInfo) infoList.get(i);
			featureImageCRCs[i] = info.getFeatureImageCRC().longValue();
		}
		for (int i = 0; i < infoList.size(); i++) {
			AboutInfo outer = (AboutInfo) infoList.get(i);
			boolean found = false;
			// see whether we already have one for same provider and same image
			for (int j = 0; j < keepers.size(); j++) {
				AboutInfo k = (AboutInfo) keepers.get(j);
				if (k.getProviderName().equals(outer.getProviderName())
					&& featureImageCRCs[j] == featureImageCRCs[i]) {
					found = true;
					break;
				}
			}
			if (!found) {
				keepers.add(outer);
			}
		}
		return (AboutInfo[]) keepers.toArray(new AboutInfo[keepers.size()]);
	}
}
