/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package domain.parse.mysql;

import domain.DomainManager;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplemysql.SimpleMySQL;
import simplemysql.SimpleMySQLResult;
import simplemysql.query.clause.CreateTableClause;
import simplemysql.query.clause.InsertClauseColumns;
import simplemysql.query.clause.InsertClauseValues;
import simplemysql.query.QueryBuilder;
import simplemysql.query.clause.SelectClause;

/**
 *
 * @author Daniel
 */
class OidTableUtility {

    private final static long firstOid = 1;
    private static boolean tableInit = false;
    
    static long loadStateFromIDSTable(SimpleMySQL mysql) {
        if(!tableInit) {
            createIDSTable(mysql);
            tableInit = true;
        }
        QueryBuilder query = new QueryBuilder();
        SelectClause select = new SelectClause();
        select.addFunctionAS("MAX", false, MysqlStrings.OID, "id");
        query.SELECT(true, select).FROM(MysqlStrings.ID_TABLE_NAME);
        Logger.getLogger(DomainManager.class.getName()).
                log(Level.INFO, query.toString());
        SimpleMySQLResult result = mysql.Query(query.toString());
        Map<String, String> fetch = result.FetchAssoc();
        if (fetch.get("id") != null) {
            return Long.parseLong(fetch.get("id"));
        } else {
            OidTableUtility.setIDSTable(String.valueOf(firstOid), mysql);
            return firstOid;
        }
    }

    private static void createIDSTable(SimpleMySQL mysql) {
        // creates the table
        QueryBuilder query = new QueryBuilder();
        CreateTableClause table = new CreateTableClause();
        table.addColumn(MysqlStrings.OID, "BIGINT");
        table.addPrimaryKey(MysqlStrings.OID);
        query.CREATE_TABLE(true, MysqlStrings.ID_TABLE_NAME, table);
        Logger.getLogger(DomainManager.class.getName()).
                log(Level.INFO, query.toString());
        mysql.Query(query.toString());
    }

    private static void updateIDSTable(String uniqueID, SimpleMySQL mysql) {
        // removes the last id
        QueryBuilder deleteLastId = new QueryBuilder();
        deleteLastId.
                DELETE(true).
                FROM(MysqlStrings.ID_TABLE_NAME);
        mysql.Query(deleteLastId.toString());
        
        // insertes a new id
        QueryBuilder insertId = new QueryBuilder();
        InsertClauseColumns columns = new InsertClauseColumns();
        InsertClauseValues values = new InsertClauseValues();
        columns.addColumns(MysqlStrings.OID);
        values.addValues(String.valueOf(uniqueID));
        insertId.INSERT(MysqlStrings.ID_TABLE_NAME, columns, values);
        mysql.Query(insertId.toString());
    }

    static void setIDSTable(String uniqueID, SimpleMySQL mysql) {
        if(!tableInit) {
            createIDSTable(mysql);
            tableInit = true;
        }
        updateIDSTable(uniqueID, mysql);
    }
    
}
