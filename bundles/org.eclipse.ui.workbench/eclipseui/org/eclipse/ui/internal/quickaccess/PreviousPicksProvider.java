/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Hochstein (Freescale) - Bug 393703 - NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 ******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.quickaccess.QuickAccessElement;

class PreviousPicksProvider extends QuickAccessProvider {

	LinkedList<QuickAccessElement> elements;
	private int maxNumberOfElements;
	private Supplier<List<QuickAccessElement>> initializer;
	private Collection<QuickAccessProvider> initialProviders;

	PreviousPicksProvider(int maxNumberOfElements) {
		this.maxNumberOfElements = maxNumberOfElements;
	}

	public void setElementsInitializer(Supplier<List<QuickAccessElement>> initializer) {
		this.initializer = initializer;
	}

	public void setInvolvedProviders(Collection<QuickAccessProvider> providers) {
		this.initialProviders = providers;
	}

	@Override
	public QuickAccessElement findElement(String id, String filterText) {
		return null;
	}

	@Override
	public QuickAccessElement[] getElements() {
		synchronized (this) {
			if (elements == null) {
				elements = new LinkedList<>();
				if (initializer != null) {
					elements.addAll(initializer.get());
				}
			}
		}
		// If the list is being restored, it may contain null elements
		return elements.stream().filter(Objects::nonNull).toArray(QuickAccessElement[]::new);
	}

	@Override
	public QuickAccessElement[] getElementsSorted(String filter, IProgressMonitor monitor) {
		return getElements();
	}

	@Override
	public String getId() {
		return "org.eclipse.ui.previousPicks"; //$NON-NLS-1$
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Previous;
	}

	@Override
	protected void doReset() {
		// operation not applicable for this provider
	}

	public void addPreviousPick(QuickAccessElement element, Consumer<QuickAccessElement> onRemoveElement) {
		elements.remove(element);
		if (elements.size() == maxNumberOfElements) {
			QuickAccessElement removedElement = elements.removeLast();
			if (onRemoveElement != null) {
				onRemoveElement.accept(removedElement);
			}
		}
		elements.addFirst(element);
	}

	public void removeElement(QuickAccessElement removedElement) {
		if (this.elements != null) {
			this.elements.remove(removedElement);
		}
	}

	@Override
	public boolean requiresUiAccess() {
		if (this.initialProviders == null) {
			return false;
		}
		return this.initialProviders.stream().anyMatch(QuickAccessProvider::requiresUiAccess);
	}
}