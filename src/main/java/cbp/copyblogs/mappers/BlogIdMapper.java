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
public class BlogIdMapper implements RowMapper<Integer> {
    
    @Override
    public Integer map(ResultSet rs, StatementContext sc) throws SQLException {
        return rs.getInt("blog_id");
    }
}
