package scalaplayer

import battlecode.common.RobotType._
import battlecode.common._

import scalaplayer.robots.{Archon, Gardener, Lumberjack, Soldier}


object RobotPlayer {
  private var rc: RobotController = _

  /**
    * run() is the method that is called when a robot is instantiated in the Battlecode world.
    * If this method returns, the robot dies!
    */
  @SuppressWarnings(Array("unused"))
  @throws[GameActionException]
  def run(rc: RobotController): Unit = {
    // This is the RobotController object. You use it to perform actions from this robot,
    // and to get information on its current status.
    RobotPlayer.rc = rc

    // Here, we've separated the controls into a different method for each RobotType.
    // You can add the missing ones or rewrite this into your own control structure.
    rc.getType match {
      case ARCHON => new Archon(rc).run()
      case GARDENER => new Gardener(rc).run()
      case SOLDIER => new Soldier(rc).run()
      case LUMBERJACK => new Lumberjack(rc).run()
      case _ => println("Unsupported type = " + rc.getType)
    }
  }

}
