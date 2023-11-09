/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Marco Descher <marco@descher.at> - Bug 422465
 * Steven Spungin <steven@spungin.tv> - Bug 437951, Bug 439709
 * Olivier Prouvost <olivier.prouvost@opcoach.com> Bug 403583, 472658
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.tools.emf.ui.common.AbstractElementEditorContribution;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.tools.emf.ui.internal.common.properties.ProjectOSGiTranslationProvider;
import org.eclipse.e4.tools.services.IClipboardService.Handler;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.impl.ResourceBundleTranslationProvider;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 * @param <M> type of the master object
 */
public abstract class AbstractComponentEditor<M> {
	private static final String GREY_SUFFIX = "Grey"; //$NON-NLS-1$

	private static final String CSS_CLASS_KEY = "org.eclipse.e4.ui.css.CssClassName"; //$NON-NLS-1$

	private static final int MAX_IMG_SIZE = 16;

	private final WritableValue<M> master = new WritableValue<>();

	public static final int SEARCH_IMAGE = 0;
	public static final int TABLE_ADD_IMAGE = 1;
	public static final int TABLE_DELETE_IMAGE = 2;
	public static final int ARROW_UP = 3;
	public static final int ARROW_DOWN = 4;

	protected static final int VERTICAL_LIST_WIDGET_INDENT = 10;

	private final List<Image> createdImages = new ArrayList<>();

	@Inject
	private EditingDomain editingDomain;
	@Inject
	private ModelEditor editor;
	@Inject
	public IResourcePool resourcePool;

	@Inject
	@Optional
	protected IProject project;

	@Inject
	@Translation
	protected Messages Messages;

	@Inject
	@Optional
	private ProjectOSGiTranslationProvider translationProvider;


	private Composite editorControl;

	private IdGenerator generator;

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public ModelEditor getEditor() {
		return editor;
	}

	public WritableValue<M> getMaster() {
		return master;
	}

	protected void setElementId(Object element) {
		if (getEditor().isAutoCreateElementId() && element instanceof MApplicationElement) {
			final MApplicationElement el = (MApplicationElement) element;
			if (el.getElementId() == null || el.getElementId().trim().length() == 0) {
				el.setElementId(Util.getDefaultElementId(((EObject) getMaster().getValue()).eResource(), el,
						getEditor().getProject()));
			}
		}
	}

	public Image createImage(String key) {
		return resourcePool.getImageUnchecked(key);
	}

	public ImageDescriptor createImageDescriptor(String key) {
		if (key == null) {
			return null;
		}
		return ImageDescriptor.createFromImage(createImage(key));
	}

	private ImageRegistry getComponentImages() {
		return editor.getComponentImages();
	}

	/**
	 * Get the image described in element if this is a MUILabel
	 *
	 * @param element the element in tree to be displayed
	 * @return image of element if iconUri is not empty (returns bad image if bad
	 *         URI), else returns null
	 */
	public Image getImageFromIconURI(MUILabel element) {

		Image img = null;
		// Returns only an image if there is a non empty Icon URI
		final String iconUri = element.getIconURI();
		if (iconUri != null && iconUri.trim().length() > 0) {
			final boolean greyVersion = shouldBeGrey(element);
			// Is this image already loaded ?
			img = getImage(iconUri, greyVersion);
			if (img == null) {
				// No image registered yet in ImageRegistry...
				final ImageDescriptor desc = getImageDescriptorFromUri(iconUri);

				// Can now add this image in the image registry
				getComponentImages().put(iconUri, desc);
				img = getImage(iconUri, greyVersion);
			}
		}

		return img;
	}

	/** @return true if the image of this element should be displayed in grey */
	private boolean shouldBeGrey(Object element) {
		// It is grey if a MUIElement is not visible or not rendered
		// It is not grey if this is not a MUIElement or if it is rendered and
		// visible.
		return element instanceof MUIElement
				&& !(((MUIElement) element).isToBeRendered() && ((MUIElement) element).isVisible());
	}

	/**
	 *
	 * @param key  the key of image (can be a constants from ResourceProvider or a
	 *             platform:/ uri location
	 * @param grey if true returns the grey version if original image exists
	 * @return the image with a give key or grey version.
	 */
	private Image getImage(String key, boolean grey) {

		// try to get image directly with right key and grey value
		Image result = getComponentImages().get(key + (grey ? GREY_SUFFIX : "")); //$NON-NLS-1$

		// may be image not yet created
		if (result == null) {
			result = getComponentImages().get(key);
			// If no image found, ask the resource pool to create it...
			if (result == null && !key.startsWith("platform:")) { //$NON-NLS-1$
				try {
					result = createImage(key);
				} catch (final Exception e) {
				}
				if (result != null) {
					getComponentImages().put(key, result);
				}
			}

			// Create the grey version of image and put it in registry
			if (result != null && grey) {
				final Image greyImg = new Image(result.getDevice(), result, SWT.IMAGE_GRAY);
				getComponentImages().put(key + GREY_SUFFIX, greyImg);
				result = greyImg;
			}
		}
		return result;
	}

	/**
	 * Get image from an element Implements algorithm described in bug #465271
	 *
	 * @param element the Application Element
	 * @param key     the element image key if no icon URI
	 * @return Image or null if nothing found
	 */
	public Image getImage(Object element, String key) {
		Image result = null;

		if (element instanceof MUILabel) {
			result = getImageFromIconURI((MUILabel) element);
		}

		if (result == null) {
			// This is a model element with a key or a MUILabel without IconUri
			final boolean greyVersion = shouldBeGrey(element);
			result = getImage(key, greyVersion);
		}

		return result;

	}

	/**
	 * Create a readable ImageDescriptor behind URI.
	 *
	 * @param uri
	 * @return
	 */
	private ImageDescriptor getImageDescriptorFromUri(String uri) {
		ImageDescriptor result = null;

		URL url = findPlatformImage(uri);

		if (url != null) {
			ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
			Image scaled = Util.scaleImage(imageDesc.createImage(), MAX_IMG_SIZE);
			createdImages.add(scaled);
			result = ImageDescriptor.createFromImage(scaled);
		}

		return result;
	}

	@SuppressWarnings("resource")
	private static URL findPlatformImage(String uri) {
		// SEVERAL CASES are possible here :
		// * uri = platform:/plugin/myplugin/icons/image.gif
		// * uri = platform:/resource/myplugin/icons/image.gif
		// * uri : platform:/plugin/myplugin/$nl$/icons/image.gif

		// We must check if file exists before creating the ImageDescriptor
		// because ImageRegistry will throw and print a DeviceResourceException
		// With the E4 editors, the platform:/plugin/ is set for
		// runtime, but the file can be in workspace during development In this
		// case, we must rather use platform:/resource/.
		// Used ideas from the ImageTooltip code around line 70 to fix this

		InputStream stream = null;
		URL url = null;

		try {
			final URL uri2url = new URL(uri);
			url = FileLocator.toFileURL(uri2url);
			stream = url.openStream();
		} catch (final IOException e) {
			// If no stream behind this URL, it is probably a platform:/plugin
			// which must be found as a platform:/resource (this case occurs in
			// the model editor when icon URI are set using the dialog)
			url = null;
			if (uri.startsWith("platform:/plugin")) //$NON-NLS-1$
			{
				try {
					// Try to get it using 'platform:/resource'
					final URL resUrl = new URL(uri.replace("platform:/plugin", "platform:/resource")); //$NON-NLS-1$//$NON-NLS-2$
					url = FileLocator.toFileURL(resUrl);
					stream = url.openStream();

				} catch (final IOException e2) {
					// No file behind, may be this is a $nl$ or a $ws$ path..
					// must use find on FileLocator which does not deal with
					// platform:/resource !
					try {
						url = FileLocator.find(new URL(uri));
						stream = url != null ? url.openStream() : null;
					} catch (final IOException ex) {
						url = null;
						// Can't do more !
					}
				}
			}
		}

		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException ex) {
			}
		}
		return url;
	}

	public Image getImage(Object element) {
		return null;
	}


	public abstract String getLabel(Object element);

	public abstract String getDetailLabel(Object element);

	public abstract String getDescription(Object element);

	public Composite getEditor(Composite parent, Object object) {
		if (generator != null) {
			generator.stopGenerating();
			generator = null;
		}
		editorControl = doGetEditor(parent, object);
		return editorControl;
	}

	protected abstract Composite doGetEditor(Composite parent, Object object);

	public abstract IObservableList<?> getChildList(Object element);

	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {};
	}

	public List<Action> getActions(Object element) {
		return Collections.emptyList();
	}

	/**
	 * Translates an input <code>String</code> using the current
	 * {@link ResourceBundleTranslationProvider} and <code>locale</code> from the
	 * {@link TranslationService}.
	 *
	 * @param string the string to translate, may not be null.
	 * @return the translated string or the input string if it could not be
	 *         translated.
	 */
	public String translate(String string) {
		return ControlFactory.tr(translationProvider, string);
	}

	/**
	 * @param element
	 * @return the list of actions that are populated in the import menu. Can be
	 *         empty but is never null.
	 */
	public List<Action> getActionsImport(Object element) {
		return Collections.emptyList();
	}

	protected String getLocalizedLabel(MUILabel element) {
		return ControlFactory.getLocalizedLabel(translationProvider, element);
	}

	private boolean isFocusChild(Control control) {
		Control c = control;
		while (c != null && c != editorControl) {
			c = c.getParent();
		}
		return c != null;
	}

	public void handleCopy() {
		if (editorControl != null) {
			final Control focusControl = editorControl.getDisplay().getFocusControl();

			if (isFocusChild(focusControl) && focusControl.getData(ControlFactory.COPY_HANDLER) != null) {
				((Handler) focusControl.getData(ControlFactory.COPY_HANDLER)).copy();
			}
		}
	}

	public void handlePaste() {
		if (editorControl != null) {
			final Control focusControl = editorControl.getDisplay().getFocusControl();

			if (isFocusChild(focusControl) && focusControl.getData(ControlFactory.COPY_HANDLER) != null) {
				((Handler) focusControl.getData(ControlFactory.COPY_HANDLER)).paste();
			}
		}
	}

	public void handleCut() {
		if (editorControl != null) {
			final Control focusControl = editorControl.getDisplay().getFocusControl();

			if (isFocusChild(focusControl) && focusControl.getData(ControlFactory.COPY_HANDLER) != null) {
				((Handler) focusControl.getData(ControlFactory.COPY_HANDLER)).cut();
			}
		}
	}

	protected Composite createScrollableContainer(Composite parent) {
		final ScrolledComposite scrolling = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolling.setBackgroundMode(SWT.INHERIT_DEFAULT);
		scrolling.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$

		final Composite contentContainer = new Composite(scrolling, SWT.NONE);

		contentContainer.setData(CSS_CLASS_KEY, "formContainer"); //$NON-NLS-1$
		scrolling.setExpandHorizontal(true);
		scrolling.setExpandVertical(true);
		scrolling.setContent(contentContainer);

		scrolling.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				final Rectangle r = scrolling.getClientArea();
				scrolling.setMinSize(contentContainer.computeSize(r.width, SWT.DEFAULT));
			}
		});

		scrolling.setLayoutData(new GridData(GridData.FILL_BOTH));

		final GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 10;
		contentContainer.setLayout(gl);

		return contentContainer;
	}

	protected void createContributedEditorTabs(CTabFolder folder, EMFDataBindingContext context,
			WritableValue<M> master, Class<? super M> clazz) {
		final List<AbstractElementEditorContribution> contributionList = editor.getTabContributionsForClass(clazz);

		for (final AbstractElementEditorContribution eec : contributionList) {
			final CTabItem item = new CTabItem(folder, SWT.BORDER);
			item.setText(eec.getTabLabel());

			final Composite parent = createScrollableContainer(folder);
			item.setControl(parent.getParent());

			eec.createContributedEditorTab(parent, context, master, getEditingDomain(), project);
		}

	}

	/**
	 * Generates an ID when the another field changes. Must be called after master
	 * is set with the objects value.
	 *
	 * @param attSource The source attribute
	 * @param attId     The id attribute to generate
	 * @param control   optional control to disable generator after losing focus or
	 *                  disposing
	 */
	protected void enableIdGenerator(EAttribute attSource, EAttribute attId, Control control) {
		if (generator != null) {
			generator.stopGenerating();
			generator = null;
		}
		if (getEditor().isAutoCreateElementId()) {
			generator = new IdGenerator();
			@SuppressWarnings("unchecked")
			IValueProperty<M, String> addSourceProp = EMFEditProperties.value(getEditingDomain(), attSource);
			@SuppressWarnings("unchecked")
			IValueProperty<M, String> attIdProp = EMFEditProperties.value(getEditingDomain(), attId);
			generator.bind(getMaster(), addSourceProp, attIdProp, control);
		}
	}

	@PreDestroy
	public void dispose() {
		createdImages.stream().filter(Objects::nonNull).filter(i -> !i.isDisposed()).forEach(Image::dispose);
	}

}
