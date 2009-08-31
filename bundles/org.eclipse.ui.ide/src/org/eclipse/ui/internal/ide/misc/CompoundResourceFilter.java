/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.misc;

import org.eclipse.core.filesystem.IFileInfoFilter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;

import org.eclipse.core.resources.FilterTypeManager;
import org.eclipse.core.resources.IResourceFilter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * Resource Filter Type allowing serializing sub filters as the arguments
 * @since 3.4
 *
 */
public class CompoundResourceFilter {

	private static final String T_FILTERS = "filters"; //$NON-NLS-1$

	protected IFileInfoFilter instantiate(IProject project, IResourceFilter filter) {
		FilterTypeManager.Descriptor desc = FilterTypeManager.getDefault().findDescriptor(filter.getId());
		if (desc != null)
			return desc.getFactory().instantiate(project, filter.getArguments());
		return null;
	}

	private static final String T_FILTER = "filter"; //$NON-NLS-1$
	private static final String T_TYPE = "type"; //$NON-NLS-1$
	private static final String T_ID = "id"; //$NON-NLS-1$
	private static final String T_ARGUMENTS = "arguments"; //$NON-NLS-1$

	/** Unserialize the filters stored in a memento 
	 * @param project
	 * @param memento
	 * @return the filter array
	 */
	public static IResourceFilter[] unserialize(final IProject project, String memento) {
		LinkedList filters = new LinkedList();
		XMLMemento mementoObject = null;
		try {
			mementoObject = XMLMemento.createReadRoot(new StringReader(memento));
		} catch (WorkbenchException e) {
			return new IResourceFilter[0];
		}
		IMemento childMem = mementoObject.getChild(T_FILTERS);
		if (childMem != null) {
			IMemento[] elementMem = childMem.getChildren(T_FILTER);
			for (int i = 0; i < elementMem.length; i++) {
				final String id = elementMem[i].getString(T_ID);
				final int type = elementMem[i].getInteger(T_TYPE).intValue();
				IMemento argumentMem = elementMem[i].getChild(T_ARGUMENTS);
				final String arguments = argumentMem != null? argumentMem.getTextData(): ""; //$NON-NLS-1$
				filters.add(new IResourceFilter() {
					public String getArguments() { return arguments; }
					public String getId() { return id; }
					public IPath getPath() { return null; }
					public IProject getProject() { return project; }
					public int getType() { return type; }
				});
			}
		}
		return (IResourceFilter[]) filters.toArray(new IResourceFilter[0]);
	}

	/** Serialize filters into in a memento 
	 * @param filters
	 * @return the memento
	 */
	public static String serialize(IResourceFilter[] filters) {
		XMLMemento memento = XMLMemento.createWriteRoot("memento"); //$NON-NLS-1$
	
		IMemento expandedMem = memento.createChild(T_FILTERS);
		for (int i = 0; i < filters.length; i++) {
			IMemento elementMem = expandedMem.createChild(T_FILTER);
			elementMem.putString(T_ID, filters[i].getId());
			elementMem.putInteger(T_TYPE, filters[i].getType());
			IMemento argumentMem = elementMem.createChild(T_ARGUMENTS);
			argumentMem.putTextData(filters[i].getArguments());
		}
	
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			return ""; //$NON-NLS-1$
		}
		return writer.toString();
	}
	
	protected abstract class FileInfoFilter implements IFileInfoFilter {
		protected IFileInfoFilter[] filterTypes;
		protected IResourceFilter[] filters;
		public FileInfoFilter(IProject project, IResourceFilter[] filters) {
			this.filters = filters;
			filterTypes = new IFileInfoFilter[filters.length];
			for (int i = 0; i < filters.length; i++)
				filterTypes[i] = instantiate(project, filters[i]);
		}
	}
}