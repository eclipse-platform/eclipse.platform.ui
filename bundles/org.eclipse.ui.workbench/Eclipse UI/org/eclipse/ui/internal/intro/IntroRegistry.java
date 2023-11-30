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
package org.eclipse.ui.internal.intro;

import java.util.ArrayList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * Registry for introduction elements.
 *
 * @since 3.0
 */
public class IntroRegistry implements IIntroRegistry {
	private static final String TAG_INTRO = "intro";//$NON-NLS-1$

	private static final String TAG_INTROPRODUCTBINDING = "introProductBinding";//$NON-NLS-1$

	private static final String ATT_INTROID = "introId"; //$NON-NLS-1$

	private static final String ATT_PRODUCTID = "productId"; //$NON-NLS-1$

	@Override
	public int getIntroCount() {
		return getIntros().length;
	}

	@Override
	public IIntroDescriptor[] getIntros() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_INTRO);
		if (point == null) {
			return new IIntroDescriptor[0];
		}

		IExtension[] extensions = point.getExtensions();
		extensions = RegistryReader.orderExtensions(extensions);

		ArrayList<IIntroDescriptor> list = new ArrayList<>(extensions.length);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(TAG_INTRO)) {
					try {
						IIntroDescriptor descriptor = new IntroDescriptor(element);
						list.add(descriptor);
					} catch (CoreException e) {
						// log an error since its not safe to open a dialog here
						WorkbenchPlugin.log(IntroMessages.Intro_could_not_create_descriptor, e.getStatus());
					}
				}
			}
		}

		return list.toArray(new IIntroDescriptor[list.size()]);
	}

	@Override
	public IIntroDescriptor getIntroForProduct(String targetProductId) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_INTRO);
		if (point == null) {
			return null;
		}

		IExtension[] extensions = point.getExtensions();
		extensions = RegistryReader.orderExtensions(extensions);

		String targetIntroId = getIntroForProduct(targetProductId, extensions);
		if (targetIntroId == null) {
			return null;
		}

		IIntroDescriptor descriptor = null;

		IIntroDescriptor[] intros = getIntros();
		for (IIntroDescriptor intro : intros) {
			if (intro.getId().equals(targetIntroId)) {
				descriptor = intro;
				break;
			}
		}

		return descriptor;
	}

	private String getIntroForProduct(String targetProductId, IExtension[] extensions) {
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(TAG_INTROPRODUCTBINDING)) {
					String introId = element.getAttribute(ATT_INTROID);
					String productId = element.getAttribute(ATT_PRODUCTID);

					if (introId == null || productId == null) {
						IStatus status = new Status(IStatus.ERROR,
								element.getDeclaringExtension().getContributor().getName(), IStatus.ERROR,
								"introId and productId must be defined.", new IllegalArgumentException()); //$NON-NLS-1$
						WorkbenchPlugin.log("Invalid intro binding", status); //$NON-NLS-1$
						continue;
					}

					if (targetProductId.equals(productId)) {
						return introId;
					}
				}
			}
		}
		return null;
	}

	@Override
	public IIntroDescriptor getIntro(String id) {
		IIntroDescriptor[] intros = getIntros();
		for (IIntroDescriptor desc : intros) {
			if (desc.getId().equals(id)) {
				return desc;
			}
		}
		return null;
	}
}
