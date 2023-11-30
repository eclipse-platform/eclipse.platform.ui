/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Ralf Heydenreich - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.internal.dialogs.about;

import java.util.Optional;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

/**
 * Encapsulates product information from product file.
 */
public final class ProductInformation {
	private final IProduct product;
	private final String productName;

	public ProductInformation() {
		this(new UnavailableProduct());
	}

	public ProductInformation(IProduct product) {
		// We are not failing for "null" here, because this is what we can get from
		// Platform, instead of that we substitute "null" with special implementation.
		this.product = Optional.ofNullable(product).orElse(new UnavailableProduct());

		this.productName = Optional.ofNullable(product.getName()).orElse("");
	}

	public String getName() {
		return productName;
	}

	public ImageDescriptor getAboutImageDescriptor() {
		if (JFaceResources.getImage(productName) == null) {
			createAboutImage();
		}

		return JFaceResources.getImageRegistry().getDescriptor(productName);
	}

	private void createAboutImage() {
		Optional<ImageDescriptor> imageDescriptor = ProductProperties.aboutImage(Optional.of(product));
		if (imageDescriptor.isPresent()) {
			JFaceResources.getImageRegistry().put(productName, imageDescriptor.get());
		}
	}

	public String getAboutText() {
		return Optional.ofNullable(ProductProperties.getAboutText(product)).orElse("");
	}

	public Optional<Image> getAboutImage() {
		if (JFaceResources.getImage(productName) == null) {
			createAboutImage();
		}
		return Optional.ofNullable(JFaceResources.getImage(productName));
	}

}
