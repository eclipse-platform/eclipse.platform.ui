#!/bin/bash


create_bugs () {
BUGS="$1"

NUM=1
curl -o search3.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?bug_id='${BUGS}'&bug_id_type=anyexact&list_id=9354617&query_format=advanced&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv&human=1'
grep -v target_milestone search3.csv | grep -v "Bug ID" | grep -v Lars \
 | grep -v RESOLVED | grep -v VERIFIED | grep -v CLOSED >t1 ; mv t1 search3.csv
}

get_new_bugs () {
BUGS="$1"

NUM=1
curl -o search2.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?bug_id='${BUGS}'&bug_id_type=anyexact&list_id=9354617&query_format=advanced&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv&human=1'
grep -v target_milestone search2.csv | grep -v "Bug ID" | grep -v Lars \
 | grep -v RESOLVED | grep -v VERIFIED | grep -v CLOSED >t1 ; mv t1 search2.csv
}


get_scheduled_bugs () {
curl -o search1.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&classification=Eclipse&component=IDE&component=Runtime&component=UI&component=User%20Assistance&list_id=9359370&product=Platform&query_format=advanced&target_milestone=4.4.1&target_milestone=4.5&target_milestone=4.5%20M1&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv&human=1'
grep -v target_milestone search1.csv | grep -v "Bug ID" | grep -v Lars \
 | grep -v RESOLVED | grep -v VERIFIED | grep -v CLOSED >t2 ; mv t2 search1.csv
}

post_process_bugs () {
rm -f search.csv
touch search.csv
for f in 1 2 3; do

	while read line; do
		BUG=$( echo "$line" | cut -f1 -d, )
		if ! grep "^${BUG}," search.csv >/dev/null; then
			echo "$line" >>search.csv
		fi
	done <search${f}.csv

done
sed 's!^\([0-9]*\),!https://bugs.eclipse.org/bugs/show_bug.cgi?id=\1,,!g' search.csv >t1 
echo "Bug,Priority,Milestone,Assignee,Status,Resolution,Title,Importance,QAContact" >search_final.csv
cat t1 >>search_final.csv
}

tmp_func () {

echo "== $F_TARGET =="
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
done < search.csv

echo '|-'
echo '|}'
echo ""
echo "Last Generated on '''$(date)'''"
echo ""

}

BUGS="430872"
while read line; do
BUGS="${BUGS}%2C$line"
done <planning44.txt

create_bugs "$BUGS"

BUGS="430872"
while read line; do
BUGS="${BUGS}%2C$line"
done <bugsFrom6Months.txt

get_new_bugs "$BUGS"

get_scheduled_bugs
post_process_bugs

