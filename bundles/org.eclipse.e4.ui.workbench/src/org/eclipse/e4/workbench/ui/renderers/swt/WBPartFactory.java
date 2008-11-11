package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.services.IServiceLocator;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.HandledItem;
import org.eclipse.e4.ui.model.application.Menu;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.ui.model.workbench.Perspective;
import org.eclipse.e4.ui.model.workbench.WorkbenchWindow;
import org.eclipse.e4.ui.services.ISelectionService;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class WBPartFactory extends PartFactory {

	public WBPartFactory() {
		super();
	}

	public Object createWidget(Part<?> part) {
		final Widget parentWidget = getParentWidget(part);

		final Widget newWidget;
		if (part instanceof WorkbenchWindow) {
			Shell wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
			TrimmedLayout tl = new TrimmedLayout(wbwShell);
			wbwShell.setLayout(tl);
			if (((WorkbenchWindow) part).getName() != null)
				wbwShell.setText(((WorkbenchWindow) part).getName());

			newWidget = wbwShell;
		} else if (part instanceof HandledItem) {
			org.eclipse.swt.widgets.Menu parentMenu = null;
			final HandledItem item = (HandledItem) part;
			if (parentWidget instanceof ToolBar) {
				ToolItem toolItem = new ToolItem((ToolBar) parentWidget,
						SWT.PUSH);
				toolItem.setText(item.getName());
				toolItem.setImage(getImage(part));
				newWidget = toolItem;
			} else {
				if (parentWidget instanceof org.eclipse.swt.widgets.Menu)
					parentMenu = (org.eclipse.swt.widgets.Menu) parentWidget;
				else if (parentWidget instanceof MenuItem)
					parentMenu = ((MenuItem) parentWidget).getMenu();
				else if (parentWidget instanceof Decorations) {
					parentMenu = ((Decorations) parentWidget).getMenuBar();
					if (parentMenu == null) {
						parentMenu = new org.eclipse.swt.widgets.Menu(
								(Decorations) parentWidget, SWT.BAR);
					}
				} else if (parentWidget instanceof Control) {
					parentMenu = ((Control) parentWidget).getMenu();
					if (parentMenu == null) {
						parentMenu = new org.eclipse.swt.widgets.Menu(
								(Control) parentWidget);
						((Control) parentWidget).setMenu(parentMenu);
					}
				}

				newWidget = createMenuItem(parentMenu, item);
			}

			if (item.getHandler() != null) {
				final ISelectionService selectionService = (ISelectionService) serviceLocator
						.getService(ISelectionService.class);
				newWidget.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						Object result = canExecuteItem(serviceLocator, item,
								selectionService);
						if (Boolean.TRUE.equals(result)) {
							executeItem(serviceLocator, item, selectionService);
						}
					}
				});
				if (parentMenu != null) {
					parentMenu.addListener(SWT.Show, new Listener() {

						public void handleEvent(Event event) {
							if (newWidget.isDisposed()) {
								return;
							}
							Object result = canExecuteItem(serviceLocator,
									item, selectionService);

							((MenuItem) newWidget).setEnabled(Boolean.TRUE
									.equals(result));
						}
					});
				} else if (newWidget instanceof ToolItem) {
					newWidget.getDisplay().timerExec(250, new Runnable() {
						public void run() {
							if (newWidget.isDisposed()) {
								return;
							}
							Object result = canExecuteItem(serviceLocator,
									item, selectionService);
							((ToolItem) newWidget).setEnabled(Boolean.TRUE
									.equals(result));
							newWidget.getDisplay().timerExec(250, this);
						}
					});
				}
			}
		} else if (part instanceof org.eclipse.e4.ui.model.application.ToolBar) {
			newWidget = new ToolBar((Composite) parentWidget, SWT.FLAT
					| SWT.NO_FOCUS);
		} else {
			newWidget = null;
		}

		return newWidget;
	}

	@Override
	public <P extends Part<?>> void processContents(Part<P> me) {
		if (me instanceof WorkbenchWindow) {
			WorkbenchWindow wbwModel = (WorkbenchWindow) me;
			Shell wbwShell = (Shell) wbwModel.getWidget();
			TrimmedLayout tl = (TrimmedLayout) wbwShell.getLayout();

			// Set the main menu bar
			Menu mainMenu = wbwModel.getMenu();
			if (mainMenu != null) {
				org.eclipse.swt.widgets.Menu bar = new org.eclipse.swt.widgets.Menu(
						wbwShell, SWT.BAR);
				mainMenu.setOwner(this);
				// TODO menus
//				bindWidget(mainMenu, bar);
//				super.processContents(mainMenu);
				wbwShell.setMenuBar(bar);
			}

			// Trim
			Part<?> topTrim = wbwModel.getTrim().getTopTrim();
			if (topTrim != null) {
				bindWidget(topTrim, tl.top);
				topTrim.setOwner(this);
				super.processContents(topTrim);
			}
			Part<?> bottomTrim = wbwModel.getTrim().getBottomTrim();
			if (bottomTrim != null) {
				bindWidget(bottomTrim, tl.bottom);
				bottomTrim.setOwner(this);
				super.processContents(bottomTrim);
			}
			Part<?> leftTrim = wbwModel.getTrim().getLeftTrim();
			if (leftTrim != null) {
				bindWidget(leftTrim, tl.left);
				leftTrim.setOwner(this);
				super.processContents(leftTrim);
			}
			Part<?> rightTrim = wbwModel.getTrim().getRightTrim();
			if (rightTrim != null) {
				bindWidget(rightTrim, tl.right);
				rightTrim.setOwner(this);
				super.processContents(rightTrim);
			}

			// Client Area
			Perspective<?> persp = wbwModel.getChildren().get(0);
			bindWidget(persp, tl.center);
			persp.setOwner(this);
			super.processContents(persp);
		}

		// TODO Auto-generated method stub
		super.processContents(me);
	}

	protected Object canExecuteItem(final IServiceLocator serviceLocator,
			final HandledItem item, final ISelectionService selectionService) {
		Object result = contributionFactory.call(item.getHandler().getObject(),
				item.getHandler().getURI(), "canExecute",
				new IServiceLocator() {
					public Object getService(Class<?> api) {
						Object result = selectionService.getSelection(api);
						if (result != null) {
							return result;
						}
						return serviceLocator.getService(api);
					}

					public boolean hasService(Class<?> api) {
						Object result = selectionService.getSelection(api);
						if (result != null) {
							return true;
						}
						return serviceLocator.hasService(api);
					}

				}, Boolean.TRUE);
		return result;
	}

	protected void executeItem(final IServiceLocator serviceLocator,
			final HandledItem item, final ISelectionService selectionService) {
		contributionFactory.call(item.getHandler().getObject(), item
				.getHandler().getURI(), "execute", new IServiceLocator() {
			public Object getService(Class<?> api) {
				Object result = selectionService.getSelection(api);
				if (result != null) {
					return result;
				}
				return serviceLocator.getService(api);
			}

			public boolean hasService(Class<?> api) {
				Object result = selectionService.getSelection(api);
				if (result != null) {
					return true;
				}
				return serviceLocator.hasService(api);
			}

		}, null);
	}

	private Widget createMenuItem(org.eclipse.swt.widgets.Menu parentMenu, HandledItem me) {
		int style = SWT.PUSH;
		if (me instanceof org.eclipse.e4.ui.model.application.MenuItem) {
			if (((org.eclipse.e4.ui.model.application.MenuItem) me).isSeparator()) {
				style = SWT.SEPARATOR;
			} else if (((org.eclipse.e4.ui.model.application.MenuItem) me).getMenu() != null) {
				style = SWT.CASCADE;
			}
		}

		MenuItem newMenuItem = new MenuItem(parentMenu, style);
		if (style != SWT.SEPARATOR) {
			newMenuItem.setText(me.getName());
			newMenuItem.setImage(getImage(me));
			newMenuItem.setEnabled(true);
		}

		if (style == SWT.CASCADE) {
			org.eclipse.swt.widgets.Menu subMenu = new org.eclipse.swt.widgets.Menu(parentMenu.getShell(), SWT.DROP_DOWN);
			newMenuItem.setMenu(subMenu);
		}
		return newMenuItem;
	}

	@Override
	public void hookControllerLogic(Part<?> me) {
		super.hookControllerLogic(me);

		Widget widget = (Widget) me.getWidget();

		// Set up the text binding...perhaps should catch exceptions?
		IObservableValue emfTextObs = EMFObservables.observeValue((EObject) me,
				ApplicationPackage.Literals.ITEM__NAME);
		if (widget instanceof Control && !(widget instanceof Composite)) {
			ISWTObservableValue uiTextObs = SWTObservables
					.observeText((Control) widget);
			dbc.bindValue(uiTextObs, emfTextObs, null, null);
		} else if (widget instanceof org.eclipse.swt.widgets.Item) {
			ISWTObservableValue uiTextObs = SWTObservables
					.observeText((org.eclipse.swt.widgets.Item) widget);
			dbc.bindValue(uiTextObs, emfTextObs, null, null);
		}

		// Set up the tool tip binding...perhaps should catch exceptions?
		IObservableValue emfTTipObs = EMFObservables.observeValue((EObject) me,
				ApplicationPackage.Literals.ITEM__TOOLTIP);
		if (widget instanceof Control) {
			ISWTObservableValue uiTTipObs = SWTObservables
					.observeTooltipText((Control) widget);
			dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		} else if (widget instanceof org.eclipse.swt.widgets.Item && !(widget instanceof MenuItem)) {
			ISWTObservableValue uiTTipObs = SWTObservables
					.observeTooltipText((org.eclipse.swt.widgets.Item) widget);
			dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		}

		// Handle generic image changes
		((EObject) me).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				Part<?> sm = (Part<?>) msg.getNotifier();
				if (ApplicationPackage.Literals.ITEM__ICON_URI.equals(msg
						.getFeature())) {
					Widget widget = (Widget) sm.getWidget();
					if (widget instanceof org.eclipse.swt.widgets.Item) {
						org.eclipse.swt.widgets.Item item = (org.eclipse.swt.widgets.Item) widget;
						Image image = getImage(sm);
						if (image != null)
							item.setImage(image);
					}
				}
			}
		});
	}
}
