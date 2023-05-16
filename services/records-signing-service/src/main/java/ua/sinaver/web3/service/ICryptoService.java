package ua.sinaver.web3.service;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public interface ICryptoService {

    byte[] generateECPrivateKey()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException;

    byte[] signDataWithECPrivateKey(byte[] data, byte[] privateKey) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, InvalidKeyException, SignatureException;
}
