/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignedContentFactory;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * A small class to manage the about dialog information for a single bundle.
 *
 * @since 3.0
 */
public class AboutBundleData extends AboutData {

	private Bundle bundle;

	private boolean isSignedDetermined = false;

	private boolean isSigned;

	public AboutBundleData(Bundle bundle) {
		super(getResourceString(bundle, Constants.BUNDLE_VENDOR), getResourceString(bundle, Constants.BUNDLE_NAME),
				getResourceString(bundle, Constants.BUNDLE_VERSION), bundle.getSymbolicName());

		this.bundle = bundle;

	}

	public int getState() {
		return bundle.getState();
	}

	/**
	 * @return a string representation of the argument state. Does not return null.
	 */
	public String getStateName() {
		switch (getState()) {
		case Bundle.INSTALLED:
			return WorkbenchMessages.AboutPluginsDialog_state_installed;
		case Bundle.RESOLVED:
			return WorkbenchMessages.AboutPluginsDialog_state_resolved;
		case Bundle.STARTING:
			return WorkbenchMessages.AboutPluginsDialog_state_starting;
		case Bundle.STOPPING:
			return WorkbenchMessages.AboutPluginsDialog_state_stopping;
		case Bundle.UNINSTALLED:
			return WorkbenchMessages.AboutPluginsDialog_state_uninstalled;
		case Bundle.ACTIVE:
			return WorkbenchMessages.AboutPluginsDialog_state_active;
		default:
			return WorkbenchMessages.AboutPluginsDialog_state_unknown;
		}
	}

	/**
	 * A function to translate the resource tags that may be embedded in a string
	 * associated with some bundle.
	 *
	 * @param headerName the used to retrieve the correct string
	 * @return the string or null if the string cannot be found
	 */
	private static String getResourceString(Bundle bundle, String headerName) {
		String value = bundle.getHeaders().get(headerName);
		return value == null ? null : Platform.getResourceString(bundle, value);
	}

	public boolean isSignedDetermined() {
		return isSignedDetermined;
	}

	public boolean isSigned() throws IllegalStateException {
		if (isSignedDetermined)
			return isSigned;
		SignedContent signedContent = getSignedContent();
		isSigned = signedContent != null && signedContent.isSigned();
		isSignedDetermined = true;
		return isSigned;
	}

	/**
	 * @return current bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * Returns the signed content for the associated bundle, or null if it cannot be
	 * found.
	 *
	 * @return the signed content for the bundle
	 * @throws IllegalStateException if there is an IOException or
	 *                               GeneralSecurityException raised while finding
	 *                               signed content.
	 */
	public SignedContent getSignedContent() throws IllegalStateException {
		final SignedContent[] result = new SignedContent[1];
		ServiceCaller.callOnce(getClass(), SignedContentFactory.class, (contentFactory) -> {
			try {
				result[0] = contentFactory.getSignedContent(bundle);
			} catch (IOException | GeneralSecurityException e) {
				throw new IllegalStateException(e);
			}
		});
		return result[0];
	}
}
