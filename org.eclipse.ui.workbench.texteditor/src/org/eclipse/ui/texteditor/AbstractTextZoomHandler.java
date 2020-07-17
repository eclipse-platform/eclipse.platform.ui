/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 469918 Zoom In/Out
 *     John Taylor <johnpaultaylorii@gmail.com> - Bug 564099
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.swt.graphics.FontData;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
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
		List<String> fontsToSet = getAffectedFontNames(fontProperty, fontRegistry);
		for (String fontName : fontsToSet) {
			FontData[] currentFontData = null;
			String currentFontName = fontName;
			while (currentFontName != null && (currentFontData = fontRegistry.getFontData(currentFontName)) == null) {
				currentFontName = fgFontToDefault.get(currentFontName);
			}
			FontData[] newFontData = createFontDescriptor(currentFontData).getFontData();
			if (newFontData != null) {
				fontRegistry.put(fontName, newFontData);
			}
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
		return getActiveTextEditor(HandlerUtil.getActiveEditor(event));
	}

	private AbstractTextEditor getActiveTextEditor(IEditorPart part) {
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
	public void setEnabled(Object evaluationContext) {
		boolean enabled = false;
		if (evaluationContext instanceof IEvaluationContext) {
			Object activeEditor = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_EDITOR_NAME);
			if (activeEditor instanceof IEditorPart) {
				enabled = getActiveTextEditor((IEditorPart) activeEditor) != null;
			}
		}
		setBaseEnabled(enabled);
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
	private List<String> getAffectedFontNames(String referenceFontName, FontRegistry fontRegistry) {
		synchronized (AbstractTextZoomHandler.class) {
			if (fgFontToDefault == null) {
				// TODO: This should rely on ThemeRegistry and IThemeElementDefinition,
				// but those aren't visible at that time. So we're recreating the font hierarchy
				fgFontToDefault= new HashMap<>();
				fgDefaultToFonts= new HashMap<>();
				for (IConfigurationElement extension : Platform.getExtensionRegistry()
						.getConfigurationElementsFor("org.eclipse.ui.themes")) { //$NON-NLS-1$
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

		String currentFontName= referenceFontName;

		// identify "root" font to change
		String rootFontName = currentFontName;
		do {
			currentFontName= fgFontToDefault.get(currentFontName);
			if (currentFontName != null) {
				rootFontName= currentFontName;
			}
		} while (currentFontName != null);

		// propagate to "children" fonts
		Set<String> fontNames = new LinkedHashSet<>();
		Set<String> alreadyProcessed = new HashSet<>();
		Queue<String> fontsToProcess = new LinkedList<>();
		fontsToProcess.add(rootFontName);
		while (!fontsToProcess.isEmpty()) {
			currentFontName = fontsToProcess.poll();
			if (!alreadyProcessed.contains(currentFontName)) { // avoid infinite loop
				alreadyProcessed.add(currentFontName);
					if (fontRegistry.hasValueFor(currentFontName)) {
						fontNames.add(currentFontName);
					}
					Set<String> children= fgDefaultToFonts.get(currentFontName);
					if (children != null) {
						fontsToProcess.addAll(children);
					}
				}
		}

		// Order matters. Child fonts must be set before parent fonts to avoid
		// double impacts.
		List<String> result = new ArrayList<>(fontNames);
		Collections.reverse(result);
		return result;
	}

}
