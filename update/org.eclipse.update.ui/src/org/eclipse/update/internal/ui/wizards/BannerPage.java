package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.internal.ui.*;
import java.net.URL;
import org.eclipse.swt.graphics.Image;

public abstract class BannerPage extends WizardPage {
	private Image bannerImage;
	private boolean bannerVisible = true;
	public BannerPage(String name) {
		super(name);
	}
	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 10;
		client.setLayout(layout);

		if (bannerVisible) {
			Composite bannerParent =
				new Composite(client, SWT.NULL /*SWT.BORDER */
			);
			bannerParent.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			layout = new GridLayout();
			layout.marginWidth = layout.marginHeight = 0;
			bannerParent.setLayout(layout);

			Label label = new Label(bannerParent, SWT.NULL);
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			label.setImage(getBannerImage());
		}

		Control contents = createContents(client);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(client);
	}

	public void setBannerVisible(boolean visible) {
		this.bannerVisible = visible;
	}
	public boolean isBannedVisible() {
		return bannerVisible;
	}

	protected URL getBannerImageURL() {
		return null;
	}
	private Image getBannerImage() {
		URL imageURL = getBannerImageURL();
		Image image = null;
		if (imageURL == null) {
			// use default
			bannerImage =
				UpdateUIPluginImages.DESC_INSTALL_BANNER.createImage();
			image = bannerImage;
		}
		return image;
	}

	public void dispose() {
		if (bannerImage != null) {
			bannerImage.dispose();
		}
		super.dispose();
	}

	protected abstract Control createContents(Composite parent);
}