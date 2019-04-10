package cbp.copyblogs.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;


/**
 * 
 * @author Christian Parker
 * 
 * Put each value in this row into a String
 */
public class SelectRowMapper implements RowMapper<String> {
    
    @Override
    public String map(ResultSet rs, StatementContext sc) throws SQLException {
        String res = "";
        
        for(int j = 2; j <= rs.getMetaData().getColumnCount(); j++) {
        	if( j == 3 ) {
        		res += ", DOMAIN";
        	}
        	else if(rs.getString(j) == null) {
        		res += ", null";
        	}
        	else {
        		res += ", '" + rs.getString(j).replaceAll("'", "''") + "'";
        	}
        }
        
        return res.substring(2);
    }    
}
