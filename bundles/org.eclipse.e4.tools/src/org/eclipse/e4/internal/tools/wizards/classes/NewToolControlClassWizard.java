/*******************************************************************************
 * Copyright (c) 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dmitry Spiridenok <d.spiridenok@gmail.com> - Bug 412672
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
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
	private String initialString;

	public NewToolControlClassWizard(String contributionURI) {
		this.initialString = contributionURI;
	}

	public NewToolControlClassWizard() {
		// Intentionally left empty
	}

	@Override
	protected String getContent() {
		ToolControlTemplate template = new ToolControlTemplate();
		return template.generate(getDomainClass());
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation",
				"New Tool Control",
				"Create a new tool control class", root, ResourcesPlugin.getWorkspace().getRoot(),initialString) {

			@Override
			protected JavaClass createInstance() {
				return new ToolControlClass(root);
			}

			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
				{
					Label l = new Label(parent, SWT.NONE);
					l.setText("Create GUI Method");

					Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(
							textProp.observe(t),
							BeanProperties.value("createGuiMethodName").observe(
									getClazz()));

					l = new Label(parent, SWT.NONE);
				}
				{
					Label l = new Label(parent, SWT.NONE);
					l.setText("Create Default Constructor");

//					Text t = new Text(parent, SWT.BORDER);
//					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//					dbc.bindValue(textProp.observe(t),
//							BeanProperties.value("defaultConstructorName")
//									.observe(getClazz()));
//					dbc.bindValue(
//							WidgetProperties.enabled().observe(t),
//							BeanProperties.value("useDefaultConstructor").observe(
//									getClazz()));

					Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
							WidgetProperties.selection().observe(b),
							BeanProperties.value("createDefaultConstructor").observe(
									getClazz()));
				}
			}
		});
	}

	@Override
	protected Set<String> getRequiredBundles() {
		Set<String> set = super.getRequiredBundles();
		set.add("org.eclipse.e4.core.di");
		return set;
	}

	public static class ToolControlClass extends JavaClass {
		private String createGuiMethodName = "createGui";
		private boolean createDefaultCostructor = false;

		public ToolControlClass(IPackageFragmentRoot root) {
			super(root);
		}

		public String getCreateGuiMethodName() {
			return createGuiMethodName;
		}

		public void setCreateGuiMethodName(String createGuiMethodName) {
			support.firePropertyChange("createGuiMethodName",
					this.createGuiMethodName,
					this.createGuiMethodName = createGuiMethodName);
		}

		public boolean isCreateDefaultConstructor() {
			return createDefaultCostructor;
		}

		public void setCreateDefaultConstructor(boolean createDefaultConstructor) {
			support.firePropertyChange("createDefaultConstructor", this.createDefaultCostructor,
					this.createDefaultCostructor = createDefaultConstructor);
		}
	}
}
