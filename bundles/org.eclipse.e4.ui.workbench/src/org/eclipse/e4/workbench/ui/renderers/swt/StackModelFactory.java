package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.services.context.spi.IContextConstants;

import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MStack;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.ui.internal.UIContextScheduler;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

public class StackModelFactory extends SWTPartFactory {

	public StackModelFactory() {
		super();
	}

	public Object createWidget(MPart<?> part) {
		Widget newWidget = null;

		if (!(part instanceof MStack))
			return null;

		Widget parentWidget = getParentWidget(part);
		if (parentWidget instanceof Composite) {
			IEclipseContext parentContext = getContextForParent(part);
			final CTabFolder ctf = new CTabFolder((Composite) parentWidget,
					SWT.BORDER);
			bindWidget(part, ctf);
			ctf.setVisible(true);
			ctf.setSimple(false);
			ctf.setTabHeight(20);
			newWidget = ctf;
			final IEclipseContext folderContext = EclipseContextFactory.create(parentContext,
					UIContextScheduler.instance);
			folderContext.set(IContextConstants.DEBUG_STRING, "TabFolder"); //$NON-NLS-1$
			part.setContext(folderContext);
			final IEclipseContext toplevelContext = getToplevelContext(part);
			final IStylingEngine engine = (IStylingEngine) folderContext.get(IStylingEngine.class.getName());
			folderContext.runAndTrack(new Runnable() {
				public void run() {
					IEclipseContext currentActive = toplevelContext;
					IEclipseContext child;
					while (currentActive != folderContext
							&& (child = (IEclipseContext) currentActive
									.get("activeChild")) != null && child != currentActive) { //$NON-NLS-1$
						currentActive = child;
					}
					// System.out.println(cti.getText() + " is now " + ((currentActive == tabItemContext) ? "active" : "inactive"));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					
					if (currentActive == folderContext) {
						engine.setClassname(ctf, "active"); //$NON-NLS-1$
					} else {
						engine.setClassname(ctf, "inactive"); //$NON-NLS-1$
					}
				}
			}, ""); //$NON-NLS-1$
		}

		return newWidget;
	}

	public void postProcess(MPart<?> part) {
		if (!(part instanceof MStack))
			return;

		CTabFolder ctf = (CTabFolder) part.getWidget();
		CTabItem[] items = ctf.getItems();
		MPart<?> selPart = ((MStack) part).getActiveChild();

		// If there's none defined then pick the first
		if (selPart == null && part.getChildren().size() > 0) {
			((MStack) part).setActiveChild((MItemPart<?>) part.getChildren().get(
					0));
			// selPart = (MPart) part.getChildren().get(0);
		} else {
			for (int i = 0; i < items.length; i++) {
				MPart<?> me = (MPart<?>) items[i].getData(OWNING_ME);
				if (selPart == me)
					ctf.setSelection(items[i]);
			}
		}
	}

	@Override
	public void childAdded(MPart<?> parentElement, MPart<?> element) {
		super.childAdded(parentElement, element);

		if (element instanceof MItemPart<?>) {
			MItemPart<?> itemPart = (MItemPart<?>) element;
			CTabFolder ctf = (CTabFolder) parentElement.getWidget();
			int createFlags = 0;

			// if(element instanceof View && ((View)element).isCloseable())
			//createFlags = createFlags | SWT.CLOSE;

			CTabItem cti = findItemForPart(parentElement, element);
			if (cti == null)
				cti = new CTabItem(ctf, createFlags);

			cti.setData(OWNING_ME, element);
			cti.setText(itemPart.getName());
			cti.setImage(getImage(element));

			// Lazy Loading: On the first pass through this method the
			// part's control will be null (we're just creating the tabs
			Control ctrl = (Control) element.getWidget();
			if (ctrl != null) {
				cti.setControl(ctrl);

				// Hook up special logic to synch up the Tab Items
				hookChildControllerLogic(parentElement, element, cti);
			}
		}
	}

	@Override
	public <P extends MPart<?>> void processContents(MPart<P> me) {
		Widget parentWidget = getParentWidget(me);
		if (parentWidget == null)
			return;

		// Lazy Loading: here we only create the CTabItems, not the parts
		// themselves; they get rendered when the tab gets selected
		List<P> parts = me.getChildren();
		if (parts != null) {
			for (MPart<?> childME : parts) {
				if (childME.isVisible())
					childAdded(me, childME);
			}
		}
	}

	private CTabItem findItemForPart(MPart<?> folder, MPart<?> part) {
		CTabFolder ctf = (CTabFolder) folder.getWidget();
		if (ctf == null)
			return null;

		CTabItem[] items = ctf.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData(OWNING_ME) == part)
				return items[i];
		}
		return null;
	}

	private void hookChildControllerLogic(final MPart<?> parentElement,
			final MPart<?> childElement, final CTabItem cti) {
		// Handle label changes
		IObservableValue textObs = EMFObservables.observeValue(
				(EObject) childElement,
				ApplicationPackage.Literals.MITEM__NAME);
		ISWTObservableValue uiObs = SWTObservables.observeText(cti);
		dbc.bindValue(uiObs, textObs, null, null);

		IObservableValue emfTTipObs = EMFObservables.observeValue(
				(EObject) childElement,
				ApplicationPackage.Literals.MITEM__TOOLTIP);
		ISWTObservableValue uiTTipObs = SWTObservables.observeTooltipText(cti);
		dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		
		// Handle tab item image changes
		((EObject) childElement).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				MPart<?> sm = (MPart<?>) msg.getNotifier();
				if (ApplicationPackage.Literals.MITEM__ICON_URI.equals(msg
						.getFeature())) {
					CTabItem item = findItemForPart(parentElement, sm);
					if (item != null) {
						Image image = getImage(sm);
						if (image != null)
							item.setImage(image);
					}
				}
			}
		});
	}

	@Override
	public void childRemoved(MPart<?> parentElement, MPart<?> child) {
		super.childRemoved(parentElement, child);

		CTabItem oldItem = findItemForPart(parentElement, child);
		if (oldItem != null) {
			oldItem.setControl(null); // prevent the widget from being disposed
			oldItem.dispose();
		}
	}

	@Override
	public void hookControllerLogic(final MPart<?> me) {
		super.hookControllerLogic(me);

		final MStack sm = (MStack) me;
		// Match the selected TabItem to its Part
		CTabFolder ctf = (CTabFolder) me.getWidget();
		ctf.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				MItemPart<?> newPart = (MItemPart<?>) e.item.getData(OWNING_ME);
				if (sm.getActiveChild() != newPart) {
					activate(newPart);
				}
			}
		});
		
		// Detect activation...picks up cases where the user clicks on the
		// (already active) tab
		ctf.addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				CTabFolder ctf = (CTabFolder) event.widget;
				MStack stack = (MStack) ctf.getData(OWNING_ME);
				MItemPart<?> part = stack.getActiveChild();
				activate(part);
			}
		});

		((EObject) me).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (ApplicationPackage.Literals.MPART__ACTIVE_CHILD.equals(msg
						.getFeature())) {
					MStack sm = (MStack) msg.getNotifier();
					MPart<?> selPart = sm.getActiveChild();
					CTabFolder ctf = (CTabFolder) ((MStack) msg.getNotifier())
							.getWidget();
					CTabItem item = findItemForPart(sm, selPart);
					if (item != null) {
						// Lazy Loading: we create the control here if necessary
						// Note that this will result in a second call to
						// 'childAdded' but
						// that logic expects this
						Control ctrl = item.getControl();
						if (ctrl == null) {
							renderer.createGui(selPart);
						}

						ctf.setSelection(item);
					}
				}
			}
		});
	}
}
