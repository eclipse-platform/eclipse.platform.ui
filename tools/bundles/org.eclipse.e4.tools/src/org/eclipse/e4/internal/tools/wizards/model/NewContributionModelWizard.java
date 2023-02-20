/*******************************************************************************
 * Copyright (c) 2010-2021 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Marco Descher <marco@descher.at> - Bug 392907, Bug 434371
 * Christoph Läubrich - Bug 572946 - [e4][Tooling] support new model-fragment header in the wizard
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class NewContributionModelWizard extends BaseApplicationModelWizard {

	private static final String MODEL_FRAGMENT_HEADER = "Model-Fragment"; //$NON-NLS-1$

	private enum ContributionMode {
		STATIC(Messages.ContributionMode_Static, Messages.ContributionMode_Static_Info), DYNAMIC(
				Messages.ContributionMode_Dynamic, Messages.ContributionMode_Dynamic_Info);

		private String label;
		private String description;

		ContributionMode(String label, String description) {
			this.label = label;
			this.description = description;
		}

		@Override
		public String toString() {
			return label;
		}

		/**
		 * @return the description
		 */
		String getDescription() {
			return description;
		}
	}

	@Override
	public String getDefaultFileName() {
		return "fragment.e4xmi"; //$NON-NLS-1$
	}

	@Override
	protected EObject createInitialModel() {
		return (EObject) MFragmentFactory.INSTANCE.createModelFragments();
	}

	@Override
	protected NewModelFilePage createWizardPage(ISelection selection) {
		return new NewContributionModelFilePage(selection, getDefaultFileName());
	}

	@Override
	protected void adjustDependencies(IFile file) {
		super.adjustFragmentDependencies(file);
	}

	@Override
	protected void registerWithExtensionPointIfRequired(IProject project, WorkspaceBundlePluginModel fModel, IFile file)
			throws CoreException {
		NewContributionModelFilePage page = (NewContributionModelFilePage) getPage(
				NewContributionModelFilePage.PAGE_NAME);
		if (page.mode == ContributionMode.DYNAMIC) {
			IBundle bundle = fModel.getBundleModel().getBundle();
			bundle.setHeader(MODEL_FRAGMENT_HEADER, file.getName());
			fModel.save();
		} else {
			super.registerWithExtensionPointIfRequired(project, fModel, file);
		}
	}

	private static final class NewContributionModelFilePage extends NewModelFilePage {

		ContributionMode mode = ContributionMode.STATIC;

		public NewContributionModelFilePage(ISelection selection, String defaultFilename) {
			super(selection, defaultFilename);
		}

		@Override
		protected void createAdditionalControls(Composite parent) {
			Label label = new Label(parent, SWT.NULL);
			label.setText(Messages.ContributionMode);
			ContributionMode[] values = ContributionMode.values();
			Button[] buttons = new Button[values.length];
			Composite buttonParent = new Composite(parent, SWT.NULL);
			buttonParent.setLayout(new GridLayout(values.length, false));
			for (int i = 0; i < values.length; i++) {
				ContributionMode mode = values[i];
				Button button = buttons[i] = new Button(buttonParent, SWT.RADIO);
				button.setSelection(this.mode == mode);
				button.setData(mode);
				button.setText(mode.toString());
			}
			new Label(parent, SWT.NULL);
			new Label(parent, SWT.NULL);
			Label modeDescriptionLabel = new Label(parent, SWT.NONE);
			modeDescriptionLabel.setText(mode.getDescription());
			GridData modeDescriptionLabelLayoutData = new GridData(GridData.FILL_BOTH);
			modeDescriptionLabelLayoutData.horizontalSpan = 2;
			modeDescriptionLabel.setLayoutData(modeDescriptionLabelLayoutData);
			for (Button button : buttons) {
				button.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button.getSelection()) {
							ContributionMode mode = (ContributionMode) button.getData();
							NewContributionModelFilePage.this.mode = mode;
							modeDescriptionLabel.setText(mode.getDescription());
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {

					}
				});
			}
		}
	}

}