/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.RetargetAction;

/**
 * This class adapts instances of <code>IAction</code> to
 * <code>IHandler</code>.
 * 
 * @since 3.0
 */
public class ActionHandler extends AbstractHandler {

    private final static String ATTRIBUTE_CHECKED = "checked"; //$NON-NLS-1$

    private final static String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

    private final static String ATTRIBUTE_HANDLED = "handled"; //$NON-NLS-1$

    private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    private final static String ATTRIBUTE_STYLE = "style"; //$NON-NLS-1$

    private IAction action;

    private Set definedAttributeNames;

    /**
     * Creates a new instance of this class given an instance of
     * <code>IAction</code>.
     * 
     * @param action
     *            the action. Must not be <code>null</code>.
     */
    public ActionHandler(IAction action) {
        super();
        if (action == null) throw new NullPointerException();

        this.action = action;
        definedAttributeNames = new HashSet();
        definedAttributeNames.add(ATTRIBUTE_CHECKED);
        definedAttributeNames.add(ATTRIBUTE_ENABLED);
        definedAttributeNames.add(ATTRIBUTE_HANDLED);
        definedAttributeNames.add(ATTRIBUTE_ID);
        definedAttributeNames.add(ATTRIBUTE_STYLE);
        definedAttributeNames = Collections
                .unmodifiableSet(definedAttributeNames);
    }

    public void execute(Object parameter) throws ExecutionException {
        if ((action.getStyle() == IAction.AS_CHECK_BOX)
                || (action.getStyle() == IAction.AS_RADIO_BUTTON)) {
            action.setChecked(!action.isChecked());
        }

        try {
            if (parameter instanceof Event)
                action.runWithEvent((Event) parameter);
            else
                action.run();
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    public Object getAttributeValue(String attributeName)
            throws NoSuchAttributeException {
        if (!definedAttributeNames.contains(attributeName))
            throw new NoSuchAttributeException();
        else if (ATTRIBUTE_CHECKED.equals(attributeName))
            return action.isChecked() ? Boolean.TRUE : Boolean.FALSE;
        else if (ATTRIBUTE_ENABLED.equals(attributeName))
            return action.isEnabled() ? Boolean.TRUE : Boolean.FALSE;
        else if (ATTRIBUTE_HANDLED.equals(attributeName)) {
            if (action instanceof RetargetAction) {
                RetargetAction retargetAction = (RetargetAction) action;
                return (retargetAction.getActionHandler() != null) ? Boolean.TRUE
                        : Boolean.FALSE;
            } else
                return Boolean.TRUE;
        } else if (ATTRIBUTE_ID.equals(attributeName))
            return action.getId();
        else if (ATTRIBUTE_STYLE.equals(attributeName))
            return new Integer(action.getStyle());
        else
            throw new NoSuchAttributeException();
    }

    public Set getDefinedAttributeNames() {
        return definedAttributeNames;
    }
}