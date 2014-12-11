#!/bin/bash


get_csv_bugs () {
	BUGS="$1"

	NUM=1
	curl -o search_o.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?bug_id='${BUGS}'&bug_id_type=anyexact&list_id=9354617&query_format=advanced&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv&human=1'
	grep -v target_milestone search_o.csv | grep -v "Bug ID" | grep -v Lars \
	  >t1 ; mv t1 search_o.csv
}

get_other_csv_bugs () {
	BUGS="$1"

	NUM=1
	curl -o search_o_other.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?bug_id='${BUGS}'&bug_id_type=nowords&classification=Eclipse&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&component=IDE&component=UI&list_id=9455933&product=Platform&query_format=advanced&target_milestone=4.4.1&target_milestone=4.5&target_milestone=4.5%20M1&&ctype=csv&human=1'
	grep -v target_milestone search_o_other.csv | grep -v "Bug ID" | grep -v Lars \
	  >t1 ; mv t1 search_o_other.csv
}

sort_csv () {
	rm -f tmp.txt
	while read line; do
		grep "^${line}," search_o.csv >>tmp.txt
	done <ordered_list.txt
	mv tmp.txt search_o.csv
}

gen_bugs_table () {
	FILE="$1"
	while read line; do
		BUG=$( echo $line | csvtool col 1 - )
		TARGET=$( echo $line | csvtool col 2 - )
		ASSIGNED_TO=$( echo $line | csvtool col 3 - )
		STATUS=$( echo $line | csvtool col 4 - )
		if [ FIXED != "$STATUS" -a CLOSED != "$STATUS" -a RESOLVED != "$STATUS" -a VERIFIED != "$STATUS" ]; then
		    TITLE=$( echo $line | csvtool col 6 - )
		    SEV=$( echo $line | csvtool col 7 - )
		    QA=$( echo $line | csvtool col 8 - )
		    if [ "platform-ui-triaged" = "$ASSIGNED_TO" -a ! -z "$QA" ]; then
				ASSIGNED_TO="$QA"
		    fi

		    echo '|-'
		    echo "| $NUM || {{bug|$BUG}} || $TARGET || $SEV || $ASSIGNED_TO || $STATUS || $PRE$TITLE$POST"
            	    (( NUM = NUM + 1 ))
            	fi
		BUG=""
		TARGET=""
		TITLE=""
		STATUS=""
		ASSIGNED_TO=""
		SEV=""
		
	done < ${FILE}
}

gen_wiki () {
	NUM=1

	echo "== Bugs for Planning =="
	echo "[https://bugs.eclipse.org/bugs/buglist.cgi?quicksearch=${BUGS}&bug_id_type=anyexact&list_id=9354617&query_format=advanced&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&human=1 Display bugs in Bugzilla]"

	echo ""
	echo '{| class="wikitable" border="1"'
	echo '|-'
	echo '! !! Bug !! TM !! Sev !! Assign !! Status !! Title'

	gen_bugs_table search_o.csv

	echo '|-'
	echo '|}'
	echo ""

}

gen_other_wiki () {
	NUM=1

	echo "== Other 4.4.1 and 4.5 bugs for Planning =="
	echo "[https://bugs.eclipse.org/bugs/buglist.cgi?quicksearch=${BUGS}&bug_id_type=nowords&classification=Eclipse&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&component=IDE&component=UI&list_id=9455933&product=Platform&query_format=advanced&target_milestone=4.4.2&target_milestone=4.5&target_milestone=4.5%20M1&human=1 Display bugs in Bugzilla]"

	echo ""
	echo '{| class="wikitable" border="1"'
	echo '|-'
	echo '! !! Bug !! TM !! Sev !! Assign !! Status !! Title'

	gen_bugs_table search_o_other.csv

	echo '|-'
	echo '|}'
	echo ""
	echo "Last Generated on '''$(date)'''"
	echo ""
}

process() {
	BUGS="435024"
	while read line; do
		BUGS="${BUGS}%2C$line"
	done <ordered_list.txt

	get_csv_bugs "$BUGS"

	get_other_csv_bugs "$BUGS"

	sort_csv

	gen_wiki

	gen_other_wiki
}

print_help() {
   echo -e "\nThe script generates the priolity list of bugs for the following WIKI page: https://wiki.eclipse.org/Platform_UI/Plan/4.x/Planning_Bugs."
   echo -e "After generating the WIKI table with bugs it needs to be copied to the WIKI page\n"
   echo -e "Usage: `basename $0` [ordered_list.txt file]\n"
}


if [ "$1" == "--help" -o "$1" == "" ]; then
  print_help  
else
  process
fi

exit 0


