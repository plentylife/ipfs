package life.plenty.model.octopi

import java.util.Date

import life.plenty.model
import life.plenty.model.connection.{CreationTime, Creator, Id}
import life.plenty.model.utils.Property

trait BasicInfo {
  val creator: User
  val creationTime: Long
}

trait WithBasicInfo {
  val _this: Octopus

  val _basicInfo: BasicInfo

  private[this] def getCreationTime: Date = if (_basicInfo != null) new Date(_basicInfo.creationTime) else null

  private[this] def getCreator: User = if (_basicInfo != null) _basicInfo.creator else null

  //  val _id: String = null
  lazy val idProperty = new Property[String]({ case Id(idValue: String) ⇒ idValue }, _this, null)
  //  val _creationTime: Long
  lazy val creationTime = new Property[Date]({ case CreationTime(t: Long) ⇒ new Date(t) }, _this, getCreationTime)
  lazy val creator = new Property[User]({ case Creator(u: User) ⇒ u }, _this, getCreator)


  if (_basicInfo != null) {
    _this.addConnection(CreationTime(_basicInfo.creationTime))
    _this.addConnection(Creator(getCreator))
  }


  def id: String = idProperty getOrLazyElse model.getHasher.b64(idGenerator)

  def idGenerator: String = {
    //    println(this, "is generating id", idProperty.getSafe, idProperty.getOrLazyElse("faulty"))
    ""
  }


}
