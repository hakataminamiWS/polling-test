package controllers

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import javax.inject._
import play.api._
import play.api.libs.ws.WSClient
import play.api.mvc._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (
    val controllerComponents: ControllerComponents,
    ws: WSClient
)(implicit
    system: ActorSystem,
    ec: ExecutionContext
) extends BaseController
    with Logging {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def access = Action { implicit request: Request[AnyContent] =>
    logger.debug("access")
    Ok("access")
  }

  def polling = Action { implicit request: Request[AnyContent] =>
    val stream: Future[Option[Boolean]] =
      Source(1 to 5)
        .throttle(1, 1.seconds)
        .mapAsync(parallelism = 1)(_ => doSomething)
        .takeWhile(_ == false, true)
        .runWith(Sink.lastOption)

    Ok(views.html.index())
  }

  def doSomething: Future[Boolean] = {
    val request = ws.url("http://localhost:9000/access")
    request.get().map(response => response.status == 400)
  }

}
