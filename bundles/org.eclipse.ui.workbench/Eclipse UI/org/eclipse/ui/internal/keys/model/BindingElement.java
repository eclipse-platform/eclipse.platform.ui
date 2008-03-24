/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys.model;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.keys.NewKeysPreferenceMessages;
import org.eclipse.ui.internal.util.Util;

/**
 * @since 3.4
 * 
 */
public class BindingElement extends ModelElement {

	public static final String PROP_TRIGGER = "trigger"; //$NON-NLS-1$
	public static final String PROP_CONTEXT = "bindingContext"; //$NON-NLS-1$
	public static final String PROP_CATEGORY = "category"; //$NON-NLS-1$
	public static final String PROP_USER_DELTA = "userDelta"; //$NON-NLS-1$
	private static final String PROP_IMAGE = "image"; //$NON-NLS-1$
	public static final String PROP_CONFLICT = "bindingConflict"; //$NON-NLS-1$
	private TriggerSequence trigger;
	private ContextElement context;
	private String category;
	private Integer userDelta;
	private Image image;
	private Boolean conflict;

	/**
	 * @param kc
	 */
	public BindingElement(KeyController kc) {
		super(kc);
	}

	/**
	 * @param b
	 * @param model
	 */
	public void init(Binding b, ContextModel model) {
		setCommandInfo(b.getParameterizedCommand());
		setTrigger(b.getTriggerSequence());
		setContext((ContextElement) model.getContextIdToElement().get(
				b.getContextId()));
		setUserDelta(new Integer(b.getType()));
		setModelObject(b);
	}

	/**
	 * @param bindingCommand
	 */
	private void setCommandInfo(ParameterizedCommand bindingCommand) {
		setId(bindingCommand.getId());
		try {
			setName(bindingCommand.getName());
		} catch (NotDefinedException e) {
			setName(NewKeysPreferenceMessages.Undefined_Command);
		}
		try {
			setDescription(bindingCommand.getCommand().getDescription());
		} catch (NotDefinedException e) {
			setDescription(Util.ZERO_LENGTH_STRING);
		}
		try {
			setCategory(bindingCommand.getCommand().getCategory().getName());
		} catch (NotDefinedException e) {
			setCategory(NewKeysPreferenceMessages.Unavailable_Category);
		}
		setConflict(Boolean.FALSE);
	}

	/**
	 * @param cmd
	 * @param type
	 *            The binding type. Check {@link Binding} constants.
	 */
	public void init(ParameterizedCommand cmd) {
		setCommandInfo(cmd);
		setTrigger(null);
		setContext(null);
		setUserDelta(new Integer(Binding.SYSTEM));

		setModelObject(cmd);
	}

	/**
	 * @return Returns the trigger.
	 */
	public TriggerSequence getTrigger() {
		return trigger;
	}

	/**
	 * @param trigger
	 *            The trigger to set.
	 */
	public void setTrigger(TriggerSequence trigger) {
		Object old = this.trigger;
		this.trigger = trigger;
		controller.firePropertyChange(this, PROP_TRIGGER, old, trigger);
	}

	/**
	 * @return Returns the context.
	 */
	public ContextElement getContext() {
		return context;
	}

	/**
	 * @param context
	 *            The context to set.
	 */
	public void setContext(ContextElement context) {
		Object old = this.context;
		this.context = context;
		controller.firePropertyChange(this, PROP_CONTEXT, old, context);
	}

	/**
	 * @return Returns the category.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            The category to set.
	 */
	public void setCategory(String category) {
		Object old = this.category;
		this.category = category;
		controller.firePropertyChange(this, PROP_CATEGORY, old, category);
	}

	/**
	 * @return Returns the userDelta.
	 */
	public Integer getUserDelta() {
		return userDelta;
	}

	/**
	 * @param userDelta
	 *            The userDelta to set.
	 */
	public void setUserDelta(Integer userDelta) {
		Object old = this.userDelta;
		this.userDelta = userDelta;
		controller.firePropertyChange(this, PROP_USER_DELTA, old, userDelta);
	}

	/**
	 * @return Returns the image.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @param image
	 *            The image to set.
	 */
	public void setImage(Image image) {
		Object old = this.image;
		this.image = image;
		controller.firePropertyChange(this, PROP_IMAGE, old, image);
	}

	/**
	 * @return Returns the conflict.
	 */
	public Boolean getConflict() {
		return conflict;
	}

	/**
	 * @param conflict
	 *            The conflict to set.
	 */
	public void setConflict(Boolean conflict) {
		Object old = this.conflict;
		this.conflict = conflict;
		controller.firePropertyChange(this, PROP_CONFLICT, old, conflict);
	}

	/**
	 * @param binding
	 * @param contextModel
	 */
	public void fill(KeyBinding binding, ContextModel contextModel) {
		setCommandInfo(binding.getParameterizedCommand());
		setTrigger(binding.getTriggerSequence());
		setContext((ContextElement) contextModel.getContextIdToElement().get(
				binding.getContextId()));
		setUserDelta(new Integer(binding.getType()));
		setModelObject(binding);
	}

	/**
	 * @param parameterizedCommand
	 */
	public void fill(ParameterizedCommand parameterizedCommand) {
		setCommandInfo(parameterizedCommand);
		setTrigger(null);
		setContext(null);
		setUserDelta(new Integer(Binding.SYSTEM));
		setModelObject(parameterizedCommand);
	}
}
