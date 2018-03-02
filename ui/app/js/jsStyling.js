let IScroll = require("iscroll");
let ResizeSensor = require('css-element-queries/src/ResizeSensor');

function addScroll() {
  console.log("add scroll");

  let rs = null;
  let tracking = null;
  let scroller = null;
  let btnLeft = null;
  let btnRight = null;
  let atSection = 0;
  let allSections = [];

  // going to assume that this will only be called when dom elements are removed
  window.addScrollToSectionContainer = function () {
    console.log("+++");

    let ss = document.getElementsByClassName("scroll-wrapper");
    window.ss = ss
    let btns = document.getElementsByClassName("section-nav-buttons");
    if (ss.length > 0) {
      let s = ss[0];
      btns = btns[0].getElementsByClassName("btn");
      allSections = s.getElementsByClassName("section");
      console.log("sections", allSections, btns);

      detachSensors(s)

      btnLeft = btns[0];
      btnRight = btns[1];

      attachSensors(s)
      setNavButtonStatus()
    }
  };

  function filterSections() {
    let sections = []
    for (let i = 0; i < allSections.length; i++) {
      let s = allSections[i]
      if (!s.classList.contains("d-none")) {
        sections.push(s)
      }
    }
    return sections
  }

  function setNavButtonStatus(sections) {
    if (!sections) {
      sections = filterSections()
    }

    let rightEdgeOffScreen = true
    // let leftEdgeOffScreen = true
    if (sections.length === 0) {
      rightEdgeOffScreen = false
      // let leftEdgeOffScreen = false
    } else {
      let rightMost = sections[sections.length - 1]
      let padding = window.getComputedStyle(rightMost, null).getPropertyValue('padding-right').replace("px", "");
      padding = parseFloat(padding)
      padding = 0
      console.log("padding", padding)
      rightEdgeOffScreen = rightMost.getBoundingClientRect().right + padding - window.innerWidth > 0
      console.log("reos", sections[sections.length - 1], sections[sections.length - 1].getBoundingClientRect().right,
        window.innerWidth, rightEdgeOffScreen)
    }

    // let leftEdgeOffScreen = sections[atSection].getBoundingClientRect().left - window.innerWidth > 0
    // console.log("leos", sections[atSection].getBoundingClientRect().left, leftEdgeOffScreen)

    let rightEnd = atSection >= sections.length - 1 && !rightEdgeOffScreen
    let leftEnd = atSection === 0
    if (rightEnd) {
      btnRight.classList.add("disabled")
    }
    if (leftEnd) {
      btnLeft.classList.add("disabled")
    }
    if (!leftEnd) {
      btnLeft.classList.remove("disabled")
    }
    if (!rightEnd) {
      btnRight.classList.remove("disabled")
    }
    console.log("btn status", leftEnd, rightEnd, sections, atSection)
  }

  function nav(right) {
    console.log("nav")
    let sections = filterSections()

    let nextSection = atSection;
    if (right) {
      rightEdgeOffScreen = sections[sections.length - 1].getBoundingClientRect().right - window.innerWidth > 0
      if (nextSection < sections.length - 1 && rightEdgeOffScreen) {
        nextSection++
      } else {
        return
      }
    } else {
      if (nextSection > 0) {
        nextSection--
      } else {
        return
      }
    }
    atSection = nextSection

    let s = sections[nextSection];
    console.log("scrolling to", s, nextSection);
    console.log("scrolling to", sections, sections.length);
    scroller.scrollToElement(s, 0, true, true)
    setNavButtonStatus(sections)
  }

  function detachSensors() {
    console.log("detaching sensors", rs, btnLeft, scroller)
    if (rs) {
      rs.detach();
    }
    // scroller.destroy()
  }

  function attachSensors(s) {
    console.log("attaching sensors to", s);

    btnLeft.addEventListener("click", function () {nav(false)});
    btnRight.addEventListener("click", function () {nav(true)})
    tracking = s;
    scroller = new IScroll(s, {
      mouseWheel: true,
      scrollbars: true,
      freeScroll: true,
      scrollX: true,
    });
    scroller.on("scrollEnd", function () {
      console.log("finished scrolling")
      setNavButtonStatus()
    })
    rs = new ResizeSensor(s, modifyScroller);
    console.log("scroller", scroller)
  }

  function modifyScroller() {
    console.log("refreshing scroller", scroller);
    setNavButtonStatus();
    scroller.refresh()
  }
}

addScroll();
