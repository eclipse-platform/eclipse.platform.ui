/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.editor;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.e4.ui.css.core.css2.CSS2FontPropertiesHelpers;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.examples.css.Activator;
import org.eclipse.e4.ui.examples.css.editor.AbstractCSSEditor;
import org.eclipse.e4.ui.css.core.serializers.CSSSerializerConfiguration;
import org.eclipse.e4.ui.css.core.util.impl.resources.FileResourcesLocatorImpl;
import org.eclipse.e4.ui.css.core.util.impl.resources.OSGiResourceLocator;
import org.eclipse.e4.ui.css.swt.dom.SWTElementProvider;
import org.eclipse.e4.ui.css.swt.dom.html.SWTHTMLElementProvider;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.css.swt.serializers.CSSSWTSerializerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract CSS SWT Editor.
 */
public abstract class AbstractCSSSWTEditor extends AbstractCSSEditor {

	protected static final String[] CSS_FILE_EXTENSION = { "*.css" };

	private Display display;

	/**
	 * Textarea which contains content of CSS style sheet.
	 */
	private StyledText textArea;

	private Button cacheResourcesCheckbox;

	/**
	 * HTML checkbox to set if CSS must be typed with HTML syntaxe or SWT
	 * syntax.
	 */
	private Button htmlCheckbox;

	/**
	 * Label wich display time elapsed when CSS Engine apply styles.
	 */
	private Label statusLabel;

	/**
	 * Apply style when text area change
	 */
	private Button applyStyleWhenTextAreaChangeCheckbox;

	/**
	 * Launch Apply style
	 */
	private Button applyStyleToShellCheckbox;

	protected Shell shell;

	private Composite leftPanel;

	private List cssFilesWidget;

	private java.util.List cssFiles = new ArrayList();

	private Text selectedCSSPropertyNameText = null;

	private Text selectedCSSPropertyValueText = null;

	protected int currentLine = -1;

	protected AbstractCSSSWTEditor(String nativeWidgetDir,
			String[] styleFileExtension) {
		super(nativeWidgetDir);
	}

	protected AbstractCSSSWTEditor() {
		super("swt");
	}

	public void display() {

		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		shell.setLayout(layout);

		// create Menus
		createMenus(shell);

		/*
		 * Create a sash form.
		 */
		SashForm form = new SashForm(shell, SWT.NONE);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));

		/*
		 * Create left panel with SWT Contents.
		 */
		createLeftPanel(form);

		/*
		 * Create right panel with TextArea which contains CSS to load.
		 */
		createRightPanel(form);
		form.setWeights(new int[] { 30, 80 });

		/*
		 * Now we open the shell.
		 */
		shell.setSize(new Point(800, 600));
		shell.open();
		shell.setText("CSS Editors");
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/*--------------------  Menus  --------------------**/

	/**
	 * Create Menus File, Options
	 * 
	 * @param shell
	 */
	protected void createMenus(Shell shell) {
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		// Create File item Menu
		createMenuItemFile(menu, shell);
		// Create Options item Menu
		// createMenuItemOptions(menu);
	}

	/**
	 * Create Menu item File with sub menu New and Open
	 * 
	 * @param menu
	 * @param shell
	 */
	protected void createMenuItemFile(Menu menu, final Shell shell) {
		MenuItem menuFileHeader = new MenuItem(menu, SWT.CASCADE);
		menuFileHeader.setText("&File");
		// File menu
		Menu menuFile = new Menu(shell, SWT.DROP_DOWN);
		menuFileHeader.setMenu(menuFile);
		// New item menu
		MenuItem itemNew = new MenuItem(menuFile, SWT.PUSH);
		itemNew.setText("&New...");
		itemNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fillTextareaWithDefaultStyleSheetContent();
			}
		});
		// Open item menu
		MenuItem itemOpen = new MenuItem(menuFile, SWT.PUSH);
		itemOpen.setText("&Open...");
		itemOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.NULL);
				dialog.setFilterExtensions(CSS_FILE_EXTENSION);
				dialog.setText("Select CSS style file");
				dialog.setFilterPath(getBaseStyleDir().getAbsolutePath());
				String path = dialog.open();
				if (path != null) {
					File file = new File(path);
					fillTextareaWithStyleSheetContent(file);
				}
			}
		});

	}

	/**
	 * Create Menu item Options
	 * 
	 * @param menu
	 */
	protected void createMenuItemOptions(Menu menu) {
		MenuItem menuOptionsHeader = new MenuItem(menu, SWT.CASCADE);
		menuOptionsHeader.setText("&Options");
		// Options menu
		Menu menuOptions = new Menu(shell, SWT.DROP_DOWN);
		menuOptionsHeader.setMenu(menuOptions);
		// New item menu
		MenuItem itemNew = new MenuItem(menuOptions, SWT.CHECK);
		itemNew.setText("&Cache Resource...");

	}

	/*--------------------  Panel  --------------------**/

	protected void createLeftPanel(Composite parent) {
		leftPanel = new Composite(parent, SWT.NONE);
		leftPanel.setLayout(new FillLayout());
		createContent(leftPanel);
	}

	protected void createRightPanel(Composite parent) {

		SashForm form = new SashForm(parent, SWT.NONE);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		Composite composite = new Composite(form, SWT.NONE);
		composite.setLayout(layout);

		statusLabel = new Label(composite, SWT.NONE);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create TextArea which contains CSS content.
		textArea = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER);
		textArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		textArea.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (applyStyleWhenTextAreaChangeCheckbox.getSelection())
					applyStyles();
			}
		});

		textArea.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				displaySelectedCSSProperty();
			}
		});
		textArea.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				displaySelectedCSSProperty();
			}
		});

		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 3;
		composite = new Composite(form, SWT.NONE);
		composite.setLayout(layout);

		Group group = new Group(composite, SWT.NONE);
		group.setText("Selected CSS Property");
		layout = new GridLayout(2, true);
		layout.verticalSpacing = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText("CSS Property name");
		selectedCSSPropertyNameText = new Text(group, SWT.BORDER
				| SWT.READ_ONLY);
		selectedCSSPropertyNameText.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		label = new Label(group, SWT.NONE);
		label.setText("CSS Property value");
		selectedCSSPropertyValueText = new Text(group, SWT.BORDER);
		selectedCSSPropertyValueText.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		selectedCSSPropertyValueText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {

				String property = selectedCSSPropertyNameText.getText();
				property += ":";
				property += selectedCSSPropertyValueText.getText();
				updateSelectedCSSPropertyValue(property);
			}
		});

		final Button b = new Button(group, SWT.PUSH | SWT.BORDER);
		b.setText("Change Color");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog colorDialog = new ColorDialog(shell);
				colorDialog.setText("ColorDialog Demo");
				String cssValue = selectedCSSPropertyValueText.getText();
				if (cssValue != null && cssValue.length() > 0) {
					try {
						CSSValue value = engine.parsePropertyValue(cssValue);
						RGB rgb = (RGB) engine.convert(value, RGB.class, null);
						if (rgb != null)
							colorDialog.setRGB(rgb);
					} catch (Exception ex) {
						handleExceptions(ex);
					}
					// engine.convert(, RGB.class,);
				}
				RGB newColor = colorDialog.open();
				if (newColor != null) {
					try {
						cssValue = engine.convert(newColor, RGB.class,
								selectedCSSPropertyNameText.getText());
						if (cssValue == null)
							return;
						selectedCSSPropertyValueText.setText(cssValue);
						String property = selectedCSSPropertyNameText.getText();
						property += ":";
						property += selectedCSSPropertyValueText.getText();
						updateSelectedCSSPropertyValue(property);
					} catch (Exception ex) {
						handleExceptions(ex);
					}
				}
			}
		});

		final Button b2 = new Button(group, SWT.PUSH | SWT.BORDER);
		b2.setText("Change Font");
		b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FontDialog fontDialog = new FontDialog(shell);
				fontDialog.setText("Choose the font");
				String value = selectedCSSPropertyValueText.getText();
				try {
					String propertyName = selectedCSSPropertyNameText.getText();
					CSSValue cssValue = engine.parsePropertyValue(value);
					CSS2FontProperties fontProperties = CSS2FontPropertiesHelpers
							.createCSS2FontProperties(cssValue, propertyName);
					FontData fontData = (FontData) engine.convert(fontProperties,
							FontData.class, display);
					if (fontData != null) {
						FontData[] fontDatas = { fontData };
						fontDialog.setFontList(fontDatas);
					}
				} catch (Exception ex) {
					handleExceptions(ex);
				}
				FontData newFontData = fontDialog.open();
				if (newFontData != null) {
					try {
						String cssValue = engine.convert(newFontData,
								FontData.class, selectedCSSPropertyNameText
										.getText());
						if (cssValue == null)
							return;
						selectedCSSPropertyValueText.setText(cssValue);
						String property = selectedCSSPropertyNameText.getText();
						property += ":";
						property += selectedCSSPropertyValueText.getText();
						updateSelectedCSSPropertyValue(property);
					} catch (Exception ex) {
						handleExceptions(ex);
					}
				}
			}
		});

		group = new Group(composite, SWT.NONE);
		group.setText("CSS Engine options");
		layout = new GridLayout();
		layout.verticalSpacing = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		cacheResourcesCheckbox = new Button(group, SWT.CHECK);
		cacheResourcesCheckbox.setText("Cache Color, Font and Cursor");
		cacheResourcesCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!cacheResourcesCheckbox.getSelection()) {
					engine.getResourcesRegistry().dispose();
				}
			}
		});
		cacheResourcesCheckbox.setSelection(true);

		applyStyleWhenTextAreaChangeCheckbox = new Button(group, SWT.CHECK);
		applyStyleWhenTextAreaChangeCheckbox
				.setText("Apply style when textarea change");
		applyStyleWhenTextAreaChangeCheckbox.setSelection(true);

		htmlCheckbox = new Button(group, SWT.CHECK);
		htmlCheckbox.setText("is HTML Selector? (otherwise it's SWT Selector)");
		htmlCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				populateCSSFiles();
				applyStylesFromSelectedFile();
			}
		});

		// Create CSS files list
		cssFilesWidget = new List(group, SWT.NONE);
		cssFilesWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cssFilesWidget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyStylesFromSelectedFile();
			}
		});
		populateCSSFiles();

		// Create Apply Style Button.
		applyStyleToShellCheckbox = new Button(group, SWT.CHECK);
		applyStyleToShellCheckbox.setText("Apply style to Shell?");
		applyStyleToShellCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (applyStyleToShellCheckbox.getSelection()) {
					applyStyles();
				} else {
					// Apply styles with NONE style to reset
					// styles applied into TextArea...
					engine = getCSSEngine();
					engine.applyStyles(shell, true);
					// Apply styles
					applyStyles();
				}

			}
		});

		Button applyStyleButton = new Button(group, SWT.BORDER);
		applyStyleButton.setText("Apply style");
		applyStyleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyStyles();
			}
		});

		form.setWeights(new int[] { 120, 90 });

		// Select the first CSS style file
		applyStylesFromSelectedFile();
	}

	public abstract void createContent(Composite parent);

	protected void displaySelectedCSSProperty() {
		selectedCSSPropertyNameText.setText("");
		selectedCSSPropertyValueText.setText("");
		int start = -1;
		int end = -1;

//		if (currentLine != -1)
//			textArea.setLineBackground(currentLine, 1, null);

		currentLine = textArea.getLineAtOffset(textArea.getCaretOffset());
		if (currentLine + 1 >= textArea.getLineCount())
			return;
		start = textArea.getOffsetAtLine(currentLine);
		end = textArea.getOffsetAtLine(currentLine + 1) - 2;
		if (start <= end) {
			String lineText = textArea.getText(start, end);
			lineText = lineText.trim();
			int index = lineText.indexOf(":");
			if (index > 0) {
				if (lineText.indexOf("{") != -1)
					return;
				String property = lineText.substring(0, index);
				String value = lineText.substring(index + 1, lineText.length());
				value = value.replaceAll(";", "");
				// Remove comment
				int commentIndex = value.indexOf("/*");
				if (commentIndex > 0)
					value = value.substring(0, commentIndex);
				selectedCSSPropertyNameText.setText(property);
				selectedCSSPropertyValueText.setText(value);

//				textArea.setLineBackground(currentLine, 1, display
//						.getSystemColor(SWT.COLOR_MAGENTA));
			}
		}
	}

	protected void updateSelectedCSSPropertyValue(String text) {
		if (currentLine != -1 && currentLine + 1 >= textArea.getLineCount())
			return;
		int start = textArea.getOffsetAtLine(currentLine);
		int end = textArea.getOffsetAtLine(currentLine + 1) - 2;

		String startLineText = textArea.getText(0, start);
		String endLineText = textArea.getText(end, textArea.getCharCount() - 1);
		if (!text.endsWith(";") && !endLineText.startsWith(";"))
			text += ";";
		if (endLineText.startsWith(":"))
			endLineText.substring(1, endLineText.length());
		String newContent = startLineText + text + endLineText;
		textArea.setText(newContent);
	}

	/*-------------------- AbstractCSSEditor methods implementation  --------------------**/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#createCSSEngine()
	 */
	protected CSSEngine createCSSEngine() {
		CSSEngine engine = new CSSSWTEngineImpl(shell.getDisplay());
		engine.getResourcesLocatorManager().registerResourceLocator(
				new FileResourcesLocatorImpl());
		return engine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getHTMLElementProvider()
	 */
	protected IElementProvider getHTMLElementProvider() {
		return SWTHTMLElementProvider.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getNativeWidgetElementProvider()
	 */
	protected IElementProvider getNativeWidgetElementProvider() {
		return SWTElementProvider.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#isHTMLSelector()
	 */
	protected boolean isHTMLSelector() {
		return htmlCheckbox.getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getStyleSheetContent()
	 */
	protected String getStyleSheetContent() {
		return textArea.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#setStyleSheetContent(java.lang.String)
	 */
	protected void setStyleSheetContent(String content) {
		textArea.setText(content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#setCSSEngineStatuts(java.lang.String)
	 */
	protected void setCSSEngineStatuts(String status) {
		statusLabel.setText(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#mustApplyStylesToWindow()
	 */
	protected boolean mustApplyStylesToWindow() {
		return applyStyleToShellCheckbox.getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getLeftPanelNativeWidget()
	 */
	protected Object getLeftPanelNativeWidget() {
		return leftPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getWindowNativeWidget()
	 */
	protected Object getWindowNativeWidget() {
		return shell;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getCSSNativeWidgetSerializerConfiguration()
	 */
	protected CSSSerializerConfiguration getCSSNativeWidgetSerializerConfiguration() {
		return CSSSWTSerializerConfiguration.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getCSSFileWidgetItemCount()
	 */
	protected int getCSSFilesWidgetItemCount() {
		return cssFilesWidget.getItemCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#getCSSFileWidgetSelectionIndex()
	 */
	protected int getCSSFilesWidgetSelectionIndex() {
		return cssFilesWidget.getSelectionIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#selectCSSFileWidget(int)
	 */
	protected void selectCSSFilesWidget(int index) {
		cssFilesWidget.select(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#addCSSFilesWidget(java.lang.String)
	 */
	protected void addItemCSSFilesWidget(String item) {
		cssFilesWidget.add(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.examples.csseditors.AbstractCSSEditor#removeAllCSSFilesWidget()
	 */
	protected void removeAllCSSFilesWidget() {
		cssFilesWidget.removeAll();
	}
}
