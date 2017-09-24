package scalaplayer.robots

import battlecode.common.{MapLocation, RobotController, RobotType}


/**
  * @author Barry Becker
  */
class Gardener(rc: RobotController) extends RobotBase(rc) {

  override def doBehavior(): Unit = {
    val xPos = rc.readBroadcast(0)
    val yPos = rc.readBroadcast(1)
    val archonLoc = new MapLocation(xPos, yPos)
    val dir = randomDirection
    // Randomly attempt to build a soldier or lumberjack in this direction
    if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random < .01) rc.buildRobot(RobotType.SOLDIER, dir)
    else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random < .01 && rc.isBuildReady)
      rc.buildRobot(RobotType.LUMBERJACK, dir)
    tryMove(randomDirection)
  }

}
