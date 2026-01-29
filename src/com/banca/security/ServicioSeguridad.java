package com.banca.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;

public class ServicioSeguridad {

    private static final String RUTA_CLAVE_AES = "secret.key";
    private static final String RUTA_PUB_DSA = "publica.dsa";
    private static final String RUTA_PRIV_DSA = "privada.dsa";
    private static final String RUTA_FIRMA = "identidad.firma";

    // --- AES ---
    public SecretKey cargarOgenerarClaveAES() throws Exception {
        File f = new File(RUTA_CLAVE_AES);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                return (SecretKey) ois.readObject();
            }
        }
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey key = kg.generateKey();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(key);
        }
        return key;
    }

    public byte[] cifrar(String datos, SecretKey key) throws Exception {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(datos.getBytes());
    }

    public String descifrar(byte[] datos, SecretKey key) throws Exception {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        return new String(c.doFinal(datos));
    }

    // --- DSA (FIRMA) ---
    public void verificarFirmaLogin() {
        try {
            // 1. Verificar si existen claves, si no, crearlas (Simulación de entorno)
            if (!new File(RUTA_PUB_DSA).exists()) generarClavesDSA();

            // 2. Leer Clave Pública
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA_PUB_DSA));
            PublicKey publica = (PublicKey) ois.readObject();
            ois.close();

            // 3. Leer Firma
            File fFirma = new File(RUTA_FIRMA);
            if (!fFirma.exists()) {
                throw new SecurityException("No se encuentra el archivo de firma digital (" + RUTA_FIRMA + "). Acceso denegado.");
            }
            byte[] firmaBytes = new byte[(int) fFirma.length()];
            try (FileInputStream fis = new FileInputStream(fFirma)) {
                fis.read(firmaBytes);
            }

            // 4. Validar
            Signature dsa = Signature.getInstance("SHA256withDSA");
            dsa.initVerify(publica);
            dsa.update("TOKEN_AUTORIZACION_2026".getBytes()); // Mensaje esperado

            if (!dsa.verify(firmaBytes)) {
                throw new SecurityException("Firma digital inválida o corrupta.");
            }
            // Si pasa, no lanza excepción

        } catch (Exception e) {
            System.err.println("⛔ ERROR DE SEGURIDAD CRÍTICO: " + e.getMessage());
            System.exit(403);
        }
    }

    private void generarClavesDSA() throws Exception {
        System.out.println("⚠️ Configuración inicial: Generando par de claves DSA y firma...");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // Guardar Pública
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_PUB_DSA))) {
            oos.writeObject(kp.getPublic());
        }
        // Generar firma válida de prueba
        Signature dsa = Signature.getInstance("SHA256withDSA");
        dsa.initSign(kp.getPrivate());
        dsa.update("TOKEN_AUTORIZACION_2026".getBytes());
        byte[] firma = dsa.sign();
        try (FileOutputStream fos = new FileOutputStream(RUTA_FIRMA)) {
            fos.write(firma);
        }
    }
}