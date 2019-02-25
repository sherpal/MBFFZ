package gamestate

import gamestate.actions.{GameAction, UpdatePlayerPos}
import exceptions.TooOldActionException

/**
  * An ActionCollector will gather all [[GameAction]]s, sort then in order and allow to recover [[GameState]]s.
  */
final class ActionCollector(
                             val originalGameState: GameState,
                             val timeBetweenGameStates: Long = 2000,
                             val timeToOldestGameState: Long = 60000
                           ) {

  /**
    * actionsAndStates remembers the [[GameState]]s from the past and the actions from one to the other.
    * An element of the list is a couple GameState, List[GameAction] where the list is sorted from oldest to
    * newest, and are the actions between that GameState and the next one. The GameStates, however, are sorted
    * from newest to oldest.
    *
    * It means that if
    * gs1 = actionsAndStates(n)._1
    * gs2 = actionsAndStates(n-1)._1
    * actions = actionsAndStates(n)._2
    * then we have
    * gs2 = gs1(actions)
    *
    * This property is maintained in the addAction method.
    */
  private var actionsAndStates: List[(GameState, List[GameAction])] = List((originalGameState, Nil))

  def backupState(n: Int): (GameState, List[GameAction]) = actionsAndStates(n)

  // TODO: add all actions that share the same time all together.
  def addActions(actions: Traversable[GameAction]): Unit = {
    actions.foreach(addAction(_, needUpdate = false))
    updateGameState()
  }


  def shouldKeepAction(action: GameAction, state: GameState): Boolean = state.isLegalAction(action)

  /**
    * Adds all the actions from the list, and removes actions that turn out to be either not legal, or should no more
    * happen.
    *
    * @param actions The actions to add, ordered from oldest to newest
    * @return        The most ancient time at which actions were removed, and the list of the ids of actions that were
    *                removed
    */
  def addAndRemoveActions(actions: List[GameAction]): (Long, List[Long]) = {
    val oldestTime: Long = actions.head.time

    def mergeActions(ls1: List[GameAction], ls2: List[GameAction], accumulator: List[GameAction]): List[GameAction] = {
      if (ls1.isEmpty)
        accumulator.reverse ++ ls2
      else if (ls2.isEmpty)
        accumulator.reverse ++ ls1
      else if (ls1.head.time <= ls2.head.time)
        mergeActions(ls1.tail, ls2, ls1.head +: accumulator)
      else
        mergeActions(ls1, ls2.tail, ls2.head +: accumulator)
    }


    val actionIdsToRemove = mergeActions(
      actionsFrom(oldestTime),
      actions,
      Nil
    ).foldLeft((gameStateUpTo(oldestTime), List[Long]()))({
      case ((state, toRemove), action) =>
        if (shouldKeepAction(action, state)) {
          (action(state), toRemove)
        } else
          (state, action.id +: toRemove)
    })
      ._2
      .reverse

    actions.foreach(addAction(_, needUpdate = false))
    removeActions(oldestTime, actionIdsToRemove)

    (oldestTime, actionIdsToRemove)
  }

  def removeActions(oldestTime: Long, actionIds: List[Long]): Unit = {

    val (after, before) = actionsAndStates.span(_._1.time >= oldestTime)

    val (toBeChanged, toRemain) = if (before.nonEmpty)
      (after :+ before.head, before.tail)
    else
      (after, before)

    // after is from newest to oldest

    val reverseAfter = toBeChanged.reverse
    // reverseAfter is from oldest to newest

    val allActionsAfter = reverseAfter.flatMap(_._2)
    // allActionsAfter is from oldest to newest


    val remainingActionsAfter = allActionsAfter.foldLeft((List[GameAction](), actionIds))({
      case ((acc, toRemove), nextAction) =>
        if (toRemove.isEmpty)
          (nextAction +: acc, Nil)
        else {
          toRemove.indexOf(nextAction.id) match {
            case -1 =>
              (nextAction +: acc, toRemove)
            case idx =>
              (acc, toRemove.drop(idx + 1))
          }
        }
    })
      ._1
      .reverse
    // remainingActionsAfter is from oldest to newest


    if (remainingActionsAfter.isEmpty) {
      actionsAndStates = toRemain
    } else if (toRemain.nonEmpty) {
      actionsAndStates = (toRemain.head._1(toRemain.head._2), remainingActionsAfter) +: toRemain
    } else {
      actionsAndStates = List((toBeChanged.last._1, remainingActionsAfter))
    }

    updateGameState()
  }


  // TODO: when adding an action, we should check for the actions coming after to see if they are still legal.
  /**
    * Adds an action to the gameState.
    * When an action is added, it is put at the last possible position, after all actions at the same time that where
    * already added.
    *
    * @param action     the [[GameAction]] to add
    * @param needUpdate whether we should update the game state after inserting the action
    */
  def addAction(action: GameAction, needUpdate: Boolean = true): Unit = {
    if (action.time > actionsAndStates.head._1.time + timeBetweenGameStates) {
      updateGameState()
      actionsAndStates = (currentGameState, List(action)) +: actionsAndStates
      if (timeToOldestGameState < actionsAndStates.head._1.time - actionsAndStates.last._1.time) {
        actionsAndStates = actionsAndStates.dropRight(1)
      }
      if (needUpdate) {
        updateGameState()
      }
    } else {
      def insertAction(action: GameAction, list: List[GameAction]): List[GameAction] = {
        // remaining is oldest to newest
        // treated   is newest to oldest
        def insertAcc(action: GameAction, treated: List[GameAction], remaining: List[GameAction]): List[GameAction] = {
          if (remaining.isEmpty) (action +: treated).reverse
          else if (remaining.head.time > action.time) treated.reverse ++ (action +: remaining)
          else insertAcc(action, remaining.head +: treated, remaining.tail)
        }

        insertAcc(action, Nil, list)
      }

      // the list actionsAndStates should typically not be too big
      // moreover, it is rare that we should add an action older than the second or third gameState,
      // provided timeToOldestGameState is not ridiculously too small...
      def add(action: GameAction, list: List[(GameState, List[GameAction])]): List[(GameState, List[GameAction])] = {
        if (action.time >= list.head._1.time) (list.head._1, insertAction(action, list.head._2)) +: list.tail
        else {
          val tail = add(action, list.tail)
          // we need to update the gameState since something changed in the past
          val head = (tail.head._1(tail.head._2), list.head._2)
          head +: tail
        }
      }

      try {
        actionsAndStates = add(action, actionsAndStates)
      } catch {
        case e: Throwable =>
          println("coucou")
          println("gs time " + actionsAndStates.head._1.time)
          println("action time " + action.time)
          throw e
      }


      if (needUpdate) {
        updateGameState()
      }
    }
  }

  /**
    * Returns the GameState as it was at time time.
    */
  def gameStateUpTo(time: Long): GameState = actionsAndStates.find(_._1.time <= time) match {
    case Some((gs, actions)) =>
      gs(actions.takeWhile(_.time <= time))
    case None =>
      println(time)
      throw new TooOldActionException(time)
  }

  def actionsFrom(time: Long): List[GameAction] =
    actionsAndStates
      .take(actionsAndStates.indexWhere(_._1.time < time) + 1)
      .reverse
      .flatMap(_._2)
      .dropWhile(_.time <= time)

  private var _currentGameState: GameState = originalGameState

  private def updateGameState(): Unit = {
    try {
      _currentGameState = actionsAndStates.head._1(actionsAndStates.head._2)
    } catch {
      case e: Throwable =>
        println(actionsAndStates.head._2.filterNot(_.isInstanceOf[UpdatePlayerPos]).mkString("\n"))
        throw e
    }
  }

  @inline def currentGameState: GameState = _currentGameState

  /**
    * Computes a GameState by adding some other actions to the stack.
    */
  def computeGameState(startingState: GameState, list1: Seq[GameAction], list2: List[GameAction]): GameState = {
    if (list1.isEmpty)
      startingState(list2)
    else if (list2.isEmpty)
      startingState(list1)
    else if (list1.head.time < list2.head.time)
      computeGameState(list1.head(startingState), list1.tail, list2)
    else
      computeGameState(list2.head(startingState), list1, list2.tail)
  }

}