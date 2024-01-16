/*******************************************************************************
 * Copyright (c) 2013 Remain BV, Industrial-TSI BV and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wim Jongmam <wim.jongman@remainsoftware.com> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Ongoing Maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.imp;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

public class ModelImportWizard extends Wizard {

	private final Class<? extends MApplicationElement> applicationElement;

	private ModelImportPage1 page1;

	private final MApplication application;

	private final AbstractComponentEditor<?> editor;

	private final String hint;

	public ModelImportWizard(Class<? extends MApplicationElement> applicationElement, AbstractComponentEditor<?> editor,
			IResourcePool resourcePool) {
		this(applicationElement, editor, "", resourcePool); //$NON-NLS-1$
	}

	public ModelImportWizard(Class<? extends MApplicationElement> applicationElement, AbstractComponentEditor<?> editor,
			String hint, IResourcePool resourcePool) {
		this.applicationElement = applicationElement;
		this.editor = editor;
		this.hint = hint;
		Object modelSelection = editor.getEditor().getModelProvider().getRoot().get(0);
		if (modelSelection instanceof MApplication) {
			application = (MApplication) modelSelection;
		} else {
			application = null;
		}
		setWindowTitle(Messages.ModelImportWizard_Model
			+ " " + applicationElement.getSimpleName() + " " + Messages.ModelImportWizard_ImportWizard); //$NON-NLS-1$ //$NON-NLS-2$
		setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(resourcePool
			.getImageUnchecked(ResourceProvider.IMG_Wizban16_imp3x_wiz)));
		Assert.isNotNull(RegistryUtil.getStruct(applicationElement, getHint()),
			Messages.ModelImportWizard_UnknownElement + ": " + applicationElement.getClass().getName()); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page1 = new ModelImportPage1();
		page1.setWizard(this);
		addPage(page1);
	}

	@Override
	public boolean performFinish() {

		return true;
	}

	/**
	 * @return the {@link MApplicationElement} passed in the constructor.
	 */
	public Class<? extends MApplicationElement> getApplicationElement() {
		return applicationElement;
	}

	/**
	 * @return the extension point name associated with the {@link MApplicationElement} that is passed in the
	 *         constructor of
	 *         this wizard.
	 * @see #getApplicationElement()
	 */
	protected String getExtensionPointName() {
		return RegistryUtil.getStruct(applicationElement, getHint()).getExtensionPointName();
	}

	/**
	 * @return the extension point id associated with the {@link MApplicationElement} that is passed in the constructor
	 *         of
	 *         this wizard.
	 * @see #getApplicationElement()
	 */
	protected String getExtensionPoint() {
		return RegistryUtil.getStruct(applicationElement, getHint()).getExtensionPoint();
	}

	/**
	 * @return the attribute name of the {@link IConfigurationElement} that
	 *         contains the description that you want to see in the wizard page.
	 */
	protected String getMappingName() {
		return RegistryUtil.getStruct(applicationElement, getHint()).getMappingName();
	}

	/**
	 * Returns the list of {@link MApplicationElement}s of the type passed in
	 * the constructor of the wizard.
	 *
	 * @return an array of {@link MApplicationElement}
	 */
	public MApplicationElement[] getElements(Class<? extends MApplicationElement> type) {
		return RegistryUtil.getModelElements(type, getHint(), application, page1.getConfigurationElements());
	}

	public AbstractComponentEditor<?> getEditor() {
		return editor;
	}

	/**
	 * Returns if this is a live model.
	 *
	 * @return true or false
	 */
	public boolean isLiveModel() {
		return editor.getEditor().isLiveModel();
	}

	/**
	 * Returns the hint that explains the meaning of the caller.
	 *
	 * @return the hint as a String
	 */
	public String getHint() {
		return hint;
	}
}
