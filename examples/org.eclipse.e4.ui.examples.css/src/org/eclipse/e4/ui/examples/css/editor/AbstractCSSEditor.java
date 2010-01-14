/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.eclipse.e4.ui.css.core.serializers.CSSHTMLSerializerConfiguration;
import org.eclipse.e4.ui.css.core.serializers.CSSSerializer;
import org.eclipse.e4.ui.css.core.serializers.CSSSerializerConfiguration;
import org.w3c.dom.DOMException;

/**
 * Abstract CSS Editor.
 */
public abstract class AbstractCSSEditor {

	/**
	 * CSS Engine.
	 */
	protected CSSEngine engine;

	/**
	 * CSS Serializer
	 */
	protected CSSSerializer serializer;

	protected String nativeWidgetDir;

	protected java.util.List cssFiles = new ArrayList();

	protected AbstractCSSEditor(String nativeWidgetDir) {
		this.nativeWidgetDir = nativeWidgetDir;
	}

	/**
	 * Return CSS Engine configured.
	 * 
	 * @return
	 */
	protected CSSEngine getCSSEngine() {
		// FIXME: don't reset, just make a new engine, see bug 289251
//		if (engine == null) {
			// Get SWT CSS Engine
			engine = createCSSEngine();
			// Error
			engine.setErrorHandler(new CSSErrorHandler() {
				public void error(Exception e) {
					handleExceptions(e);
				}
			});

//		} else {
//			// Remove all style sheets.
//			engine.reset();
//		}

		if (isHTMLSelector()) {
			// Register HTML Element Provider to retrieve
			// w3c Element HTMLElement coming from Native Widget.
			engine.setElementProvider(getHTMLElementProvider());
		} else {
			// Register Native Widget (SWT, Swing...) Element Provider to
			// retrieve
			// w3c Element Element coming from Native Widget.
			engine.setElementProvider(getNativeWidgetElementProvider());
		}
		return engine;
	}

	/**
	 * Apply Styles comming from <code>getStyleSheetContent()</code> method to
	 * implement <code>widget</code>.
	 * 
	 * @param widget
	 */
	protected void applyStyles(Object widget) {
		try {
			Date d1 = new Date();
			engine = getCSSEngine();
			// 1. Parse Style Sheet coming from getStyleSheetContent().
			StringReader reader = new StringReader(getStyleSheetContent());
			engine.parseStyleSheet(reader);

			// 2. Apply styles
			engine.applyStyles(widget, true, true);
			Date d2 = new Date();

			// 3. Display time elapsed
			setCSSEngineStatuts("Apply style with "
					+ (d2.getTime() - d1.getTime()) + "ms.");

		} catch (Exception ex) {
			handleExceptions(ex);
		}
	}

	/**
	 * Apply styles to the fully window or left panel widget.
	 */
	protected void applyStyles() {
		if (mustApplyStylesToWindow()) {
			applyStyles(getWindowNativeWidget());
		} else
			applyStyles(getLeftPanelNativeWidget());
	}

	protected void fillTextareaWithStyleSheetContent(File file) {
		try {
			fillTextareaWithStyleSheetContent(new FileInputStream(file));
		} catch (Exception e) {
			handleExceptions(e);
		}
	}

	/**
	 * Fill the TextArea which store the style sheet content with the
	 * <code>stream</code> content.
	 * 
	 * @param stream
	 */
	protected void fillTextareaWithStyleSheetContent(InputStream stream) {
		try {
			StringWriter writer = new StringWriter();
			InputStreamReader streamReader = new InputStreamReader(stream);
			BufferedReader buffer = new BufferedReader(streamReader);
			String line = "";
			boolean b = false;
			while (null != (line = buffer.readLine())) {
				if (b)
					writer.write("\n");
				writer.write(line);
				b = true;
			}
			buffer.close();
			streamReader.close();
			String content = writer.toString();
			setStyleSheetContent(content);
		} catch (Exception e) {
			handleExceptions(e);
		}
	}

	protected void fillTextareaWithDefaultStyleSheetContent() {
		if (mustApplyStylesToWindow())
			fillTextareaWithDefaultStyleSheetContent(getWindowNativeWidget());
		else
			fillTextareaWithDefaultStyleSheetContent(getLeftPanelNativeWidget());
	}

	protected void fillTextareaWithDefaultStyleSheetContent(Object widget) {
		if (serializer == null)
			this.serializer = new CSSSerializer();
		StringWriter writer = new StringWriter();
		try {
			CSSSerializerConfiguration configuration = (isHTMLSelector() ? getCSSHTMLSerializerConfiguration()
					: getCSSNativeWidgetSerializerConfiguration());
			serializer.serialize(writer, getCSSEngine(), widget, true,
					configuration);
			setStyleSheetContent(writer.toString());
		} catch (Exception e) {
			handleExceptions(e);
		}
	}

	protected void applyStylesFromSelectedFile() {
		int index = getCSSFilesWidgetSelectionIndex();
		if (index == -1)
			if (getCSSFilesWidgetItemCount() > 1) {
				index = 1;
				selectCSSFilesWidget(index);
				// cssFilesWidget.select(index);
			}
		if (index < 1) {
			setStyleSheetContent("");
			return;
		}
		File file = (File) cssFiles.get(index - 1);
		fillTextareaWithStyleSheetContent(file);
	}

	protected void populateCSSFiles() {
		removeAllCSSFilesWidget();
		int size = cssFiles.size();
		for (int i = 0; i < size; i++) {
			cssFiles.remove(0);
		}
		addItemCSSFilesWidget("None");
		File baseDir = getBaseStyleDir();
		File[] files = baseDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isFile()) {
				addItemCSSFilesWidget(file.getName());
				cssFiles.add(file);
			}
		}
	}

	/**
	 * Display CSS Engine error
	 * 
	 * @param e
	 */
	protected void handleExceptions(Exception e) {
		//Don't bother reporting property errors since you get them on every keystroke
		//but the last when typing a property name
		if(isPropertyValueError(e))
			return;
		e.printStackTrace();
	}

	/**
	 * Return true if @e is a property value exception.
	 * These will happen as the user types, so ignore them since
	 * they often represent a word the user hasn't finished yet.
	 * @param e the exception to be tested
	 * @return true if @e is a property value exception
	 */
	protected boolean isPropertyValueError(Exception e) {
		if(! (e instanceof DOMExceptionImpl))
			return false;
		DOMExceptionImpl domE = (DOMExceptionImpl) e;
		return domE.code == DOMException.INVALID_ACCESS_ERR;
	}
	
	protected File getBaseStyleDir() {
		if (isHTMLSelector())
			return new File(getBaseStyleDirName() + "/html");
		if (nativeWidgetDir != null)
			return new File(getBaseStyleDirName() + "/" + nativeWidgetDir);
		return new File(getBaseStyleDirName());
	}

	protected String getBaseStyleDirName() {
		return "styles";
	}

	/**
	 * Create Instance of CSS Engine
	 * 
	 * @return
	 */
	protected abstract CSSEngine createCSSEngine();

	/**
	 * Return true if HTML selector must be used and false if Native Widget
	 * Selector must be used.
	 * 
	 * @return
	 */
	protected abstract boolean isHTMLSelector();

	/**
	 * Return Native Widget Element provider.
	 * 
	 * @return
	 */
	protected abstract IElementProvider getNativeWidgetElementProvider();

	/**
	 * Return HTML Element provider.
	 * 
	 * @return
	 */
	protected abstract IElementProvider getHTMLElementProvider();

	/**
	 * Return style sheet content.
	 * 
	 * @return
	 */
	protected abstract String getStyleSheetContent();

	/**
	 * Set style sheet content.
	 * 
	 * @param content
	 */
	protected abstract void setStyleSheetContent(String content);

	/**
	 * Set CSS Engine status.
	 * 
	 * @param status
	 */
	protected abstract void setCSSEngineStatuts(String status);

	/**
	 * Return true if Styles must be applied to the fully Window and false
	 * otherwise.
	 * 
	 * @return
	 */
	protected abstract boolean mustApplyStylesToWindow();

	/**
	 * Get Window Native Widget.
	 * 
	 * @return
	 */
	protected abstract Object getWindowNativeWidget();

	/**
	 * Get Left Panel Native widget.
	 * 
	 * @return
	 */
	protected abstract Object getLeftPanelNativeWidget();

	/**
	 * Return CSS Serializer configuration for Native Widget.
	 * 
	 * @return
	 */
	protected abstract CSSSerializerConfiguration getCSSNativeWidgetSerializerConfiguration();

	/**
	 * Return CSS Serializer configuration for HTML.
	 * 
	 * @return
	 */
	protected CSSSerializerConfiguration getCSSHTMLSerializerConfiguration() {
		return CSSHTMLSerializerConfiguration.INSTANCE;
	}

	/**
	 * Return selection index of CSS files widget.
	 * 
	 * @return
	 */
	protected abstract int getCSSFilesWidgetSelectionIndex();

	/**
	 * Return item count of CSS files widget.
	 * 
	 * @return
	 */
	protected abstract int getCSSFilesWidgetItemCount();

	/**
	 * Select item of CSS files widget at <code>index</code>.
	 * 
	 * @param index
	 */
	protected abstract void selectCSSFilesWidget(int index);

	/**
	 * Remove all items of CSS files widget.
	 */
	protected abstract void removeAllCSSFilesWidget();

	/**
	 * Add item of CSS files widget.
	 * 
	 * @param item
	 */
	protected abstract void addItemCSSFilesWidget(String item);

}
