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
package org.eclipse.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDelayedLabelDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * Manages the decorators contributed via the decorators extension point.
 * Views which allow decoration of their elements should use the label
 * decorator returned by <code>getLabelDecorator()</code>.
 * <p>
 * This class is not intended to be implemented by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDecoratorManager extends IDelayedLabelDecorator{

    /**
     * Returns the label decorator which applies the decorations from all
     * enabled decorators.
     * Views which allow decoration of their elements should use this 
     * label decorator.
     * This decorator should be disposed when it is no longer referenced
     * by the caller or the images created within it may be kept until
     * {@link JFaceResources#getResources()} is disposed.
     *
     * @return {@link ILabelDecorator}
     * @see DecoratingLabelProvider
     * @see IBaseLabelProvider#dispose()
     */
    ILabelDecorator getLabelDecorator();

    /**
     * Return the IBaseLabelProvider that corresponds to the
     * decoratorId. This can handle both lightweight and full
     * decorators.
     * 
     * @param decoratorId the decorator id
     * @return the label provider
     */
    IBaseLabelProvider getBaseLabelProvider(String decoratorId);

    /**
     * Returns the full label decorator instance for the specified decorator id
     * if it is enabled. Otherwise returns <code>null</code>. Returns
     * <code>null</code> for lightweight decorators. It is recommended that
     * getBaseLabelProvider is used instead so that lightweight decorators are
     * also checked.
     * 
     * @param decoratorId the decorator id
     * @return the label decorator
     */
    ILabelDecorator getLabelDecorator(String decoratorId);

    /**
     * Returns the lightweight label decorator instance for the specified 
     * decorator id if it is enabled. Otherwise returns <code>null</code>.
     * Returns <code>null</code> for heavyweight decorators.
     * Use <code>getLabelDecorator</code> instead for heavyweight 
     * decorators.
     * 
     * @param decoratorId the decorator id
     * @return the lightweight label decorator
     * @deprecated use getBaseLabelProvider(String) instead.
     */
    ILightweightLabelDecorator getLightweightLabelDecorator(String decoratorId);

    /**
     * Returns whether the specified decorator is enabled.
     * 
     * @param decoratorId the decorator id
     * @return <code>true</code> if the decorator is enabled, or
     *   <code>false</code> if not
     */
    boolean getEnabled(String decoratorId);

    /**
     * Sets whether the specified decorator is enabled.
     * 
     * @param decoratorId the decorator id
     * @param enabled <code>true</code> to enable the decorator, or
     * <code>false</code> to disable it
     * @throws CoreException if the decorator cannot be instantiated
     */
    void setEnabled(String decoratorId, boolean enabled) throws CoreException;

    /**
     * Fire a LabelProviderChangedEvent for the decorator that corresponds to
     * decoratorID if it exists and is enabled using the IBaseLabelProvider
     * as the argument to the event. Otherwise do nothing.
     * <p> This method must be called from the user interface thread as widget
     * updates may result. </p>
     * 
     * @param decoratorId the decorator id
     */
    void update(String decoratorId);

}
