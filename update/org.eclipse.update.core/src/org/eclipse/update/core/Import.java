package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.HashMap;
import java.util.Map;

import org.eclipse.update.core.model.ImportModel;

/**
 * Convenience implementation of a plug-in dependency.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IImport
 * @see org.eclipse.update.core.model.ImportModel
 * @since 2.0
 */
public class Import extends ImportModel implements IImport {

	private static Map table;

	static {
		table = new HashMap();
		table.put("compatible", new Integer(IImport.RULE_COMPATIBLE)); //$NON-NLS-1$
		table.put("perfect", new Integer(IImport.RULE_PERFECT)); //$NON-NLS-1$
		table.put("equivalent", new Integer(IImport.RULE_EQUIVALENT)); //$NON-NLS-1$
		table.put("greaterOrHigher", new Integer(IImport.RULE_GRATER_OR_EQUAL));
		//$NON-NLS-1$
	}

	/**
	 * Returns an identifier of the dependent plug-in.
	 * @see IImport#getIdentifier()
	 */
	public VersionedIdentifier getVersionedIdentifier() {
		return new VersionedIdentifier(getPluginIdentifier(), getPluginVersion());
	}

	/**
	 * Returns the matching rule for the dependency.
	 * @see IImport#getRule()
	 */
	public int getRule() {
		return ((Integer) table.get(getMatchingRuleName())).intValue();
	}

}