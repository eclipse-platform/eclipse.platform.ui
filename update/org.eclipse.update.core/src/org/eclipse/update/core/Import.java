package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;
import java.util.Map;
import org.apache.xml.serialize.IndentPrinter;
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.core.model.ImportModel;

/**
 * Default implementation of IImport
 */
public class Import extends ImportModel implements IImport {
	
	private static Map table;
	
	static {
		table = new HashMap();
		table.put("compatible",new Integer(IImport.RULE_COMPATIBLE));
		table.put("perfect",new Integer(IImport.RULE_PERFECT));
		table.put("equivalent",new Integer(IImport.RULE_EQUIVALENT));	
		table.put("greaterOrHigher",new Integer(IImport.RULE_GRATER_OR_EQUAL));
	}

	/**
	 * @see IImport#getIdentifier()
	 */
	public VersionedIdentifier getIdentifier() {
		return new VersionedIdentifier(getPluginIdentifier(),getPluginVersion());
	}

	/**
	 * @see IImport#getRule()
	 */
	public int getRule() {
		return ((Integer)table.get(getMatchingRuleName())).intValue();
	}

}

