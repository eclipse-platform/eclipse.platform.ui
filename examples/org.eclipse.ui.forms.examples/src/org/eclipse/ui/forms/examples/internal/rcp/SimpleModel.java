/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.ui.forms.examples.internal.rcp;
import java.util.ArrayList;
/**
 * @author dejan
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SimpleModel {
	private final ArrayList<IModelListener> modelListeners;
	private ArrayList<NamedObject> objects;
	public SimpleModel() {
		modelListeners = new ArrayList<>();
		initialize();
	}
	public void addModelListener(IModelListener listener) {
		if (!modelListeners.contains(listener))
			modelListeners.add(listener);
	}
	public void removeModelListener(IModelListener listener) {
		modelListeners.remove(listener);
	}
	public void fireModelChanged(Object[] objects, String type, String property) {
		for (IModelListener modelListener : modelListeners) {
			modelListener.modelChanged(objects,
					type, property);
		}
	}
	public Object[] getContents() {
		return objects.toArray();
	}
	private void initialize() {
		objects = new ArrayList<>();
		NamedObject[] objects = {
				new TypeOne("TypeOne instance 1", 2, true, "Some text"),
				new TypeOne("TypeOne instance 2", 1, false, "Different text"),
				new TypeOne("TypeOne instance 3", 3, true, "Text"),
				new TypeOne("TypeOne instance 4", 0, false, "Longer text"),
				new TypeOne("TypeOne instance 5", 1, true, "Sample"),
				new TypeTwo("TypeTwo instance 1", false, true),
				new TypeTwo("TypeTwo instance 2", true, false)};
		add(objects, false);
	}
	public void add(NamedObject[] objs, boolean notify) {
		for (NamedObject obj : objs) {
			objects.add(obj);
			obj.setModel(this);
		}
		if (notify)
			fireModelChanged(objs, IModelListener.ADDED, "");
	}
	public void remove(NamedObject[] objs, boolean notify) {
		for (NamedObject obj : objs) {
			objects.remove(obj);
			obj.setModel(null);
		}
		if (notify)
			fireModelChanged(objs, IModelListener.REMOVED, "");
	}
}
