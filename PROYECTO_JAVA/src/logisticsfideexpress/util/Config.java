/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.util;

/**
 *
 * @author JETLI
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class Config {

    private static final Properties P = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            P.load(in);
        } catch (IOException e) {
            System.err.println("Sin config.properties: se usan valores por defecto.");
        }
    }

    private Config() { }

    public static String get(String clave, String porDefecto) {
        return P.getProperty(clave, porDefecto);
    }
}