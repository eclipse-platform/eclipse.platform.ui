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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractComponentEditor {
	private EditingDomain editingDomain;

	private static Map<Integer, Image> IMAGE_MAP = new HashMap<Integer, Image>();
	private static final String[] IMAGES = {
		"/icons/full/obj16/zoom.png", //$NON-NLS-1$
		"/icons/full/obj16/table_add.png", //$NON-NLS-1$
		"/icons/full/obj16/table_delete.png", //$NON-NLS-1$
		"/icons/full/obj16/arrow_up.png", //$NON-NLS-1$
		"/icons/full/obj16/arrow_down.png" //$NON-NLS-1$
	};

	private WritableValue master = new WritableValue();

	public static final int SEARCH_IMAGE = 0;
	public static final int TABLE_ADD_IMAGE = 1;
	public static final int TABLE_DELETE_IMAGE = 2;
	public static final int ARROW_UP = 3;
	public static final int ARROW_DOWN = 4;

	public AbstractComponentEditor(EditingDomain editingDomain) {
		this.editingDomain = editingDomain;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public WritableValue getMaster() {
		return master;
	}

	public Image getImage( Display d, int id) {
		Image img = IMAGE_MAP.get(id);
		if( img == null ) {
			try {
				InputStream in = AbstractComponentEditor.class.getClassLoader().getResourceAsStream(IMAGES[id]);
				img = new Image(d, in);
				IMAGE_MAP.put(id, img);
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return img;
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
	
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {};
	}
	
	public List<Action> getActions(Object element) {
		return Collections.emptyList();
	}
}
