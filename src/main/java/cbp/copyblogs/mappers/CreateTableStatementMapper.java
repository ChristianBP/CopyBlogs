package cbp.copyblogs.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
*
* @author Christian Parker
* 
*/
public class CreateTableStatementMapper implements RowMapper<String> {
    
    @Override
    public String map(ResultSet rs, StatementContext sc) throws SQLException {
        return rs.getString("Create Table");
    }    
}
