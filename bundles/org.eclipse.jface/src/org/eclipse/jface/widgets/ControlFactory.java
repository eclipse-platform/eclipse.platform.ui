/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract factory for controls. Factories for controls that inherit from
 * Control should extend this factory to handle the properties of Control
 * itself, like enabled or tool tip.
 *
 * @param <F>
 * @param <C>
 */
public abstract class ControlFactory<F extends ControlFactory<?,?>, C extends Control> {
	private Class<F> factoryClass;

	private String tooltipText;
	private Boolean enabled;

	private Object layoutData;

	private Function<Composite, C> controlCreator;

	/**
	 * @param factoryClass
	 * @param controlCreator
	 */
	protected ControlFactory(Class<F> factoryClass, Function<Composite, C> controlCreator) {
		this.factoryClass = factoryClass;
		this.controlCreator = controlCreator;
	}

	/**
	 * Sets the tool tip.
	 *
	 * @param tooltipText
	 * @return this
	 */
	public F tooltip(String tooltipText) {
		this.tooltipText = tooltipText;
		return factoryClass.cast(this);
	}

	/**
	 * Sets the enabled state.
	 *
	 * @param enabled
	 * @return this
	 */
	public F enabled(boolean enabled) {
		this.enabled = Boolean.valueOf(enabled);
		return factoryClass.cast(this);
	}

	/**
	 * Sets the layoutData.
	 *
	 * @param layoutData
	 * @return this
	 */
	public F layoutData(Object layoutData) {
		this.layoutData = layoutData;
		return factoryClass.cast(this);
	}

	/**
	 * @param parent
	 * @return this
	 */
	public final C create(Composite parent) {
		C control = controlCreator.apply(parent);
		applyProperties(control);
		return control;
	}

	/**
	 * Applies all the properties for the control which have been set by the
	 * caller.<br>
	 *
	 * @param control
	 */
	protected void applyProperties(C control) {
		if (this.enabled != null) {
			control.setEnabled(this.enabled.booleanValue());
		}
		if (this.tooltipText != null) {
			control.setToolTipText(this.tooltipText);
		}
		if (this.layoutData != null) {
			control.setLayoutData(this.layoutData);
		}
	}
}