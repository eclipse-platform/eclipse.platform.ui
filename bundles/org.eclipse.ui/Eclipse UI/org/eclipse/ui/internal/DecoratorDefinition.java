package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The DecoratorDefinition is the class that holds onto
 * the label decorator, the name and the name of the
 * class a decorator definition applies to,
 */

public class DecoratorDefinition {

	private String name;
	private String objectClass;
	private String description;
	private ILabelDecorator decorator;
	private boolean adaptable;
	private boolean enabled;
	private boolean defaultEnabled;

	//A flag that is set if there is an error creating the decorator
	private boolean decoratorCreationFailed = false;
	private String id;
	private IConfigurationElement element;

	/**
	 * Create a new instance of the receiver with the
	 * supplied values.
	 */

	DecoratorDefinition(
		String identifier,
		String label,
		String decoratorDescription,
		String className,
		boolean isAdaptable,
		boolean initEnabled,
		IConfigurationElement configElement) {
		this.id = identifier;
		this.name = label;
		this.objectClass = className;
		this.adaptable = isAdaptable;
		this.element = configElement;
		this.description = decoratorDescription;
		this.enabled = initEnabled;
		this.defaultEnabled = initEnabled;
	}

	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription(){
		return this.description;
	}

	/**
	 * Gets the objectClass.
	 * @return Returns a String
	 */
	public String getObjectClass() {
		return objectClass;
	}

	/**
	 * Gets the decorator and creates it if it does
	 * not exist yet. Throws a CoreException if there is a problem
	 * creating the decorator.
	 * This method should not be called unless a check for
	 * enabled to be true is done first.
	 * @return Returns a ILabelDecorator
	 */
	private ILabelDecorator internalGetDecorator() throws CoreException {
		if (decoratorCreationFailed)
			return null;

		final CoreException[] exceptions = new CoreException[1];

		if (decorator == null) {
			Platform.run(new SafeRunnableAdapter(WorkbenchMessages.format("DecoratorManager.ErrorActivatingDecorator", new String[] { getName()})) { //$NON-NLS-1$
				public void run() {
					try {
						decorator =
							(ILabelDecorator) WorkbenchPlugin.createExtension(
								element,
								WizardsRegistryReader.ATT_CLASS);
					} catch (CoreException exception) {
						exceptions[0] = exception;
					}
				}
			});
		}

		if (decorator == null) {
			this.decoratorCreationFailed = true;
			this.enabled = false;
			return null;
		}

		if (exceptions[0] != null)
			throw exceptions[0];

		return decorator;
	}

	/**
	 * Gets the enabled.
	 * @return Returns a boolean
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled flag and adds or removes the decorator
	 * manager as a listener as appropriate.
	 * @param enabled The enabled to set
	 */
	public void setEnabled(boolean newState) throws CoreException {

		//Only refresh if there has been a change
		if (this.enabled != newState) {
			this.enabled = newState;
			refreshDecorator();
		}
	}

	/**
	 * Sets the enabled flag and adds or removes the decorator
	 * manager as a listener as appropriate. Handle any exceptions
	 * within this class
	 * @param enabled The enabled to set
	 */
	public void setEnabledWithErrorHandling(boolean newState) {

		try {
			setEnabled(newState);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
	}

	/**
	 * Refresh the current decorator based on our enable
	 * state.
	 */

	private void refreshDecorator() throws CoreException {
		DecoratorManager manager =
			(DecoratorManager) WorkbenchPlugin
				.getDefault()
				.getDecoratorManager();

		if (this.enabled) {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				currentDecorator.addListener(manager);
		} else {
			if (decorator != null) {
				ILabelDecorator cached = decorator;
				cached.removeListener(manager);
				//Clear the decorator before disposing
				decorator = null;
				cached.dispose();
			}
		}

	}

	/**
	 * Return whether or not this decorator should be 
	 * applied to adapted types.
	 */

	public boolean isAdaptable() {
		return adaptable;
	}
	/**
	 * Gets the id.
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}

	/** 
	 * A CoreException has occured. Inform the user and disable
	 * the receiver.
	 */

	private void handleCoreException(CoreException exception) {

		//If there is an error then reset the enabling to false
		MessageDialog.openError(
			null,
			WorkbenchMessages.getString("Internal_error"), //$NON-NLS-1$
			exception.getLocalizedMessage());
		this.enabled = false;
	}

	/**
	 * Decorate the image provided for the element type.
	 * This method should not be called unless a check for
	 * isEnabled() has been done first.
	 * Return null if there is no image or if an error occurs.
	 */
	Image decorateImage(Image image, Object element) {
		try {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				return currentDecorator.decorateImage(image, element);

		} catch (CoreException exception) {
			handleCoreException(exception);
		}
		return null;
	}
	/**
	 * Decorate the text provided for the element type.
	 * This method should not be called unless a check for
	 * isEnabled() has been done first.
	 * Return null if there is no text or if there is an exception.
	 */
	String decorateText(String text, Object element) {
		try {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				return currentDecorator.decorateText(text, element);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
		return null;
	}

	/**
	 * Add a listener for the decorator.If there is an exception
	 * then inform the user and disable the receiver.
	 * This method should not be called unless a check for
	 * isEnabled() has been done first.
	 */
	void addListener(ILabelProviderListener listener) {
		try {
			//Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				currentDecorator.addListener(listener);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name. If there is an 
	* exception disable the receiver and return false.
	* This method should not be called unless a check for
	* isEnabled() has been done first.
	*/
	boolean isLabelProperty(Object element, String property) {
		try { //Internal decorator might be null so be prepared
			ILabelDecorator currentDecorator = internalGetDecorator();
			if (currentDecorator != null)
				return currentDecorator.isLabelProperty(element, property);
		} catch (CoreException exception) {
			handleCoreException(exception);
			return false;
		}
		return false;
	}

	/**
	 * Return the default value for this type - this value
	 * is the value read from the element description.
	 */
	public boolean getDefaultValue(){
		return defaultEnabled;
	}

	/**
	 * Gets the decorator.
	 * @return Returns a ILabelDecorator
	 * @throws CoreException. This will be removed and is only
	 * here for backwards compatability.
	 */
	public ILabelDecorator getDecorator() throws CoreException{
		return decorator;
	}

}