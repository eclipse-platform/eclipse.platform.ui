/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
import org.eclipse.e4.internal.tools.wizards.classes.templates.ImperativeExpressionTemplate;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewImperativeExpressionClassWizard extends AbstractNewClassWizard {
	private static final String EVALUATE_METHOD_NAME = "evaluateMethodName"; //$NON-NLS-1$

	private String initialString;

	public NewImperativeExpressionClassWizard(String contributionURI) {
		initialString = contributionURI;
	}

	public NewImperativeExpressionClassWizard() {
		// Intentially left empty
	}

	@Override
	protected String getContent() {
		final ImperativeExpressionTemplate template = new ImperativeExpressionTemplate();
		return template.generate(getDomainClass());
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage(
				"Classinformation", //$NON-NLS-1$
				Messages.NewImperativeExpressionClassWizard_NewImperativeExpression,
				Messages.NewImperativeExpressionClassWizard_CreateNewImperativeExpression, root,
				ResourcesPlugin.getWorkspace().getRoot(),
				initialString) {

			@Override
			protected JavaClass createInstance() {
				return new ImperativeExpressionClass(root);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				final IWidgetValueProperty textProp = WidgetProperties
						.text(SWT.Modify);

				{
					Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewImperativeExpressionClassWizard_EvaluateMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(
							textProp.observe(t),
							BeanProperties.value(EVALUATE_METHOD_NAME).observe(
									getClazz()));

					l = new Label(parent, SWT.NONE);
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

	public static class ImperativeExpressionClass extends JavaClass {
		private String evaluateMethodName = "evaluate"; //$NON-NLS-1$

		public ImperativeExpressionClass(IPackageFragmentRoot root) {
			super(root);
		}

		public String getEvaluateMethodName() {
			return evaluateMethodName;
		}

		public void setEvaluateMethodName(String evaluateMethodName) {
			support.firePropertyChange(EVALUATE_METHOD_NAME,
					this.evaluateMethodName,
					this.evaluateMethodName = evaluateMethodName);
		}

	}
}
