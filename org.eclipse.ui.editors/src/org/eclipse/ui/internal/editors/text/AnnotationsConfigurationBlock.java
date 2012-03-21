/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.icu.text.Collator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
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

		final String isNextPreviousNavigationKey;

		ListItem(String label, Image image, String colorKey, String textKey, String overviewRulerKey, String highlightKey, String verticalRulerKey, String textStyleKey, String navigationKey) {
			this.label= label;
			this.image= image;
			this.colorKey= colorKey;
			this.highlightKey= highlightKey;
			this.overviewRulerKey= overviewRulerKey;
			this.textKey= textKey;
			this.textStyleKey= textStyleKey;
			this.verticalRulerKey= verticalRulerKey;
			this.isNextPreviousNavigationKey= navigationKey;
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


	private final class ItemLabelProvider extends LabelProvider {

		public String getText(Object element) {
			return ((ListItem)element).label;
		}

		public Image getImage(Object element) {
			ListItem item= (ListItem)element;
			return item.image;
		}
	}


	private static class ArrayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((String[]) element)[0].toString();
		}
	}

	final static String[] HIGHLIGHT= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_HIGHLIGHT, "not used"}; //$NON-NLS-1$
	final static String[] UNDERLINE= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_UNDERLINE, AnnotationPreference.STYLE_UNDERLINE};
	final static String[] BOX= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_BOX, AnnotationPreference.STYLE_BOX};
	final static String[] DASHED_BOX= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_DASHED_BOX, AnnotationPreference.STYLE_DASHED_BOX};
	final static String[] IBEAM= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_IBEAM, AnnotationPreference.STYLE_IBEAM};
	final static String[] SQUIGGLES= new String[] {TextEditorMessages.AnnotationsConfigurationBlock_SQUIGGLES, AnnotationPreference.STYLE_SQUIGGLES};
	final static String[] PROBLEM_UNDERLINE= new String[] { TextEditorMessages.AnnotationsConfigurationBlock_PROBLEM_UNDERLINE, AnnotationPreference.STYLE_PROBLEM_UNDERLINE };



	private OverlayPreferenceStore fStore;
	private ColorSelector fAnnotationForegroundColorEditor;

	private Button fShowInTextCheckBox;
	private Button fShowInOverviewRulerCheckBox;
	private Button fShowInVerticalRulerCheckBox;

	private Button fIsNextPreviousTargetCheckBox;

	private StructuredViewer fAnnotationTypeViewer;
	private final ListItem[] fListModel;

	private ComboViewer fDecorationViewer;
	private final Set fImageKeys= new HashSet();

	public AnnotationsConfigurationBlock(OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		MarkerAnnotationPreferences markerAnnotationPreferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
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
			if (info.getIsGoToNextNavigationTargetKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getIsGoToNextNavigationTargetKey()));
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
		layout.marginHeight= 0;
		layout.marginWidth= 0;
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

		fShowInVerticalRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInVerticalRulerCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_showInVerticalRuler);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 20;
		fShowInVerticalRulerCheckBox.setLayoutData(gd);

		fShowInOverviewRulerCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInOverviewRulerCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_showInOverviewRuler);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 20;
		fShowInOverviewRulerCheckBox.setLayoutData(gd);

		fShowInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInTextCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_showInText);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalIndent= 20;
		fShowInTextCheckBox.setLayoutData(gd);


		fDecorationViewer= new ComboViewer(optionsComposite, SWT.READ_ONLY);
		fDecorationViewer.setContentProvider(new ArrayContentProvider());
		fDecorationViewer.setLabelProvider(new ArrayLabelProvider());
		fDecorationViewer.setComparator(new ViewerComparator(Collator.getInstance()));

		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		fDecorationViewer.getControl().setLayoutData(gd);
		fDecorationViewer.setInput(new Object[] { HIGHLIGHT, SQUIGGLES, BOX, DASHED_BOX, UNDERLINE, PROBLEM_UNDERLINE, IBEAM });
		fDecorationViewer.getCombo().setVisibleItemCount(fDecorationViewer.getCombo().getItemCount());

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

		addFiller(optionsComposite);

		fIsNextPreviousTargetCheckBox= new Button(optionsComposite, SWT.CHECK);
		fIsNextPreviousTargetCheckBox.setText(TextEditorMessages.AnnotationsConfigurationBlock_isNavigationTarget);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 0;
		fIsNextPreviousTargetCheckBox.setLayoutData(gd);


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

		fIsNextPreviousTargetCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				fStore.setValue(item.isNextPreviousNavigationKey, fIsNextPreviousTargetCheckBox.getSelection());
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

	private void addFiller(Composite composite) {
		PixelConverter pixelConverter= new PixelConverter(composite);

		Label filler= new Label(composite, SWT.LEFT);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	/**
	 * Applies the given data.
	 *
	 * @param data the annotation type to select in the list or <code>null</code>
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#applyData(java.lang.Object)
	 * @since 3.4
	 */
	public void applyData(Object data) {
		if (!(data instanceof String))
			return;

		for (int i= 0; i < fListModel.length; i++) {
			final ListItem element= fListModel[i];
			if (data.equals(element.label)) {
				final Control control= fAnnotationTypeViewer.getControl();
				control.getDisplay().asyncExec(new Runnable() {
					public void run() {
						control.setFocus();
						fAnnotationTypeViewer.setSelection(new StructuredSelection(element), true);
					}
				});
				return;
			}
		}
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

		if (item.isNextPreviousNavigationKey != null) {
			fIsNextPreviousTargetCheckBox.setEnabled(true);
			fIsNextPreviousTargetCheckBox.setSelection(fStore.getBoolean(item.isNextPreviousNavigationKey));
		} else {
			fIsNextPreviousTargetCheckBox.setEnabled(false);
			fIsNextPreviousTargetCheckBox.setSelection(false);
		}

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

				listModelItems.add(new ListItem(label, image, info.getColorPreferenceKey(), info.getTextPreferenceKey(), info.getOverviewRulerPreferenceKey(), info.getHighlightPreferenceKey(), info
						.getVerticalRulerPreferenceKey(), info.getTextStylePreferenceKey(), info.getIsGoToNextNavigationTargetKey()));
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
			AnnotationPreference delegatingPreference= EditorsPlugin.getDefault().getAnnotationPreferenceLookup().getAnnotationPreference(annotationType);
			ImageDescriptor descriptor= delegatingPreference.getImageDescriptor();
			if (descriptor != null) {
				registry.put(annotationType, descriptor);
				image= registry.get(annotationType);
			} else {
				String symbolicImageName= preference.getSymbolicImageName();
				if (symbolicImageName != null) {
					String key= DefaultMarkerAnnotationAccess.getSharedImageName(symbolicImageName);
					if (key != null) {
						ISharedImages sharedImages= PlatformUI.getWorkbench().getSharedImages();
						image= sharedImages.getImage(key);
					}
				}
			}
		}

		if (image == null)
			return null;

		// create custom image
		final int SIZE= 16; // square images
		ImageData data= image.getImageData();
		Image copy;
		if (data.height > SIZE || data.width > SIZE) {
			// scale down to icon size
			copy= new Image(Display.getCurrent(), data.scaledTo(SIZE, SIZE));

		} else if (data.height == SIZE && data.width == SIZE) {
			// nothing to scale, return the image
			return image;

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
				list.add(PROBLEM_UNDERLINE);
				list.add(BOX);
				list.add(DASHED_BOX);
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
