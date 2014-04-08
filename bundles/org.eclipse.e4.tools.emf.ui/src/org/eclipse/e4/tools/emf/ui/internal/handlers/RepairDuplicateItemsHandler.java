/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432372
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.EmfUtil;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.EmptyFilterOption;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;

public class RepairDuplicateItemsHandler extends MarkDuplicateItemsBase {

	static public String name = "org.eclipse.e4.tools.active-object-viewer"; //$NON-NLS-1$
	static String attName = "elementId"; //$NON-NLS-1$

	@Override
	@Execute
	public void execute(IEclipseContext context) {
		IViewEObjects viewer = (IViewEObjects) context.get(name);
		Collection<EObject> all = viewer.getAllEObjects();
		Map<String, List<EObject>> map = MarkDuplicateElementIdsHandler.getDuplicateMap(attName, all);
		Iterator<Entry<String, List<EObject>>> it = map.entrySet().iterator();
		CompoundCommand compoundCommand = new CompoundCommand();
		EditingDomain editingDomain = viewer.getEditingDomain();

		while (it.hasNext()) {
			Entry<String, List<EObject>> entry = it.next();
			List<EObject> listDups = entry.getValue();
			if (listDups.size() > 1) {
				ArrayList<EObject> toFix = new ArrayList<EObject>(listDups.subList(1, listDups.size()));
				Iterator<EObject> itToFix = toFix.iterator();
				while (itToFix.hasNext()) {
					EObject eObject = itToFix.next();
					EAttribute att = EmfUtil.getAttribute(eObject, attName);
					String value = (String) eObject.eGet(att);
					// do not repair empty or null values
					if (E.isEmpty(value)) {
						break;
					}
					int index = -1;
					while (map.containsKey(value)) {
						index++;
						int lastDot = value.lastIndexOf('.');
						if (lastDot == -1) {
							lastDot = value.length();
						}
						try {
							String suffix = value.substring(lastDot + 1);
							Integer.parseInt(suffix);
							// ends with integer
							value = value.substring(0, lastDot);
						} catch (Exception e) {
						}
						value = value + "." + index; //$NON-NLS-1$
					}
					Command cmd = SetCommand.create(editingDomain, eObject, att, value);
					compoundCommand.append(cmd);
					// Note: If the compound command cannot execute, we
					// must revert these changes
					listDups.remove(eObject);
					List<EObject> newList = new ArrayList<EObject>();
					newList.add(eObject);
					map.put(value, newList);
					// Note: end

				}
			}
		}

		if (compoundCommand.isEmpty() == false) {
			if (compoundCommand.canExecute()) {
				editingDomain.getCommandStack().execute(compoundCommand);
			} else {
				// this will rollback our local map changes
				// Object obj =
				// ContextInjectionFactory.make(MarkDuplicateItemsHandler.class,
				// context);
				// ContextInjectionFactory.invoke(obj, Execute.class, context);
			}
			Collection<EObject> duplicateList = MarkDuplicateElementIdsHandler.getDuplicateList(attName, all);
			applyEmptyOption(duplicateList, attName, EmptyFilterOption.EXCLUDE);
			viewer.highlightEObjects(duplicateList);
		}
	}

	@CanExecute
	public boolean canExecute(IEclipseContext context) {
		IViewEObjects viewer = (IViewEObjects) context.get(name);
		return (viewer != null);
	}

}