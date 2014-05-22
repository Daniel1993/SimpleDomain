/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package domain;

/**
 * Initialize specific parsers.
 * @author Daniel
 */
public class ParserInitializer {
    
    private ParserInitializer() {
    }
    
    public static boolean initializeMySQL(Parser p, String host, String database,
            String user, String pass) {
        return p.initialize(host, database, user, pass);
    }
    
}
