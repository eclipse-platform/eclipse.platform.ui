/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;


/**
 * Configures Annotation preferences.
 *
 * @since 3.0
 */
class AnnotationsConfigurationBlock implements IPreferenceConfigurationBlock {
	private static final class ListItem {
		final String label;
		final Image image;
		final String colorKey;
		final String highlightKey;
		final String overviewRulerKey;
		final String textStyleKey;
		final String textKey;
		final String verticalRulerKey;

		ListItem(String label, Image image, String colorKey, String textKey, String overviewRulerKey, String highlightKey, String verticalRulerKey, String textStyleKey) {
			this.label= label;
			this.image= image;
			this.colorKey= colorKey;
			this.highlightKey= highlightKey;
			this.overviewRulerKey= overviewRulerKey;
			this.textKey= textKey;
			this.textStyleKey= textStyleKey;
			this.verticalRulerKey= verticalRulerKey;
		}
	}

	private static final class ItemContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return (ListItem[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private final class ItemLabelProvider extends LabelProvider implements IColorProvider {

		public String getText(Object element) {
			return ((ListItem) element).label;
		}


		public Image getImage(Object element) {
			ListItem item= (ListItem) element;
			if (item.verticalRulerKey != null && fStore.getBoolean(item.verticalRulerKey))
				return item.image;

			return null; // don't show icon if preference is not to show in vertical ruler
		}


		public Color getForeground(Object element) {
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			String key= ((ListItem) element).highlightKey;
			if (key != null && fStore.getBoolean(key)) {
				RGB color= PreferenceConverter.getColor(fStore, ((ListItem)element).colorKey);
				color= interpolate(color, new RGB(255, 255, 255), 0.6);
				return EditorsPlugin.getDefault().getSharedTextColors().getColor(color);
			}
			return null;
		}

		/**
		 * Returns a specification of a color that lies between the given
		 * foreground and background color using the given scale factor.
		 *
		 * @param fg the foreground color
		 * @param bg the background color
		 * @param scale the scale factor
		 * @return the interpolated color
		 */
		private RGB interpolate(RGB fg, RGB bg, double scale) {
			return new RGB(
				(int) ((1.0-scale) * fg.red + scale * bg.red),
				(int) ((1.0-scale) * fg.green + scale * bg.green),
				(int) ((1.0-scale) * fg.blue + scale * bg.blue)
			);
		}
	}

	private static class ArrayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((String[]) element)[0].toString();
		}
	}

	/* copied from DefaultMarkerAnnotationAccess */
	public static final String ERROR_SYSTEM_IMAGE= "error"; //$NON-NLS-1$
	public static final String WARNING_SYSTEM_IMAGE= "warning"; //$NON-NLS-1$
	public static final String INFO_SYSTEM_IMAGE= "info"; //$NON-NLS-1$
	public static final String TASK_SYSTEM_IMAGE= "task"; //$NON-NLS-1$
	public static final String BOOKMARK_SYSTEM_IMAGE= "bookmark"; //$NON-NLS-1$

	private final static Map MAPPING;

	static {
		MAPPING= new HashMap();
		MAPPING.put(ERROR_SYSTEM_IMAGE, ISharedImages.IMG_OBJS_ERROR_TSK);
		MAPPING.put(WARNING_SYSTEM_IMAGE, ISharedImages.IMG_OBJS_WARN_TSK);
		MAPPING.put(INFO_SYSTEM_IMAGE, ISharedImages.IMG_OBJS_INFO_TSK);
		MAPPING.put(TASK_SYSTEM_IMAGE, IDE.SharedImages.IMG_OBJS_TASK_TSK);
		MAPPING.put(BOOKMARK_SYSTEM_IMAGE, IDE.SharedImages.IMG_OBJS_BKMRK_TSK);
	}

	final static String[] HIGHLIGHT= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_HIGHLIGHT, "not used"}; //$NON-NLS-1$
	final static String[] UNDERLINE= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_UNDERLINE, AnnotationPreference.STYLE_UNDERLINE};
	final static String[] BOX= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_BOX, AnnotationPreference.STYLE_BOX};
	final static String[] IBEAM= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_IBEAM, AnnotationPreference.STYLE_IBEAM};
	final static String[] SQUIGGLES= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_SQUIGGLES, AnnotationPreference.STYLE_SQUIGGLES};



	private OverlayPreferenceStore fStore;
	private ColorSelector fAnnotationForegroundColorEditor;

	private Button fShowInTextCheckBox;
	private Button fShowInOverviewRulerCheckBox;
	private Button fShowInVerticalRulerCheckBox;

	private StructuredViewer fAnnotationTypeViewer;
	private final ListItem[] fListModel;

	private ComboViewer fDecorationViewer;
	private final Set fImageKeys= new HashSet();

	public AnnotationsConfigurationBlock(OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		MarkerAnnotationPreferences markerAnnotationPreferences= new MarkerAnnotationPreferences();
		fStore= store;
		fStore.addKeys(createOverlayStoreKeys(markerAnnotationPreferences));
		fListModel= createAnnotationTypeListModel(markerAnnotationPreferences);
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys(MarkerAnnotationPreferences preferences) {

		ArrayList overlayKeys= new ArrayList();
		Iterator e= preferences.getAnnotationPreferences().iterator();

		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getColorPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getTextPreferenceKey()));
			if (info.getHighlightPreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getHighlightPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getOverviewRulerPreferenceKey()));
			if (info.getVerticalRulerPreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getVerticalRulerPreferenceKey()));
			if (info.getTextStylePreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getTextStylePreferenceKey()));
		}
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {

		PixelConverter pixelConverter= new PixelConverter(parent);

		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		composite.setLayout(layout);

		Label label= new Label(composite, SWT.LEFT);
		label.setText(TextEditorMessages.AnnotationsConfigurationBlock_annotationPresentationOptions);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);

		fAnnotationTypeViewer= new TableViewer(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fAnnotationTypeViewer.setLabelProvider(new ItemLabelProvider());
		fAnnotationTypeViewer.setContentProvider(new ItemContentProvider());
		gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(20);
		fAnnotationTypeViewer.getControl().setLayoutData(gd);

		Composite optionsComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		optionsComposite.setLayout(layout);
		optionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label= new Label(optionsComposite, SWT.LEFT);
		label.setText(TextEditorMessages.AnnotationsConfigurationBlock_labels_showIn);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        label.setLayoutData(gd);

		// we only allow to set either "show in text" or "highlight in text", but not both

		fShowInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInTextCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_showInText);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalIndent= 20;
		fShowInTextCheckBox.setLayoutData(gd);

		fDecorationViewer= new ComboViewer(optionsComposite, SWT.READ_ONLY);
		fDecorationViewer.setContentProvider(new ArrayContentProvider());
		fDecorationViewer.setLabelProvider(new ArrayLabelProvider());
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		fDecorationViewer.getControl().setLayoutData(gd);
		fDecorationViewer.setInput(new Object[] {HIGHLIGHT, SQUIGGLES, BOX, UNDERLINE, IBEAM});

		fShowInOverviewRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
        fShowInOverviewRulerCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_showInOverviewRuler);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 20;
		fShowInOverviewRulerCheckBox.setLayoutData(gd);

		fShowInVerticalRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInVerticalRulerCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_showInVerticalRuler);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 20;
		fShowInVerticalRulerCheckBox.setLayoutData(gd);

		label= new Label(optionsComposite, SWT.LEFT);
		label.setText(TextEditorMessages.AnnotationsConfigurationBlock_color);
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalIndent= 20;
		label.setLayoutData(gd);

		fAnnotationForegroundColorEditor= new ColorSelector(optionsComposite);
		Button foregroundColorButton= fAnnotationForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAnnotationTypeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleAnnotationListSelection();
			}
		});

		fShowInTextCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				final boolean value= fShowInTextCheckBox.getSelection();
				if (value) {
					// enable whatever is in the combo
					String[] decoration= (String[]) ((IStructuredSelection) fDecorationViewer.getSelection()).getFirstElement();
					if (HIGHLIGHT.equals(decoration))
						fStore.setValue(item.highlightKey, true);
					else
						fStore.setValue(item.textKey, true);
				} else {
					// disable both
					if (item.textKey != null)
						fStore.setValue(item.textKey, false);
					if (item.highlightKey != null)
						fStore.setValue(item.highlightKey, false);
				}
				fStore.setValue(item.textKey, value);
				updateDecorationViewer(item, false);
				fAnnotationTypeViewer.refresh(item);
			}
		});

		fShowInOverviewRulerCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				fStore.setValue(item.overviewRulerKey, fShowInOverviewRulerCheckBox.getSelection());
				fAnnotationTypeViewer.refresh(item);
			}
		});

		fShowInVerticalRulerCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				fStore.setValue(item.verticalRulerKey, fShowInVerticalRulerCheckBox.getSelection());
				fAnnotationTypeViewer.refresh(item);
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				PreferenceConverter.setValue(fStore, item.colorKey, fAnnotationForegroundColorEditor.getColorValue());
				fAnnotationTypeViewer.refresh(item);
			}
		});

		fDecorationViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			/*
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				String[] decoration= (String[]) ((IStructuredSelection) fDecorationViewer.getSelection()).getFirstElement();
				ListItem item= getSelectedItem();

				if (fShowInTextCheckBox.getSelection()) {
					if (HIGHLIGHT.equals(decoration)) {
						fStore.setValue(item.highlightKey, true);
						if (item.textKey != null) {
							fStore.setValue(item.textKey, false);
							if (item.textStyleKey != null)
								fStore.setValue(item.textStyleKey, AnnotationPreference.STYLE_NONE);
						}
					} else {
						if (item.highlightKey != null)
							fStore.setValue(item.highlightKey, false);
						if (item.textKey != null) {
							fStore.setValue(item.textKey, true);
							if (item.textStyleKey != null)
								fStore.setValue(item.textStyleKey, decoration[1]);
						}
					}
				}

				fAnnotationTypeViewer.refresh(item);
			}
		});

		composite.layout();
		return composite;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#canPerformOk()
	 */
	public boolean canPerformOk() {
		return true;
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public void performOk() {
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	public void performDefaults() {
		fStore.loadDefaults();
		fAnnotationTypeViewer.refresh();
		handleAnnotationListSelection();
	}

	private void handleAnnotationListSelection() {
		ListItem item= getSelectedItem();

		RGB rgb= PreferenceConverter.getColor(fStore, item.colorKey);
		fAnnotationForegroundColorEditor.setColorValue(rgb);

		boolean highlight= item.highlightKey == null ? false : fStore.getBoolean(item.highlightKey);
		boolean showInText = item.textKey == null ? false : fStore.getBoolean(item.textKey);
		fShowInTextCheckBox.setSelection(showInText || highlight);

		updateDecorationViewer(item, true);

		fShowInOverviewRulerCheckBox.setSelection(fStore.getBoolean(item.overviewRulerKey));

		if (item.verticalRulerKey != null) {
			fShowInVerticalRulerCheckBox.setSelection(fStore.getBoolean(item.verticalRulerKey));
			fShowInVerticalRulerCheckBox.setEnabled(true);
		} else {
			fShowInVerticalRulerCheckBox.setSelection(true);
			fShowInVerticalRulerCheckBox.setEnabled(false);
		}
	}



	public void initialize() {

		fAnnotationTypeViewer.setInput(fListModel);
		fAnnotationTypeViewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAnnotationTypeViewer != null && !fAnnotationTypeViewer.getControl().isDisposed()) {
					fAnnotationTypeViewer.setSelection(new StructuredSelection(fListModel[0]));
				}
			}
		});

	}

	private ListItem[] createAnnotationTypeListModel(MarkerAnnotationPreferences preferences) {
		ArrayList listModelItems= new ArrayList();
		Iterator e= preferences.getAnnotationPreferences().iterator();

		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			if (info.isIncludeOnPreferencePage()) {
				String label= info.getPreferenceLabel();
				if (containsMoreThanOne(preferences.getAnnotationPreferences().iterator(), label))
					label += " (" + info.getAnnotationType() + ")";  //$NON-NLS-1$//$NON-NLS-2$

				Image image= getImage(info);

				listModelItems.add(new ListItem(label, image, info.getColorPreferenceKey(), info.getTextPreferenceKey(), info.getOverviewRulerPreferenceKey(), info.getHighlightPreferenceKey(), info.getVerticalRulerPreferenceKey(), info.getTextStylePreferenceKey()));
			}
		}

		Comparator comparator= new Comparator() {
			/*
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				if (!(o2 instanceof ListItem))
					return -1;
				if (!(o1 instanceof ListItem))
					return 1;

				String label1= ((ListItem)o1).label;
				String label2= ((ListItem)o2).label;

				return Collator.getInstance().compare(label1, label2);

			}
		};
		Collections.sort(listModelItems, comparator);

		ListItem[] items= new ListItem[listModelItems.size()];
		listModelItems.toArray(items);
		return items;
	}

	/**
	 * Returns the image for the given annotation and the given annotation preferences or
	 * <code>null</code> if there is no such image.
	 *
	 * @param preference the annotation preference
	 * @return the image or <code>null</code>
	 * @since 3.1
	 */
	private Image getImage(AnnotationPreference preference) {

		ImageRegistry registry= EditorsPlugin.getDefault().getImageRegistry();

		String annotationType= (String) preference.getAnnotationType();
		if (annotationType == null)
			return null;

		String customImage= annotationType + "__AnnotationsConfigurationBlock_Image"; //$NON-NLS-1$

		Image image;
		image= registry.get(customImage);
		if (image != null)
			return image;

		image= registry.get(annotationType);
		if (image == null) {
			ImageDescriptor descriptor= preference.getImageDescriptor();
			if (descriptor != null) {
				registry.put(annotationType, descriptor);
				image= registry.get(annotationType);
			} else {
				String key= translateSymbolicImageName(preference.getSymbolicImageName());
				if (key != null) {
					ISharedImages sharedImages= PlatformUI.getWorkbench().getSharedImages();
					image= sharedImages.getImage(key);
				}
			}
		}

		if (image == null)
			return image;

		// create custom image
		final int SIZE= 16; // square images
		ImageData data= image.getImageData();
		Image copy;
		if (data.height > SIZE || data.width > SIZE) {
			// scale down to icon size
			copy= new Image(Display.getCurrent(), data.scaledTo(SIZE, SIZE));
		} else {
			// don't scale up, but rather copy into the middle and mark everything else transparent
			ImageData mask= data.getTransparencyMask();
			ImageData resized= new ImageData(SIZE, SIZE, data.depth, data.palette);
			ImageData resizedMask= new ImageData(SIZE, SIZE, mask.depth, mask.palette);

			int xo= Math.max(0, (SIZE - data.width) / 2);
			int yo= Math.max(0, (SIZE - data.height) / 2);

			for (int y= 0; y < SIZE; y++) {
				for (int x= 0; x < SIZE; x++) {
					if (y >= yo && x >= xo && y < yo + data.height && x < xo + data.width) {
						resized.setPixel(x, y, data.getPixel(x - xo, y - yo));
						resizedMask.setPixel(x, y, mask.getPixel(x - xo, y - yo));
					}
				}
			}

			copy= new Image(Display.getCurrent(), resized, resizedMask);
		}

		fImageKeys.add(customImage);
		registry.put(customImage, copy);
		return copy;
	}

	/**
	 * Translates the given symbolic image name into the according symbolic image name
	 * the {@link org.eclipse.ui.ISharedImages} understands.
	 *
	 * @param symbolicImageName the symbolic system image name to be translated
	 * @return the shared image name
	 * @since 3.1
	 */
	private String translateSymbolicImageName(String symbolicImageName) {
		return (String) MAPPING.get(symbolicImageName);
	}

	private boolean containsMoreThanOne(Iterator annotationPrefernceIterator, String label) {
		if (label == null)
			return false;

		int count= 0;
		while (annotationPrefernceIterator.hasNext()) {
			if (label.equals(((AnnotationPreference)annotationPrefernceIterator.next()).getPreferenceLabel()))
				count++;

			if (count == 2)
				return true;
		}
		return false;
	}

	/*
	 * @see IPreferenceConfigurationBlock#dispose()
	 */
	public void dispose() {
		ImageRegistry registry= EditorsPlugin.getDefault().getImageRegistry();

		for (Iterator it= fImageKeys.iterator(); it.hasNext();) {
			String string= (String) it.next();
			registry.remove(string);
		}

		fImageKeys.clear();
	}

	private ListItem getSelectedItem() {
		return (ListItem) ((IStructuredSelection) fAnnotationTypeViewer.getSelection()).getFirstElement();
	}

	private void updateDecorationViewer(ListItem item, boolean changed) {
		// decoration selection: if the checkbox is enabled, there is
		// only one case where the combo is not enabled: if both the highlight and textStyle keys are null
		final boolean enabled= fShowInTextCheckBox.getSelection() && !(item.highlightKey == null && item.textStyleKey == null);
		fDecorationViewer.getControl().setEnabled(enabled);

		if (changed) {
			String[] selection= null;
			ArrayList list= new ArrayList();

			// highlighting
			if (item.highlightKey != null) {
				list.add(HIGHLIGHT);
				if (fStore.getBoolean(item.highlightKey))
					selection= HIGHLIGHT;
			}

			// legacy default= squiggly lines
			list.add(SQUIGGLES);

			// advanced styles
			if (item.textStyleKey != null) {
				list.add(UNDERLINE);
				list.add(BOX);
				list.add(IBEAM);
			}

			// set selection
			if (selection == null) {
				String val= item.textStyleKey == null ? SQUIGGLES[1] : fStore.getString(item.textStyleKey);
				for (Iterator iter= list.iterator(); iter.hasNext();) {
					String[] elem= (String[]) iter.next();
					if (elem[1].equals(val)) {
						selection= elem;
						break;
					}
				}
			}

			fDecorationViewer.setInput(list.toArray(new Object[list.size()]));
			if (selection != null)
				fDecorationViewer.setSelection(new StructuredSelection((Object) selection), true);
		}
	}
}
