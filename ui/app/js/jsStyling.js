let iscroll = require("iscroll")
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function positionNav() {
  console.log("positioning nav")

  let currentShift = 0;
  let rs = null

  window.positionSectionNav = function () {
    console.log("***")
    let nss = document.getElementsByClassName("section-nav-buttons")
    if (nss.length > 0) {
      let ns = nss[0]
      let rect = ns.getBoundingClientRect()
      // because the arrows

      let shift = window.innerWidth - rect.right

      console.log("BS", window.innerWidth, shift, rect.right)

      let newShift = shift + currentShift
      currentShift = newShift
      ns.style = "left:" + (newShift) + "px;"

      console.log("BS", window.innerWidth, shift, rect.right)

      if (!rs) {
        rs = new ResizeSensor(ns, positionSectionNav)
      }

    }
  }

  window.onresize = positionSectionNav
  window.onscroll = positionSectionNav
}

positionNav()