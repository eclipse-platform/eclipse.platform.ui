package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;

public abstract class BannerPage extends DialogPage {
	private Image bannerImage;
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
		Composite bannerParent = new Composite(client, SWT.BORDER);
		bannerParent.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		bannerParent.setLayout(layout);
		
		Label label = new Label(bannerParent, SWT.NULL);
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
		label.setImage(getBannerImage());
		
		Control contents = createContents(client);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(client);
	}
	
	protected URL getBannerImageURL() {
		return null;
	}
	private Image getBannerImage() {
		URL imageURL = getBannerImageURL();
		Image image=null;
		if (imageURL==null) {
			// use default
			bannerImage = UpdateUIPluginImages.DESC_INSTALL_BANNER.createImage();
			image = bannerImage;
		}
		return image;
	}
	
	public void dispose() {
		if (bannerImage!=null) {
			bannerImage.dispose();
		}
		super.dispose();
	}
	
	protected abstract Control createContents(Composite parent);
}