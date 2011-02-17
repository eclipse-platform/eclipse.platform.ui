
var defaultName = "http://help.eclipse.org/helios/";

function getHelpPath() {
    var path = getCookie();
    if (path !== null) return decodeURIComponent(path);
    return defaultName;
}

function showHelpPath() {
    var pathNode = document.getElementById("path");
    var pathValue=document.createTextNode("Testing help system: " + getHelpPath() + "index.jsp");
    pathNode.appendChild(pathValue);
}

// Patches every anchor in a page
function patchAnchors() {
    var doclinks = document.getElementsByTagName("a");
    for (var i = 0; i < doclinks.length; i++) {
        var slash = doclinks[i].href.indexOf('/', 8);
        slash = doclinks[i].href.indexOf('/', slash + 1);
        doclinks[i].href = getHelpPath() + doclinks[i].href.substring(slash + 1);
    }
}

function getCookie() {
	var nameEquals = "server=";
	var cookies = document.cookie.split(";");
	for (var i=0;i<cookies.length;++i) {
		var cookie = cookies[i];
		if (cookie.charAt(0) == ' ') {
			cookie = cookie.substring(1, cookie.length);
		}
		if (cookie.indexOf(nameEquals) == 0) {
			return cookie.substring(nameEquals.length, cookie.length);
		}
	}
	return null;
}

function setCookie(value) {
	var date = new Date();
	date.setTime(date.getTime()+(365*24*60*60*1000));
	document.cookie = "server=" + value + "; expires=" + date.toGMTString();
}