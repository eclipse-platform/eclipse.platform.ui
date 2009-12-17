/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.IObjectContributor;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The DeclarativeDecoratorDefinition is a decorator definition that is defined
 * entirely from xml and will not require the activation of its defining
 * plug-in.
 */
class LightweightDecoratorDefinition extends DecoratorDefinition implements
		IObjectContributor {

	private static final String ATT_LOCATION = "location"; //$NON-NLS-1$

	static final String ATT_ICON = "icon"; //$NON-NLS-1$

	// Constants for quadrants
	/**
	 * Position <code>TOP_LEFT</code>. Value <code>0</code>
	 */
	public static final int TOP_LEFT = 0;

	/**
	 * Position <code>TOP_RIGHT</code>. Value <code>1</code>
	 */
	public static final int TOP_RIGHT = 1;

	/**
	 * Position <code>BOTTOM_LEFT</code>. Value <code>2</code>
	 */
	public static final int BOTTOM_LEFT = 2;

	/**
	 * Position <code>BOTTOM_RIGHT</code>. Value <code>3</code>
	 */
	public static final int BOTTOM_RIGHT = 3;

	/**
	 * Position <code>UNDERLAY</code>. Value <code>4</code>
	 */
	public static final int UNDERLAY = 4;

	private static final String UNDERLAY_STRING = "UNDERLAY"; //$NON-NLS-1$

	/**
	 * Position <code>REPLACE</code>. Value <code>5</code>
	 */
	public static final int REPLACE = 5;
	
	private static final String REPLACE_STRING = "REPLACE"; //$NON-NLS-1$
	
	private static final String ATT_QUADRANT = "quadrant"; //$NON-NLS-1$

	// Constants for quadrants
	private static final String TOP_LEFT_STRING = "TOP_LEFT"; //$NON-NLS-1$

	private static final String TOP_RIGHT_STRING = "TOP_RIGHT"; //$NON-NLS-1$

	private static final String BOTTOM_LEFT_STRING = "BOTTOM_LEFT"; //$NON-NLS-1$

	/**
	 * The DeclarativeDecorator is the internal decorator supplied by the
	 * decorator definition.
	 */
	private ILightweightLabelDecorator decorator;

	private int quadrant;

	private boolean hasReadQuadrant;

	private String[] objectClasses;

	LightweightDecoratorDefinition(String identifier,
			IConfigurationElement element) {
		super(identifier, element);
	}

	/**
	 * Gets the decorator and creates it if it does not exist yet. Throws a
	 * CoreException if there is a problem creating the decorator. This method
	 * should not be called unless a check for enabled to be true is done first.
	 * 
	 * @return Returns a ILabelDecorator
	 */
	protected ILightweightLabelDecorator internalGetDecorator()
			throws CoreException {
		if (labelProviderCreationFailed) {
			return null;
		}

		final CoreException[] exceptions = new CoreException[1];

		if (decorator == null) {

			if (isDeclarative()) {
				decorator = new DeclarativeDecorator(definingElement,
						getIconLocation());
			} else {

				Platform.run(new ISafeRunnable() {
					public void run() {
						try {
							decorator = (ILightweightLabelDecorator) WorkbenchPlugin
									.createExtension(definingElement,
											DecoratorDefinition.ATT_CLASS);
							decorator.addListener(WorkbenchPlugin.getDefault()
									.getDecoratorManager());
						} catch (CoreException exception) {
							exceptions[0] = exception;
						}
					}

					/*
					 * (non-Javadoc) Method declared on ISafeRunnable.
					 */
					public void handleException(Throwable e) {
						// Do nothing as Core will handle the logging
					}
				});
			}
		} else {
			return decorator;
		}

		if (decorator == null) {
			this.labelProviderCreationFailed = true;
			setEnabled(false);
		}

		if (exceptions[0] != null) {
			throw exceptions[0];
		}

		return decorator;
	}

	/**
	 * Return whether or not this represents a declarative decorator.
	 * 
	 * @return boolean <code>true</code> if this is declarative
	 */
	private boolean isDeclarative() {
		return definingElement.getAttribute(DecoratorDefinition.ATT_CLASS) == null;
	}

	/**
	 * Return the icon location.
	 * 
	 * @return the icon location
	 */
	private String getIconLocation() {
		return definingElement.getAttribute(ATT_ICON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#internalGetLabelProvider()
	 */
	protected IBaseLabelProvider internalGetLabelProvider()
			throws CoreException {
		return internalGetDecorator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#isFull()
	 */
	public boolean isFull() {
		return false;
	}

	/**
	 * Returns the quadrant.One of the following constants in
	 * DecoratorRegistryReader: TOP_LEFT TOP_RIGHT BOTTOM_LEFT BOTTOM_RIGHT
	 * UNDERLAY REPLACE
	 * 
	 * @return int
	 */
	public int getQuadrant() {
		if (!hasReadQuadrant) {
			hasReadQuadrant = true;
			quadrant = getLocationConstant(definingElement
					.getAttribute(ATT_LOCATION), definingElement);
		}
		return quadrant;
	}

	/**
	 * Get the constant value based on the location supplied. Default to bottom
	 * right.
	 * 
	 * @since 3.1
	 */
	private int getLocationConstant(String locationDefinition,
			IConfigurationElement element) {

		// Backwards compatibility
		if (locationDefinition == null) {
			locationDefinition = element.getAttribute(ATT_QUADRANT);
		}

		if (TOP_RIGHT_STRING.equals(locationDefinition)) {
			return TOP_RIGHT;
		}
		if (TOP_LEFT_STRING.equals(locationDefinition)) {
			return TOP_LEFT;
		}
		if (BOTTOM_LEFT_STRING.equals(locationDefinition)) {
			return BOTTOM_LEFT;
		}
		if (UNDERLAY_STRING.equals(locationDefinition)) {
			return UNDERLAY;
		}
		if (REPLACE_STRING.equals(locationDefinition)) {
			return REPLACE;
		}
		return BOTTOM_RIGHT;

	}

	/**
	 * Decorate the element using the decoration to store the result.
	 * @param element
	 * @param decoration
	 */
	public void decorate(Object element, IDecoration decoration) {
		try {
			// Internal decorator might be null so be prepared
			ILightweightLabelDecorator currentDecorator = internalGetDecorator();
			if(currentDecorator == null) {
				return;
			}
			
			if (isAdaptable()) {
				String[] classes = getObjectClasses();
				for (int i = 0; i < classes.length; i++) {
					String className = classes[i];
					Object adapted = LegacyResourceSupport.getAdapter(element,
							className);
					if (adapted != null) {
						currentDecorator.decorate(adapted, decoration);
					}					
				}				
			}
			else{
				if (currentDecorator != null && element != null) {
					currentDecorator.decorate(element, decoration);
				}
			}
		} catch (CoreException exception) {
			handleCoreException(exception);
		}

	}

	/**
	 * Returns the lightweight decorator, or <code>null</code> if not enabled.
	 * 
	 * @return the lightweight decorator, or <code>null</code> if not enabled
	 */
	public ILightweightLabelDecorator getDecorator() {
		return decorator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#refreshDecorator()
	 */
	protected void refreshDecorator() {
		// Only do something if disabled so as to prevent
		// gratutitous activation
		if (!this.enabled && decorator != null) {
			IBaseLabelProvider cached = decorator;
			decorator = null;
			disposeCachedDecorator(cached);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IObjectContributor#isApplicableTo(java.lang.Object)
	 */
	public boolean isApplicableTo(Object object) {
		return isEnabledFor(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IObjectContributor#canAdapt()
	 */
	public boolean canAdapt() {
		return isAdaptable();
	}

	/**
	 * Get the object classes to which this decorator is registered.
	 * 
	 * @return String [] the object classes to which this decorator is
	 *         registered
	 */
	public String[] getObjectClasses() {
		if (objectClasses == null) {
			getEnablement();
		}
		return objectClasses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#initializeEnablement()
	 */
	protected void initializeEnablement() {
		super.initializeEnablement();
		ActionExpression expression = getEnablement();
		if (expression != null) {
			objectClasses = expression.extractObjectClasses();
		}

		// If the class is null set it to Object
		if (objectClasses == null) {
			objectClasses = new String[] {Object.class.getName()};
		}
	}

}
