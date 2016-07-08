/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 469918 Zoom In/Out
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.FontData;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.AbstractMultiEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * Abstract handler to change the default font size on Text editors.
 */
abstract class AbstractTextZoomHandler extends AbstractHandler {

	private static Map<String, String> fgFontToDefault;

	private static Map<String, Set<String>> fgDefaultToFonts;

	private int fFontSizeOffset;

	/**
	 * Implementations of this class have to specify in the constructor how much the font would
	 * change when the handler is executed.
	 *
	 * @param fontSizeOffset how many points the default font will change. The offset can be
	 *            negative, to reduce font size (zoom out) or positive to increase font size (zoom
	 *            in)
	 */
	protected AbstractTextZoomHandler(int fontSizeOffset) {
		fFontSizeOffset= fontSizeOffset;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractTextEditor textEditor= getActiveTextEditor(event);
		if (textEditor == null) {
			return null;
		}
		FontRegistry fontRegistry= textEditor.getSite().getWorkbenchWindow().getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
		String fontProperty= textEditor.getSymbolicFontName();
		if (fontProperty == null) {
			fontProperty= JFaceResources.TEXT_FONT;
		}
		Set<String> fontsToSet= getAffectedFontNames(fontProperty, fontRegistry);
		FontData[] initialFontData= null;
		String currentFontName= fontProperty;
		while (currentFontName != null && (initialFontData= fontRegistry.getFontData(currentFontName)) == null) {
			currentFontName= fgFontToDefault.get(currentFontName);
		}

		FontData[] newFontData= createFontDescriptor(initialFontData).getFontData();
		if (newFontData != null) {
			fontsToSet.stream().forEach(fontName -> fontRegistry.put(fontName, newFontData));
		}
		return Status.OK_STATUS;
	}

	private FontDescriptor createFontDescriptor(FontData[] initialFontData) {
		int destFontSize= initialFontData[0].getHeight() + fFontSizeOffset;
		if (destFontSize <= 0) {
			return FontDescriptor.createFrom(initialFontData);
		}
		return FontDescriptor.createFrom(initialFontData).setHeight(destFontSize);
	}

	private AbstractTextEditor getActiveTextEditor(ExecutionEvent event) {
		IWorkbenchPart part= HandlerUtil.getActiveEditor(event);
		if (part instanceof AbstractTextEditor) {
			return (AbstractTextEditor)part;
		} else if ((part instanceof AbstractMultiEditor) && ((AbstractMultiEditor)part).getActiveEditor() instanceof AbstractTextEditor) {
			return (AbstractTextEditor)((AbstractMultiEditor)part).getActiveEditor();
		} else if ((part instanceof MultiPageEditorPart) && ((MultiPageEditorPart)part).getSelectedPage() instanceof AbstractTextEditor) {
			return (AbstractTextEditor)((MultiPageEditorPart)part).getSelectedPage();
		}
		return part != null ? part.getAdapter(AbstractTextEditor.class) : null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 *
	 * @param referenceFontName the font name on which change is initially requested
	 * @param fontRegistry the font registry
	 * @return the names of the fonts that should be modified together with the referenceFontName.
	 *         Those are parent fonts from which the reference font inherit, or children font that
	 *         are set to default or inherit from reference font or a common parent that is affected
	 *         too.
	 */
	private Set<String> getAffectedFontNames(String referenceFontName, FontRegistry fontRegistry) {
		synchronized (AbstractTextZoomHandler.class) {
			if (fgFontToDefault == null) {
				// TODO: This should rely on ThemeRegistry and IThemeElementDefinition,
				// but those aren't visible at that time. So we're recreating the font hierarchy
				fgFontToDefault= new HashMap<>();
				fgDefaultToFonts= new HashMap<>();
				IConfigurationElement[] themeElements= Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.themes"); //$NON-NLS-1$
				for (int i= 0; i < themeElements.length; i++) {
					IConfigurationElement extension= themeElements[i];
					if ("fontDefinition".equals(extension.getName())) { //$NON-NLS-1$
						String fontId= extension.getAttribute("id"); //$NON-NLS-1$
						String defaultsTo= extension.getAttribute("defaultsTo"); //$NON-NLS-1$
						if (defaultsTo != null) {
							fgFontToDefault.put(fontId, defaultsTo);
							if (!fgDefaultToFonts.containsKey(defaultsTo)) {
								fgDefaultToFonts.put(defaultsTo, new HashSet<>());
							}
							fgDefaultToFonts.get(defaultsTo).add(fontId);
						}

					}
				}
			}
		}
		Set<String> res= new HashSet<>();
		FontData[] referenceFontData= fontRegistry.getFontData(referenceFontName);
		if (fontRegistry.hasValueFor(referenceFontName)) {
			res.add(referenceFontName);
		}
		String currentFontName= referenceFontName;
		String rootFontName= referenceFontName;
		// identify "root" font to change
		do {
			currentFontName= fgFontToDefault.get(currentFontName);
			if (currentFontName != null && Arrays.equals(referenceFontData, fontRegistry.getFontData(currentFontName))) {
				rootFontName= currentFontName;
			}
		} while (currentFontName != null);
		LinkedList<String> fontsToProcess= new LinkedList<>();
		fontsToProcess.add(rootFontName);
		// propage to "children" fonts
		Set<String> alreadyProcessed= new HashSet<>();
		while (!fontsToProcess.isEmpty()) {
			currentFontName= fontsToProcess.get(0);
			fontsToProcess.remove(0);
			// with recent Java, use currentFOntName = fontsToProcess.poll instead of the 2 lines above
			if (!alreadyProcessed.contains(currentFontName)) { // avoid infinite loop
				alreadyProcessed.add(currentFontName);
				FontData[] currentFontData= fontRegistry.getFontData(currentFontName);
				if (currentFontData == null || Arrays.equals(referenceFontData, currentFontData)) {
					if (fontRegistry.hasValueFor(currentFontName)) {
						res.add(currentFontName);
					}
					Set<String> children= fgDefaultToFonts.get(currentFontName);
					if (children != null) {
						fontsToProcess.addAll(children);
					}
				}
			}
		}
		return res;
	}

}
