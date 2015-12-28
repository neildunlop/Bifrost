package com.datinko.asgard.bifrost.actors

import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{RequestStrategy, OneByOneRequestStrategy, ActorSubscriber}
import com.typesafe.scalalogging.LazyLogging
import _root_.io.scalac.amqp.{Message}
import kamon.Kamon

/**
 * Created by neild on 28/12/2015.
 */
class SlowDownActor(name: String, delayPerMsg: Long, initialDelay: Long) extends ActorSubscriber with LazyLogging {
  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  // setup actorname to provided name for better tracing of stats
  val actorName = name

  val consumeCounter = Kamon.metrics.counter("slowdownactor-consumed-counter")

  // default delay is 0
  var delay = 0l

  def this(name: String) {
    this(name, 0, 0)
  }

  def this(name: String, delayPerMsg: Long) {
    this(name, delayPerMsg, 0)
  }

  override def receive: Receive = {

    case OnNext(msg: Message) =>
      delay += delayPerMsg
      Thread.sleep(initialDelay + (delay / 1000), delay % 1000 toInt)
      logger.debug(s"Message in slowdown actor sink ${self.path} '$actorName': $msg")
      consumeCounter.increment(1)
    case _ =>
      logger.debug(s"Unknown message in $actorName: ")

  }
}
