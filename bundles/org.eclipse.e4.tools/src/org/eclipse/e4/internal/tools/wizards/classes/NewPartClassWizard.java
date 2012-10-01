/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Sopot Cela <sopotcela@gmail.com>
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.resources.ResourcesPlugin;
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

	private String initialString;

	public NewPartClassWizard(String contributionURI) {
		this.initialString = contributionURI;
	}
	
	public NewPartClassWizard() {
		// Intentially left empty 
	}

	@Override
	public void addPages() {
		addPage(new AbstractNewClassPage("Classinformation", "New Part",
				"Create a new part class", root, ResourcesPlugin.getWorkspace()
						.getRoot(), initialString) {

			@Override
			protected void createFields(Composite parent, DataBindingContext dbc) {
				IWidgetValueProperty textProp = WidgetProperties
						.text(SWT.Modify);
				IWidgetValueProperty enabledProp = WidgetProperties.enabled();

				{
					Label l = new Label(parent, SWT.NONE);
					l.setText("PostContruct Method");

					Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
							BeanProperties.value("postConstructMethodName")
									.observe(getClazz()));
					dbc.bindValue(
							enabledProp.observe(t),
							BeanProperties.value("usePostConstruct").observe(
									getClazz()));

					Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
							WidgetProperties.selection().observe(b),
							BeanProperties.value("usePostConstruct").observe(
									getClazz()));
				}
				
				{
					Label l = new Label(parent, SWT.NONE);
					l.setText("Predestroy Method");

					Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
							BeanProperties.value("preDestroyMethodName")
									.observe(getClazz()));
					dbc.bindValue(
							enabledProp.observe(t),
							BeanProperties.value("usePredestroy").observe(
									getClazz()));

					Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
							WidgetProperties.selection().observe(b),
							BeanProperties.value("usePredestroy").observe(
									getClazz()));
				}
				
				{
					Label l = new Label(parent, SWT.NONE);
					l.setText("Focus Method");

					Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
							BeanProperties.value("focusMethodName")
									.observe(getClazz()));
					dbc.bindValue(
							enabledProp.observe(t),
							BeanProperties.value("useFocus").observe(
									getClazz()));

					Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
							WidgetProperties.selection().observe(b),
							BeanProperties.value("useFocus").observe(
									getClazz()));
				}
				
				{
					Label l = new Label(parent, SWT.NONE);
					l.setText("Persist Method");

					Text t = new Text(parent, SWT.BORDER);
					t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					dbc.bindValue(textProp.observe(t),
							BeanProperties.value("persistMethodName")
									.observe(getClazz()));
					dbc.bindValue(
							enabledProp.observe(t),
							BeanProperties.value("usePersist").observe(
									getClazz()));

					Button b = new Button(parent, SWT.CHECK);
					dbc.bindValue(
							WidgetProperties.selection().observe(b),
							BeanProperties.value("usePersist").observe(
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
		Set<String> rv = super.getRequiredBundles();
		PartClass cl = (PartClass)getDomainClass();
		if( cl.usePostConstruct || cl.usePredestroy ) {
			rv.add("javax.annotation");
		} else if( cl.useFocus || cl.usePersist ) {
			rv.add("org.eclipse.e4.ui.di");
		}
		
		return rv;
	}
	
	@Override
	protected String getContent() {
		PartTemplate template = new PartTemplate();
		return template.generate(getDomainClass());
	}

	public static class PartClass extends JavaClass {
		private PropertyChangeSupport support = new PropertyChangeSupport(this);

		private boolean usePostConstruct;
		private String postConstructMethodName = "postConstruct";

		private boolean usePredestroy;
		private String preDestroyMethodName = "preDestroy";

		private boolean useFocus = true;
		private String focusMethodName = "onFocus";

		private boolean usePersist;
		private String persistMethodName = "save";

		public PartClass(IPackageFragmentRoot fragmentRoot) {
			super(fragmentRoot);
		}
		
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			support.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			support.removePropertyChangeListener(listener);
		}

		public boolean isUsePostConstruct() {
			return usePostConstruct;
		}

		public void setUsePostConstruct(boolean usePostConstruct) {
			support.firePropertyChange("usePostConstruct",
					this.usePostConstruct,
					this.usePostConstruct = usePostConstruct);
		}

		public String getPostConstructMethodName() {
			return postConstructMethodName;
		}

		public void setPostConstructMethodName(String postConstructMethodName) {
			support.firePropertyChange("postConstructMethodName",
					this.postConstructMethodName,
					this.postConstructMethodName = postConstructMethodName);
		}

		public boolean isUsePredestroy() {
			return usePredestroy;
		}

		public void setUsePredestroy(boolean usePredestroy) {
			support.firePropertyChange("usePredestroy", this.usePredestroy,
					this.usePredestroy = usePredestroy);
		}

		public String getPreDestroyMethodName() {
			return preDestroyMethodName;
		}

		public void setPreDestroyMethodName(String preDestroyMethodName) {
			support.firePropertyChange("preDestroyMethodName",
					this.preDestroyMethodName,
					this.preDestroyMethodName = preDestroyMethodName);
		}

		public boolean isUseFocus() {
			return useFocus;
		}

		public void setUseFocus(boolean useFocus) {
			support.firePropertyChange("useFocus", this.useFocus,
					this.useFocus = useFocus);
		}

		public String getFocusMethodName() {
			return focusMethodName;
		}

		public void setFocusMethodName(String focusMethodName) {
			support.firePropertyChange("focusMethodName", this.focusMethodName,
					this.focusMethodName = focusMethodName);
		}

		public boolean isUsePersist() {
			return usePersist;
		}

		public void setUsePersist(boolean usePersist) {
			support.firePropertyChange("usePersist", this.usePersist,
					this.usePersist = usePersist);
		}

		public String getPersistMethodName() {
			return persistMethodName;
		}

		public void setPersistMethodName(String persistMethodName) {
			support.firePropertyChange("persistMethodName",
					this.persistMethodName,
					this.persistMethodName = persistMethodName);
		}
	}
}
