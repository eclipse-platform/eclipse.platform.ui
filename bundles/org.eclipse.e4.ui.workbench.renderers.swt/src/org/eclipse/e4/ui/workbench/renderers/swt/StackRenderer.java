/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.BasicPartList;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.SWTRenderersMessages;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.w3c.dom.css.CSSValue;

public class StackRenderer extends LazyStackRenderer {
	@Inject
	@Named(WorkbenchRendererFactory.SHARED_ELEMENTS_STORE)
	Map<MUIElement, Set<MPlaceholder>> renderedMap;

	public static final String TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$
	private static final String SHELL_CLOSE_EDITORS_MENU = "shell_close_editors_menu"; //$NON-NLS-1$
	private static final String STACK_SELECTED_PART = "stack_selected_part"; //$NON-NLS-1$

	/**
	 * Add this tag to prevent the next tab's activation from granting focus to
	 * the part. This is used to keep the focus on the CTF when traversing the
	 * tabs using the keyboard.
	 */
	private static final String INHIBIT_FOCUS = "InhibitFocus"; //$NON-NLS-1$

	// Minimum characters in for stacks outside the shared area
	private static int MIN_VIEW_CHARS = 1;

	// Minimum characters in for stacks inside the shared area
	private static int MIN_EDITOR_CHARS = 15;

	// View Menu / TB data constants
	private static final String TOP_RIGHT = "topRight"; //$NON-NLS-1$
	//private static final String MENU_TB = "menuTB"; //$NON-NLS-1$
	//private static final String PART_TB = "partTB"; //$NON-NLS-1$

	Image viewMenuImage;

	@Inject
	IStylingEngine stylingEngine;

	@Inject
	IEventBroker eventBroker;

	@Inject
	IPresentationEngine renderer;

	private EventHandler itemUpdater;

	private EventHandler dirtyUpdater;

	/**
	 * An event handler for listening to changes to the state of view menus and
	 * its child menu items. Depending on what state these items are in, the
	 * view menu should or should not be rendered in the tab folder.
	 */
	private EventHandler viewMenuUpdater;

	/**
	 * An event handler for listening to changes to the children of an element
	 * container. The tab folder may need to layout itself again if a part's
	 * toolbar has been changed.
	 */
	private EventHandler childrenHandler;
	private EventHandler tabStateHandler;

	// Manages CSS styling based on active part changes
	private EventHandler stylingHandler;

	private boolean ignoreTabSelChanges = false;

	// private ToolBar menuTB;
	// private boolean menuButtonShowing = false;

	// private Control partTB;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#requiresFocus
	 * (org.eclipse.e4.ui.model.application.ui.basic.MPart)
	 */
	@Override
	protected boolean requiresFocus(MPart element) {
		MUIElement inStack = element.getCurSharedRef() != null ? element
				.getCurSharedRef() : element;
		if (inStack.getParent() != null
				&& inStack.getParent().getTransientData()
						.containsKey(INHIBIT_FOCUS)) {
			inStack.getParent().getTransientData().remove(INHIBIT_FOCUS);
			return false;
		}

		return super.requiresFocus(element);
	}

	public StackRenderer() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init(eventBroker);

		// TODO: Refactor using findItemForPart(MPart) method
		itemUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				MUIElement element = (MUIElement) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(element instanceof MPart))
					return;

				MPart part = (MPart) element;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);
				Object newValue = event
						.getProperty(UIEvents.EventTags.NEW_VALUE);

				// is this a direct child of the stack?
				if (element.getParent() != null
						&& element.getParent().getRenderer() == StackRenderer.this) {
					CTabItem cti = findItemForPart(element, element.getParent());
					if (cti != null) {
						updateTab(cti, part, attName, newValue);
					}
					return;
				}

				// Do we have any stacks with place holders for the element
				// that's changed?
				MWindow win = modelService.getTopLevelWindowFor(part);
				List<MPlaceholder> refs = modelService.findElements(win, null,
						MPlaceholder.class, null);
				if (refs != null) {
					for (MPlaceholder ref : refs) {
						if (ref.getRef() != part)
							continue;

						MElementContainer<MUIElement> refParent = ref
								.getParent();
						// can be null, see bug 328296
						if (refParent != null
								&& refParent.getRenderer() instanceof StackRenderer) {
							CTabItem cti = findItemForPart(ref, refParent);
							if (cti != null) {
								updateTab(cti, part, attName, newValue);
							}
						}
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, itemUpdater);

		// TODO: Refactor using findItemForPart(MPart) method
		dirtyUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);

				// Ensure that this event is for a MMenuItem
				if (!(objElement instanceof MPart)) {
					return;
				}

				// Extract the data bits
				MPart part = (MPart) objElement;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);
				Object newValue = event
						.getProperty(UIEvents.EventTags.NEW_VALUE);

				// Is the part directly under the stack?
				MElementContainer<MUIElement> parent = part.getParent();
				if (parent != null
						&& parent.getRenderer() == StackRenderer.this) {
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
						MElementContainer<MUIElement> refParent = ref
								.getParent();
						if (refParent.getRenderer() instanceof StackRenderer) {
							CTabItem cti = findItemForPart(ref, refParent);
							if (cti != null) {
								updateTab(cti, part, attName, newValue);
							}
						}
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.Dirtyable.TOPIC,
				UIEvents.Dirtyable.DIRTY), dirtyUpdater);

		viewMenuUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);

				// Ensure that this event is for a MMenuItem
				if (!(objElement instanceof MMenuElement)) {
					return;
				}

				EObject parent = ((EObject) objElement).eContainer();
				while (parent instanceof MMenuElement) {
					MUIElement element = (MUIElement) parent;
					if (!element.isToBeRendered() || !element.isVisible()) {
						return;
					}

					objElement = parent;
					parent = parent.eContainer();
				}

				// if we're a view menu, the parent element is a part
				if (!(parent instanceof MPart)) {
					return;
				}

				MPart element = (MPart) parent;
				MUIElement parentElement = element.getParent();
				if (parentElement == null) {
					MPlaceholder placeholder = element.getCurSharedRef();
					if (placeholder == null) {
						return;
					}

					parentElement = placeholder.getParent();
					if (parentElement == null) {
						return;
					}
				}

				Object widget = parentElement.getWidget();
				if (widget instanceof CTabFolder) {
					Boolean newValue = (Boolean) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					CTabFolder folder = (CTabFolder) widget;
					if (newValue.booleanValue()) {
						if (getViewMenuTB(folder) == null) {
							disposeViewMenu(folder);
							setupMenuButton(element, folder);
							layoutTopRight(folder);
						}
					} else if (!isMenuVisible((MMenu) objElement)) {
						disposeViewMenu(folder);
					}
				}
			}
		};
		eventBroker
				.subscribe(UIEvents.UIElement.TOPIC_VISIBLE, viewMenuUpdater);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED,
				viewMenuUpdater);

		childrenHandler = new EventHandler() {
			public void handleEvent(Event event) {
				Object changedObj = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				// only interested in changes to toolbars
				if (!(changedObj instanceof MToolBar)) {
					return;
				}

				Object container = ((EObject) changedObj).eContainer();
				// check if this is a part's toolbar
				if (container instanceof MPart) {
					MElementContainer<?> parent = ((MPart) container)
							.getParent();
					// only relayout if this part is the selected element and we
					// actually rendered this element
					if (parent instanceof MPartStack
							&& parent.getSelectedElement() == container
							&& parent.getRenderer() == StackRenderer.this) {
						Object widget = parent.getWidget();
						if (widget instanceof CTabFolder) {
							layoutTopRight((CTabFolder) widget);
						}
					}
				}
			}
		};
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN,
				childrenHandler);

		stylingHandler = new EventHandler() {
			public void handleEvent(Event event) {
				MUIElement changed = (MUIElement) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(changed instanceof MPart))
					return;

				MPart newActivePart = (MPart) changed;
				MUIElement partParent = newActivePart.getParent();
				if (partParent == null
						&& newActivePart.getCurSharedRef() != null)
					partParent = newActivePart.getCurSharedRef().getParent();

				MPartStack pStack = (MPartStack) (partParent instanceof MPartStack ? partParent
						: null);

				EModelService ms = newActivePart.getContext().get(
						EModelService.class);
				List<String> tags = new ArrayList<String>();
				tags.add(CSSConstants.CSS_ACTIVE_CLASS);
				List<MUIElement> activeElements = ms.findElements(
						ms.getTopLevelWindowFor(newActivePart), null,
						MUIElement.class, tags);
				for (MUIElement element : activeElements) {
					if (element instanceof MPartStack && element != pStack) {
						styleElement(element, false);
					} else if (element instanceof MPart
							&& element != newActivePart) {
						styleElement(element, false);
					}
				}

				if (pStack != null)
					styleElement(pStack, true);
				styleElement(newActivePart, true);
			}
		};
		eventBroker.subscribe(UIEvents.UILifeCycle.ACTIVATE, stylingHandler);

		tabStateHandler = new TabStateHandler();
		eventBroker.subscribe(UIEvents.ApplicationElement.TOPIC_TAGS,
				tabStateHandler);
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT,
				tabStateHandler);
	}

	/**
	 * Determines if the menu provided or any one of its children should be
	 * rendered.
	 * 
	 * @param menu
	 *            the menu to determine if it should be displayed in the tab
	 *            folder
	 * @return <tt>true</tt> if the menu should be drawn in the tab folder,
	 *         <tt>false</tt> otherwise
	 */
	private boolean isMenuVisible(MMenu menu) {
		if (menu.isToBeRendered() && menu.isVisible()) {
			for (MMenuElement element : menu.getChildren()) {
				if (element.isToBeRendered() && element.isVisible()) {
					return true;
				} else if (element instanceof MMenu
						&& isMenuVisible((MMenu) element)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void updateTab(CTabItem cti, MPart part, String attName,
			Object newValue) {
		if (UIEvents.UILabel.LABEL.equals(attName)) {
			String newName = (String) newValue;
			cti.setText(getLabel(part, newName));
		} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
			cti.setImage(getImage(part));
		} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
			String newTTip = (String) newValue;
			cti.setToolTipText(getToolTip(newTTip));
		} else if (UIEvents.Dirtyable.DIRTY.equals(attName)) {
			Boolean dirtyState = (Boolean) newValue;
			String text = cti.getText();
			boolean hasAsterisk = text.length() > 0 && text.charAt(0) == '*';
			if (dirtyState.booleanValue()) {
				if (!hasAsterisk) {
					cti.setText('*' + text);
				}
			} else if (hasAsterisk) {
				cti.setText(text.substring(1));
			}
		}
	}

	@PreDestroy
	public void contextDisposed() {
		super.contextDisposed(eventBroker);

		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(dirtyUpdater);
		eventBroker.unsubscribe(viewMenuUpdater);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(stylingHandler);
		eventBroker.unsubscribe(tabStateHandler);
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
		return newToolTip == null || newToolTip.length() == 0 ? null
				: LegacyActionTools.escapeMnemonics(newToolTip);
	}

	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPartStack) || !(parent instanceof Composite))
			return null;

		Composite parentComposite = (Composite) parent;

		// Ensure that all rendered PartStacks have an Id
		if (element.getElementId() == null
				|| element.getElementId().length() == 0) {
			String generatedId = "PartStack@" + Integer.toHexString(element.hashCode()); //$NON-NLS-1$
			element.setElementId(generatedId);
		}

		// TBD: need to define attributes to handle this
		final CTabFolder ctf = new CTabFolder(parentComposite, SWT.BORDER);
		ctf.setMRUVisible(getInitialMRUValue(ctf));

		// Adjust the minimum chars based on the location
		int location = modelService.getElementLocation(element);
		if ((location & EModelService.IN_SHARED_AREA) != 0) {
			ctf.setMinimumCharacters(MIN_EDITOR_CHARS);
			ctf.setUnselectedCloseVisible(true);
		} else {
			ctf.setMinimumCharacters(MIN_VIEW_CHARS);
			ctf.setUnselectedCloseVisible(false);
		}

		bindWidget(element, ctf); // ?? Do we need this ?

		// Add a composite to manage the view's TB and Menu
		addTopRight(ctf);

		return ctf;
	}

	private boolean getInitialMRUValue(Control control) {
		boolean result = false;
		CSSRenderingUtils util = context.get(CSSRenderingUtils.class);
		if (util == null)
			return result;

		CSSValue value = util.getCSSValue(control,
				"MPartStack", "swt-mru-visible"); //$NON-NLS-1$ //$NON-NLS-2$

		if (value == null) {
			value = util.getCSSValue(control, "MPartStack", "mru-visible"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (value == null)
			return result;

		return Boolean.parseBoolean(value.getCssText());
	}

	/**
	 * @param ctf
	 */
	private void addTopRight(CTabFolder ctf) {
		Composite trComp = new Composite(ctf, SWT.NONE);
		trComp.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_CYAN));
		RowLayout rl = new RowLayout();
		trComp.setLayout(rl);
		rl.marginBottom = rl.marginTop = rl.marginRight = rl.marginLeft = 0;
		ctf.setData(TOP_RIGHT, trComp);
		ctf.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Composite c = (Composite) e.widget.getData(TOP_RIGHT);
				if (c != null && !c.isDisposed())
					c.dispose();
			}
		});
	}

	private Composite getTRComposite(CTabFolder ctf) {
		return (Composite) ctf.getData(TOP_RIGHT);
	}

	/**
	 * Disposes of the view menu associated with the given tab folder.
	 * 
	 * @param ctf
	 *            the tab folder to clear of its view menu
	 */
	public void disposeViewMenu(CTabFolder ctf) {
		ToolBar vmTB = getViewMenuTB(ctf);
		if (vmTB != null && !vmTB.isDisposed())
			vmTB.dispose();
	}

	public void clearTR(CTabFolder ctf) {
		disposeViewMenu(ctf);
		ToolBar vmTB = getViewMenuTB(ctf);
		if (vmTB != null && !vmTB.isDisposed())
			vmTB.dispose();

		MToolBar viewTBModel = getViewTB(ctf);
		if (viewTBModel != null && viewTBModel.getWidget() != null)
			viewTBModel.setVisible(false);

		ctf.setTopRight(null);
		getTRComposite(ctf).setVisible(false);
	}

	public void adjustTR(final CTabFolder ctf, MPart part) {
		// Clear the current info
		clearTR(ctf);

		if (part == null)
			return;

		// Show the TB, create one if necessary
		if (part.getToolbar() != null && part.getToolbar().isToBeRendered()) {
			part.getToolbar().setVisible(true);
			Object tbObj = renderer.createGui(part.getToolbar(),
					getTRComposite(ctf), part.getContext());
			// The TB renderer actually wraps the TB in a Composite, if the TB
			// changes size we need to re-pack the composite
			if (tbObj instanceof Composite) {
				Control[] kids = ((Composite) tbObj).getChildren();
				for (Control kid : kids) {
					if (kid instanceof ToolBar) {
						kid.addControlListener(new ControlListener() {
							public void controlResized(ControlEvent e) {
								// Force a layout of the TB / CTF
								if (!ctf.isDisposed()) {
									Control tr = ctf.getTopRight();
									if (tr != null && !tr.isDisposed())
										tr.pack();
								}
							}

							public void controlMoved(ControlEvent e) {
							}
						});
					}
				}
			}
		}

		setupMenuButton(part, ctf);
		layoutTopRight(ctf);
	}

	/**
	 * Asks the specified tab folder to layout its top right control.
	 * 
	 * @param ctf
	 *            the tab folder that should be laid out
	 */
	public void layoutTopRight(CTabFolder ctf) {
		Composite trComp = getTRComposite(ctf);
		if (trComp.getChildren().length > 0) {
			trComp.setVisible(true);
			ctf.setTopRight(trComp, SWT.RIGHT | SWT.WRAP);
		} else {
			ctf.setTopRight(null);
			trComp.setVisible(false);
		}

		trComp.pack();
		trComp.layout(true, true);
	}

	private MToolBar getViewTB(CTabFolder ctf) {
		Composite trComp = (Composite) ctf.getData(TOP_RIGHT);

		// The view menu TB *is* modeled so it's OWNING_ME != null
		for (Control kid : trComp.getChildren()) {
			if (kid.getData(OWNING_ME) instanceof MToolBar)
				return (MToolBar) kid.getData(OWNING_ME);
		}
		return null;
	}

	private ToolBar getViewMenuTB(CTabFolder ctf) {
		Composite trComp = (Composite) ctf.getData(TOP_RIGHT);

		// The view menu TB is not modeled so it's OWNING_ME == null
		for (Control kid : trComp.getChildren()) {
			if (kid.getData(OWNING_ME) == null
					&& TAG_VIEW_MENU.equals(kid.getData()))
				return (ToolBar) kid;
		}
		return null;
	}

	protected void createTab(MElementContainer<MUIElement> stack,
			MUIElement element) {
		MPart part = null;
		if (element instanceof MPart)
			part = (MPart) element;
		else if (element instanceof MPlaceholder) {
			part = (MPart) ((MPlaceholder) element).getRef();
			part.setCurSharedRef((MPlaceholder) element);
		}

		CTabFolder ctf = (CTabFolder) stack.getWidget();

		CTabItem cti = findItemForPart(element, stack);
		if (cti != null) {
			if (element.getWidget() != null
					&& cti.getControl() != element.getWidget())
				cti.setControl((Control) element.getWidget());
			return;
		}

		int createFlags = SWT.NONE;
		if (part != null && isClosable(part)) {
			createFlags |= SWT.CLOSE;
		}

		// Create the tab; we may have more visible tabs than currently shown
		// (e.g., a result of calling partStack.getChildren().addAll(partList))
		int index = Math.min(calcIndexFor(stack, element), ctf.getItemCount());
		cti = new CTabItem(ctf, createFlags, index);

		cti.setData(OWNING_ME, element);
		cti.setText(getLabel(part, part.getLocalizedLabel()));
		cti.setImage(getImage(part));
		cti.setToolTipText(getToolTip(part.getLocalizedTooltip()));
		if (element.getWidget() != null) {
			// The part might have a widget but may not yet have been placed
			// under this stack, check this
			Control ctrl = (Control) element.getWidget();
			if (ctrl.getParent() == ctf)
				cti.setControl((Control) element.getWidget());
		}
	}

	private int calcIndexFor(MElementContainer<MUIElement> stack,
			final MUIElement part) {
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
	public void childRendered(
			final MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);

		if (!(((MUIElement) parentElement) instanceof MPartStack)
				|| !(element instanceof MStackElement))
			return;

		createTab(parentElement, element);
	}

	private CTabItem findItemForPart(MUIElement element,
			MElementContainer<MUIElement> stack) {
		if (stack == null)
			stack = element.getParent();

		CTabFolder ctf = (CTabFolder) stack.getWidget();
		if (ctf == null)
			return null;

		CTabItem[] items = ctf.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData(OWNING_ME) == element)
				return items[i];
		}
		return null;
	}

	public CTabItem findItemForPart(MPart part) {
		// is this a direct child of the stack?
		if (part.getParent() != null
				&& part.getParent().getRenderer() == StackRenderer.this) {
			CTabItem cti = findItemForPart(part, part.getParent());
			if (cti != null) {
				return cti;
			}
		}

		// Do we have any stacks with place holders for the element
		// that's changed?
		MWindow win = modelService.getTopLevelWindowFor(part);
		List<MPlaceholder> refs = modelService.findElements(win, null,
				MPlaceholder.class, null);
		if (refs != null) {
			for (MPlaceholder ref : refs) {
				if (ref.getRef() != part)
					continue;

				MElementContainer<MUIElement> refParent = ref.getParent();
				// can be null, see bug 328296
				if (refParent != null
						&& refParent.getRenderer() instanceof StackRenderer) {
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
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		CTabFolder ctf = (CTabFolder) parentElement.getWidget();
		if (ctf == null)
			return;

		// find the 'stale' tab for this element and dispose it
		CTabItem cti = findItemForPart(child, parentElement);
		if (cti != null && !cti.isDisposed()) {
			cti.setControl(null);
			cti.dispose();
		}

		// Check if we have to reset the currently active child for the stack
		if (parentElement.getSelectedElement() == child) {
			clearTR(ctf);
		} else {
			if (child instanceof MPlaceholder) {
				MPlaceholder placeholder = (MPlaceholder) child;
				child = placeholder.getRef();

				if (child == null || child.getCurSharedRef() != placeholder) {
					// if this placeholder isn't currently managing this
					// element, no need to do anything about its toolbar, just
					// return here
					return;
				}
			}

			if (child instanceof MPart) {
				MToolBar toolbar = ((MPart) child).getToolbar();
				if (toolbar != null) {
					toolbar.setVisible(false);
				}
			}
		}
	}

	@Override
	public void hookControllerLogic(final MUIElement me) {
		super.hookControllerLogic(me);

		if (!(me instanceof MElementContainer<?>))
			return;

		final MElementContainer<MUIElement> stack = (MElementContainer<MUIElement>) me;

		// Match the selected TabItem to its Part
		final CTabFolder ctf = (CTabFolder) me.getWidget();

		// Handle traverse events for accessibility
		ctf.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ARROW_NEXT
						|| e.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					me.getTransientData().put(INHIBIT_FOCUS, true);
				} else if (e.detail == SWT.TRAVERSE_RETURN) {
					me.getTransientData().remove(INHIBIT_FOCUS);
					CTabItem cti = ctf.getSelection();
					if (cti != null) {
						MUIElement stackElement = (MUIElement) cti
								.getData(OWNING_ME);
						if (stackElement instanceof MPlaceholder)
							stackElement = ((MPlaceholder) stackElement)
									.getRef();
						if ((stackElement instanceof MPart)
								&& (ctf.isFocusControl())) {
							MPart thePart = (MPart) stackElement;
							renderer.focusGui(thePart);
						}
					}
				}
			}
		});

		// Detect activation...picks up cases where the user clicks on the
		// (already active) tab
		ctf.addListener(SWT.Activate, new org.eclipse.swt.widgets.Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				if (event.detail == SWT.MouseDown) {
					CTabFolder ctf = (CTabFolder) event.widget;
					if (ctf.getSelection() == null)
						return;

					// get the item under the cursor
					Point cp = event.display.getCursorLocation();
					cp = event.display.map(null, ctf, cp);
					CTabItem overItem = ctf.getItem(cp);

					// If the item we're over is *not* the current one do
					// nothing (it'll get activated when the tab changes)
					if (overItem == null || overItem == ctf.getSelection()) {
						MUIElement uiElement = (MUIElement) ctf.getSelection()
								.getData(OWNING_ME);
						if (uiElement instanceof MPlaceholder)
							uiElement = ((MPlaceholder) uiElement).getRef();
						if (uiElement instanceof MPart)
							activate((MPart) uiElement);
					}
				}
			}
		});

		ctf.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// prevent recursions
				if (ignoreTabSelChanges)
					return;

				MUIElement ele = (MUIElement) e.item.getData(OWNING_ME);
				ele.getParent().setSelectedElement(ele);
				if (ele instanceof MPlaceholder)
					ele = ((MPlaceholder) ele).getRef();
				if (ele instanceof MPart)
					activate((MPart) ele);
			}
		});

		MouseListener mouseListener = new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CTabItem item = ctf.getSelection();
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
				CTabItem item = ctf.getItem(new Point(e.x, e.y));

				// If the user middle clicks on a tab, close it
				if (item != null && e.button == 2) {
					closePart(item, false);
				}

				// If the user clicks on the tab or empty stack space, call
				// setFocus()
				if (e.button == 1) {
					if (item == null) {
						Rectangle clientArea = ctf.getClientArea();
						if (!clientArea.contains(e.x, e.y)) {
							// User clicked in empty space
							item = ctf.getSelection();
						}
					}

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
			}
		};
		ctf.addMouseListener(mouseListener);

		CTabFolder2Adapter closeListener = new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				event.doit = closePart(event.item, true);
			}

			@Override
			public void showList(CTabFolderEvent event) {
				event.doit = false;
				showAvailableItems(stack, ctf);
			}
		};
		ctf.addCTabFolder2Listener(closeListener);

		ctf.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				Point absolutePoint = new Point(e.x, e.y);
				Point relativePoint = ctf.getDisplay().map(null, ctf,
						absolutePoint);
				CTabItem eventTabItem = ctf.getItem(relativePoint);

				// If click happened in empty area, still show the menu
				if (eventTabItem == null) {
					Rectangle clientArea = ctf.getClientArea();
					if (!clientArea.contains(relativePoint)) {
						eventTabItem = ctf.getSelection();
					}
				}

				if (eventTabItem != null) {
					MUIElement uiElement = (MUIElement) eventTabItem
							.getData(AbstractPartRenderer.OWNING_ME);
					MPart tabPart = (MPart) ((uiElement instanceof MPart) ? uiElement
							: ((MPlaceholder) uiElement).getRef());
					openMenuFor(tabPart, ctf, absolutePoint);
				}
			}
		});
	}

	public void showAvailableItems(MElementContainer<?> stack, CTabFolder ctf) {
		IEclipseContext ctxt = getContext(stack);
		final BasicPartList editorList = new BasicPartList(ctf.getShell(),
				SWT.ON_TOP, SWT.V_SCROLL | SWT.H_SCROLL,
				ctxt.get(EPartService.class), stack, this, ctf,
				(ISWTResourceUtilities) ctxt.get(IResourceUtilities.class),
				getInitialMRUValue(ctf));
		editorList.setInput();

		Point size = editorList.computeSizeHint();
		editorList.setSize(size.x, size.y);

		Point location = ctf.toDisplay(getChevronLocation(ctf));
		Monitor mon = ctf.getMonitor();
		Rectangle bounds = mon.getClientArea();
		if (location.x + size.x > bounds.x + bounds.width) {
			location.x = bounds.x + bounds.width - size.x;
		}
		if (location.y + size.y > bounds.y + bounds.height) {
			location.y = bounds.y + bounds.height - size.y;
		}
		editorList.setLocation(location);

		editorList.setVisible(true);
		editorList.setFocus();
		editorList.getShell().addListener(SWT.Deactivate, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				editorList.getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						editorList.dispose();
					}
				});
			}
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
	 * @param widget
	 *            the part that owns this widget
	 * @param check
	 *            <tt>true</tt> if the part should be checked to see if it has
	 *            been defined as being not closeable for users, <tt>false</tt>
	 *            if this check should not be performed
	 * @return <tt>true</tt> if the part was closed, <tt>false</tt> otherwise
	 */
	private boolean closePart(Widget widget, boolean check) {
		MUIElement uiElement = (MUIElement) widget
				.getData(AbstractPartRenderer.OWNING_ME);
		MPart part = (MPart) ((uiElement instanceof MPart) ? uiElement
				: ((MPlaceholder) uiElement).getRef());
		if (!check && !isClosable(part)) {
			return false;
		}

		IEclipseContext partContext = part.getContext();
		IEclipseContext parentContext = getContextForParent(part);
		// a part may not have a context if it hasn't been rendered
		IEclipseContext context = partContext == null ? parentContext
				: partContext;
		// Allow closes to be 'canceled'
		EPartService partService = (EPartService) context
				.get(EPartService.class.getName());
		if (partService.savePart(part, true)) {
			partService.hidePart(part);
			return true;
		}
		// the user has canceled out of the save operation, so don't close the
		// part
		return false;
	}

	protected void showTab(MUIElement element) {
		super.showTab(element);

		// an invisible element won't have the correct widget hierarchy
		if (!element.isVisible()) {
			return;
		}

		final CTabFolder ctf = (CTabFolder) getParentWidget(element);
		CTabItem cti = findItemForPart(element, null);
		if (cti == null) {
			createTab(element.getParent(), element);
			cti = findItemForPart(element, element.getParent());
		}
		Control ctrl = (Control) element.getWidget();
		if (ctrl != null && ctrl.getParent() != ctf) {
			ctrl.setParent(ctf);
			cti.setControl(ctrl);
		} else if (element.getWidget() == null) {
			Control tabCtrl = (Control) renderer.createGui(element);
			cti.setControl(tabCtrl);
		}

		ignoreTabSelChanges = true;
		// Ensure that the newly selected control is correctly sized
		if (cti.getControl() instanceof Composite) {
			Composite ctiComp = (Composite) cti.getControl();
			ctiComp.layout(true, true);
		}
		ctf.setSelection(cti);
		ignoreTabSelChanges = false;

		// Clear out the current Top Right info
		MPart part = (MPart) ((element instanceof MPart) ? element
				: ((MPlaceholder) element).getRef());
		adjustTR(ctf, part);
	}

	/**
	 * Creates a view menu for the given part in the contained tab folder.
	 * 
	 * @param part
	 *            the part that should have its view menu created
	 * @param ctf
	 *            the containing tab folder
	 */
	public void setupMenuButton(MPart part, CTabFolder ctf) {
		MMenu viewMenu = getViewMenu(part);

		// View menu (if any)
		if (viewMenu != null && hasVisibleMenuItems(viewMenu, part)) {
			showMenuButton(part, ctf, viewMenu);
		} else {
			// hide the menu's TB
			ToolBar menuTB = getViewMenuTB(ctf);
			if (menuTB != null) {
				menuTB.dispose();
			}
		}
	}

	private void showMenuButton(MPart part, CTabFolder ctf, MMenu menu) {
		ToolBar menuTB = getViewMenuTB(ctf);
		if (menuTB == null) {
			menuTB = new ToolBar(getTRComposite(ctf), SWT.FLAT | SWT.RIGHT);
			menuTB.setData(TAG_VIEW_MENU);
			ToolItem ti = new ToolItem(menuTB, SWT.PUSH);
			ti.setImage(getViewMenuImage());
			ti.setHotImage(null);
			ti.setToolTipText(SWTRenderersMessages.viewMenu);

			ti.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					showMenu((ToolItem) e.widget);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					showMenu((ToolItem) e.widget);
				}
			});
			menuTB.getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						public void getName(AccessibleEvent e) {
							if (e.childID != ACC.CHILDID_SELF) {
								Accessible accessible = (Accessible) e
										.getSource();
								ToolBar toolBar = (ToolBar) accessible
										.getControl();
								if (0 <= e.childID
										&& e.childID < toolBar.getItemCount()) {
									ToolItem item = toolBar.getItem(e.childID);
									if (item != null) {
										e.result = item.getToolTipText();
									}
								}
							}
						}
					});
		}

		ToolItem ti = menuTB.getItem(0);
		ti.setData("theMenu", menu); //$NON-NLS-1$
		ti.setData("thePart", part); //$NON-NLS-1$
	}

	/**
	 * @param item
	 */
	protected void showMenu(ToolItem item) {
		// Create the UI for the menu
		final MMenu menuModel = (MMenu) item.getData("theMenu"); //$NON-NLS-1$
		Menu menu = null;
		Object obj = menuModel.getWidget();
		if (obj instanceof Menu) {
			menu = (Menu) obj;
		}
		if (menu == null || menu.isDisposed()) {
			MPart part = (MPart) item.getData("thePart"); //$NON-NLS-1$
			Control ctrl = (Control) part.getWidget();
			final Menu tmpMenu = (Menu) renderer.createGui(menuModel,
					ctrl.getShell(), part.getContext());
			menu = tmpMenu;
			if (tmpMenu != null) {
				ctrl.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (!tmpMenu.isDisposed()) {
							tmpMenu.dispose();
						}
					}
				});
			}
		}
		if (menu == null) {
			return;
		}

		// ...and Show it...
		Rectangle ib = item.getBounds();
		Point displayAt = item.getParent().toDisplay(ib.x, ib.y + ib.height);
		menu.setLocation(displayAt);
		menu.setVisible(true);

		Display display = Display.getCurrent();
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		if (!menu.isDisposed() && !(menu.getData() instanceof MenuManager)) {
			menu.dispose();
		}
	}

	private Image getViewMenuImage() {
		if (viewMenuImage == null) {
			Display d = Display.getCurrent();

			Image viewMenu = new Image(d, 16, 16);
			Image viewMenuMask = new Image(d, 16, 16);

			Display display = Display.getCurrent();
			GC gc = new GC(viewMenu);
			GC maskgc = new GC(viewMenuMask);
			gc.setForeground(display
					.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
			gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			int[] shapeArray = new int[] { 6, 1, 15, 1, 11, 5, 10, 5 };
			gc.fillPolygon(shapeArray);
			gc.drawPolygon(shapeArray);

			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			Color white = display.getSystemColor(SWT.COLOR_WHITE);

			maskgc.setBackground(black);
			maskgc.fillRectangle(0, 0, 16, 16);

			maskgc.setBackground(white);
			maskgc.setForeground(white);
			maskgc.fillPolygon(shapeArray);
			maskgc.drawPolygon(shapeArray);
			gc.dispose();
			maskgc.dispose();

			ImageData data = viewMenu.getImageData();
			data.transparentPixel = data.getPixel(0, 0);

			viewMenuImage = new Image(d, viewMenu.getImageData(),
					viewMenuMask.getImageData());
			viewMenu.dispose();
			viewMenuMask.dispose();
		}
		return viewMenuImage;
	}

	private void openMenuFor(MPart part, CTabFolder folder, Point point) {
		Menu tabMenu = createTabMenu(folder, part);
		tabMenu.setData(STACK_SELECTED_PART, part);
		tabMenu.setLocation(point.x, point.y);
		tabMenu.setVisible(true);
	}

	private boolean isClosable(MPart part) {
		// if it's a shared part check its current ref
		if (part.getCurSharedRef() != null) {
			return !(part.getCurSharedRef().getTags()
					.contains(IPresentationEngine.NO_CLOSE));
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

		int closeableElements = 0;
		if (isClosable(part)) {
			MenuItem menuItemClose = new MenuItem(menu, SWT.NONE);
			menuItemClose.setText(SWTRenderersMessages.menuClose);
			menuItemClose.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					MPart part = (MPart) menu.getData(STACK_SELECTED_PART);
					EPartService partService = getContextForParent(part).get(
							EPartService.class);
					if (partService.savePart(part, true))
						partService.hidePart(part);
				}
			});
			closeableElements++;
		}

		MElementContainer<MUIElement> parent = getParent(part);
		if (parent != null) {
			closeableElements += getCloseableSiblingParts(part).size();

			if (closeableElements >= 2) {
				MenuItem menuItemOthers = new MenuItem(menu, SWT.NONE);
				menuItemOthers.setText(SWTRenderersMessages.menuCloseOthers);
				menuItemOthers.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						MPart part = (MPart) menu.getData(STACK_SELECTED_PART);
						closeSiblingParts(part, true);
					}
				});

				MenuItem menuItemAll = new MenuItem(menu, SWT.NONE);
				menuItemAll.setText(SWTRenderersMessages.menuCloseAll);
				menuItemAll.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						MPart part = (MPart) menu.getData(STACK_SELECTED_PART);
						closeSiblingParts(part, false);
					}
				});
			}
		}

		return menu;
	}

	private MElementContainer<MUIElement> getParent(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			return placeholder == null ? null : placeholder.getParent();
		}
		return parent;
	}

	private List<MPart> getCloseableSiblingParts(MPart part) {
		// broken out from closeSiblingParts so it can be used to determine how
		// many closeable siblings are available
		MElementContainer<MUIElement> container = getParent(part);
		List<MPart> closeableSiblings = new ArrayList<MPart>();
		if (container == null)
			return closeableSiblings;

		List<MUIElement> children = container.getChildren();
		for (MUIElement child : children) {
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

	private void closeSiblingParts(MPart part, boolean skipThisPart) {
		MElementContainer<MUIElement> container = getParent(part);
		if (container == null)
			return;

		List<MPart> others = getCloseableSiblingParts(part);

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

		EPartService partService = getContextForParent(part).get(
				EPartService.class);
		// try using the ISaveHandler first... This gives better control of
		// dialogs...
		ISaveHandler saveHandler = getContextForParent(part).get(
				ISaveHandler.class);
		if (saveHandler != null) {
			final List<MPart> toPrompt = new ArrayList<MPart>(others);
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
		if (part.getMenus() == null) {
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
	 * @param viewMenu
	 *            the view menu to check
	 * @param part
	 *            the view menu's parent part
	 * @return <tt>true</tt> if the specified view menu has visible children,
	 *         <tt>false</tt> otherwise
	 */
	private boolean hasVisibleMenuItems(MMenu viewMenu, MPart part) {
		if (!viewMenu.isToBeRendered() || !viewMenu.isVisible()) {
			return false;
		}

		for (MMenuElement menuElement : viewMenu.getChildren()) {
			if (menuElement.isToBeRendered() && menuElement.isVisible()) {
				if (menuElement instanceof MOpaqueMenuItem) {
					IContributionItem item = (IContributionItem) ((MOpaqueMenuItem) menuElement)
							.getOpaqueItem();
					if (item != null && item.isVisible()) {
						return true;
					}
				} else if (menuElement instanceof MOpaqueMenuSeparator) {
					IContributionItem item = (IContributionItem) ((MOpaqueMenuSeparator) menuElement)
							.getOpaqueItem();
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
			MenuManager manager = ((MenuManagerRenderer) menuRenderer)
					.getManager(viewMenu);
			if (manager != null && manager.isVisible()) {
				return true;
			}
		}

		Control control = (Control) part.getWidget();
		if (control != null) {
			Menu menu = (Menu) renderer.createGui(viewMenu, control.getShell(),
					part.getContext());
			if (menu != null) {
				menuRenderer = viewMenu.getRenderer();
				if (menuRenderer instanceof MenuManagerRenderer) {
					MenuManagerRenderer menuManagerRenderer = (MenuManagerRenderer) menuRenderer;
					MenuManager manager = menuManagerRenderer
							.getManager(viewMenu);
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

	@SuppressWarnings("javadoc")
	public class TabStateHandler implements EventHandler {
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);

			if (!validateElement(element)
					|| !validateValues(oldValue, newValue)) {
				return;
			}

			MPart part = newValue instanceof MPlaceholder ? (MPart) ((MPlaceholder) newValue)
					.getRef() : (MPart) element;
			CTabItem cti = findItemForPart(part);

			if (cti == null) {
				return;
			}

			if (CSSConstants.CSS_CONTENT_CHANGE_CLASS.equals(newValue)) {
				part.getTags().remove(CSSConstants.CSS_CONTENT_CHANGE_CLASS);
				if (cti != cti.getParent().getSelection()) {
					part.getTags().add(CSSConstants.CSS_HIGHLIGHTED_CLASS);
				}
			} else if (newValue instanceof MPlaceholder // part gets active
					&& part.getTags().contains(
							CSSConstants.CSS_HIGHLIGHTED_CLASS)) {
				part.getTags().remove(CSSConstants.CSS_HIGHLIGHTED_CLASS);
			}

			setCSSInfo(part, cti);
			reapplyStyles(cti.getParent());
		}

		public boolean validateElement(Object element) {
			return element instanceof MPart || element instanceof MPartStack;
		}

		public boolean validateValues(Object oldValue, Object newValue) {
			return newValue instanceof MPlaceholder // part gets active
					|| isTagAdded(CSSConstants.CSS_BUSY_CLASS, oldValue,
							newValue) // part gets busy
					|| isTagRemoved(CSSConstants.CSS_BUSY_CLASS, oldValue,
							newValue) // part gets idle
					|| isTagAdded(CSSConstants.CSS_CONTENT_CHANGE_CLASS,
							oldValue, newValue); // content of part changed
		}

		private boolean isTagAdded(String tagName, Object oldValue,
				Object newValue) {
			return oldValue == null && tagName.equals(newValue);
		}

		private boolean isTagRemoved(String tagName, Object oldValue,
				Object newValue) {
			return newValue == null && tagName.equals(oldValue);
		}
	}
}
