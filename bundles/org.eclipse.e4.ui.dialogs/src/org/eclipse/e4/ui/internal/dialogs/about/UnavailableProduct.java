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

import org.eclipse.core.runtime.IProduct;
import org.eclipse.e4.ui.dialogs.textbundles.E4DialogMessages;
import org.osgi.framework.Bundle;

/**
 * Dummy implementation if no product file / product information is available.
 */
public final class UnavailableProduct implements IProduct {

	@Override
	public String getApplication() {
		return "";
	}

	@Override
	public String getName() {
		return E4DialogMessages.AboutDialog_defaultProductName;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public String getProperty(String key) {
		return "";
	}

	@Override
	public Bundle getDefiningBundle() {
		return null;
	}

}
