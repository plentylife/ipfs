package life.plenty.model.security

import life.plenty.model.octopi.{BasicUser, User}

class SecureUser(private val _id: String) extends User {
  override def id: String = if (_id == null) {
    throw new Exception("SecuredUser ID cannot be null")
  } else _id
}

object SecureUser {
  import LibSodium._

  def apply(email: String, password: String): SecureUser = {
    val salt = crypto_generichash(crypto_pwhash_SALTBYTES, email, null)
    val passHash = LibSodiumWrapper.crypto_pwhash(crypto_box_SEEDBYTES, password, salt)
    val keyPair = crypto_box_seed_keypair(passHash)
    val id = crypto_generichash(40, to_base64(keyPair.publicKey), null)

    new SecureUser(to_base64(id))
  }
}

