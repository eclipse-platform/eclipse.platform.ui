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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractComponentEditor {
	private EditingDomain editingDomain;

	public AbstractComponentEditor(EditingDomain editingDomain) {
		this.editingDomain = editingDomain;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public abstract Image getImage(Object element,Display display);
	public abstract String getLabel(Object element);
	public abstract String getDetailLabel(Object element);

	public abstract String getDescription(Object element);
	public abstract Composite getEditor(Composite parent, Object object);
	public abstract IObservableList getChildList(Object element);

	protected Image loadSharedImage(Display d, URL path) {
		try {
			URL url = FileLocator.resolve(path);
			if( url != null ) {
				InputStream in = url.openStream();
				Image image = new Image(d, in);
				in.close();
				return image;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
