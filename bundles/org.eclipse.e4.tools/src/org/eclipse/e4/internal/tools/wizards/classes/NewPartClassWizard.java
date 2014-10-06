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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
import org.eclipse.e4.internal.tools.wizards.classes.templates.PartTemplate;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewPartClassWizard extends AbstractNewClassWizard {

	private static final String PERSIST_METHOD_NAME = "persistMethodName"; //$NON-NLS-1$
	private static final String USE_PERSIST = "usePersist"; //$NON-NLS-1$
	private static final String USE_FOCUS = "useFocus"; //$NON-NLS-1$
	private static final String FOCUS_METHOD_NAME = "focusMethodName"; //$NON-NLS-1$
	private static final String USE_PREDESTROY = "usePredestroy"; //$NON-NLS-1$
	private static final String PRE_DESTROY_METHOD_NAME = "preDestroyMethodName"; //$NON-NLS-1$
	private static final String USE_POST_CONSTRUCT = "usePostConstruct"; //$NON-NLS-1$
	private static final String POST_CONSTRUCT_METHOD_NAME = "postConstructMethodName"; //$NON-NLS-1$
	private String initialString;

	public NewPartClassWizard(String contributionURI) {
		initialString = contributionURI;
	}

	public NewPartClassWizard() {
		// Intentially left empty
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation", Messages.NewPartClassWizard_NewPart, //$NON-NLS-1$
			Messages.NewPartClassWizard_CreateNewPart, root, ResourcesPlugin.getWorkspace()
			.getRoot(), initialString) {

			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				final IWidgetValueProperty textProp = WidgetProperties
					.text(SWT.Modify);
				final IWidgetValueProperty enabledProp = WidgetProperties.enabled();

				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewPartClassWizard_PostConstructMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
						BeanProperties.value(POST_CONSTRUCT_METHOD_NAME)
						.observe(getClazz()));
					dbc.bindValue(
						enabledProp.observe(t),
						BeanProperties.value(USE_POST_CONSTRUCT).observe(
							getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(USE_POST_CONSTRUCT).observe(
							getClazz()));
				}

				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewPartClassWizard_PredestroyMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
						BeanProperties.value(PRE_DESTROY_METHOD_NAME)
						.observe(getClazz()));
					dbc.bindValue(
						enabledProp.observe(t),
						BeanProperties.value(USE_PREDESTROY).observe(
							getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(USE_PREDESTROY).observe(
							getClazz()));
				}

				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewPartClassWizard_FocusMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
						BeanProperties.value(FOCUS_METHOD_NAME)
						.observe(getClazz()));
					dbc.bindValue(
						enabledProp.observe(t),
						BeanProperties.value(USE_FOCUS).observe(
							getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(USE_FOCUS).observe(
							getClazz()));
				}

				{
					final Label l = new Label(parent, SWT.NONE);
					l.setText(Messages.NewPartClassWizard_PersistMethod);

					final Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
						BeanProperties.value(PERSIST_METHOD_NAME)
						.observe(getClazz()));
					dbc.bindValue(
						enabledProp.observe(t),
						BeanProperties.value(USE_PERSIST).observe(
							getClazz()));

					final Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
						WidgetProperties.selection().observe(b),
						BeanProperties.value(USE_PERSIST).observe(
							getClazz()));
				}
			}

			@Override
			protected JavaClass createInstance() {
				return new PartClass(root);
			}

		});
	}

	@Override
	protected Set<String> getRequiredBundles() {
		final Set<String> rv = super.getRequiredBundles();
		final PartClass cl = (PartClass) getDomainClass();
		if (cl.usePostConstruct || cl.usePredestroy) {
			rv.add("javax.annotation"); //$NON-NLS-1$
		} else if (cl.useFocus || cl.usePersist) {
			rv.add("org.eclipse.e4.ui.di"); //$NON-NLS-1$
		}

		return rv;
	}

	@Override
	protected String getContent() {
		final PartTemplate template = new PartTemplate();
		return template.generate(getDomainClass());
	}

	public static class PartClass extends JavaClass {

		private final PropertyChangeSupport support = new PropertyChangeSupport(this);

		private boolean usePostConstruct = true;
		private String postConstructMethodName = "postConstruct"; //$NON-NLS-1$

		private boolean usePredestroy;
		private String preDestroyMethodName = "preDestroy"; //$NON-NLS-1$

		private boolean useFocus;
		private String focusMethodName = "onFocus"; //$NON-NLS-1$

		private boolean usePersist;
		private String persistMethodName = "save"; //$NON-NLS-1$

		public PartClass(IPackageFragmentRoot fragmentRoot) {
			super(fragmentRoot);
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			support.addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			support.removePropertyChangeListener(listener);
		}

		public boolean isUsePostConstruct() {
			return usePostConstruct;
		}

		public void setUsePostConstruct(boolean usePostConstruct) {
			support.firePropertyChange(USE_POST_CONSTRUCT,
				this.usePostConstruct,
				this.usePostConstruct = usePostConstruct);
		}

		public String getPostConstructMethodName() {
			return postConstructMethodName;
		}

		public void setPostConstructMethodName(String postConstructMethodName) {
			support.firePropertyChange(POST_CONSTRUCT_METHOD_NAME,
				this.postConstructMethodName,
				this.postConstructMethodName = postConstructMethodName);
		}

		public boolean isUsePredestroy() {
			return usePredestroy;
		}

		public void setUsePredestroy(boolean usePredestroy) {
			support.firePropertyChange(USE_PREDESTROY, this.usePredestroy,
				this.usePredestroy = usePredestroy);
		}

		public String getPreDestroyMethodName() {
			return preDestroyMethodName;
		}

		public void setPreDestroyMethodName(String preDestroyMethodName) {
			support.firePropertyChange(PRE_DESTROY_METHOD_NAME,
				this.preDestroyMethodName,
				this.preDestroyMethodName = preDestroyMethodName);
		}

		public boolean isUseFocus() {
			return useFocus;
		}

		public void setUseFocus(boolean useFocus) {
			support.firePropertyChange(USE_FOCUS, this.useFocus,
				this.useFocus = useFocus);
		}

		public String getFocusMethodName() {
			return focusMethodName;
		}

		public void setFocusMethodName(String focusMethodName) {
			support.firePropertyChange(FOCUS_METHOD_NAME, this.focusMethodName,
				this.focusMethodName = focusMethodName);
		}

		public boolean isUsePersist() {
			return usePersist;
		}

		public void setUsePersist(boolean usePersist) {
			support.firePropertyChange(USE_PERSIST, this.usePersist,
				this.usePersist = usePersist);
		}

		public String getPersistMethodName() {
			return persistMethodName;
		}

		public void setPersistMethodName(String persistMethodName) {
			support.firePropertyChange(PERSIST_METHOD_NAME,
				this.persistMethodName,
				this.persistMethodName = persistMethodName);
		}
	}
}
