package tikape.runko.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tikape.runko.domain.Alue;

public class AlueDao implements Dao<Alue, Integer>{
    private Database database;

    public AlueDao(Database database) {
        this.database = database;
    }
    
    // luodaan tietokantaan uusi alue . Uuden alueen alue_id saadaan tietokannasta
    public Alue create(Alue uusiAlue) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO Alue (kuvaus) VALUES ( ? )");
        stmt.setObject(1, uusiAlue.getKuvaus());
        stmt.execute();
        
        // etsitään juuri luodun alueen alue_id
        stmt = connection.prepareStatement("SELECT Alue.alue_id AS id FROM Alue WHERE Alue.kuvaus = ? ;");       
        stmt.setObject(1, uusiAlue.getKuvaus());
        ResultSet rs = stmt.executeQuery();
        
        // jos epäonnistui palautetaan null
        boolean hasOne = rs.next();
        if (!hasOne) {
            return null;
        }
        
        // palautetaan juuri luotu uusi alue 
        int id = rs.getInt("id");
        System.out.println("UUsi ID ON : " + id);
        Alue luotuAlue = new Alue(id, uusiAlue.getKuvaus(), 0 , "");
        stmt.close();
        connection.close();        
        return luotuAlue;
    }

    // etsitään alue_id:n perusteella yksi alue sekä viestien määrä alueessa ja viimeisen ajankohta
    @Override
    public Alue findOne(Integer key) throws SQLException {
        Connection connection = database.getConnection();
        // luodaan erilaiset rivit sqlite ja postgresql varten että kellonaika toimii järkevästi
        String viimeisinAika = "DATETIME(MAX(Viesti.ajankohta), 'localtime')  AS viimeisin ";        
        if (database.getDatabaseAddress().contains("postgres")) {
            viimeisinAika = "TO_CHAR(MAX(Viesti.ajankohta) AT TIME ZONE 'UTC' AT TIME ZONE 'EEST', 'YYYY-MM-DD HH24:MI:SS' ) AS viimeisin ";
        }         
        PreparedStatement stmt = connection.prepareStatement("SELECT Alue.alue_id AS id, Alue.kuvaus AS kuvaus, "
                + "COUNT(Viesti.viesti_id) AS viesteja, "
                + viimeisinAika
                + "FROM Alue LEFT JOIN Aihe ON Alue.alue_id=Aihe.alue_id LEFT JOIN Viesti ON Aihe.aihe_id=Viesti.aihe_id " 
                + "WHERE Alue.alue_id = ? GROUP BY Alue.alue_id ORDER BY Alue.kuvaus;");       
        stmt.setObject(1, key);
        ResultSet rs = stmt.executeQuery();
        
        // aluetta ei löytynyt palauetaan null
        boolean hasOne = rs.next();
        if (!hasOne) {
            return null;
        }
        
        // palautetaan löytynyt alue
        int id = rs.getInt("id");
        String kuvaus = rs.getString("kuvaus");
        int viesteja = rs.getInt("viesteja");
        String viimeisin = rs.getString("viimeisin");

        Alue alue = new Alue(id, kuvaus, viesteja, viimeisin);

        rs.close();
        stmt.close();
        connection.close();

        return alue;    
    }

    // etsitään kaikki alueet sekä viestin määrä alueessa sekä viimesien ajankohnta
    @Override
    public List<Alue> findAll() throws SQLException {
        Connection connection = database.getConnection();
        // sqlite ja postgresql erot ajannäyttämisen suhteen
        String viimeisinAika = "DATETIME(MAX(Viesti.ajankohta), 'localtime')  AS viimeisin ";        
        if (database.getDatabaseAddress().contains("postgres")) {
            viimeisinAika = "TO_CHAR(MAX(Viesti.ajankohta) AT TIME ZONE 'UTC' AT TIME ZONE 'EEST', 'YYYY-MM-DD HH24:MI:SS' ) AS viimeisin ";
        } 
        PreparedStatement stmt = connection.prepareStatement(""
                + "SELECT Alue.alue_id AS id, Alue.kuvaus AS kuvaus, "
                + "COUNT(Viesti.viesti_id) AS viesteja, "
                + viimeisinAika
                + "FROM Alue "
                + "LEFT JOIN Aihe ON Alue.alue_id=Aihe.alue_id "
                + "LEFT JOIN Viesti ON Aihe.aihe_id=Viesti.aihe_id " 
                + "GROUP BY Alue.alue_id "
                + "ORDER BY Alue.kuvaus;");
      
        ResultSet rs = stmt.executeQuery();
        List<Alue> alueet = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String kuvaus = rs.getString("kuvaus");
            int viesteja = rs.getInt("viesteja");
            String viimeisin = rs.getString("viimeisin");

            alueet.add(new Alue(id, kuvaus, viesteja, viimeisin));
        }

        rs.close();
        stmt.close();
        connection.close();

        return alueet;
    }

    // ei tarvita projektissa, vai TODO
    @Override
    public void delete(Integer key) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // ei tarvita, ei tehty
    @Override
    public List<Alue> findAllIn(Integer id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
