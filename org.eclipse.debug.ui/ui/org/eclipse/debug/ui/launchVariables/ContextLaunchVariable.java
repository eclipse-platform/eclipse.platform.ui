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
package org.eclipse.debug.ui.launchVariables;


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
 * Abtract representation of launch configuration variables.
 * @since 3.0
 */
public class ContextLaunchVariable implements IContextLaunchVariable {
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
	public ContextLaunchVariable(String tag, String description, IConfigurationElement element) {
		super();
		this.tag = tag;
		this.description = description;
		this.element = element;
	}
	
	/**
	 * @see IContextLaunchVariable
	 */
	public IVariableExpander getExpander() {
		if (expander == null) {
			try {
				expander = (IVariableExpander) createObject(ContextLaunchVariableRegistry.TAG_EXPANDER_CLASS);
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
	 * @see IContextLaunchVariable#getComponent()
	 */
	public final IVariableComponent getComponent() {
		String className = element.getAttribute(ContextLaunchVariableRegistry.TAG_COMPONENT_CLASS);
		if (className == null || className.trim().length() == 0)
			return defaultComponent;
			
		Object component = createObject(ContextLaunchVariableRegistry.TAG_COMPONENT_CLASS);
		if (component == null)
			return new DefaultVariableComponent(true);
		else
			return (IVariableComponent)component;
	}
	
	/**
	 * @see IContextLaunchVariable#getDescription()
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @see IContextLaunchVariable#getName()
	 */
	public final String getName() {
		return tag;
	}


	/**
	 * Default variable component implementation which does not
	 * allow variable value editing visually.
	 */	
	protected static final class DefaultVariableComponent extends AbstractVariableComponent {
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
		public void createContents(Composite parent, String varTag, IVariableComponentContainer page) {
			container= page;
			if (showError) {
				message = new Label(parent, SWT.NONE);
				GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
				message.setLayoutData(data);
				message.setFont(parent.getFont());
				message.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationVariable.Problem_displaying_UI")); //$NON-NLS-1$
				message.setForeground(JFaceColors.getErrorText(message.getDisplay()));
			}
		}
	}
}
