/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.themes;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import junit.framework.TestCase;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemeRegistry;
import org.eclipse.ui.internal.themes.WorkbenchThemeManager.ThemeRegistryModifiedHandler;
import org.eclipse.ui.themes.ITheme;
import org.osgi.service.event.Event;

import static org.mockito.Mockito.*;

/**
 * @since 3.5
 *
 */
public class ThemeRegistryModifiedHandlerTest extends TestCase {
	public void testHandleEvent() throws Exception {
		//given
		FontDefinition fontDefinition1 = mock(FontDefinition.class);
		doReturn("fontDefinition1").when(fontDefinition1).getId();
		doReturn(true).when(fontDefinition1).isOverridden();

		FontDefinition fontDefinition2 = mock(FontDefinition.class);
		doReturn("fontDefinition2").when(fontDefinition2).getId();
		doReturn(false).when(fontDefinition2).isOverridden();

		ColorDefinition colorDefinition = mock(ColorDefinition.class);
		doReturn("colorDefinition").when(colorDefinition).getId();
		doReturn(true).when(colorDefinition).isOverridden();

		ThemeRegistry themeRegistry = spy(new ThemeRegistry());
		doReturn(new FontDefinition[]{fontDefinition1, fontDefinition2}).when(themeRegistry).getFonts();
		doReturn(new ColorDefinition[] {colorDefinition}).when(themeRegistry).getColors();

		FontRegistry fontRegistry = mock(FontRegistry.class);

		ColorRegistry colorRegistry = mock(ColorRegistry.class);

		ITheme colorsAndFontsTheme = mock(ITheme.class);
		doReturn("3.x theme id").when(colorsAndFontsTheme).getId();

		org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme = mock(org.eclipse.e4.ui.css.swt.theme.ITheme.class);
		doReturn("css theme id").when(cssTheme).getId();

		ThemeRegistryModifiedHandlerTestable handler = spy(new ThemeRegistryModifiedHandlerTestable());
		doReturn(themeRegistry).when(handler).getThemeRegistry();
		doReturn(fontRegistry).when(handler).getFontRegistry();
		doReturn(colorRegistry).when(handler).getColorRegistry();
		doReturn(colorsAndFontsTheme).when(handler).getColorsAndFontsTheme();
		doReturn(cssTheme).when(handler).getTheme();

		//when
		handler.handleEvent(mock(Event.class));

		//then
		verify(handler, times(1)).populateThemeRegistries(themeRegistry, fontRegistry, colorRegistry, cssTheme, colorsAndFontsTheme);

		verify(handler, times(1)).sendThemeDefinitionChangedEvent();
	}

	public void testPopulateThemeRegistries() throws Exception {
		//given
		FontDefinition fontDefinition1 = mock(FontDefinition.class);
		doReturn("fontDefinition1").when(fontDefinition1).getId();
		doReturn(true).when(fontDefinition1).isOverridden();

		FontDefinition fontDefinition2 = mock(FontDefinition.class);
		doReturn("fontDefinition2").when(fontDefinition2).getId();
		doReturn(false).when(fontDefinition2).isOverridden();
		doReturn(false).when(fontDefinition2).isAddedByCss();

		ColorDefinition colorDefinition1 = mock(ColorDefinition.class);
		doReturn("colorDefinition1").when(colorDefinition1).getId();
		doReturn(false).when(colorDefinition1).isOverridden();
		doReturn(false).when(colorDefinition1).isAddedByCss();

		ColorDefinition colorDefinition2 = mock(ColorDefinition.class);
		doReturn("colorDefinition2").when(colorDefinition2).getId();
		doReturn(true).when(colorDefinition2).isAddedByCss();

		ThemeRegistry themeRegistry = spy(new ThemeRegistry());
		doReturn(new FontDefinition[]{fontDefinition1, fontDefinition2}).when(themeRegistry).getFonts();
		doReturn(new ColorDefinition[] {colorDefinition1, colorDefinition2}).when(themeRegistry).getColors();

		ThemeRegistryModifiedHandlerTestable handler = spy(new ThemeRegistryModifiedHandlerTestable());

		//when
		handler.populateThemeRegistries(themeRegistry, mock(FontRegistry.class), mock(ColorRegistry.class),
				mock(org.eclipse.e4.ui.css.swt.theme.ITheme.class), mock(ITheme.class));


		//then
		verify(fontDefinition1, times(1)).isOverridden();
		verify(fontDefinition1, never()).isAddedByCss();
		verify(handler, times(1)).populateDefinition(any(org.eclipse.e4.ui.css.swt.theme.ITheme.class),
				any(ITheme.class), any(FontRegistry.class), eq(fontDefinition1), any(IPreferenceStore.class));

		verify(fontDefinition2, times(1)).isOverridden();
		verify(fontDefinition2, times(1)).isAddedByCss();
		verify(handler, never()).populateDefinition(any(org.eclipse.e4.ui.css.swt.theme.ITheme.class),
				any(ITheme.class), any(FontRegistry.class), eq(fontDefinition2), any(IPreferenceStore.class));

		verify(colorDefinition1, times(1)).isOverridden();
		verify(colorDefinition1, times(1)).isAddedByCss();
		verify(handler, never()).populateDefinition(any(org.eclipse.e4.ui.css.swt.theme.ITheme.class),
				any(ITheme.class), any(ColorRegistry.class), eq(colorDefinition1), any(IPreferenceStore.class));

		verify(colorDefinition2, times(1)).isOverridden();
		verify(colorDefinition2, times(1)).isAddedByCss();
		verify(handler, times(1)).populateDefinition(any(org.eclipse.e4.ui.css.swt.theme.ITheme.class),
				any(ITheme.class), any(ColorRegistry.class), eq(colorDefinition2), any(IPreferenceStore.class));
	}

	public static class ThemeRegistryModifiedHandlerTestable extends ThemeRegistryModifiedHandler {
		@Override
		public void populateThemeRegistries(ThemeRegistry themeRegistry,
				FontRegistry fontRegistry, ColorRegistry colorRegistry,
				org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme, ITheme theme) {
			super.populateThemeRegistries(themeRegistry, fontRegistry, colorRegistry, cssTheme, theme);
		}

		@Override
		public void sendThemeDefinitionChangedEvent() {
		}

		@Override
		public org.eclipse.e4.ui.css.swt.theme.ITheme getTheme() {
			return null;
		}

		@Override
		public ITheme getColorsAndFontsTheme() {
			return null;
		}

		@Override
		public ThemeRegistry getThemeRegistry() {
			return null;
		}

		@Override
		public FontRegistry getFontRegistry() {
			return null;
		}

		@Override
		public ColorRegistry getColorRegistry() {
			return null;
		}

		@Override
		public void populateDefinition(org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme,
				ITheme theme, FontRegistry registry, FontDefinition definition,
				IPreferenceStore store) {
		}

		@Override
		public void populateDefinition(org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme,
				ITheme theme, ColorRegistry registry, ColorDefinition definition,
				IPreferenceStore store) {
		}
	}
}
