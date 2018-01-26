package life.plenty.model

import rx.Rx

package object utils {

  /** unsafe */
  implicit def getRx[T](r: Rx[Option[T]]): T = r.now.get
}
