let IScroll = require("iscroll")
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function positionNav() {
  let currentShift = 0;
  let rs = null

  window.positionSectionNav = function () {
    let nss = document.getElementsByClassName("section-nav-buttons")
    if (nss.length > 0) {
      let ns = nss[0]
      let rect = ns.getBoundingClientRect()
      // because the arrows

      let shift = window.innerWidth - rect.right
      let newShift = shift + currentShift
      currentShift = newShift
      ns.style = "left:" + (newShift) + "px;"

      if (!rs) {
        rs = new ResizeSensor(ns, positionSectionNav)
      }

    }
  }

  window.onscroll = positionSectionNav
}

function fitToEdge() {
  window.fitLayoutContainerToEdge = function () {
    let cs = document.getElementsByClassName("top-space-layout-content")
    if (cs.length > 0) {
      let c = cs[0]
      let w = window.innerWidth - c.getBoundingClientRect().left
      c.style = "width:" + w + "px";

      // fitScrollWrapper()

    }
  }

  // function fitScrollWrapper() {
  //   let sw = document.getElementsByClassName("scroll-wrapper")[0]
  //   let h = window.innerHeight - sw.getBoundingClientRect().top
  //   sw.style = "height:" + h + "px";
  //   console.log("fitting scroll wrapper", sw.getBoundingClientRect().top, window.innerHeight)
  // }
}

function addScroll() {
  console.log("add scroll")

  let rs = null;
  let tracking = null;
  let scroller = null;

  window.addScrollToSectionContainer = function () {
    console.log("+++")

    let ss = document.getElementsByClassName("scroll-wrapper")
    if (ss.length > 0) {
      let s = ss[0]
      if (!tracking) {
        attachSensors(s)
      } else if (tracking !== s) {
        detachSensors(s)
      }

    }
  }

  function detachSensors() {
    rs.detach()
    scroller.destroy()
  }

  function attachSensors(s) {
    console.log("attaching to", s)
    tracking = s
    scroller = new IScroll(s, {
      mouseWheel: true,
      scrollbars: true,
      // freeScroll: true,
      scrollX: true,
      snap: ".section "
    })
    rs = new ResizeSensor(s, modifyScroller)
    console.log("scroller", scroller)
  }

  function modifyScroller() {
    console.log("refreshing scroller", scroller)
    scroller.refresh()
  }
}

window.onresize = function () {
  positionSectionNav();
  fitLayoutContainerToEdge();
}


fitToEdge()
addScroll()
positionNav()

// document.onload = () => {
//   console.log("DOC LOAD")
//   document.getElementsByTagName("body")[0].onscroll = positionSectionNav
// }