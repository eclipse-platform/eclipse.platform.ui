/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumber;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumeric;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

/**
 * Applies the CSS properties that map directly onto a single
 * {@link CTabFolder} setter.
 */
public class CSSPropertyCTabFolderSWTHandler extends AbstractCSSPropertySWTHandler {

	private record BooleanProperty(BiConsumer<CTabFolder, Boolean> setter, Predicate<CTabFolder> getter) {
	}

	private record IntProperty(Function<CSSValue, Integer> parser, BiConsumer<CTabFolder, Integer> setter,
			ToIntFunction<CTabFolder> getter) {
	}

	private static final Map<String, BooleanProperty> BOOLEAN_PROPERTIES = Map.ofEntries(
			Map.entry("border-visible", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setBorderVisible, CTabFolder::getBorderVisible)),
			Map.entry("swt-maximize-visible", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setMaximizeVisible, CTabFolder::getMaximizeVisible)),
			Map.entry("swt-minimize-visible", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setMinimizeVisible, CTabFolder::getMinimizeVisible)),
			Map.entry("swt-maximized", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setMaximized, CTabFolder::getMaximized)),
			Map.entry("swt-minimized", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setMinimized, CTabFolder::getMinimized)),
			Map.entry("swt-simple", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setSimple, CTabFolder::getSimple)),
			Map.entry("swt-single", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setSingle, CTabFolder::getSingle)),
			Map.entry("swt-unselected-close-visible", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setUnselectedCloseVisible, CTabFolder::getUnselectedCloseVisible)),
			Map.entry("swt-unselected-image-visible", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setUnselectedImageVisible, CTabFolder::getUnselectedImageVisible)),
			Map.entry("swt-selected-image-visible", //$NON-NLS-1$
					new BooleanProperty(CTabFolder::setSelectedImageVisible, CTabFolder::getSelectedImageVisible)));

	private static final Map<String, IntProperty> INT_PROPERTIES = Map.of(
			"swt-tab-height", //$NON-NLS-1$
			new IntProperty(
					value -> value instanceof CssNumeric numeric && numeric.unit() == CssUnit.PX
							? Integer.valueOf((int) numeric.value())
							: null,
					CTabFolder::setTabHeight, CTabFolder::getTabHeight),
			"swt-tab-text-minimum-characters", //$NON-NLS-1$
			new IntProperty(
					value -> value instanceof CssNumber number ? Integer.valueOf((int) number.value()) : null,
					CTabFolder::setMinimumCharacters, CTabFolder::getMinimumCharacters));

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof CTabFolder folder)) {
			return;
		}
		BooleanProperty booleanProperty = BOOLEAN_PROPERTIES.get(property);
		if (booleanProperty != null) {
			booleanProperty.setter().accept(folder, (Boolean) engine.convert(value, Boolean.class, null));
			return;
		}
		IntProperty intProperty = INT_PROPERTIES.get(property);
		if (intProperty != null) {
			Integer parsed = intProperty.parser().apply(value);
			if (parsed != null) {
				intProperty.setter().accept(folder, parsed);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof CTabFolder folder)) {
			return null;
		}
		BooleanProperty booleanProperty = BOOLEAN_PROPERTIES.get(property);
		if (booleanProperty != null) {
			return Boolean.toString(booleanProperty.getter().test(folder));
		}
		IntProperty intProperty = INT_PROPERTIES.get(property);
		if (intProperty != null) {
			return Integer.toString(intProperty.getter().applyAsInt(folder));
		}
		return null;
	}
}
