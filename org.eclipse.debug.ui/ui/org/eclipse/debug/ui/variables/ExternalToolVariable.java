/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.variables;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Abtract representation of the different variables.
 */
public class ExternalToolVariable {
	private static final IVariableComponent defaultComponent = new DefaultVariableComponent(false);
	
	private String tag;
	private String description;
	private IConfigurationElement element;
	private IVariableExpander expander;

	/**
	 * Creates an variable definition
	 * 
	 * @param tag the variable tag
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	/*package*/ ExternalToolVariable(String tag, String description, IConfigurationElement element) {
		super();
		this.tag = tag;
		this.description = description;
		this.element = element;
	}
	
	/**
	 * Returns the object that can expand the variable
	 */
	public IVariableExpander getExpander() {
		if (expander == null) {
			try {
				expander = (IVariableExpander) createObject(ExternalToolVariableRegistry.TAG_EXPANDER_CLASS);
			} catch (ClassCastException exception) {
			}
			if (expander == null) {
				return DefaultVariableExpander.getDefault();
			}
		}
		return expander;
	}
	
	/**
	 * Creates an instance of the class specified by
	 * the given element attribute name. Can return
	 * <code>null</code> if none or if problems creating
	 * the instance.
	 */
	protected final Object createObject(String attributeName) {
		try {
			return element.createExecutableExtension(attributeName);
		} catch (CoreException e) {
			DebugUIPlugin.log(e.getStatus());
			return null;
		}
	}
	
	/**
	 * Returns the component class to allow
	 * visual editing of the variable's value.
	 */
	public final IVariableComponent getComponent() {
		String className = element.getAttribute(ExternalToolVariableRegistry.TAG_COMPONENT_CLASS);
		if (className == null || className.trim().length() == 0)
			return defaultComponent;
			
		Object component = createObject(ExternalToolVariableRegistry.TAG_COMPONENT_CLASS);
		if (component == null)
			return new DefaultVariableComponent(true);
		else
			return (IVariableComponent)component;
	}
	
	/**
	 * Returns the variable's description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Returns the variable's tag
	 */
	public final String getTag() {
		return tag;
	}


	/**
	 * Default variable component implementation which does not
	 * allow variable value editing visually.
	 */	
	private static final class DefaultVariableComponent extends AbstractVariableComponent {
		private boolean showError = false;
		private Label message = null;
		
		public DefaultVariableComponent(boolean showError) {
			super();
			this.showError = showError;
		}
		
		/* (non-Javadoc)
		 * Method declared on IVariableComponent.
		 */
		public Control getControl() {
			return message;
		}
				
		/* (non-Javadoc)
		 * Method declared on IVariableComponent.
		 */
		public void createContents(Composite parent, String varTag, IGroupDialogPage page) {
			if (showError) {
				message = new Label(parent, SWT.NONE);
				GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
				message.setLayoutData(data);
				message.setFont(parent.getFont());
				message.setText(LaunchConfigurationsMessages.getString("ExternalToolVariable.Problem_displaying_UI_component_of_selected_variable._1")); //$NON-NLS-1$
				message.setForeground(JFaceColors.getErrorText(message.getDisplay()));
			}
		}
	}
}
