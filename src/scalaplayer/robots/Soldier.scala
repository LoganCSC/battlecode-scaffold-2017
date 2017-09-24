package scalaplayer.robots

import battlecode.common.{RobotController, Team}


/**
  * @author Barry Becker
  */
class Soldier(rc: RobotController) extends RobotBase(rc) {

  override def doBehavior(): Unit = {
    // See if there are any nearby enemy robots
    val robots = rc.senseNearbyRobots(-1, enemy)

    // If there are some...
    if (robots.nonEmpty) { // And we have enough bullets, and haven't attacked yet this turn...
      if (rc.canFireSingleShot) { // ...Then fire a bullet in the direction of the enemy.
        rc.fireSingleShot(rc.getLocation.directionTo(robots(0).location))
      }
    }
    tryMove(randomDirection)
  }

}
