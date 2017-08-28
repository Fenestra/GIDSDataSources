
var docs = [ {id: "someID", docType: "Letter", docNumber: "MA-10000L", description: "A dummy document"} ];
var lastDocID = "";

function docSelected(aTr) {
    nextSelected = aTr.rowIndex;
    lastDocID = docs[nextSelected].id
    console.log("Row index is: " + aTr.rowIndex + " "+ lastDocID );
    $(aTr).parent().children().removeClass("info");
    $(aTr).addClass("info");
}
/*
function renderDocFunction(resp) {
  closeWaitPage();
  console.log("render processed "+resp);
  window.location.assign(resp);
}
*/
function renderDoc() {
  console.log("renderDoc called for "+lastDocID);
  showWaitPage("Please wait for the server to respond...");
//  getFromHttp("GET", "/gidsapi/docs/render/"+lastDocID, renderDocFunction);
  window.location.assign("/gidsapi/showDoc/"+lastDocID);
}


function docsByRefDivFunction(resp) {
  closeWaitPage();
  console.log("resp processed");
  var tblheader = "<tr> <th class='col-xs-4'>ID</th> <th class='col-xs-2'>Doc Type</th> <th class='col-xs-2'>Doc Number</th> <th class='col-xs-4'>Description</th> </tr>";
  someHTML = "<tr onclick='docSelected(this)' class='info'>";
  docs = JSON.parse(resp);
  for (i in docs) {
     if (i > 0)
       someHTML = someHTML + "<tr onclick='docSelected(this)'>";
     someHTML = someHTML +
        "<td class='col-xs-4'>" + docs[i].id + "</td>"+
        "<td class='col-xs-2'>" + docs[i].docType + "</td>"+
        "<td class='col-xs-2'>" + docs[i].docNumber + "</td>"+
        "<td class='col-xs-4'>" + docs[i].description + "</td>"+
        "</tr>";
  }
   $('#tablebody').html(someHTML);
   $('#tableheader').html(tblheader);
   lastDocID = docs[0].id;
}

function refSelected(ref) {
  showWaitPage("Please wait for the server to respond...");
  getFromHttp("GET", "/gidsapi/docs/"+ref, docsByRefDivFunction);
  console.log("selected "+ref);
}


document.addEventListener("DOMContentLoaded", function() {
   var e = document.getElementById("refdiv");
   refSelected(e.options[e.selectedIndex].value);
});
