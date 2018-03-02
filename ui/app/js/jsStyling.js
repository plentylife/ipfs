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

  window.addScrollToSectionContainer = function () {
    console.log("+++");

    let ss = document.getElementsByClassName("scroll-wrapper");
    let btns = document.getElementsByClassName("section-nav-buttons");
    if (ss.length > 0) {
      let s = ss[0];
      btns = btns[0].getElementsByClassName("btn");
      allSections = s.getElementsByClassName("section");
      console.log("sections", allSections, btns);

      if (!tracking) {
        attachSensors(s)
      } else if (tracking !== s) {
        detachSensors(s)
      }

      if (!btnLeft) {
        btnLeft = btns[0];
        btnRight = btns[1];
        btnLeft.addEventListener("click", function () {nav(false)});
        btnRight.addEventListener("click", function () {nav(true)})
      }
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
    let rightEnd = atSection === sections.length - 1
    let leftEnd = atSection === 0
    if (rightEnd) {
      btnRight.classList.add("disabled")
      btnLeft.classList.remove("disabled")
    }
    if (leftEnd) {
      btnLeft.classList.add("disabled")
      btnRight.classList.remove("disabled")
    }
    if (!(leftEnd || rightEnd)) {
      btnLeft.classList.remove("disabled")
      btnRight.classList.remove("disabled")
    }
  }

  function nav(right) {
    let sections = filterSections()

    let nextSection = atSection;
    if (right) {
      if (nextSection < sections.length - 1) {
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
    setNavButtonStatus(sections)

    let s = sections[nextSection];
    console.log("scrolling to", s, nextSection);
    console.log("scrolling to", sections, sections.length);
    scroller.scrollToElement(s, 0, true, true)
  }

  function detachSensors() {
    rs.detach();
    scroller.destroy()
  }

  function attachSensors(s) {
    console.log("attaching to", s);
    tracking = s;
    scroller = new IScroll(s, {
      mouseWheel: true,
      scrollbars: true,
      freeScroll: true,
      scrollX: true,
    });
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
