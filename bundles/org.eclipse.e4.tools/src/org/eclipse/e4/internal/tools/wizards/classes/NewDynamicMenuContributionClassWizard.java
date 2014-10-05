/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marco Descher <marco@descher.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
import org.eclipse.e4.internal.tools.wizards.classes.templates.DynamicMenuContributionTemplate;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewDynamicMenuContributionClassWizard extends
AbstractNewClassWizard {

	private static final String ABOUT_TO_SHOW_METHOD_NAME = "aboutToShowMethodName"; //$NON-NLS-1$
	private static final String ABOUT_TO_HIDE_METHOD_NAME = "aboutToHideMethodName"; //$NON-NLS-1$
	private static final String USE_ABOUT_TO_HIDE = "useAboutToHide"; //$NON-NLS-1$
	private final String initialString;

	public NewDynamicMenuContributionClassWizard(String contributionURI) {
		initialString = contributionURI;
	}

	@Override
	protected String getContent() {
		final DynamicMenuContributionTemplate template = new DynamicMenuContributionTemplate();
		return template.generate(getDomainClass());
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation", //$NON-NLS-1$
			Messages.NewDynamicMenuContributionClassWizard_NewDynamicContribution,
			Messages.NewDynamicMenuContributionClassWizard_CreateNewContribution, root, ResourcesPlugin.getWorkspace().getRoot(),
			initialString) {

			@Override
			protected JavaClass createInstance() {
				return new DynamicMenuContributionClass(root);
			}

			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				final IWidgetValueProperty textProp = WidgetProperties
					.text(SWT.Modify);

				{
					Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewDynamicMenuContributionClassWizard_AboutToShowMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(
						textProp.observe(t),
						BeanProperties.value(ABOUT_TO_SHOW_METHOD_NAME).observe(
							getClazz()));

					l = new Label(parent, SWT.NONE);
				}

				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewDynamicMenuContributionClassWizard_AboutToShowMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
						BeanProperties.value(ABOUT_TO_HIDE_METHOD_NAME)
						.observe(getClazz()));
					dbc.bindValue(
						WidgetProperties.enabled().observe(t),
						BeanProperties.value(USE_ABOUT_TO_HIDE).observe(
							getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(USE_ABOUT_TO_HIDE).observe(
							getClazz()));
				}
			}
		});
	}

	@Override
	protected Set<String> getRequiredBundles() {
		final Set<String> set = super.getRequiredBundles();
		set.add("org.eclipse.e4.ui.di"); //$NON-NLS-1$
		set.add("org.eclipse.e4.ui.model.workbench"); //$NON-NLS-1$
		return set;
	}

	public static class DynamicMenuContributionClass extends JavaClass {
		private String aboutToShowMethodName = "aboutToShow"; //$NON-NLS-1$
		private String aboutToHideMethodName = "aboutToHide"; //$NON-NLS-1$
		private boolean useAboutToHide = false;

		public DynamicMenuContributionClass(IPackageFragmentRoot root) {
			super(root);
		}

		public String getAboutToShowMethodName() {
			return aboutToShowMethodName;
		}

		public void setAboutToShowMethodName(String executeMethodName) {
			support.firePropertyChange(ABOUT_TO_SHOW_METHOD_NAME,
				aboutToShowMethodName,
				aboutToShowMethodName = executeMethodName);
		}

		public String getAboutToHideMethodName() {
			return aboutToHideMethodName;
		}

		public void setAboutToHideMethodName(String canExecuteMethodName) {
			support.firePropertyChange(ABOUT_TO_HIDE_METHOD_NAME,
				aboutToHideMethodName,
				aboutToHideMethodName = canExecuteMethodName);
		}

		public boolean isUseAboutToHide() {
			return useAboutToHide;
		}

		public void setUseAboutToHide(boolean useAboutToHide) {
			support.firePropertyChange(USE_ABOUT_TO_HIDE, this.useAboutToHide,
				this.useAboutToHide = useAboutToHide);
		}
	}

}
