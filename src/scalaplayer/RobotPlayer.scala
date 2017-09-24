package scalaplayer

import battlecode.common.RobotType._
import battlecode.common._


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
      case ARCHON => runArchon()
      case GARDENER => runGardener()
      case SOLDIER => runSoldier()
      case LUMBERJACK => runLumberjack()
      case _ => println("Unsupported type = " + rc.getType)
    }
  }

  @throws[GameActionException]
  private[scalaplayer] def runArchon() = {
    System.out.println("I'm an archon!")
    // The code you want your robot to perform every round should be in this loop
    while ( {
      true
    }) { // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
      try { // Generate a random direction
        val dir = randomDirection
        // Randomly attempt to build a gardener in this direction
        if (rc.canHireGardener(dir) && Math.random < .01) rc.hireGardener(dir)
        // Move randomly
        tryMove(randomDirection)
        // Broadcast archon's location for other robots on the team to know
        val myLocation = rc.getLocation
        rc.broadcast(0, myLocation.x.toInt)
        rc.broadcast(1, myLocation.y.toInt)
        // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
        Clock.`yield`()
      } catch {
        case e: Exception =>
          System.out.println("Archon Exception")
          e.printStackTrace()
      }
    }
  }

  @throws[GameActionException]
  private[scalaplayer] def runGardener() = {
    System.out.println("I'm a gardener!")
    while ( {
      true
    }) try { // Listen for home archon's location
      val xPos = rc.readBroadcast(0)
      val yPos = rc.readBroadcast(1)
      val archonLoc = new MapLocation(xPos, yPos)
      val dir = randomDirection
      // Randomly attempt to build a soldier or lumberjack in this direction
      if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random < .01) rc.buildRobot(RobotType.SOLDIER, dir)
      else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random < .01 && rc.isBuildReady) rc.buildRobot(RobotType.LUMBERJACK, dir)
      tryMove(randomDirection)
      Clock.`yield`()
    } catch {
      case e: Exception =>
        System.out.println("Gardener Exception")
        e.printStackTrace()
    }
  }

  @throws[GameActionException]
  private[scalaplayer] def runSoldier() = {
    System.out.println("I'm an soldier!")
    val enemy = rc.getTeam.opponent
    while ( {
      true
    }) try {
      val myLocation = rc.getLocation
      // See if there are any nearby enemy robots
      val robots = rc.senseNearbyRobots(-1, enemy)
      // If there are some...
      if (robots.nonEmpty) { // And we have enough bullets, and haven't attacked yet this turn...
        if (rc.canFireSingleShot) { // ...Then fire a bullet in the direction of the enemy.
          rc.fireSingleShot(rc.getLocation.directionTo(robots(0).location))
        }
      }
      tryMove(randomDirection)
      Clock.`yield`()
    } catch {
      case e: Exception =>
        System.out.println("Soldier Exception")
        e.printStackTrace()
    }
  }

  @throws[GameActionException]
  private[scalaplayer] def runLumberjack() = {
    System.out.println("I'm a lumberjack!")
    val enemy = rc.getTeam.opponent
    while ( {
      true
    }) try { // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
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
      Clock.`yield`()
    } catch {
      case e: Exception =>
        System.out.println("Lumberjack Exception")
        e.printStackTrace()
    }
  }

  /**
    * Returns a random Direction
    * @return a random Direction
    */
  private[scalaplayer] def randomDirection = new Direction(Math.random.toFloat * 2 * Math.PI.toFloat)

  /**
    * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
    * @param dir The intended direction of movement
    * @return true if a move was performed
    * @throws GameActionException
    */
  @throws[GameActionException]
  private[scalaplayer] def tryMove(dir: Direction): Boolean = tryMove(dir, 20, 3)

  /**
    * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
    * @param dir           The intended direction of movement
    * @param degreeOffset  Spacing between checked directions (degrees)
    * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
    * @return true if a move was performed
    * @throws GameActionException
    */
  @throws[GameActionException]
  private[scalaplayer] def tryMove(dir: Direction, degreeOffset: Float, checksPerSide: Int): Boolean = {
    // First, try intended direction
    if (rc.canMove(dir)) {
      rc.move(dir)
      return true
    }
    // Now try a bunch of similar angles
    val moved = false
    var currentCheck = 1
    while ( {
      currentCheck <= checksPerSide
    }) { // Try the offset of the left side
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
  private[scalaplayer] def willCollideWithMe(bullet: BulletInfo): Boolean = {
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
