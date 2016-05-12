#!/bin/bash

create_wiki_header() {
echo "Overview page of the work done for the Eclipse 4.5 service and 4.6 release. For the general themes of this release please see [https://wiki.eclipse.org/Platform_UI/Plan/4.6 Platform UI targets for Eclipse Neon] "
echo ""
echo "'''This list is generated automatically, please do not update manually'''"
echo ""
echo "This list is your planning backlog for the Eclipse 4.6 release. The list will be refreshed in order to better adjust the priorities of the bugs during the release."
}

create_wiki_section () {
F_TARGET="$1" ; shift
F_SEARCH="$1" ; shift

NUM=1
curl -o search.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?classification=Eclipse&component=Runtime&component=IDE&component=User%20Assistance&component=UI&component=Text&order=bug_status&list_id=5935738&product=Platform&query_format=advanced&target_milestone='$F_SEARCH'&query_based_on=&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Ccomponent&ctype=csv'
grep -v target_milestone search.csv >t1 ; mv t1 search.csv


echo "== $F_TARGET =="
echo ""
echo '{| class="wikitable sortable" border="1"'
echo '|-'
echo '! !! Bug !! TM !! Component !! Sev !! Assign !! Status !! Title'

while read line; do
	BUG=$( echo $line | csvtool col 1 - )
	TARGET=$( echo $line | csvtool col 2 - )
	ASSIGNED_TO=$( echo $line | csvtool col 3 - )
	STATUS=$( echo $line | csvtool col 4 - )
	if [ CLOSED = "$STATUS" -o RESOLVED = "$STATUS" -o VERIFIED = "$STATUS" ]; then
		STATUS=$( echo $line | csvtool col 5 - )
		PRE="<strike>"
		POST="</strike>"
	fi
	TITLE=$( echo $line | csvtool col 6 - )
	SEV=$( echo $line | csvtool col 7 - )
	COMPONENT=$( echo $line | csvtool col 8 - )
        if [ "$TITLE -gt 100" ]; then
		TITLE="${TITLE:0:100}"
	fi

	echo '|-'
	echo "| $NUM || $PRE{{bug|$BUG}}$POST || $TARGET || $COMPONENT || $SEV || $ASSIGNED_TO || $STATUS || $PRE$TITLE$POST"
	BUG=""
	TARGET=""
	TITLE=""
	STATUS=""
	ASSIGNED_TO=""
	SEV=""
	PRE=""
	POST=""
	COMPONENT=""
	(( NUM = NUM + 1 ))
done < search.csv

echo '|-'
echo '|}'
echo ""
echo "Last Generated on '''$(date)'''"
echo ""

}

create_wiki_header

echo "= Work targeting Eclipse 4.5.1 and 4.5.2 ="

create_wiki_section "4.5.1" 4.5.1
create_wiki_section "4.5.2" 4.5.2


echo "= Work targeting Eclipse 4.6 ="

echo ""

create_wiki_section "4.6 M1" 4.6%20M1
create_wiki_section "4.6 M2" 4.6%20M2
create_wiki_section "4.6 M3" 4.6%20M3
create_wiki_section "4.6 M4" 4.6%20M4
create_wiki_section "4.6 M5" 4.6%20M5
create_wiki_section "4.6 M6" 4.6%20M6
create_wiki_section "4.6 M7" 4.6%20M7
create_wiki_section "4.6 RC1" 4.6%20RC1
create_wiki_section "4.6 RC2" 4.6%20RC2
create_wiki_section "4.6 RC3" 4.6%20RC3
create_wiki_section "4.6 RC4" 4.6%20RC4
create_wiki_section "4.6" 4.6




