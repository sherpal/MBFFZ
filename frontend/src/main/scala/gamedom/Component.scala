package gamedom

import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.api.L.{render => laminarRender}
import org.scalajs.dom

/**
  * Some kind of component-ish stuff.
  * Not yet convinced by this.
  */
trait Component[+Ref <: dom.Element] {

  val baseElement: ReactiveElement[Ref]

  def render(container: dom.Element): Unit = {
    laminarRender(container, baseElement)
  }

}
