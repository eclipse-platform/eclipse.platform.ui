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
import java.util.HashMap;
import java.util.Map;
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
 */
public final class ActionHandler extends AbstractHandler {

    private final static String ATTRIBUTE_CHECKED = "checked"; //$NON-NLS-1$

    private final static String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$

    /*
     * TODO This should be changed. CommandManager should not look for this
     * attribute (search by string "handled" to find it..). Instead, code in
     * workbench should be changed such that if a RetargetAction loses its
     * action, this ActionHandler instance's corresponding HandlerSubmission
     * should be removed. In any case, this attribute especially should never be
     * made public. Also, RetargetAction doesn't not notify that this property
     * has changed. All handler attributes must notify listeners on change.
     */
    private final static String ATTRIBUTE_HANDLED = "handled"; //$NON-NLS-1$

    private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

    private final static String ATTRIBUTE_STYLE = "style"; //$NON-NLS-1$

    private IAction action;

    private Map attributeValuesByName;

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
        this.attributeValuesByName = getAttributeValuesByNameFromAction();
        this.action.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                String property = propertyChangeEvent.getProperty();
                if (IAction.ENABLED.equals(property)
                        || IAction.CHECKED.equals(property)) {
                    Map previousAttributeValuesByName = attributeValuesByName;
                    attributeValuesByName = getAttributeValuesByNameFromAction();
                    if (!attributeValuesByName
                            .equals(previousAttributeValuesByName))
                            fireHandlerChanged(new HandlerEvent(
                                    ActionHandler.this, true,
                                    previousAttributeValuesByName));
                }
            }
        });
    }

    public Object execute(Map parameterValuesByName) throws ExecutionException {
        if ((action.getStyle() == IAction.AS_CHECK_BOX)
                || (action.getStyle() == IAction.AS_RADIO_BUTTON))
                action.setChecked(!action.isChecked());
        try {
            action.runWithEvent(new Event());
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    public Map getAttributeValuesByName() {
        return attributeValuesByName;
    }

    private Map getAttributeValuesByNameFromAction() {
        Map attributeValuesByName = new HashMap();
        attributeValuesByName.put(ATTRIBUTE_CHECKED,
                action.isChecked() ? Boolean.TRUE : Boolean.FALSE);
        attributeValuesByName.put(ATTRIBUTE_ENABLED,
                action.isEnabled() ? Boolean.TRUE : Boolean.FALSE);
        boolean handled = true;
        if (action instanceof RetargetAction) {
            RetargetAction retargetAction = (RetargetAction) action;
            handled = retargetAction.getActionHandler() != null;
        }
        attributeValuesByName.put(ATTRIBUTE_HANDLED, handled ? Boolean.TRUE
                : Boolean.FALSE);
        attributeValuesByName.put(ATTRIBUTE_ID, action.getId());
        attributeValuesByName.put(ATTRIBUTE_STYLE, new Integer(action
                .getStyle()));
        return Collections.unmodifiableMap(attributeValuesByName);
    }
}