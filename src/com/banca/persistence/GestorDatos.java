package com.banca.persistence;

import com.banca.model.Cuenta;
import com.banca.security.ServicioSeguridad;
import javax.crypto.SecretKey;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorDatos {

    private static final String FICHERO_DB = "banco.dat";
    private ServicioSeguridad seguridad;
    private SecretKey claveAES;

    public GestorDatos() throws Exception {
        this.seguridad = new ServicioSeguridad();
        this.claveAES = seguridad.cargarOgenerarClaveAES();
    }

    public void guardarCuentas(List<Cuenta> cuentas) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHERO_DB))) {
            oos.writeObject(cuentas);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Cuenta> cargarCuentas() {
        File f = new File(FICHERO_DB);
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            List<Cuenta> lista = (List<Cuenta>) ois.readObject();

            // Proceso vital: Descifrar los datos para uso en memoria
            for (Cuenta c : lista) {
                String num = seguridad.descifrar(c.getNumeroCuentaCifrado(), claveAES);
                String nom = seguridad.descifrar(c.getNombreClienteCifrado(), claveAES);
                c.setDatosDescifrados(num, nom);
            }
            return lista;
        } catch (Exception e) {
            System.err.println("Error leyendo base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ServicioSeguridad getServicioSeguridad() {
        return seguridad;
    }

    public SecretKey getClaveAES() {
        return claveAES;
    }
}