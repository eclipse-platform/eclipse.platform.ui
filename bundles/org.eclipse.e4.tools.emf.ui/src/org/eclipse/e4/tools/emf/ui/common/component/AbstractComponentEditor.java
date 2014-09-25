/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 422465
 *     Steven Spungin <steven@spungin.tv> - Bug 437951, Bug 439709
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common.component;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.tools.emf.ui.common.AbstractElementEditorContribution;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ProjectOSGiTranslationProvider;
import org.eclipse.e4.tools.services.IClipboardService.Handler;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.impl.ResourceBundleTranslationProvider;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractComponentEditor {
	private static final String CSS_CLASS_KEY = "org.eclipse.e4.ui.css.CssClassName"; //$NON-NLS-1$

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
	@Optional
	protected IProject project;

	@Inject
	@Translation
	protected Messages Messages;

	@Inject
	@Optional
	private ProjectOSGiTranslationProvider translationProvider;

	private Composite editorControl;

	private IdGenerator generator;

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
		if (key == null) {
			return null;
		}
		return ImageDescriptor.createFromImage(createImage(key));
	}

	public abstract Image getImage(Object element, Display display);

	public abstract String getLabel(Object element);

	public abstract String getDetailLabel(Object element);

	public abstract String getDescription(Object element);

	public Composite getEditor(Composite parent, Object object) {
		if (generator != null) {
			generator.stopGenerating();
			generator = null;
		}
		editorControl = doGetEditor(parent, object);
		return editorControl;
	}

	protected abstract Composite doGetEditor(Composite parent, Object object);

	public abstract IObservableList getChildList(Object element);

	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {};
	}

	public List<Action> getActions(Object element) {
		return Collections.emptyList();
	}

	/**
	 * Translates an input <code>String</code> using the current
	 * {@link ResourceBundleTranslationProvider} and <code>locale</code> from
	 * the {@link TranslationService}.
	 *
	 * @param string
	 *            the string to translate, may not be null.
	 * @return the translated string or the input string if it could not be
	 *         translated.
	 */
	public String translate(String string) {
		return ControlFactory.tr(translationProvider, string);
	}

	/**
	 * @param element
	 * @return the list of actions that are populated in the import menu. Can be
	 *         empty but is never null.
	 */
	public List<Action> getActionsImport(Object element) {
		return Collections.emptyList();
	}

	protected String getLocalizedLabel(MUILabel element) {
		return ControlFactory.getLocalizedLabel(translationProvider, element);
	}

	private boolean isFocusChild(Control control) {
		Control c = control;
		while (c != null && c != editorControl) {
			c = c.getParent();
		}
		return c != null;
	}

	public void handleCopy() {
		if (editorControl != null) {
			Control focusControl = editorControl.getDisplay().getFocusControl();

			if (isFocusChild(focusControl) && focusControl.getData(ControlFactory.COPY_HANDLER) != null) {
				((Handler) focusControl.getData(ControlFactory.COPY_HANDLER)).copy();
			}
		}
	}

	public void handlePaste() {
		if (editorControl != null) {
			Control focusControl = editorControl.getDisplay().getFocusControl();

			if (isFocusChild(focusControl) && focusControl.getData(ControlFactory.COPY_HANDLER) != null) {
				((Handler) focusControl.getData(ControlFactory.COPY_HANDLER)).paste();
			}
		}
	}

	public void handleCut() {
		if (editorControl != null) {
			Control focusControl = editorControl.getDisplay().getFocusControl();

			if (isFocusChild(focusControl) && focusControl.getData(ControlFactory.COPY_HANDLER) != null) {
				((Handler) focusControl.getData(ControlFactory.COPY_HANDLER)).cut();
			}
		}
	}

	protected Composite createScrollableContainer(Composite parent) {
		final ScrolledComposite scrolling = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolling.setBackgroundMode(SWT.INHERIT_DEFAULT);
		scrolling.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$

		final Composite contentContainer = new Composite(scrolling, SWT.NONE);

		contentContainer.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$
		scrolling.setExpandHorizontal(true);
		scrolling.setExpandVertical(true);
		scrolling.setContent(contentContainer);

		scrolling.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolling.getClientArea();
				scrolling.setMinSize(contentContainer.computeSize(r.width, SWT.DEFAULT));
			}
		});

		scrolling.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 10;
		contentContainer.setLayout(gl);

		return contentContainer;
	}

	protected void createContributedEditorTabs(CTabFolder folder, EMFDataBindingContext context, WritableValue master, Class<?> clazz) {
		List<AbstractElementEditorContribution> contributionList = editor.getTabContributionsForClass(clazz);

		for (AbstractElementEditorContribution eec : contributionList) {
			CTabItem item = new CTabItem(folder, SWT.BORDER);
			item.setText(eec.getTabLabel());

			Composite parent = createScrollableContainer(folder);
			item.setControl(parent.getParent());

			eec.createContributedEditorTab(parent, context, master, getEditingDomain(), project);
		}

	}

	/**
	 * Generates an ID when the another field changes. Must be called after
	 * master is set with the objects value.
	 *
	 * @param attSource
	 *            The source attribute
	 * @param attId
	 *            The id attribute to generate
	 * @param control
	 *            optional control to disable generator after losing focus or
	 *            disposing
	 */
	protected void enableIdGenerator(EAttribute attSource, EAttribute attId, Control control) {
		if (generator != null) {
			generator.stopGenerating();
			generator = null;
		}
		if (getEditor().isAutoCreateElementId()) {
			generator = new IdGenerator();
			generator.bind(getMaster(), EMFEditProperties.value(getEditingDomain(), attSource), EMFEditProperties.value(getEditingDomain(), attId), control);
		}
	}
}
