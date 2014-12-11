#!/bin/bash

MILESTONE="$1"

create_wiki_section () {
	F_TARGET="$1" ; shift
	F_SEARCH="$1" ; shift

	NUM=1
	curl -o search.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?classification=Eclipse&component=Runtime&component=IDE&component=User%20Assistance&component=UI&list_id=5935738&product=Platform&query_format=advanced&target_milestone='$F_SEARCH'&query_based_on=&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv'
	grep -v target_milestone search.csv >t1 ; mv t1 search.csv


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

process() {
	echo "= Completed milestones ="
	echo ""

	create_wiki_section "${MILESTONE}" `echo ${MILESTONE} | sed -r 's/[ ]+/%20/g'`
}

print_help() {
   echo -e "\nThe script generates the list of bugs for given release and milestone."
   echo -e "It is the content of the following WIKI page: https://wiki.eclipse.org/Platform_UI/Plan/4.x/Planning_Bugs."
   echo -e "After generating the WIKI table with bugs it needs to be copied manually to the WIKI page.\n"
   echo -e "Usage: `basename $0` [release\ milestone]\n"
   echo -e "For instance in order to generate the WIKI for '4.5 M4', the following command needs to be executed: `basename $0` 4.5\ M4\n"
   echo -e "\n--------------------------------------------------------------------"
   echo -e "The WIKI page section with the Distributed Development Team members:\n"
   echo -e "According to our current rules for the DDT participation, when particular bug belongs to the priority list of the current release [1] then the bug's assignee becomes the DDT member for the particular milestone. Afterwards their name needs to be added to the DDT section of the proper milestone.\n"
   echo -e "[1] https://wiki.eclipse.org/Platform_UI/Plan/4.x/Planning_Bugs\n"
}

if [ "$1" == "--help" -o "$1" == "" ]; then
  print_help  
else
  process
fi

exit 0



