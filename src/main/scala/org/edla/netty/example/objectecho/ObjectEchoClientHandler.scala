package org.edla.netty.example.objectecho

import java.util.ArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

import org.jboss.netty.channel.ChannelEvent
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelState
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.channel.ExceptionEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.channel.SimpleChannelUpstreamHandler

/**
 * Handler implementation for the object echo client.  It initiates the
 * ping-pong traffic between the object echo client and server by sending the
 * first message to the server.
 */
class ObjectEchoClientHandler(firstMessageSize: Int) extends SimpleChannelUpstreamHandler {

  require(firstMessageSize > 0)

  private val logger = Logger.getLogger(getClass.getName)

  private val transferredMessages = new AtomicLong

  private val firstMessage = new ArrayList[java.lang.Integer](firstMessageSize)
  val range = 0.until(firstMessageSize)
  for (i <- range) { firstMessage.add(i) }

  def getTransferredBytes: Long = transferredMessages.get

  override def handleUpstream(ctx: ChannelHandlerContext, e: ChannelEvent) {
    e match {
      case c: ChannelStateEvent => if (c.getState != ChannelState.INTEREST_OPS) logger.info(e.toString)
      case _ => None
    }
    super.handleUpstream(ctx, e)
  }

  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    // Send the first message if this handler is a client-side handler.
    e.getChannel.write(firstMessage)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    // Echo back the received object to the client.
    transferredMessages.incrementAndGet
    e.getChannel.write(e.getMessage)
  }

  override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
    // Close the connection when an exception is raised.
    logger.warning("Unexpected exception from downstream." + e.getCause)
    e.getChannel.close()
  }
}
