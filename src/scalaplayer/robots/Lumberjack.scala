package scalaplayer.robots

import battlecode.common.{GameConstants, RobotController, RobotType, Team}


/**
  * @author Barry Becker
  */
class Lumberjack(rc: RobotController) extends RobotBase(rc) {

  override def doBehavior(): Unit = {
    var robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy)
    if (robots.length > 0 && !rc.hasAttacked) { // Use strike() to hit all nearby robots!
      rc.strike()
    }
    else { // No close robots, so search for robots within sight radius
      robots = rc.senseNearbyRobots(-1, enemy)
      // If there is a robot, move towards it
      if (robots.length > 0) {
        val myLocation = rc.getLocation
        val enemyLocation = robots(0).getLocation
        val toEnemy = myLocation.directionTo(enemyLocation)
        tryMove(toEnemy)
      }
      else { // Move Randomly
        tryMove(randomDirection)
      }
    }
  }

}