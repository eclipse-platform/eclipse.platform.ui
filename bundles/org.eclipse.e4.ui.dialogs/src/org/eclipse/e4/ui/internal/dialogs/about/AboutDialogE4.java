/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf Heydenreich - Bug 559694
 *******************************************************************************/
package org.eclipse.e4.ui.internal.dialogs.about;

import java.util.Optional;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.dialogs.textbundles.E4DialogMessages;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Displays information about the product.
 *
 */
public class AboutDialogE4 extends TrayDialog {

	private final int maxImageWidth = 250;

	private final ProductInformation product;

	private StyledText textWidget;

	private AboutText aboutText;

	/**
	 * Create an instance of the AboutDialogE4 for the given window.
	 *
	 * @param parentShell The parent of the dialog.
	 */
	public AboutDialogE4(final Shell parentShell) {
		super(parentShell);
		product = new ProductInformation(Platform.getProduct());
	}

	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(NLS.bind(E4DialogMessages.AboutDialog_shellTitle, product.getName()));

		// FIXME HelpSystem
		// EHelpService.setHelp(newShell, IWorkbenchHelpContextIds.ABOUT_DIALOG);
	}

	/**
	 * Creates and returns the contents of the upper part of the dialog (above the
	 * button bar).
	 *
	 * Subclasses should override.
	 *
	 * @param parent the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		// brand the about box if there is product info
		Optional<Image> aboutImage = Optional.empty();
		Optional<AboutItem> aboutItem = Optional.empty();

		// if the about image is small enough, then show the text
		aboutImage = product.getAboutImage();
		if (!aboutImage.isPresent() || aboutImage.get().getBounds().width <= maxImageWidth) {
			String aboutTextProperty = product.getAboutText();
			if (aboutTextProperty != null) {
				aboutText = new AboutText(aboutTextProperty);
				aboutItem = aboutText.getAboutItem();
			}
		}

		// create a composite which is the parent of the top area and the bottom
		// button bar, this allows there to be a second child of this composite
		// with a banner background on top but not have on the bottom
		Composite workArea = WidgetFactory.composite(SWT.NONE).layoutData(new GridData(GridData.FILL_BOTH))
				.create(parent);
		GridLayoutFactory.fillDefaults().applyTo(workArea);

		// page group
		Color background = JFaceColors.getBannerBackground(parent.getDisplay());
		Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());
		Composite top = (Composite) super.createDialogArea(workArea);

		// override any layout inherited from createDialogArea
		GridLayoutFactory.fillDefaults().applyTo(top);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setBackground(background);
		top.setForeground(foreground);

		// the image & text
		final Composite topContainer = WidgetFactory.composite(SWT.NONE).background(background).foreground(foreground)
				.create(top);
		GridLayoutFactory.fillDefaults().numColumns(aboutImage == null || !aboutItem.isPresent() ? 1 : 2)
		.applyTo(topContainer);

		int topContainerHeightHint = calculateTopContainerHeightAndCreateImage(aboutImage, background, foreground,
				topContainer,
				parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, topContainerHeightHint).applyTo(topContainer);

		aboutItem.ifPresent(c -> createTextWidget(parent, c, background, foreground, topContainer));

		return workArea;
	}

	private void createTextWidget(final Composite parent, AboutItem aboutItem, Color background, Color foreground,
			final Composite topContainer) {
		final Composite textComposite = WidgetFactory.composite(SWT.NONE).background(background).create(topContainer);

		GridDataFactory.fillDefaults().grab(true, true).hint(400, SWT.DEFAULT).applyTo(textComposite);
		GridLayoutFactory.fillDefaults().applyTo(textComposite);

		textWidget = new StyledText(textComposite, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		textWidget.setFont(parent.getFont());
		textWidget.setText(aboutItem.getText());
		textWidget.setBackground(background);
		textWidget.setForeground(foreground);
		textWidget.setAlwaysShowScrollBars(false);

		aboutText = new AboutText(textWidget, () -> aboutItem);

		createTextMenu();

		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(textWidget);
		textComposite.layout();
	}

	private int calculateTopContainerHeightAndCreateImage(Optional<Image> aboutImage,
			Color background,
			Color foreground,
			final Composite topContainer, Composite parent) {
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
		if (aboutImage.isPresent()) {
			Label imageLabel = WidgetFactory.label(SWT.NONE).background(background).foreground(foreground)
					.image(aboutImage.get()).create(topContainer);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(imageLabel);
			topContainerHeightHint = Math.max(topContainerHeightHint, aboutImage.get().getBounds().height);
		}
		return topContainerHeightHint;
	}

	/**
	 * Create the context menu for the text widget.
	 *
	 * @since 3.4
	 */
	private void createTextMenu() {
		final MenuManager popupManager = new MenuManager();

		// FIXME missing commands for EDIT_COPY and EDIT_SELECT_ALL
		// as context menu
		textWidget.setMenu(popupManager.createContextMenu(textWidget));
		textWidget.addDisposeListener(e -> popupManager.dispose());

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}
}
