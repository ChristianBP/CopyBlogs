package cbp.copyblogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cbp.copyblogs.dao.ApplicationDao;
import cbp.copyblogs.dao.SchemaDao;

/**
 * 
 * @author Christian Parker
 *
 * Takes a source blog, a schema for the source blog, and a destination blog, and copies the source to the destination.
 */
public class BlogCopier {
	private static final String CONFIG_LOCATION = "/cbp/copyBlogs/";
	private static Logger logger;

	private SourceBlog sourceBlog;
	private DestinationBlog destinationBlog;
	private WordpressDbConnection sourceSchema;
	
	private Integer sourceBlogId;
	private Integer destinationBlogId;
	
	public BlogCopier(SourceBlog sourceBlog, DestinationBlog destinationBlog, WordpressDbConnection sourceSchema) {
		this.sourceBlog = sourceBlog;
		this.destinationBlog = destinationBlog;
		this.sourceSchema = sourceSchema;
	}

	/**
	 * Copy the source blog to the destination blog
	 * 
	 * @throws Exception
	 */
	public void copy() throws Exception {
		PropertyConfigurator.configure(CONFIG_LOCATION + "log4j.properties");
		BlogCopier.logger = LoggerFactory.getLogger(BlogCopier.class);
		
		try(Handle sourceApplicationHandle = sourceBlog.fetchDataSource();
			Handle destinationApplicationHandle = destinationBlog.fetchDataSource();
			Handle sourceSchemaHandle = sourceSchema.fetchDataSource(); )
		{
			ApplicationDao sourceApplicationDb = sourceApplicationHandle.attach(ApplicationDao.class);
			SchemaDao sourceSchemaDb = sourceSchemaHandle.attach(SchemaDao.class);
			ApplicationDao destinationApplicationDb = destinationApplicationHandle.attach(ApplicationDao.class);
			
			sourceBlogId = sourceBlog.getSourceBlogId(sourceApplicationDb);
			destinationBlogId = destinationBlog.getDestinationBlogId(destinationApplicationDb);
			
			String destinationTable;
			
			if(sourceBlogId == null) {
				logger.error("Couldn't find blog with specified path");
				throw new Exception("Couldn't find blog with specified path");
			}
			else {
				logger.info("Path: " + sourceBlog.getPath());
				logger.info("Source Blog ID: " + sourceBlogId);
				logger.info("Destination Blog ID: " + destinationBlogId);
				logger.info("");
				
				String rowValues =  sourceApplicationDb.getBlogRowValues(sourceBlog.getTablePrefix() + "blogs", Integer.toString(sourceBlogId));
				destinationBlog.replaceRowInBlogsTable(destinationBlogId, destinationApplicationDb, rowValues);
				
				List<String> tables = sourceSchemaDb.getTableNames(sourceSchema.getTablePrefix() + sourceBlogId + "\\_%");
				
				for( String sourceTable : tables ) {
					
					destinationTable = sourceTable.replace(sourceBlog.getTablePrefix() + sourceBlogId, destinationBlog.getTablePrefix() + destinationBlogId);
	
					logger.info("Dropping table: " + destinationTable);
					destinationBlog.dropIfExists(destinationTable, destinationApplicationDb);
					logger.info("Dropped table: " + destinationTable);
					
					logger.info("Creating table: " + destinationTable);
					String createTableStatement = sourceApplicationDb.getCreateTableStatement(sourceTable).replace(sourceTable, destinationTable);
					destinationApplicationDb.createTable(createTableStatement);
					logger.info("Created table: " + destinationTable);
					
					logger.info("Copying values from " + sourceTable + " to " + destinationTable);
					copyValues(sourceTable, destinationTable, sourceApplicationDb, destinationApplicationDb);
					logger.info("Copied values from " + sourceTable + " to " + destinationTable);
					
					logger.info("");
				}
				
				logger.info("Copying usermeta for all tables");
				copyUserMeta(sourceBlogId, destinationBlogId, sourceApplicationDb, destinationApplicationDb);
				logger.info("Usermeta copied");
							
				logger.info("");
			}
		
		}
		catch(Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
		
		
	}

	
	
    /**
     * We've created the table, now lets copy all the rows over one by one.
     * 
     * @param sourceTable
     * @param destinationTable
     * @param sourceApplicationDb the Handle for the sourceDb with the ApplicationDao class attached
     * @param destinationApplicationDb the Handle for the destinationDb with the ApplicationDao class attached
     */
    public void copyValues(String sourceTable, String destinationTable, ApplicationDao sourceApplicationDb, ApplicationDao destinationApplicationDb) {
    	List<List<Object>> tableRows = sourceApplicationDb.selectAll(sourceTable);
    	tableRows = replaceHostName(tableRows);
    	for(List<Object> row : tableRows ) {
    		destinationApplicationDb.insertValues(destinationTable, row);
    	}
    }
	
    
    
	/**
     * We need to copy over all the usermeta for the blog as well
     * <p>
     * The usermeta associated with the blog will always have a meta_key prefixed by 'tablePrefix + blogId'
     * This will only copy the usermeta over for users that exist in both databases.
     * A user is considered to exist in both if they have the same 'user_login' in the 'users' table in both DBs.
     * Once we know they exist in both we determine the user's id in both DBs and then copy over the meta info and swap out the ids.
     * </p>
     * 
     * @param sourceBlogId
     * @param destinationBlogId
     * @param sourceApplicationDb the Handle for the sourceDb with the ApplicationDao class attached
     * @param destinationApplicationDb the Handle for the destinationDb with the ApplicationDao class attached
     */
    private void copyUserMeta(int sourceBlogId, int destinationBlogId, ApplicationDao sourceApplicationDb, ApplicationDao destinationApplicationDb) {
    	List<List<Object>> userMeta = sourceApplicationDb.selectAllUserMeta(sourceBlog.getTablePrefix() + "usermeta", sourceBlog.getTablePrefix() + sourceBlogId + "\\_%");
    	userMeta = replaceHostName(userMeta);
    	Map<Integer,String> userIdLoginMap = sourceApplicationDb.getUserIdsAndLogins(sourceBlog.getTablePrefix() + "usermeta", sourceBlog.getTablePrefix(), sourceBlog.getTablePrefix() + sourceBlogId + "\\_%");
    	
    	String tempMeta;
    	Integer destinationUserId;
    	// Maps valid Ids in the FROM DB to valid Ids in the TO DB
    	Map<Integer,Integer> validIds = new HashMap<>();
    	
    	for(Entry<Integer,String> idLogin : userIdLoginMap.entrySet()) {
    		destinationUserId = destinationApplicationDb.getUserId(destinationBlog.getTablePrefix(), idLogin.getValue());
    		if( destinationUserId != null ) {
    			validIds.put(idLogin.getKey(), destinationUserId);
    		}
    	}
    	
    	for(List<Object> row : userMeta) {

    		if( validIds.keySet().contains( Integer.parseInt(row.get(1).toString()) ) ) {
	    		// 2 is the index of the meta_key which will be something like "prefix_blogId_typeOfMeta"
	    		tempMeta = row.get(2).toString();
	    		row.set(2, tempMeta.replace(sourceBlog.getTablePrefix() + sourceBlogId, destinationBlog.getTablePrefix() + destinationBlogId));
	    		
	    		row.set(1, Integer.toString(validIds.get(Integer.parseInt(row.get(1).toString()))));

	    		destinationApplicationDb.insertUserMeta(destinationBlog.getTablePrefix(), row.get(1).toString(), row.get(2).toString(), row.get(3).toString());
    		}
    	}
    	
    }
    
    
    
	/**
	 * Replace all instances of the old hostname with the new hostname in tableRows
	 * 
	 * @param tableRows
	 * @return
	 */
    private List<List<Object>> replaceHostName(List<List<Object>> tableRows) {
    	
        // If the host names are the same then we don't need to replace anything
        if(sourceBlog.getHostname().equals(destinationBlog.getHostname()))
        	return tableRows;
        
        List<List<Object>> res = new ArrayList<>();
        List<Object> resRow;
        
        for(List<Object> row : tableRows) {
        	resRow = new ArrayList<>();
	        for(Object value : row) {
	        	// The data is not a string and so can't have a hostname
	        	if(value.equals(null) || value.equals(1)) {
	        		resRow.add(value);
	        	}
	        	// We need to replace the hostname in serialized data
	        	else if( value.toString().matches("^.:.*") ) {
	        		while( value.toString().contains(sourceBlog.getHostname()) ) {
	        			value = switchUrls(value.toString(), sourceBlog.getHostname(), destinationBlog.getHostname());
	        		}
	        		resRow.add(value);
	        	}
	        	// Have to replace $blogId_user_roles option in the $blogId_options table
	        	else if(value.toString().equals(sourceBlogId + "_user_roles")) {
	        		resRow.add(destinationBlogId + "_user_roles");
	        	}
	        	// We need to replace the hostname
	        	else {
	        		resRow.add(value.toString().replace(sourceBlog.getHostname(), destinationBlog.getHostname()));
	        	}
	        }
	        res.add(resRow);
        }
        
        return res;
    }    
    
    
    
    /**
     * If this function is being run then we're looking at serialized PHP data.
     * Strings have a number that represents the number of characters in that string.
     * We find the first instance of the oldUrl and then traverse the string backwards
     * until we find an instance of "s:(a number):".
     * Now we change the number by adding the length of the newUrl minus the length of the oldUrl.
     * We return the new string and replace the first instance of the oldUrl with the newUrl so
     * that the string is still properly serialized just with the newUrl in place of the oldUrl.
     * 
     * @param str
     * @param oldUrl
     * @param newUrl
     * @return The string, still properly serialized, with the first instance of oldUrl replaced by newUrl
     */
    private String switchUrls(String str, String oldUrl, String newUrl) {
    	String result = str;
    	int index = str.indexOf(oldUrl);
    	
    	if( index >= 0 ) {
    		
    		int difference = newUrl.length() - oldUrl.length();
    		
    		for( int i = index; i >= 0; i-- ) {
    			
    			if( str.substring(0,i).matches(".*s:\\d+$") ) {
    				
    				String[] split = str.substring(0,i).split(":");
    				
    				split[split.length-1] = Integer.toString(Integer.parseInt(split[split.length-1]) + difference);
    				
    				result = String.join(":", split) + str.substring(i);
    				
    				break;
    			}
    		}
    	}
    	return result.replaceFirst(Pattern.quote(oldUrl), newUrl);
    }
    
    
    
    // GETTERS
	public Integer getSourceBlogId() {
		return sourceBlogId;
	}
	
	public Integer getDestinationBlogId() {
		return destinationBlogId;
	}

	public static String getConfigLocation() {
		return CONFIG_LOCATION;
	}
}
