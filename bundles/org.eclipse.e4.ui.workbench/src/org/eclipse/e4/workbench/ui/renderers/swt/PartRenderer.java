/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.Handler;
import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;

public class PartRenderer {	
	private final List partFactories = new ArrayList();
	
	// SWT property ids containing the currently rendered part (and factory) for a given widget
	public static final String FACTORY = "partFactory"; //$NON-NLS-1$

	private final IContributionFactory contributionFactory;
	private final IEclipseContext context;
	
	public PartRenderer(IContributionFactory contributionFactory,
			IEclipseContext context) {
		this.contributionFactory = contributionFactory;
		this.context = context;
	}
	
	public void addPartFactory(PartFactory factory) {
		partFactories.add(factory);
	}
	
	public Object createGui(Part element) {
		if (!element.isVisible())
			return null;
		
		// Create a control appropriate to the part
		Object newWidget = createWidget(element);
			
		// Remember that we've created the control
		if (newWidget != null) {
			// Bind the widget to its model element
			element.setWidget(newWidget);
			
			// Process its internal structure through the factory that created it
			PartFactory factory = getFactoryFor(element);
			factory.bindWidget(element, newWidget);
			hookControllerLogic(element);
			factory.processContents(element);
			factory.postProcess(element);
			
			// Now that we have a widget let the parent know
			if (element.getParent() instanceof Part) {
				Part parentElement = (Part) element.getParent();
				PartFactory parentFactory = getFactoryFor(parentElement);
				parentFactory.childAdded(parentElement, element);
			}
		}
		
		return newWidget;
	}

	protected Object createWidget(Part element) {
		// Iterate through the factories until one actually creates the widget
		for (Iterator iterator = partFactories.iterator(); iterator.hasNext();) {
			PartFactory factory = (PartFactory) iterator.next();

			// *** Put any declarative tests here to prevent aggressive loading 
			// For example, test whether this factory handles a particular model type ('StackModel'...) 
			
			Object newWidget = factory.createWidget(element);
			if (newWidget != null) {
				// Remember which factory created the widget
				setFactoryFor(element, factory);

				processHandlers(element);
				if (element.getMenu() != null) {
					factory.createMenu(newWidget, element.getMenu());
				}
				if (element.getToolBar() != null) {
					factory.createToolBar(newWidget, element.getToolBar());
				}
				return newWidget;
			}
		}
		
		return null;
	}

	protected void processHandlers(Part<?> element) {
		for (Handler contributedHandler : element.getHandlers()) {
			contributedHandler.setObject(contributionFactory.create(
					contributedHandler.getURI(), context));
		}
	}
	
	/**
	 * Manages the relationship between a Part<?> and its rendered Widget.
	 * 
	 *    Part<?>.getWidget().getData(OWNING_ME) == Part<?>
	 *    
	 * @param element The UI element
	 * @param widget The widget
	 */
//	public void bindWidget(Part<?> me, Widget widget) {
//		me.setWidget(widget);
//		widget.setData(OWNING_ME, me);
//		
//		hookControllerLogic(me, widget);
//	}

	private void hookControllerLogic(final Part<?> element) {
		// Delegate widget specific hook-up to the creating factory
		PartFactory factory = (PartFactory) getFactoryFor(element);
		if (factory != null)
			factory.hookControllerLogic(element);
        
		// Handle 'adds'
        ((EObject)element).eAdapters().add(new AdapterImpl() {
        	@Override
        	public void notifyChanged(Notification msg) {
        		if (ApplicationPackage.Literals.PART__CHILDREN.equals(msg.getFeature())
        			 && msg.getEventType() == Notification.ADD) {
            			Part parent = (Part) msg.getNotifier();
            			PartFactory parentFactory = getFactoryFor(parent);
            			if (parentFactory == null)
            				return;
            			
        				Part added = (Part) msg.getNewValue();
        				parentFactory.childAdded(parent, added);
        		}
        	}
        });
		
		// Handle 'removes'
        ((EObject)element).eAdapters().add(new AdapterImpl() {
        	@Override
        	public void notifyChanged(Notification msg) {
        		if (ApplicationPackage.Literals.PART__CHILDREN.equals(msg.getFeature())
        			 && msg.getEventType() == Notification.REMOVE) {
            			Part<?> parent = (Part<?>) msg.getNotifier();
            			PartFactory parentFactory = getFactoryFor(parent);
            			if (parentFactory == null)
            				return;
            			
        				Part<?> removed = (Part<?>) msg.getOldValue();
        				parentFactory.childRemoved(parent, removed);
        		}
        	}
        });
	}

	protected void setFactoryFor(Part element, PartFactory factory) {
		element.setOwner(factory);
	}
	
	protected PartFactory getFactoryFor(Part element) {
		return (PartFactory) element.getOwner();
	}
}
