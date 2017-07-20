function showWaitPage(msg) {
//console.log("showWaitPage");
   $("#waitModal").modal();
//   document.body.style.cursor = "wait";
//   document.getElementById("waitModal").modal();
   document.getElementById("waitText").innerHTML = "<h3 style='text-align:center'>"+msg+"</h3>";
}

function closeWaitPage() {
   $("#waitModal").modal("hide");
}

function showJsonErrorPage(msg) {
   var res = JSON.parse(msg);
   showErrorPage(res.error.msg);
}

function showErrorPage(msg) {
//console.log("showErrorPage");
   closeWaitPage();
   $("#errorModal").modal({backdrop:true});
   document.getElementById("errorText").innerHTML = "<p>"+msg+"</p>";
}

function showModalWait(msg) {
  showWaitPage("wait a mo");
  showErrorPage("a problem occurred");
}

function loadQnr() {
  loadURL("/gidsapi/qnrs/get/"+document.getElementById("qnrID").value);
}

function loadQnrQuestions() {
  loadURL("/gidsapi/qnrs/questions/"+document.getElementById("qnrID").value);
}

function loadURL(url) {
  showWaitPage("Please wait for the server to respond...");
//  console.log("wait page shown");
  window.location.assign(url);
//  getFromHttp("GET", url, urlFunction);
//hiddenlink   hideme pushButton
//  document.getElementById("hiddenlink").href = url;
//  document.getElementById("hiddenlink").click();
}

function urlFunction(resp) {
  closeWaitPage();
  document.getElementById("resulttext").textContent = resp;
  console.log("resp processed");
}

function apiTest(url) {
  showWaitPage("Please wait for the server to respond...");
  getFromHttp("GET", url, urlFunction);
//hiddenlink   hideme pushButton
//  document.getElementById("hiddenlink").href = url;
//  document.getElementById("hiddenlink").click();
}

/*
function resetDBFunction(resp) {
  closeWaitPage();
  window.location.reload();
}

document.addEventListener("DOMContentLoaded", function() {
   token = document.getElementById("token").textContent;
   REFPERIOD = document.getElementById("ref-period").textContent;
   currentManifest = document.getElementById("manifest-id").textContent;
   if ( (window.location.href.indexOf("index") >= 0) || (window.location.href.indexOf("badUser") >= 0) ) {
     sessionStorage.setItem("undo", "");
     sessionStorage.setItem("redo", "");
     showUndoControls();
    }
   else
      initializeUndo();
});
*/