/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.split.renderer.swt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.BasicPartList;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.SWTRenderersMessages;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.MApplication;
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
import org.eclipse.e4.ui.workbench.renderers.swt.ElementReferenceRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.LazyStackRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IContributionItem;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.w3c.dom.css.CSSValue;

//TODO Forked from da787fbdd1de87b5598aebe7b2c03f8278a78613
@SuppressWarnings("restriction")
public class SplitStackRenderer extends LazyStackRenderer {

	public static final String TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$
	private static final String SHELL_CLOSE_EDITORS_MENU = "shell_close_editors_menu"; //$NON-NLS-1$
	private static final String STACK_SELECTED_PART = "stack_selected_part"; //$NON-NLS-1$

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

	private boolean ignoreTabSelChanges = false;

	private ActivationJob activationJob = null;

	@Inject
	private MApplication application;

	// private ToolBar menuTB;
	// private boolean menuButtonShowing = false;

	// private Control partTB;

	private class ActivationJob implements Runnable {

		/**
		 * Returns whether it is acceptable for a stack to be activated. As the
		 * activation occurs asynchronously, the original activation request may
		 * have been invalidated since the request was originally enqueued.
		 * <p>
		 * For example, an activation request that was enqueued no longer should
		 * be honoured if a dialog window gets opened in the interim.
		 * </p>
		 * 
		 * @return <code>true</code> if the requested stack should be activated,
		 *         <code>false</code> otherwise
		 */
		private boolean shouldActivate() {
			if (application != null) {
				IEclipseContext applicationContext = application.getContext();
				IEclipseContext activeChild = applicationContext
						.getActiveChild();
				if (activeChild == null
						|| activeChild.get(MWindow.class) != application
								.getSelectedElement()
						|| application.getSelectedElement() != modelService
								.getTopLevelWindowFor(stackToActivate)) {
					return false;
				}
			}
			return true;
		}

		public MElementContainer<MUIElement> stackToActivate = null;

		public void run() {
			activationJob = null;
			if (stackToActivate != null
					&& stackToActivate.getSelectedElement() != null
					&& shouldActivate()) {
				// Ensure we're activating a stack in the current perspective,
				// when using a dialog to open a perspective
				// we end up in the situation where this stack is in the
				// previously active perspective
				int location = modelService.getElementLocation(stackToActivate);
				if ((location & EModelService.IN_ACTIVE_PERSPECTIVE) == 0
						&& (location & EModelService.OUTSIDE_PERSPECTIVE) == 0
						&& (location & EModelService.IN_SHARED_AREA) == 0)
					return;

				MUIElement selElement = stackToActivate.getSelectedElement();
				if (!isValid(selElement))
					return;
				
				//TODO Change for 8009
				activate(getPart(selElement));
			}
		}
	}
	
	//TODO Change for 8009
	private MPart getPart(MUIElement element) {
		if (element instanceof MPart) {
			return (MPart) element;
		} else if (element instanceof MPlaceholder) {
			return (MPart) ((MPlaceholder) element).getRef();
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			Object selectedElement = container.getSelectedElement();
			MPart part = getPart((MUIElement) selectedElement);
			if (part != null) {
				return part;
			}
			for (Object o : ((MElementContainer) element).getChildren()) {
				if (o instanceof MPart) {
					return (MPart) o;
				}
			}
		}
		return null;
	}

	private boolean isValid(MUIElement element) {
		if (element == null || !element.isToBeRendered()) {
			return false;
		}

		if (element instanceof MApplication) {
			return true;
		}

		MUIElement parent = element.getParent();
		if (parent == null && element instanceof MWindow) {
			// might be a detached window
			parent = (MUIElement) ((EObject) element).eContainer();
		}

		if (parent == null) {
			// might be a shared part, try to find the placeholder
			MWindow window = modelService.getTopLevelWindowFor(element);
			return window == null ? false : isValid(modelService
					.findPlaceholderFor(window, element));
		}

		return isValid(parent);
	}

	synchronized private void activateStack(MElementContainer<MUIElement> stack) {
		CTabFolder ctf = (CTabFolder) stack.getWidget();
		if (ctf == null || ctf.isDisposed())
			return;

		if (activationJob == null) {
			activationJob = new ActivationJob();
			activationJob.stackToActivate = stack;
			ctf.getDisplay().asyncExec(activationJob);
		} else {
			activationJob.stackToActivate = stack;
		}
	}

	public SplitStackRenderer() {
		super();
	}

	@PostConstruct
	public void init() {
		super.init(eventBroker);

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
						&& element.getParent().getRenderer() == SplitStackRenderer.this) {
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
								&& refParent.getRenderer() instanceof SplitStackRenderer) {
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
				
				//TODO Change for 8009
				if (parent != null) {
					if (parent.getRenderer() != SplitStackRenderer.this) {
						objElement = parent;
						parent = parent.getParent();
					}
				}
				
				if (parent != null
						&& parent.getRenderer() == SplitStackRenderer.this) {
					CTabItem cti = findItemForPart(part, parent);
					if (cti != null) {
						updateTab(cti, part, attName, newValue);
					}
					return;
				}

				// Do we have any stacks with place holders for the element
				// that's changed?
				// FIXME this method needs a new implementation
				List<MPlaceholder> refs = new ArrayList<MPlaceholder>(); 
						//ElementReferenceRenderer
						//.getRenderedPlaceholders(part);
				for (MPlaceholder ref : refs) {
					MElementContainer<MUIElement> refParent = ref.getParent();
					if (refParent.getRenderer() instanceof SplitStackRenderer) {
						CTabItem cti = findItemForPart(ref, refParent);
						if (cti != null) {
							updateTab(cti, part, attName, newValue);
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
							&& parent.getRenderer() == SplitStackRenderer.this) {
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
			cti.setToolTipText(newTTip);
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
	}

	private String getLabel(MUILabel itemPart, String newName) {
		if (newName == null) {
			newName = ""; //$NON-NLS-1$
		}
		if (itemPart instanceof MDirtyable && ((MDirtyable) itemPart).isDirty()) {
			newName = '*' + newName;
		}
		return newName;
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
			// The TB renderer actually wraps the TB in a Composite
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
										ctf.getTopRight().pack();
									ctf.layout(true, true);
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
		ctf.layout(true, true);
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
		//TODO Change for 8009
		MPart part = getPart(element);
		
		if (element instanceof MPlaceholder) {
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

		// Create the tab
		int index = calcIndexFor(stack, element);
		cti = new CTabItem(ctf, createFlags, index);

		cti.setData(OWNING_ME, element);
		cti.setText(getLabel(part, part.getLocalizedLabel()));
		cti.setImage(getImage(part));
		cti.setToolTipText(part.getLocalizedTooltip());
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

		Object parent = parentElement;
		if (!(parent instanceof MPartStack)
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
		ctf.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// prevent recursions
				if (ignoreTabSelChanges)
					return;

				MUIElement ele = (MUIElement) e.item.getData(OWNING_ME);
				ele.getParent().setSelectedElement(ele);
				activateStack(stack);
			}
		});

		MouseListener mouseListener = new MouseAdapter() {
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

		// Detect activation...picks up cases where the user clicks on the
		// (already active) tab
		ctf.addListener(SWT.Activate, new org.eclipse.swt.widgets.Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				CTabFolder ctf = (CTabFolder) event.widget;
				MElementContainer<MUIElement> stack = (MElementContainer<MUIElement>) ctf
						.getData(OWNING_ME);
				activateStack(stack);
			}
		});

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
				ctxt.get(EPartService.class), stack,
				(ISWTResourceUtilities) ctxt.get(IResourceUtilities.class),
				getInitialMRUValue(ctf));
		editorList.setInput();

		Point size = editorList.computeSizeHint();
		editorList.setSize(size.x, size.y);

		editorList.setLocation(ctf.toDisplay(getChevronLocation(ctf)));
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
		ctf.setSelection(cti);
		ignoreTabSelChanges = false;

		// Clear out the current Top Right info
		MPart part = getPart(element);
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
		}

		MElementContainer<MUIElement> parent = getParent(part);
		if (parent != null) {
			int count = 0;
			for (MUIElement element : parent.getChildren()) {
				if (element.isToBeRendered()) {
					count++;
					if (count == 2) {
						MenuItem menuItemOthers = new MenuItem(menu, SWT.NONE);
						menuItemOthers
								.setText(SWTRenderersMessages.menuCloseOthers);
						menuItemOthers
								.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent e) {
										MPart part = (MPart) menu
												.getData(STACK_SELECTED_PART);
										closeSiblingParts(part, true);
									}
								});

						MenuItem menuItemAll = new MenuItem(menu, SWT.NONE);
						menuItemAll.setText(SWTRenderersMessages.menuCloseAll);
						menuItemAll
								.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent e) {
										MPart part = (MPart) menu
												.getData(STACK_SELECTED_PART);
										closeSiblingParts(part, false);
									}
								});
						break;
					}
				}
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

	private void closeSiblingParts(MPart part, boolean skipThisPart) {
		MElementContainer<MUIElement> container = getParent(part);
		if (container == null)
			return;

		List<MUIElement> children = container.getChildren();
		List<MPart> others = new LinkedList<MPart>();
		for (MUIElement child : children) {
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
				others.add(otherPart);
		}

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
}
