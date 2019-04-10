package cbp.copyblogs.dao;

import java.util.List;
import java.util.Map;

import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import cbp.copyblogs.mappers.BlogIdMapper;
import cbp.copyblogs.mappers.CreateTableStatementMapper;
import cbp.copyblogs.mappers.SelectAllMapper;
import cbp.copyblogs.mappers.SelectRowMapper;

/**
 *
 * @author Christian Parker
 * 
 * These are roughly in the order they'll be used.
 */
public interface ApplicationDao {
	
	/**
	 * First we find the blog_id of the path we're looking at
	 * 
	 * @param table The blogs table
	 * @param path Path to the blog
	 * @return
	 */
	@SqlQuery("SELECT blog_id FROM <table> WHERE path = :path")
	@RegisterRowMapper(BlogIdMapper.class)
	Integer getBlogId(@Define("table") String table, @Bind("path") String path);
	
	
	
	/**
	 * Find the greatest blog_id in the table
	 * 
	 * @param table
	 * @return
	 */
	@SqlQuery("SELECT MAX(blog_id) FROM <table>")
	int getMaxBlogId(@Define("table") String table);
	
	
	
	/**
	 * Delete the row in the blogs table that relates to the blog_id
	 * 
	 * @param table
	 * @param blogId
	 */
	@SqlUpdate("DELETE FROM <table> WHERE blog_id = :blogId")
	void deleteRow(@Define("table") String table, @Bind("blogId") String blogId);
	
	
	
	/**
	 * Get the values from the blog_id row to be copied
	 * 
	 * @param table
	 * @param blogId
	 * @return
	 */
	@SqlQuery("SELECT * FROM <table> WHERE blog_id = :blogId")
	@RegisterRowMapper(SelectRowMapper.class)
	String getBlogRowValues(@Define("table") String table, @Bind("blogId") String blogId);
	
	
	
	/**
	 * We need the domain of the destination database
	 * @param table
	 * @return
	 */
	@SqlQuery("SELECT DISTINCT(domain) FROM <table>")
	String getDomain(@Define("table") String table);
	
	
	
	/* The method in SchemaDao executes here to get the names of all tables associated with the blog_id we found */
    

	
	/**
	 * Drop if exists
	 * 
	 * @param table
	 */
	@SqlUpdate("DROP TABLE IF EXISTS <table>")
	void dropTable(@Define("table") String table);
	
	
	
	/**
	 * Get the statement used to create each table 
	 * 
	 * @param table
	 * @return
	 */
	@SqlQuery("SHOW CREATE TABLE <table>")
	@RegisterRowMapper(CreateTableStatementMapper.class)
	String getCreateTableStatement(@Define("table") String table);
	
	
	
	/**
	 * Create the table in the destination database using the create statement we just got
	 * 
	 * @param create_statement
	 */
	@SqlUpdate("<create_statement>")
	void createTable(@Define("create_statement") String create_statement);
	
	
	
	/**
	 * Select all rows from a table
	 * 
	 * @param table
	 * @return
	 */
	@SqlQuery("SELECT * FROM <table>")
	@RegisterRowMapper(SelectAllMapper.class)
	List<List<Object>> selectAll(@Define("table") String table);
	
	
	
	/**
	 * Insert a row into the table created in the destination database
	 * 
	 * @param table
	 * @param values
	 */
	@SqlUpdate("INSERT INTO <table> VALUES (<values>)")
	void insertValues(@Define("table") String table, @BindList("values") List<Object> values);
	
	
	
	/**
	 * Insert the data into the table created in the destination database
	 * 
	 * @param table
	 * @param values
	 */
	@SqlUpdate("INSERT INTO <table> VALUES (<values>)")
	void insertValues(@Define("table") String table, @Define("values") String values);
	
	
	
	/**
	 * Get all rows from the usermeta table that are associated with a certain blog_id
	 * 
	 * @param table
	 * @param prefix
	 * @return
	 */
	@SqlQuery("SELECT * FROM <table> WHERE meta_key LIKE :prefix")
	@RegisterRowMapper(SelectAllMapper.class)
	List<List<Object>> selectAllUserMeta(@Define("table") String table, @Bind("prefix") String prefix);
	
	
	
	/**
	 * Get the ids and user_logins that are associated with the usermeta from {@link #selectAllUserMeta(String, String) selectAllUserMeta}
	 * 
	 * @param table
	 * @param prefix The wordpress table prefix
	 * @param fullPrefix The wordpress table prefix + the blog_id
	 * @return A Hashmap that maps ids to user_logins
	 */
	@SqlQuery("SELECT DISTINCT(<prefix>users.ID), <prefix>users.user_login FROM <prefix>usermeta LEFT JOIN <prefix>users ON <prefix>usermeta.user_id = <prefix>users.ID WHERE <prefix>usermeta.meta_key LIKE :fullPrefix")
	@KeyColumn("ID")
	@ValueColumn("user_login")
	Map<Integer,String> getUserIdsAndLogins(@Define("table") String table, @Define("prefix") String prefix, @Bind("fullPrefix") String fullPrefix);
	
	
	
	/**
	 * Get the id associated with a user_login
	 * 
	 * @param prefix
	 * @param login
	 * @return
	 */
	@SqlQuery("SELECT id FROM <prefix>users WHERE user_login = :login")
	Integer getUserId(@Define("prefix")String prefix, @Bind("login")String login);
	
	
	
	/**
	 * Insert user meta from {@link #selectAllUserMeta(String, String) selectAllUserMeta} after it has been edited for the destination database
	 * 
	 * @param prefix
	 * @param user_id
	 * @param meta_key
	 * @param meta_value
	 */
	@SqlUpdate("INSERT INTO <prefix>usermeta(user_id, meta_key, meta_value) VALUES (:user_id, :meta_key, :meta_value)")
	void insertUserMeta(@Define("prefix")String prefix, @Bind("user_id")String user_id, @Bind("meta_key")String meta_key, @Bind("meta_value")String meta_value);
}
