package scalaplayer.robots

import battlecode.common.RobotController


/**
  * @author Barry Becker
  */
class Archon(rc: RobotController) extends RobotBase(rc) {

  override def doBehavior(): Unit = {
    val dir = randomDirection
    // Randomly attempt to build a gardener in this direction
    if (rc.canHireGardener(dir) && Math.random < .01) rc.hireGardener(dir)
    // Move randomly
    tryMove(randomDirection)
    // Broadcast archon's location for other robots on the team to know
    val myLocation = rc.getLocation
    rc.broadcast(0, myLocation.x.toInt)
    rc.broadcast(1, myLocation.y.toInt)
  }

}
