function AddClipboardDecoration() {
  'use strict'

  // https://gitlab.com/antora/antora-ui-default/-/blob/master/src/js/06-copy-to-clipboard.js
  // https://gitlab.com/antora/antora-ui-default/-/blob/master/LICENSE
  
  var CMD_RX = /^\$ (\S[^\\\n]*(\\\n(?!\$ )[^\\\n]*)*)(?=\n|$)/gm
  var LINE_CONTINUATION_RX = /( ) *\\\n *|\\\n( ?) */g
  var TRAILING_SPACE_RX = / +$/gm

  var config = (document.getElementById('site-script') || { dataset: {} }).dataset
  var supportsCopy = window.navigator.clipboard
  var svgAs = config.svgAs
  var uiRootPath = (config.uiRootPath == null ? window.uiRootPath : config.uiRootPath) || '.'

  var items = document.querySelectorAll('.requirement .content .title')
  ;[].slice.call(items).forEach(function (pre) {
    var code, language, lang, copy, toast, toolbox

    ;(toolbox = document.createElement('div')).className = 'source-toolbox'
    if (lang) toolbox.appendChild(lang)
    if (supportsCopy) {
      ;(copy = document.createElement('button')).className = 'copy-button'
      copy.setAttribute('title', 'Copy to clipboard')
      if (svgAs === 'svg') {
        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
        svg.setAttribute('class', 'copy-icon')
        var use = document.createElementNS('http://www.w3.org/2000/svg', 'use')
        use.setAttribute('href', uiRootPath + '/images/octicons-16.svg#icon-clippy')
        svg.appendChild(use)
        copy.appendChild(svg)
      } else {
        var img = document.createElement('img')
        img.src = uiRootPath + '/images/octicons-16.svg#view-clippy'
        img.alt = 'copy icon'
        img.className = 'copy-icon'
        copy.appendChild(img)
      }
      ;(toast = document.createElement('span')).className = 'copy-toast'
      toast.appendChild(document.createTextNode('Copied!'))
      copy.appendChild(toast)
      toolbox.appendChild(copy)
    }
    pre.parentNode.appendChild(toolbox)
    if (copy) {
      var strCurrentPage = window.location.href.split('#')[0]
      var strId = pre.parentNode.parentNode.id
      var strText = `${strCurrentPage}#${strId}`
      copy.addEventListener('click', writeToClipboard.bind(copy, strText))
    }
  })

  function extractCommands (text) {
    var cmds = []
    var m
    while ((m = CMD_RX.exec(text))) cmds.push(m[1].replace(LINE_CONTINUATION_RX, '$1$2'))
    return cmds.join(' && ')
  }

  function writeToClipboard (strText) {
    window.navigator.clipboard.writeText(strText).then(
      function () {
        this.classList.add('clicked')
        this.offsetHeight // eslint-disable-line no-unused-expressions
        this.classList.remove('clicked')
      }.bind(this),
      function () {}
    )
  }
}
