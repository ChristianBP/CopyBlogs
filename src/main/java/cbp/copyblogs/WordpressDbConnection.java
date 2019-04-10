package cbp.copyblogs;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/** 
 * 
 * @author Christian Parker
 *
 * Contains the information for a wordpress db connection as well as the path of a blog in that db
 */
public class WordpressDbConnection {
	protected final String hostName;
	protected final String path;
	protected final String dbUrl;
	protected final String dbDriver;
	protected final String tablePrefix;
	
	public WordpressDbConnection(String hostName, String path, String dbUrl, String dbDriver, String tablePrefix) {
		this.hostName = hostName;
		this.path = path;
		this.dbUrl = dbUrl;
		this.dbDriver = dbDriver;
		this.tablePrefix = tablePrefix;
	}

	
	
    /**
     * Connect to a wordpress database
     * 
     * @param username
     * @param password
     * @param dbURL
     * @param dbDriver
     * @return
     */
	public Handle fetchDataSource() {    	
        try {
            Class.forName(dbDriver);
        } catch (Exception ex) {
            //logger.error("Error getting jdbc driver:  " + ex);
            throw new RuntimeException("Couldn't get Datasource", ex);
        }
        try {
        	Jdbi dbi = Jdbi.create(dbUrl);
        	dbi.installPlugin(new SqlObjectPlugin());    
            return dbi.open();
        } catch (Exception ex) {
            //logger.error("Error accessing database:  " + ex);
            throw new RuntimeException("Couldn't get Datasource", ex);
        }
	}
	
	// GETTERS
	public String getTablePrefix() {
		return tablePrefix;
	}
	
	public String getHostname() {
		return hostName;
	}

}
