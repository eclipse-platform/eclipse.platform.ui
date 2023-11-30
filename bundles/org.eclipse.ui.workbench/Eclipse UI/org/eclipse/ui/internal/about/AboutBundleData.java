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
import java.util.Date;
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

	private ExtendedSigningInfo info;

	private boolean isSignedDetermined = false;

	private boolean isSigned;

	public AboutBundleData(Bundle bundle) {
		this(bundle, null);
	}

	/**
	 *
	 * @param bundle the bundle.
	 * @param info   the extended bundle signing information.
	 *
	 * @since 3.125
	 */
	public AboutBundleData(Bundle bundle, ExtendedSigningInfo info) {
		super(getResourceString(bundle, Constants.BUNDLE_VENDOR), getResourceString(bundle, Constants.BUNDLE_NAME),
				getResourceString(bundle, Constants.BUNDLE_VERSION), bundle.getSymbolicName());

		this.bundle = bundle;
		this.info = info;
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
		if (!isSigned && info != null) {
			isSigned = info.isSigned(bundle);
		}
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
	 * @return the extended key signing info or <code>null</code>
	 *
	 * @since 3.125
	 */
	public ExtendedSigningInfo getInfo() {
		return info;
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
		ServiceCaller.callOnce(getClass(), SignedContentFactory.class, contentFactory -> {
			try {
				result[0] = contentFactory.getSignedContent(bundle);
			} catch (IOException | GeneralSecurityException e) {
				throw new IllegalStateException(e);
			}
		});
		return result[0];
	}

	/**
	 * Traditionally, the only form of signing support has been jar signing, as made
	 * available via {@link SignedContent}. This interface allows the
	 * {@link AboutPluginsPage AboutPluginsPage} to be adapted to support
	 * additional/alternative forms of signing.
	 *
	 * <p>
	 * The AboutPluginsPage uses the following idiom to acquire an instance of
	 * ExtendedSigningInfo and passes it to the
	 * {@link AboutBundleData#AboutBundleData(Bundle, ExtendedSigningInfo) new
	 * AboutBundleData constructor} and is available via
	 * {@link AboutBundleData#getInfo()}.
	 * </p>
	 *
	 * <pre>
	 * Platform.getAdapterManager().getAdapter(this, AboutBundleData.ExtendedSigningInfo.class);
	 * </pre>
	 *
	 * @see AboutBundleData#AboutBundleData(Bundle, ExtendedSigningInfo)
	 *
	 * @since 3.125
	 */
	public interface ExtendedSigningInfo {

		/**
		 * Whether this bundle is signed in an extended way.
		 *
		 * @param bundle the bundle in question.
		 * @return whether this bundle is signed.
		 */
		boolean isSigned(Bundle bundle);

		/**
		 * A user presentable description of the type of extended signing.
		 *
		 * @param bundle the bundle in question.
		 * @return a label for the type of signing, or <code>null</code> if the bundle
		 *         isn't signed in an extended way.
		 */
		String getSigningType(Bundle bundle);

		/**
		 * The date the bundle was signed.
		 *
		 * @param bundle the bundle in question.
		 * @return the date the bundle was signed, or <code>null</code> if the bundle
		 *         isn't signed.
		 */
		Date getSigningTime(Bundle bundle);

		/**
		 * A user presentable multi-line text description of the how the bundle is
		 * signed.
		 *
		 * @return a description of the how the bundle is signed.
		 */
		String getSigningDetails(Bundle bundle);
	}
}
