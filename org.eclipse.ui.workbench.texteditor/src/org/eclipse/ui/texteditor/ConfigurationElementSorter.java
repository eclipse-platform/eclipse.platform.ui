/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginPrerequisite;

import org.eclipse.jface.text.Assert;

/**
 * Allows to sort an array based on their elements' configuration elements
 * according to the prerequisite relation of their defining plug-ins.
 * <p>
 * This class can directly be used or subclassed.
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
		private Set fDescriptorSet;
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

			IPluginDescriptor pluginDesc0= (IPluginDescriptor)fDescriptorMapping.get(element0);
			IPluginDescriptor pluginDesc1= (IPluginDescriptor)fDescriptorMapping.get(element1);
			
			// performance tuning - code below would give same result
			if (pluginDesc0.getUniqueIdentifier().equals(pluginDesc1.getUniqueIdentifier()))
				return false;
			
			Set prereqUIds0= (Set)fPrereqsMapping.get(pluginDesc0);
			
			return prereqUIds0.contains(pluginDesc1.getUniqueIdentifier());
		}
		
		/**
		 * Initialize this comarator.
		 * 
		 * @param elements an array of Java editor hover descriptors
		 */
		private void initialize(Object[] elements) {
			int length= elements.length;
			fDescriptorMapping= new HashMap(length);
			fPrereqsMapping= new HashMap(length);
			fDescriptorSet= new HashSet(length);
			
			for (int i= 0; i < length; i++) {
				IPluginDescriptor descriptor= getConfigurationElement(elements[i]).getDeclaringExtension().getDeclaringPluginDescriptor();
				fDescriptorMapping.put(elements[i], descriptor);
				fDescriptorSet.add(descriptor);
			}
			
			Iterator iter= fDescriptorSet.iterator();
			while (iter.hasNext()) {
				IPluginDescriptor descriptor= (IPluginDescriptor)iter.next();
				List toTest= new ArrayList(fDescriptorSet);
				toTest.remove(descriptor);
				Set prereqUIds= new HashSet(Math.max(0, toTest.size() - 1));
				fPrereqsMapping.put(descriptor, prereqUIds);
				
				IPluginPrerequisite[] prereqs= descriptor.getPluginPrerequisites();
				int i= 0;
				while (i < prereqs.length && !toTest.isEmpty()) {
					String prereqUId= prereqs[i].getUniqueIdentifier();
					for (int j= 0; j < toTest.size();) {
						IPluginDescriptor toTest_j= (IPluginDescriptor)toTest.get(j);
						if (toTest_j.getUniqueIdentifier().equals(prereqUId)) {
							toTest.remove(toTest_j);
							prereqUIds.add(toTest_j.getUniqueIdentifier());
						} else
							j++;
					}
					i++;
				}
			}
		}

	}
}
