package life.plenty.model.security

import life.plenty.model.security.LibSodium.{crypto_pwhash_ALG_DEFAULT, crypto_pwhash_MEMLIMIT_INTERACTIVE, crypto_pwhash_OPSLIMIT_INTERACTIVE}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}
import scala.scalajs.js.typedarray.Uint8Array

@js.native
@JSGlobal("sodium")
object LibSodium extends js.Object {
  val crypto_pwhash_MEMLIMIT_INTERACTIVE: Int = js.native
  val crypto_pwhash_OPSLIMIT_INTERACTIVE: Int = js.native
  val crypto_pwhash_ALG_DEFAULT: Int = js.native
  val crypto_pwhash_BYTES_MIN: Int = js.native
  val crypto_pwhash_SALTBYTES: Int = js.native
  val crypto_box_SEEDBYTES: Int = js.native

  def crypto_pwhash(keySize: Int, pass: String, salt: Uint8Array,
                    opsLimit: Int, memLimit: Int, alg: Int): Uint8Array = js.native

  def crypto_box_seed_keypair(seed: Uint8Array): KeyPair = js.native
  def crypto_generichash(size: Int, msg: String, key: Uint8Array): Uint8Array = js.native

  def to_base64(what: Uint8Array): String = js.native
  def from_base64(what: String): Uint8Array = js.native
}

trait KeyPair extends js.Object {
  val publicKey: Uint8Array = js.native
  val privateKey: Uint8Array = js.native
  val keyType: String = js.native
}

object LibSodiumWrapper {
  def crypto_pwhash(keySize: Int, pass: String, salt: Uint8Array): Uint8Array =
    LibSodium.crypto_pwhash(keySize, pass, salt, crypto_pwhash_OPSLIMIT_INTERACTIVE,
      crypto_pwhash_MEMLIMIT_INTERACTIVE / 2,
      crypto_pwhash_ALG_DEFAULT)
}
