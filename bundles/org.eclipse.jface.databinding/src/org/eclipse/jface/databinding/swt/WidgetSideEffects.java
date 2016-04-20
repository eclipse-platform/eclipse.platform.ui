/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.observable.sideeffect.CompositeSideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

/**
 * A factory for the creation of an {@link ISideEffectFactory}, which internally
 * manages the created ISideEffects in an {@link CompositeSideEffect}. The given
 * Widget is used to attach an {@link DisposeListener} so that all
 * {@link ISideEffect} objects will be disposed automatically on Widget
 * disposal.
 *
 * @since 1.8
 */
public final class WidgetSideEffects {

	/**
	 * Creates an {@link ISideEffectFactory} which will dispose all created
	 * {@link ISideEffect} instances automatically in case the given
	 * {@link Widget} is disposed.
	 *
	 * @param disposableWidget
	 *            {@link Widget} where a dispose listener will be attached to
	 *            automatically dispose all {@link ISideEffect} instances which
	 *            have been created by the returned {@link ISideEffectFactory}.
	 * @return ISideEffectFactory
	 */
	public static ISideEffectFactory createFactory(Widget disposableWidget) {
		CompositeSideEffect compositeSideEffect = (CompositeSideEffect) disposableWidget
				.getData(CompositeSideEffect.class.getName());
		if (compositeSideEffect == null) {
			CompositeSideEffect newCompositeSideEffect = new CompositeSideEffect();
			disposableWidget.setData(CompositeSideEffect.class.getName(), newCompositeSideEffect);
			disposableWidget.addDisposeListener(e -> newCompositeSideEffect.dispose());
			compositeSideEffect = newCompositeSideEffect;
		}
		return ISideEffectFactory.createFactory(compositeSideEffect::add);
	}

	private WidgetSideEffects() {
	}
}
