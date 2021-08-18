package org.cryptomator.cryptolib.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

class Pkcs12Helper {

	private static final String KEYSTORE_ALIAS_KEY = "key";
	private static final String KEYSTORE_ALIAS_CERT = "crt";

	private Pkcs12Helper() {
	}

	/**
	 * Stores the given key pair in PKCS#12 format.
	 *
	 * @param keyPair      The key pair to export
	 * @param out          The output stream to which the result will be written
	 * @param pw           The password to protect the key material
	 * @param signatureAlg A suited signature algorithm to sign a x509v3 cert holding the public key
	 * @throws IOException     In case of I/O errors
	 * @throws Pkcs12Exception If any cryptographic operation fails
	 */
	public static void export(KeyPair keyPair, OutputStream out, char[] pw, String signatureAlg) throws IOException, Pkcs12Exception {
		try {
			KeyStore keyStore = getKeyStore();
			keyStore.load(null, pw);
			X509Certificate cert = X509CertBuilder.createSelfSignedCert(keyPair, signatureAlg);
			X509Certificate[] chain = new X509Certificate[]{cert};
			keyStore.setKeyEntry(KEYSTORE_ALIAS_KEY, keyPair.getPrivate(), pw, chain);
			keyStore.setCertificateEntry(KEYSTORE_ALIAS_CERT, cert);
			keyStore.store(out, pw);
		} catch (GeneralSecurityException e) {
			throw new Pkcs12Exception("Failed to store PKCS12 file.", e);
		}
	}

	/**
	 * Loads a key pair from PKCS#12 format.
	 *
	 * @param in Where to load the key pair from
	 * @param pw The password to protect the key material
	 * @throws IOException             In case of I/O errors
	 * @throws Pkcs12PasswordException If the supplied password is incorrect
	 * @throws Pkcs12Exception         If any cryptographic operation fails
	 */
	public static KeyPair load(InputStream in, char[] pw) throws IOException, Pkcs12PasswordException, Pkcs12Exception {
		try {
			KeyStore keyStore = getKeyStore();
			keyStore.load(in, pw);
			PrivateKey sk = (PrivateKey) keyStore.getKey(KEYSTORE_ALIAS_KEY, pw);
			PublicKey pk = keyStore.getCertificate(KEYSTORE_ALIAS_CERT).getPublicKey();
			return new KeyPair(pk, sk);
		} catch (UnrecoverableKeyException e) {
			throw new Pkcs12PasswordException(e);
		} catch (IOException e) {
			if (e.getCause() instanceof UnrecoverableKeyException) {
				throw new Pkcs12PasswordException(e);
			} else {
				throw e;
			}
		} catch (GeneralSecurityException e) {
			throw new Pkcs12Exception("Failed to load PKCS12 file.", e);
		}
	}

	private static KeyStore getKeyStore() {
		try {
			return KeyStore.getInstance("PKCS12");
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Every implementation of the Java platform is required to support PKCS12.");
		}
	}

}
