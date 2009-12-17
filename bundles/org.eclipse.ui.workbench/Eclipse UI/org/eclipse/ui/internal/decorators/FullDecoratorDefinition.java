/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The RunnableDecoratorDefinition is the definition for 
 * decorators that have an ILabelDecorator class to instantiate.
 */

class FullDecoratorDefinition extends DecoratorDefinition {

	ILabelDecorator decorator;

    /**
     * Create a new instance of the receiver with the
     * supplied values.
     */

    FullDecoratorDefinition(String identifier, IConfigurationElement element) {
        super(identifier, element);
    }

    /**
     * Gets the decorator and creates it if it does
     * not exist yet. Throws a CoreException if there is a problem
     * creating the decorator.
     * This method should not be called unless a check for
     * enabled to be true is done first.
     * @return Returns a ILabelDecorator
     */
    protected ILabelDecorator internalGetDecorator() throws CoreException {
        if (labelProviderCreationFailed) {
			return null;
		}

        final CoreException[] exceptions = new CoreException[1];

        if (decorator == null) {
            Platform
                    .run(new SafeRunnable(
                            NLS.bind(WorkbenchMessages.DecoratorManager_ErrorActivatingDecorator, getName() )) { 
                        public void run() {
                            try {
                                decorator = (ILabelDecorator) WorkbenchPlugin
                                        .createExtension(
                                                definingElement,
                                                DecoratorDefinition.ATT_CLASS);
                                decorator.addListener(WorkbenchPlugin
                                        .getDefault().getDecoratorManager());
                            } catch (CoreException exception) {
                                exceptions[0] = exception;
                            }
                        }
                    });
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#refreshDecorator()
     */
    protected void refreshDecorator() {
        //Only do something if disabled so as to prevent
        //gratutitous activation
        if (!this.enabled && decorator != null) {
            IBaseLabelProvider cached = decorator;
            decorator = null;
            disposeCachedDecorator(cached);
        }
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
            if (currentDecorator != null) {
				return currentDecorator.decorateImage(image, element);
			}

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
            if (currentDecorator != null) {
				return currentDecorator.decorateText(text, element);
			}
        } catch (CoreException exception) {
            handleCoreException(exception);
        }
        return null;
    }

    /**
     * Returns the decorator, or <code>null</code> if not enabled.
     * 
     * @return the decorator, or <code>null</code> if not enabled
     */
    public ILabelDecorator getDecorator() {
        return decorator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#internalGetLabelProvider()
     */
    protected IBaseLabelProvider internalGetLabelProvider()
            throws CoreException {
        return internalGetDecorator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.decorators.DecoratorDefinition#isFull()
     */
    public boolean isFull() {
        return true;
    }

}
