package ua.sinaver.web3.service;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.springframework.stereotype.Service;

@Service
public class CryptoService implements ICryptoService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom("axelar".getBytes());

    @Override
    public byte[] generateECPrivateKey()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // Create an ECGenParameterSpec object for the secp256k1 curve.
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");

        // Create a KeyPairGenerator object for the ECDSA algorithm.
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");

        // Initialize the KeyPairGenerator object with the ECGenParameterSpec object and
        // the SecureRandom object.
        keyPairGenerator.initialize(ecGenParameterSpec, SECURE_RANDOM);

        // Generate an EC key pair.
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair.getPrivate().getEncoded();

    }

    @Override
    public byte[] signDataWithECPrivateKey(byte[] data, byte[] privateKeyEncoded)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException,
            SignatureException {
        // Create a Signature object for the ECDSA algorithm.
        Signature signature = Signature.getInstance("ECDSA", "BC");

        // Load the private key from a file.
        PrivateKey privateKey = KeyFactory.getInstance("EC", "BC")
                .generatePrivate(new PKCS8EncodedKeySpec(privateKeyEncoded));

        // Initialize the Signature object with the private key.
        signature.initSign(privateKey);

        // Update the Signature object with the message to be signed.
        signature.update(data);

        // Sign the message.
        return signature.sign();
    }

}
