/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Grant (Google) - initial implementation
 ******************************************************************************/

package org.eclipse.jface.resource;

import java.util.function.Supplier;

import org.eclipse.swt.graphics.ImageData;

/**
 * ImageDescriptor which gets ImageData from a supplier.
 *
 * @since 3.12
 */
final class SuppliedImageDescriptor extends ImageDescriptor {

	private final Supplier<ImageData> supplier;

	SuppliedImageDescriptor(Supplier<ImageData> supplier) {
		this.supplier = supplier;
	}

	@Override
	public ImageData getImageData() {
		return supplier.get();
	}
}
