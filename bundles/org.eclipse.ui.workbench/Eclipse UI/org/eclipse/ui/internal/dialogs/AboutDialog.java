/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440149, 472654
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 496319, 498301
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 527162
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.ui.commands.ICommandService;
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
import org.eclipse.ui.services.IServiceLocator;

/**
 * Displays information about the product.
 */
public class AboutDialog extends TrayDialog {
	private static final String COPY_BUILD_ID_COMMAND = "org.eclipse.ui.ide.copyBuildIdCommand"; //$NON-NLS-1$

	private static final int MAX_IMAGE_WIDTH_FOR_TEXT = 250;

	private static final int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;

	private String productName;

	private IProduct product;

	private AboutBundleGroupData[] bundleGroupInfos;

	private ArrayList<Image> images = new ArrayList<>();

	private AboutFeaturesButtonManager buttonManager = new AboutFeaturesButtonManager();

	private StyledText text;

	private AboutTextManager aboutTextManager;

	/**
	 * Create an instance of the AboutDialog for the given window.
	 *
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
		LinkedList<AboutBundleGroupData> groups = new LinkedList<>();
		if (providers != null) {
			for (IBundleGroupProvider provider : providers) {
				IBundleGroup[] bundleGroups = provider.getBundleGroups();
				for (IBundleGroup bundleGroup : bundleGroups) {
					groups.add(new AboutBundleGroupData(bundleGroup));
				}
			}
		}
		bundleGroupInfos = groups.toArray(new AboutBundleGroupData[0]);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case DETAILS_ID:
			BusyIndicator.showWhile(getShell().getDisplay(), () -> {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				InstallationDialog dialog = new InstallationDialog(getShell(), workbenchWindow);
				dialog.setModalParent(AboutDialog.this);
				dialog.open();
			});
			break;
		default:
			super.buttonPressed(buttonId);
			break;
		}
	}

	@Override
	public boolean close() {
		// dispose all images
		for (Image image : images) {
			image.dispose();
		}
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(NLS.bind(WorkbenchMessages.AboutDialog_shellTitle, productName));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IWorkbenchHelpContextIds.ABOUT_DIALOG);
	}

	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses should override.
	 *
	 * @param parent the button bar composite
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, DETAILS_ID, WorkbenchMessages.AboutDialog_DetailsButton, false);

		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		Button b = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
		b.setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// brand the about box if there is product info
		Image aboutImage = null;
		AboutItem item = null;
		if (product != null) {
			ImageDescriptor imageDescriptor = ProductProperties.getAboutImage(product);
			if (imageDescriptor != null) {
				aboutImage = imageDescriptor.createImage();
			}

			// if the about image is small enough, then show the text
			if (aboutImage == null || aboutImage.getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
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

		GC gc = new GC(parent);
		// arbitrary default
		int topContainerHeightHint = 100;
		try {
			// default height enough for 6 lines of text
			topContainerHeightHint = Math.max(topContainerHeightHint, gc.getFontMetrics().getHeight() * 6);
		} finally {
			gc.dispose();
		}

		// image on left side of dialog
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

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = topContainerHeightHint;
		topContainer.setLayoutData(data);

		if (item != null) {
			final int minWidth = 400; // This value should really be calculated
			// from the computeSize(SWT.DEFAULT,
			// SWT.DEFAULT) of all the
			// children in infoArea excluding the
			// wrapped styled text
			// There is no easy way to do this.
			final ScrolledComposite scroller = new ScrolledComposite(topContainer, SWT.V_SCROLL | SWT.H_SCROLL);
			data = new GridData(GridData.FILL_BOTH);
			data.widthHint = minWidth;
			scroller.setLayoutData(data);

			final Composite textComposite = new Composite(scroller, SWT.NONE);
			textComposite.setBackground(background);

			layout = new GridLayout();
			textComposite.setLayout(layout);

			text = new StyledText(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);

			// Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263.
//    		text.setCaret(null);

			text.setFont(parent.getFont());
			text.setText(item.getText());
			text.setCursor(null);
			text.setBackground(background);
			text.setForeground(foreground);

			aboutTextManager = new AboutTextManager(text);
			aboutTextManager.setItem(item);

			createTextMenu();

			GridData gd = new GridData();
			gd.verticalAlignment = GridData.BEGINNING;
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			text.setLayoutData(gd);

			// Adjust the scrollbar increments
			scroller.getHorizontalBar().setIncrement(20);
			scroller.getVerticalBar().setIncrement(20);

			final boolean[] inresize = new boolean[1]; // flag to stop unneccesary
			// recursion
			textComposite.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					if (inresize[0]) {
						return;
					}
					inresize[0] = true;
					// required because of bugzilla report 4579
					textComposite.layout(true);
					// required because you want to change the height that the
					// scrollbar will scroll over when the width changes.
					int width = textComposite.getClientArea().width;
					Point p = textComposite.computeSize(width, SWT.DEFAULT);
					scroller.setMinSize(minWidth, p.y);
					inresize[0] = false;
				}
			});

			scroller.setExpandHorizontal(true);
			scroller.setExpandVertical(true);
			Point p = textComposite.computeSize(minWidth, SWT.DEFAULT);
			textComposite.setSize(p.x, p.y);
			scroller.setMinWidth(minWidth);
			scroller.setMinHeight(p.y);

			scroller.setContent(textComposite);
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

	/**
	 * Create the context menu for the text widget.
	 *
	 * @since 3.4
	 */
	private void createTextMenu() {
		final MenuManager textManager = new MenuManager();
		IServiceLocator serviceLocator = PlatformUI.getWorkbench();
		ICommandService commandService = serviceLocator.getService(ICommandService.class);
		textManager.add(new CommandContributionItem(new CommandContributionItemParameter(serviceLocator, null,
				IWorkbenchCommandConstants.EDIT_COPY, CommandContributionItem.STYLE_PUSH)));
		if (commandService.getCommand(COPY_BUILD_ID_COMMAND).isDefined()) {
			textManager.add(new CommandContributionItem(new CommandContributionItemParameter(serviceLocator, null,
					COPY_BUILD_ID_COMMAND, CommandContributionItem.STYLE_PUSH)));
		}
		textManager.add(new CommandContributionItem(new CommandContributionItemParameter(serviceLocator, null,
				IWorkbenchCommandConstants.EDIT_SELECT_ALL, CommandContributionItem.STYLE_PUSH)));
		text.setMenu(textManager.createContextMenu(text));
		text.addDisposeListener(e -> textManager.dispose());

	}

	private void createFeatureImageButtonRow(Composite parent) {
		Composite featureContainer = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = true;
		featureContainer.setLayout(rowLayout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		featureContainer.setLayoutData(data);

		for (AboutBundleGroupData bundleGroupInfo : bundleGroupInfos) {
			createFeatureButton(featureContainer, bundleGroupInfo);
		}
	}

	private Button createFeatureButton(Composite parent, final AboutBundleGroupData info) {
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

		button.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = info.getProviderName();
			}
		});
		button.addSelectionListener(widgetSelectedAdapter(event -> {
			AboutBundleGroupData[] groupInfos = buttonManager.getRelatedInfos(info);
			AboutBundleGroupData selection = (AboutBundleGroupData) event.widget.getData();
			AboutFeaturesDialog d = new AboutFeaturesDialog(getShell(), productName, groupInfos, selection);
			d.open();
		}));

		return button;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
