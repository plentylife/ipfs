// let IScroll = require("iscroll");
let IScroll = require("iscroll/build/iscroll-zoom");
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function addScroll() {
  let rs = null;
  let scroller = null;

  // going to assume that this will only be called when dom elements are removed
  window.addScrollToSectionContainer = function () {
    let ss = document.getElementsByClassName("scroll-wrapper");
    if (ss.length > 0) {
      let s = ss[0];
      detachSensors(s)
      attachSensors(s)
    }
  };

  function detachSensors() {
    if (rs) {
      rs.detach();
    }
    // scroller.destroy()
  }

  function attachSensors(s) {
    scroller = new IScroll(s, {
      mouseWheel: true,
      scrollbars: true,
      // freeScroll: true,
      scrollX: true,
      scrollY: true,
      zoom: true,
      wheelAction: 'zoom',
      zoomMin: 0.5,
      zoomMax: 2,
      startZoom: 0.7
    });
    rs = new ResizeSensor(s, modifyScroller);
  }

  function modifyScroller() {
    // console.log("refresh sensor")
    scroller.refresh();
    // setTimeout(function () {
    // }, 1000);    // scroller.refresh()
  }
}

addScroll();
