/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.Policy;
import org.eclipse.ui.internal.navigator.dnd.NavigatorPluginDropAction;
import org.eclipse.ui.part.PluginTransfer;

/**
 * 
 * Provides an implementation of {@link DragSourceAdapter} which uses the
 * extensions provided by the associated {@link INavigatorContentService}.
 * 
 * <p>
 * Clients should not need to create an instance of this class unless they are
 * creating their own custom viewer. Otherwise, {@link CommonViewer} configures
 * its drag adapter automatically.
 * </p> 
 * 
 * @see INavigatorDnDService
 * @see CommonDragAdapterAssistant
 * @see CommonDropAdapter
 * @see CommonDropAdapterAssistant
 * @see CommonViewer
 * @since 3.2
 * 
 */
public final class CommonDragAdapter extends DragSourceAdapter {

	private final INavigatorContentService contentService;

	private final ISelectionProvider provider;

	private CommonDragAdapterAssistant setDataAssistant;
	
	private List assistantsToUse;
	
	/**
	 * Create a DragAdapter that drives the configuration of the drag data.
	 * 
	 * @param aContentService
	 *            The content service this Drag Adapter is associated with
	 * @param aProvider
	 *            The provider that can give the current selection from the
	 *            appropriate viewer.
	 */
	public CommonDragAdapter(INavigatorContentService aContentService,
			ISelectionProvider aProvider) {
		super();
		contentService = aContentService;
		provider = aProvider;
		assistantsToUse = new ArrayList();
	}

	/**
	 * 
	 * @return An array of supported Drag Transfer types. The list contains [
	 *         {@link LocalSelectionTransfer#getTransfer()},
	 *         {@link PluginTransfer#getInstance()}] in addition to any
	 *         supported types contributed by the
	 *         {@link CommonDragAdapterAssistant assistants}.
	 * @see CommonDragAdapterAssistant
	 * @see LocalSelectionTransfer
	 * @see PluginTransfer
	 */
	public Transfer[] getSupportedDragTransfers() {
		CommonDragAdapterAssistant[] assistants = contentService
				.getDnDService().getCommonDragAssistants();

		Set supportedTypes = new LinkedHashSet();
		supportedTypes.add(PluginTransfer.getInstance());
		supportedTypes.add(LocalSelectionTransfer.getTransfer());
		Transfer[] transferTypes = null;
		for (int i = 0; i < assistants.length; i++) {
			transferTypes = assistants[i].getSupportedTransferTypes();
			for (int j = 0; j < transferTypes.length; j++) {
				if (transferTypes[j] != null) {
					supportedTypes.add(transferTypes[j]);
				}
			}
		}
		
		Transfer[] transfers = (Transfer[]) supportedTypes
				.toArray(new Transfer[supportedTypes.size()]);
		return transfers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(final DragSourceEvent event) {
		if (Policy.DEBUG_DND) {
			System.out.println("CommonDragAdapter.dragStart (begin): " + event); //$NON-NLS-1$
		}
		SafeRunner.run(new NavigatorSafeRunnable() {
			public void run() throws Exception {
				DragSource dragSource = (DragSource) event.widget;
				if (Policy.DEBUG_DND) {
					System.out.println("CommonDragAdapter.dragStart source: " + dragSource); //$NON-NLS-1$
				}
				Control control = dragSource.getControl();
				if (control == control.getDisplay().getFocusControl()) {
					ISelection selection = provider.getSelection();
					assistantsToUse.clear();

					if (!selection.isEmpty()) {
						LocalSelectionTransfer.getTransfer().setSelection(selection);

						boolean doIt = false;
						INavigatorDnDService dndService = contentService.getDnDService();
						CommonDragAdapterAssistant[] assistants = dndService
								.getCommonDragAssistants();
						if (assistants.length == 0)
							doIt = true;
						for (int i = 0; i < assistants.length; i++) {
							if (Policy.DEBUG_DND) {
								System.out
										.println("CommonDragAdapter.dragStart assistant: " + assistants[i]); //$NON-NLS-1$
							}
							event.doit = true;
							assistants[i].dragStart(event, (IStructuredSelection) selection);
							doIt |= event.doit;
							if (event.doit) {
								if (Policy.DEBUG_DND) {
									System.out
											.println("CommonDragAdapter.dragStart assistant - event.doit == true"); //$NON-NLS-1$
								}
								assistantsToUse.add(assistants[i]);
							}
						}

						event.doit = doIt;
					} else {
						event.doit = false;
					}
				} else {
					event.doit = false;
				}
			}
		});

		if (Policy.DEBUG_DND) {
			System.out.println("CommonDragAdapter.dragStart (end): doit=" + event.doit); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(final DragSourceEvent event) {

		final ISelection selection = LocalSelectionTransfer.getTransfer()
				.getSelection();

		if (Policy.DEBUG_DND) {
			System.out
					.println("CommonDragAdapter.dragSetData: event" + event + " selection=" + selection); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (LocalSelectionTransfer.getTransfer()
				.isSupportedType(event.dataType)) {
			event.data = selection;

			if (Policy.DEBUG_DND) {
				System.out
						.println("CommonDragAdapter.dragSetData set LocalSelectionTransfer: " + event.data); //$NON-NLS-1$
			}
		} else if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = NavigatorPluginDropAction
					.createTransferData(contentService);
			if (Policy.DEBUG_DND) {
				System.out
						.println("CommonDragAdapter.dragSetData set PluginTransfer: " + event.data); //$NON-NLS-1$
			}
		} else if (selection instanceof IStructuredSelection) {
			if (Policy.DEBUG_DND) {
				System.out
						.println("CommonDragAdapter.dragSetData looking for assistants"); //$NON-NLS-1$
			}

			for (int i = 0, len = assistantsToUse.size(); i < len; i++) {
				final CommonDragAdapterAssistant assistant = (CommonDragAdapterAssistant) assistantsToUse.get(i); 
				if (Policy.DEBUG_DND) {
					System.out
							.println("CommonDragAdapter.dragSetData assistant: " + assistant); //$NON-NLS-1$
				}

				Transfer[] supportedTransferTypes = assistant
						.getSupportedTransferTypes();
				final boolean[] getOut = new boolean[1];
				for (int j = 0; j < supportedTransferTypes.length; j++) {
					if (supportedTransferTypes[j].isSupportedType(event.dataType)) {
						SafeRunner.run(new NavigatorSafeRunnable() {
							public void run() throws Exception {
								if (Policy.DEBUG_DND) {
									System.out
											.println("CommonDragAdapter.dragSetData supported xfer type"); //$NON-NLS-1$
								}
								if (assistant.setDragData(event, (IStructuredSelection) selection)) {
									if (Policy.DEBUG_DND) {
										System.out
												.println("CommonDragAdapter.dragSetData set data " + event.data); //$NON-NLS-1$
									}
									setDataAssistant = assistant;
									getOut[0] = true;
								}
							}
						});
						if (getOut[0])
							return;
					}
				}
			}

			if (Policy.DEBUG_DND) {
				System.out
						.println("CommonDragAdapter.dragSetData FAILED no assistant handled it"); //$NON-NLS-1$
			}
			event.doit = false;

		} else {
			if (Policy.DEBUG_DND) {
				System.out
						.println("CommonDragAdapter.dragSetData FAILED can't identify transfer type"); //$NON-NLS-1$
			}
			event.doit = false;
		}
	}
	 
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {

		if (Policy.DEBUG_DND) {
			System.out.println("CommonDragAdapter.dragFinished(): " + event); //$NON-NLS-1$
		}

		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();

		if (event.doit && selection instanceof IStructuredSelection && setDataAssistant != null)
			setDataAssistant.dragFinished(event, (IStructuredSelection) selection);
			
		setDataAssistant = null;

		LocalSelectionTransfer.getTransfer().setSelection(null);

		// TODO Handle clean up if drop target was outside of workbench
		// if (event.doit != false) {
		//			
		// }
	}

}
