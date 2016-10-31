//-------------------- HTTP calls ----------------
// usage example: getFromHttp("GET", "man/list", manifestFunction)
//   then you assume manifestFunction takes over everything when response comes back

function getFromHttp(verb, url, respFunction) {
   console.log("called getFromHttp "+verb+" "+url);
   var xmlhttp = new XMLHttpRequest();
   xmlhttp.onreadystatechange=function() {
      if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
          if (xmlhttp.responseText.indexOf("error\":") >= 0)
             showJsonErrorPage(xmlhttp.responseText);
          respFunction(xmlhttp.responseText);
      }
   }
   xmlhttp.open(verb, url, true);
   xmlhttp.send();
}

//----------------------------

function postJSON(aURL, strData, postFunction) {
   console.log("in postJSON for "+aURL);
   $.ajax({
     type: "POST",
     url: aURL,
     contentType: 'application/json',
     data: strData ,
     always: postFunction,
     success: postFunction
     });
};

function putJSON(aURL, strData, putFunction) {
   console.log("in putJSON for "+aURL+" sending "+strData);
   $.ajax({
     type: "PUT",
     url: aURL,
     contentType: 'application/json',
     data: strData ,
     always: putFunction,
     success: putFunction
     });
};

function deleteJSON(aURL, strData, delFunction) {
   console.log("in deleteJSON for "+aURL+" sending "+strData);
   $.ajax({
     type: "DELETE",
     url: aURL,
     contentType: 'application/json',
     data: strData ,
     always: delFunction,
     success: delFunction
     });
};

function syncDeleteJSON(aURL, strData) {
   console.log("in syncDeleteJSON for "+aURL+" sending "+strData);
   var call = $.ajax({
     type: "DELETE",
     async: false,
     url: aURL,
     contentType: 'application/json',
     data: strData
     });
   var resp = call.responseText;
   if (resp.indexOf("error\":") >= 0)
      showJsonErrorPage(resp);
   return resp;
};

function syncPutJSON(aURL, strData) {
   console.log("in syncPutJSON for "+aURL+" sending "+strData);
   var call = $.ajax({
     type: "PUT",
     async: false,
     url: aURL,
     contentType: 'application/json',
     data: strData
     });
   var resp = call.responseText;
   if (resp.indexOf("error\":") >= 0)
       showJsonErrorPage(resp);
   return resp;
};
