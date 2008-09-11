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
package org.eclipse.ui.texteditor.rulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.rulers.DAG;
import org.eclipse.ui.internal.texteditor.rulers.ExtensionPointHelper;
import org.eclipse.ui.internal.texteditor.rulers.RulerColumnMessages;
import org.eclipse.ui.internal.texteditor.rulers.RulerColumnPlacementConstraint;

import org.eclipse.ui.texteditor.ConfigurationElementSorter;


/**
 * A registry for all extensions to the
 * <code>rulerColumns</code> extension point.
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class RulerColumnRegistry {

	private static final String EXTENSION_POINT= "rulerColumns"; //$NON-NLS-1$
	private static final String QUALIFIED_EXTENSION_POINT= TextEditorPlugin.PLUGIN_ID + '.' + EXTENSION_POINT;

	/** The singleton instance. */
	private static RulerColumnRegistry fgSingleton= null;

	/**
	 * Returns the default computer registry.
	 * <p>
	 * TODO keep this or add some other singleton, e.g. TextEditorPlugin?
	 * </p>
	 *
	 * @return the singleton instance
	 */
	public static synchronized RulerColumnRegistry getDefault() {
		if (fgSingleton == null) {
			fgSingleton= new RulerColumnRegistry();
		}

		return fgSingleton;
	}

	/**
	 * All descriptors (element type:
	 * {@link RulerColumnDescriptor}).
	 */
	private List fDescriptors= null;
	/**
	 * All descriptors by id (element type: {@link RulerColumnDescriptor}).
	 */
	private Map fDescriptorMap= null;

	/**
	 * <code>true</code> if this registry has been loaded.
	 */
	private boolean fLoaded= false;

	/**
	 * Creates a new instance.
	 */
	RulerColumnRegistry() {
	}

	/**
	 * Returns the list of {@link RulerColumnDescriptor}s describing all extensions to the
	 * <code>rulerColumns</code> extension point. The list's iterator traverses the descriptors in
	 * the ordering implied by the placement specifications of the contributions.
	 * <p>
	 * The returned list is unmodifiable and guaranteed to never change. Note that the set of
	 * descriptors may change over time due to dynamic plug-in removal or addition.
	 * </p>
	 *
	 * @return the sorted list of extensions to the <code>rulerColumns</code> extension point
	 *         (element type: {@link RulerColumnDescriptor})
	 */
	public List getColumnDescriptors() {
		ensureExtensionPointRead();
		return fDescriptors;
	}

	/**
	 * Returns the {@link RulerColumnDescriptor} with the given identity, <code>null</code> if no
	 * such descriptor exists.
	 *
	 * @param id the identity of the ruler contribution as given in the extension point xml.
	 * @return the {@link RulerColumnDescriptor} with the given identity, <code>null</code> if no
	 *         such descriptor exists
	 */
	public RulerColumnDescriptor getColumnDescriptor(String id) {
		Assert.isLegal(id != null);
		ensureExtensionPointRead();
		return (RulerColumnDescriptor) fDescriptorMap.get(id);
	}

	/**
	 * Ensures that the extensions are read and stored in
	 * <code>fDescriptorsByPartition</code>.
	 */
	private void ensureExtensionPointRead() {
		boolean reload;
		synchronized (this) {
			reload= !fLoaded;
			fLoaded= true;
		}
		if (reload)
			reload();
	}

	/**
	 * Reloads the extensions to the extension point.
	 * <p>
	 * This method can be called more than once in order to reload from
	 * a changed extension registry.
	 * </p>
	 */
	public void reload() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		List elements= new ArrayList(Arrays.asList(registry.getConfigurationElementsFor(TextEditorPlugin.PLUGIN_ID, EXTENSION_POINT)));

		List descriptors= new ArrayList();
		Map descriptorMap= new HashMap();

		for (Iterator iter= elements.iterator(); iter.hasNext();) {
			IConfigurationElement element= (IConfigurationElement) iter.next();
			try {
				RulerColumnDescriptor desc= new RulerColumnDescriptor(element, this);
				String id= desc.getId();
				if (descriptorMap.containsKey(id)) {
					noteDuplicateId(desc);
					continue;
				}

				descriptors.add(desc);
				descriptorMap.put(id, desc);
			} catch (InvalidRegistryObjectException x) {
				/*
				 * Element is not valid any longer as the contributing plug-in was unloaded or for
				 * some other reason. Do not include the extension in the list and inform the user
				 * about it.
				 */
				noteInvalidExtension(element, x);
			} catch (CoreException x) {
				warnUser(x.getStatus());
			}
		}

		sort(descriptors);

		synchronized (this) {
			fDescriptors= Collections.unmodifiableList(descriptors);
			fDescriptorMap= Collections.unmodifiableMap(descriptorMap);
		}
	}

	/**
	 * Sorts the column contributions.
	 *
	 * @param descriptors the descriptors to sort
	 */
	private void sort(List descriptors) {
		/*
		 * Topological sort of the DAG defined by the plug-in dependencies
		 * 1. TopoSort descriptors by plug-in dependency
		 * 2. Insert into Directed Acyclic Graph
		 * 3. TopoSort DAG: pick the source with the lowest gravity and remove from DAG
		 */
		ConfigurationElementSorter sorter= new ConfigurationElementSorter() {
			public IConfigurationElement getConfigurationElement(Object object) {
				return ((RulerColumnDescriptor) object).getConfigurationElement();
			}
		};
		Object[] array= descriptors.toArray();
		sorter.sort(array);

		Map descriptorsById= new HashMap();
		for (int i= 0; i < array.length; i++) {
			RulerColumnDescriptor desc= (RulerColumnDescriptor) array[i];
			descriptorsById.put(desc.getId(), desc);
		}

		DAG dag= new DAG();
		for (int i= 0; i < array.length; i++) {
			RulerColumnDescriptor desc= (RulerColumnDescriptor) array[i];
			dag.addVertex(desc);

			Set before= desc.getPlacement().getConstraints();
			for (Iterator it= before.iterator(); it.hasNext();) {
				RulerColumnPlacementConstraint constraint= (RulerColumnPlacementConstraint) it.next();
				String id= constraint.getId();
				RulerColumnDescriptor target= (RulerColumnDescriptor) descriptorsById.get(id);
				if (target == null) {
					noteUnknownTarget(desc, id);
				} else {
					boolean success;
					if (constraint.isBefore())
						success= dag.addEdge(desc, target);
					else
						success= dag.addEdge(target, desc);
					if (!success)
						noteCycle(desc, target);
				}
			}
		}

		Comparator gravityComp= new Comparator() {
			public int compare(Object o1, Object o2) {
				float diff= ((RulerColumnDescriptor) o1).getPlacement().getGravity() - ((RulerColumnDescriptor) o2).getPlacement().getGravity();
				if (diff == 0)
					return 0;
				if (diff < 0)
					return -1;
				return 1;
			}
		};

		/* Topological sort - always select the source with the least gravity */
		Set toProcess= dag.getSources();
		int index= 0;
		while (!toProcess.isEmpty()) {
			Object next= Collections.min(toProcess, gravityComp);
			array[index]= next;
			index++;
			dag.removeVertex(next);
			toProcess= dag.getSources();
		}
		Assert.isTrue(index == array.length);

		ListIterator it= descriptors.listIterator();
		for (int i= 0; i < index; i++) {
			it.next();
			it.set(array[i]);
		}
	}

	private void noteInvalidExtension(IConfigurationElement element, InvalidRegistryObjectException x) {
		String message= MessageFormat.format(RulerColumnMessages.RulerColumnRegistry_invalid_msg, new Object[] {ExtensionPointHelper.findId(element)});
		warnUser(message, x);
	}

	private void noteUnknownTarget(RulerColumnDescriptor desc, String referencedId) {
		String message= MessageFormat.format(RulerColumnMessages.RulerColumnRegistry_unresolved_placement_msg, new Object[] {QUALIFIED_EXTENSION_POINT, referencedId, desc.getName(), desc.getContributor()});
		warnUser(message, null);
	}

	private void noteCycle(RulerColumnDescriptor desc, RulerColumnDescriptor target) {
		String message= MessageFormat.format(RulerColumnMessages.RulerColumnRegistry_cyclic_placement_msg, new Object[] {QUALIFIED_EXTENSION_POINT, target.getName(), desc.getName(), desc.getContributor()});
		warnUser(message, null);
	}

	private void noteDuplicateId(RulerColumnDescriptor desc) {
		String message= MessageFormat.format(RulerColumnMessages.RulerColumnRegistry_duplicate_id_msg, new Object[] {QUALIFIED_EXTENSION_POINT, desc.getId(), desc.getContributor()});
		warnUser(message, null);
	}

	private void warnUser(String message, Exception exception) {
		IStatus status= new Status(IStatus.WARNING, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, exception);
		warnUser(status);
	}

	private void warnUser(IStatus status) {
		TextEditorPlugin.getDefault().getLog().log(status);
	}
}
