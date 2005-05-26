/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class StartupProgressMonitorDialog extends ProgressMonitorDialog {

	private final static int MAX_IMAGE_WIDTH_FOR_TEXT = 250;

	private Image aboutImage = null;

	public StartupProgressMonitorDialog(Shell parent) {
		super(parent);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		String productName = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			productName = product.getName();
		}
		if (productName == null) {
			productName =  WorkbenchMessages.Startup_DefaultProductName;
		}
		String productStarting = NLS.bind(WorkbenchMessages.Startup_Starting,
				productName);
		shell.setText(productStarting);
	}

	protected Control createContents(Composite parent) {

		IProduct product = Platform.getProduct();
		boolean showProgressUnderImage = false;
		// brand the about box if there is product info
		aboutImage = null;
		if (product != null) {
			ImageDescriptor imageDescriptor = ProductProperties
					.getAboutImage(product);
			if (imageDescriptor != null)
				aboutImage = imageDescriptor.createImage();

			if (aboutImage != null) {
				// if the about image is too large, show progress underneath
				if (aboutImage.getBounds().width > MAX_IMAGE_WIDTH_FOR_TEXT) {
					showProgressUnderImage = true;
				}
			}
		}

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = showProgressUnderImage ? 1 : 2;
		//gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		container.setLayout(gridLayout);

		GridData data;
		if (aboutImage != null) {
			Label imageLabel = new Label(container, SWT.NONE);

			data = new GridData();
			data.horizontalAlignment = SWT.LEFT;
			data.verticalAlignment = SWT.TOP;
			data.grabExcessHorizontalSpace = false;
			imageLabel.setLayoutData(data);
			imageLabel.setImage(aboutImage);
		}

		Composite progressArea = new Composite(container, SWT.NONE);
		super.createContents(progressArea);
		
		return container;
	}
	
	protected Control createButtonBar(Composite parent) {
		return null; //super.createButtonBar(parent);
	}

	public boolean close() {
		if (aboutImage != null) {
			aboutImage.dispose();
			aboutImage = null;
		}

		return super.close();
	}
	
	protected Image getImage() {
		// no image in the progress area if we have an about image
		return aboutImage == null ? super.getImage() : null;
	}

}
