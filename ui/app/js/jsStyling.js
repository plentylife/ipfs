// let IScroll = require("iscroll");
let IScroll = require("iscroll/build/iscroll-zoom");
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function addScroll() {
  let rs = null;
  let scroller = null;
  let scrollWrapper  = null;

  // going to assume that this will only be called when dom elements are removed
  window.addScrollToSectionContainer = function () {
    //console.log("**** adding scroller")
    let sws = document.getElementsByClassName("scroll-wrapper");
    if (sws.length > 0) {
      scrollWrapper = sws[0];
      detachSensors(scrollWrapper)
      attachSensors(scrollWrapper)
      modifyScroller()
    }
  };

  let lastHeight = "0px";
  function detachSensors() {
    lastHeight = "0px";
    if (rs) {
      rs.detach();
    }
  }

  function attachSensors(s) {
    scroller = new IScroll(s, {
      mouseWheel: false,
      scrollbars: true,
      scrollX: true,
      scrollY: false,
      eventPassthrough: true,
      interactiveScrollbars: true
    });
    scroller.on("scrollStart", scrollStart)
    scroller.on("scrollEnd", scrollEnd)
    let container = document.getElementsByClassName("top-space-child-display")[0];
    rs = new ResizeSensor(container, modifyScroller);
  }

  function scrollStart(e) {
    scrollWrapper.classList.add("scrolling", e)
    //console.log("scroll stat")
  }

  function scrollEnd() {
    scrollWrapper.classList.remove("scrolling")
    //console.log("scroll end")
  }

  function modifyScroller() {
    if (scrollWrapper) {
      let content = scrollWrapper.getElementsByClassName("top-space-child-display")[0]
      let contentHeight = window.getComputedStyle(content)["height"]
      if (lastHeight != contentHeight) {
        //console.log(scrollWrapper.style)
        scrollWrapper.style.height = contentHeight
        //console.log("scroller height set", contentHeight, lastHeight)
        lastHeight = contentHeight
      }
    }
    scroller.refresh();
    //console.log("scroller refresh", scroller)
  }
}

addScroll();
