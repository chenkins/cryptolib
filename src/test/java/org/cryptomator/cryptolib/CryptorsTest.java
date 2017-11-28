/*******************************************************************************
 * Copyright (c) 2016 Sebastian Stenzel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the accompanying LICENSE.txt.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.cryptolib;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.FileNameCryptor;
import org.cryptomator.cryptolib.api.KeyFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CryptorsTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private final SecureRandom seeder = Mockito.mock(SecureRandom.class);

	@Before
	public void setup() {
		Mockito.when(seeder.generateSeed(Mockito.anyInt())).then(new Answer<byte[]>() {

			@Override
			public byte[] answer(InvocationOnMock invocation) throws Throwable {
				return new byte[(int) invocation.getArgument(0)];
			}

		});
	}

	@Test
	public void testVersion1() {
		CryptorProvider cryptorProvider = Cryptors.version1(seeder);
		Assert.assertNotNull(cryptorProvider);
		Cryptor cryptor = cryptorProvider.createNew();
		Assert.assertNotNull(cryptor);
		FileContentCryptor fileContentCryptor = cryptor.fileContentCryptor();
		FileHeaderCryptor fileHeaderCryptor = cryptor.fileHeaderCryptor();
		FileNameCryptor fileNameCryptor = cryptor.fileNameCryptor();
		Assert.assertNotNull(fileContentCryptor);
		Assert.assertNotNull(fileHeaderCryptor);
		Assert.assertNotNull(fileNameCryptor);
	}

	@Test
	public void testCleartextSize() {
		Cryptor c = Mockito.mock(Cryptor.class);
		FileContentCryptor cc = Mockito.mock(FileContentCryptor.class);
		Mockito.when(c.fileContentCryptor()).thenReturn(cc);
		Mockito.when(cc.cleartextChunkSize()).thenReturn(32);
		Mockito.when(cc.ciphertextChunkSize()).thenReturn(40);

		Assert.assertEquals(0l, Cryptors.cleartextSize(0l, c));
		Assert.assertEquals(1l, Cryptors.cleartextSize(9l, c));
		Assert.assertEquals(31l, Cryptors.cleartextSize(39l, c));
		Assert.assertEquals(32l, Cryptors.cleartextSize(40l, c));
		Assert.assertEquals(33l, Cryptors.cleartextSize(49l, c));
		Assert.assertEquals(34l, Cryptors.cleartextSize(50l, c));
		Assert.assertEquals(63l, Cryptors.cleartextSize(79l, c));
		Assert.assertEquals(64l, Cryptors.cleartextSize(80l, c));
		Assert.assertEquals(65l, Cryptors.cleartextSize(89l, c));
	}

	@Test
	public void testCleartextSizeWithInvalidCiphertextSize() {
		Cryptor c = Mockito.mock(Cryptor.class);
		FileContentCryptor cc = Mockito.mock(FileContentCryptor.class);
		Mockito.when(c.fileContentCryptor()).thenReturn(cc);
		Mockito.when(cc.cleartextChunkSize()).thenReturn(32);
		Mockito.when(cc.ciphertextChunkSize()).thenReturn(40);

		Collection<Integer> undefinedValues = Arrays.asList(1, 8, 41, 48, 81, 88);
		for (Integer val : undefinedValues) {
			try {
				Cryptors.cleartextSize(val, c);
				Assert.fail("Expected exception for input value " + val);
			} catch (IllegalArgumentException e) {
				continue;
			}
		}
	}

	@Test
	public void testCiphertextSize() {
		Cryptor c = Mockito.mock(Cryptor.class);
		FileContentCryptor cc = Mockito.mock(FileContentCryptor.class);
		Mockito.when(c.fileContentCryptor()).thenReturn(cc);
		Mockito.when(cc.cleartextChunkSize()).thenReturn(32);
		Mockito.when(cc.ciphertextChunkSize()).thenReturn(40);

		Assert.assertEquals(0l, Cryptors.ciphertextSize(0l, c));
		Assert.assertEquals(9l, Cryptors.ciphertextSize(1l, c));
		Assert.assertEquals(39l, Cryptors.ciphertextSize(31l, c));
		Assert.assertEquals(40l, Cryptors.ciphertextSize(32l, c));
		Assert.assertEquals(49l, Cryptors.ciphertextSize(33l, c));
		Assert.assertEquals(50l, Cryptors.ciphertextSize(34l, c));
		Assert.assertEquals(79l, Cryptors.ciphertextSize(63l, c));
		Assert.assertEquals(80l, Cryptors.ciphertextSize(64l, c));
		Assert.assertEquals(89l, Cryptors.ciphertextSize(65l, c));
	}

	@Test
	public void testChangePassphrase() {
		CryptorProvider cryptorProvider = Cryptors.version1(seeder);
		Cryptor cryptor1 = cryptorProvider.createNew();
		byte[] origMasterkey = cryptor1.writeKeysToMasterkeyFile("password", 42).serialize();
		byte[] newMasterkey = Cryptors.changePassphrase(cryptorProvider, origMasterkey, "password", "betterPassw0rd!");
		Cryptor cryptor2 = cryptorProvider.createFromKeyFile(KeyFile.parse(newMasterkey), "betterPassw0rd!", 42);
		Assert.assertNotNull(cryptor2);
	}

}
