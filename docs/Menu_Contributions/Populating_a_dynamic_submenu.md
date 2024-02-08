Menu Contributions/Populating a dynamic submenu
===============================================

Add a dynamic submenu to the ProblemView menu
=============================================

In [Menu Contributions/Problems View Example](./Problems_View_Example.md "Menu Contributions/Problems View Example") we added 2 dynamic menus. 
You then have to extend the abstract [CompoundContributionItem](http://help.eclipse.org/latest/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/actions/CompoundContributionItem.html) class in your provided class.

        <menu id="org.eclipse.ui.views.problems.groupBy.menu"
        label="%ProblemView.GroupBy.label"
        mnemonic="%ProblemView.GroupBy.mnemonic">
        <dynamic class="org.eclipse.ui.views.markers.internal.GroupByItems"
        id="org.eclipse.ui.views.problems.groupBy.items"/>
        </menu>

When your menu is populated, you'll have your getContributionItems() method called:

        protected IContributionItem[] getContributionItems() {
            IContributionItem[] list = new IContributionItem[2];
            Map parms = new HashMap();
            parms.put("groupBy", "Severity");
            list[0] = new CommandContributionItem(null,
                    "org.eclipse.ui.views.problems.grouping",
                    parms, null, null, null, "Severity", null,
                    null, CommandContributionItem.STYLE_PUSH);
         
            parms = new HashMap();
            parms.put("groupBy", "None");
            list[1] = new CommandContributionItem(null,
                    "org.eclipse.ui.views.problems.grouping",
                    parms, null, null, null, "None", null, null,
                    CommandContributionItem.STYLE_PUSH);
            return list;
        }

