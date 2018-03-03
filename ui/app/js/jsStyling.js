let IScroll = require("iscroll");
// let IScroll = require("iscroll/build/iscroll-zoom");
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function addScroll() {
  let rs = null;
  let scroller = null;
  let scrollWrapper  = null;
  let content = null;

  // going to assume that this will only be called when dom elements are removed
  window.addScrollToSectionContainer = function () {
    let sws = document.getElementsByClassName("scroll-wrapper");
    if (sws.length > 0) {
      scrollWrapper = sws[0];
      content = scrollWrapper.getElementsByClassName("top-space-child-display")[0]

      detachSensors(scrollWrapper)
      attachSensors(scrollWrapper)
    }
  };

  let lastHeight = 0;
  let scrolledOnce = false
  function detachSensors() {
    lastHeight = 0;
    scrolledOnce = false;
    if (rs) {
      rs.detach();
    }
    // scroller.destroy()
  }

  function attachSensors(s) {
    scroller = new IScroll(s, {
      mouseWheel: true,
      scrollbars: true,
      freeScroll: true,
      scrollX: true,
      scrollY: true,
      // eventPassthrough: true,
      interactiveScrollbars: true,
      // zoom: false,
      // wheelAction: 'zoom',
      // zoomMin: 0.5,
      // zoomMax: 2,
      // startZoom: 0.7
    });
    // scroller.on("beforeScrollStart", preAdjust)
    scroller.on("scrollStart", fullScreen)
    scroller.on("scrollEnd", smallScreen)

    console.log("resize on", content)
    rs = new ResizeSensor(content, modifyScroller);
  }


  // let startingHeight = 0
  // let endingHeight = 0
  let startingMaxX = 0
  let endingMaxX = 0
  let startingMaxY = 0
  let endingMaxY = 0

  function preAdjust(sx, sy, ex, ey) {
    return function () {
      if (scrolledOnce) {
        console.log("----")
        console.log("reposition scroll from ", scroller.x, scroller.y)
        console.log("by", ex / sx, ey / sy)
        console.log("to", scroller.x * ex / sx, scroller.y * ey / sy)

        scroller.scrollTo(scroller.x * ex / sx, scroller.y * ey / sy)
      }
    }
  }

  function fullScreen() {
    let pa = preAdjust(startingMaxX, startingMaxY, endingMaxX, endingMaxY)

    scrollWrapper.classList.add("scrolling")

    startingMaxX = scroller.maxScrollX
    startingMaxY = scroller.maxScrollY

    scroller.refresh()
    pa()
    scrolledOnce = true


    endingMaxX = scroller.maxScrollX
    endingMaxY = scroller.maxScrollY

    console.log("scrolling", startingMaxX, endingMaxX)
    console.log("scrolling", startingMaxY, endingMaxY)
  }

  function smallScreen() {
    setTimeout(function () {
      scrollWrapper.classList.remove("scrolling")
      scroller.refresh()
      console.log("end scrolling", scroller.x, scroller.y)
      scroller.scrollTo(scroller.x * startingMaxX / endingMaxX, scroller.y * startingMaxY / endingMaxY)
      console.log("end scrolling adjusted", scroller.x, scroller.y)
    }, 500)
  }

  function modifyScroller() {
    console.log("scroller refresh", scroller)
    // console.log("refresh sensor")
    scroller.refresh();
    // if (scrollWrapper) {
    //   let content = scrollWrapper.getElementsByClassName("top-space-child-display")[0]
    //   let contentHeight = window.getComputedStyle(content)["height"]
    //   if (lastHeight != contentHeight) {
    //     scrollWrapper.style.height = contentHeight
    //     console.log("scroller height set", contentHeight, lastHeight)
    //     lastHeight = contentHeight
    //   }
    // }
  }
}

addScroll();
