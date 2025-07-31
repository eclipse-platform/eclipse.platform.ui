/*******************************************************************************
 * Copyright (c) 2008, 2025 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 429728, 430166, 441150, 442285, 472654, 495718
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 337588, 388476, 461573
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 442285, 487348
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 497618
 *     Christoph Läubrich - Bug 433465
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import static java.util.Collections.singletonList;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.internal.workbench.PartStackUtil;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.BasicPartList;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.SWTRenderersMessages;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * SWT default renderer for a MPartStack model elements
 *
 * Style bits for the underlying CTabFolder can be set via the
 * IPresentation.STYLE_OVERRIDE_KEY key
 */
public class StackRenderer extends LazyStackRenderer {

	private static final String ONBOARDING_CONTAINER = "EditorStack.OnboardingContainer"; //$NON-NLS-1$
	private static final String ONBOARDING_COMPOSITE = "EditorStack.OnboardingComposite"; //$NON-NLS-1$
	private static final String ONBOARDING_IMAGE = "EditorStack.OnboardingImage"; //$NON-NLS-1$
	private static final String ONBOARDING_TEXT = "EditorStack.OnboardingText"; //$NON-NLS-1$

	/**
	 * Id of a a control.
	 */
	private static final String ID = "id"; //$NON-NLS-1$

	private static final String THE_PART_KEY = "thePart"; //$NON-NLS-1$

	/**
	 * Key to control the default default value of the "most recently used" order
	 * enablement
	 */
	public static final String MRU_KEY_DEFAULT = "enableMRUDefault"; //$NON-NLS-1$

	/**
	 * Key to control the actual boolean preference of the "most recently used"
	 * order enablement
	 */
	public static final String MRU_KEY = "enableMRU"; //$NON-NLS-1$

	/**
	 * Default default value for MRU behavior.
	 */
	public static final boolean MRU_DEFAULT = true;

	/*
	 * JFace key for default workbench tab font
	 */
	private static final String TAB_FONT_KEY = "org.eclipse.ui.workbench.TAB_TEXT_FONT"; //$NON-NLS-1$

	@Inject
	@Preference(nodePath = "org.eclipse.e4.ui.workbench.renderers.swt")
	private IEclipsePreferences preferences;

	@Inject
	@Named(WorkbenchRendererFactory.SHARED_ELEMENTS_STORE)
	Map<MUIElement, Set<MPlaceholder>> renderedMap;

	public static final String TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$
	private static final String SHELL_CLOSE_EDITORS_MENU = "shell_close_editors_menu"; //$NON-NLS-1$
	private static final String STACK_SELECTED_PART = "stack_selected_part"; //$NON-NLS-1$

	/**
	 * Add this tag to prevent the next tab's activation from granting focus toac
	 * the part. This is used to keep the focus on the CTabFolder when traversing
	 * the tabs using the keyboard.
	 */
	private static final String INHIBIT_FOCUS = "InhibitFocus"; //$NON-NLS-1$

	// Minimum characters in for stacks outside the shared area
	private static int MIN_VIEW_CHARS = 1;

	// Minimum characters in for stacks inside the shared area
	private static int MIN_EDITOR_CHARS = 15;

	/**
	 * Top spacing for onboarding composite, needed so that tabfolder borders are
	 * shown
	 */
	public static final int ONBOARDING_TOP_SPACING = 30;
	/**
	 * (Left, Right, Bottom) Spacing for onboarding composite, needed so that
	 * tabfolder borders are shown
	 */
	public static final int ONBOARDING_SPACING = 2;

	/**
	 * The threshold to show the onbarding image. If tabfolder height gets below
	 * this threshold, the image is hidden.
	 */
	public static final int ONBOARDING_SHOW_IMAGE_HEIGHT_THRESHOLD = 450;

	private MPerspective currentPerspectiveForOnboarding;

	private Image viewMenuImage;
	private String viewMenuURI = "platform:/plugin/org.eclipse.e4.ui.workbench.renderers.swt/icons/full/elcl16/view_menu.svg"; //$NON-NLS-1$

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private IPresentationEngine renderer;

	private boolean ignoreTabSelChanges;

	private TabStateHandler tabStateHandler;

	private boolean imageChanged;

	List<CTabItem> getItemsToSet(MPart part) {
		List<CTabItem> itemsToSet = new ArrayList<>();

		MUIElement partParent = part.getParent();
		if (partParent instanceof MPartStack) {
			addItemToSet(itemsToSet, part);
		} else if (partParent instanceof MPartSashContainer) {
			MElementContainer<MUIElement> parentParent = partParent.getParent();
			if (parentParent instanceof MPart) {
				MPart parentParentMPart = (MPart) parentParent;
				addItemToSet(itemsToSet, parentParentMPart);
			}
		} else if (part.getCurSharedRef() != null) {
			MWindow topWin = modelService.getTopLevelWindowFor(part);
			List<MPlaceholder> partRefs = modelService.findElements(topWin, part.getElementId(), MPlaceholder.class);
			for (MPlaceholder ref : partRefs) {
				CTabItem item = findItemForPart(ref, null);
				if (item != null) {
					itemsToSet.add(item);
				}
			}
		}

		return itemsToSet;
	}

	private void addItemToSet(List<CTabItem> itemsToSet, MPart parentParent) {
		CTabItem item = findItemForPart(parentParent);
		if (item != null) {
			itemsToSet.add(item);
		}
	}

	@Inject
	@Optional
	void subscribePerspectiveSwitched(
			@UIEventTopic(UIEvents.UILifeCycle.PERSPECTIVE_SWITCHED) org.osgi.service.event.Event event) {
		Object element = event.getProperty(EventTags.ELEMENT);
		if (element instanceof MPerspective perspective) {
			currentPerspectiveForOnboarding = perspective;
			getEditorTabFolders(perspective).forEach(this::initializeOnboardingInformationInEditorStack);
		}
	}

	private void initializeOnboardingInformationInEditorStack(CTabFolder editorStackTabFolder) {
		MPerspective perspective = currentPerspectiveForOnboarding;
		if (perspective == null) {
			return;
		}

		Composite onboardingContainer = getChild(editorStackTabFolder, ONBOARDING_CONTAINER, Composite.class);
		if (onboardingContainer == null || onboardingContainer.isDisposed()) {
			return;
		}

		Composite onboardingComposite = getChild(onboardingContainer, ONBOARDING_COMPOSITE, Composite.class);
		if (onboardingComposite == null || onboardingComposite.isDisposed()) {
			return;
		}

		Label onboardingImage = getChild(onboardingComposite, ONBOARDING_IMAGE, Label.class);
		Label onboardingText = getChild(onboardingComposite, ONBOARDING_TEXT, Label.class);

		if (onboardingImage == null || onboardingImage.isDisposed() || onboardingText == null
				|| onboardingText.isDisposed()) {
			return;
		}

		Arrays.stream(onboardingComposite.getChildren()).filter(c -> c != onboardingImage)
				.filter(c -> c != onboardingText).forEach(Control::dispose);

		String textId = "persp.editorOnboardingText:"; //$NON-NLS-1$
		perspective.getTags().stream().filter(tag -> tag.startsWith(textId)).map(tag -> tag.substring(textId.length()))
				.findFirst().ifPresentOrElse(onboardingText::setText, () -> onboardingText.setText("")); //$NON-NLS-1$

		String iconId = "persp.editorOnboardingImageUri:"; //$NON-NLS-1$
		perspective.getTags().stream().filter(tag -> tag.startsWith(iconId)).map(tag -> tag.substring(iconId.length()))
				.findFirst().map(this::getImageFromURI).filter(Predicate.not(Image::isDisposed))
				.ifPresentOrElse(onboardingImage::setImage, () -> onboardingImage.setImage(null));

		String commandId = "persp.editorOnboardingCommand:"; //$NON-NLS-1$
		String[] commands = perspective.getTags().stream().filter(tag -> tag.startsWith(commandId))
				.map(tag -> tag.substring(commandId.length())).toArray(String[]::new);

		Color color = JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR);

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory commandGridDataFactory = GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER);

		for (int i = 0; i < commands.length; i++) {
			labelGridDataFactory.indent(SWT.DEFAULT, i == 0 ? 10 : SWT.DEFAULT);
			commandGridDataFactory.indent(SWT.DEFAULT, i == 0 ? 10 : SWT.DEFAULT);

			String[] commandAndText = commands[i].split("\\$\\$\\$"); //$NON-NLS-1$

			WidgetFactory.label(SWT.NONE).text(commandAndText[0]).foreground(color)
					.supplyLayoutData(labelGridDataFactory::create).create(onboardingComposite);
			WidgetFactory.label(SWT.NONE).text(commandAndText[1]).foreground(color)
					.supplyLayoutData(commandGridDataFactory::create).create(onboardingComposite);
		}
	}

	@SuppressWarnings("unchecked")
	@Inject
	@Optional
	void subscribeTopicTransientDataChanged(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TRANSIENTDATA) org.osgi.service.event.Event event) {
		Object changedElement = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (!(changedElement instanceof MPart))
			return;

		String key;
		if (UIEvents.isREMOVE(event)) {
			key = ((Entry<String, Object>) event.getProperty(UIEvents.EventTags.OLD_VALUE)).getKey();
		} else {
			key = ((Entry<String, Object>) event.getProperty(UIEvents.EventTags.NEW_VALUE)).getKey();
		}

		if (!IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY.equals(key)
				&& !IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY.equals(key))
			return;

		MPart part = (MPart) changedElement;
		List<CTabItem> itemsToSet = getItemsToSet(part);
		for (CTabItem item : itemsToSet) {
			if (key.equals(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY)) {
				changePartTabImage(part, item);
			} else if (key.equals(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY)) {
				String newTip = getToolTip(part);
				item.setToolTipText(getToolTip(newTip));
			}
		}
	}

	/**
	 * Handles changes in tags
	 */
	@Inject
	@Optional
	void subscribeTopicTagsChanged(@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {

		if (tabStateHandler == null) {
			tabStateHandler = new TabStateHandler();
		}
		tabStateHandler.handleEvent(event);

		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MPart))
			return;

		final MPart part = (MPart) changedObj;
		CTabItem item = findItemForPart(part);
		if (item == null || item.isDisposed())
			return;

		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE, IPresentationEngine.ADORNMENT_PIN)) {
				item.setImage(getImage(part));
			}
		} else if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE, IPresentationEngine.ADORNMENT_PIN)) {
				item.setImage(getImage(part));
			}
		}
	}

	@Inject
	@Optional
	void subscribeTopicChildrenChanged(@UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
		shouldTopRightAdjusted(event);
	}

	@Inject
	@Optional
	void subscribeTopicUILabelChanged(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(element instanceof MPart))
			return;

		MPart part = (MPart) element;

		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);

		// is this a direct child of the stack?
		if (part.getParent() != null && part.getParent().getRenderer() == StackRenderer.this) {
			CTabItem cti = findItemForPart(part);
			if (cti != null) {
				updateTab(cti, part, attName, newValue);
			}
			return;
		}

		// Do we have any stacks with place holders for the element
		// that's changed?
		MWindow win = modelService.getTopLevelWindowFor(part);
		List<MPlaceholder> refs = modelService.findElements(win, null, MPlaceholder.class, null);
		if (refs != null) {
			for (MPlaceholder ref : refs) {
				if (ref.getRef() != part)
					continue;

				MElementContainer<MUIElement> refParent = ref.getParent();
				// can be null, see bug 328296
				if (refParent != null && refParent.getRenderer() instanceof StackRenderer) {
					CTabItem cti = findItemForPart(ref, refParent);
					if (cti != null) {
						updateTab(cti, part, attName, newValue);
					}
				}
			}
		}
	}

	@Inject
	@Optional
	void subscribeTopicDirtyChanged(@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event event) {
		Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);

		// Ensure that this event is for a MMenuItem
		if (!(objElement instanceof MPart)) {
			return;
		}

		// Extract the data bits
		MPart part = (MPart) objElement;

		updatePartTab(event, part);
	}

	@Inject
	@Optional
	void subscribeTopicClosablePartChanged(@UIEventTopic(UIEvents.Part.TOPIC_CLOSEABLE) Event event) {
		updateClosableTab(event);
	}

	@Inject
	@Optional
	void subscribeTopicClosablePlaceholderChanged(@UIEventTopic(UIEvents.Placeholder.TOPIC_CLOSEABLE) Event event) {
		updateClosableTab(event);
	}

	private void updateClosableTab(Event event) {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

		MPart part = null;
		if (element instanceof MPart) {
			part = (MPart) element;
		} else if (element instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) element).getRef();
			if (ref instanceof MPart) {
				part = (MPart) ref;
			}
		}

		if (part == null) {
			return;
		}

		updatePartTab(event, part);
	}

	private void updatePartTab(Event event, MPart part) {
		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);

		// Is the part directly under the stack?
		MElementContainer<MUIElement> parent = part.getParent();
		if (parent != null && parent.getRenderer() == StackRenderer.this) {
			CTabItem cti = findItemForPart(part, parent);
			if (cti != null) {
				updateTab(cti, part, attName, newValue);
			}
			return;
		}

		// Do we have any stacks with place holders for the element
		// that's changed?
		Set<MPlaceholder> refs = renderedMap.get(part);
		if (refs != null) {
			for (MPlaceholder ref : refs) {
				MElementContainer<MUIElement> refParent = ref.getParent();
				if (refParent.getRenderer() instanceof StackRenderer) {
					CTabItem cti = findItemForPart(ref, refParent);
					if (cti != null) {
						updateTab(cti, part, attName, newValue);
					}
				}
			}
		}
	}

	@Inject
	@Optional
	void subscribeTopicVisibleChanged(@UIEventTopic(UIEvents.UIElement.TOPIC_VISIBLE) Event event) {
		shouldTopRightAdjusted(event);
	}

	@Inject
	@Optional
	void subscribeTopicToBeRenderedChanged(@UIEventTopic(UIEvents.UIElement.TOPIC_TOBERENDERED) Event event) {
		shouldTopRightAdjusted(event);
	}

	@Inject
	@Optional
	private UISynchronize synchronize;

	/**
	 * An event handler for listening to changes to the state of view menus and part
	 * toolbars. Depending on what state these items are in, the view menu and
	 * toolbar should or should not be rendered on the tab folder.
	 */
	private void shouldTopRightAdjusted(Event event) {
		Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);

		// Ensure that this event is for a MMenuItem or MToolBar
		if (!(objElement instanceof MMenuElement && !(objElement instanceof MPopupMenu))
				&& !(objElement instanceof MToolBar)) {
			return;
		}

		// Ensure that it's a View part's menu or toolbar
		MUIElement uiElement = (MUIElement) objElement;
		MUIElement parent = modelService.getContainer(uiElement);
		if (!(parent instanceof MPart)) {
			return;
		}

		// Get the partstack and the element in the partstack
		MStackElement element = (MPart) parent;
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}
		MUIElement parentElement = element.getParent();

		if (!(parentElement instanceof MPartStack)) {
			return;
		}

		Object widget = parentElement.getWidget();
		if (widget instanceof CTabFolder) {
			// The widget is created by the PartRenderingEngine, however the order of this
			// call and the call in the engine are unknown. Therefore, call it
			// asynchronously such that it is always last.
			synchronize.asyncExec(() -> adjustTopRight((CTabFolder) widget));
		}
	}

	@Inject
	@Optional
	void subscribeTopicActivateChanged(@UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
		// Manages CSS styling based on active part changes
		MUIElement changed = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(changed instanceof MPart)) {
			return;
		}

		MPart newActivePart = (MPart) changed;
		MUIElement partParent = newActivePart.getParent();
		if (partParent == null && newActivePart.getCurSharedRef() != null) {
			partParent = newActivePart.getCurSharedRef().getParent();
		}

		// Skip sash containers
		while (partParent != null && partParent instanceof MPartSashContainer) {
			partParent = partParent.getParent();
		}

		// Ensure the stack of a split part gets updated when one
		// of its internal parts gets activated
		if (partParent instanceof MCompositePart) {
			partParent = partParent.getParent();
		}

		MPartStack pStack = (MPartStack) (partParent instanceof MPartStack ? partParent : null);

		List<MUIElement> activeElements = modelService.findElements(modelService.getTopLevelWindowFor(newActivePart),
				null, MUIElement.class, singletonList(CSSConstants.CSS_ACTIVE_CLASS));
		for (MUIElement element : activeElements) {
			if (element instanceof MPartStack && element != pStack) {
				styleElement(element, false);
			} else if (element instanceof MPart && element != newActivePart) {
				styleElement(element, false);
			}
		}

		if (pStack != null) {
			styleElement(pStack, true);
		}
		styleElement(newActivePart, true);
	}

	@Inject
	@Optional
	void subscribeTopicSelectedelementChanged(
			@UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		if (tabStateHandler == null) {
			tabStateHandler = new TabStateHandler();
		}
		tabStateHandler.handleEvent(event);
	}

	@Override
	protected boolean requiresFocus(MPart element) {
		MUIElement inStack = element.getCurSharedRef() != null ? element.getCurSharedRef() : element;
		if (inStack.getParent() != null && inStack.getParent().getTransientData().containsKey(INHIBIT_FOCUS)) {
			inStack.getParent().getTransientData().remove(INHIBIT_FOCUS);
			return false;
		}

		return super.requiresFocus(element);
	}

	@PostConstruct
	public void init() {
		super.init(eventBroker);
	}

	protected void updateTab(CTabItem cti, MPart part, String attName, Object newValue) {
		switch (attName) {
		case UIEvents.UILabel.LABEL:
		case UIEvents.UILabel.LOCALIZED_LABEL:
			String newName = (String) newValue;
			cti.setText(getLabel(part, newName));
			break;
		case UIEvents.Dirtyable.DIRTY:
			cti.setText(getLabel(part, part.getLocalizedLabel()));
			break;
		case UIEvents.UILabel.ICONURI:
			changePartTabImage(part, cti);
			break;
		case UIEvents.UILabel.TOOLTIP:
		case UIEvents.UILabel.LOCALIZED_TOOLTIP:
			String newTTip = (String) newValue;
			cti.setToolTipText(getToolTip(newTTip));
			break;
		case UIEvents.Part.CLOSEABLE:
			Boolean closeableState = (Boolean) newValue;
			cti.setShowClose(closeableState.booleanValue());
			break;
		default:
			break;
		}
	}

	private void changePartTabImage(MPart part, CTabItem item) {
		this.imageChanged = true;
		item.setImage(getImage(part));
		this.imageChanged = false;
	}

	@PreDestroy
	public void contextDisposed() {
		super.contextDisposed(eventBroker);
	}

	private String getLabel(MUILabel itemPart, String newName) {
		if (newName == null) {
			newName = ""; //$NON-NLS-1$
		} else {
			newName = LegacyActionTools.escapeMnemonics(newName);
		}

		if (itemPart instanceof MDirtyable && ((MDirtyable) itemPart).isDirty()) {
			newName = '*' + newName;
		}
		return newName;
	}

	private String getToolTip(String newToolTip) {
		return newToolTip == null || newToolTip.length() == 0 ? null : LegacyActionTools.escapeMnemonics(newToolTip);
	}

	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPartStack) || !(parent instanceof Composite))
			return null;

		MPartStack pStack = (MPartStack) element;
		int location = modelService.getElementLocation(element);
		boolean isInSharedArea = (location & EModelService.IN_SHARED_AREA) != 0;
		if (isInSharedArea) {
			PartStackUtil.makeEditorStack(pStack);
		}

		Composite parentComposite = (Composite) parent;

		// Ensure that all rendered PartStacks have an Id
		if (element.getElementId() == null || element.getElementId().length() == 0) {
			String generatedId = "PartStack@" + Integer.toHexString(element.hashCode()); //$NON-NLS-1$
			element.setElementId(generatedId);
		}

		int styleOverride = getStyleOverride(pStack);
		int style = styleOverride == -1 ? SWT.BORDER : styleOverride;
		CTabFolder tabFolder = new CTabFolder(parentComposite, style);
		if (PartStackUtil.isEditorStack(element)) {
			createOnboardingControls(tabFolder);
			initializeOnboardingInformationInEditorStack(tabFolder);
			int drop = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
			// Bug: eclipse-platform/eclipse.platform.swt/issues/649
			Composite dropZone = new Composite(tabFolder, SWT.NONE);// additional Composite/child to support dropping
			new Label(dropZone, SWT.NONE);
			new DropTarget(dropZone, drop);
		}
		tabFolder.setMRUVisible(getMRUValue());

		// Adjust the minimum chars based on the location
		if (isInSharedArea) {
			tabFolder.setMinimumCharacters(MIN_EDITOR_CHARS);
			tabFolder.setUnselectedCloseVisible(true);
		} else {
			tabFolder.setMinimumCharacters(MIN_VIEW_CHARS);
			tabFolder.setUnselectedCloseVisible(false);
		}

		bindWidget(element, tabFolder); // ?? Do we need this ?

		// Add a composite to manage the view's TB and Menu
		addTopRight(tabFolder);

		return tabFolder;
	}

	private void createOnboardingControls(CTabFolder tabFolder) {
		Composite onBoardingContainer = WidgetFactory.composite(SWT.NONE).data(ID, ONBOARDING_CONTAINER)
				.layout(GridLayoutFactory.swtDefaults().create()).background(tabFolder.getBackground())
				.create(tabFolder);

		Composite onboardingComposite = WidgetFactory.composite(SWT.NONE).background(tabFolder.getBackground())
				.data(ID, ONBOARDING_COMPOSITE).create(onBoardingContainer);
		GridDataFactory.create(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER).grab(true, true)
				.applyTo(onboardingComposite);

		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).spacing(10, SWT.DEFAULT)
				.applyTo(onboardingComposite);

		GridDataFactory gridDataFactory = GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.indent(SWT.DEFAULT, 10).span(2, 1);

		Label onboardingImage = WidgetFactory.label(SWT.NONE).supplyLayoutData(gridDataFactory::create)
				.data(ID, ONBOARDING_IMAGE).create(onboardingComposite);

		Color color = JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR);
		WidgetFactory.label(SWT.NONE).foreground(color).supplyLayoutData(gridDataFactory::create)
				.data(ID, ONBOARDING_TEXT).create(onboardingComposite);

		onBoardingContainer.setLocation(ONBOARDING_SPACING, ONBOARDING_TOP_SPACING);
		Consumer<ControlEvent> sizeUpdate = e -> setOnboardingControlSize(tabFolder, onBoardingContainer,
				onboardingComposite, onboardingImage);
		tabFolder.addControlListener(ControlListener.controlResizedAdapter(sizeUpdate));
		Consumer<CTabFolderEvent> tabCountUpdate = e -> setOnboardingControlSize(tabFolder, onBoardingContainer,
				onboardingComposite, onboardingImage);
		tabFolder.addCTabFolder2Listener(CTabFolder2Listener.itemsCountAdapter(tabCountUpdate));

		tabFolder.addDisposeListener(e -> {
			if (onboardingImage != null && !onboardingImage.isDisposed() && onboardingImage.getImage() != null
					&& !onboardingImage.getImage().isDisposed()) {
				onboardingImage.dispose();
			}
		});
	}

	private void setOnboardingControlSize(CTabFolder tabFolder, Composite onBoarding, Composite onboardingComposite,
			Label onBoardingImage) {
		if (onBoarding == null || onBoarding.isDisposed() || tabFolder == null || tabFolder.isDisposed()
				|| onBoardingImage == null || onBoardingImage.isDisposed()) {
			return;
		}
		boolean showComposite = tabFolder.getItemCount() == 0;
		boolean compositeVisible = onBoarding.isVisible();
		boolean imageVisible = onBoardingImage.isVisible();
		if (showComposite) {
			if (!compositeVisible) {
				onBoarding.setVisible(true);
			}
			Rectangle folderBounds = tabFolder.getBounds();
			int width = folderBounds.width - 2 * ONBOARDING_SPACING;
			int height = folderBounds.height - ONBOARDING_TOP_SPACING - ONBOARDING_SPACING;
			if (!new Point(width, height).equals(onBoarding.getSize())) {
				onBoarding.setSize(width, height);

				boolean showImage = height > ONBOARDING_SHOW_IMAGE_HEIGHT_THRESHOLD;
				if (imageVisible != showImage || showComposite != compositeVisible) {
					onBoardingImage.setVisible(showImage);
					((GridData) onBoardingImage.getLayoutData()).exclude = !showImage;
					onboardingComposite.getParent().layout(true);
				}

			}
		} else {
			if (compositeVisible) {
				onBoarding.setVisible(false);
				onBoarding.setSize(0, 0);
			}
		}
	}

	private boolean getMRUValue() {
		return getMRUValueFromPreferences();
	}

	private boolean getMRUValueFromPreferences() {
		boolean initialMRUValue = preferences.getBoolean(MRU_KEY_DEFAULT, MRU_DEFAULT);
		return preferences.getBoolean(MRU_KEY, initialMRUValue);
	}

	private void updateMRUValue(CTabFolder tabFolder) {
		boolean actualMRUValue = getMRUValue();
		tabFolder.setMRUVisible(actualMRUValue);
	}

	private void addTopRight(CTabFolder tabFolder) {
		Composite trComp = new Composite(tabFolder, SWT.NONE);
		RowLayout rl = new RowLayout();
		trComp.setLayout(rl);
		rl.spacing = 0;
		rl.marginBottom = rl.marginTop = rl.marginRight = rl.marginLeft = 0;
		tabFolder.setTopRight(trComp, SWT.RIGHT | SWT.WRAP);

		// Initially it's not visible
		trComp.setVisible(false);

		// Create a toolbar for the view's drop-down menu
		ToolBar menuTB = new ToolBar(trComp, SWT.FLAT | SWT.RIGHT);
		menuTB.setData(TAG_VIEW_MENU);
		tabFolder.setData(TAG_VIEW_MENU, menuTB);
		RowData rd = new RowData();
		menuTB.setLayoutData(rd);
		ToolItem ti = new ToolItem(menuTB, SWT.PUSH);
		ti.setImage(getViewMenuImage());
		ti.setHotImage(null);
		ti.setToolTipText(SWTRenderersMessages.viewMenu);

		// Initially it's not visible
		rd.exclude = true;
		menuTB.setVisible(false);

		ti.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showMenu((ToolItem) e.widget);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				showMenu((ToolItem) e.widget);
			}
		});
		menuTB.getAccessible().addAccessibleListener(AccessibleListener.getNameAdapter(e -> {
			if (e.childID != ACC.CHILDID_SELF) {
				Accessible accessible = (Accessible) e.getSource();
				ToolBar toolBar = (ToolBar) accessible.getControl();
				if (0 <= e.childID && e.childID < toolBar.getItemCount()) {
					ToolItem item = toolBar.getItem(e.childID);
					if (item != null) {
						e.result = item.getToolTipText();
					}
				}
			}
		}));

		// Set an initial bounds
		trComp.pack();
	}

	public void adjustTopRight(final CTabFolder tabFolder) {
		if (tabFolder.isDisposed()) {
			return;
		}

		// Gather the parameters
		MPartStack stack = (MPartStack) tabFolder.getData(OWNING_ME);
		MUIElement element = stack.getSelectedElement();
		MPart part = null;
		if (element != null) {
			part = (MPart) ((element instanceof MPart) ? element : ((MPlaceholder) element).getRef());
		}

		Composite trComp = (Composite) tabFolder.getTopRight();

		boolean needsTB = part != null && part.getToolbar() != null && part.getToolbar().isToBeRendered();

		// View menu (if any)
		MMenu viewMenu = getViewMenu(part);
		boolean needsMenu = viewMenu != null && hasVisibleMenuItems(viewMenu, part);

		// Check the current state of the TB's
		ToolBar menuTB = (ToolBar) tabFolder.getData(TAG_VIEW_MENU);

		// We need to modify the 'exclude' bit based on if the menuTB is
		// visible or not
		RowData rd = (RowData) menuTB.getLayoutData();
		ToolItem item = menuTB.getItem(0);
		if (needsMenu) {
			item.setData(THE_PART_KEY, part);
			rd.exclude = false;
			String tooltip = viewMenu.getTooltip();
			if (tooltip == null) {
				tooltip = viewMenu.getLabel();
			}
			if (tooltip == null) {
				tooltip = SWTRenderersMessages.viewMenu;
			}
			item.setToolTipText(tooltip);
			menuTB.setVisible(true);
		} else {
			item.setData(THE_PART_KEY, null);
			rd.exclude = true;
			menuTB.setVisible(false);
		}

		menuTB.moveBelow(null);

		if (needsMenu || needsTB) {
			tabFolder.getTopRight().setVisible(true);
		} else {
			tabFolder.getTopRight().setVisible(false);
		}

		// Pack the result
		try {
			trComp.setRedraw(false);
			trComp.pack();
			trComp.requestLayout();
		} finally {
			trComp.setRedraw(true);
		}

		updateMRUValue(tabFolder);
	}

	@Override
	protected void createTab(MElementContainer<MUIElement> stack, MUIElement element) {

		// an invisible element won't have the correct widget hierarchy
		if (!element.isVisible()) {
			return;
		}

		MPart part = null;
		if (element instanceof MPart)
			part = (MPart) element;
		else if (element instanceof MPlaceholder) {
			part = (MPart) ((MPlaceholder) element).getRef();
			if (part != null) {
				part.setCurSharedRef((MPlaceholder) element);
			}
		}

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();

		CTabItem tabItem = findItemForPart(element, stack);
		if (tabItem != null) {
			if (element.getWidget() != null && tabItem.getControl() != element.getWidget())
				tabItem.setControl((Control) element.getWidget());
			return;
		}
		updateMRUValue(tabFolder);
		int createFlags = SWT.NONE;
		if (part != null && isClosable(part)) {
			createFlags |= SWT.CLOSE;
		}

		// Create the tab; we may have more visible tabs than currently shown
		// (e.g., a result of calling partStack.getChildren().addAll(partList))
		int index = Math.min(calcIndexFor(stack, element), tabFolder.getItemCount());
		tabItem = new CTabItem(tabFolder, createFlags, index);

		tabItem.setData(OWNING_ME, element);
		tabItem.setText(getLabel(part, part.getLocalizedLabel()));
		tabItem.setImage(getImage(part));

		String toolTip = getToolTip(part);
		if (toolTip == null)
			toolTip = part.getLocalizedTooltip();
		tabItem.setToolTipText(getToolTip(toolTip));
		if (element.getWidget() != null) {
			// The part might have a widget but may not yet have been placed
			// under this stack, check this
			Control control = (Control) element.getWidget();
			if (control.getParent() == tabFolder)
				tabItem.setControl((Control) element.getWidget());
		}
	}

	private int calcIndexFor(MElementContainer<MUIElement> stack, final MUIElement part) {
		int index = 0;

		// Find the -visible- part before this element
		for (MUIElement mPart : stack.getChildren()) {
			if (mPart == part)
				return index;
			if (mPart.isToBeRendered() && mPart.isVisible())
				index++;
		}
		return index;
	}

	@Override
	public void childRendered(final MElementContainer<MUIElement> parentElement, MUIElement element) {
		super.childRendered(parentElement, element);

		if (!(((MUIElement) parentElement) instanceof MPartStack) || !(element instanceof MStackElement))
			return;

		createTab(parentElement, element);
	}

	private CTabItem findItemForPart(MUIElement element, MElementContainer<MUIElement> stack) {
		if (stack == null)
			stack = element.getParent();
		if (!(stack.getWidget() instanceof CTabFolder))
			return null;
		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		if (tabFolder == null || tabFolder.isDisposed())
			return null;

		CTabItem[] items = tabFolder.getItems();
		for (CTabItem item : items) {
			if (item.getData(OWNING_ME) == element)
				return item;
		}
		return null;
	}

	public CTabItem findItemForPart(MPart part) {
		// Invisible parts don't have items
		if (!part.isToBeRendered())
			return null;

		// is this a direct child of the stack?
		if (part.getParent() != null && part.getParent().getRenderer() == StackRenderer.this) {
			CTabItem cti = findItemForPart(part, part.getParent());
			if (cti != null) {
				return cti;
			}
		}

		// Do we have any stacks with place holders for the element
		// that's changed?
		MWindow win = modelService.getTopLevelWindowFor(part);

		if (win == null)
			return null;

		List<MPlaceholder> refs = modelService.findElements(win, null, MPlaceholder.class);
		if (refs != null) {
			for (MPlaceholder ref : refs) {
				if (ref.getRef() != part)
					continue;

				MElementContainer<MUIElement> refParent = ref.getParent();
				// can be null, see bug 328296
				if (refParent != null && refParent.getRenderer() instanceof StackRenderer) {
					CTabItem cti = findItemForPart(ref, refParent);
					if (cti != null) {
						return cti;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement, MUIElement child) {
		super.hideChild(parentElement, child);

		CTabFolder tabFolder = (CTabFolder) parentElement.getWidget();
		if (tabFolder == null)
			return;

		// Check if we have to reset the currently active child for the stack
		CTabItem tabItem = findItemForPart(child, parentElement);
		if (tabItem == tabFolder.getSelection()) {
			// If we're the only part we need to clear the top right...
			if (tabFolder.getItemCount() == 1) {
				adjustTopRight(tabFolder);
			}
		}

		// find the 'stale' tab for this element and dispose it
		if (tabItem != null && !tabItem.isDisposed()) {
			tabItem.dispose();
		}
	}

	@Override
	public void hookControllerLogic(final MUIElement me) {
		super.hookControllerLogic(me);

		if (!(me instanceof MElementContainer<?>))
			return;

		@SuppressWarnings("unchecked")
		final MElementContainer<MUIElement> stack = (MElementContainer<MUIElement>) me;

		// Match the selected TabItem to its Part
		final CTabFolder tabFolder = (CTabFolder) me.getWidget();

		// Handle traverse events for accessibility
		tabFolder.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_ARROW_NEXT || e.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
				me.getTransientData().put(INHIBIT_FOCUS, true);
			} else if (e.detail == SWT.TRAVERSE_RETURN) {
				me.getTransientData().remove(INHIBIT_FOCUS);
				CTabItem cti = tabFolder.getSelection();
				if (cti != null) {
					MUIElement stackElement = (MUIElement) cti.getData(OWNING_ME);
					if (stackElement instanceof MPlaceholder)
						stackElement = ((MPlaceholder) stackElement).getRef();
					if ((stackElement instanceof MPart) && (tabFolder.isFocusControl())) {
						MPart thePart = (MPart) stackElement;
						renderer.focusGui(thePart);
					}
				}
			}
		});

		// Detect activation...picks up cases where the user clicks on the
		// (already active) tab
		tabFolder.addListener(SWT.Activate, event -> {
			if (event.detail == SWT.MouseDown) {
				CTabFolder tabFolder1 = (CTabFolder) event.widget;
				if (tabFolder1.getSelection() == null)
					return;

				// get the item under the cursor
				Point cp = event.display.getCursorLocation();
				cp = event.display.map(null, tabFolder1, cp);
				CTabItem overItem = tabFolder1.getItem(cp);

				// If the item we're over is *not* the current one do
				// nothing (it'll get activated when the tab changes)
				if (overItem == null || overItem == tabFolder1.getSelection()) {
					MUIElement uiElement = (MUIElement) tabFolder1.getSelection().getData(OWNING_ME);
					if (uiElement instanceof MPlaceholder)
						uiElement = ((MPlaceholder) uiElement).getRef();
					if (uiElement instanceof MPart)
						activate((MPart) uiElement);
				}
			}
		});

		tabFolder.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			// prevent recursions
			if (ignoreTabSelChanges)
				return;

			MUIElement ele = (MUIElement) e.item.getData(OWNING_ME);
			ele.getParent().setSelectedElement(ele);
			if (ele instanceof MPlaceholder)
				ele = ((MPlaceholder) ele).getRef();
			if (ele instanceof MPart)
				activate((MPart) ele);
		}));

		MouseListener mouseListener = new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CTabItem item = tabFolder.getSelection();
				if (item != null) {
					MUIElement ele = (MUIElement) item.getData(OWNING_ME);
					if (ele.getParent().getSelectedElement() == ele) {
						Control ctrl = (Control) ele.getWidget();
						if (ctrl != null) {
							ctrl.setFocus();
						}
					}
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (tabFolder.isDisposed()) {
					// 517654: MouseUp may be sent after stack has been disposed
					return;
				}
				CTabItem item = tabFolder.getItem(new Point(e.x, e.y));

				// If the user middle clicks on a tab, close it
				if (item != null && e.button == 2) {
					closePart(item);
				}

				// If the user clicks on the tab or empty stack space, call
				// setFocus() to transfer it from the tabfolder to the client widget
				if (e.button == 1) {
					if (item == null) {
						Rectangle clientArea = tabFolder.getClientArea();
						if (!clientArea.contains(e.x, e.y)) {
							// User clicked in empty space
							item = tabFolder.getSelection();
						}
					}

					// but only transfer focus if we have it.
					// If we don't own it, the widget has already the focus
					// so don't set it second time
					if (item != null && tabFolder.isFocusControl()) {
						MUIElement ele = (MUIElement) item.getData(OWNING_ME);
						if (ele.getParent().getSelectedElement() == ele) {
							Control ctrl = (Control) ele.getWidget();
							if (ctrl != null) {
								ctrl.setFocus();
							}
						}
					}
				}
			}
		};
		tabFolder.addMouseListener(mouseListener);

		CTabFolder2Adapter closeListener = new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				event.doit = closePart(event.item);
			}

			@Override
			public void showList(CTabFolderEvent event) {
				event.doit = false;
				showAvailableItems(stack, tabFolder);
			}
		};
		tabFolder.addCTabFolder2Listener(closeListener);

		tabFolder.addMenuDetectListener(e -> {
			Point absolutePoint = new Point(e.x, e.y);
			Point relativePoint = tabFolder.getDisplay().map(null, tabFolder, absolutePoint);
			CTabItem eventTabItem = tabFolder.getItem(relativePoint);

			// If click happened in empty area, still show the menu
			if (eventTabItem == null) {
				Rectangle clientArea = tabFolder.getClientArea();
				if (!clientArea.contains(relativePoint)) {
					eventTabItem = tabFolder.getSelection();
				}
			}

			if (eventTabItem != null) {
				MUIElement uiElement = (MUIElement) eventTabItem.getData(AbstractPartRenderer.OWNING_ME);
				MPart tabPart = (MPart) ((uiElement instanceof MPart) ? uiElement
						: ((MPlaceholder) uiElement).getRef());
				openMenuFor(tabPart, tabFolder, absolutePoint);
			}
		});

		tabFolder.addControlListener(ControlListener.controlResizedAdapter(e -> updateMRUValue(tabFolder)));
	}

	/**
	 * Shows a popup dialog with the list of editors availavle in a given
	 * {@link CTabFolder}. By default the popup origin will be located close to the
	 * chevron location.
	 */
	public void showAvailableItems(MElementContainer<?> stack, CTabFolder tabFolder) {
		showAvailableItems(stack, tabFolder, false);
	}

	/**
	 * Shows a popup dialog with the list of editors available in the given
	 * CTabFolder. If {@code forceCenter} enabled, the dialog is centered
	 * horizontally; otherwise, the dialog origin is placed at chevron location. he
	 * dialog is placed at
	 *
	 * @param forceCenter center the dialog if true
	 */
	public void showAvailableItems(MElementContainer<?> stack, CTabFolder tabFolder, boolean forceCenter) {
		IEclipseContext ctxt = getContext(stack);
		final BasicPartList editorList = new BasicPartList(tabFolder.getShell(), SWT.ON_TOP,
				SWT.V_SCROLL | SWT.H_SCROLL, ctxt.get(EPartService.class), stack, this, getMRUValueFromPreferences());
		editorList.setInput();

		Point size = editorList.computeSizeHint();
		editorList.setSize(size.x, size.y);

		Point location = null;
		if (forceCenter) {
			// placed to the center
			Rectangle ca = tabFolder.getClientArea();
			location = tabFolder.toDisplay(ca.x, ca.y);
			location.x = Math.max(0, (location.x + ((ca.width - size.x) / 2)));
			location.y = Math.max(0, (location.y + ((ca.height - size.y) / 3)));
		} else {
			// placed at chevron location
			location = tabFolder.toDisplay(getChevronLocation(tabFolder));
			Monitor mon = tabFolder.getMonitor();
			Rectangle bounds = mon.getClientArea();
			if (location.x + size.x > bounds.x + bounds.width) {
				location.x = bounds.x + bounds.width - size.x;
			}
			if (location.y + size.y > bounds.y + bounds.height) {
				location.y = bounds.y + bounds.height - size.y;
			}
		}
		editorList.setLocation(location);

		editorList.setVisible(true);
		editorList.setFocus();
		editorList.getShell().addListener(SWT.Deactivate, event -> {
			editorList.getShell().getDisplay().asyncExec(() -> {
				if (!editorList.hasFocus()) {
					editorList.dispose();
				}
			});
		});
	}

	private Point getChevronLocation(CTabFolder tabFolder) {
		// get the last visible item
		int numItems = tabFolder.getItemCount();
		CTabItem item = null;
		for (int i = 0; i < numItems; i++) {
			CTabItem tempItem = tabFolder.getItem(i);
			if (tempItem.isShowing()) {
				item = tempItem;
			}
		}

		// if we have no visible tabs, abort.
		if (item == null) {
			return new Point(0, 0);
		}

		Rectangle itemBounds = item.getBounds();
		int x = itemBounds.x + itemBounds.width;
		int y = itemBounds.y + itemBounds.height;
		return new Point(x, y);
	}

	/**
	 * Closes the part that's backed by the given widget.
	 *
	 * @param widget the part that owns this widget
	 * @return <tt>true</tt> if the part was closed, <tt>false</tt> otherwise
	 */
	private boolean closePart(Widget widget) {
		MUIElement uiElement = (MUIElement) widget.getData(AbstractPartRenderer.OWNING_ME);
		MPart part = (MPart) ((uiElement instanceof MPart) ? uiElement : ((MPlaceholder) uiElement).getRef());
		if (!isClosable(part)) {
			return false;
		}

		IEclipseContext partContext = part.getContext();
		IEclipseContext parentContext = getContextForParent(part);
		// a part may not have a context if it hasn't been rendered
		IEclipseContext context = partContext == null ? parentContext : partContext;
		// ask user to save if necessary and close part if it is not dirty
		EPartService partService = context.get(EPartService.class);
		if (partService.savePart(part, true)) {
			partService.hidePart(part);
			return true;
		}
		// the user has canceled the save operation, so the part is not closed
		return false;
	}

	@Override
	protected void showTab(MUIElement element) {
		super.showTab(element);

		// an invisible element won't have the correct widget hierarchy
		if (!element.isVisible()) {
			return;
		}

		final CTabFolder tabFolder = (CTabFolder) getParentWidget(element);
		CTabItem tabItem = findItemForPart(element, null);
		if (tabItem == null) {
			createTab(element.getParent(), element);
			tabItem = findItemForPart(element, element.getParent());
		}
		Control ctrl = (Control) element.getWidget();
		if (ctrl != null && ctrl.getParent() != tabFolder) {
			ctrl.setParent(tabFolder);
			tabItem.setControl(ctrl);
		} else if (element.getWidget() == null) {
			Control tabCtrl = (Control) renderer.createGui(element);
			tabItem.setControl(tabCtrl);
		}

		ignoreTabSelChanges = true;
		// Ensure that the newly selected control is correctly sized
		if (tabItem.getControl() instanceof Composite) {
			Composite ctiComp = (Composite) tabItem.getControl();
			// see bug 461573, 528720: call below is still needed to make view
			// descriptions visible after unhiding the view with changed bounds
			ctiComp.requestLayout();
		}
		tabFolder.setSelection(tabItem);
		ignoreTabSelChanges = false;

		// Show the new state
		adjustTopRight(tabFolder);
	}

	protected void showMenu(ToolItem item) {
		MPart part = (MPart) item.getData(THE_PART_KEY);
		if (part == null) {
			return;
		}
		Control ctrl = (Control) part.getWidget();
		MMenu menuModel = getViewMenu(part);
		if (menuModel == null || !menuModel.isToBeRendered())
			return;

		final Menu swtMenu = (Menu) renderer.createGui(menuModel, ctrl.getShell(), part.getContext());
		if (swtMenu == null)
			return;

		ctrl.addDisposeListener(e -> {
			if (!swtMenu.isDisposed()) {
				swtMenu.dispose();
			}
		});

		// ...and Show it...
		Rectangle ib = item.getBounds();
		Point displayAt = item.getParent().toDisplay(ib.x, ib.y + ib.height);
		swtMenu.setLocation(displayAt);
		swtMenu.setVisible(true);

		Display display = swtMenu.getDisplay();
		while (!swtMenu.isDisposed() && swtMenu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		if (!swtMenu.isDisposed() && !(swtMenu.getData() instanceof MenuManager)) {
			swtMenu.dispose();
		}
	}

	private Image getViewMenuImage() {
		if (viewMenuImage == null) {
			viewMenuImage = getImageFromURI(viewMenuURI);
		}
		return viewMenuImage;
	}

	private void openMenuFor(MPart part, CTabFolder folder, Point point) {
		Menu tabMenu = createTabMenu(folder, part);
		tabMenu.setData(STACK_SELECTED_PART, part);
		tabMenu.setLocation(point.x, point.y);
		tabMenu.setVisible(true);
	}

	protected boolean isClosable(MPart part) {
		MPlaceholder curSharedRef = part.getCurSharedRef();
		if (curSharedRef != null) {
			// if it's a shared part check its current ref
			if (curSharedRef.getTags().contains(IPresentationEngine.NO_CLOSE)) {
				return false;
			}
			return curSharedRef.isCloseable();
		}

		return part.isCloseable();
	}

	private Menu createTabMenu(CTabFolder folder, MPart part) {
		Shell shell = folder.getShell();
		Menu cachedMenu = (Menu) shell.getData(SHELL_CLOSE_EDITORS_MENU);
		if (cachedMenu == null) {
			cachedMenu = new Menu(folder);
			shell.setData(SHELL_CLOSE_EDITORS_MENU, cachedMenu);
		} else {
			for (MenuItem item : cachedMenu.getItems()) {
				item.dispose();
			}
		}

		final Menu menu = cachedMenu;
		populateTabMenu(menu, part);
		return menu;
	}

	/**
	 * Populate the tab's context menu for the given part.
	 *
	 * @param menu the menu to be populated
	 * @param part the relevant part
	 */
	protected void populateTabMenu(final Menu menu, MPart part) {

		int closeableElements = 0;
		if (isClosable(part)) {
			createMenuItem(menu, SWTRenderersMessages.menuClose, e -> closePart(menu));
			closeableElements++;
		}

		MElementContainer<MUIElement> parent = getParent(part);
		if (parent != null) {
			closeableElements += getCloseableSiblingParts(part).size();

			if (closeableElements >= 2) {
				createMenuItem(menu, SWTRenderersMessages.menuCloseOthers, e -> closeSiblingParts(menu, true));

				// create menu for parts on the left
				if (!getCloseableSideParts(part, true).isEmpty()) {
					createMenuItem(menu, SWTRenderersMessages.menuCloseLeft, e -> closeSideParts(menu, true));
				}

				// create menu for parts on the right
				if (!getCloseableSideParts(part, false).isEmpty()) {
					createMenuItem(menu, SWTRenderersMessages.menuCloseRight, e -> closeSideParts(menu, false));
				}

				new MenuItem(menu, SWT.SEPARATOR);

				createMenuItem(menu, SWTRenderersMessages.menuCloseAll, e -> closeSiblingParts(menu, false));
			}
		}

		if (isDetachable(part)) {
			if (closeableElements > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}

			createMenuItem(menu, SWTRenderersMessages.menuDetach, e -> detachActivePart(menu));
		}
	}

	protected boolean isDetachable(MPart part) {
		// if it's a shared part check its current ref
		if (part.getCurSharedRef() != null) {
			return !part.getCurSharedRef().getTags().contains(IPresentationEngine.NO_DETACH);
		}

		return !part.getTags().contains(IPresentationEngine.NO_DETACH);
	}

	/**
	 *
	 * Detaches the currently selected part
	 */
	private void detachActivePart(final Menu menu) {
		MPart selectedPart = (MPart) menu.getData(STACK_SELECTED_PART);
		CTabItem cti = findItemForPart(selectedPart);
		if (cti == null || cti.getParent() == null) {
			return;
		}
		CTabFolder parent = cti.getParent();

		EModelService modelService = getContextForParent(selectedPart).get(EModelService.class);
		Rectangle bounds = parent.getBounds();
		Point display = parent.toDisplay(bounds.x, bounds.y);
		modelService.detach(selectedPart, display.x, display.y, bounds.width, bounds.height);
	}

	/**
	 *
	 * Closes the currently selected part
	 */
	private void closePart(final Menu menu) {
		MPart selectedPart = (MPart) menu.getData(STACK_SELECTED_PART);
		EPartService partService = getContextForParent(selectedPart).get(EPartService.class);
		if (partService.savePart(selectedPart, true)) {
			partService.hidePart(selectedPart);
		}
	}

	/**
	 * Helper method for creating menu items
	 */
	private MenuItem createMenuItem(final Menu menu, String menuItemText, Consumer<SelectionEvent> c) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(menuItemText);
		menuItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(c));
		return menuItem;

	}

	private MElementContainer<MUIElement> getParent(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			return placeholder == null ? null : placeholder.getParent();
		}
		return parent;
	}

	private List<MPart> getCloseableSideParts(MPart part, boolean left) {
		MElementContainer<MUIElement> container = getParent(part);
		if (container == null) {
			return new ArrayList<>();
		}

		int thisPartIdx = getPartIndex(part, container);
		if (thisPartIdx == -1) {
			return new ArrayList<>();
		}
		List<MUIElement> children = container.getChildren();
		final int start = left ? 0 : thisPartIdx + 1;
		final int end = left ? thisPartIdx : children.size();

		return getCloseableSiblingParts(part, children, start, end);
	}

	private int getPartIndex(MPart part, MElementContainer<MUIElement> container) {
		List<MUIElement> children = container.getChildren();
		for (int i = 0; i < children.size(); i++) {
			MUIElement child = children.get(i);
			MPart otherPart = null;
			if (child instanceof MPart) {
				otherPart = (MPart) child;
			} else if (child instanceof MPlaceholder) {
				MUIElement otherItem = ((MPlaceholder) child).getRef();
				if (otherItem instanceof MPart) {
					otherPart = (MPart) otherItem;
				}
			}
			if (otherPart == part) {
				return i;
			}
		}
		return -1;
	}

	private List<MPart> getCloseableSiblingParts(MPart part) {
		MElementContainer<MUIElement> container = getParent(part);
		if (container == null) {
			return new ArrayList<>();
		}

		List<MUIElement> children = container.getChildren();
		return getCloseableSiblingParts(part, children, 0, children.size());
	}

	private List<MPart> getCloseableSiblingParts(MPart part, List<MUIElement> children, final int start,
			final int end) {
		// broken out from closeSiblingParts so it can be used to determine how
		// many closeable siblings are available
		List<MPart> closeableSiblings = new ArrayList<>();
		for (int i = start; i < end; i++) {
			MUIElement child = children.get(i);
			// If the element isn't showing skip it
			if (!child.isToBeRendered())
				continue;

			MPart otherPart = null;
			if (child instanceof MPart)
				otherPart = (MPart) child;
			else if (child instanceof MPlaceholder) {
				MUIElement otherItem = ((MPlaceholder) child).getRef();
				if (otherItem instanceof MPart)
					otherPart = (MPart) otherItem;
			}
			if (otherPart == null)
				continue;

			if (part.equals(otherPart))
				continue; // skip selected item
			if (otherPart.isToBeRendered() && isClosable(otherPart))
				closeableSiblings.add(otherPart);
		}
		return closeableSiblings;
	}

	private void closeSideParts(Menu menu, boolean left) {
		MPart selectedPart = (MPart) menu.getData(STACK_SELECTED_PART);
		MElementContainer<MUIElement> container = getParent(selectedPart);
		if (container == null) {
			return;
		}
		List<MPart> others = getCloseableSideParts(selectedPart, left);
		closeSiblingParts(selectedPart, others, true);
	}

	private void closeSiblingParts(Menu menu, boolean skipThisPart) {
		MPart part = (MPart) menu.getData(STACK_SELECTED_PART);
		MElementContainer<MUIElement> container = getParent(part);
		if (container == null) {
			return;
		}
		List<MPart> others = getCloseableSiblingParts(part);
		closeSiblingParts(part, others, skipThisPart);
	}

	private void closeSiblingParts(MPart part, List<MPart> others, boolean skipThisPart) {
		MElementContainer<MUIElement> container = getParent(part);

		// add the current part last so that we unrender obscured items first
		if (!skipThisPart && part.isToBeRendered() && isClosable(part)) {
			others.add(part);
		}

		// add the selected element of the stack at the end, else we may end up
		// selecting another part when we hide it since it is the selected
		// element
		MUIElement selectedElement = container.getSelectedElement();
		if (others.remove(selectedElement)) {
			others.add((MPart) selectedElement);
		} else if (selectedElement instanceof MPlaceholder) {
			selectedElement = ((MPlaceholder) selectedElement).getRef();
			if (others.remove(selectedElement)) {
				others.add((MPart) selectedElement);
			}
		}

		EPartService partService = getContextForParent(part).get(EPartService.class);
		// try using the ISaveHandler first... This gives better control of
		// dialogs...
		ISaveHandler saveHandler = getContextForParent(part).get(ISaveHandler.class);
		if (saveHandler != null) {
			final List<MPart> toPrompt = new ArrayList<>(others);
			toPrompt.retainAll(partService.getDirtyParts());

			boolean cancel = false;
			if (toPrompt.size() > 1) {
				cancel = !saveHandler.saveParts(toPrompt, true);
			} else if (toPrompt.size() == 1) {
				cancel = !saveHandler.save(toPrompt.get(0), true);
			}
			if (cancel) {
				return;
			}

			for (MPart other : others) {
				partService.hidePart(other);
			}
			return;
		}

		// No ISaveHandler, fall back to just using the part service...
		for (MPart otherPart : others) {
			if (partService.savePart(otherPart, true))
				partService.hidePart(otherPart);
		}
	}

	public static MMenu getViewMenu(MPart part) {
		if (part == null || part.getMenus() == null) {
			return null;
		}
		for (MMenu menu : part.getMenus()) {
			if (menu.getTags().contains(TAG_VIEW_MENU)) {
				return menu;
			}
		}
		return null;
	}

	/**
	 * Determine whether the given view menu has any visible menu items.
	 *
	 * @param viewMenu the view menu to check
	 * @param part     the view menu's parent part
	 * @return <tt>true</tt> if the specified view menu has visible children,
	 *         <tt>false</tt> otherwise
	 */
	private boolean hasVisibleMenuItems(MMenu viewMenu, MPart part) {
		if (!viewMenu.isToBeRendered() || !viewMenu.isVisible()) {
			return false;
		}

		for (MMenuElement menuElement : viewMenu.getChildren()) {
			if (menuElement.isToBeRendered() && menuElement.isVisible()) {
				if (OpaqueElementUtil.isOpaqueMenuItem(menuElement)
						|| OpaqueElementUtil.isOpaqueMenuSeparator(menuElement)) {
					IContributionItem item = (IContributionItem) OpaqueElementUtil.getOpaqueItem(menuElement);
					if (item != null && item.isVisible()) {
						return true;
					}
				} else {
					return true;
				}
			}
		}

		Object menuRenderer = viewMenu.getRenderer();
		if (menuRenderer instanceof MenuManagerRenderer) {
			MenuManager manager = ((MenuManagerRenderer) menuRenderer).getManager(viewMenu);
			if (manager != null && manager.isVisible()) {
				return true;
			}
		}

		Control control = (Control) part.getWidget();
		if (control != null) {
			Menu menu = (Menu) renderer.createGui(viewMenu, control.getShell(), part.getContext());
			if (menu != null) {
				menuRenderer = viewMenu.getRenderer();
				if (menuRenderer instanceof MenuManagerRenderer) {
					MenuManagerRenderer menuManagerRenderer = (MenuManagerRenderer) menuRenderer;
					MenuManager manager = menuManagerRenderer.getManager(viewMenu);
					if (manager != null) {
						// remark ourselves as dirty so that the menu will be
						// reconstructed
						manager.markDirty();
					}
				}
				return menu.getItemCount() != 0;
			}
		}
		return false;
	}

	/**
	 * An event handler for listening to changes to the children of an element
	 * container. The tab folder may need to layout itself again if a part's toolbar
	 * has been changed.
	 */
	public class TabStateHandler implements EventHandler {

		@Override
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);

			if (!validateElement(element) || !validateValues(oldValue, newValue)) {
				return;
			}

			MPart part = newValue instanceof MPlaceholder ? (MPart) ((MPlaceholder) newValue).getRef()
					: (MPart) element;
			CTabItem cti = findItemForPart(part);
			if (cti == null) {
				return;
			}

			boolean isCssEngineActive = isCssEngineActive(cti);
			boolean isSelectedTab = cti == cti.getParent().getSelection();
			boolean partActivatedEvent = newValue instanceof MPlaceholder;

			if (CSSConstants.CSS_CONTENT_CHANGE_CLASS.equals(newValue)) {
				part.getTags().remove(CSSConstants.CSS_CONTENT_CHANGE_CLASS);
				if (!isSelectedTab) {
					addHighlight(part, cti, isCssEngineActive);
				}
			} else if (partActivatedEvent && part.getTags().contains(CSSConstants.CSS_HIGHLIGHTED_CLASS)) {
				removeHighlight(part, cti, isCssEngineActive);
			}

			String prevCssCls = WidgetElement.getCSSClass(cti);
			setCSSInfo(part, cti);

			if (prevCssCls == null || !prevCssCls.equals(WidgetElement.getCSSClass(cti))) {
				reapplyStyles(cti.getParent());
			}

			// Only update tab busy state if the CSS engine is not active
			if (isCssEngineActive || partActivatedEvent) {
				return;
			}

			updateBusyStateNoCss(cti, newValue, oldValue);
		}

		public boolean validateElement(Object element) {
			return element instanceof MPart || element instanceof MPartStack;
		}

		public boolean validateValues(Object oldValue, Object newValue) {
			return newValue instanceof MPlaceholder
					// part gets active
					|| isTagAdded(CSSConstants.CSS_BUSY_CLASS, oldValue, newValue)
					// part gets busy
					|| isTagRemoved(CSSConstants.CSS_BUSY_CLASS, oldValue, newValue)
					// part gets idle
					|| isTagAdded(CSSConstants.CSS_CONTENT_CHANGE_CLASS, oldValue, newValue);
			// content of part changed
		}

		private boolean isTagAdded(String tagName, Object oldValue, Object newValue) {
			return oldValue == null && tagName.equals(newValue);
		}

		private boolean isTagRemoved(String tagName, Object oldValue, Object newValue) {
			return newValue == null && tagName.equals(oldValue);
		}
	}

	static boolean isCssEngineActive(CTabItem cti) {
		return WidgetElement.getEngine(cti.getParent()) != null;
	}

	static void removeHighlight(MPart part, CTabItem cti, boolean cssEngineActive) {
		part.getTags().remove(CSSConstants.CSS_HIGHLIGHTED_CLASS);
		if (!cssEngineActive) {
			cti.setFont(JFaceResources.getFontRegistry().get(TAB_FONT_KEY));
		}
	}

	static void addHighlight(MPart part, CTabItem cti, boolean cssEngineActive) {
		part.getTags().add(CSSConstants.CSS_HIGHLIGHTED_CLASS);
		if (!cssEngineActive) {
			cti.setFont(JFaceResources.getFontRegistry().getBold(TAB_FONT_KEY));
		}
	}

	/**
	 * Updates the visual for busy state of the part tab in case CSS engine is not
	 * active
	 */
	static void updateBusyStateNoCss(CTabItem cti, Object newValue, Object oldValue) {
		Font updatedFont = null;
		if (CSSConstants.CSS_BUSY_CLASS.equals(newValue)) {
			updatedFont = JFaceResources.getFontRegistry().getItalic(TAB_FONT_KEY);
		} else if (CSSConstants.CSS_BUSY_CLASS.equals(oldValue)) {
			updatedFont = JFaceResources.getFontRegistry().get(TAB_FONT_KEY);
		}
		if (updatedFont != null) {
			cti.setFont(updatedFont);
		}
	}

	@Override
	protected boolean imageChanged() {
		return this.imageChanged;
	}

	private Stream<CTabFolder> getEditorTabFolders(MPerspective perspective) {
		Predicate<Object> tabFolders = CTabFolder.class::isInstance;
		Function<Object, CTabFolder> toTabFolder = CTabFolder.class::cast;

		List<MPartStack> elements = modelService.findElements(perspective, null, MPartStack.class,
				List.of(PartStackUtil.EDITOR_STACK_TAG));
		return elements.stream().map(MUIElement::getWidget).filter(tabFolders).map(toTabFolder);
	}

	private <T extends Control> T getChild(Composite parent, String id, Class<T> type) {
		if (parent == null || parent.isDisposed()) {
			return null;
		}
		return Arrays.stream(parent.getChildren()).filter(child -> id.equals(child.getData(ID)))
				.filter(type::isInstance).map(type::cast).findFirst().orElse(null);
	}
}
