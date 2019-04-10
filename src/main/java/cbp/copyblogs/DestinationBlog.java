package cbp.copyblogs;

import cbp.copyblogs.dao.ApplicationDao;

/**
 * 
 * @author Christian Parker
 *
 * Has some functions that are specific to the destination blog.
 */
public class DestinationBlog extends WordpressDbConnection {
	
	public DestinationBlog(String hostName, String path, String dbUrl, String dbDriver, String tablePrefix) {
		super(hostName, path, dbUrl, dbDriver, tablePrefix);
	}
	
	

    /**
     * If the path exists in the destination wordpress instance, return that blog id. 
     * Else return the highest blog id plus one to be used in creating a new blog.
     * 
     * @param toApplicationDb the Handle for the DestinationDb with the ApplicationDao class attached
     * @return Blog Id of destination blog
     */
    public Integer getDestinationBlogId(ApplicationDao destinationApplicationDb) {
		Integer destinationDbBlogId = destinationApplicationDb.getBlogId(tablePrefix + "blogs", path);
		
    	if( destinationDbBlogId == null ) {
    		destinationDbBlogId = destinationApplicationDb.getMaxBlogId(tablePrefix + "blogs") + 1;
    	}
    	
		return destinationDbBlogId;
    }
    
    
    
    /**
     * The destinationTable might already exist so lets drop it so we can replace it later
     * 
     * @param destinationTable Table to be dropped
     * @param destinationApplicationDb the Handle for the destinationDb with the ApplicationDao class attached
     */
    public void dropIfExists(String destinationTable, ApplicationDao destinationApplicationDb) {
    	destinationApplicationDb.dropTable(destinationTable);
    }


    
    /**
     * If it exists, delete the destination blog's row from 'prefix + "blogs"' table. Then insert a new row for the blog being copied over.
     * 
     * @param destinationBlogId
     * @param destinationApplicationDb the Handle for the toDb with the ApplicationDao class attached
     * @param rowValues the row values being inserted
     */
	public void replaceRowInBlogsTable(Integer destinationBlogId, ApplicationDao destinationApplicationDb, String rowValues) {
		destinationApplicationDb.deleteRow(tablePrefix + "blogs", Integer.toString(destinationBlogId));
    	String values = "'" + destinationBlogId + "', " + rowValues;
    	values = values.replace("DOMAIN", "'" + destinationApplicationDb.getDomain(tablePrefix + "blogs") + "'");
    	destinationApplicationDb.insertValues(tablePrefix + "blogs", values);
	}
}
