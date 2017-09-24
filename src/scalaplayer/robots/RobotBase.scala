package scalaplayer.robots

import battlecode.common._
import RobotBase.DEGREE_OFFSET

object RobotBase {
  val DEGREE_OFFSET = 20 // was 30
}

/**
  * @author Barry Becker
  */
abstract class RobotBase(rc: RobotController) {

  def run(): Unit = {
    System.out.println("I'm a " + rc.getType)
    // The code you want your robot to perform every round should be in this loop
    while (true) { // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
      try { // Generate a random direction
        doBehavior()

        // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
        Clock.`yield`()
      } catch {
        case e: Exception =>
          System.out.println(rc.getType + " Exception")
          e.printStackTrace()
      }
    }
  }

  def doBehavior(): Unit

  protected def enemy: Team = rc.getTeam.opponent

  /**
    * Returns a random Direction
    * @return a random Direction
    */
  protected def randomDirection = new Direction(Math.random.toFloat * 2 * Math.PI.toFloat)

  /**
    * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
    * @param dir The intended direction of movement
    * @return true if a move was performed
    */
  @throws[GameActionException]
  protected def tryMove(dir: Direction): Boolean = tryMove(dir, DEGREE_OFFSET, 3)

  /**
    * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
    * @param dir           The intended direction of movement
    * @param degreeOffset  Spacing between checked directions (degrees)
    * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
    * @return true if a move was performed
    */
  @throws[GameActionException]
  protected def tryMove(dir: Direction, degreeOffset: Float, checksPerSide: Int): Boolean = {
    // First, try intended direction
    if (rc.canMove(dir)) {
      rc.move(dir)
      return true
    }
    // Now try a bunch of similar angles
    var currentCheck = 1
    while (currentCheck <= checksPerSide) { // Try the offset of the left side
      if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
        rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck))
        return true
      }
      // Try the offset on the right side
      if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
        rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck))
        return true
      }
      // No move performed, try slightly further
      currentCheck += 1
    }
    // A move never happened, so return false.
    false
  }

  /**
    * A slightly more complicated example function, this returns true if the given bullet is on a collision
    * course with the current robot. Doesn't take into account objects between the bullet and this robot.
    * @param bullet The bullet in question
    * @return True if the line of the bullet's path intersects with this robot's current position.
    */
  protected def willCollideWithMe(bullet: BulletInfo): Boolean = {
    val myLocation = rc.getLocation
    // Get relevant bullet information
    val propagationDirection = bullet.dir
    val bulletLocation = bullet.location
    // Calculate bullet relations to this robot
    val directionToRobot = bulletLocation.directionTo(myLocation)
    val distToRobot = bulletLocation.distanceTo(myLocation)
    val theta = propagationDirection.radiansBetween(directionToRobot)
    // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
    if (Math.abs(theta) > Math.PI / 2) return false
    // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
    // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
    // This corresponds to the smallest radius circle centered at our location that would intersect with the
    // line that is the path of the bullet.
    val perpendicularDist = Math.abs(distToRobot * Math.sin(theta)).toFloat // soh cah toa :)
    perpendicularDist <= rc.getType.bodyRadius
  }
}



