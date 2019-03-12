package gamedom

import com.raquo.airstream.eventbus.EventBus
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.{ReactiveElement, ReactiveHtmlElement}
import org.scalajs.dom.html
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.CanvasRenderingContext2D

/**
  * Creates the canvas in which the game is drawn.
  */
final class GameCanvas extends Component[html.Canvas] {

  private implicit class VarWithAssignOp[T](v: Var[T]) {
    def :=(t: T): Unit = v.set(t)
  }

  /**
    * Events pushed to that stream contain pairs of keys and booleans, where
    * - the key is the key involved in the dom key event
    * - the boolean states whether the key is pressed or released
    */
  private val pressedKeysBus = new EventBus[(String, Boolean)]()
  def $pressedKeysInfo: EventStream[(String, Boolean)] = pressedKeysBus.events

  val mousePosition: Var[(Double, Double)] = Var((0, 0))

  /** The `mousePosition` is updated in the `onMouseMove` of the canvas. */
  private val mouseMoveObserver = Observer[(Double, Double)](pair => mousePosition := pair)

  val gameCanvas: ReactiveHtmlElement[html.Canvas] = canvas(
    tabIndex := 1,
    inContext(canvas =>
      onMouseMove.map(ev => (
        ev.clientX - canvas.ref.getBoundingClientRect().left,
        ev.clientY - canvas.ref.getBoundingClientRect().top)
      ) --> mouseMoveObserver
    ),
    onKeyDown.map(ev => (ev.key, true)) --> pressedKeysBus,  // adding to pressed keys
    onKeyUp.map(ev => (ev.key, false)) --> pressedKeysBus    // removing from pressed keys
  )

  /**
    * We can't use the width := 800 modifier as html.Canvas is kinda glitchy in that regard.
    */
  gameCanvas.ref.width = 800
  gameCanvas.ref.height = 600
  /** Immediately gives the focus otherwise it will not respond to key strokes. */
  gameCanvas.ref.focus()

  val ctx: CanvasRenderingContext2D = gameCanvas.ref.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  val width: Int = gameCanvas.ref.width
  val height: Int = gameCanvas.ref.height

  def clear(): Unit = {
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, width, height)
  }

  val baseElement: ReactiveElement[Canvas] = gameCanvas

}
