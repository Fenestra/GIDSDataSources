var pageNum = 0;
var currentItem = "page";
var currentSelection = "";
var pageList = [];
var currentPage = null;
var currentGroup = null;
var availableWidth = 8.5;
var availableHeight = 11;

function itemSelected(itemname) {
  console.log("itemSelected got "+itemname);
  currentSelection = itemname;
};

function menuChange(tabname) {
  console.log("menuChange got "+tabname);
  currentItem = tabname;
};


function renderFunction(resp) {
//  console.log("renderFunction got "+resp);
  document.getElementById("svg").innerHTML = resp;
};

function showPage() {
  //PUT the pagelist to it
//console.log(JSON.stringify(pagelist));
//console.log("currentpage is "+JSON.stringify(currentPage));
   putJSON("/gidsapi/renderLayout", JSON.stringify([currentPage]), renderFunction);
};

function addItem() {
//    console.log("addItem now "+currentItem);
  switch (currentItem) {
    case "page": addPage(); break;
    case "group": addGroup(); break;
    case "text": addText(); break;
    default: console.log("no code for currentItem of "+currentItem);
  }
  buildPageList();
};

function addText() {
  width  = eval(document.getElementById("twidth").value);
  height = eval(document.getElementById("theight").value);
  text = document.getElementById("ttext").value;
  font = {
    "name": document.getElementById("tfontname").value,
    "size": document.getElementById("tfontsize").value,
    "bold": document.getElementById("tfontbold").checked,
    "italic": document.getElementById("tfontitalic").checked
  };
  resp = {
    "kind": "text",
    "description": "Text w:"+width+" h:"+height+" "+font.name+" "+font.size+" ("+text+")",
    "width": width,
    "height": height,
    "font": font,
    "data": text
  };
console.log(resp);
  currentGroup.children.push(resp);
//  buildPageList();
};

function addGroup() {
  size  = eval(document.getElementById("gsize").value);
  minsize = eval(document.getElementById("gmin").value);
  orientation = document.getElementById("gorient").value;
  if (orientation == "horizontal") {
    w = availableWidth;
    h = size;
    availableHeight = availableHeight - size;
  } else {
    w = size;
    h = availableHeight;
    availableWidth = availableWidth - size;
  }
  currentGroup = {
    "kind": "group",
    "orientation": orientation,
    "description": "Group "+orientation+" w:"+w+" h:"+h+" min:"+minsize,
    "width": w,
    "height": h,
    "min": minsize,
    "data": orientation,
    "children": []
  };
console.log(currentGroup);
  currentPage.children.push(currentGroup);
};

function addPage() {
  var pcolor = document.getElementById("pcolor").value;
  var ptype = document.getElementById("ptype").value;
  console.log("color is "+pcolor+" and pagetype is "+ptype);
//  var oldHTML = document.getElementById("pagelist").innerHTML;
  pageNum = pageNum + 1;
  availableWidth = 8.5;
  availableHeight = 11;
  var name = "page" + pageNum;
  currentPage = {
    "pagename": name,
    "pagenum": pageNum,
    "description": "Page " + pageNum + " " + ptype + " " + pcolor,
    "width": availableWidth,
    "height": availableHeight,
    "pagetype": ptype,
    "pagecolor": pcolor,
    "children": []
  };
  pageList.push(currentPage);
};

function buildGroupList(group) {
  console.log("buildPageList doing page "+page);
  someHTML = "<li><a data-toggle='pill' >" + group.description + "</a>";
  someHTML = someHTML + "<ul class='dropdown'>";
  for (i in group.children) {
    someHTML = someHTML +
     "<li><a data-toggle='pill'>"+group.children[i].description+"</a></li>";
  };
  someHTML = someHTML + "</ul></li>";
 //  console.log(someHTML);
 return someHTML;
};

function buildPageList() {
  someHTML = ""; //<li class='active'><a data-toggle='pill' onclick='itemSelected('page" +
    pageNum + "'+)'>" + pageNum + " " + ptype + " " + pcolor + "</a></li>";
  for (p in pageList) {
     page = pageList[p];
console.log("buildPageList doing page "+page);
     someHTML = someHTML +
       "<li><a data-toggle='pill' onclick='itemSelected('" +
         page.pagename + "'+)'>" + page.description + "</a>";
     someHTML = someHTML + "<ul class='dropdown'>";
     for (i in page.children) {
       someHTML = someHTML + buildGroupList(page.children[i]);
     };
     someHTML = someHTML + "</ul></li>";
   };
   console.log(someHTML);
  $('#pagelist').html(someHTML);
};

document.addEventListener("DOMContentLoaded", function() {
//  window.location.assign("#page");
});

