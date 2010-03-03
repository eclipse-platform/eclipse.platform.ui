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
package org.eclipse.e4.tools.emf.ui.common.component;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractComponentEditor {
//	public abstract boolean canHandle(Object object);
	public abstract Image getImage(Display display);
	public abstract String getLabel(Object element);
	public abstract String getDetailLabel(Object element);

	public abstract String getDescription(Object element);
	public abstract Composite getEditor(Composite parent, Object object);
	public abstract IObservableList getChildList(Object element);

}
