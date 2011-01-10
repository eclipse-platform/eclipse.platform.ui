/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.LinkedList;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.ProductProperties;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.about.AboutBundleGroupData;
import org.eclipse.ui.internal.about.AboutFeaturesButtonManager;
import org.eclipse.ui.internal.about.AboutItem;
import org.eclipse.ui.internal.about.AboutTextManager;
import org.eclipse.ui.internal.about.InstallationDialog;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Displays information about the product.
 */
public class AboutDialog extends TrayDialog {
    private final static int MAX_IMAGE_WIDTH_FOR_TEXT = 250;
	private final static int TEXT_MARGIN = 5;

    private final static int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;

    private String productName;

    private IProduct product;

    private AboutBundleGroupData[] bundleGroupInfos;

    private ArrayList images = new ArrayList();

    private AboutFeaturesButtonManager buttonManager = new AboutFeaturesButtonManager();

    private StyledText text;
    
    private AboutTextManager aboutTextManager;

	private AboutItem item;

    /**
     * Create an instance of the AboutDialog for the given window.
     * @param parentShell The parent of the dialog.
     */
    public AboutDialog(Shell parentShell) {
        super(parentShell);

        product = Platform.getProduct();
        if (product != null) {
			productName = product.getName();
		}
        if (productName == null) {
			productName = WorkbenchMessages.AboutDialog_defaultProductName;
		}

        // create a descriptive object for each BundleGroup
        IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
        LinkedList groups = new LinkedList();
        if (providers != null) {
			for (int i = 0; i < providers.length; ++i) {
                IBundleGroup[] bundleGroups = providers[i].getBundleGroups();
                for (int j = 0; j < bundleGroups.length; ++j) {
					groups.add(new AboutBundleGroupData(bundleGroups[j]));
				}
            }
		}
        bundleGroupInfos = (AboutBundleGroupData[]) groups
                .toArray(new AboutBundleGroupData[0]);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case DETAILS_ID:
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					InstallationDialog dialog = new InstallationDialog(getShell(), workbenchWindow);
					dialog.setModalParent(AboutDialog.this);
					dialog.open();	
				}
			});
            break;
        default:
            super.buttonPressed(buttonId);
            break;
        }
    }

    public boolean close() {
        // dispose all images
        for (int i = 0; i < images.size(); ++i) {
            Image image = (Image) images.get(i);
            image.dispose();
        }

        return super.close();
    }

    /*
     * (non-Javadoc) Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(NLS.bind(WorkbenchMessages.AboutDialog_shellTitle,productName ));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				IWorkbenchHelpContextIds.ABOUT_DIALOG);
    }

    /**
     * Add buttons to the dialog's button bar.
     * 
     * Subclasses should override.
     * 
     * @param parent
     *            the button bar composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton(parent, DETAILS_ID, WorkbenchMessages.AboutDialog_DetailsButton, false); 

        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        Button b = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        b.setFocus();
    }

    /**
     * Creates and returns the contents of the upper part 
     * of the dialog (above the button bar).
     *
     * Subclasses should overide.
     *
     * @param parent  the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
         // brand the about box if there is product info
        Image aboutImage = null;
		item = null;
        if (product != null) {
            ImageDescriptor imageDescriptor = ProductProperties
                    .getAboutImage(product);
            if (imageDescriptor != null) {
				aboutImage = imageDescriptor.createImage();
			}

            // if the about image is small enough, then show the text
            if (aboutImage == null
                    || aboutImage.getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
                String aboutText = ProductProperties.getAboutText(product);
                if (aboutText != null) {
					item = AboutTextManager.scan(aboutText);
				}
            }

            if (aboutImage != null) {
				images.add(aboutImage);
			}
        }

        // create a composite which is the parent of the top area and the bottom
        // button bar, this allows there to be a second child of this composite with 
        // a banner background on top but not have on the bottom
        Composite workArea = new Composite(parent, SWT.NONE);
        GridLayout workLayout = new GridLayout();
        workLayout.marginHeight = 0;
        workLayout.marginWidth = 0;
        workLayout.verticalSpacing = 0;
        workLayout.horizontalSpacing = 0;
        workArea.setLayout(workLayout);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // page group
        Color background = JFaceColors.getBannerBackground(parent.getDisplay());
        Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());
        Composite top = (Composite) super.createDialogArea(workArea);

        // override any layout inherited from createDialogArea 
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        top.setLayout(layout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        top.setBackground(background);
        top.setForeground(foreground);

        // the image & text	
        final Composite topContainer = new Composite(top, SWT.NONE);
        topContainer.setBackground(background);
        topContainer.setForeground(foreground);

        layout = new GridLayout();
        layout.numColumns = (aboutImage == null || item == null ? 1 : 2);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        topContainer.setLayout(layout);
        

		// Calculate a good height for the text
        GC gc = new GC(parent);
		int lineHeight = gc.getFontMetrics().getHeight();
		gc.dispose();

        int topContainerHeightHint = 100;
        
		topContainerHeightHint = Math.max(topContainerHeightHint, lineHeight * 6);

        //image on left side of dialog
        if (aboutImage != null) {
            Label imageLabel = new Label(topContainer, SWT.NONE);
            imageLabel.setBackground(background);
            imageLabel.setForeground(foreground);

            GridData data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.BEGINNING;
            data.grabExcessHorizontalSpace = false;
            imageLabel.setLayoutData(data);
            imageLabel.setImage(aboutImage);
            topContainerHeightHint = Math.max(topContainerHeightHint, aboutImage.getBounds().height);
        }
        
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        topContainer.setLayoutData(data);
		// used only to drive initial size so that there are no hints in the
		// layout data
		topContainer.setSize(432, topContainerHeightHint);
        
        if (item != null) {
			text = new StyledText(topContainer, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
			configureText(topContainer);

			// computing trim for later
			Rectangle rect = text.computeTrim(0, 0, 100, 100);
			final int xTrim = rect.width - 100;
			final int yTrim = rect.height - 100;

			topContainer.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					text.setSize(SWT.DEFAULT, SWT.DEFAULT);
					topContainer.layout(true);
					// do we need a scroll bar?
					Point size = text.getSize();
					int availableHeight = size.y - yTrim;
					int availableWidth = size.x - xTrim - (2 * TEXT_MARGIN);
					Point newSize = text.computeSize(availableWidth, SWT.DEFAULT, true);
					int style = text.getStyle();
					if (newSize.y > availableHeight) {
						if ((style & SWT.V_SCROLL) == 0) {
							recreateWrappedText(topContainer, true);
						}
					} else {
						if ((style & SWT.V_SCROLL) != 0) {
							recreateWrappedText(topContainer, false);
						}
					}
					topContainer.layout(true);
				}
			});
		}

        // horizontal bar
        Label bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);

        // add image buttons for bundle groups that have them
        Composite bottom = (Composite) super.createDialogArea(workArea);
        // override any layout inherited from createDialogArea 
        layout = new GridLayout();
        bottom.setLayout(layout);
        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        
        bottom.setLayoutData(data);

        createFeatureImageButtonRow(bottom);

        // spacer
        bar = new Label(bottom, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);

        return workArea;
    }

	void recreateWrappedText(Composite parent, boolean withScrolling) {
		int style = text.getStyle();
		if (withScrolling) {
			style |= SWT.V_SCROLL;
		} else {
			style ^= SWT.V_SCROLL;
		}
		boolean hasFocus = text.isFocusControl();
		Point selection = text.getSelection();
		text.dispose();
		text = new StyledText(parent, style);
		configureText(parent);
		if (hasFocus) {
			text.setFocus();
		}
		text.setSelection(selection);
	}

	void configureText(final Composite parent) {
		// Don't set caret to 'null' as this causes
		// https://bugs.eclipse.org/293263.
		// text.setCaret(null);
		Color background = JFaceColors.getBannerBackground(parent.getDisplay());
		Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());

		text.setFont(parent.getFont());
		text.setText(item.getText());
		text.setCursor(null);
		text.setBackground(background);
		text.setForeground(foreground);
		text.setMargins(TEXT_MARGIN, TEXT_MARGIN, TEXT_MARGIN, 0);

		aboutTextManager = new AboutTextManager(text);
		aboutTextManager.setItem(item);

		createTextMenu();

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		text.setLayoutData(gd);
	}

    /**
	 * Create the context menu for the text widget.
	 * 
	 * @since 3.4
	 */
	private void createTextMenu() {
		final MenuManager textManager = new MenuManager();
		textManager.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, IWorkbenchCommandConstants.EDIT_COPY,
						CommandContributionItem.STYLE_PUSH)));
		textManager.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, IWorkbenchCommandConstants.EDIT_SELECT_ALL,
						CommandContributionItem.STYLE_PUSH)));
		text.setMenu(textManager.createContextMenu(text));
		text.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				textManager.dispose();
			}
		});
		
	}

	private void createFeatureImageButtonRow(Composite parent) {
        Composite featureContainer = new Composite(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        featureContainer.setLayout(rowLayout);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        featureContainer.setLayoutData(data);

        for (int i = 0; i < bundleGroupInfos.length; i++) {
			createFeatureButton(featureContainer, bundleGroupInfos[i]);
		}
    }

    private Button createFeatureButton(Composite parent,
            final AboutBundleGroupData info) {
        if (!buttonManager.add(info)) {
			return null;
		}

        ImageDescriptor desc = info.getFeatureImage();
        Image featureImage = null;

        Button button = new Button(parent, SWT.FLAT | SWT.PUSH);
        button.setData(info);
        featureImage = desc.createImage();
        images.add(featureImage);
        button.setImage(featureImage);
        button.setToolTipText(info.getProviderName());
        
        button.getAccessible().addAccessibleListener(new AccessibleAdapter(){
        	/* (non-Javadoc)
			 * @see org.eclipse.swt.accessibility.AccessibleAdapter#getName(org.eclipse.swt.accessibility.AccessibleEvent)
			 */
			public void getName(AccessibleEvent e) {
				e.result = info.getProviderName();
			}
        });
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                AboutBundleGroupData[] groupInfos = buttonManager
                        .getRelatedInfos(info);
                AboutBundleGroupData selection = (AboutBundleGroupData) event.widget
                        .getData();

                AboutFeaturesDialog d = new AboutFeaturesDialog(getShell(),
                        productName, groupInfos, selection);
                d.open();
            }
        });

        return button;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}
}
