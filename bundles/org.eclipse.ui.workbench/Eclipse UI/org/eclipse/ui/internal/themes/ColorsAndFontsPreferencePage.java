/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cornel Izbasa <cizbasa@info.uvt.ro> - Bug 436247
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136, 472654
 *     Robert Roth <robert.roth.off@gmail.com> - Bugs 274005, 456291
 *     Mickael Istria (Red Hat Inc.) - Theme and fontregistry rather than pref
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static org.eclipse.ui.internal.themes.WorkbenchThemeManager.EMPTY_COLOR_VALUE;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.themes.IThemePreview;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Preference page for management of system colors, gradients and fonts.
 *
 * @since 3.0
 */
public final class ColorsAndFontsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String EMPTY = ""; //$NON-NLS-1$

	private static final String SPACE = " "; //$NON-NLS-1$

	private static final String SELECTED_ELEMENT_PREF = "ColorsAndFontsPreferencePage.selectedElement"; //$NON-NLS-1$
	/**
	 * The preference that stores the expanded state.
	 */
	private static final String EXPANDED_ELEMENTS_PREF = "ColorsAndFontsPreferencePage.expandedCategories"; //$NON-NLS-1$
	/**
	 * The token that separates expanded elements in EXPANDED_ELEMENTS_PREF.
	 */
	private static final String EXPANDED_ELEMENTS_TOKEN = "\t"; //$NON-NLS-1$

	/**
	 * Marks category tokens in EXPANDED_ELEMENTS_PREF and SELECTED_ELEMENT_PREF.
	 */
	private static final char MARKER_CATEGORY = 'T';

	/**
	 * Marks color tokens in EXPANDED_ELEMENTS_PREF and SELECTED_ELEMENT_PREF.
	 */
	private static final char MARKER_COLOR = 'C';

	/**
	 * Marks font tokens in EXPANDED_ELEMENTS_PREF and SELECTED_ELEMENT_PREF.
	 */
	private static final char MARKER_FONT = 'F';

	private static final int DEFINITION_NOT_AVAIL_COLOR = SWT.COLOR_WIDGET_LIGHT_SHADOW;

	private class ThemeContentProvider implements ITreeContentProvider {

		private IThemeRegistry registry;

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ThemeElementCategory) {
				String categoryId = ((ThemeElementCategory) parentElement).getId();
				Object[] defintions = categoryMap.get(categoryId);
				if (defintions == null) {
					defintions = getCategoryChildren(categoryId);
					categoryMap.put(categoryId, defintions);
				}
				return defintions;
			}

			ArrayList<IHierarchalThemeElementDefinition> list = new ArrayList<>();
			IHierarchalThemeElementDefinition def = (IHierarchalThemeElementDefinition) parentElement;
			String id = def.getId();
			IHierarchalThemeElementDefinition[] defs;
			if (def instanceof ColorDefinition) {
				defs = registry.getColors();
			} else {
				defs = registry.getFonts();
			}

			for (IHierarchalThemeElementDefinition elementDefinition : defs) {
				if (id.equals(elementDefinition.getDefaultsTo()) && ColorsAndFontsPreferencePage.equals(
						((ICategorizedThemeElementDefinition) def).getCategoryId(),
						((ICategorizedThemeElementDefinition) elementDefinition).getCategoryId())) {
					list.add(elementDefinition);
				}
			}
			return list.toArray();
		}

		private Object[] getCategoryChildren(String categoryId) {
			ArrayList<IThemeElementDefinition> list = new ArrayList<>();

			if (categoryId != null) {
				for (ThemeElementCategory category : registry.getCategories()) {
					if (categoryId.equals(category.getParentId())) {
						Set<?> bindings = themeRegistry.getPresentationsBindingsFor(category);
						if (bindings == null) {
							list.add(category);
						}
					}
				}
			}
			ColorDefinition[] colorDefinitions = themeRegistry.getColorsFor(currentTheme.getId());
			for (ColorDefinition colorDefinition : colorDefinitions) {
				if (!colorDefinition.isEditable()) {
					continue;
				}
				String catId = colorDefinition.getCategoryId();
				if ((catId == null && categoryId == null)
						|| (catId != null && categoryId != null && categoryId.equals(catId))) {
					if (colorDefinition.getDefaultsTo() != null && parentIsInSameCategory(colorDefinition)) {
						continue;
					}
					list.add(colorDefinition);
				}
			}
			FontDefinition[] fontDefinitions = themeRegistry.getFontsFor(currentTheme.getId());
			for (FontDefinition fontDefinition : fontDefinitions) {
				if (!fontDefinition.isEditable()) {
					continue;
				}
				String catId = fontDefinition.getCategoryId();
				if ((catId == null && categoryId == null)
						|| (catId != null && categoryId != null && categoryId.equals(catId))) {
					if (fontDefinition.getDefaultsTo() != null && parentIsInSameCategory(fontDefinition)) {
						continue;
					}
					list.add(fontDefinition);
				}
			}
			return list.toArray(new Object[list.size()]);
		}

		private boolean parentIsInSameCategory(ColorDefinition definition) {
			String defaultsTo = definition.getDefaultsTo();
			for (ColorDefinition colorDef : registry.getColors()) {
				if (colorDef.getId().equals(defaultsTo)
						&& ColorsAndFontsPreferencePage.equals(colorDef.getCategoryId(), definition.getCategoryId())) {
					return true;
				}
			}
			return false;
		}

		private boolean parentIsInSameCategory(FontDefinition definition) {
			String defaultsTo = definition.getDefaultsTo();
			for (FontDefinition fontDef : registry.getFonts()) {
				if (fontDef.getId().equals(defaultsTo)
						&& ColorsAndFontsPreferencePage.equals(fontDef.getCategoryId(), definition.getCategoryId())) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof ThemeElementCategory)
				return registry;

			if (element instanceof ColorDefinition) {
				String defaultId = ((IHierarchalThemeElementDefinition) element).getDefaultsTo();
				if (defaultId != null) {
					ColorDefinition defaultElement = registry.findColor(defaultId);
					if (parentIsInSameCategory(defaultElement))
						return defaultElement;
				}
				String categoryId = ((ColorDefinition) element).getCategoryId();
				return registry.findCategory(categoryId);
			}

			if (element instanceof FontDefinition) {
				String defaultId = ((FontDefinition) element).getDefaultsTo();
				if (defaultId != null) {
					FontDefinition defaultElement = registry.findFont(defaultId);
					if (parentIsInSameCategory(defaultElement))
						return defaultElement;
				}
				String categoryId = ((FontDefinition) element).getCategoryId();
				return registry.findCategory(categoryId);
			}

			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ThemeElementCategory) {
				return true;
			}

			IHierarchalThemeElementDefinition def = (IHierarchalThemeElementDefinition) element;
			String id = def.getId();
			IHierarchalThemeElementDefinition[] defs;
			if (def instanceof ColorDefinition) {
				defs = registry.getColors();
			} else {
				defs = registry.getFonts();
			}

			for (IHierarchalThemeElementDefinition elementDefinition : defs) {
				if (id.equals(elementDefinition.getDefaultsTo()) && ColorsAndFontsPreferencePage.equals(
						((ICategorizedThemeElementDefinition) def).getCategoryId(),
						((ICategorizedThemeElementDefinition) elementDefinition).getCategoryId())) {
					return true;
				}
			}

			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			ArrayList<Object> list = new ArrayList<>();
			Object[] uncatChildren = getCategoryChildren(null);
			list.addAll(Arrays.asList(uncatChildren));
			for (ThemeElementCategory category : ((IThemeRegistry) inputElement).getCategories()) {
				if (category.getParentId() == null) {
					Set<?> bindings = themeRegistry.getPresentationsBindingsFor(category);
					if (bindings == null) {
						Object[] children = getChildren(category);
						if (children != null && children.length > 0) {
							list.add(category);
						}
					}
				}
			}
			return list.toArray(new Object[list.size()]);
		}

		@Override
		public void dispose() {
			categoryMap.clear();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			categoryMap.clear();
			registry = (IThemeRegistry) newInput;
		}

	}

	private class PresentationLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {

		private HashMap<Font, Font> fonts = new HashMap<>();

		private HashMap<Color, Image> images = new HashMap<>();

		private int imageSize = -1;

		private int usableImageSize = -1;

		private IPropertyChangeListener listener = event -> {
			if (event.getNewValue() != null) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(PresentationLabelProvider.this));
			} else {
				// Some theme definition element has been modified and we
				// need to refresh the viewer
				refreshAllLabels();
			}
		};

		private Image emptyImage;

		public PresentationLabelProvider() {
			hookListeners();
		}

		/**
		 * Hook the listeners onto the various registries.
		 */
		public void hookListeners() {
			colorRegistry.addListener(listener);
			fontRegistry.addListener(listener);
		}

		@Override
		public void dispose() {
			super.dispose();
			colorRegistry.removeListener(listener);
			fontRegistry.removeListener(listener);
			for (Image image : images.values()) {
				image.dispose();
			}
			images.clear();

			if (emptyImage != null) {
				emptyImage.dispose();
				emptyImage = null;
			}

			// clear the fonts.
			clearFontCache();
		}

		/**
		 * Clears and disposes all fonts.
		 */
		public void clearFontCache() {
			for (Font font : fonts.values()) {
				font.dispose();
			}
			fonts.clear();
		}

		/**
		 * Clears and disposes all fonts and fires a label update.
		 */
		public void clearFontCacheAndUpdate() {
			clearFontCache();
			fireLabelProviderChanged(new LabelProviderChangedEvent(PresentationLabelProvider.this));
		}

		@Override
		public Font getFont(Object element) {
			Display display = tree.getDisplay();
			if (element instanceof FontDefinition) {
				int parentHeight = tree.getViewer().getControl().getFont().getFontData()[0].getHeight();
				Font baseFont = fontRegistry.get(((FontDefinition) element).getId());
				Font font = fonts.get(baseFont);
				if (font == null) {
					FontData[] data = baseFont.getFontData();
					for (FontData fontData : data) {
						fontData.setHeight(parentHeight);
					}
					font = new Font(display, data);

					fonts.put(baseFont, font);
				}
				return font;
			}
			return JFaceResources.getDialogFont();
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof ColorDefinition) {
				Display display = tree.getDisplay();
				Color c = colorRegistry.get(((ColorDefinition) element).getId());
				Color foregroundColor = display.getSystemColor(SWT.COLOR_BLACK);
				if (!isAvailableInCurrentTheme((ColorDefinition) element)) {
					c = display.getSystemColor(SWT.COLOR_WHITE);
					foregroundColor = display.getSystemColor(DEFINITION_NOT_AVAIL_COLOR);
				}
				Image image = images.get(c);
				if (image == null) {
					ensureImageSize();
					image = new Image(display, imageSize, imageSize);

					GC gc = new GC(image);
					gc.setBackground(tree.getViewer().getControl().getBackground());
					gc.setForeground(tree.getViewer().getControl().getBackground());
					gc.drawRectangle(0, 0, imageSize - 1, imageSize - 1);

					gc.setForeground(foregroundColor);
					gc.setBackground(c);

					int offset = (imageSize - usableImageSize) / 2;
					gc.drawRectangle(offset, offset, usableImageSize - offset, usableImageSize - offset);
					gc.fillRectangle(offset + 1, offset + 1, usableImageSize - offset - 1,
							usableImageSize - offset - 1);
					gc.dispose();

					images.put(c, image);
				}
				return image;

			} else if (element instanceof FontDefinition) {
				return workbench.getSharedImages().getImage(IWorkbenchGraphicConstants.IMG_OBJ_FONT);
			} else {
				return workbench.getSharedImages().getImage(IWorkbenchGraphicConstants.IMG_OBJ_THEME_CATEGORY);
			}
		}

		private void ensureImageSize() {
			if (imageSize == -1) {
				imageSize = tree.getViewer().getTree().getItemHeight();
				usableImageSize = Math.max(1, imageSize - 4);
			}
		}

		@Override
		public String getText(Object element) {
			if (element instanceof IHierarchalThemeElementDefinition) {
				IHierarchalThemeElementDefinition themeElement = (IHierarchalThemeElementDefinition) element;
				if (themeElement.getDefaultsTo() != null) {
					String myCategory = ((ICategorizedThemeElementDefinition) themeElement).getCategoryId();
					ICategorizedThemeElementDefinition def;
					if (element instanceof ColorDefinition) {
						def = themeRegistry.findColor(themeElement.getDefaultsTo());
					} else {
						def = themeRegistry.findFont(themeElement.getDefaultsTo());
					}
					if (def != null && !ColorsAndFontsPreferencePage.equals(def.getCategoryId(), myCategory)) {
						if (isDefault(themeElement)) {
							return MessageFormat.format(RESOURCE_BUNDLE.getString("defaultFormat_default"), //$NON-NLS-1$
									themeElement.getName(), def.getName());
						}
						return MessageFormat.format(RESOURCE_BUNDLE.getString("defaultFormat_override"), //$NON-NLS-1$
								themeElement.getName(), def.getName());
					}
				}
			}
			return ((IThemeElementDefinition) element).getName();
		}

		/**
		 * Return whether the element is set to default.
		 *
		 * @param def the definition
		 * @return whether the element is set to default
		 * @since 3.2
		 */
		private boolean isDefault(IThemeElementDefinition def) {
			if (def instanceof FontDefinition) {
				FontDefinition fontDef = (FontDefinition) def;
				String defaultFontID = fontDef.getDefaultsTo();
				return defaultFontID != null && Arrays.equals(fontRegistry.getFontData(def.getId()),
						fontRegistry.getFontData(defaultFontID));
			}
			if (def instanceof ColorDefinition) {
				ColorDefinition colorDef = (ColorDefinition) def;
				String defaultColorID = colorDef.getDefaultsTo();
				if (defaultColorID == null)
					return false;
				RGB defaultRGB = colorRegistry.getRGB(defaultColorID);
				return defaultRGB != null && defaultRGB.equals(colorRegistry.getRGB(colorDef.getId()));
			}
			return false;
		}

		/**
		 * Returns the DEFINITION_NOT_AVAIL_COLOR color when definition is not present
		 * in the current theme or null when it is available
		 *
		 * @return the DEFINITION_NOT_AVAIL_COLOR color or null
		 */
		@Override
		public Color getForeground(Object element) {
			if (element instanceof ThemeElementDefinition
					&& !isAvailableInCurrentTheme((ThemeElementDefinition) element)) {
				return tree.getDisplay().getSystemColor(DEFINITION_NOT_AVAIL_COLOR);
			}
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}
	}

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(ColorsAndFontsPreferencePage.class.getName());

	/**
	 * Map to precalculate category color lists.
	 */
	private Map<String, Object[]> categoryMap = new HashMap<>(7);

	private Font appliedDialogFont;

	/**
	 * Map of definition ColorDefinition-&gt;RGB capturing the explicit changes made by
	 * the user. These changes need to be stored into the preference store.
	 */
	private Map<ColorDefinition, RGB> colorPreferencesToSet = new HashMap<>(7);

	private CascadingColorRegistry colorRegistry;

	/**
	 * Map of definition id-&gt;RGB capturing the temporary changes caused by a
	 * 'defaultsTo' color change.
	 */
	private Map<String, RGB> colorValuesToSet = new HashMap<>(7);

	/**
	 * The default color preview composite.
	 */
	private Composite defaultColorPreview;

	/**
	 * The default font preview composite.
	 */
	private Composite defaultFontPreview;

	/**
	 * The composite to use when no preview is available.
	 */
	private Composite defaultNoPreview;

	/**
	 * Currently selected font for preview; might be null.
	 */
	private Font currentFont;

	/**
	 * Currently selected color for preview; might be null.
	 */
	private Color currentColor;

	/**
	 * Canvas used to draw default color preview
	 */
	private Canvas colorSampler;

	/**
	 * Canvas used to draw default font preview
	 */
	private Canvas fontSampler;

	private String fontSampleText;

	private List<Control> dialogFontWidgets = new ArrayList<>();

	private Button fontChangeButton;

	/**
	 * The button to edit the default of the selected element.
	 *
	 * @since 3.7
	 */
	private Button editDefaultButton;

	/**
	 * The button to go to the default of the selected element.
	 *
	 * @since 3.7
	 */
	private Button goToDefaultButton;

	/**
	 * The button to expand the tree.
	 *
	 * @since 4.5
	 */
	private Button expandAllButton;

	/**
	 * Map of definition FontDefinition-&gt;FontData[] capturing the changes explicitly
	 * made by the user. These changes need to be stored into the preference store.
	 */
	private Map<FontDefinition, FontData[]> fontPreferencesToSet = new HashMap<>(7);

	private CascadingFontRegistry fontRegistry;

	private Button fontResetButton;

	private Button fontSystemButton;

	/**
	 * Map of definition id-&gt;FontData[] capturing the temporary changes caused by a
	 * 'defaultsTo' font change.
	 *
	 * @deprecated in this page, we should only care about preferences, preference
	 *             to fontValues synchronization in registry is handled in the
	 *             ThemeAPI and listeners
	 */
	@Deprecated
	private Map<String, FontData[]> fontValuesToSet = new HashMap<>(7);

	/**
	 * The composite that is parent to all previews.
	 */
	private Composite previewComposite;

	/**
	 * A mapping from PresentationCategory-&gt;Composite for the created previews.
	 */
	private Map<ThemeElementCategory, Composite> previewMap = new HashMap<>(7);

	/**
	 * Set containing all IPresentationPreviews created.
	 */
	private Set<IThemePreview> previewSet = new HashSet<>(7);

	/**
	 * The layout for the previewComposite.
	 */
	private StackLayout stackLayout;

	private final IThemeRegistry themeRegistry;

	private ITheme currentTheme;

	private PresentationLabelProvider labelProvider;

	private CascadingThemeExt cascadingTheme;

	private IPropertyChangeListener themeChangeListener;

	private Workbench workbench;

	private FilteredTree tree;

	private Text descriptionText;

	private IEventBroker eventBroker;

	private EventHandler themeRegistryRestyledHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			if (isAnyThemeChanged()) {
				updateThemeInfo(workbench.getThemeManager());
			}
			refreshCategory();
			refreshAllLabels();

			clearPreviews();
			previewMap.clear();
			restoreTreeSelection();
			updateControls();
		}

		private boolean isAnyThemeChanged() {
			return currentTheme != workbench.getThemeManager().getCurrentTheme() || currentCSSTheme != getActiveTheme();
		}
	};

	private org.eclipse.e4.ui.css.swt.theme.ITheme currentCSSTheme;

	private IThemeEngine themeEngine;

	/**
	 * Create a new instance of the receiver.
	 */
	public ColorsAndFontsPreferencePage() {
		themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
		// no-op
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Everything else except the following string patterns is ignored:
	 * </p>
	 * <ul>
	 * <li><strong>selectCategory:</strong>ID - selects and expands the category
	 * with the given ID</li>
	 * <li><strong>selectFont:</strong>ID - selects the font with the given ID</li>
	 * <li><strong>selectColor:</strong>ID - selects the color with the given
	 * ID</li>
	 * </ul>
	 *
	 * @param data the data to be applied
	 */
	@Override
	public void applyData(Object data) {
		if (tree == null || !(data instanceof String))
			return;

		ThemeRegistry themeRegistry = (ThemeRegistry) tree.getViewer().getInput();
		String command = (String) data;
		if (command.startsWith("selectCategory:")) { //$NON-NLS-1$
			String categoryId = command.substring(15);
			ThemeElementCategory category = themeRegistry.findCategory(categoryId);
			if (category != null) {
				selectAndReveal(category);
				tree.getViewer().expandToLevel(category, 1);
			}
		} else if (command.startsWith("selectFont:")) { //$NON-NLS-1$
			String id = command.substring(11);
			FontDefinition fontDef = themeRegistry.findFont(id);
			if (fontDef != null) {
				selectAndReveal(fontDef);
			}
		} else if (command.startsWith("selectColor:")) { //$NON-NLS-1$
			String id = command.substring(12);
			ColorDefinition colorDef = themeRegistry.findColor(id);
			if (colorDef != null) {
				selectAndReveal(colorDef);
			}
		}
	}

	/**
	 * Selects and reveals the given element.
	 *
	 * @param selection the object to select and reveal
	 * @since 3.7
	 */
	private void selectAndReveal(Object selection) {
		TreeViewer viewer = tree.getViewer();
		viewer.setSelection(new StructuredSelection(selection), false);
		viewer.reveal(selection);
		viewer.getTree().setFocus();
	}

	private static boolean equals(String string, String string2) {
		if ((string == null && string2 == null))
			return true;
		if (string == null || string2 == null)
			return false;
		if (string.equals(string2))
			return true;
		return false;
	}

	/**
	 * Get the active theme.
	 *
	 * @return the active theme if there is one; <code>null</code> if there isn't or
	 *         {@link #themeEngine} is <code>null</code>.
	 */
	private org.eclipse.e4.ui.css.swt.theme.ITheme getActiveTheme() {
		if (themeEngine != null) {
			return themeEngine.getActiveTheme();
		}
		return null;
	}

	/**
	 * Create a button for the preference page.
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
		button.setText(label);
		myApplyDialogFont(button);
		setButtonLayoutData(button);
		button.setEnabled(false);
		return button;
	}

	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		separator.setFont(parent.getFont());
		separator.setVisible(false);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.FONTS_PREFERENCE_PAGE);

		parent.addDisposeListener(e -> {
			if (appliedDialogFont != null)
				appliedDialogFont.dispose();
		});

		final SashForm advancedComposite = new SashForm(parent, SWT.VERTICAL);
		GridData sashData = new GridData(SWT.FILL, SWT.FILL, true, true);
		advancedComposite.setLayoutData(sashData);

		Composite mainColumn = new Composite(advancedComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		mainColumn.setFont(parent.getFont());
		mainColumn.setLayout(layout);

		GridData data = new GridData(GridData.BEGINNING);
		data.horizontalSpan = 2;
		Label label = new Label(mainColumn, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("colorsAndFonts")); //$NON-NLS-1$
		myApplyDialogFont(label);
		label.setLayoutData(data);

		createTree(mainColumn);

		// --- buttons
		Composite controlColumn = new Composite(mainColumn, SWT.NONE);
		data = new GridData(GridData.FILL_VERTICAL);
		controlColumn.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		controlColumn.setLayout(layout);

		// we need placeholder to offset the filter control of the table
		Label placeholder = new Label(controlColumn, SWT.NONE);
		GridData placeholderData = new GridData(SWT.TOP);
		placeholderData.heightHint = convertVerticalDLUsToPixels(12);
		placeholder.setLayoutData(placeholderData);

		fontChangeButton = createButton(controlColumn, RESOURCE_BUNDLE.getString("openChange")); //$NON-NLS-1$
		fontSystemButton = createButton(controlColumn, WorkbenchMessages.FontsPreference_useSystemFont);
		fontResetButton = createButton(controlColumn, RESOURCE_BUNDLE.getString("reset")); //$NON-NLS-1$
		createSeparator(controlColumn);
		editDefaultButton = createButton(controlColumn, RESOURCE_BUNDLE.getString("editDefault")); //$NON-NLS-1$
		goToDefaultButton = createButton(controlColumn, RESOURCE_BUNDLE.getString("goToDefault")); //$NON-NLS-1$
		createSeparator(controlColumn);
		expandAllButton = createButton(controlColumn, RESOURCE_BUNDLE.getString("expandAll")); //$NON-NLS-1$
		expandAllButton.setEnabled(true);
		// --- end of buttons

		createDescriptionControl(mainColumn);

		Composite previewColumn = new Composite(advancedComposite, SWT.NONE);
		GridLayout previewLayout = new GridLayout();
		previewLayout.marginTop = 7;
		previewLayout.marginWidth = 0;
		previewLayout.marginHeight = 0;
		previewColumn.setFont(parent.getFont());
		previewColumn.setLayout(previewLayout);

		// --- create preview control
		Composite composite = new Composite(previewColumn, SWT.NONE);

		GridData data2 = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data2);
		GridLayout layout2 = new GridLayout(1, true);
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		composite.setLayout(layout2);

		Label label2 = new Label(composite, SWT.LEFT);
		label2.setText(RESOURCE_BUNDLE.getString("preview")); //$NON-NLS-1$
		myApplyDialogFont(label2);

		previewComposite = new Composite(composite, SWT.NONE);
		previewComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		stackLayout = new StackLayout();
		stackLayout.marginHeight = 0;
		stackLayout.marginWidth = 0;
		previewComposite.setLayout(stackLayout);
		// -- end of preview control

		defaultFontPreview = createFontPreviewControl();
		defaultColorPreview = createColorPreviewControl();
		defaultNoPreview = createNoPreviewControl();

		hookListeners();

		updateTreeSelection(tree.getViewer().getSelection());

		advancedComposite.setWeights(new int[] { 75, 25 });
		return advancedComposite;
	}

	/**
	 * Create the <code>ListViewer</code> that will contain all color definitions as
	 * defined in the extension point.
	 *
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createTree(Composite parent) {
		labelProvider = new PresentationLabelProvider();

		// Create a custom pattern matcher that will allow
		// non-category elements to be returned in the event that their children
		// do not and also search the descriptions.
		PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(Viewer viewer, Object element) {
				if (super.isLeafMatch(viewer, element))
					return true;
				return wordMatches(getText(element));
			}

			private String getText(Object element) {
				StringBuilder text = new StringBuilder(EMPTY);
				if (element instanceof ICategorizedThemeElementDefinition) {
					text.append(((ICategorizedThemeElementDefinition) element).getDescription());
				}
				if (element instanceof FontDefinition) {
					Font font = fontRegistry.get(((FontDefinition) element).getId());
					if (font != null) {
						for (FontData data : font.getFontData()) {
							text.append(SPACE);
							text.append(data.getName() + SPACE);
							text.append(data.getHeight() + SPACE);
							text.append(data.getStyle() == SWT.NORMAL ? RESOURCE_BUNDLE.getString("normalFont") + SPACE //$NON-NLS-1$
									: EMPTY);
							text.append((data.getStyle() & SWT.BOLD) == SWT.BOLD
									? RESOURCE_BUNDLE.getString("boldFont") + SPACE //$NON-NLS-1$
									: EMPTY);
							text.append((data.getStyle() & SWT.ITALIC) == SWT.ITALIC
									? RESOURCE_BUNDLE.getString("italicFont") + SPACE //$NON-NLS-1$
									: EMPTY);
							break;
						}
					}
				}
				return text.toString();
			}
		};
		filter.setIncludeLeadingWildcard(true);

		tree = new FilteredTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);
		tree.setQuickSelectionMode(true);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = Math.max(285, convertWidthInCharsToPixels(30));
		data.heightHint = Math.max(175, convertHeightInCharsToPixels(10));
		tree.setLayoutData(data);
		myApplyDialogFont(tree.getViewer().getControl());
		Text filterText = tree.getFilterControl();
		if (filterText != null)
			myApplyDialogFont(filterText);

		tree.getViewer().setLabelProvider(labelProvider);
		tree.getViewer().setContentProvider(new ThemeContentProvider());
		tree.getViewer().setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				if (element instanceof ThemeElementCategory)
					return 0;
				return 1;
			}
		});
		tree.getViewer().setInput(WorkbenchPlugin.getDefault().getThemeRegistry());
		tree.getViewer().addDoubleClickListener(event -> {
			IStructuredSelection s = (IStructuredSelection) event.getSelection();
			Object element = s.getFirstElement();
			if (tree.getViewer().isExpandable(element))
				tree.getViewer().setExpandedState(element, !tree.getViewer().getExpandedState(element));

			if (element instanceof ThemeElementDefinition) {
				ThemeElementDefinition definition = (ThemeElementDefinition) element;

				if (element instanceof FontDefinition) {
					editFont(tree.getDisplay());
				} else if (element instanceof ColorDefinition && isAvailableInCurrentTheme(definition)) {
					editColor(tree.getDisplay());
				}
				updateControls();
			}
		});

		restoreTreeExpansion();
		restoreTreeSelection();
	}

	@Override
	public void dispose() {
		eventBroker.unsubscribe(themeRegistryRestyledHandler);
		workbench.getThemeManager().removePropertyChangeListener(themeChangeListener);
		clearPreviews();
		colorRegistry.dispose();
		fontRegistry.dispose();
		super.dispose();
	}

	/**
	 * Clear all previews.
	 */
	private void clearPreviews() {
		if (cascadingTheme != null)
			cascadingTheme.dispose();

		for (IThemePreview preview : previewSet) {
			try {
				preview.dispose();
			} catch (RuntimeException e) {
				WorkbenchPlugin.log(RESOURCE_BUNDLE.getString("errorDisposePreviewLog"), //$NON-NLS-1$
					StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e));
			}
		}
		previewSet.clear();
	}

	/**
	 * Get the ancestor of the given color, if any.
	 *
	 * @param definition the descendant <code>ColorDefinition</code>.
	 * @return the ancestor <code>ColorDefinition</code>, or <code>null</code> if
	 *         none.
	 */
	private ColorDefinition getColorAncestor(ColorDefinition definition) {
		String defaultsTo = definition.getDefaultsTo();
		if (defaultsTo == null)
			return null;
		return themeRegistry.findColor(defaultsTo);
	}

	/**
	 * Get the RGB value of the given colors ancestor, if any.
	 *
	 * @param definition the descendant <code>ColorDefinition</code>.
	 * @return the ancestor <code>RGB</code>, or <code>null</code> if none.
	 */
	private RGB getColorAncestorValue(ColorDefinition definition) {
		ColorDefinition ancestor = getColorAncestor(definition);
		if (ancestor == null)
			return null;
		return getColorValue(ancestor);
	}

	/**
	 * Get the RGB value for the specified definition. Cascades through
	 * preferenceToSet, valuesToSet and finally the registry.
	 *
	 * @param definition the <code>ColorDefinition</code>.
	 * @return the <code>RGB</code> value.
	 */
	private RGB getColorValue(ColorDefinition definition) {
		String id = definition.getId();
		RGB updatedRGB = colorPreferencesToSet.get(definition);
		if (updatedRGB == null) {
			updatedRGB = colorValuesToSet.get(id);
			if (updatedRGB == null)
				updatedRGB = currentTheme.getColorRegistry().getRGB(id);
		}
		return updatedRGB;
	}

	/**
	 * Get colors that descend from the provided color.
	 *
	 * @param definition the ancestor <code>ColorDefinition</code>.
	 * @return the ColorDefinitions that have the provided definition as their
	 *         defaultsTo attribute.
	 */
	private ColorDefinition[] getDescendantColors(ColorDefinition definition) {
		List<ColorDefinition> list = new ArrayList<>(5);
		String id = definition.getId();

		ColorDefinition[] colors = themeRegistry.getColors();
		ColorDefinition[] sorted = new ColorDefinition[colors.length];
		System.arraycopy(colors, 0, sorted, 0, sorted.length);

		Arrays.sort(sorted, new IThemeRegistry.HierarchyComparator(colors));

		for (ColorDefinition colorDefinition : sorted) {
			if (id.equals(colorDefinition.getDefaultsTo()))
				list.add(colorDefinition);
		}
		return list.toArray(new ColorDefinition[list.size()]);
	}

	private FontDefinition[] getDescendantFonts(FontDefinition definition) {
		List<FontDefinition> list = new ArrayList<>(5);
		String id = definition.getId();

		FontDefinition[] fonts = themeRegistry.getFonts();
		FontDefinition[] sorted = new FontDefinition[fonts.length];
		System.arraycopy(fonts, 0, sorted, 0, sorted.length);

		Arrays.sort(sorted, new IThemeRegistry.HierarchyComparator(fonts));

		for (FontDefinition fontDefinition : sorted) {
			if (id.equals(fontDefinition.getDefaultsTo()))
				list.add(fontDefinition);
		}
		return list.toArray(new FontDefinition[list.size()]);
	}

	private FontDefinition getFontAncestor(FontDefinition definition) {
		String defaultsTo = definition.getDefaultsTo();
		if (defaultsTo == null)
			return null;
		return themeRegistry.findFont(defaultsTo);
	}

	private FontData[] getFontAncestorValue(FontDefinition definition) {
		FontDefinition ancestor = getFontAncestor(definition);
		if (ancestor == null) {
			return PreferenceConverter.getDefaultFontDataArray(getPreferenceStore(), createPreferenceKey(definition));
		}
		return getFontValue(ancestor);
	}

	protected FontData[] getFontValue(FontDefinition definition) {
		String id = definition.getId();
		FontData[] updatedFD = fontPreferencesToSet.get(definition);
		if (updatedFD == null) {
			updatedFD = fontValuesToSet.get(id);
			if (updatedFD == null)
				updatedFD = currentTheme.getFontRegistry().getFontData(id);
		}
		return updatedFD;
	}

	protected ColorDefinition getSelectedColorDefinition() {
		Object o = tree.getViewer().getStructuredSelection().getFirstElement();
		if (o instanceof ColorDefinition) {
			return themeRegistry.findColor(((ColorDefinition) o).getId());
		}
		return null;
	}

	protected FontDefinition getSelectedFontDefinition() {
		Object o = tree.getViewer().getStructuredSelection().getFirstElement();
		if (o instanceof FontDefinition) {
			return themeRegistry.findFont(((FontDefinition) o).getId());
		}
		return null;
	}

	protected boolean isFontSelected() {
		Object o = tree.getViewer().getStructuredSelection().getFirstElement();
		return (o instanceof FontDefinition);
	}

	protected boolean isColorSelected() {
		Object o = tree.getViewer().getStructuredSelection().getFirstElement();
		return (o instanceof ColorDefinition);
	}

	/**
	 * Hook all control listeners.
	 */
	private void hookListeners() {
		TreeViewer viewer = tree.getViewer();
		viewer.addSelectionChangedListener(event -> updateTreeSelection(event.getSelection()));

		fontChangeButton.addSelectionListener(widgetSelectedAdapter(event -> {
			Display display = event.display;
			if (isFontSelected())
				editFont(display);
			else if (isColorSelected())
				editColor(display);
			updateControls();
		}));

		fontResetButton.addSelectionListener(widgetSelectedAdapter(e -> {
			if (isFontSelected())
				resetFont(getSelectedFontDefinition(), false);
			else if (isColorSelected())
				resetColor(getSelectedColorDefinition(), false);
			updateControls();
		}));

		fontSystemButton.addSelectionListener(widgetSelectedAdapter(event -> {
			FontDefinition definition = getSelectedFontDefinition();
			if (definition == null)
				return;
			FontData[] defaultFontData = JFaceResources.getDefaultFont().getFontData();
			setFontPreferenceValue(definition, defaultFontData, false);
			updateControls();
		}));

		editDefaultButton.addSelectionListener(widgetSelectedAdapter(event -> {
			Display display = event.display;
			FontDefinition fontDefinition = getSelectedFontDefinition();
			if (fontDefinition != null) {
				String defaultFontId = fontDefinition.getDefaultsTo();
				FontDefinition defaultFontDefinition = themeRegistry.findFont(defaultFontId);
				editFont(defaultFontDefinition, display);
			} else {
				ColorDefinition colorDefinition = getSelectedColorDefinition();
				if (colorDefinition != null) {
					String defaultColorId = colorDefinition.getDefaultsTo();
					ColorDefinition defaultColorDefinition = themeRegistry.findColor(defaultColorId);
					editColor(defaultColorDefinition, display);
				}
			}
			updateControls();
		}));

		goToDefaultButton.addSelectionListener(widgetSelectedAdapter(event -> {
			FontDefinition fontDefinition = getSelectedFontDefinition();
			if (fontDefinition != null) {
				String defaultFontId = fontDefinition.getDefaultsTo();
				FontDefinition defaultFontDefinition = themeRegistry.findFont(defaultFontId);
				selectAndReveal(defaultFontDefinition);
			} else {
				ColorDefinition colorDefinition = getSelectedColorDefinition();
				if (colorDefinition != null) {
					String defaultColorId = colorDefinition.getDefaultsTo();
					ColorDefinition defaultColorDefinition = themeRegistry.findColor(defaultColorId);
					selectAndReveal(defaultColorDefinition);
				}
			}
			updateControls();
		}));

		expandAllButton.addSelectionListener(widgetSelectedAdapter(event -> tree.getViewer().expandAll()));

	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = (Workbench) aWorkbench;
		themeEngine = workbench.getService(IThemeEngine.class);
		setPreferenceStore(PrefUtil.getInternalPreferenceStore());

		final IThemeManager themeManager = aWorkbench.getThemeManager();
		themeChangeListener = event -> {
			if (event.getProperty().equals(IThemeManager.CHANGE_CURRENT_THEME)) {
				updateThemeInfo(themeManager);
				refreshCategory();
				refreshAllLabels();
			}
		};
		themeManager.addPropertyChangeListener(themeChangeListener);

		updateThemeInfo(themeManager);

		eventBroker = workbench.getService(IEventBroker.class);
		eventBroker.subscribe(WorkbenchThemeManager.Events.THEME_REGISTRY_RESTYLED, themeRegistryRestyledHandler);
	}

	private void updateThemeInfo(IThemeManager manager) {
		clearPreviews();
		categoryMap.clear();

		if (labelProvider != null)
			labelProvider.dispose(); // nuke the old cache

		if (colorRegistry != null)
			colorRegistry.dispose();
		if (fontRegistry != null)
			fontRegistry.dispose();

		currentTheme = manager.getCurrentTheme();

		currentCSSTheme = getActiveTheme();

		colorRegistry = new CascadingColorRegistry(currentTheme.getColorRegistry());
		fontRegistry = new CascadingFontRegistry(currentTheme.getFontRegistry());

		if (cascadingTheme != null) {
			cascadingTheme.dispose();
			cascadingTheme = new CascadingThemeExt(currentTheme, colorRegistry, fontRegistry);
		}

		fontPreferencesToSet.clear();
		fontValuesToSet.clear();

		colorPreferencesToSet.clear();
		colorValuesToSet.clear();

		if (labelProvider != null)
			labelProvider.hookListeners(); // rehook the listeners
	}

	/**
	 * Answers whether the definition is currently set to the default value.
	 *
	 * @param definition the <code>ColorDefinition</code> to check.
	 * @return Return whether the definition is currently mapped to the default
	 *         value, either in the preference store or in the local change record
	 *         of this preference page.
	 */
	private boolean isDefault(ColorDefinition definition) {
		String id = definition.getId();

		if (colorPreferencesToSet.containsKey(definition)) {
			if (definition.getValue() != null) { // value-based color
				if (colorPreferencesToSet.get(definition).equals(definition.getValue()))
					return true;
			} else if (colorPreferencesToSet.get(definition).equals(getColorAncestorValue(definition)))
				return true;
		} else if (colorValuesToSet.containsKey(id)) {
			if (definition.getValue() != null) { // value-based color
				if (colorValuesToSet.get(id).equals(definition.getValue()))
					return true;
			} else {
				if (colorValuesToSet.get(id).equals(getColorAncestorValue(definition)))
					return true;
			}
		} else if (definition.getValue() != null) { // value-based color
			if (getPreferenceStore().isDefault(createPreferenceKey(definition)))
				return true;
		} else {
			// a descendant is default if it's the same value as its ancestor
			RGB rgb = getColorValue(definition);
			if (rgb != null && rgb.equals(getColorAncestorValue(definition)))
				return true;
		}
		return false;
	}

	private boolean isDefault(FontDefinition definition) {
		String id = definition.getId();

		if (fontPreferencesToSet.containsKey(definition)) {
			if (definition.getValue() != null) { // value-based font
				if (Arrays.equals(fontPreferencesToSet.get(definition), definition.getValue()))
					return true;
			} else {
				FontData[] ancestor = getFontAncestorValue(definition);
				if (Arrays.equals(fontPreferencesToSet.get(definition), ancestor))
					return true;
			}
		} else if (fontValuesToSet.containsKey(id)) {
			if (definition.getValue() != null) { // value-based font
				if (Arrays.equals(fontValuesToSet.get(id), definition.getValue()))
					return true;
			} else {
				FontData[] ancestor = getFontAncestorValue(definition);
				if (Arrays.equals(fontValuesToSet.get(id), ancestor))
					return true;
			}

		} else if (definition.getValue() != null) { // value-based font
			if (getPreferenceStore().isDefault(createPreferenceKey(definition)))
				return true;
		} else {
			FontData[] ancestor = getFontAncestorValue(definition);
			if (ancestor == null)
				return true;
			// a descendant is default if it's the same value as its ancestor
			if (Arrays.equals(getFontValue(definition), ancestor))
				return true;
		}
		return false;
	}

	/**
	 * Apply the dialog font to the control and store it for later so that it can be
	 * used for a later update.
	 */
	private void myApplyDialogFont(Control control) {
		control.setFont(JFaceResources.getDialogFont());
		dialogFontWidgets.add(control);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		super.performApply();

		// Apply the default font to the dialog.
		Font oldFont = appliedDialogFont;
		FontDefinition fontDefinition = themeRegistry.findFont(JFaceResources.DIALOG_FONT);
		if (fontDefinition == null)
			return;
		FontData[] newData = getFontValue(fontDefinition);

		appliedDialogFont = new Font(getControl().getDisplay(), newData);

		updateForDialogFontChange(appliedDialogFont);
		getApplyButton().setFont(appliedDialogFont);
		getDefaultsButton().setFont(appliedDialogFont);

		if (oldFont != null) {
			oldFont.dispose();
		}

		if (performColorOk() && performFontOk()) {
			publishThemeRegistryModifiedEvent();
		}
	}

	private void performColorDefaults() {
		ColorDefinition[] definitions = themeRegistry.getColors();

		// apply defaults in depth-order.
		ColorDefinition[] definitionsCopy = new ColorDefinition[definitions.length];
		System.arraycopy(definitions, 0, definitionsCopy, 0, definitions.length);

		Arrays.sort(definitionsCopy, new IThemeRegistry.HierarchyComparator(definitions));

		for (ColorDefinition colorDefinition : definitionsCopy) {
			resetColor(colorDefinition, true);
		}
	}

	private boolean performColorOk() {
		for (Map.Entry<ColorDefinition, RGB> entry : colorPreferencesToSet.entrySet()) {
			String key = createPreferenceKey(entry.getKey());
			String rgbString = StringConverter.asString(entry.getValue());
			String storeString = getPreferenceStore().getString(key);

			if (!rgbString.equals(storeString))
				getPreferenceStore().setValue(key, rgbString);
		}

		colorValuesToSet.clear();
		colorPreferencesToSet.clear();
		return true;
	}

	private String createPreferenceKey(ThemeElementDefinition definition) {
		if (definition.isOverridden() || definition.isAddedByCss()) {
			return ThemeElementHelper.createPreferenceKey(currentCSSTheme, currentTheme, definition.getId());
		}
		return ThemeElementHelper.createPreferenceKey(currentTheme, definition.getId());
	}

	@Override
	public void performDefaults() {
		performColorDefaults();
		performFontDefaults();
		updateControls();
		tree.getViewer().refresh();
	}

	private void performFontDefaults() {
		FontDefinition[] definitions = themeRegistry.getFonts();

		// apply defaults in depth-order.
		FontDefinition[] definitionsCopy = new FontDefinition[definitions.length];
		System.arraycopy(definitions, 0, definitionsCopy, 0, definitions.length);

		Arrays.sort(definitionsCopy, new IThemeRegistry.HierarchyComparator(definitions));

		for (FontDefinition fontDefinition : definitionsCopy) {
			resetFont(fontDefinition, true);
		}
	}

	private boolean performFontOk() {
		for (Map.Entry<FontDefinition, FontData[]> entry : fontPreferencesToSet.entrySet()) {
			String key = createPreferenceKey(entry.getKey());
			String fdString = PreferenceConverter.getStoredRepresentation(entry.getValue());
			String storeString = getPreferenceStore().getString(key);

			if (!fdString.equals(storeString))
				getPreferenceStore().setValue(key, fdString);
		}

		fontValuesToSet.clear();
		fontPreferencesToSet.clear();
		return true;
	}

	@Override
	public boolean performOk() {
		saveTreeExpansion();
		saveTreeSelection();
		boolean result = performColorOk() && performFontOk();
		if (result) {
			PrefUtil.savePrefs();
			publishThemeRegistryModifiedEvent();
		}
		return result;
	}

	/**
	 * Refreshes the category.
	 */
	private void refreshCategory() {
		updateControls();
	}

	/**
	 * Resets the supplied definition to its default value.
	 *
	 * @param definition the <code>ColorDefinition</code> to reset.
	 * @return whether any change was made.
	 */
	private boolean resetColor(ColorDefinition definition, boolean force) {
		if (force || !isDefault(definition)) {
			RGB newRGB;
			if (definition.getValue() != null)
				newRGB = definition.getValue();
			else
				newRGB = getColorAncestorValue(definition);

			if (newRGB != null) {
				setColorPreferenceValue(definition, newRGB, true);
				setRegistryValue(definition, newRGB);
				refreshElement(definition);
				return true;
			}
		}
		return false;
	}

	protected boolean resetFont(FontDefinition definition, boolean force) {
		if (force || !isDefault(definition)) {
			FontData[] newFD;
			if (definition.isOverridden()) {
				newFD = definition.getValue();
			} else if (!force && definition.getDefaultsTo() != null) {
				newFD = getFontAncestorValue(definition);
			} else {
				newFD = PreferenceConverter.getDefaultFontDataArray(getPreferenceStore(),
						createPreferenceKey(definition));
			}
			if (newFD != null) {
				setFontPreferenceValue(definition, newFD, true);
				refreshElement(definition);
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the value (in preferences) for the given color.
	 *
	 * @param definition the <code>ColorDefinition</code> to set.
	 * @param newRGB     the new <code>RGB</code> value for the definitions
	 *                   identifier.
	 */
	protected void setColorPreferenceValue(ColorDefinition definition, RGB newRGB, boolean reset) {
		setDescendantRegistryValues(definition, newRGB);
		colorPreferencesToSet.put(definition, newRGB);
		updateDefinitionState(definition, reset);
	}

	/**
	 * Set the value (in registry) for the given colors children.
	 *
	 * @param definition the <code>ColorDefinition</code> whose children should be
	 *                   set.
	 * @param newRGB     the new <code>RGB</code> value for the definitions
	 *                   identifier.
	 */
	private void setDescendantRegistryValues(ColorDefinition definition, RGB newRGB) {
		for (ColorDefinition colorDefinition : getDescendantColors(definition)) {
			if (isDefault(colorDefinition)) {
				setDescendantRegistryValues(colorDefinition, newRGB);
				setRegistryValue(colorDefinition, newRGB);
				colorValuesToSet.put(colorDefinition.getId(), newRGB);
			}
		}
	}

	private void setDescendantRegistryValues(FontDefinition definition, FontData[] datas, boolean reset) {
		for (FontDefinition fontDefinition : getDescendantFonts(definition)) {
			if (isDefault(fontDefinition)) {
				setFontPreferenceValue(fontDefinition, datas, reset);
			}
		}
	}

	protected void setFontPreferenceValue(FontDefinition definition, FontData[] datas, boolean reset) {
		// descendant values must be computed and set before updating current,
		// or isDefault will miss them
		setDescendantRegistryValues(definition, datas, reset);
		fontPreferencesToSet.put(definition, datas);
		setRegistryValue(definition, datas);
		updateDefinitionState(definition, reset);
	}

	/**
	 * Updates the working registry.
	 */
	protected void setRegistryValue(ColorDefinition definition, RGB newRGB) {
		colorRegistry.put(definition.getId(), newRGB);
	}

	protected void setRegistryValue(FontDefinition definition, FontData[] datas) {
		fontRegistry.put(definition.getId(), datas);
	}

	/**
	 * Returns the preview for the category.
	 *
	 * @param category the category
	 * @return the preview for the category, or its ancestors preview if it does not
	 *         have one.
	 */
	private IThemePreview getThemePreview(ThemeElementCategory category) throws CoreException {
		IThemePreview preview = category.createPreview();
		if (preview != null)
			return preview;

		if (category.getParentId() != null) {
			int idx = Arrays.binarySearch(themeRegistry.getCategories(), category.getParentId(),
					IThemeRegistry.ID_COMPARATOR);
			if (idx >= 0)
				return getThemePreview(themeRegistry.getCategories()[idx]);
		}
		return null;
	}

	private CascadingThemeExt getCascadingTheme() {
		if (cascadingTheme == null) {
			cascadingTheme = new CascadingThemeExt(currentTheme, colorRegistry, fontRegistry);
		}
		return cascadingTheme;
	}

	/**
	 * Update for a change in the dialog font.
	 */
	private void updateForDialogFontChange(Font newFont) {
		Iterator<Control> iterator = dialogFontWidgets.iterator();
		while (iterator.hasNext()) {
			iterator.next().setFont(newFont);
		}

		// recalculate the fonts for the tree
		labelProvider.clearFontCacheAndUpdate();
	}

	private void updateTreeSelection(ISelection selection) {
		ThemeElementCategory category = null;
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element instanceof ThemeElementCategory) {
			category = (ThemeElementCategory) element;
		} else if (element instanceof ColorDefinition) {
			String categoryID = ((ColorDefinition) element).getCategoryId();
			category = WorkbenchPlugin.getDefault().getThemeRegistry().findCategory(categoryID);
		} else if (element instanceof FontDefinition) {
			String categoryID = ((FontDefinition) element).getCategoryId();
			category = WorkbenchPlugin.getDefault().getThemeRegistry().findCategory(categoryID);
		}
		Composite previewControl = null;
		if (category != null) { // check if there is a preview for it
			previewControl = previewMap.get(category);
			if (previewControl == null) {
				try {
					IThemePreview preview = getThemePreview(category);
					if (preview != null) {
						previewControl = new Composite(previewComposite, SWT.NONE);
						previewControl.setLayout(new FillLayout());
						ITheme theme = getCascadingTheme();
						preview.createControl(previewControl, theme);
						previewSet.add(preview);
						previewMap.put(category, previewControl);
					}
				} catch (CoreException e) {
					previewControl = new Composite(previewComposite, SWT.NONE);
					previewControl.setLayout(new FillLayout());
					myApplyDialogFont(previewControl);
					Text error = new Text(previewControl, SWT.WRAP | SWT.READ_ONLY);
					error.setText(RESOURCE_BUNDLE.getString("errorCreatingPreview")); //$NON-NLS-1$
					WorkbenchPlugin.log(RESOURCE_BUNDLE.getString("errorCreatePreviewLog"), //$NON-NLS-1$
							StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e));
				}
			}
		}

		if (previewControl == null) { // there is no preview for this theme, use default preview
			if (element instanceof ColorDefinition)
				previewControl = defaultColorPreview;
			else if (element instanceof FontDefinition)
				previewControl = defaultFontPreview;
			else
				previewControl = defaultNoPreview;
		}

		stackLayout.topControl = previewControl;
		previewComposite.layout();
		updateControls();
	}

	/**
	 * Restore the selection state of the tree.
	 *
	 * @since 3.1
	 */
	private void restoreTreeSelection() {
		String selectedElementString = getPreferenceStore().getString(SELECTED_ELEMENT_PREF);
		if (selectedElementString == null)
			return;
		Object element = findElementFromMarker(selectedElementString);
		if (element == null)
			return;
		tree.getViewer().setSelection(new StructuredSelection(element), true);
	}

	/**
	 * Save the selection state of the tree.
	 *
	 * @since 3.1
	 */
	private void saveTreeSelection() {
		IStructuredSelection selection = tree.getViewer().getStructuredSelection();
		Object element = selection.getFirstElement();
		StringBuilder buffer = new StringBuilder();
		appendMarkerToBuffer(buffer, element);
		if (buffer.length() > 0)
			buffer.append(((IThemeElementDefinition) element).getId());
		getPreferenceStore().setValue(SELECTED_ELEMENT_PREF, buffer.toString());
	}

	/**
	 * Restore the expansion state of the tree.
	 *
	 * @since 3.1
	 */
	private void restoreTreeExpansion() {
		String expandedElementsString = getPreferenceStore().getString(EXPANDED_ELEMENTS_PREF);
		if (expandedElementsString == null)
			return;
		String[] expandedElementIDs = expandedElementsString.split(EXPANDED_ELEMENTS_TOKEN);
		if (expandedElementIDs.length == 0)
			return;

		List<IThemeElementDefinition> elements = new ArrayList<>(expandedElementIDs.length);
		for (String expandedElementID : expandedElementIDs) {
			IThemeElementDefinition def = findElementFromMarker(expandedElementID);
			if (def != null)
				elements.add(def);
		}
		tree.getViewer().setExpandedElements(elements.toArray());
	}

	/**
	 * Find the theme element from the given string. It will check the first
	 * character against the known constants and then call the appropriate method on
	 * the theme registry. If the element does not exist or the string is invalid
	 * <code>null</code> is returned.
	 *
	 * @param string the string to parse
	 * @return the element, or <code>null</code>
	 */
	private IThemeElementDefinition findElementFromMarker(String string) {
		if (string.length() < 2)
			return null;

		char marker = string.charAt(0);
		String id = string.substring(1);
		IThemeElementDefinition def = null;
		switch (marker) {
		case MARKER_FONT:
			def = themeRegistry.findFont(id);
			break;
		case MARKER_COLOR:
			def = themeRegistry.findColor(id);
			break;
		case MARKER_CATEGORY:
			def = themeRegistry.findCategory(id);
			break;
		}
		return def;
	}

	/**
	 * Saves the expansion state of the tree.
	 *
	 * @since 3.1
	 */
	private void saveTreeExpansion() {
		Object[] elements = tree.getViewer().getExpandedElements();
		List<String> elementIds = new ArrayList<>(elements.length);

		StringBuilder buffer = new StringBuilder();
		for (Object object : elements) {
			appendMarkerToBuffer(buffer, object);

			if (buffer.length() != 0) {
				buffer.append(((IThemeElementDefinition) object).getId());
				elementIds.add(buffer.toString());
			}
			buffer.setLength(0);
		}

		for (Iterator<String> i = elementIds.iterator(); i.hasNext();) {
			String id = i.next();
			buffer.append(id);
			if (i.hasNext()) {
				buffer.append(EXPANDED_ELEMENTS_TOKEN);
			}
		}

		getPreferenceStore().setValue(EXPANDED_ELEMENTS_PREF, buffer.toString());
	}

	private void appendMarkerToBuffer(StringBuilder buffer, Object object) {
		if (object instanceof FontDefinition) {
			buffer.append(MARKER_FONT);
		} else if (object instanceof ColorDefinition) {
			buffer.append(MARKER_COLOR);
		} else if (object instanceof ThemeElementCategory) {
			buffer.append(MARKER_CATEGORY);
		}
	}

	/**
	 * Edit the currently selected font.
	 *
	 * @param display the display to open the dialog on
	 * @since 3.2
	 */
	private void editFont(Display display) {
		editFont(getSelectedFontDefinition(), display);
	}

	/**
	 * Edit the given font.
	 *
	 * @param definition the font definition
	 * @param display    the display to open the dialog on
	 * @since 3.7
	 */
	private void editFont(FontDefinition definition, Display display) {
		if (definition != null) {
			final FontDialog fontDialog = new FontDialog(getShell());
			fontDialog.setEffectsVisible(false);
			fontDialog.setFontList(getFontValue(definition));
			final FontData data = fontDialog.open();
			if (data != null) {
				setFontPreferenceValue(definition, fontDialog.getFontList(), false);
				refreshElement(definition);
			}
		}
	}

	private void editColor(Display display) {
		editColor(getSelectedColorDefinition(), display);
	}

	private void editColor(ColorDefinition definition, Display display) {
		if (definition == null)
			return;
		RGB currentColor = colorRegistry.getRGB(definition.getId());

		ColorDialog colorDialog = new ColorDialog(display.getActiveShell());
		colorDialog.setRGB(currentColor);
		RGB selectedColor = colorDialog.open();
		if ((selectedColor != null) && (!selectedColor.equals(currentColor))) {
			setColorPreferenceValue(definition, selectedColor, false);
			setRegistryValue(definition, selectedColor);
			refreshElement(definition);
		}
	}

	protected void updateControls() {
		FontDefinition fontDefinition = getSelectedFontDefinition();
		if (fontDefinition != null) {
			boolean isSetToDefault = isDefault(fontDefinition);
			boolean hasEditableDefault = defaultIsEditable(fontDefinition);
			fontChangeButton.setEnabled(true);
			fontSystemButton.setEnabled(true);
			fontResetButton.setEnabled(!isSetToDefault);
			editDefaultButton.setEnabled(hasEditableDefault && isSetToDefault);
			goToDefaultButton.setEnabled(hasEditableDefault);
			setCurrentFont(fontDefinition);
			return;
		}
		ColorDefinition colorDefinition = getSelectedColorDefinition();
		if (colorDefinition != null) {
			boolean isSetToDefault = isDefault(getSelectedColorDefinition());
			boolean hasEditableDefault = defaultIsEditable(colorDefinition);
			fontChangeButton.setEnabled(true);
			fontSystemButton.setEnabled(false);
			fontResetButton.setEnabled(!isSetToDefault);
			editDefaultButton.setEnabled(hasEditableDefault && isSetToDefault);
			goToDefaultButton.setEnabled(hasEditableDefault);
			setCurrentColor(colorDefinition);
			return;
		}
		// not a font or a color?
		fontChangeButton.setEnabled(false);
		fontSystemButton.setEnabled(false);
		fontResetButton.setEnabled(false);
		editDefaultButton.setEnabled(false);
		goToDefaultButton.setEnabled(false);
		descriptionText.setText(""); //$NON-NLS-1$
	}

	/**
	 * Find out if the default can be edited. Note, isEditable=false also means that
	 * a property is not shown to user.
	 *
	 * @return True if definition has a default and the default's isEditable=true.
	 *         False otherwise.
	 */
	private boolean defaultIsEditable(IHierarchalThemeElementDefinition definition) {
		assert definition instanceof ColorDefinition || definition instanceof FontDefinition;
		String defaultId = definition.getDefaultsTo();
		if (defaultId == null) {
			return false;
		}
		IEditable defaultDefinition = null;
		if (definition instanceof ColorDefinition) {
			defaultDefinition = themeRegistry.findColor(defaultId);
		} else if (definition instanceof FontDefinition) {
			defaultDefinition = themeRegistry.findFont(defaultId);
		}
		return (defaultDefinition != null && defaultDefinition.isEditable());
	}

	/**
	 * @return Return the default "No preview available." preview.
	 */
	private Composite createNoPreviewControl() {
		Composite noPreviewControl = new Composite(previewComposite, SWT.NONE);
		noPreviewControl.setLayout(new FillLayout());
		Label l = new Label(noPreviewControl, SWT.LEFT);
		l.setText(RESOURCE_BUNDLE.getString("noPreviewAvailable")); //$NON-NLS-1$
		myApplyDialogFont(l);
		return noPreviewControl;
	}

	private void setCurrentFont(FontDefinition fontDefinition) {
		currentFont = fontRegistry.get(fontDefinition.getId());
		FontData[] fontData = currentFont != null ? currentFont.getFontData() : new FontData[0];

		// recalculate sample text
		StringBuilder tmp = new StringBuilder();
		for (FontData currentFontData : fontData) {
			tmp.append(currentFontData.getName());
			tmp.append(' ');
			tmp.append(currentFontData.getHeight());

			int style = currentFontData.getStyle();
			if ((style & SWT.BOLD) != 0) {
				tmp.append(' ');
				tmp.append(RESOURCE_BUNDLE.getString("boldFont")); //$NON-NLS-1$
			}
			if ((style & SWT.ITALIC) != 0) {
				tmp.append(' ');
				tmp.append(RESOURCE_BUNDLE.getString("italicFont")); //$NON-NLS-1$
			}
		}
		fontSampleText = tmp.toString();
		descriptionText.setText(fomatDescription(fontDefinition));
		fontSampler.redraw();
	}

	public void setCurrentColor(ColorDefinition colorDefinition) {
		currentColor = colorRegistry.get(colorDefinition.getId());
		colorSampler.redraw();
		descriptionText.setText(fomatDescription(colorDefinition));
	}

	private Composite createFontPreviewControl() {
		fontSampler = new Canvas(previewComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		fontSampler.setLayout(gridLayout);
		fontSampler.setLayoutData(new GridData(GridData.FILL_BOTH));

		fontSampler.addPaintListener(e -> {
			if (currentFont != null) // do the font preview
				paintFontSample(e.gc);
		});
		return fontSampler;
	}

	private void paintFontSample(GC gc) {
		if (currentFont == null || currentFont.isDisposed())
			return;

		// draw rectangle all around
		Rectangle clientArea = colorSampler.getClientArea();
		FontMetrics standardFontMetrics = gc.getFontMetrics();
		int standardLineHeight = standardFontMetrics.getHeight();
		int maxHeight = standardLineHeight * 4;
		if (clientArea.height > maxHeight)
			clientArea = new Rectangle(clientArea.x, clientArea.y, clientArea.width, maxHeight);

		gc.setForeground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawRectangle(0, 0, clientArea.width - 1, clientArea.height - 1);

		gc.setForeground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.setFont(currentFont);
		FontMetrics fontMetrics = gc.getFontMetrics();
		int lineHeight = fontMetrics.getHeight();
		int topY = clientArea.y + 5;

		gc.setClipping(1, 1, clientArea.width - 2, clientArea.height - 2);
		gc.drawText(fontSampleText, clientArea.x + 5, topY);
		gc.drawText(RESOURCE_BUNDLE.getString("fontTextSample"), clientArea.x + 5, topY + lineHeight); //$NON-NLS-1$
	}

	private Composite createColorPreviewControl() {
		colorSampler = new Canvas(previewComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		colorSampler.setLayout(gridLayout);
		colorSampler.setLayoutData(new GridData(GridData.FILL_BOTH));

		colorSampler.addPaintListener(e -> {
			if (currentColor != null) // do the color preview
				paintColorSample(e.gc);
		});
		return colorSampler;
	}

	private void paintColorSample(GC gc) {
		if (currentColor == null || currentColor.isDisposed())
			return;
		gc.setFont(previewComposite.getDisplay().getSystemFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		int lineHeight = fontMetrics.getHeight();
		Rectangle clientArea = colorSampler.getClientArea();
		int maxHeight = lineHeight * 4;
		if (clientArea.height > maxHeight)
			clientArea = new Rectangle(clientArea.x, clientArea.y, clientArea.width, maxHeight);

		String messageTop = RESOURCE_BUNDLE.getString("fontColorSample"); //$NON-NLS-1$
		String fontColorString = RESOURCE_BUNDLE.getString("fontColorString"); //$NON-NLS-1$
		RGB rgb = currentColor.getRGB();
		String messageBottom = MessageFormat.format(fontColorString, Integer.valueOf(rgb.red),
				Integer.valueOf(rgb.green), Integer.valueOf(rgb.blue));

		// calculate position of the vertical line
		int separator = (clientArea.width - 2) / 3;

		// calculate text positions
		int verticalCenter = clientArea.height / 2;
		int textTopY = (verticalCenter - lineHeight) / 2;
		if (textTopY < 1)
			textTopY = 1;
		textTopY += clientArea.y;

		int textBottomY = verticalCenter + textTopY;
		if (textBottomY > clientArea.height - 2)
			textBottomY = clientArea.height - 2;
		textBottomY += clientArea.y;

		int stringWidthTop = gc.stringExtent(messageTop).x;
		int textTopX = (separator - stringWidthTop - 1) / 2;
		if (textTopX < 1)
			textTopX = 1;
		textTopX += clientArea.x;

		int stringWidthBottom = gc.stringExtent(messageBottom).x;
		int textBottomX = (separator - stringWidthBottom - 1) / 2;
		if (textBottomX < 1)
			textBottomX = 1;
		textBottomX += clientArea.x;

		// put text on the left - default background
		gc.setForeground(currentColor);
		gc.drawText(messageTop, textTopX, textTopY);
		gc.drawText(messageBottom, textBottomX, textBottomY);

		// fill right rectangle
		gc.setBackground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		int rightWidth = clientArea.width - 2 - separator * 2;
		gc.fillRectangle(separator * 2, 1, rightWidth, clientArea.height - 2);
		// put text in the right rectangle
		gc.setForeground(currentColor);
		gc.drawText(messageTop, separator * 2 + textTopX, textTopY);
		gc.drawText(messageBottom, separator * 2 + textBottomX, textBottomY);

		// fill center rectangle
		gc.setBackground(currentColor);
		gc.fillRectangle(separator, 1, separator, clientArea.height - 2);
		// text: center top
		gc.setForeground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.drawText(messageTop, separator + textTopX, textTopY);
		gc.setForeground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.drawText(messageBottom, separator + textBottomX, textBottomY);
		// niceties
		gc.setForeground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawLine(separator, verticalCenter, separator * 2 - 1, verticalCenter);

		// draw rectangle all around
		gc.setForeground(previewComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawRectangle(0, 0, clientArea.width - 1, clientArea.height - 1);
	}

	private void createDescriptionControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("description")); //$NON-NLS-1$
		myApplyDialogFont(label);

		descriptionText = new Text(composite, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(3);
		data.widthHint = convertWidthInCharsToPixels(30);
		descriptionText.setLayoutData(data);
		myApplyDialogFont(descriptionText);
	}

	private void publishThemeRegistryModifiedEvent() {
		if (eventBroker != null) {
			eventBroker.send(WorkbenchThemeManager.Events.THEME_REGISTRY_MODIFIED, null);
		}
	}

	private void refreshAllLabels() {
		tree.getViewer().refresh(); // refresh all the labels in the tree
	}

	private boolean isAvailableInCurrentTheme(ThemeElementDefinition definition) {
		if (definition instanceof ColorDefinition) {
			ColorDefinition colorDef = (ColorDefinition) definition;
			RGB value = colorDef.getValue();
			if ((value == null || value == EMPTY_COLOR_VALUE) && colorDef.getDefaultsTo() == null) {
				return false;
			}
			return colorRegistry.get(definition.getId()) != null;
		}
		return true;
	}

	private String fomatDescription(ThemeElementDefinition definition) {
		String description = definition.getDescription() != null ? definition.getDescription() : ""; //$NON-NLS-1$
		if (isAvailableInCurrentTheme(definition)) {
			return description;
		}
		return MessageFormat.format(RESOURCE_BUNDLE.getString("definitionNotAvailInTheme"), //$NON-NLS-1$
				description).trim();
	}

	private void updateDefinitionState(ThemeElementDefinition definition, boolean reset) {
		if (reset) {
			definition.removeState(ThemeElementDefinition.State.MODIFIED_BY_USER);
		} else {
			definition.appendState(ThemeElementDefinition.State.MODIFIED_BY_USER);
		}
	}

	private void refreshElement(ThemeElementDefinition definition) {
		tree.getViewer().refresh(definition);
		updateTreeSelection(tree.getViewer().getSelection());

		Object newValue = definition instanceof ColorDefinition ? ((ColorDefinition) definition).getValue()
				: ((FontDefinition) definition).getValue();
		getCascadingTheme().fire(new PropertyChangeEvent(this, definition.getId(), null, newValue));
	}

	private static class CascadingThemeExt extends CascadingTheme {
		public CascadingThemeExt(ITheme currentTheme, CascadingColorRegistry colorRegistry,
				CascadingFontRegistry fontRegistry) {
			super(currentTheme, colorRegistry, fontRegistry);
		}

		@Override
		public void fire(PropertyChangeEvent event) {
			super.fire(event);
		}

	}
}
