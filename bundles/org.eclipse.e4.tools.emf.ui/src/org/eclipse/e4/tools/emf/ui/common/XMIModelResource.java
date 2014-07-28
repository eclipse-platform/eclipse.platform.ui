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
package org.eclipse.e4.tools.emf.ui.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;

public class XMIModelResource implements IModelResource {
	private EditingDomain editingDomain;
	private Resource resource;
	private List<ModelListener> listeners = new ArrayList<IModelResource.ModelListener>();

	private IObservableList list;

	public XMIModelResource(URI uri) {
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		ResourceSet resourceSet = new ResourceSetImpl();
		BasicCommandStack commandStack = new BasicCommandStack();
		commandStack.addCommandStackListener(new CommandStackListener() {

			@Override
			public void commandStackChanged(EventObject event) {
				fireDirtyChanged();
				fireCommandStackChanged();
			}
		});
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, resourceSet);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());
		resource = resourceSet.getResource(uri, true);
	}

	@Override
	public IObservableList getRoot() {
		if (list != null) {
			return list;
		}

		list = EMFEditProperties.resource(getEditingDomain()).observe(resource);

		return list;
	}

	@Override
	public void replaceRoot(EObject eObject) {
		E4XMIResource resource = (E4XMIResource) eObject.eResource();
		Map<EObject, String> idMap = new HashMap<EObject, String>();
		idMap.put(eObject, resource.getID(eObject));

		TreeIterator<EObject> it = EcoreUtil.getAllContents(eObject, true);
		while (it.hasNext()) {
			EObject o = it.next();
			resource = (E4XMIResource) o.eResource();
			idMap.put(o, resource.getID(o));
		}

		resource = (E4XMIResource) ((EObject) list.get(0)).eResource();

		Command cmdRemove = new RemoveCommand(getEditingDomain(), resource.getContents(), list.get(0));
		Command cmdAdd = new AddCommand(getEditingDomain(), resource.getContents(), eObject);
		CompoundCommand cmd = new CompoundCommand(Arrays.asList(cmdRemove, cmdAdd));
		getEditingDomain().getCommandStack().execute(cmd);

		for (Entry<EObject, String> e : idMap.entrySet()) {
			resource.setID(e.getKey(), e.getValue());
		}
	}

	@Override
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	@Override
	public boolean isSaveable() {
		return true;
	}

	@Override
	public void addModelListener(ModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeModelListener(ModelListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean isDirty() {
		return ((BasicCommandStack) getEditingDomain().getCommandStack()).isSaveNeeded();
	}

	private void fireDirtyChanged() {
		for (ModelListener listener : listeners) {
			listener.dirtyChanged();
		}
	}

	private void fireCommandStackChanged() {
		for (ModelListener listener : listeners) {
			listener.commandStackChanged();
		}
	}

	@Override
	public IStatus save() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			resource.save(map);

			BasicCommandStack commandStack = (BasicCommandStack) getEditingDomain().getCommandStack();
			commandStack.saveIsDone();

			fireDirtyChanged();
			fireCommandStackChanged();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}
}
