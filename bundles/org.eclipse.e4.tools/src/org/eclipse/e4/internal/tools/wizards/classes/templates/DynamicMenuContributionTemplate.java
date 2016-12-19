package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.NewDynamicMenuContributionClassWizard.DynamicMenuContributionClass;

public class DynamicMenuContributionTemplate
{
  protected static String nl;
  public static synchronized DynamicMenuContributionTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    DynamicMenuContributionTemplate result = new DynamicMenuContributionTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = " ";
  protected final String TEXT_2 = NL + "package ";
  protected final String TEXT_3 = ";";
  protected final String TEXT_4 = NL + NL + "import java.util.List;" + NL + "" + NL + "import org.eclipse.e4.ui.di.AboutToShow;";
  protected final String TEXT_5 = NL + "import org.eclipse.e4.ui.di.AboutToHide;";
  protected final String TEXT_6 = NL + NL + "import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;" + NL + "" + NL + "public class ";
  protected final String TEXT_7 = " {" + NL + "\t@AboutToShow" + NL + "\tpublic void ";
  protected final String TEXT_8 = "(List<MMenuElement> items) {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_9 = NL + "\t" + NL + "\t@AboutToHide" + NL + "\tpublic void ";
  protected final String TEXT_10 = "(List<MMenuElement> items) {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_11 = "\t" + NL + "}";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     DynamicMenuContributionClass domainClass = (DynamicMenuContributionClass)argument; 
    stringBuffer.append(TEXT_1);
     if( domainClass.getPackageFragment() != null && domainClass.getPackageFragment().getElementName().trim().length() > 0 ) { 
    stringBuffer.append(TEXT_2);
    stringBuffer.append( domainClass.getPackageFragment().getElementName() );
    stringBuffer.append(TEXT_3);
     } 
    stringBuffer.append(TEXT_4);
     if( domainClass.isUseAboutToHide() )  { 
    stringBuffer.append(TEXT_5);
     } 
    stringBuffer.append(TEXT_6);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_7);
    stringBuffer.append( domainClass.getAboutToShowMethodName() );
    stringBuffer.append(TEXT_8);
     if( domainClass.isUseAboutToHide() )  { 
    stringBuffer.append(TEXT_9);
    stringBuffer.append( domainClass.getAboutToHideMethodName() );
    stringBuffer.append(TEXT_10);
     } 
    stringBuffer.append(TEXT_11);
    return stringBuffer.toString();
  }
}
