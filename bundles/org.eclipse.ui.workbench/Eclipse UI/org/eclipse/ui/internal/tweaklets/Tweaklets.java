/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @since 3.3
 *
 */
public class Tweaklets {

	public static class TweakKey {
		Class tweakClass;

		/**
		 * @param tweakClass
		 */
		public TweakKey(Class tweakClass) {
			this.tweakClass = tweakClass;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(tweakClass);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final TweakKey other = (TweakKey) obj;
			return Objects.equals(tweakClass, other.tweakClass);
		}
	}

	private static Map defaults = new HashMap();
	private static Map tweaklets = new HashMap();

	public static void setDefault(TweakKey definition, Object implementation) {
		defaults.put(definition, implementation);
	}

	public static Object get(TweakKey definition) {
		Object result = tweaklets.get(definition);
		if (result == null) {
			result = createTweaklet(definition);
			if (result == null) {
				result = getDefault(definition);
			}
			Assert.isNotNull(result);
			tweaklets.put(definition, result);
		}
		return result;
	}

	/**
	 * @param definition
	 * @return
	 */
	private static Object getDefault(TweakKey definition) {
		return defaults.get(definition);
	}

	/**
	 * @param definition
	 * @return
	 */
	private static Object createTweaklet(TweakKey definition) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("org.eclipse.ui.internalTweaklets"); //$NON-NLS-1$
		for (IConfigurationElement element : elements) {
			if (definition.tweakClass.getName().equals(element.getAttribute("definition"))) { //$NON-NLS-1$
				try {
					Object tweaklet = element.createExecutableExtension("implementation"); //$NON-NLS-1$
					tweaklets.put(definition, tweaklet);
					return tweaklet;
				} catch (CoreException e) {
					StatusManager.getManager().handle(
							StatusUtil.newStatus(IStatus.ERROR, "Error with extension " + element, e), //$NON-NLS-1$
							StatusManager.LOG);
				}
			}
		}
		return null;
	}

}
