/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import com.ibm.icu.text.MessageFormat;

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

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
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.themes.IThemePreview;

/**
 * Preference page for management of system colors, gradients and fonts.
 * 
 * @since 3.0
 */
public final class ColorsAndFontsPreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage {
	
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
			
    private class ThemeContentProvider implements ITreeContentProvider {

        private IThemeRegistry registry;

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ThemeElementCategory) {
                String categoryId = ((ThemeElementCategory) parentElement)
                        .getId();
                Object[] defintions = (Object[]) categoryMap.get(categoryId);
                if (defintions == null) {
                    defintions = getCategoryChildren(categoryId);
                    categoryMap.put(categoryId, defintions);
                }
                return defintions;
            }

			ArrayList list = new ArrayList();
			IHierarchalThemeElementDefinition def = (IHierarchalThemeElementDefinition) parentElement;
			String id = def.getId();
			IHierarchalThemeElementDefinition[] defs;
			if (def instanceof ColorDefinition) {
				defs = registry.getColors();
			} else {
				defs = registry.getFonts();
			}

			for (int i = 0; i < defs.length; i++) {
				if (id.equals(defs[i].getDefaultsTo())
						&& ColorsAndFontsPreferencePage.equals(
								((ICategorizedThemeElementDefinition) def)
										.getCategoryId(),
								((ICategorizedThemeElementDefinition) defs[i])
										.getCategoryId())) {
					list.add(defs[i]);
				}
			}
			return list.toArray();
        }

        private Object[] getCategoryChildren(String categoryId) {
            ArrayList list = new ArrayList();

            if (categoryId != null) {
                ThemeElementCategory[] categories = registry.getCategories();
                for (int i = 0; i < categories.length; i++) {
                    if (categoryId.equals(categories[i].getParentId())) {
                        Set bindings = themeRegistry
                                .getPresentationsBindingsFor(categories[i]);
                        if (bindings == null
                                || bindings.contains(workbench
                                        .getPresentationId())) {
							list.add(categories[i]);
						}
                    }
                }
            }
            {
                ColorDefinition[] colorDefinitions = themeRegistry
                        .getColorsFor(currentTheme.getId());
                for (int i = 0; i < colorDefinitions.length; i++) {
                    if (!colorDefinitions[i].isEditable()) {
						continue;
					}
                    String catId = colorDefinitions[i].getCategoryId();
                    if ((catId == null && categoryId == null)
                            || (catId != null && categoryId != null && categoryId
                                    .equals(catId))) {
                        if (colorDefinitions[i].getDefaultsTo() != null
                                && parentIsInSameCategory(colorDefinitions[i])) {
							continue;
						}
                        list.add(colorDefinitions[i]);
                    }
                }
            }
            {
                FontDefinition[] fontDefinitions = themeRegistry
                        .getFontsFor(currentTheme.getId());
                for (int i = 0; i < fontDefinitions.length; i++) {
                    if (!fontDefinitions[i].isEditable()) {
						continue;
					}
                    String catId = fontDefinitions[i].getCategoryId();
                    if ((catId == null && categoryId == null)
                            || (catId != null && categoryId != null && categoryId
                                    .equals(catId))) {
                        if (fontDefinitions[i].getDefaultsTo() != null
                                && parentIsInSameCategory(fontDefinitions[i])) {
							continue;
						}
                        list.add(fontDefinitions[i]);
                    }
                }
            }
            return list.toArray(new Object[list.size()]);
        }

        private boolean parentIsInSameCategory(ColorDefinition definition) {
            String defaultsTo = definition.getDefaultsTo();
            ColorDefinition[] defs = registry.getColors();
            for (int i = 0; i < defs.length; i++) {
                if (defs[i].getId().equals(defaultsTo)
                        && ColorsAndFontsPreferencePage.equals(defs[i]
                                .getCategoryId(), definition.getCategoryId())) {
					return true;
				}
            }
            return false;
        }

        private boolean parentIsInSameCategory(FontDefinition definition) {
            String defaultsTo = definition.getDefaultsTo();
            FontDefinition[] defs = registry.getFonts();
            for (int i = 0; i < defs.length; i++) {
                if (defs[i].getId().equals(defaultsTo)
                        && ColorsAndFontsPreferencePage.equals(defs[i]
                                .getCategoryId(), definition.getCategoryId())) {
					return true;
				}
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
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

			for (int i = 0; i < defs.length; i++) {
				if (id.equals(defs[i].getDefaultsTo())
						&& ColorsAndFontsPreferencePage.equals(
								((ICategorizedThemeElementDefinition) def)
										.getCategoryId(),
								((ICategorizedThemeElementDefinition) defs[i])
										.getCategoryId())) {
					return true;
				}
			}

            return false;
        }

        /*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
        public Object[] getElements(Object inputElement) {
            ArrayList list = new ArrayList();
            Object[] uncatChildren = getCategoryChildren(null);
            list.addAll(Arrays.asList(uncatChildren));
            ThemeElementCategory[] categories = ((IThemeRegistry) inputElement)
                    .getCategories();
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].getParentId() == null) {
                    Set bindings = themeRegistry
                            .getPresentationsBindingsFor(categories[i]);
                    if (bindings == null
                            || bindings.contains(workbench.getPresentationId())) {
						list.add(categories[i]);
					}
                }
            }
            return list.toArray(new Object[list.size()]);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            categoryMap.clear();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            categoryMap.clear();
            registry = (IThemeRegistry) newInput;
        }

    }

    private class PresentationLabelProvider extends LabelProvider implements
            IFontProvider {

        private HashMap fonts = new HashMap();

        private HashMap images = new HashMap();

        private int imageSize = -1;

        private int usableImageSize = -1;

        private IPropertyChangeListener listener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                fireLabelProviderChanged(new LabelProviderChangedEvent(
                        PresentationLabelProvider.this));
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

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose() {
            super.dispose();
            colorRegistry.removeListener(listener);
            fontRegistry.removeListener(listener);
            for (Iterator i = images.values().iterator(); i.hasNext();) {
                ((Image) i.next()).dispose();
            }
            images.clear();

            if (emptyImage != null) {
                emptyImage.dispose();
                emptyImage = null;
            }

            //clear the fonts.
            clearFontCache();
        }

        /**
         * Clears and disposes all fonts.
         */
        public void clearFontCache() {
            for (Iterator i = fonts.values().iterator(); i.hasNext();) {
                ((Font) i.next()).dispose();
            }
            fonts.clear();
        }
        
        /**
         * Clears and disposes all fonts and fires a label update.
         */
        public void clearFontCacheAndUpdate() {
        	clearFontCache();
        	fireLabelProviderChanged(new LabelProviderChangedEvent(
                    PresentationLabelProvider.this));
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            Display display = tree.getDisplay();
            if (element instanceof FontDefinition) {
                int parentHeight = tree.getViewer().getControl().getFont()
                        .getFontData()[0].getHeight();
                Font baseFont = fontRegistry.get(((FontDefinition) element)
                        .getId());
                Font font = (Font) fonts.get(baseFont);
                if (font == null) {
                    FontData[] data = baseFont.getFontData();
                    for (int i = 0; i < data.length; i++) {
                        data[i].setHeight(parentHeight);
                    }
                    font = new Font(display, data);

                    fonts.put(baseFont, font);
                }
                return font;
            }

            return JFaceResources.getDialogFont();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            if (element instanceof ColorDefinition) {
                Color c = colorRegistry
                        .get(((ColorDefinition) element).getId());
                Image image = (Image) images.get(c);
                if (image == null) {
                    Display display = tree.getDisplay();
                    ensureImageSize();
                    //int size = presentationList.getControl().getFont().getFontData()[0].getHeight();
                    image = new Image(display, imageSize, imageSize);

                    GC gc = new GC(image);
                    gc.setBackground(tree.getViewer().getControl()
                            .getBackground());
                    gc.setForeground(tree.getViewer().getControl()
                            .getBackground());
                    gc.drawRectangle(0, 0, imageSize - 1, imageSize - 1);

                    gc.setForeground(tree.getViewer().getControl()
                            .getForeground());
                    gc.setBackground(c);

                    int offset = (imageSize - usableImageSize) / 2;
                    gc.drawRectangle(offset, offset, usableImageSize - offset,
                            usableImageSize - offset);
                    gc.fillRectangle(offset + 1, offset + 1, usableImageSize
                            - offset - 1, usableImageSize - offset - 1);
                    gc.dispose();

                    images.put(c, image);
                }
                return image;

            } else if (element instanceof FontDefinition) {
                return workbench.getSharedImages().getImage(
                        IWorkbenchGraphicConstants.IMG_OBJ_FONT);
            } else {
                return workbench.getSharedImages().getImage(
                        IWorkbenchGraphicConstants.IMG_OBJ_THEME_CATEGORY);
            }
        }

        private void ensureImageSize() {
            if (imageSize == -1) {
                imageSize = tree.getViewer().getTree().getItemHeight();
                usableImageSize = Math.max(1, imageSize - 4);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            if (element instanceof IHierarchalThemeElementDefinition) {
                IHierarchalThemeElementDefinition themeElement = (IHierarchalThemeElementDefinition) element;
				if (themeElement
                        .getDefaultsTo() != null) {
                    String myCategory = ((ICategorizedThemeElementDefinition) themeElement)
                            .getCategoryId();
                    ICategorizedThemeElementDefinition def;
                    if (element instanceof ColorDefinition) {
						def = themeRegistry
                                .findColor(themeElement
                                        .getDefaultsTo());
					} else {
						def = themeRegistry
                                .findFont(themeElement
                                        .getDefaultsTo());
					}

                    if (!ColorsAndFontsPreferencePage.equals(def
                            .getCategoryId(), myCategory)) {
                    		if (isDefault(themeElement)) {
							return MessageFormat
									.format(
											RESOURCE_BUNDLE
													.getString("defaultFormat_default"), new Object[] { themeElement.getName(), def.getName() }); //$NON-NLS-1$
						}
                		
                			return MessageFormat
							.format(
									RESOURCE_BUNDLE
											.getString("defaultFormat_override"), new Object[] { themeElement.getName(), def.getName() }); //$NON-NLS-1$
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
				return ColorsAndFontsPreferencePage.this.isDefault((FontDefinition)def);
			} else if (def instanceof ColorDefinition) {
				return ColorsAndFontsPreferencePage.this.isDefault((ColorDefinition)def);
			}
			return false;
		}
    }

    /**
     * The translation bundle in which to look up internationalized text.
     */
    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(ColorsAndFontsPreferencePage.class.getName());

    /**
     * Map to precalculate category color lists.
     */
    private Map categoryMap = new HashMap(7);

    private Font appliedDialogFont;

    /**
     * The composite containing all color-specific controls. 
     */
    private Composite colorControls;

    /**
     * Map of definition id->RGB objects that map to changes expressed in this
     * UI session.  These changes should be made in preferences and the 
     * registry.
     */
    private Map colorPreferencesToSet = new HashMap(7);

    private CascadingColorRegistry colorRegistry;

    private Button colorResetButton;

    private ColorSelector colorSelector;

    /**
     * Map of definition id->RGB objects that map to changes expressed in this
     * UI session.  These changes should be made in the registry.
     */
    private Map colorValuesToSet = new HashMap(7);

    /**
     * The composite that contains the font or color controls (or none).
     */
    private Composite controlArea;

    /**
     * The layout for the controlArea.
     */
    private StackLayout controlAreaLayout;

    /**
     * The composite to use when no preview is available. 
     */
    private Composite defaultPreviewControl;

    private Text descriptionText;

    private List dialogFontWidgets = new ArrayList();

    private Button fontChangeButton;

    /**
     * The composite containing all font-specific controls. 
     */
    private Composite fontControls;

    private Map fontPreferencesToSet = new HashMap(7);

    private CascadingFontRegistry fontRegistry;

    private Button fontResetButton;

    private Button fontSystemButton;

    /**
     * Map of definition id->FontData[] objects that map to changes expressed in 
     * this UI session.  These changes should be made in preferences and the 
     * registry.
     */
    private Map fontValuesToSet = new HashMap(7);

    /**
     * The list of fonts and colors.
     */
    //private TreeViewer presentationList;
    /**
     * The composite that is parent to all previews.
     */
    private Composite previewComposite;

    /**
     * A mapping from PresentationCategory->Composite for the created previews.
     */
    private Map previewMap = new HashMap(7);

    /**
     * Set containing all IPresentationPreviews created. 
     */
    private Set previewSet = new HashSet(7);

    /**
     * The layout for the previewComposite.
     */
    private StackLayout stackLayout;

    private final IThemeRegistry themeRegistry;

    private ITheme currentTheme;

    private PresentationLabelProvider labelProvider;

    private CascadingTheme cascadingTheme;

    private IPropertyChangeListener themeChangeListener;

    private Workbench workbench;

    private FilteredTree tree;

    /**
     * Create a new instance of the receiver. 
     */
    public ColorsAndFontsPreferencePage() {
        themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
        //no-op
    }

    private static boolean equals(String string, String string2) {
        if ((string == null && string2 == null)) {
			return true;
		}
        if (string == null || string2 == null) {
			return false;
		}
        if (string.equals(string2)) {
			return true;
		}

        return false;
    }

    /**
     * Create a button for the preference page.
     * @param parent
     * @param label
     */
    private Button createButton(Composite parent, String label) {
        Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
        button.setText(label);
        myApplyDialogFont(button);
        setButtonLayoutData(button);
        button.setEnabled(false);
        return button;
    }

    /**
     * Create the color selection control. 
     */
    private void createColorControl() {
        Composite composite = new Composite(colorControls, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        colorSelector = new ColorSelector(composite);
        colorSelector.getButton().setLayoutData(new GridData());
        myApplyDialogFont(colorSelector.getButton());
        colorSelector.setEnabled(false);

        colorResetButton = createButton(composite, RESOURCE_BUNDLE
                .getString("reset")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
    	
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.FONTS_PREFERENCE_PAGE);
    	
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (appliedDialogFont != null) {
					appliedDialogFont.dispose();
				}
            }
        });
        Composite mainColumn = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        mainColumn.setFont(parent.getFont());
        mainColumn.setLayout(layout);

        GridData data = new GridData(GridData.BEGINNING);
        Label label = new Label(mainColumn, SWT.LEFT);
        label.setText(RESOURCE_BUNDLE.getString("colorsAndFonts")); //$NON-NLS-1$
        myApplyDialogFont(label);
        label.setLayoutData(data);

        Composite controlRow = new Composite(mainColumn, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        controlRow.setLayout(layout);
        data = new GridData(GridData.FILL_HORIZONTAL);
        controlRow.setLayoutData(data);

        createTree(controlRow);
        Composite controlColumn = new Composite(controlRow, SWT.NONE);
        data = new GridData(GridData.FILL_VERTICAL);
        controlColumn.setLayoutData(data);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        controlColumn.setLayout(layout);

        controlArea = new Composite(controlColumn, SWT.NONE);
        controlAreaLayout = new StackLayout();
        controlArea.setLayout(controlAreaLayout);

        colorControls = new Composite(controlArea, SWT.NONE);
        colorControls.setLayout(new FillLayout());
        createColorControl();

        fontControls = new Composite(controlArea, SWT.NONE);
        fontControls.setLayout(new FillLayout());
        createFontControl();

        createDescriptionControl(mainColumn);

        createPreviewControl(mainColumn);

        hookListeners();
        
        updateTreeSelection(tree.getViewer().getSelection());

        return mainColumn;
    }

    /**
     * Create the text box that will contain the current color/font description 
     * text (if any).
     * 
     * @param parent the parent <code>Composite</code>.
     */
    private void createDescriptionControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = convertHeightInCharsToPixels(5);
        composite.setLayoutData(data);

        Label label = new Label(composite, SWT.LEFT);
        label.setText(RESOURCE_BUNDLE.getString("description")); //$NON-NLS-1$
        myApplyDialogFont(label);

        descriptionText = new Text(composite, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
        data = new GridData(GridData.FILL_BOTH);
        descriptionText.setLayoutData(data);
        myApplyDialogFont(descriptionText);
    }

    private void createFontControl() {
        Composite composite = new Composite(fontControls, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        fontSystemButton = createButton(composite, WorkbenchMessages.FontsPreference_useSystemFont);

        fontChangeButton = createButton(composite, JFaceResources
                .getString("openChange")); //$NON-NLS-1$

        fontResetButton = createButton(composite, RESOURCE_BUNDLE
                .getString("reset")); //$NON-NLS-1$
    }

    /**
     * Create the <code>ListViewer</code> that will contain all color 
     * definitions as defined in the extension point.
     * 
     * @param parent the parent <code>Composite</code>.
     */
    private void createTree(Composite parent) {
        labelProvider = new PresentationLabelProvider();
        // create a new tree with a custom pattern matcher that will allow
        // non-category elements to be returned in the event that their children
        // do not
        tree = new FilteredTree(parent, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER, new PatternFilter() {
            
            /* (non-Javadoc)
             * @see org.eclipse.ui.dialogs.PatternFilter#isParentMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
             */
            protected boolean isParentMatch(Viewer viewer, Object element) {
                Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                        .getContentProvider()).getChildren(element);
                if (children.length > 0
                        && element instanceof ThemeElementCategory) {
					return filter(viewer, element, children).length > 0;
				}
                return false;
            }
        });

        GridData data = new GridData(GridData.FILL_HORIZONTAL
                | GridData.VERTICAL_ALIGN_FILL);
        data.heightHint = Math.max(175, convertHeightInCharsToPixels(10));
        tree.setLayoutData(data);
        myApplyDialogFont(tree.getViewer().getControl());
        Text filterText = tree.getFilterControl();
        if (filterText != null) {
			myApplyDialogFont(filterText);
		}

        tree.getViewer().setLabelProvider(labelProvider);
        tree.getViewer().setContentProvider(new ThemeContentProvider());
        tree.getViewer().setComparator(new ViewerComparator() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
             */
            public int category(Object element) {
                if (element instanceof ThemeElementCategory) {
					return 0;
				}
                return 1;
            }
        });
        tree.getViewer().setInput(
                WorkbenchPlugin.getDefault().getThemeRegistry());
        tree.getViewer().addDoubleClickListener(new IDoubleClickListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
             */
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection s = (IStructuredSelection) event
                        .getSelection();
                Object element = s.getFirstElement();
                if (tree.getViewer().isExpandable(element)) {
                    tree.getViewer().setExpandedState(element,
                            !tree.getViewer().getExpandedState(element));
                }
                
                if (element instanceof FontDefinition) {
                		editFont(tree.getDisplay());
                }
                else if (element instanceof ColorDefinition) {
                		colorSelector.open();
                }
            }
        });
        
        restoreTreeExpansion();
        restoreTreeSelection();
    }

    private void createPreviewControl(Composite mainColumn) {
        Composite composite = new Composite(mainColumn, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 175;
        composite.setLayoutData(data);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.LEFT);
        label.setText(RESOURCE_BUNDLE.getString("preview")); //$NON-NLS-1$
        myApplyDialogFont(label);
        previewComposite = new Composite(composite, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        previewComposite.setLayoutData(data);
        stackLayout = new StackLayout();
        previewComposite.setLayout(stackLayout);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
     */
    public void dispose() {
        super.dispose();

        workbench.getThemeManager().removePropertyChangeListener(
                themeChangeListener);

        clearPreviews();

        colorRegistry.dispose();
        fontRegistry.dispose();

    }

    /**
     * Clear all previews.
     */
    private void clearPreviews() {
        if (cascadingTheme != null) {
			cascadingTheme.dispose();
		}

        for (Iterator i = previewSet.iterator(); i.hasNext();) {
            IThemePreview preview = (IThemePreview) i.next();
            try {
                preview.dispose();
            } catch (RuntimeException e) {
                WorkbenchPlugin
                        .log(
                                RESOURCE_BUNDLE
                                        .getString("errorDisposePreviewLog"), StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e)); //$NON-NLS-1$
            }
        }

        previewSet.clear();
    }

    /**
     * Get the ancestor of the given color, if any.
     * 
     * @param definition the descendant <code>ColorDefinition</code>.
     * @return the ancestor <code>ColorDefinition</code>, or <code>null</code> 
     * 		if none.
     */
    private ColorDefinition getColorAncestor(ColorDefinition definition) {
        String defaultsTo = definition.getDefaultsTo();
        if (defaultsTo == null) {
			return null;
		}

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
        if (ancestor == null) {
			return null;
		}

        return getColorValue(ancestor);
    }

    /**
     * Get the RGB value for the specified definition.  Cascades through 
     * preferenceToSet, valuesToSet and finally the registry.
     * 
     * @param definition the <code>ColorDefinition</code>.
     * @return the <code>RGB</code> value.
     */
    private RGB getColorValue(ColorDefinition definition) {
        String id = definition.getId();
        RGB updatedRGB = (RGB) colorPreferencesToSet.get(id);
        if (updatedRGB == null) {
            updatedRGB = (RGB) colorValuesToSet.get(id);
            if (updatedRGB == null) {
				updatedRGB = currentTheme.getColorRegistry().getRGB(id);
			}
        }
        return updatedRGB;
    }

    /**
     * @return Return the default "No preview available." preview.
     */
    private Composite getDefaultPreviewControl() {
        if (defaultPreviewControl == null) {
            defaultPreviewControl = new Composite(previewComposite, SWT.NONE);
            defaultPreviewControl.setLayout(new FillLayout());
            Label l = new Label(defaultPreviewControl, SWT.LEFT);
            l.setText(RESOURCE_BUNDLE.getString("noPreviewAvailable")); //$NON-NLS-1$
            myApplyDialogFont(l);
        }
        return defaultPreviewControl;
    }

    /**
     * Get colors that descend from the provided color.
     * 
     * @param definition the ancestor <code>ColorDefinition</code>.
     * @return the ColorDefinitions that have the provided definition as their 
     * 		defaultsTo attribute.
     */
    private ColorDefinition[] getDescendantColors(ColorDefinition definition) {
        List list = new ArrayList(5);
        String id = definition.getId();

        ColorDefinition[] colors = themeRegistry.getColors();
        ColorDefinition[] sorted = new ColorDefinition[colors.length];
        System.arraycopy(colors, 0, sorted, 0, sorted.length);

        Arrays.sort(sorted, new IThemeRegistry.HierarchyComparator(colors));

        for (int i = 0; i < sorted.length; i++) {
            if (id.equals(sorted[i].getDefaultsTo())) {
				list.add(sorted[i]);
			}
        }

        return (ColorDefinition[]) list
                .toArray(new ColorDefinition[list.size()]);
    }

    private FontDefinition[] getDescendantFonts(FontDefinition definition) {
        List list = new ArrayList(5);
        String id = definition.getId();

        FontDefinition[] fonts = themeRegistry.getFonts();
        FontDefinition[] sorted = new FontDefinition[fonts.length];
        System.arraycopy(fonts, 0, sorted, 0, sorted.length);

        Arrays.sort(sorted, new IThemeRegistry.HierarchyComparator(fonts));

        for (int i = 0; i < sorted.length; i++) {
            if (id.equals(sorted[i].getDefaultsTo())) {
				list.add(sorted[i]);
			}
        }

        return (FontDefinition[]) list.toArray(new FontDefinition[list.size()]);
    }

    private FontDefinition getFontAncestor(FontDefinition definition) {
        String defaultsTo = definition.getDefaultsTo();
        if (defaultsTo == null) {
			return null;
		}

        return themeRegistry.findFont(defaultsTo);
    }

    private FontData[] getFontAncestorValue(FontDefinition definition) {
        FontDefinition ancestor = getFontAncestor(definition);
        if (ancestor == null) {
			return PreferenceConverter.getDefaultFontDataArray(
                    getPreferenceStore(), ThemeElementHelper
                            .createPreferenceKey(currentTheme, definition
                                    .getId()));
		}

        return getFontValue(ancestor);
    }

    protected FontData[] getFontValue(FontDefinition definition) {
        String id = definition.getId();
        FontData[] updatedFD = (FontData[]) fontPreferencesToSet.get(id);
        if (updatedFD == null) {
            updatedFD = (FontData[]) fontValuesToSet.get(id);
            if (updatedFD == null) {
				updatedFD = currentTheme.getFontRegistry().getFontData(id);
			}
        }
        return updatedFD;
    }

    protected ColorDefinition getSelectedColorDefinition() {
        Object o = ((IStructuredSelection) tree.getViewer().getSelection())
                .getFirstElement();
        if (o instanceof ColorDefinition) {
			return (ColorDefinition) o;
		}
        return null;
    }

    protected FontDefinition getSelectedFontDefinition() {
        Object o = ((IStructuredSelection) tree.getViewer().getSelection())
                .getFirstElement();
        if (o instanceof FontDefinition) {
			return (FontDefinition) o;
		}
        return null;
    }

    /**
     * Hook all control listeners.
     */
    private void hookListeners() {
        colorSelector.addListener(new IPropertyChangeListener() {

            /* (non-Javadoc)
             * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
             */
            public void propertyChange(PropertyChangeEvent event) {
                ColorDefinition definition = getSelectedColorDefinition();

                RGB newRGB = (RGB) event.getNewValue();
                if (definition != null && newRGB != null
                        && !newRGB.equals(event.getOldValue())) {
                    setColorPreferenceValue(definition, newRGB);
                    setRegistryValue(definition, newRGB);
                }

                updateColorControls(definition);
            }
        });

        TreeViewer viewer = tree.getViewer();
		viewer.addSelectionChangedListener(
                new ISelectionChangedListener() {

                    /* (non-Javadoc)
                     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
                     */
                    public void selectionChanged(SelectionChangedEvent event) {
                        updateTreeSelection(event.getSelection());
                    }
                });

        colorResetButton.addSelectionListener(new SelectionAdapter() {

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                ColorDefinition definition = getSelectedColorDefinition();
                if (resetColor(definition)) {
					updateColorControls(definition);
				}
            }
        });

        fontResetButton.addSelectionListener(new SelectionAdapter() {

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                FontDefinition definition = getSelectedFontDefinition();
                if (resetFont(definition)) {
					updateFontControls(definition);
				}
            }
        });

        fontChangeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	Display display = event.display;
            	editFont(display);
            }
        });

        fontSystemButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FontDefinition definition = getSelectedFontDefinition();
                if (definition != null) {
                    FontData[] defaultFontData = JFaceResources
                            .getDefaultFont().getFontData();
                    setFontPreferenceValue(definition, defaultFontData);
                    setRegistryValue(definition, defaultFontData);

                    updateFontControls(definition);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench aWorkbench) {
        this.workbench = (Workbench) aWorkbench;
        setPreferenceStore(PrefUtil.getInternalPreferenceStore());

        final IThemeManager themeManager = aWorkbench.getThemeManager();

        themeChangeListener = new IPropertyChangeListener() {

            /* (non-Javadoc)
             * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
             */
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
                        IThemeManager.CHANGE_CURRENT_THEME)) {
                    updateThemeInfo(themeManager);
                    refreshCategory();
                    tree.getViewer().refresh(); // refresh all the labels in the tree
                }
            }
        };
        themeManager.addPropertyChangeListener(themeChangeListener);

        updateThemeInfo(themeManager);
    }

    private void updateThemeInfo(IThemeManager manager) {
        clearPreviews();
        categoryMap.clear();

        if (labelProvider != null) {
			labelProvider.dispose(); // nuke the old cache
		}

        currentTheme = manager.getCurrentTheme();

        if (colorRegistry != null) {
			colorRegistry.dispose();
		}
        if (fontRegistry != null) {
			fontRegistry.dispose();
		}

        currentTheme = manager.getCurrentTheme();

        colorRegistry = new CascadingColorRegistry(currentTheme
                .getColorRegistry());
        fontRegistry = new CascadingFontRegistry(currentTheme.getFontRegistry());

        fontPreferencesToSet.clear();
        fontValuesToSet.clear();

        colorPreferencesToSet.clear();
        colorValuesToSet.clear();

        if (labelProvider != null) {
			labelProvider.hookListeners(); // rehook the listeners	    
		}
    }

    /**
     * Answers whether the definition is currently set to the default value.
     * 
     * @param definition the <code>ColorDefinition</code> to check.
     * @return Return whether the definition is currently mapped to the default 
     * 		value, either in the preference store or in the local change record 
     * 		of this preference page.
     */
    private boolean isDefault(ColorDefinition definition) {
        String id = definition.getId();

        if (colorPreferencesToSet.containsKey(id)) {
            if (definition.getValue() != null) { // value-based color
                if (colorPreferencesToSet.get(id).equals(definition.getValue())) {
					return true;
				}
            } else {
                if (colorPreferencesToSet.get(id).equals(
                        getColorAncestorValue(definition))) {
					return true;
				}
            }
        } else {
            if (definition.getValue() != null) { // value-based color
                if (getPreferenceStore().isDefault(
                        ThemeElementHelper
                                .createPreferenceKey(currentTheme, id))) {
					return true;
				}
            } else {
                // a descendant is default if it's the same value as its ancestor
                if (getColorValue(definition).equals(
                        getColorAncestorValue(definition))) {
					return true;
				}
            }
        }
        return false;
    }

    private boolean isDefault(FontDefinition definition) {
        String id = definition.getId();

        if (fontPreferencesToSet.containsKey(id)) {
            if (definition.getValue() != null) { // value-based font
                if (Arrays.equals((FontData[]) fontPreferencesToSet.get(id),
                        definition.getValue())) {
					return true;
				}
            } else {
                FontData[] ancestor = getFontAncestorValue(definition);
                if (Arrays.equals((FontData[]) fontPreferencesToSet.get(id),
                        ancestor)) {
					return true;
				}
            }
        } else {
            if (definition.getValue() != null) { // value-based font
                if (getPreferenceStore().isDefault(
                        ThemeElementHelper
                                .createPreferenceKey(currentTheme, id))) {
					return true;
				}
            } else {
                FontData[] ancestor = getFontAncestorValue(definition);
                if (ancestor == null) {
					return true;
				}

                // a descendant is default if it's the same value as its ancestor
                if (Arrays.equals(getFontValue(definition), ancestor)) {
					return true;
				}
            }
        }
        return false;
    }

    /**
     * Apply the dialog font to the control and store 
     * it for later so that it can be used for a later
     * update.
     * @param control
     */
    private void myApplyDialogFont(Control control) {
        control.setFont(JFaceResources.getDialogFont());
        dialogFontWidgets.add(control);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    protected void performApply() {
        super.performApply();

        //Apply the default font to the dialog.
        Font oldFont = appliedDialogFont;

        FontDefinition fontDefinition = themeRegistry
                .findFont(JFaceResources.DIALOG_FONT);
        if (fontDefinition == null) {
			return;
		}

        FontData[] newData = getFontValue(fontDefinition);

        appliedDialogFont = new Font(getControl().getDisplay(), newData);

        updateForDialogFontChange(appliedDialogFont);
        getApplyButton().setFont(appliedDialogFont);
        getDefaultsButton().setFont(appliedDialogFont);

        if (oldFont != null) {
			oldFont.dispose();
		}
    }

    private void performColorDefaults() {
        ColorDefinition[] definitions = themeRegistry.getColors();

        // apply defaults in depth-order.
        ColorDefinition[] definitionsCopy = new ColorDefinition[definitions.length];
        System
                .arraycopy(definitions, 0, definitionsCopy, 0,
                        definitions.length);

        Arrays.sort(definitionsCopy, new IThemeRegistry.HierarchyComparator(
                definitions));

        for (int i = 0; i < definitionsCopy.length; i++) {
			resetColor(definitionsCopy[i]);
		}

        updateColorControls(getSelectedColorDefinition());
    }

    private boolean performColorOk() {
        for (Iterator i = colorPreferencesToSet.keySet().iterator(); i
                .hasNext();) {
            String id = (String) i.next();
            String key = ThemeElementHelper.createPreferenceKey(currentTheme,
                    id);
            RGB rgb = (RGB) colorPreferencesToSet.get(id);
            String rgbString = StringConverter.asString(rgb);
            String storeString = getPreferenceStore().getString(key);

            if (!rgbString.equals(storeString)) {
                getPreferenceStore().setValue(key, rgbString);
            }
        }

        colorValuesToSet.clear();
        colorPreferencesToSet.clear();
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        performColorDefaults();
        performFontDefaults();
    }

    private void performFontDefaults() {
        FontDefinition[] definitions = themeRegistry.getFonts();

        // apply defaults in depth-order.
        FontDefinition[] definitionsCopy = new FontDefinition[definitions.length];
        System
                .arraycopy(definitions, 0, definitionsCopy, 0,
                        definitions.length);

        Arrays.sort(definitionsCopy, new IThemeRegistry.HierarchyComparator(
                definitions));

        for (int i = 0; i < definitionsCopy.length; i++) {
			resetFont(definitionsCopy[i]);
		}

        updateFontControls(getSelectedFontDefinition());
    }

    private boolean performFontOk() {
        for (Iterator i = fontPreferencesToSet.keySet().iterator(); i.hasNext();) {
            String id = (String) i.next();
            String key = ThemeElementHelper.createPreferenceKey(currentTheme,
                    id);
            FontData[] fd = (FontData[]) fontPreferencesToSet.get(id);

            String fdString = PreferenceConverter.getStoredRepresentation(fd);
            String storeString = getPreferenceStore().getString(key);

            if (!fdString.equals(storeString)) {
                getPreferenceStore().setValue(key, fdString);
            }
        }

        fontValuesToSet.clear();
        fontPreferencesToSet.clear();
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
    	saveTreeExpansion();
    	saveTreeSelection();
        boolean result =  performColorOk() && performFontOk();
        if(result) {
			PrefUtil.savePrefs();
		}
        return result;
    }

    /**
     * Refreshes the category.
     */
    private void refreshCategory() {
        updateColorControls(null);
        updateFontControls(null);
    }

    /**
     * Resets the supplied definition to its default value.
     * 
     * @param definition the <code>ColorDefinition</code> to reset.
     * @return whether any change was made.
     */
    private boolean resetColor(ColorDefinition definition) {
        if (!isDefault(definition)) {

            RGB newRGB;
            if (definition.getValue() != null) {
                newRGB = definition.getValue();
            } else {
                newRGB = getColorAncestorValue(definition);
            }

            if (newRGB != null) {
                setColorPreferenceValue(definition, newRGB);
                setRegistryValue(definition, newRGB);
                return true;
            }
        }
        return false;
    }

    protected boolean resetFont(FontDefinition definition) {
        if (!isDefault(definition)) {

            FontData[] newFD;
            if (definition.getDefaultsTo() != null) {
                newFD = getFontAncestorValue(definition);
            } else {
                newFD = PreferenceConverter.getDefaultFontDataArray(
                        getPreferenceStore(), ThemeElementHelper
                                .createPreferenceKey(currentTheme, definition
                                        .getId()));
            }

            if (newFD != null) {
                setFontPreferenceValue(definition, newFD);
                setRegistryValue(definition, newFD);
                return true;
            }
        }
        return false;
    }

    /**
     * Set the value (in preferences) for the given color.  
     * 
     * @param definition the <code>ColorDefinition</code> to set.
     * @param newRGB the new <code>RGB</code> value for the definitions 
     * 		identifier.
     */
    protected void setColorPreferenceValue(ColorDefinition definition,
            RGB newRGB) {
        setDescendantRegistryValues(definition, newRGB);
        colorPreferencesToSet.put(definition.getId(), newRGB);
    }

    /**
     * Set the value (in registry) for the given colors children.  
     * 
     * @param definition the <code>ColorDefinition</code> whose children should 
     * 		be set.
     * @param newRGB the new <code>RGB</code> value for the definitions 
     * 		identifier.
     */
    private void setDescendantRegistryValues(ColorDefinition definition,
            RGB newRGB) {
        ColorDefinition[] children = getDescendantColors(definition);

        for (int i = 0; i < children.length; i++) {
            if (isDefault(children[i])) {
                setDescendantRegistryValues(children[i], newRGB);
                setRegistryValue(children[i], newRGB);
                colorValuesToSet.put(children[i].getId(), newRGB);
            }
        }
    }

    private void setDescendantRegistryValues(FontDefinition definition,
            FontData[] datas) {
        FontDefinition[] children = getDescendantFonts(definition);

        for (int i = 0; i < children.length; i++) {
            if (isDefault(children[i])) {
                setDescendantRegistryValues(children[i], datas);
                setRegistryValue(children[i], datas);
                fontValuesToSet.put(children[i].getId(), datas);
            }
        }
    }

    protected void setFontPreferenceValue(FontDefinition definition,
            FontData[] datas) {
        setDescendantRegistryValues(definition, datas);
        fontPreferencesToSet.put(definition.getId(), datas);
    }

    /**
     * Updates the working registry.
     * @param definition
     * @param newRGB
     */
    protected void setRegistryValue(ColorDefinition definition, RGB newRGB) {
        colorRegistry.put(definition.getId(), newRGB);
    }

    protected void setRegistryValue(FontDefinition definition, FontData[] datas) {
        fontRegistry.put(definition.getId(), datas);
    }

    /**
     * Swap in the color selection controls.
     */
    protected void swapColorControls() {
        controlAreaLayout.topControl = colorControls;
        controlArea.layout();
    }

    /**
     * Swap in the font selection controls.
     */
    protected void swapFontControls() {
        controlAreaLayout.topControl = fontControls;
        controlArea.layout();
    }

    /**
     * Swap in no controls (empty the control area)
     */
    protected void swapNoControls() {
        controlAreaLayout.topControl = null;
        controlArea.layout();
    }

    /**
     * Set the color list.
     * @param category the category to use.
     */
    private void updateCategorySelection(ThemeElementCategory category) {

        Composite previewControl = (Composite) previewMap.get(category);
        if (previewControl == null) {
            if (category != null) {
                try {
                    IThemePreview preview = getThemePreview(category);
                    if (preview != null) {
                        previewControl = new Composite(previewComposite,
                                SWT.NONE);
                        previewControl.setLayout(new FillLayout());
                        ITheme theme = getCascadingTheme();
                        preview.createControl(previewControl, theme);
                        previewSet.add(preview);
                    }
                } catch (CoreException e) {
                    previewControl = new Composite(previewComposite, SWT.NONE);
                    previewControl.setLayout(new FillLayout());
                    myApplyDialogFont(previewControl);
                    Text error = new Text(previewControl, SWT.WRAP
                            | SWT.READ_ONLY);
                    error.setText(RESOURCE_BUNDLE
                            .getString("errorCreatingPreview")); //$NON-NLS-1$
                    WorkbenchPlugin
                            .log(
                                    RESOURCE_BUNDLE
                                            .getString("errorCreatePreviewLog"), StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e)); //$NON-NLS-1$
                }
            }
        }
        if (previewControl == null) {
            previewControl = getDefaultPreviewControl();
        }
        previewMap.put(category, previewControl);
        stackLayout.topControl = previewControl;
        previewComposite.layout();
    }

    /**
     * Returns the preview for the category.
     * @param category the category
     * @return the preview for the category, or its ancestors preview if it does not have one.
     */
    private IThemePreview getThemePreview(ThemeElementCategory category)
            throws CoreException {
        IThemePreview preview = category.createPreview();
        if (preview != null) {
			return preview;
		}

        if (category.getParentId() != null) {
            int idx = Arrays.binarySearch(themeRegistry.getCategories(),
                    category.getParentId(), IThemeRegistry.ID_COMPARATOR);
            if (idx >= 0) {
				return getThemePreview(themeRegistry.getCategories()[idx]);
			}
        }

        return null;
    }

    private ITheme getCascadingTheme() {
        if (cascadingTheme == null) {
			cascadingTheme = new CascadingTheme(currentTheme, colorRegistry,
                    fontRegistry);
		}
        return cascadingTheme;
    }

    /**
     * Update the color controls based on the supplied definition.
     * 
     * @param definition The currently selected <code>ColorDefinition</code>.
     */
    protected void updateColorControls(ColorDefinition definition) {
        if (definition == null) {
            colorResetButton.setEnabled(false);
            colorSelector.setEnabled(false);
            descriptionText.setText(""); //$NON-NLS-1$
            return;
        }

        colorSelector.setColorValue(getColorValue(definition));

        colorResetButton.setEnabled(!isDefault(definition));
        colorSelector.setEnabled(true);
        String description = definition.getDescription();
        descriptionText.setText(description == null ? "" : description); //$NON-NLS-1$		
    }

    protected void updateFontControls(FontDefinition definition) {
        if (definition == null) {
            fontSystemButton.setEnabled(false);
            fontResetButton.setEnabled(false);
            fontChangeButton.setEnabled(false);
            descriptionText.setText(""); //$NON-NLS-1$
            return;
        }

        fontSystemButton.setEnabled(true);
        fontResetButton.setEnabled(!isDefault(definition));
        fontChangeButton.setEnabled(true);
        String description = definition.getDescription();
        descriptionText.setText(description == null ? "" : description); //$NON-NLS-1$		
    }

    /**
     * Update for a change in the dialog font.
     * @param newFont
     */
    private void updateForDialogFontChange(Font newFont) {
        Iterator iterator = dialogFontWidgets.iterator();
        while (iterator.hasNext()) {
            ((Control) iterator.next()).setFont(newFont);
        }

        //recalculate the fonts for the tree
        labelProvider.clearFontCacheAndUpdate();
    }
    
    private void updateTreeSelection(ISelection selection) {
		if (selection.isEmpty()) {
		    swapNoControls();
		    updateColorControls(null);
		    updateCategorySelection(null);
		} else {
		    Object element = ((IStructuredSelection) selection).getFirstElement();
		    if (element instanceof ThemeElementCategory) {
		        swapNoControls();
		        String description = ((ThemeElementCategory) element)
		                .getDescription();
		        descriptionText
		                .setText(description == null ? "" : description); //$NON-NLS-1$
		        updateCategorySelection((ThemeElementCategory) element);
		    } else if (element instanceof ColorDefinition) {
		        updateColorControls((ColorDefinition) element);
		        swapColorControls();
		        updateCategorySelection(WorkbenchPlugin
		                .getDefault().getThemeRegistry()
		                .findCategory(
		                        ((ColorDefinition) element)
		                                .getCategoryId()));
		    } else if (element instanceof FontDefinition) {
		        updateFontControls((FontDefinition) element);
		        swapFontControls();
		        updateCategorySelection(WorkbenchPlugin
		                .getDefault().getThemeRegistry()
		                .findCategory(
		                        ((FontDefinition) element)
		                                .getCategoryId()));
		    }
		}
	}

    /**
	 * Restore the selection state of the tree.
	 * 
	 * @since 3.1
	 */
	private void restoreTreeSelection() {
		String selectedElementString = getPreferenceStore().getString(
				SELECTED_ELEMENT_PREF);

		if (selectedElementString == null) {
			return;
		}

		Object element = findElementFromMarker(selectedElementString);
		if (element == null) {
			return;
		}

		tree.getViewer().setSelection(new StructuredSelection(element), true);
	}

	/**
	 * Save the selection state of the tree.
	 *
	 * @since 3.1
	 */
	private void saveTreeSelection() {
		IStructuredSelection selection = (IStructuredSelection) tree
				.getViewer().getSelection();
		Object element = selection.getFirstElement();
		StringBuffer buffer = new StringBuffer();
		appendMarkerToBuffer(buffer, element);
		if (buffer.length() > 0) {
			buffer.append(((IThemeElementDefinition) element).getId());
		}
		getPreferenceStore().setValue(SELECTED_ELEMENT_PREF, buffer.toString());
	}

	/**
	 * Restore the expansion state of the tree.
	 * 
	 * @since 3.1
	 */
	private void restoreTreeExpansion() {
		String expandedElementsString = getPreferenceStore().getString(
				EXPANDED_ELEMENTS_PREF);
		if (expandedElementsString == null) {
			return;
		}

		String[] expandedElementIDs = Util.getArrayFromList(expandedElementsString, EXPANDED_ELEMENTS_TOKEN);
		if (expandedElementIDs.length == 0) {
			return;
		}

		List elements = new ArrayList(expandedElementIDs.length);
		for (int i = 0; i < expandedElementIDs.length; i++) {
			IThemeElementDefinition def = findElementFromMarker(expandedElementIDs[i]);

			if (def != null) {
				elements.add(def);
			}
		}
		tree.getViewer().setExpandedElements(elements.toArray());
	}

	/**
	 * Find the theme element from the given string. It will check the first
	 * character against the known constants and then call the appropriate
	 * method on the theme registry. If the element does not exist or the string
	 * is invalid <code>null</code> is returned.
	 * 
	 * @param string the string to parse
	 * @return the element, or <code>null</code>
	 */
	private IThemeElementDefinition findElementFromMarker(String string) {
		if (string.length() < 2) {
			return null;
		}

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
		List elementIds = new ArrayList(elements.length);

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			appendMarkerToBuffer(buffer, object);

			if (buffer.length() != 0) {
				buffer.append(((IThemeElementDefinition) object).getId());
				elementIds.add(buffer.toString());
			}
			buffer.setLength(0);
		}

		for (Iterator i = elementIds.iterator(); i.hasNext();) {
			String id = (String) i.next();
			buffer.append(id);
			if (i.hasNext()) {
				buffer.append(EXPANDED_ELEMENTS_TOKEN);
			}
		}

		getPreferenceStore()
				.setValue(EXPANDED_ELEMENTS_PREF, buffer.toString());
	}

	private void appendMarkerToBuffer(StringBuffer buffer, Object object) {
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
		final FontDefinition definition = getSelectedFontDefinition();
		if (definition != null) {
			final FontDialog fontDialog = new FontDialog(fontChangeButton
					.getShell());
			fontDialog.setFontList(getFontValue(definition));
			final FontData data = fontDialog.open();
			
			if (data != null) {
				setFontPreferenceValue(definition, fontDialog.getFontList());
				setRegistryValue(definition, fontDialog.getFontList());
			}

			updateFontControls(definition);
		}
	}
}
