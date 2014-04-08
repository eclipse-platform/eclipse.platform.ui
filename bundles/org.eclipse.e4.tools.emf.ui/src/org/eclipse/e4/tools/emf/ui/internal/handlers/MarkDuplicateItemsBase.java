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
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.EmfUtil;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.EmptyFilterOption;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

public class MarkDuplicateItemsBase extends AbstractHandler {

	private String attName;

	@Execute
	public void execute(IEclipseContext context) {
		IViewEObjects viewer = (IViewEObjects) context.get(VIEWER_KEY);
		Collection<EObject> duplicates = getDuplicateList(getAttName(), viewer.getAllEObjects());
		applyEmptyOption(duplicates, getAttName(), EmptyFilterOption.EXCLUDE);
		viewer.highlightEObjects(duplicates);
	}

	static Collection<EObject> getDuplicateList(String attName, Collection<EObject> all) {
		Map<String, List<EObject>> map = getDuplicateMap(attName, all);
		List<EObject> duplicates = new ArrayList<EObject>();
		for (String key : map.keySet()) {
			List<EObject> list = map.get(key);
			if (list.size() > 1) {
				duplicates.addAll(list);
			}
		}
		return duplicates;
	}

	static Map<String, List<EObject>> getDuplicateMap(String attName, Collection<EObject> selected) {
		ConcurrentHashMap<String, List<EObject>> map = new ConcurrentHashMap<String, List<EObject>>();
		for (Iterator<?> it = selected.iterator(); it.hasNext();) {
			Object object = it.next();
			if (object instanceof EObject) {
				EObject item = (EObject) object;
				EAttribute att = EmfUtil.getAttribute(item, attName);
				if (att != null) {
					String attValue;
					Object obj = item.eGet(att);
					if (obj == null) {
						attValue = ""; //$NON-NLS-1$
					} else {
						attValue = String.valueOf(obj);
					}
					List<EObject> list = map.get(attValue);
					if (list == null) {
						list = new ArrayList<EObject>();
						map.put(attValue, list);
					}
					list.add(item);
				}
			}
		}
		return map;
	}

	public String getAttName() {
		return attName;
	}

	public void setAttributeName(String attName) {
		this.attName = attName;
	}

	static protected void applyEmptyOption(Collection<EObject> marked, String attName, EmptyFilterOption emptyFilterOption) {
		switch (emptyFilterOption) {
		case EXCLUDE:
			for (Iterator<?> it = marked.iterator(); it.hasNext();) {
				EObject eObject = (EObject) it.next();
				if (E.isEmpty(EmfUtil.getAttributeValue(eObject, attName))) {
					it.remove();
				}
			}
			break;
		case ONLY:
			for (Iterator<?> it = marked.iterator(); it.hasNext();) {
				EObject eObject = (EObject) it.next();
				if (E.notEmpty(EmfUtil.getAttributeValue(eObject, attName))) {
					it.remove();
				}
			}
			break;
		case INCLUDE:
		default:
			break;
		}
	}
}
