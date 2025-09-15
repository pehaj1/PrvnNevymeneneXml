package son.nevymenene;

import java.sql.*;

public class DbfConnector {

    private final String folder = "c:\\Sms\\";
    private final String url = "jdbc:dbf:/c:/DBFS";
    private final Connection conn;
    private Statement stmt;
    private ResultSet results;
   

    public DbfConnector() throws SQLException {
        this.conn = DriverManager.getConnection(url);

    }

    public ResultSet getResultSetFromDatabase() {
        try {
            stmt = conn.createStatement();
            results = stmt.executeQuery("SELECT A.CLCIS6, B.PORCM, B.DRUH, A.VCMER1, A.VCMER2, A.XSTAV, B.VCMER1, B.VCMER2, B.XSTAV"
                    + " FROM UKMER A"
                    + " JOIN UKMER B ON A.CLCIS6 = B.CLCIS6"
                    + " AND A.PORCM = B.PORCM"
                    + " WHERE A.VCMER1 LIKE '9999%'"
                    + "  AND A.XSTAV = 'A'"
                    + "  AND B.XSTAV = 'N'"
                    + " ORDER BY A.CLCIS,PORCM;");
            int i = 1;
            /*while (results.next()) {

                String clcis = results.getString("A.CLCIS");
                String vcmer1 = results.getString("A.VCMER1");
                String porcm = results.getString("B.PORCM");
                String miestnost = results.getString("B.DRUH");
                String vcmer2 = results.getString("A.VCMER2");
                String xstav = results.getString("A.XSTAV");
                String vcmer1B = results.getString("B.VCMER1");
                String vcmer2B = results.getString("B.VCMER2");
                String xstavB = results.getString("B.XSTAV");
                String value = i + " " + clcis + " " + vcmer1 + " " + porcm + " " + miestnost + " " + vcmer2 + " " + xstav + " " + vcmer1B + " " + vcmer2B + " " + xstavB;
                System.out.println(value);
                
                
                
                i++;

            } */

        } catch (Exception ex) {
            System.out.println("Problem s dbf pripojenim");
   
        }
        return results;
    }
    
    
    

}
