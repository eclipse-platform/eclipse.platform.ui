/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

var highlighted=false;
onload=highlight;
document.onreadystatechange=highlight;
function highlight(){
	if(highlighted){
		return;
	}
	highlighted=true;
	if (!document.body) return;
	for(i=0; i<keywords.length; i++){
		word=keywords[i].toLowerCase();
		highlightWordInNode(word, document.body);
	}
}
function highlightWordInNode(aWord, aNode){
    if (aNode.nodeType == 1){
    	var children = aNode.childNodes;
    	for(var i=0; i < children.length; i++) {
    		highlightWordInNode(aWord, children[i]);
    	}
    }
    else if(aNode.nodeType==3){
    	highlightWordInText(aWord, aNode);
	}

}
function highlightWordInText(aWord, textNode){
	allText=new String(textNode.data);
	allTextLowerCase=allText.toLowerCase();
	index=allTextLowerCase.indexOf(aWord);
	if(index>0){
		while(index>0){
			before=allText.substring(0,index);
			newBefore=document.createTextNode(before);
			textNode.parentNode.insertBefore(newBefore, textNode);
			spanNode=document.createElement("span");
			spanNode.style.background="ButtonFace";
			spanNode.style.color="ButtonText";
			textNode.parentNode.insertBefore(spanNode, textNode);
			boldText=document.createTextNode(allText.substring(index,index+aWord.length));
			spanNode.appendChild(boldText);
			allText=allText.substring(index+aWord.length);
			allTextLowerCase=allText.toLowerCase();
			index=allTextLowerCase.indexOf(aWord);
		}
		newAfter=document.createTextNode(allText);
		textNode.parentNode.insertBefore(newAfter, textNode);
		textNode.parentNode.removeChild(textNode);
	}
}
