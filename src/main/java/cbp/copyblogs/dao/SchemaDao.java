package cbp.copyblogs.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/**
 *
 * @author Christian Parker
 * 
 */
public interface SchemaDao {
    
	/**
	 * Get the names of all tables associated with a blog_id (prefix is actually 'prefix' + 'blog_id')
	 * 
	 * @param prefix
	 * @return All tables like 'prefix'
	 */
	@SqlQuery("SELECT DISTINCT table_name FROM tables WHERE table_name LIKE :prefix")
	List<String> getTableNames(@Bind("prefix") String prefix);

}
