package banca.security;

import banca.model.Cuenta;
import banca.model.Movimiento;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
        import java.security.*;
        import java.util.ArrayList;

public class GestorSeguridad {

    private static final String FICHERO_DATOS = "cuenta.dat";
    private static final String FICHERO_CLAVE_AES = "clave_secreta.aes"; // Guardamos la clave para poder descifrar luego

    // --- MÉTODOS AES (Simétrico) ---

    // Genera o carga la clave AES
    public SecretKey obtenerClaveAES() throws Exception {
        File f = new File(FICHERO_CLAVE_AES);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                return (SecretKey) ois.readObject();
            }
        } else {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            SecretKey key = kg.generateKey();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(key);
            }
            return key;
        }
    }

    public byte[] cifrarAES(String texto, SecretKey key) throws Exception {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(texto.getBytes());
    }

    public String descifrarAES(byte[] datos, SecretKey key) throws Exception {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        return new String(c.doFinal(datos));
    }

    // --- GUARDAR DATOS (Híbrido: Cifrado + Texto Plano) ---
    public void guardarCuenta(Cuenta cuenta) {
        try {
            SecretKey key = obtenerClaveAES();

            // 1. Ciframos atributos sensibles
            byte[] numCuentaCifrado = cifrarAES(cuenta.getNumeroCuenta(), key);
            byte[] nombreCifrado = cifrarAES(cuenta.getNombreCliente(), key);

            // 2. Escribimos en el fichero con una estructura mixta
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHERO_DATOS))) {
                // Escribimos los objetos cifrados (byte[])
                oos.writeObject(numCuentaCifrado);
                oos.writeObject(nombreCifrado);
                // Escribimos la lista tal cual (Serializable, sin cifrar)
                oos.writeObject(cuenta.getMovimientos());
            }
            System.out.println("💾 Datos guardados correctamente en " + FICHERO_DATOS);

        } catch (Exception e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }
    }

    // --- CARGAR DATOS ---
    @SuppressWarnings("unchecked")
    public Cuenta cargarCuenta() {
        File f = new File(FICHERO_DATOS);
        if (!f.exists()) return null;

        try {
            SecretKey key = obtenerClaveAES();

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                // 1. Leemos los bytes cifrados
                byte[] numCuentaCifrado = (byte[]) ois.readObject();
                byte[] nombreCifrado = (byte[]) ois.readObject();

                // 2. Desciframos
                String numCuenta = descifrarAES(numCuentaCifrado, key);
                String nombre = descifrarAES(nombreCifrado, key);

                // 3. Leemos la lista sin cifrar
                ArrayList<Movimiento> movimientos = (ArrayList<Movimiento>) ois.readObject();

                // 4. Reconstruimos el objeto
                Cuenta c = new Cuenta(numCuenta, nombre);
                c.setMovimientos(movimientos);
                return c;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos: " + e.getMessage());
            return null;
        }
    }

    // --- MÉTODOS DSA (Firma Digital) ---
    // Genera ficheros para la primera ejecución
    public void generarEntornoFirmaDSA() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            // Guardamos Pública
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("publica.dsa"))){
                oos.writeObject(kp.getPublic());
            }

            // Firmamos un fichero de 'token'
            Signature dsa = Signature.getInstance("SHA256withDSA");
            dsa.initSign(kp.getPrivate());
            dsa.update("USUARIO_AUTORIZADO".getBytes());
            byte[] firma = dsa.sign();

            try(FileOutputStream fos = new FileOutputStream("token.firma")){
                fos.write(firma);
            }
            System.out.println("⚙️ Entorno DSA generado.");

        } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean verificarIdentidadDSA() {
        try {
            // Leemos clave pública
            ObjectInputStream oisPub = new ObjectInputStream(new FileInputStream("publica.dsa"));
            PublicKey pubKey = (PublicKey) oisPub.readObject();
            oisPub.close();

            // Leemos la firma
            File f = new File("token.firma");
            byte[] firmaBytes = new byte[(int)f.length()];
            FileInputStream fis = new FileInputStream(f);
            fis.read(firmaBytes);
            fis.close();

            // Verificamos
            Signature dsa = Signature.getInstance("SHA256withDSA");
            dsa.initVerify(pubKey);
            dsa.update("USUARIO_AUTORIZADO".getBytes()); // El mensaje original esperado
            return dsa.verify(firmaBytes);

        } catch (Exception e) {
            return false;
        }
    }
}