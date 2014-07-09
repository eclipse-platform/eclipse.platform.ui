#!/bin/bash 

rm -f mars_prior_list.txt

while read BUG_NUM; do
	LINE=""
	WEIGHT=0
	LINE_END=$( grep ${BUG_NUM} mars_04Dani.csv | head -1 | csvtool col 2,4 - | sed 's!^\([0-9]*\),!https://bugs.eclipse.org/bugs/show_bug.cgi?id=\1,\1,!g' )
	for file in mars*.csv; do
		LW=$( grep ${BUG_NUM} $file |  head -1 | csvtool col 1 - )
		if [ -z "$LW" -o 0 = "$LW" ]; then
			LINE="${LINE}${LW},"
		else
			LINE="${LINE}${LW},"
			WEIGHT=$(( WEIGHT + 2**LW ))
		fi
	done
	echo "${LINE}${WEIGHT},${LINE_END}" >> mars_prior_list.txt
done <ordered_list.txt

sort -rns -t, -k8 mars_prior_list.txt >sorted_bug_list.csv


