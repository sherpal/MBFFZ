package exceptions

final class TooOldActionException(time: Long)
  extends Exception(s"Received action from time $time. (Now it's ${new java.util.Date().getTime})")
