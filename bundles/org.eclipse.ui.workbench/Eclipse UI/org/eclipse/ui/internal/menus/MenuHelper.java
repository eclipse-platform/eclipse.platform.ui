package org.eclipse.ui.internal.menus;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.CommandContributionItem;

public class MenuHelper {

	public static void trace(String msg, Throwable error) {
		WorkbenchSWTActivator.trace(Policy.MENUS, msg, error);
	}

	public static final String ACTION_SET_CMD_PREFIX = "AS::"; //$NON-NLS-1$
	public static final String MAIN_MENU_ID = "org.eclipse.ui.main.menu"; //$NON-NLS-1$
	private static Field urlField;

	public static int indexForId(MElementContainer<MMenuElement> parentMenu, String id) {
		if (id == null || id.length() == 0) {
			return -1;
		}
		int i = 0;
		for (MMenuElement item : parentMenu.getChildren()) {
			if (id.equals(item.getElementId())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static String getActionSetCommandId(IConfigurationElement element) {
		String id = MenuHelper.getDefinitionId(element);
		if (id != null) {
			return id;
		}
		id = MenuHelper.getId(element);
		String actionSetId = null;
		Object obj = element.getParent();
		while (obj instanceof IConfigurationElement && actionSetId == null) {
			IConfigurationElement parent = (IConfigurationElement) obj;
			if (parent.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
				actionSetId = MenuHelper.getId(parent);
			}
			obj = parent.getParent();
		}
		return ACTION_SET_CMD_PREFIX + actionSetId + '/' + id;
	}

	/**
	 * @param imageDescriptor
	 * @return
	 */
	public static String getImageUrl(ImageDescriptor imageDescriptor) {
		if (imageDescriptor == null)
			return null;
		Class idc = imageDescriptor.getClass();
		if (idc.getName().endsWith("URLImageDescriptor")) { //$NON-NLS-1$
			URL url = getUrl(idc, imageDescriptor);
			return url.toExternalForm();
		}
		return null;
	}

	private static URL getUrl(Class idc, ImageDescriptor imageDescriptor) {
		try {
			if (urlField == null) {
				urlField = idc.getDeclaredField("url"); //$NON-NLS-1$
				urlField.setAccessible(true);
			}
			return (URL) urlField.get(imageDescriptor);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static MCommand getCommandById(MApplication app, String cmdId) {
		final List<MCommand> cmds = app.getCommands();
		for (MCommand cmd : cmds) {
			if (cmdId.equals(cmd.getElementId())) {
				return cmd;
			}
		}
		return null;
	}

	/**
	 * @param element
	 *            the configuration element
	 * @return <code>true</code> if the checkEnabled is <code>true</code>.
	 */
	static boolean getVisibleEnabled(IConfigurationElement element) {
		IConfigurationElement[] children = element
				.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
		String checkEnabled = null;

		if (children.length > 0) {
			checkEnabled = children[0].getAttribute(IWorkbenchRegistryConstants.ATT_CHECK_ENABLED);
		}

		return checkEnabled != null && checkEnabled.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	static MExpression getVisibleWhen(IConfigurationElement commandAddition) {
		try {
			IConfigurationElement[] visibleConfig = commandAddition
					.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
			if (visibleConfig.length > 0 && visibleConfig.length < 2) {
				IConfigurationElement[] visibleChild = visibleConfig[0].getChildren();
				if (visibleChild.length > 0) {
					Expression visWhen = ExpressionConverter.getDefault().perform(visibleChild[0]);
					MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
					exp.setCoreExpressionId("programmatic.value"); //$NON-NLS-1$
					exp.setCoreExpression(visWhen);
					return exp;
					// visWhenMap.put(configElement, visWhen);
				}
			}
		} catch (InvalidRegistryObjectException e) {
			// visWhenMap.put(configElement, null);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// visWhenMap.put(configElement, null);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static class Key {
		private MMenuContribution contribution;
		private int tag = -1;
		private int hc = -1;

		public Key(MMenuContribution mc) {
			this.contribution = mc;
			mc.setWidget(this);
		}

		int getSchemeTag() {
			if (tag == -1) {
				List<String> tags = contribution.getTags();
				if (tags.contains("scheme:menu")) { //$NON-NLS-1$
					tag = 1;
				} else if (tags.contains("scheme:popup")) { //$NON-NLS-1$
					tag = 2;
				} else if (tags.contains("scheme:toolbar")) { //$NON-NLS-1$
					tag = 3;
				} else {
					tag = 0;
				}
			}
			return tag;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key)) {
				return false;
			}
			Key other = (Key) obj;
			MCoreExpression vexp1 = (MCoreExpression) contribution.getVisibleWhen();
			Object exp1 = vexp1 == null ? null : vexp1.getCoreExpression();
			MCoreExpression vexp2 = (MCoreExpression) other.contribution.getVisibleWhen();
			Object exp2 = vexp2 == null ? null : vexp2.getCoreExpression();
			return Util.equals(contribution.getParentID(), other.contribution.getParentID())
					&& Util.equals(contribution.getPositionInParent(),
							other.contribution.getPositionInParent())
					&& getSchemeTag() == other.getSchemeTag() && Util.equals(exp1, exp2);
		}

		@Override
		public int hashCode() {
			if (hc == -1) {
				MCoreExpression vexp1 = (MCoreExpression) contribution.getVisibleWhen();
				Object exp1 = vexp1 == null ? null : vexp1.getCoreExpression();
				hc = Util.hashCode(contribution.getParentID());
				hc = hc * 87 + Util.hashCode(contribution.getPositionInParent());
				hc = hc * 87 + getSchemeTag();
				hc = hc * 87 + Util.hashCode(exp1);
			}
			return hc;
		}

		@Override
		public String toString() {
			return "Key " + contribution.getParentID() + "--" + contribution.getPositionInParent() //$NON-NLS-1$ //$NON-NLS-2$
					+ "--" + getSchemeTag() + "--" + contribution.getVisibleWhen(); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private static Key getKey(MMenuContribution contribution) {
		if (contribution.getWidget() instanceof Key) {
			return (Key) contribution.getWidget();
		}
		return new Key(contribution);
	}

	public static void printContributions(ArrayList<MMenuContribution> contributions) {
		for (MMenuContribution c : contributions) {
			trace("\n" + c, null); //$NON-NLS-1$
			for (MMenuElement element : c.getChildren()) {
				printElement(1, element);
			}
		}
	}

	private static void printElement(int level, MMenuElement element) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < level; i++) {
			buf.append('\t');
		}
		buf.append(element.toString());
		trace(buf.toString(), null);
		if (element instanceof MMenu) {
			for (MMenuElement item : ((MMenu) element).getChildren()) {
				printElement(level + 1, item);
			}
		}
	}

	public static void mergeContributions(ArrayList<MMenuContribution> contributions,
			ArrayList<MMenuContribution> result) {
		HashMap<Key, ArrayList<MMenuContribution>> buckets = new HashMap<Key, ArrayList<MMenuContribution>>();
		trace("mergeContributions size: " + contributions.size(), null); //$NON-NLS-1$
		printContributions(contributions);
		// first pass, sort by parentId?position,scheme,visibleWhen
		for (MMenuContribution contribution : contributions) {
			Key key = getKey(contribution);
			ArrayList<MMenuContribution> slot = buckets.get(key);
			if (slot == null) {
				slot = new ArrayList<MMenuContribution>();
				buckets.put(key, slot);
			}
			slot.add(contribution);
		}
		Iterator<MMenuContribution> i = contributions.iterator();
		while (i.hasNext() && !buckets.isEmpty()) {
			MMenuContribution contribution = i.next();
			Key key = getKey(contribution);
			ArrayList<MMenuContribution> slot = buckets.remove(key);
			if (slot == null) {
				continue;
			}
			MMenuContribution toContribute = null;
			for (MMenuContribution item : slot) {
				if (toContribute == null) {
					toContribute = item;
					continue;
				}
				Object[] array = item.getChildren().toArray();
				for (int c = 0; c < array.length; c++) {
					MMenuElement me = (MMenuElement) array[c];
					if (!containsMatching(toContribute.getChildren(), me)) {
						toContribute.getChildren().add(me);
					}
				}
			}
			if (toContribute != null) {
				toContribute.setWidget(null);
				result.add(toContribute);
			}
		}
		trace("mergeContributions: final size: " + result.size(), null); //$NON-NLS-1$
	}

	private static boolean containsMatching(List<MMenuElement> children, MMenuElement me) {
		for (MMenuElement element : children) {
			if (Util.equals(me.getElementId(), element.getElementId())
					&& element.getClass().isInstance(me)
					&& (element instanceof MMenuSeparator || element instanceof MMenu)) {
				return true;
			}
		}
		return false;
	}

	public static void mergeActionSetContributions(ArrayList<MMenuContribution> contributions,
			ArrayList<MMenuContribution> result) {
		ListIterator<MMenuContribution> i = contributions.listIterator(contributions.size());
		MMenuContribution currentContribution = null;
		while (i.hasPrevious()) {
			MMenuContribution c = i.previous();
			if (currentContribution == null) {
				currentContribution = c;
				continue;
			}
			if (c.getParentID().equals(currentContribution.getParentID())
					&& c.getPositionInParent().equals(currentContribution.getPositionInParent())) {
				ListIterator<MMenuElement> j = c.getChildren().listIterator(c.getChildren().size());
				while (j.hasPrevious()) {
					currentContribution.getChildren().add(j.previous());
				}
				c.getChildren().clear();
			} else {
				result.add(currentContribution);
				currentContribution = c;
			}
		}
		if (currentContribution != null) {
			result.add(currentContribution);
		}
	}

	/*
	 * Support Utilities
	 */
	public static String getId(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		// For sub-menu management -all- items must be id'd so enforce this
		// here (we could optimize by checking the 'name' of the config
		// element == "menu"
		if (id == null || id.length() == 0) {
			id = getCommandId(element);
		}
		if (id == null || id.length() == 0) {
			id = element.toString();
		}

		return id;
	}

	static String getName(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	static int getMode(IConfigurationElement element) {
		if ("FORCE_TEXT".equals(element.getAttribute(IWorkbenchRegistryConstants.ATT_MODE))) { //$NON-NLS-1$
			return CommandContributionItem.MODE_FORCE_TEXT;
		}
		return 0;
	}

	static String getLabel(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}

	static String getPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_PATH);
	}

	static String getMenuBarPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MENUBAR_PATH);
	}

	static String getMnemonic(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MNEMONIC);
	}

	static String getTooltip(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}

	static String getIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	}

	static String getDisabledIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_DISABLEDICON);
	}

	static String getHoverIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_HOVERICON);
	}

	static String getIconUrl(IConfigurationElement element, String attr) {
		String extendingPluginId = element.getDeclaringExtension().getContributor().getName();

		String iconPath = element.getAttribute(attr);
		if (iconPath == null) {
			return null;
		}
		if (!iconPath.startsWith("platform:")) { //$NON-NLS-1$
			iconPath = "platform:/plugin/" + extendingPluginId + "/" + iconPath; //$NON-NLS-1$//$NON-NLS-2$
		}
		URL url = null;
		try {
			url = FileLocator.find(new URL(iconPath));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url == null ? null : url.toString();
	}

	static String getHelpContextId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_HELP_CONTEXT_ID);
	}

	public static boolean isSeparatorVisible(IConfigurationElement element) {
		String val = element.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE);
		return Boolean.valueOf(val).booleanValue();
	}

	public static String getClassSpec(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}

	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	public static ItemType getStyle(IConfigurationElement element) {
		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		if (style == null || style.length() == 0) {
			return ItemType.PUSH;
		}
		if (IWorkbenchRegistryConstants.STYLE_TOGGLE.equals(style)) {
			return ItemType.CHECK;
		}
		if (IWorkbenchRegistryConstants.STYLE_RADIO.equals(style)) {
			return ItemType.RADIO;
		}
		if (IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style)) {
			trace("Failed to get style for " + IWorkbenchRegistryConstants.STYLE_PULLDOWN, null); //$NON-NLS-1$
			// return CommandContributionItem.STYLE_PULLDOWN;
		}
		return ItemType.PUSH;
	}

	public static boolean getRetarget(IConfigurationElement element) {
		String r = element.getAttribute(IWorkbenchRegistryConstants.ATT_RETARGET);
		return Boolean.valueOf(r);
	}

	public static String getDefinitionId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFINITION_ID);
	}

	public static Map<String, String> getParameters(IConfigurationElement element) {
		HashMap<String, String> map = new HashMap<String, String>();
		IConfigurationElement[] parameters = element
				.getChildren(IWorkbenchRegistryConstants.TAG_PARAMETER);
		for (int i = 0; i < parameters.length; i++) {
			String name = parameters[i].getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			String value = parameters[i].getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
			if (name != null && value != null) {
				map.put(name, value);
			}
		}
		return map;
	}

	public static MMenu createMenuAddition(IConfigurationElement menuAddition) {
		MMenu element = MenuFactoryImpl.eINSTANCE.createMenu();
		String id = MenuHelper.getId(menuAddition);
		element.setElementId(id);
		String text = MenuHelper.getLabel(menuAddition);
		String mnemonic = MenuHelper.getMnemonic(menuAddition);
		if (text != null && mnemonic != null) {
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		element.setIconURI(MenuHelper
				.getIconUrl(menuAddition, IWorkbenchRegistryConstants.ATT_ICON));
		element.setLabel(Util.safeString(text));

		return element;
	}

	public static MMenuElement createLegacyActionAdditions(MApplication app,
			IConfigurationElement element) {
		String id = MenuHelper.getId(element);
		String text = MenuHelper.getLabel(element);
		String mnemonic = MenuHelper.getMnemonic(element);
		if (text != null && mnemonic != null) {
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		String iconUri = MenuHelper.getIconUrl(element, IWorkbenchRegistryConstants.ATT_ICON);
		String cmdId = MenuHelper.getActionSetCommandId(element);
		if (cmdId == null) {
			return null;
		}
		MHandledMenuItem item = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
		item.setElementId(id);
		item.setLabel(text);
		MCommand cmd = getCommandById(app, cmdId);
		if (cmd == null) {
			trace("Failed to find command: " + cmdId, null); //$NON-NLS-1$
			return null;
		}
		item.setCommand(cmd);
		if (iconUri != null) {
			item.setIconURI(iconUri);
		}
		return item;
	}

	public static String getDescription(IConfigurationElement configElement) {
		return configElement.getAttribute(IWorkbenchRegistryConstants.TAG_DESCRIPTION);
	}
}
