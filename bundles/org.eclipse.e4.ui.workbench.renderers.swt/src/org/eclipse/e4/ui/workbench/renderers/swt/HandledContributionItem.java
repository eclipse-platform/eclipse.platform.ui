/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joseph Carroll <jdsalingerjr@gmail.com> - Bug 385414 Contributing wizards to toolbar always displays icon and text
 *     Snjezana Peco <snjezana.peco@redhat.com> - Memory leaks in Juno when opening and closing XML Editor - http://bugs.eclipse.org/397909
 *     Marco Descher <marco@descher.at> - Bug 397677
 *     Dmitry Spiridenok - Bug 429756
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445723, 450863, 472654
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 461026
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 473779
******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.help.EHelpService;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

public class HandledContributionItem extends AbstractContributionItem {
	/**
	 * Constant from org.eclipse.ui.handlers.RadioState.PARAMETER_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE_PARAMETER = "org.eclipse.ui.commands.radioStateParameter"; //$NON-NLS-1$

	/**
	 * Constant from org.eclipse.ui.handlers.RadioState.STATE_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE = "org.eclipse.ui.commands.radioState"; //$NON-NLS-1$

	/**
	 * Constant from org.eclipse.ui.handlers.RegistryToggleState.STATE_ID
	 */
	private static final String ORG_ECLIPSE_UI_COMMANDS_TOGGLE_STATE = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private static final String WW_SUPPORT = "org.eclipse.ui.IWorkbenchWindow"; //$NON-NLS-1$
	private static final String HCI_STATIC_CONTEXT = "HCI-staticContext"; //$NON-NLS-1$

	@Inject
	private ECommandService commandService;

	@Inject
	private EBindingService bindingService;

	@Inject
	@Optional
	private IUpdateService updateService;

	@Inject
	@Optional
	private EHelpService helpService;

	@Inject
	@Optional
	@SuppressWarnings("restriction")
	private ICommandHelpService commandHelpService;

	private Runnable unreferenceRunnable;

	private IStateListener stateListener = new IStateListener() {
		@Override
		public void handleStateChange(State state, Object oldValue) {
			updateState();
		}
	};

	private IEclipseContext infoContext;

	private State styleState;

	private State toggleState;

	private State radioState;

	@Override
	public void setModel(MItem item) {
		if (!(item instanceof MHandledItem)) {
			throw new IllegalArgumentException("Only instances of MHandledItem are allowed"); //$NON-NLS-1$
		}

		super.setModel(item);

		generateCommand();
		if (getModel().getCommand() == null) {
			if (logger != null) {
				logger.error("Element " + getModel().getElementId() + " invalid, no command defined."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * This method seems to be necessary for calls via reflection when called
	 * with MHandledItem parameter.
	 *
	 * @param item
	 *            The model item
	 */
	public void setModel(MHandledItem item) {
		setModel((MItem) item);
	}

	/**
	 *
	 */
	private void generateCommand() {
		if (getModel().getCommand() != null && getModel().getWbCommand() == null) {
			String cmdId = getModel().getCommand().getElementId();
			if (cmdId == null) {
				Activator.log(IStatus.ERROR, "Unable to generate parameterized command for " + getModel() //$NON-NLS-1$
						+ ". ElementId is not allowed to be null."); //$NON-NLS-1$
				return;
			}
			List<MParameter> modelParms = getModel().getParameters();
			Map<String, Object> parameters = new HashMap<>(4);
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
			ParameterizedCommand parmCmd = commandService.createCommand(cmdId, parameters);
			Activator.trace(Policy.DEBUG_MENUS, "command: " + parmCmd, null); //$NON-NLS-1$
			if (parmCmd == null) {
				Activator.log(IStatus.ERROR, "Unable to generate parameterized command for " + getModel() //$NON-NLS-1$
								+ " with " + parameters); //$NON-NLS-1$
				return;
			}

			getModel().setWbCommand(parmCmd);

			styleState = parmCmd.getCommand().getState(IMenuStateIds.STYLE);
			toggleState = parmCmd.getCommand().getState(ORG_ECLIPSE_UI_COMMANDS_TOGGLE_STATE);
			radioState = parmCmd.getCommand().getState(ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE);
			updateState();

			if (styleState != null) {
				styleState.addListener(stateListener);
			} else if (toggleState != null) {
				toggleState.addListener(stateListener);
			} else if (radioState != null) {
				radioState.addListener(stateListener);
			}
		}
	}

	private void updateState() {
		if (styleState != null) {
			getModel().setSelected(((Boolean) styleState.getValue()).booleanValue());
		} else if (toggleState != null) {
			getModel().setSelected(((Boolean) toggleState.getValue()).booleanValue());
		} else if (radioState != null && getModel().getWbCommand() != null) {
			ParameterizedCommand c = getModel().getWbCommand();
			Object parameter = c.getParameterMap().get(ORG_ECLIPSE_UI_COMMANDS_RADIO_STATE_PARAMETER);
			String value = (String) radioState.getValue();
			getModel().setSelected(value != null && value.equals(parameter));
		}
	}

	@Override
	protected void postMenuFill() {
		if (updateService != null) {
			unreferenceRunnable = updateService.registerElementForUpdate(getModel().getWbCommand(), getModel());
		}
	}

	@Override
	protected void postToolbarFill() {
		hookCheckListener();

		if (updateService != null) {
			unreferenceRunnable = updateService.registerElementForUpdate(getModel().getWbCommand(), getModel());
		}
	}

	private void hookCheckListener() {
		if (getModel().getType() != ItemType.CHECK) {
			return;
		}
		Object obj = getModel().getTransientData().get(ItemType.CHECK.toString());
		if (obj instanceof IContextFunction) {
			IEclipseContext context = getContext(getModel());
			IEclipseContext staticContext = getStaticContext(null);
			staticContext.set(MPart.class, context.get(MPart.class));
			staticContext.set(WW_SUPPORT, context.get(WW_SUPPORT));

			IContextFunction func = (IContextFunction) obj;
			obj = func.compute(staticContext, null);
			if (obj != null) {
				getModel().getTransientData().put(DISPOSABLE, obj);
			}
		}
	}

	private void unhookCheckListener() {
		if (getModel().getType() != ItemType.CHECK) {
			return;
		}
		final Object obj = getModel().getTransientData().remove(DISPOSABLE);
		if (obj == null) {
			return;
		}
		((Runnable) obj).run();
	}

	@Override
	protected void updateMenuItem() {
		MenuItem item = (MenuItem) widget;
		String text = getModel().getLocalizedLabel();
		ParameterizedCommand parmCmd = getModel().getWbCommand();
		String keyBindingText = null;
		if (parmCmd != null) {
			if (text == null || text.isEmpty()) {
				try {
					text = parmCmd.getName(getModel().getCommand().getLocalizedCommandName());
				} catch (NotDefinedException e) {
					e.printStackTrace();
				}
			}
			if (bindingService != null) {
				TriggerSequence binding = bindingService.getBestSequenceFor(parmCmd);
				if (binding != null)
					keyBindingText = binding.format();
			}
		}
		if (text != null) {
			if (getModel() instanceof MMenuElement) {
				String mnemonics = ((MMenuElement) getModel()).getMnemonics();
				if (mnemonics != null && !mnemonics.isEmpty()) {
					int idx = text.indexOf(mnemonics);
					if (idx != -1) {
						text = text.substring(0, idx) + '&'
								+ text.substring(idx);
					}
				}
			}
			if (keyBindingText == null)
				item.setText(text);
			else
				item.setText(text + '\t' + keyBindingText);
		} else {
			item.setText(""); //$NON-NLS-1$
		}
		final String tooltip = getToolTipText(false);
		item.setToolTipText(tooltip);
		item.setSelection(getModel().isSelected());
		item.setEnabled(getModel().isEnabled());
	}

	@Override
	protected void updateToolItem() {
		ToolItem item = (ToolItem) widget;

		if (item.getImage() == null || getModel().getTags().contains(FORCE_TEXT)) {
			final String text = getModel().getLocalizedLabel();
			if (text == null || text.length() == 0) {
				final MCommand command = getModel().getCommand();
				if (command == null) {
					// Set some text so that the item stays visible in the menu
					item.setText("UnLabled"); //$NON-NLS-1$
				} else {
					item.setText(command.getLocalizedCommandName());
				}
			} else {
				item.setText(text);
			}
		} else {
			item.setText(""); //$NON-NLS-1$
		}

		final String tooltip = getToolTipText(true);
		item.setToolTipText(tooltip);
		item.setSelection(getModel().isSelected());
		item.setEnabled(getModel().isEnabled());
	}

	private String getToolTipText(boolean attachKeybinding) {
		String text = getModel().getLocalizedTooltip();
		ParameterizedCommand parmCmd = getModel().getWbCommand();
		if (parmCmd == null) {
			generateCommand();
			parmCmd = getModel().getWbCommand();
		}

		if (parmCmd != null && text == null) {
			try {
				text = parmCmd.getName();
			} catch (NotDefinedException e) {
				return null;
			}
		}

		TriggerSequence sequence = bindingService.getBestSequenceFor(parmCmd);
		if (attachKeybinding && sequence != null) {
			text = text + " (" + sequence.format() + ')'; //$NON-NLS-1$
		}
		return text;
	}

	@Override
	protected void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			if (unreferenceRunnable != null) {
				unreferenceRunnable.run();
				unreferenceRunnable = null;
			}
			unhookCheckListener();
			ToolItemUpdater updater = getUpdater();
			if (updater != null) {
				updater.removeItem(this);
			}
			if (infoContext != null) {
				infoContext.dispose();
				infoContext = null;
			}
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget.removeListener(SWT.DefaultSelection, getItemListener());
			widget.removeListener(SWT.Help, getItemListener());
			widget = null;
			getModel().setWidget(null);
			disposeOldImages();
		}
	}

	@Override
	public void dispose() {
		if (widget != null) {
			if (unreferenceRunnable != null) {
				unreferenceRunnable.run();
				unreferenceRunnable = null;
			}

			ParameterizedCommand command = getModel().getWbCommand();
			if (command != null) {
				if (styleState != null) {
					styleState.removeListener(stateListener);
					styleState = null;
				}
				if (toggleState != null) {
					toggleState.removeListener(stateListener);
					toggleState = null;
				}
				if (radioState != null) {
					radioState.removeListener(stateListener);
					radioState = null;
				}
			}
			widget.dispose();
			widget = null;
			getModel().setWidget(null);
		}
	}

	@Override
	@SuppressWarnings("restriction")
	protected void handleHelpRequest() {
		MCommand command = getModel().getCommand();
		if (command == null || helpService == null
				|| commandHelpService == null) {
			return;
		}

		String contextHelpId = commandHelpService.getHelpContextId(command.getElementId(), getContext(getModel()));
		if (contextHelpId != null) {
			helpService.displayHelp(contextHelpId);
		}
	}

	private IEclipseContext getStaticContext(Event event) {
		if (infoContext == null) {
			infoContext = EclipseContextFactory.create(HCI_STATIC_CONTEXT);
			ContributionsAnalyzer.populateModelInterfaces(getModel(), infoContext,
					getModel().getClass().getInterfaces());
		}
		if (event == null) {
			infoContext.remove(Event.class);
		} else {
			infoContext.set(Event.class, event);
		}
		return infoContext;
	}

	@Override
	protected void executeItem(Event trigger) {
		ParameterizedCommand cmd = getModel().getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = getContext(getModel());
		EHandlerService service = (EHandlerService) lclContext.get(EHandlerService.class.getName());
		final IEclipseContext staticContext = getStaticContext(trigger);
		service.executeHandler(cmd, staticContext);
	}

	@Override
	protected boolean canExecuteItem(Event trigger) {
		ParameterizedCommand cmd = getModel().getWbCommand();
		if (cmd == null) {
			return false;
		}
		final IEclipseContext lclContext = getContext(getModel());
		EHandlerService service = lclContext.get(EHandlerService.class);
		if (service == null) {
			return false;
		}
		final IEclipseContext staticContext = getStaticContext(trigger);
		return service.canExecute(cmd, staticContext);
	}

	@Override
	public MHandledItem getModel() {
		return (MHandledItem) super.getModel();
	}
}
