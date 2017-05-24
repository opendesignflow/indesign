package org.odfi.indesign.core.module.ui.www.external

import org.markdown4j.Markdown4jProcessor

trait MarkdownBuilder extends ExternalBuilder {
  
  var markdownProcessor = new Markdown4jProcessor
  
  def markdown(str:String) = {
    var pnode = p {
      textContent(markdownProcessor.process(str.stripMargin))
    }
  }
  
}