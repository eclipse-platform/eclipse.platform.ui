/*******************************************************************************
 * Copyright (c) 2010, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 395825
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 486876
 *     Christoph Läubrich - make the application service to work in dialog context
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

public class ApplicationPartServiceImpl implements EPartService {

	private static final Supplier<RuntimeException> NO_VALID_PARTSERVICE = () -> new IllegalStateException(
			"No valid PartService can be acquired from the current context"); //$NON-NLS-1$

	private MApplication application;

	private EModelService modelService;

	@Inject
	ApplicationPartServiceImpl(MApplication application, EModelService modelService) {
		this.application = application;
		this.modelService = modelService;
	}

	private Optional<EPartService> getActiveWindowService() {
		IEclipseContext activeWindowContext = application.getContext().getActiveChild();
		if (activeWindowContext == null) {
			// in this case the application has no focus so we can't determine the active
			// child.
			return Optional.empty();
		}
		EPartService activeWindowPartService = activeWindowContext.get(EPartService.class);
		if (activeWindowPartService == null) {
			throw new IllegalStateException("Active window context is invalid"); //$NON-NLS-1$
		}
		if (activeWindowPartService == this) {
			// in this case we would run into an infinite recursion, so from the current
			// active window we can't acquire another part service
			return Optional.empty();
		}
		return Optional.of(activeWindowPartService);
	}

	private Optional<EPartService> getActiveWindowService(MPart part) {
		return getActiveWindowService().or(() -> {
			IEclipseContext context = part.getContext();
			if (context != null) {
				// First try the context of the part
				EPartService partService = context.get(EPartService.class);
				if (partService instanceof PartServiceImpl) {
					return Optional.of(partService);
				}
				// Otherwise use the context of the contained window
				MWindow window = modelService.getTopLevelWindowFor(part);
				if (window != null) {
					IEclipseContext windowContext = window.getContext();
					EPartService windowPartService = windowContext.get(EPartService.class);
					if (windowPartService instanceof PartServiceImpl) {
						return Optional.of(windowPartService);
					}
				}
			}
			return Optional.empty();
		});
	}

	@Override
	public void addPartListener(IPartListener listener) {
		throw new UnsupportedOperationException(
				"Listeners should only be attached/removed from a window's part service"); //$NON-NLS-1$
	}

	@Override
	public void removePartListener(IPartListener listener) {
		throw new UnsupportedOperationException(
				"Listeners should only be attached/removed from a window's part service"); //$NON-NLS-1$
	}

	@Override
	public boolean isPartOrPlaceholderInPerspective(String elementId, MPerspective perspective) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).isPartOrPlaceholderInPerspective(elementId,
				perspective);
	}

	@Override
	public void switchPerspective(MPerspective perspective) {
		getActiveWindowService().ifPresentOrElse(service -> service.switchPerspective(perspective),
				() -> switchPerspectiveInternal(perspective));
	}

	@Override
	public Optional<MPerspective> switchPerspective(String perspectiveId) {
		Objects.requireNonNull(perspectiveId);
		Optional<EPartService> windowService = getActiveWindowService();
		if (windowService.isPresent()) {
			return windowService.get().switchPerspective(perspectiveId);
		}
		List<MPerspective> result = modelService.findElements(application, perspectiveId, MPerspective.class, null);
		if (!result.isEmpty()) {
			MPerspective perspective = result.get(0);
			switchPerspectiveInternal(perspective);
			return java.util.Optional.of(perspective);
		}
		return Optional.empty();
	}

	private void switchPerspectiveInternal(MPerspective perspective) {
		perspective.getParent().setSelectedElement(perspective);
		UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_SWITCHED, perspective);
	}

	@Override
	public void activate(MPart part) {
		getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).activate(part);
	}

	@Override
	public void activate(MPart part, boolean requiresFocus) {
		getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).activate(part, requiresFocus);
	}

	@Override
	public void requestActivation() {
		getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).requestActivation();
	}

	@Override
	public void bringToTop(MPart part) {
		getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).bringToTop(part);
	}

	@Override
	public MPart findPart(String id) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).findPart(id);
	}

	@Override
	public Collection<MPart> getParts() {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).getParts();
	}

	@Override
	public MPart getActivePart() {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).getActivePart();
	}

	@Override
	public boolean isPartVisible(MPart part) {
		return getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).isPartVisible(part);
	}

	@Override
	public MPart createPart(String id) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).createPart(id);
	}

	@Override
	public MPlaceholder createSharedPart(String id) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).createSharedPart(id);
	}

	@Override
	public MPlaceholder createSharedPart(String id, boolean force) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).createSharedPart(id, force);
	}

	@Override
	public MPart showPart(String id, PartState partState) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).showPart(id, partState);
	}

	@Override
	public MPart showPart(MPart part, PartState partState) {
		return getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).showPart(part, partState);
	}

	@Override
	public void hidePart(MPart part) {
		getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).hidePart(part);
	}

	@Override
	public void hidePart(MPart part, boolean force) {
		getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).hidePart(part, force);
	}

	@Override
	public Collection<MPart> getDirtyParts() {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).getDirtyParts();
	}

	@Override
	public boolean savePart(MPart part, boolean confirm) {
		return getActiveWindowService(part).orElseThrow(NO_VALID_PARTSERVICE).savePart(part, confirm);
	}

	@Override
	public boolean saveAll(boolean confirm) {
		return getActiveWindowService().orElseThrow(NO_VALID_PARTSERVICE).saveAll(confirm);
	}

}
