/*******************************************************************************
 * Copyright (c) 2009, 2013 Fair Issac Corp and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides 
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * A label provider that keeps track of queries to its methods.
 *
 */
public class TrackingLabelProvider extends LabelProvider implements
ICommonLabelProvider, IDescriptionProvider, IColorProvider,
IFontProvider, IStyledLabelProvider {
	
	private static final boolean PRINT_DEBUG_INFO = false;
	public static Color BG_COLOR = Display.getCurrent().getSystemColor(
			SWT.COLOR_GRAY);
	public static Color FG_COLOR = Display.getCurrent().getSystemColor(
			SWT.COLOR_BLACK);
	public static Font FONT = new Font(Display.getDefault(), new FontData());
	
	public final static Map 
		descriptionQueries = new HashMap(),
		backgroundQueries = new HashMap(),
		foregroundQueries = new HashMap(),
		fontQueries = new HashMap(),
		styledTextQueries = new HashMap(),
		textQueries = new HashMap(),
		imageQueries = new HashMap();
			
	private String _id;

	@Override
	public void init(ICommonContentExtensionSite config) {
		_id = config.getExtension().getId();
		int i = _id.lastIndexOf('.');
		if (i >= 0) {
			_id = _id.substring(i+1);
		}
	}
	
	@Override
	public Image getImage(Object element) {
		_track(imageQueries, element);
		return null;
	}

	private void _track(Map map, Object element) {
		String entry = (String) map.get(element);
		StringBuffer builder = new StringBuffer(entry==null ? "" : entry);
		builder.append(_id);
		map.put(element, builder.toString());
		if (PRINT_DEBUG_INFO)
			System.out.println(_id + ": " + element + " map: " + map);
	}

	@Override
	public String getText(Object element) {
		_track(textQueries, element);
		return null;
	}

	@Override
	public String getDescription(Object element) {
		_track(descriptionQueries, element);
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		_track(backgroundQueries, element);
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		_track(foregroundQueries, element);
		return null;
	}

	@Override
	public Font getFont(Object element) {
		_track(fontQueries, element);
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		_track(styledTextQueries, element);
		return null;
	}

	@Override
	public void restoreState(IMemento aMemento) {}
	@Override
	public void saveState(IMemento aMemento) {}

	public static void resetQueries() {
		if (PRINT_DEBUG_INFO)
			System.out.println("resetQueries()");
		descriptionQueries.clear();
		backgroundQueries.clear();
		foregroundQueries.clear();
		fontQueries.clear();
		styledTextQueries.clear();
		textQueries.clear();
		imageQueries.clear();
	}
}
