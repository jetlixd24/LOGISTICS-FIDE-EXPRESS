/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import logisticsfideexpress.util.Config;

public final class ConexionBD {

    private ConexionBD() { }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(
            Config.get("db.url",
                "jdbc:mysql://localhost:3306/logistics_fide_express?serverTimezone=America/Lima"),
            Config.get("db.user", "root"),
            Config.get("db.password", "casita123"));
    }
}