function outputUpdate(b,a){document.querySelector("#"+b).value=a}function ticks(h,a){var c=document.querySelector("#"+h);if(c.hasOwnProperty("list")&&c.hasOwnProperty("min")&&c.hasOwnProperty("max")&&c.hasOwnProperty("step")){var f=document.createElement("datalist");var e=parseInt(c.getAttribute("min"));var d=a;var g=parseInt(c.getAttribute("max"));f.id=c.getAttribute("list");for(var b=e;b<g+d;b=b+d){f.innerHTML+="<option value="+b+"></option>"}c.parentNode.insertBefore(f,c.nextSibling)}};