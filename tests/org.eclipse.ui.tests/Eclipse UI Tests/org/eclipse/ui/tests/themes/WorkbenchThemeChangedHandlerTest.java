/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.themes;

import java.util.Arrays;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemeRegistry;
import org.eclipse.ui.internal.themes.ThemesExtension;
import org.eclipse.ui.internal.themes.WorkbenchThemeManager.WorkbenchThemeChangedHandler;
import org.osgi.service.event.Event;

import junit.framework.TestCase;
import static org.mockito.Mockito.*;
import static org.eclipse.ui.internal.themes.WorkbenchThemeManager.EMPTY_COLOR_VALUE;
import static org.eclipse.ui.internal.themes.WorkbenchThemeManager.EMPRY_FONT_DATA_VALUE;

/**
 * @since 3.5
 *
 */
public class WorkbenchThemeChangedHandlerTest extends TestCase {
	public void testOverrideThemeDefinitions() throws Exception {
		//given
		IStylingEngine stylingEngine = mock(IStylingEngine.class);
		
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
		
		ThemesExtension themesExtension = mock(ThemesExtension.class); 
		
		WorkbenchThemeChangedHandlerTestable handler = spy(new WorkbenchThemeChangedHandlerTestable());
		doReturn(stylingEngine).when(handler).getStylingEngine();
		doReturn(themeRegistry).when(handler).getThemeRegistry();
		doReturn(fontRegistry).when(handler).getFontRegistry();
		doReturn(colorRegistry).when(handler).getColorRegistry();
		doReturn(themesExtension).when(handler).createThemesExtension();
		
		
		//when
		handler.handleEvent(mock(Event.class));
		
		//then
		verify(stylingEngine, times(4)).style(anyObject());
		verify(stylingEngine, times(1)).style(themesExtension);
		verify(stylingEngine, times(1)).style(fontDefinition1);
		verify(stylingEngine, times(1)).style(fontDefinition2);
		verify(stylingEngine, times(1)).style(colorDefinition);
		
		verify(fontRegistry, times(2)).put(eq("fontDefinition1"), any(FontData[].class));
		verify(fontRegistry, times(1)).put(eq("fontDefinition1"), eq(EMPRY_FONT_DATA_VALUE));
		verify(fontRegistry, never()).put(eq("fontDefinition2"), any(FontData[].class));
		verify(colorRegistry, times(2)).put(eq("colorDefinition"), any(RGB.class));
		verify(colorRegistry, times(1)).put(eq("colorDefinition"), eq(EMPTY_COLOR_VALUE));
		
		verify(handler, times(1)).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(fontRegistry), eq(fontDefinition1), any(IPreferenceStore.class));
		verify(handler, never()).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(fontRegistry), eq(fontDefinition2), any(IPreferenceStore.class));
		verify(handler, times(1)).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(colorRegistry), eq(colorDefinition), any(IPreferenceStore.class));
		
		verify(stylingEngine, times(1)).style(fontDefinition2);
		verify(stylingEngine, times(1)).style(colorDefinition);
		
		verify(handler, times(1)).sendThemeRegistryRestyledEvent();
	}

	public void testAddThemeDefinitions() throws Exception {
		//given
		IStylingEngine stylingEngine = mock(IStylingEngine.class);
		
		FontDefinition fontDefinition = mock(FontDefinition.class);
		doReturn("fontDefinition").when(fontDefinition).getId();
		doReturn(true).when(fontDefinition).isOverridden();
		
		ColorDefinition colorDefinition = mock(ColorDefinition.class);
		doReturn("colorDefinition").when(colorDefinition).getId();
		doReturn(true).when(colorDefinition).isOverridden();
		
		ThemeRegistry themeRegistry = spy(new ThemeRegistry());
		
		FontRegistry fontRegistry = mock(FontRegistry.class);
		
		ColorRegistry colorRegistry = mock(ColorRegistry.class);
		
		ThemesExtension themesExtension = mock(ThemesExtension.class);
		doReturn(Arrays.asList(fontDefinition, colorDefinition)).when(themesExtension).getDefinitions();		
		
		WorkbenchThemeChangedHandlerTestable handler = spy(new WorkbenchThemeChangedHandlerTestable());
		doReturn(stylingEngine).when(handler).getStylingEngine();
		doReturn(themeRegistry).when(handler).getThemeRegistry();
		doReturn(fontRegistry).when(handler).getFontRegistry();
		doReturn(colorRegistry).when(handler).getColorRegistry();
		doReturn(themesExtension).when(handler).createThemesExtension();
		
		
		//when
		handler.handleEvent(mock(Event.class));
		
		//then		
		verify(stylingEngine, times(3)).style(anyObject());
		verify(stylingEngine, times(1)).style(themesExtension);
		verify(stylingEngine, times(1)).style(fontDefinition);
		verify(stylingEngine, times(1)).style(colorDefinition);
		
		verify(fontRegistry, times(1)).put(eq("fontDefinition"), any(FontData[].class));
		assertEquals(1, themeRegistry.getFonts().length);
		verify(colorRegistry, times(1)).put(eq("colorDefinition"), any(RGB.class));
		assertEquals(1, themeRegistry.getColors().length);
	}
	
	public void testOverrideAndAddThemeDefinitions() throws Exception {
		//given
		IStylingEngine stylingEngine = mock(IStylingEngine.class);
		
		FontDefinition fontDefinition1 = mock(FontDefinition.class);
		doReturn("fontDefinition1").when(fontDefinition1).getId();
		doReturn(true).when(fontDefinition1).isOverridden();
		
		FontDefinition fontDefinition2 = mock(FontDefinition.class);
		doReturn("fontDefinition2").when(fontDefinition2).getId();
		doReturn(true).when(fontDefinition2).isOverridden();	
		
		ColorDefinition colorDefinition1 = mock(ColorDefinition.class);
		doReturn("colorDefinition1").when(colorDefinition1).getId();
		doReturn(true).when(colorDefinition1).isOverridden();
		
		ColorDefinition colorDefinition2 = mock(ColorDefinition.class);
		doReturn("colorDefinition2").when(colorDefinition2).getId();
		doReturn(true).when(colorDefinition2).isOverridden();
		
		ThemeRegistry themeRegistry = spy(new ThemeRegistry());
		doReturn(new FontDefinition[]{fontDefinition1, fontDefinition2}).when(themeRegistry).getFonts();
		doReturn(new ColorDefinition[] {colorDefinition1, colorDefinition2}).when(themeRegistry).getColors();
		
		FontRegistry fontRegistry = mock(FontRegistry.class);
		
		ColorRegistry colorRegistry = mock(ColorRegistry.class);
		
		ThemesExtension themesExtension = mock(ThemesExtension.class); 
		
		WorkbenchThemeChangedHandlerTestable handler = spy(new WorkbenchThemeChangedHandlerTestable());
		doReturn(stylingEngine).when(handler).getStylingEngine();
		doReturn(themeRegistry).when(handler).getThemeRegistry();
		doReturn(fontRegistry).when(handler).getFontRegistry();
		doReturn(colorRegistry).when(handler).getColorRegistry();
		doReturn(themesExtension).when(handler).createThemesExtension();
		
		
		//when
		handler.handleEvent(mock(Event.class));
		
		//then
		verify(stylingEngine, times(5)).style(anyObject());
		verify(stylingEngine, times(1)).style(themesExtension);
		verify(stylingEngine, times(1)).style(fontDefinition1);
		verify(stylingEngine, times(1)).style(fontDefinition2);
		verify(stylingEngine, times(1)).style(colorDefinition1);
		verify(stylingEngine, times(1)).style(colorDefinition2);
		
		verify(fontRegistry, times(2)).put(eq("fontDefinition1"), any(FontData[].class));
		verify(fontRegistry, times(1)).put(eq("fontDefinition1"), eq(EMPRY_FONT_DATA_VALUE));
		verify(fontRegistry, times(2)).put(eq("fontDefinition2"), any(FontData[].class));
		verify(fontRegistry, times(1)).put(eq("fontDefinition2"), eq(EMPRY_FONT_DATA_VALUE));
		verify(colorRegistry, times(2)).put(eq("colorDefinition1"), any(RGB.class));
		verify(colorRegistry, times(1)).put(eq("colorDefinition1"), eq(EMPTY_COLOR_VALUE));
		verify(colorRegistry, times(2)).put(eq("colorDefinition2"), any(RGB.class));
		verify(colorRegistry, times(1)).put(eq("colorDefinition2"), eq(EMPTY_COLOR_VALUE));
		
		verify(handler, times(1)).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(fontRegistry), eq(fontDefinition1), any(IPreferenceStore.class));
		verify(handler, times(1)).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(fontRegistry), eq(fontDefinition2), any(IPreferenceStore.class));
		verify(handler, times(1)).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(colorRegistry), eq(colorDefinition1), any(IPreferenceStore.class));
		verify(handler, times(1)).populateDefinition(any(ITheme.class), any(org.eclipse.ui.themes.ITheme.class), 
				eq(colorRegistry), eq(colorDefinition2), any(IPreferenceStore.class));
		
		verify(handler, times(1)).sendThemeRegistryRestyledEvent();
	}
	
	public static class WorkbenchThemeChangedHandlerTestable extends WorkbenchThemeChangedHandler {
		@Override
		public IStylingEngine getStylingEngine() {
			return super.getStylingEngine();
		}

		@Override
		public ThemeRegistry getThemeRegistry() {
			return super.getThemeRegistry();
		}

		@Override
		public FontRegistry getFontRegistry() {
			return super.getFontRegistry();
		}

		@Override
		public ColorRegistry getColorRegistry() {
			return super.getColorRegistry();
		}
		
		@Override
		public ThemesExtension createThemesExtension() {
			return super.createThemesExtension();
		}
		
		@Override
		public void sendThemeRegistryRestyledEvent() {
		}
		
		@Override
		public ITheme getTheme(Event event) {
			return null;
		}
		
		@Override
		public org.eclipse.ui.themes.ITheme getColorsAndFontsTheme() {
			return null;
		}
		
		@Override
		public void populateDefinition(ITheme cssTheme,
				org.eclipse.ui.themes.ITheme theme, ColorRegistry registry, ColorDefinition definition,
				IPreferenceStore store) {			
		}
		
		@Override
		protected void populateDefinition(ITheme cssTheme,
				org.eclipse.ui.themes.ITheme theme, FontRegistry registry, FontDefinition definition,
				IPreferenceStore store) {			
		}
	}
}
