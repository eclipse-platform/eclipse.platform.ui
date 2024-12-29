/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.quicksearch.ITextViewerCreator;
import org.osgi.framework.Bundle;

/**
 * Creates <code>ITextViewerCreator</code>s from an <code>IConfigurationElement</code>.
 *
 * @see ITextViewerCreator
 */
public class ViewerDescriptor implements IViewerDescriptor {
	private final static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private final static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private final static String LINKED_EDITOR_ATTRIBUTE = "linkedEditor"; //$NON-NLS-1$
	private final static String LABEL_ATTRIBUTE = "label"; //$NON-NLS-1$
	private final static String ICON_ATTRIBUTE = "icon"; //$NON-NLS-1$

	private final IConfigurationElement fConfiguration;
	private final String fViewerId;
	private final String fLabel;
	private final String fLinkedEditorId;

	private ITextViewerCreator fViewerCreator;
	private Image fIcon;

	public ViewerDescriptor(IConfigurationElement config) {
		fConfiguration = config;
		fViewerId = config.getContributor().getName() + config.getAttribute(ID_ATTRIBUTE);
		fLabel = fConfiguration.getAttribute(LABEL_ATTRIBUTE);
		fLinkedEditorId = fConfiguration.getAttribute(LINKED_EDITOR_ATTRIBUTE);
	}

	@Override
	public ITextViewerCreator getViewerCreator() {
		if (fViewerCreator == null) {
			try {
				fViewerCreator = (ITextViewerCreator) fConfiguration.createExecutableExtension(CLASS_ATTRIBUTE);
			} catch (CoreException e) {
				QuickSearchActivator.log(e);
			}
		}
		return fViewerCreator;
	}

	@Override
	public String getLabel() {
		return fLabel;
	}

	String getLinkedEditorId() {
		return fLinkedEditorId;
	}

	String getViewerClass() {
		return fConfiguration.getAttribute(CLASS_ATTRIBUTE);
	}

	@Override
	public Image getIcon() {
		if (fIcon == null) {
			var icon = getIconImageDescriptor();
			if (icon == null) {
				icon = ImageDescriptor.getMissingImageDescriptor();
			}
			QuickSearchPluginImages.getImageRegistry().put(fViewerId, icon);
			fIcon = QuickSearchPluginImages.getImage(fViewerId);
		}
		return fIcon;
	}

	private ImageDescriptor getIconImageDescriptor() {
		String iconPath = fConfiguration.getAttribute(ICON_ATTRIBUTE);
		if (iconPath != null) {
			Bundle bundle = Platform.getBundle(fConfiguration.getContributor().getName());
			return ImageDescriptor.createFromURLSupplier(true, () -> {
				URL iconURL = FileLocator.find(bundle, IPath.fromOSString(iconPath), null);
				if (iconURL != null) {
					return iconURL;
				} else { // try to search as a URL in case it is absolute path
					try {
						return FileLocator.find(new URI(iconPath).toURL());
					} catch (MalformedURLException | URISyntaxException e) {
						// return null
					}
				}
				return null;
			});
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ViewerDescriptor ["); //$NON-NLS-1$
		sb.append("fViewerId="); //$NON-NLS-1$
		sb.append(fViewerId);
		sb.append(", "); //$NON-NLS-1$
		if (fViewerCreator != null) {
			sb.append("viewerCreator="); //$NON-NLS-1$
			sb.append(fViewerCreator);
			sb.append(", "); //$NON-NLS-1$
		}
		String viewerClass = getViewerClass();
		if (viewerClass != null) {
			sb.append("viewerClass="); //$NON-NLS-1$
			sb.append(viewerClass);
			sb.append(", "); //$NON-NLS-1$
		}
		if (fConfiguration != null) {
			sb.append("configuration="); //$NON-NLS-1$
			sb.append(fConfiguration);
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
