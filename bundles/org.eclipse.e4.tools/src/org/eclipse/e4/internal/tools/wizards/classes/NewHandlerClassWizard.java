/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Sopot Cela <sopotcela@gmail.com>
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
import org.eclipse.e4.internal.tools.wizards.classes.templates.HandlerTemplate;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewHandlerClassWizard extends AbstractNewClassWizard {
	private static final String USE_CAN_EXECUTE = "useCanExecute"; //$NON-NLS-1$
	private static final String CAN_EXECUTE_METHOD_NAME = "canExecuteMethodName"; //$NON-NLS-1$
	private static final String EXECUTE_METHOD_NAME = "executeMethodName"; //$NON-NLS-1$
	private String initialString;

	public NewHandlerClassWizard(String contributionURI) {
		initialString = contributionURI;
	}

	public NewHandlerClassWizard() {
		// Intentially left empty
	}

	@Override
	protected String getContent() {
		final HandlerTemplate template = new HandlerTemplate();
		return template.generate(getDomainClass());
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage(
			"Classinformation", //$NON-NLS-1$
			Messages.NewHandlerClassWizard_NewHandler,
			Messages.NewHandlerClassWizard_CreateNewHandler, root, ResourcesPlugin.getWorkspace().getRoot(),
			initialString) {

			@Override
			protected JavaClass createInstance() {
				return new HandlerClass(root);
			}

			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				final IWidgetValueProperty textProp = WidgetProperties
					.text(SWT.Modify);

				{
					Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewHandlerClassWizard_ExecuteMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(
						textProp.observe(t),
						BeanProperties.value(EXECUTE_METHOD_NAME).observe(
							getClazz()));

					l = new Label(parent, SWT.NONE);
				}

				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewHandlerClassWizard_CanExecuteMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
						BeanProperties.value(CAN_EXECUTE_METHOD_NAME)
						.observe(getClazz()));
					dbc.bindValue(
						WidgetProperties.enabled().observe(t),
						BeanProperties.value(USE_CAN_EXECUTE).observe(
							getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(USE_CAN_EXECUTE).observe(
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

	public static class HandlerClass extends JavaClass {
		private String executeMethodName = "execute"; //$NON-NLS-1$
		private String canExecuteMethodName = "canExecute"; //$NON-NLS-1$
		private boolean useCanExecute = false;

		public HandlerClass(IPackageFragmentRoot root) {
			super(root);
		}

		public String getExecuteMethodName() {
			return executeMethodName;
		}

		public void setExecuteMethodName(String executeMethodName) {
			support.firePropertyChange(EXECUTE_METHOD_NAME,
				this.executeMethodName,
				this.executeMethodName = executeMethodName);
		}

		public String getCanExecuteMethodName() {
			return canExecuteMethodName;
		}

		public void setCanExecuteMethodName(String canExecuteMethodName) {
			support.firePropertyChange(CAN_EXECUTE_METHOD_NAME,
				this.canExecuteMethodName,
				this.canExecuteMethodName = canExecuteMethodName);
		}

		public boolean isUseCanExecute() {
			return useCanExecute;
		}

		public void setUseCanExecute(boolean useCanExecute) {
			support.firePropertyChange(USE_CAN_EXECUTE, this.useCanExecute,
				this.useCanExecute = useCanExecute);
		}
	}
}
