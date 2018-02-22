package life.plenty.model.security

import life.plenty.model.octopi.BasicUser

trait SecuredUser {
  private[SecuredUser] def setId(newId: String) = _id = newId
  private[this] var _id: String = null

  def id: String = if (_id == null) {
    throw new Exception("SecuredUser ID cannot be null")
  } else _id
}

object SecuredUser {
  import LibSodium._
  def apply(email: String, password: String, onto: SecuredUser): SecuredUser = {
    val passHash = LibSodiumWrapper.crypto_pwhash(crypto_pwhash_BYTES_MIN, password, email)
    val keyPair = crypto_box_seed_keypair(passHash)
    val id = crypto_generichash(40, to_base64(keyPair.publicKey), null)

    onto.setId(to_base64(id))
    onto
  }
}

