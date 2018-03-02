// let IScroll = require("iscroll");
let IScroll = require("iscroll/build/iscroll-zoom");
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function addScroll() {
  let rs = null;
  let scroller = null;
  let scrollWrapper  = null;

  // going to assume that this will only be called when dom elements are removed
  window.addScrollToSectionContainer = function () {
    let sws = document.getElementsByClassName("scroll-wrapper");
    if (sws.length > 0) {
      scrollWrapper = sws[0];
      detachSensors(scrollWrapper)
      attachSensors(scrollWrapper)
    }
  };

  let lastHeight = 0;
  function detachSensors() {
    lastHeight = 0;
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
      scrollY: false,
      eventPassthrough: true,
      // zoom: false,
      // wheelAction: 'zoom',
      // zoomMin: 0.5,
      // zoomMax: 2,
      // startZoom: 0.7
    });
    rs = new ResizeSensor(s, modifyScroller);
  }

  function modifyScroller() {
    console.log("scroller refresh", scroller)
    // console.log("refresh sensor")
    scroller.refresh();
    if (scrollWrapper) {
      let content = scrollWrapper.getElementsByClassName("top-space-child-display")[0]
      let contentHeight = window.getComputedStyle(content)["height"]
      if (lastHeight != contentHeight) {
        scrollWrapper.style.height = contentHeight
        console.log("scroller height set", contentHeight, lastHeight)
        lastHeight = contentHeight
      }
    }
  }
}

addScroll();
