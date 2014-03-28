/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
@SuppressWarnings("javadoc")
public abstract class ContributionManagerRenderer<Model extends MUIElement, ModelEl extends MUIElement, Manager, ContrRec>
extends SWTPartRenderer {
	private final String MANAGER_FOR_MODEL = getClass().getName() + ".managerForModel"; //$NON-NLS-1$

	private final String CONTRIBUTION_FOR_MODEL = getClass().getName() + ".contributionForModel"; //$NON-NLS-1$

	private final String CONTRIBUTION_RECORD_FOR_MODEL = getClass().getName()
			+ ".contrRecForModel"; //$NON-NLS-1$

	private final String CONTIBUTION_RECORD_LIST_FOR_MODEL = getClass()
			.getName() + ".contrRecListForModel"; //$NON-NLS-1$

	private Map<Manager, WeakReference<Model>> managerToModel = new WeakHashMap<Manager, WeakReference<Model>>();

	private Map<IContributionItem, WeakReference<ModelEl>> contributionToModel = new WeakHashMap<IContributionItem, WeakReference<ModelEl>>();

	@Inject
	protected Logger logger;

	@Inject
	protected IEventBroker eventBroker;

	@Inject
	protected EModelService modelService;

	private EventHandler closedWindowHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			MUIElement changedElement = (MUIElement) event
					.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MWindow)) {
				return;
			}

			MWindow window = (MWindow) changedElement;
			Shell shell = (Shell) window.getWidget();

			if (shell == null || shell.isDisposed()) {
				logCacheState("Before clean up"); //$NON-NLS-1$
				cleanUpCaches(window);
				logCacheState("After clean up"); //$NON-NLS-1$
			}
		}

		private void cleanUpCaches(MWindow window) {
			for (Iterator<WeakReference<Model>> iter = managerToModel.values()
					.iterator(); iter.hasNext();) {
				Model model = iter.next().get();
				if (model != null) {
					MWindow parentWindow = modelService
							.getTopLevelWindowFor(model
						.getParent());
					if (window == parentWindow) {
						cleanUp(model);
					}
				}
			}
		}

		private void logCacheState(String phaseName) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"{0}: managerToModel = {1}", new Object[] { phaseName, managerToModel.size() }); //$NON-NLS-1$
				logger.debug(
						"{0}: contributionToModel = {1}", new Object[] { phaseName, contributionToModel.size() }); //$NON-NLS-1$
			}
		}
	};

	protected void init() {
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET,
				closedWindowHandler);
	}

	protected void contextDisposed() {
		eventBroker.unsubscribe(closedWindowHandler);
	}

	protected abstract void cleanUp(Model model);

	public Model getModel(Manager manager) {
		WeakReference<Model> model = managerToModel.get(manager);
		return model != null ? model.get() : null;
	}

	public ModelEl getModelElement(IContributionItem item) {
		WeakReference<ModelEl> modelEl = contributionToModel.get(item);
		return modelEl != null ? modelEl.get() : null;
	}

	public void linkModelToManager(Model model, Manager manager) {
		model.getTransientData().put(MANAGER_FOR_MODEL, manager);
		managerToModel.put(manager, new WeakReference<Model>(model));
	}

	public void clearModelToManager(Model model, Manager manager) {
		model.getTransientData().remove(MANAGER_FOR_MODEL);
		managerToModel.remove(manager);
	}

	@SuppressWarnings("unchecked")
	public Manager getManager(Model model) {
		return (Manager) model.getTransientData().get(MANAGER_FOR_MODEL);
	}

	public void linkModelToContribution(ModelEl modelEl, IContributionItem item) {
		modelEl.getTransientData().put(CONTRIBUTION_FOR_MODEL, item);
		contributionToModel.put(item, new WeakReference<ModelEl>(modelEl));
	}

	public IContributionItem getContribution(ModelEl modelEl) {
		return (IContributionItem) modelEl.getTransientData().get(
				CONTRIBUTION_FOR_MODEL);
	}

	public void clearModelToContribution(ModelEl modelEl, IContributionItem item) {
		modelEl.getTransientData().remove(CONTRIBUTION_FOR_MODEL);
		contributionToModel.remove(item);
	}

	public void linkElementToContributionRecord(ModelEl modelEl, ContrRec record) {
		modelEl.getTransientData().put(CONTRIBUTION_RECORD_FOR_MODEL, record);
	}

	@SuppressWarnings("unchecked")
	public ContrRec getContributionRecord(MUIElement element) {
		return (ContrRec) element.getTransientData().get(
				CONTRIBUTION_RECORD_FOR_MODEL);
	}

	protected void removeContributionRecord(MUIElement element) {
		element.getTransientData().remove(CONTRIBUTION_RECORD_FOR_MODEL);
	}

	/**
	 * Search the records for testing. Look, but don't touch!
	 *
	 * @return the array of active ContributionRecords.
	 */
	@SuppressWarnings("unchecked")
	public ContrRec[] getContributionRecords(ModelEl... modelEls) {
		HashSet<ContrRec> records = new HashSet<ContrRec>();
		for (ModelEl modelEl: modelEls) {
			records.add(getContributionRecord(modelEl));
		}
		return (ContrRec[]) records.toArray();
	}

	@SuppressWarnings("unchecked")
	public List<ContrRec> getList(MUIElement modelEl) {
		List<ContrRec> tmp = (List<ContrRec>) modelEl.getTransientData().get(
				CONTIBUTION_RECORD_LIST_FOR_MODEL);
		if (tmp == null) {
			tmp = new ArrayList<ContrRec>();
		}
		return tmp;
	}

	@SuppressWarnings("unchecked")
	public void addRecord(ModelEl modelEl, ContrRec rec) {
		List<ContrRec> tmp = (List<ContrRec>) modelEl.getTransientData().get(
				CONTIBUTION_RECORD_LIST_FOR_MODEL);
		if (tmp == null) {
			tmp = new ArrayList<ContrRec>();
			modelEl.getTransientData().put(CONTIBUTION_RECORD_LIST_FOR_MODEL,
					tmp);
		}
		tmp.add(rec);
	}

	@SuppressWarnings("unchecked")
	public void removeRecord(ModelEl modelEl, ContrRec rec) {
		List<ContrRec> tmp = (List<ContrRec>) modelEl.getTransientData().get(
				CONTIBUTION_RECORD_LIST_FOR_MODEL);
		if (tmp != null) {
			tmp.remove(rec);
			if (tmp.isEmpty()) {
				modelEl.getTransientData().remove(
						CONTIBUTION_RECORD_LIST_FOR_MODEL);
			}
		}
	}

}
