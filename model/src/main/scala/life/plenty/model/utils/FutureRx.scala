package life.plenty.model.utils

import scala.concurrent.ExecutionContext.Implicits.global
import rx.{Rx, Var}

import scala.concurrent.Future

//sealed trait FutureRx[T] {
//  def get(default: T): Rx[T]
//}
//
//
//
//object FutureRx {
//  type FRx[T] = Future[Rx[T]]
//  type FRxChain[T] = FRx[FRx[T]]
//
//  def apply[T](what: T) = {
//    Future { Rx {what}}
//  }
//
//  implicit class FutureRxOps[T](frx: Future[Rx[T]]) {
//    def flatMapRx[M](f: T ⇒ Rx[M]): FRx[M] = {
//      frx map { rx ⇒
//        rx flatMap f
//      }
//    }
//
//    def mapChain[M](f: T ⇒ FRx[M]): FRxChain[M] = {
//      frx map { rx ⇒
//        rx map f
//      }
//    }
//  }
//
//  implicit class FutureRxChain[T](chain: FRxChain[T]) {
//    def get(default: T): Rx[T] = {
//      val res = Var(default)
//      chain foreach {
//        _ foreach {
//          _ foreach {
//            _ foreach {v ⇒ res() = v}
//          }
//        }
//      }
//      res
//    }
//  }
//
//}
