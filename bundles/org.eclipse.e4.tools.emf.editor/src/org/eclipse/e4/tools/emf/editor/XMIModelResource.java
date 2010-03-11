/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.workbench.ui.internal.E4XMIResourceFactory;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;

public class XMIModelResource implements IModelResource {
	private EditingDomain editingDomain;
	private Resource resource;
	private List<ModelListener> listeners = new ArrayList<IModelResource.ModelListener>();
	private boolean dirty;

	public XMIModelResource(String uri) {
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		ResourceSet resourceSet = new ResourceSetImpl();
		BasicCommandStack commandStack = new BasicCommandStack();
		commandStack.addCommandStackListener(new CommandStackListener() {
			
			public void commandStackChanged(EventObject event) {
				dirty = true;
				fireDirtyChanged();
			}
		});
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
				commandStack, resourceSet);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new E4XMIResourceFactory());
		resource = resourceSet.getResource(URI.createURI(uri), true);
	}

	public IObservableList getRoot() {
		WritableList list = new WritableList();
		list.add(resource.getContents().get(0));
		return list;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public boolean isSaveable() {
		return true;
	}

	public void addModelListener(ModelListener listener) {
		listeners.add(listener);
	}
	
	public void removeModelListener(ModelListener listener) {
		listeners.remove(listener);
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	private void fireDirtyChanged() {
		for( ModelListener listener : listeners ) {
			listener.dirtyChanged();
		}
	}
	
	public IStatus save() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			resource.save(map);
			editingDomain.getCommandStack().flush();
			dirty = false;
			fireDirtyChanged();
		} catch (Exception e) {
			// TODO: handle exception
		}

		return Status.OK_STATUS;
	}

}
