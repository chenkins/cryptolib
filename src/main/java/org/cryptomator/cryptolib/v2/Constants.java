/*******************************************************************************
 * Copyright (c) 2016 Sebastian Stenzel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE.txt.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.cryptolib.v2;

final class Constants {

	static final String ENC_ALG = "AES";
	static final String MAC_ALG = "HmacSHA256";

	static final int KEY_LEN_BYTES = 32;
	static final int DEFAULT_SCRYPT_SALT_LENGTH = 8;
	static final int DEFAULT_SCRYPT_COST_PARAM = 1 << 15; // 2^15
	static final int DEFAULT_SCRYPT_BLOCK_SIZE = 8;

	static final int GCM_NONCE_SIZE = 12; // 96 bit IVs strongly recommended for GCM
	static final int PAYLOAD_SIZE = 32 * 1024;
	static final int GCM_TAG_SIZE = 16;
	static final int CHUNK_SIZE = GCM_NONCE_SIZE + PAYLOAD_SIZE + GCM_TAG_SIZE;

}