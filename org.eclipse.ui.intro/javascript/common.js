
function toggleSection(id) {
	if (document.getElementById) {
   		var element = document.getElementById(id);
	   	element.style.display=(element.style.display=="block")?"none":"block";
   	}
   	return false;
}