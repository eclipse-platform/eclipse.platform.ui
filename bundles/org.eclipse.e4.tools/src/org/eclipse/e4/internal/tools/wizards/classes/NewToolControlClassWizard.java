/*******************************************************************************
 * Copyright (c) 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Spiridenok <d.spiridenok@gmail.com> - Bug 412672
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
import org.eclipse.e4.internal.tools.wizards.classes.templates.ToolControlTemplate;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewToolControlClassWizard extends AbstractNewClassWizard {
	private static final String CREATE_DEFAULT_CONSTRUCTOR = "createDefaultConstructor"; //$NON-NLS-1$
	private static final String CREATE_GUI_METHOD_NAME = "createGuiMethodName"; //$NON-NLS-1$
	private String initialString;

	public NewToolControlClassWizard(String contributionURI) {
		initialString = contributionURI;
	}

	public NewToolControlClassWizard() {
		// Intentionally left empty
	}

	@Override
	protected String getContent() {
		final ToolControlTemplate template = new ToolControlTemplate();
		return template.generate(getDomainClass());
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation", //$NON-NLS-1$
			Messages.NewToolControlClassWizard_NewToolControl,
			Messages.NewToolControlClassWizard_CreateNewToolControl, root, ResourcesPlugin.getWorkspace().getRoot(), initialString) {

			@Override
			protected JavaClass createInstance() {
				return new ToolControlClass(root);
			}

			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
				{
					Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewToolControlClassWizard_CreateGUIMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(
						textProp.observe(t),
						BeanProperties.value(CREATE_GUI_METHOD_NAME).observe(
							getClazz()));

					l = new Label(parent, SWT.NONE);
				}
				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewToolControlClassWizard_CreateDefaultConstructor);

					// Text t = new Text(parent, SWT.BORDER);
					// t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					// dbc.bindValue(textProp.observe(t),
					// BeanProperties.value("defaultConstructorName")
					// .observe(getClazz()));
					// dbc.bindValue(
					// WidgetProperties.enabled().observe(t),
					// BeanProperties.value("useDefaultConstructor").observe(
					// getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(CREATE_DEFAULT_CONSTRUCTOR).observe(
							getClazz()));
				}
			}
		});
	}

	@Override
	protected Set<String> getRequiredBundles() {
		final Set<String> set = super.getRequiredBundles();
		set.add("org.eclipse.e4.core.di"); //$NON-NLS-1$
		return set;
	}

	public static class ToolControlClass extends JavaClass {
		private String createGuiMethodName = "createGui"; //$NON-NLS-1$
		private boolean createDefaultCostructor = false;

		public ToolControlClass(IPackageFragmentRoot root) {
			super(root);
		}

		public String getCreateGuiMethodName() {
			return createGuiMethodName;
		}

		public void setCreateGuiMethodName(String createGuiMethodName) {
			support.firePropertyChange(CREATE_GUI_METHOD_NAME,
				this.createGuiMethodName,
				this.createGuiMethodName = createGuiMethodName);
		}

		public boolean isCreateDefaultConstructor() {
			return createDefaultCostructor;
		}

		public void setCreateDefaultConstructor(boolean createDefaultConstructor) {
			support.firePropertyChange(CREATE_DEFAULT_CONSTRUCTOR, createDefaultCostructor,
				createDefaultCostructor = createDefaultConstructor);
		}
	}
}
