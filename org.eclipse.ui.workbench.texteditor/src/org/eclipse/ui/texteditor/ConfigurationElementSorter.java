/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import org.eclipse.osgi.util.ManifestElement;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

/**
 * Allows to sort an array based on their elements' configuration elements
 * according to the prerequisite relation of their defining plug-ins.
 * <p>
 * This class may be subclassed.
 * </p>
 *
 * @since 3.0
 */
public abstract class ConfigurationElementSorter {

	/**
	 * Sorts the given array based on its elements' configuration elements
	 * according to the prerequisite relation of their defining plug-ins.
	 *
	 * @param elements the array to be sorted
	 */
	public final void sort(Object[] elements) {
		Arrays.sort(elements, new ConfigurationElementComparator(elements));
	}

	/**
	 * Returns the configuration element for the given object.
	 *
	 * @param object the object
	 * @return the object's configuration element, must not be <code>null</code>
	 */
	public abstract IConfigurationElement getConfigurationElement(Object object);

	/**
	 * Compare configuration elements according to the prerequisite relation
	 * of their defining plug-ins.
	 */
	private class ConfigurationElementComparator implements Comparator {

		private Map fDescriptorMapping;
		private Map fPrereqsMapping;

		public ConfigurationElementComparator(Object[] elements) {
			Assert.isNotNull(elements);
			initialize(elements);
		}

		/*
		 * @see Comparator#compare(java.lang.Object, java.lang.Object)
		 * @since 2.0
		 */
		public int compare(Object object0, Object object1) {

			if (dependsOn(object0, object1))
				return -1;

			if (dependsOn(object1, object0))
				return +1;

			return 0;
		}

		/**
		 * Returns whether one configuration element depends on the other element.
		 * This is done by checking the dependency chain of the defining plug-ins.
		 *
		 * @param element0 the first element
		 * @param element1 the second element
		 * @return <code>true</code> if <code>element0</code> depends on <code>element1</code>.
		 * @since 2.0
		 */
		private boolean dependsOn(Object element0, Object element1) {
			if (element0 == null || element1 == null)
				return false;

			String pluginDesc0= (String)fDescriptorMapping.get(element0);
			String pluginDesc1= (String)fDescriptorMapping.get(element1);

			// performance tuning - code below would give same result
			if (pluginDesc0.equals(pluginDesc1))
				return false;

			Set prereqUIds0= (Set)fPrereqsMapping.get(pluginDesc0);

			return prereqUIds0.contains(pluginDesc1);
		}

		/**
		 * Initialize this comparator.
		 *
		 * @param elements an array of Java editor hover descriptors
		 */
		private void initialize(Object[] elements) {
			int length= elements.length;
			fDescriptorMapping= new HashMap(length);
			fPrereqsMapping= new HashMap(length);
			Set fBundleSet= new HashSet(length);

			for (int i= 0; i < length; i++) {
			    IConfigurationElement configElement= getConfigurationElement(elements[i]);
				Bundle bundle= Platform.getBundle(configElement.getContributor().getName());
				fDescriptorMapping.put(elements[i], bundle.getSymbolicName());
				fBundleSet.add(bundle);
			}

			Iterator iter= fBundleSet.iterator();
			while (iter.hasNext()) {
				Bundle bundle= (Bundle)iter.next();
				List toTest= new ArrayList(fBundleSet);
				toTest.remove(bundle);
				Set prereqUIds= new HashSet(Math.max(0, toTest.size() - 1));
				fPrereqsMapping.put(bundle.getSymbolicName(), prereqUIds);

				String requires = (String)bundle.getHeaders().get(Constants.REQUIRE_BUNDLE);
				ManifestElement[] manifestElements;
                try {
                    manifestElements = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, requires);
                } catch (BundleException e) {
                	String uid= getExtensionPointUniqueIdentifier(bundle);
                	String message= "ConfigurationElementSorter for '" + uid + "': getting required plug-ins for '" + bundle.getSymbolicName() + "' failed"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    				Status status= new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, e);
    				TextEditorPlugin.getDefault().getLog().log(status);
                    continue;
                }

                if (manifestElements == null)
                	continue;

				int i= 0;
				while (i < manifestElements.length && !toTest.isEmpty()) {
					String prereqUId= manifestElements[i].getValue();
					for (int j= 0; j < toTest.size();) {
						Bundle toTest_j= (Bundle)toTest.get(j);
						if (toTest_j.getSymbolicName().equals(prereqUId)) {
							toTest.remove(toTest_j);
							prereqUIds.add(toTest_j.getSymbolicName());
						} else
							j++;
					}
					i++;
				}
			}
		}

		/**
		 * Returns the unique extension point identifier for the
		 * configuration element which belongs to the given bundle.
		 *
		 * @param bundle the bundle
		 * @return the unique extension point identifier or "unknown" if not found
		 * @since 3.0.1
		 */
		private String getExtensionPointUniqueIdentifier(Bundle bundle) {
			if (bundle != null) {
				String bundleName= bundle.getSymbolicName();
				if (bundleName != null) {
					Set entries= fDescriptorMapping.entrySet();
					Iterator iter= entries.iterator();
					while (iter.hasNext()) {
						Map.Entry entry= (Map.Entry)iter.next();
						if (bundleName.equals(entry.getValue())) {
						    IExtension extension = getConfigurationElement(entry.getKey()).getDeclaringExtension();
							return extension.getExtensionPointUniqueIdentifier();
						}
					}
				}
			}
			return "unknown";  //$NON-NLS-1$
		}

	}
}
