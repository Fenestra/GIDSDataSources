
var docs = [ {id: "someID", docType: "Letter", docNumber: "MA-10000L", description: "A dummy document"} ];
var lastDocID = "";

function docSelected(aTr) {
    nextSelected = aTr.rowIndex - 1;
    lastDocID = docs[nextSelected].id
    console.log("Row index is: " + aTr.rowIndex + " "+ lastDocID );
 //  $('#tablebody').html(someHTML);
    $(aTr).parent().children().removeClass("info");
    $(aTr).addClass("info");
}

function renderDocFunction(resp) {
  closeWaitPage();
  console.log("render processed "+resp);
  window.location.assign(resp);
}

function renderDoc() {
  console.log("renderDoc called for "+lastDocID)
  showWaitPage("Please wait for the server to respond...");
  getFromHttp("GET", "/gidsapi/docs/render/"+lastDocID, renderDocFunction);
}


function docsByRefDivFunction(resp) {
  closeWaitPage();
  console.log("resp processed");
  someHTML = "<tr onclick='docSelected(this)' class='info'>";
  docs = JSON.parse(resp);
  for (i in docs) {
     if (i > 0)
       someHTML = someHTML + "<tr onclick='docSelected(this)'>";
     someHTML = someHTML +
        "<td>" + docs[i].id + "</td>"+
        "<td>" + docs[i].docType + "</td>"+
        "<td>" + docs[i].docNumber + "</td>"+
        "<td>" + docs[i].description + "</td>"+
        "</tr>";
  }
   $('#tablebody').html(someHTML);
   lastDocID = docs[0].id
}

function refSelected(ref) {
  document.getElementById("doctext").textContent = ref+" was selected";
  showWaitPage("Please wait for the server to respond...");
  getFromHttp("GET", "/api/docs/"+ref, docsByRefDivFunction);
  console.log("selected "+ref);
}


document.addEventListener("DOMContentLoaded", function() {
   var e = document.getElementById("refdiv");
   refSelected(e.options[e.selectedIndex].value);
});
