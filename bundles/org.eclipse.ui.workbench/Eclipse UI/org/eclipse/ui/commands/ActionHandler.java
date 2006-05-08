/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IHandlerAttributes;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.RetargetAction;

/**
 * This class adapts instances of <code>IAction</code> to
 * <code>IHandler</code>.
 * 
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.jface.commands.ActionHandler
 */
public final class ActionHandler extends AbstractHandler {

    /**
     * The attribute name for the checked property of the wrapped action. This
     * indicates whether the action should be displayed with as a checked check
     * box.
     */
    private final static String ATTRIBUTE_CHECKED = "checked"; //$NON-NLS-1$

    /**
     * The attribute name for the enabled property of the wrapped action.
     */
    private final static String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

    /**
     * <p>
     * The name of the attribute indicating whether the wrapped instance of
     * <code>RetargetAction</code> has a handler.
     * </p>
     */
    private final static String ATTRIBUTE_HANDLED = IHandlerAttributes.ATTRIBUTE_HANDLED;

    /**
     * The attribute name for the identifier of the wrapped action. This is the
     * action identifier, and not the command identifier.
     */
    private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    /**
     * The attribute name for the visual style of the wrapped action. The style
     * can be things like a pull-down menu, a check box, a radio button or a
     * push button.
     */
    private final static String ATTRIBUTE_STYLE = "style"; //$NON-NLS-1$

    /**
     * The wrapped action. This value is never <code>null</code>.
     */
    private final IAction action;

    /**
     * The map of attributes values. The keys are <code>String</code> values
     * of the attribute names (given above). The values can be any type of
     * <code>Object</code>.
     * 
     * This map is always null if there are no IHandlerListeners registered.
     *  
     */
    private Map attributeValuesByName;

    /**
     * The property change listener hooked on to the action. This is initialized
     * when the first listener is attached to this handler, and is removed when
     * the handler is disposed or the last listener is removed.
     */
    private IPropertyChangeListener propertyChangeListener;

    /**
     * Creates a new instance of this class given an instance of
     * <code>IAction</code>.
     * 
     * @param action
     *            the action. Must not be <code>null</code>.
     */
    public ActionHandler(IAction action) {
        if (action == null) {
			throw new NullPointerException();
		}

        this.action = action;
    }

    /**
     * @see org.eclipse.ui.commands.IHandler#addHandlerListener(org.eclipse.ui.commands.IHandlerListener)
     * @since 3.1
     */
    public void addHandlerListener(IHandlerListener handlerListener) {
        if (!hasListeners()) {
            attachListener();
        }

        super.addHandlerListener(handlerListener);
    }

    /**
     * When a listener is attached to this handler, then this registers a
     * listener with the underlying action.
     * 
     * @since 3.1
     */
    private final void attachListener() {
        if (propertyChangeListener == null) {
            attributeValuesByName = getAttributeValuesByNameFromAction();

            propertyChangeListener = new IPropertyChangeListener() {
                public void propertyChange(
                        PropertyChangeEvent propertyChangeEvent) {
                    String property = propertyChangeEvent.getProperty();
                    if (IAction.ENABLED.equals(property)
                            || IAction.CHECKED.equals(property)
                            || IHandlerAttributes.ATTRIBUTE_HANDLED
                                    .equals(property)) {

                        Map previousAttributeValuesByName = attributeValuesByName;
                        attributeValuesByName = getAttributeValuesByNameFromAction();
                        if (!attributeValuesByName
                                .equals(previousAttributeValuesByName)) {
							fireHandlerChanged(new HandlerEvent(
                                    ActionHandler.this, true,
                                    previousAttributeValuesByName));
						}
                    }
                }
            };
        }

        this.action.addPropertyChangeListener(propertyChangeListener);
    }

    /**
     * When no more listeners are registered, then this is used to removed the
     * property change listener from the underlying action.
     * 
     * @since 3.1
     *  
     */
    private final void detachListener() {
        this.action.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = null;
        attributeValuesByName = null;
    }

    /**
     * Removes the property change listener from the action.
     * 
     * @see org.eclipse.ui.commands.IHandler#dispose()
     */
    public void dispose() {
        if (hasListeners()) {
            action.removePropertyChangeListener(propertyChangeListener);
        }
    }

   
    /* (non-Javadoc)
     * @see org.eclipse.ui.commands.IHandler#execute(java.util.Map)
     */
    public Object execute(Map parameterValuesByName) throws ExecutionException {
        if ((action.getStyle() == IAction.AS_CHECK_BOX)
                || (action.getStyle() == IAction.AS_RADIO_BUTTON)) {
			action.setChecked(!action.isChecked());
		}
        try {
            action.runWithEvent(new Event());
        } catch (Exception e) {
            throw new ExecutionException(
                    "While executing the action, an exception occurred", e); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Returns the action associated with this handler
     * 
     * @return the action associated with this handler (not null)
     * @since 3.1
     */
    public IAction getAction() {
        return action;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.commands.IHandler#getAttributeValuesByName()
     */
    public Map getAttributeValuesByName() {
        if (attributeValuesByName == null) {
            return getAttributeValuesByNameFromAction();
        }

        return attributeValuesByName;
    }

    /**
     * An accessor for the attribute names from the action. This reads out all
     * of the attributes from an action into a local map.
     * 
     * @return A map of the attribute values indexed by the attribute name. The
     *         attributes names are strings, but the values can by any object.
     *  
     */
    private Map getAttributeValuesByNameFromAction() {
        Map map = new HashMap();
        map.put(ATTRIBUTE_CHECKED, action.isChecked() ? Boolean.TRUE
                : Boolean.FALSE);
        map.put(ATTRIBUTE_ENABLED, action.isEnabled() ? Boolean.TRUE
                : Boolean.FALSE);
        boolean handled = true;
        if (action instanceof RetargetAction) {
            RetargetAction retargetAction = (RetargetAction) action;
            handled = retargetAction.getActionHandler() != null;
        }
        map.put(ATTRIBUTE_HANDLED, handled ? Boolean.TRUE : Boolean.FALSE);
        map.put(ATTRIBUTE_ID, action.getId());
        map.put(ATTRIBUTE_STYLE, new Integer(action.getStyle()));
        return Collections.unmodifiableMap(map);
    }

    /**
     * @see org.eclipse.ui.commands.IHandler#removeHandlerListener(org.eclipse.ui.commands.IHandlerListener)
     * @since 3.1
     */
    public void removeHandlerListener(IHandlerListener handlerListener) {
        super.removeHandlerListener(handlerListener);

        if (!hasListeners()) {
            detachListener();
        }
    }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("ActionHandler(action="); //$NON-NLS-1$
		buffer.append(action);
		buffer.append(')');

		return buffer.toString();
	}
}
