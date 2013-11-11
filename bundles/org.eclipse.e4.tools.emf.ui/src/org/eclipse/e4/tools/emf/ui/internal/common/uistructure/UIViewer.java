/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.uistructure;

import java.util.Collections;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.tools.emf.ui.common.IScriptingSupport;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ControlHighlighter;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFValueProperty;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class UIViewer {
	public TreeViewer createViewer(Composite parent, EStructuralFeature feature, final IObservableValue master, IResourcePool resourcePool, final Messages messages) {
		final TreeViewer viewer = new TreeViewer(parent);
		viewer.setContentProvider(new WidgetContentProvider());
		viewer.setLabelProvider(new WidgetLabelProvider(resourcePool));
		IEMFValueProperty property = EMFProperties.value(feature);
		IObservableValue value = property.observeDetail(master);
		value.addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (event.diff.getNewValue() != null) {
					viewer.setInput(Collections.singleton(event.diff.getNewValue()));
					viewer.expandToLevel(2);
				} else {
					viewer.setInput(Collections.emptyList());
				}
			}
		});

		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				final Object o = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (o instanceof Control) {
					manager.add(new Action(messages.ModelEditor_ShowControl) {
						@Override
						public void run() {
							ControlHighlighter.show((Control) o);
						}
					});
				}

				IExtensionRegistry registry = RegistryFactory.getRegistry();
				IExtensionPoint extPoint = registry.getExtensionPoint("org.eclipse.e4.tools.emf.ui.scripting"); //$NON-NLS-1$
				final IConfigurationElement[] elements = extPoint.getConfigurationElements();

				final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();

				if (elements.length > 0 && !s.isEmpty()) {
					MenuManager scriptExecute = new MenuManager(messages.ModelEditor_Script);
					manager.add(scriptExecute);
					for (IConfigurationElement e : elements) {
						final IConfigurationElement le = e;
						scriptExecute.add(new Action(e.getAttribute("label")) { //$NON-NLS-1$
							@Override
							public void run() {
								try {
									MApplicationElement o = (MApplicationElement) master.getValue();
									IScriptingSupport support = (IScriptingSupport) le.createExecutableExtension("class"); //$NON-NLS-1$
									support.openEditor(viewer.getControl().getShell(), s.getFirstElement(), ModelUtils.getContainingContext(o));
								} catch (CoreException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					}
				}
			}
		});

		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));

		return viewer;
	}
}
