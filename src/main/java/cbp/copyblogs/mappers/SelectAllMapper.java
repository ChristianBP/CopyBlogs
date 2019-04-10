package cbp.copyblogs.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;


/**
 * 
 * @author Christian Parker
 * 
 * Put each value into a list of objects
 */
public class SelectAllMapper implements RowMapper<List<Object>> {
    
    @Override
    public List<Object> map(ResultSet rs, StatementContext sc) throws SQLException {
    	List<Object> res = new ArrayList<Object>();
        String current;
        
        for(int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
        	
        	current = rs.getString(j);
        	
        	if(current == null) {
        		res.add(null);
        	}
        	else if( rs.getMetaData().getColumnType(j) == Types.BIT ) {
        		res.add(1);
        	}
        	else {
        		res.add(current);
        	}
        }
        
        return res;
    }    
}
