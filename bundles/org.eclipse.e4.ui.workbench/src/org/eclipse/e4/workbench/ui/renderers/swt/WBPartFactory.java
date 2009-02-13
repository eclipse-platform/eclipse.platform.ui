package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.e4.core.services.context.spi.IContextConstants;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.workbench.MPerspective;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IHandlerService;
import org.eclipse.e4.workbench.ui.internal.UIContextScheduler;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class WBPartFactory extends SWTPartFactory {

	public Object createWidget(MPart<?> part) {
		final Widget newWidget;
	
		if (part instanceof MWorkbenchWindow) {
			IEclipseContext parentContext = getContextForParent(part);
			Shell wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
			TrimmedLayout tl = new TrimmedLayout(wbwShell);
			wbwShell.setLayout(tl);
			if (((MWorkbenchWindow) part).getName() != null)
				wbwShell.setText(((MWorkbenchWindow) part).getName());
	
			newWidget = wbwShell;
			bindWidget(part, newWidget);
			final IHandlerService hs = new PartHandlerService(part);
			IEclipseContext localContext = EclipseContextFactory.create(parentContext, UIContextScheduler.instance);
			localContext.set(IContextConstants.DEBUG_STRING, "MWorkbenchWindow"); //$NON-NLS-1$
			part.setContext(localContext);
			localContext.set(IHandlerService.class.getName(), hs);
			parentContext.set(IServiceConstants.ACTIVE_CHILD, localContext);
		} else {
			newWidget = null;
		}
	
		return newWidget;
	}

	@Override
	public <P extends MPart<?>> void processContents(MPart<P> me) {
		if (me instanceof MWorkbenchWindow) {
			MWorkbenchWindow wbwModel = (MWorkbenchWindow) me;
			Shell wbwShell = (Shell) wbwModel.getWidget();
			TrimmedLayout tl = (TrimmedLayout) wbwShell.getLayout();
	
			// MTrim
			MPart<?> topTrim = wbwModel.getTrim().getTopTrim();
			if (topTrim != null) {
				bindWidget(topTrim, tl.top);
				topTrim.setOwner(this);
				super.processContents(topTrim);
			}
			MPart<?> bottomTrim = wbwModel.getTrim().getBottomTrim();
			if (bottomTrim != null) {
				bindWidget(bottomTrim, tl.bottom);
				bottomTrim.setOwner(this);
				super.processContents(bottomTrim);
			}
			MPart<?> leftTrim = wbwModel.getTrim().getLeftTrim();
			if (leftTrim != null) {
				bindWidget(leftTrim, tl.left);
				leftTrim.setOwner(this);
				super.processContents(leftTrim);
			}
			MPart<?> rightTrim = wbwModel.getTrim().getRightTrim();
			if (rightTrim != null) {
				bindWidget(rightTrim, tl.right);
				rightTrim.setOwner(this);
				super.processContents(rightTrim);
			}
	
			// Client Area
			MPerspective<?> persp = wbwModel.getChildren().get(0);
			bindWidget(persp, tl.center);
			persp.setOwner(this);
			super.processContents(persp);
		}
	
		// TODO Auto-generated method stub
		super.processContents(me);
	}

	@Override
	public void hookControllerLogic(MPart<?> me) {
		super.hookControllerLogic(me);
	
		Widget widget = (Widget) me.getWidget();
	
		// Set up the text binding...perhaps should catch exceptions?
		IObservableValue emfTextObs = EMFObservables.observeValue((EObject) me,
				ApplicationPackage.Literals.MITEM__NAME);
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
				ApplicationPackage.Literals.MITEM__TOOLTIP);
		if (widget instanceof Control) {
			ISWTObservableValue uiTTipObs = SWTObservables
					.observeTooltipText((Control) widget);
			dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		} else if (widget instanceof org.eclipse.swt.widgets.Item
				&& !(widget instanceof MenuItem)) {
			ISWTObservableValue uiTTipObs = SWTObservables
					.observeTooltipText((org.eclipse.swt.widgets.Item) widget);
			dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		}
	
		// Handle generic image changes
		((EObject) me).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				MPart<?> sm = (MPart<?>) msg.getNotifier();
				if (ApplicationPackage.Literals.MITEM__ICON_URI.equals(msg
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
