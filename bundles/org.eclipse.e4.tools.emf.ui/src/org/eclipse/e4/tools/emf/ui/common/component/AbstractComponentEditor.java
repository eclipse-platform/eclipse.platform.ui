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
import java.util.List;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractComponentEditor {
	private EditingDomain editingDomain;

	private static ImageRegistry IMG_REG = new ImageRegistry();
	// private static Map<Integer, Image> IMAGE_MAP = new HashMap<Integer,
	// Image>();

	private static final String[] IMAGES = { "/icons/full/obj16/zoom.png", //$NON-NLS-1$
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

	protected static final int VERTICAL_LIST_WIDGET_INDENT = 10;

	private ModelEditor editor;

	public AbstractComponentEditor(EditingDomain editingDomain, ModelEditor editor) {
		this.editingDomain = editingDomain;
		this.editor = editor;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public ModelEditor getEditor() {
		return editor;
	}

	public WritableValue getMaster() {
		return master;
	}

	protected void setElementId(Object element) {
		if (getEditor().isAutoCreateElementId() && element instanceof MApplicationElement) {
			MApplicationElement el = (MApplicationElement) element;
			if (el.getElementId() == null || el.getElementId().trim().length() == 0) {
				el.setElementId(Util.getDefaultElementId(((EObject) getMaster().getValue()).eResource(), el, getEditor().getProject()));
			}
		}
	}

	public Image getImage(Display d, int id) {
		Image img = IMG_REG.get(IMAGES[id]);
		if (img == null) {
			try {
				InputStream in = AbstractComponentEditor.class.getClassLoader().getResourceAsStream(IMAGES[id]);
				img = new Image(d, in);
				IMG_REG.put(IMAGES[id], img);
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return img;
	}

	public abstract Image getImage(Object element, Display display);

	public abstract String getLabel(Object element);

	public abstract String getDetailLabel(Object element);

	public abstract String getDescription(Object element);

	public abstract Composite getEditor(Composite parent, Object object);

	public abstract IObservableList getChildList(Object element);

	protected Image loadSharedImage(Display d, URL path) {
		Image img = IMG_REG.get(path.toString());
		if (img == null) {
			try {
				URL url = FileLocator.resolve(path);
				if (url != null) {
					InputStream in = url.openStream();
					img = new Image(d, in);
					IMG_REG.put(path.toString(), img);
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return img;
	}

	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {};
	}

	public List<Action> getActions(Object element) {
		return Collections.emptyList();
	}
}
