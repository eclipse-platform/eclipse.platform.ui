#!/bin/bash

# script to upload a file called "output.txt" to https://wiki.eclipse.org/Platform_UI/Plan/4.6/Milestones
# usage: ./updateMileStoneWiki.sh eclipseuser eclipsepw 
# generate "output.txt" via:
#  ./wiki-from-csv.sh > output

ECLIPSELOGIN="https://dev.eclipse.org/site_login/index.php"
WIKIAPI="https://wiki.eclipse.org/api.php"
cookie_jar="eclipsewiki.txt"
USERNAME=$1
PASSWORD=$2
NEWTEXT=""
TITLE="Platform_UI/Plan/4.6/Milestones"
echo "Loggingin ..."
CR=$(curl 'https://dev.eclipse.org/site_login/index.php' -L \
--cookie $cookie_jar \
--cookie-jar $cookie_jar \
--data "username=${USERNAME}&password=${PASSWORD}&stage=login&takemeback=https%253A%252F%252Fwiki.eclipse.org%252Findex.php%253Ftitle%253DSpecial%253AUserlogin%2526action%253Dsubmitlogin%2526type%253Dlogin%2526returnto%253DMain_Page&btn-submit=")
echo "Fetching edit token..."
CR=$(curl -S \
--location \
--cookie $cookie_jar \
--cookie-jar $cookie_jar \
--keepalive-time 60 \
--header "Connection: keep-alive" \
--compressed \
--request "POST" "${WIKIAPI}?action=tokens&format=txt")
CR2=($CR)
EDITTOKEN=${CR2[8]}
if [ ${#EDITTOKEN} = 34 ]; then
echo "Edit token is: $EDITTOKEN"
else
echo "Edit token not set."
echo $CR
exit
fi
 
echo "Reading file"
 
echo $1
echo $2
echo $3
NEWTEXT=`cat output.txt`
echo $NEWTEXT

 
echo "Updating content ..."
 
 
CR=$(curl -S \
--location \
--cookie $cookie_jar \
--cookie-jar $cookie_jar \
--keepalive-time 60 \
--header "Accept-Language: en-us" \
--header "Connection: keep-alive" \
--header "Expect:" \
--form "token=${EDITTOKEN}" \
--form "title=${TITLE}" \
--form "text=${NEWTEXT}" \
--request "POST" "${WIKIAPI}?action=edit&format=json")
echo $CR
read -p "Done..."
exit 
