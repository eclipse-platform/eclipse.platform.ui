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

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ProjectOSGiTranslationProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.Translation;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractComponentEditor {
	private WritableValue master = new WritableValue();

	public static final int SEARCH_IMAGE = 0;
	public static final int TABLE_ADD_IMAGE = 1;
	public static final int TABLE_DELETE_IMAGE = 2;
	public static final int ARROW_UP = 3;
	public static final int ARROW_DOWN = 4;

	protected static final int VERTICAL_LIST_WIDGET_INDENT = 10;

	@Inject
	private EditingDomain editingDomain;
	@Inject
	private ModelEditor editor;
	@Inject
	protected IResourcePool resourcePool;

	@Inject
	@Translation
	protected Messages Messages;

	@Inject
	@Optional
	private ProjectOSGiTranslationProvider translationProvider;

	@Inject
	@Named(TranslationService.LOCALE)
	private String locale;

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

	public Image createImage(String key) {
		return resourcePool.getImageUnchecked(key);
	}

	public ImageDescriptor createImageDescriptor(String key) {
		return ImageDescriptor.createFromImage(createImage(key));
	}

	public abstract Image getImage(Object element, Display display);

	public abstract String getLabel(Object element);

	public abstract String getDetailLabel(Object element);

	public abstract String getDescription(Object element);

	public abstract Composite doGetEditor(Composite parent, Object object);

	public abstract IObservableList getChildList(Object element);

	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {};
	}

	public List<Action> getActions(Object element) {
		return Collections.emptyList();
	}

	protected String getLocalizedLabel(MUILabel element) {
		return ControlFactory.getLocalizedLabel(translationProvider, element, locale);
	}

	public void handleCopy() {

	}

	public void handlePaste() {

	}

	public void handleCut() {

	}
}
