package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.e4.ui.model.workbench.Perspective;
import org.eclipse.e4.ui.model.workbench.WorkbenchWindow;
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

	public Object createWidget(Part<?> part) {
		final Widget newWidget;
	
		if (part instanceof WorkbenchWindow) {
			Shell wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
			TrimmedLayout tl = new TrimmedLayout(wbwShell);
			wbwShell.setLayout(tl);
			if (((WorkbenchWindow) part).getName() != null)
				wbwShell.setText(((WorkbenchWindow) part).getName());
	
			newWidget = wbwShell;
			bindWidget(part, newWidget);
			final IHandlerService hs = new PartHandlerService(part);
			IEclipseContext localContext = EclipseContextFactory.create("WorkbenchWindow", context, UIContextScheduler.instance);
			localContext.set(IHandlerService.class.getName(), hs);
			wbwShell.setData("LOCATOR", localContext);
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
