#!/bin/bash


get_csv_bugs () {
BUGS="$1"

NUM=1
curl -o search_o.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?bug_id='${BUGS}'&bug_id_type=anyexact&list_id=9354617&query_format=advanced&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv&human=1'
grep -v target_milestone search_o.csv | grep -v "Bug ID" | grep -v Lars \
 | grep -v RESOLVED | grep -v VERIFIED | grep -v CLOSED >t1 ; mv t1 search_o.csv
}

sort_csv () {
rm -f tmp.txt
while read line; do
	grep "^${line}," search_o.csv >>tmp.txt
done <ordered_list.txt
mv tmp.txt search_o.csv
}


gen_wiki () {

NUM=1

echo "== Bugs for Planning =="
echo ""
echo '{| class="wikitable" border="1"'
echo '|-'
echo '! !! Bug !! TM !! Sev !! Assign !! Status !! Title'

while read line; do
	BUG=$( echo $line | csvtool col 1 - )
	TARGET=$( echo $line | csvtool col 2 - )
	ASSIGNED_TO=$( echo $line | csvtool col 3 - )
	STATUS=$( echo $line | csvtool col 4 - )
	if [ RESOLVED = "$STATUS" -o VERIFIED = "$STATUS" ]; then
		STATUS=$( echo $line | csvtool col 5 - )
		PRE="<strike>"
		POST="</strike>"
	fi
	TITLE=$( echo $line | csvtool col 6 - )
	SEV=$( echo $line | csvtool col 7 - )
	QA=$( echo $line | csvtool col 8 - )
	if [ "platform-ui-triaged" = "$ASSIGNED_TO" -a ! -z "$QA" ]; then
		ASSIGNED_TO="$QA"
	fi

	echo '|-'
	echo "| $NUM || $PRE{{bug|$BUG}}$POST || $TARGET || $SEV || $ASSIGNED_TO || $STATUS || $PRE$TITLE$POST"
	BUG=""
	TARGET=""
	TITLE=""
	STATUS=""
	ASSIGNED_TO=""
	SEV=""
	PRE=""
	POST=""
	(( NUM = NUM + 1 ))
done < search_o.csv

echo '|-'
echo '|}'
echo ""
echo "Last Generated on '''$(date)'''"
echo ""

}

BUGS="435024"
while read line; do
BUGS="${BUGS}%2C$line"
done <ordered_list.txt

get_csv_bugs "$BUGS"

sort_csv

gen_wiki 
